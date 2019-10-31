package com.webank.wecube.platform.core.service.resource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Strings;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;
import com.webank.wecube.platform.core.dto.PageInfo;
import com.webank.wecube.platform.core.dto.QueryResponse;
import com.webank.wecube.platform.core.dto.ResourceQueryRequest;
import com.webank.wecube.platform.core.dto.SqlQueryRequest;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginMysqlInstanceRepository;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.EncryptionUtils;

@Service
public class ResourceDataQueryService {
    private Logger logger = LoggerFactory.getLogger(ResourceDataQueryService.class);
    
    @Autowired
    private PluginMysqlInstanceRepository pluginMysqlInstanceRepository;
    @Autowired
    private ResourceProperties resourceProperties;
    @Autowired
    private MysqlAccountManagementService mysqlAcctMngService;
    
    @Autowired
    private S3Client s3client;
    
    @Autowired
    private PluginInstanceRepository pluginInstanceRepository;
    
    //for test
    @Autowired
    private DataSource dataSource;
    
    
    public QueryResponse<List<String>> queryDB(int packageId, SqlQueryRequest sqlQueryRequest){
        DataSource dataSource = getDataSource(packageId);
        return queryDB(dataSource,sqlQueryRequest);
    }
    
    public QueryResponse<List<String>> queryDB(DataSource dataSource, SqlQueryRequest sqlQueryRequest){
        List<List<String>> results = new LinkedList<>(); 
        
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            int totalCount = queryTotalCount(statement,sqlQueryRequest.getSqlQuery());
            
            String limitedSql = getLimitedSql(sqlQueryRequest); 
            ResultSet rs = statement.executeQuery(limitedSql);
            ResultSetMetaData resultSetMd = rs.getMetaData();
            int columnCount = resultSetMd.getColumnCount();
            
            List<String> headers = getHeaders(resultSetMd);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            while(rs.next()) {
                List<String> rowValue = new ArrayList<>(columnCount);
                for(int col=1;col<=columnCount;col++) {
                    int colType = resultSetMd.getColumnType(col);
                    String literalVal = null;
                    switch(colType) {
                    case Types.DATE:
                        Date date = rs.getDate(col);
                        if(date != null) {
                            literalVal = dateFormat.format(date);
                        }
                        break;
                    case Types.TIMESTAMP:
                        Timestamp timestamp = rs.getTimestamp(col);
                        if(timestamp != null) {
                            literalVal = datetimeFormat.format(timestamp);
                        }
                        break;
                    default:
                        literalVal = rs.getString(col);
                        break;
                    }
                    rowValue.add(literalVal);
                }
                results.add(rowValue);
            }
            QueryResponse<List<String>> response = null;
            if(sqlQueryRequest.getPageable() != null) {
                response = new QueryResponse<>(new PageInfo(totalCount,sqlQueryRequest.getPageable().getStartIndex(),sqlQueryRequest.getPageable().getPageSize()),
                        results,headers);
            }else {
                response = new QueryResponse<>(new PageInfo(totalCount,0,totalCount),results,headers);
            }
            return response;
            
        }catch(Exception e) {
            String errorMessage = String.format("Fail to execute sql query:%s", sqlQueryRequest.toString());
            logger.error(errorMessage, e);
            throw new WecubeCoreException(errorMessage, e);
        }    
    }

    private List<String> getHeaders(ResultSetMetaData resultSetMd) throws SQLException {
        List<String> headers = new ArrayList<>(resultSetMd.getColumnCount());
        for(int i=1;i<=resultSetMd.getColumnCount();i++) {
            headers.add(resultSetMd.getColumnLabel(i));
        }
        return headers;
    }


    private String getLimitedSql(SqlQueryRequest sqlQueryRequest) {
        if(sqlQueryRequest.getPageable() != null) {
            int startIndex = sqlQueryRequest.getPageable().getStartIndex();
            int pageSize = sqlQueryRequest.getPageable().getPageSize();
            StringBuilder limitedSqlBuilder = new StringBuilder();
            limitedSqlBuilder.append("select * from (")
                .append(sqlQueryRequest.getSqlQuery())
                .append(") alias limit ")
                .append(startIndex)
                .append(",")
                .append(pageSize);
            return limitedSqlBuilder.toString();
        }else {
            return sqlQueryRequest.getSqlQuery();
        }
    }


    private int queryTotalCount(Statement statement, String sqlQuery) throws SQLException {
        StringBuilder countSqlBuilder = new StringBuilder();
        countSqlBuilder.append("select count(1) from ")
            .append("(")
            .append(sqlQuery)
            .append(") alias");
        ResultSet rs = statement.executeQuery(countSqlBuilder.toString());
        if(rs.first()) {
            int totalCount = rs.getInt(1);
            return totalCount;
        }else {
            throw new WecubeCoreException(String.format("Failed to get total count of query: %s",sqlQuery));
        }
    }


    private DataSource getDataSource(int packageId) {
        PluginMysqlInstance pluginMysqlInstance = pluginMysqlInstanceRepository.findByPluginPackageId(packageId);
        if(pluginMysqlInstance == null) {
            throw new WecubeCoreException(String.format("Can not find out PluginMysqlInstance for package id:%d",packageId));
        }
        
        String dbUsername = pluginMysqlInstance.getUsername();
        String password = EncryptionUtils.decryptWithAes(pluginMysqlInstance.getPassword(),resourceProperties.getPasswordEncryptionSeed(), dbUsername);
        
        ResourceItem resourceItem = pluginMysqlInstance.getResourceItem();
        if(resourceItem == null) {
            throw new WecubeCoreException(String.format("Can not find out ResourceItem for packageId:%d", packageId));
        }
        
        ResourceServer resourceServer = resourceItem.getResourceServer();
        if(resourceServer == null) {
            throw new WecubeCoreException(String.format("Can not find out mysql ResourceServer for packageId:%d", packageId));
        }
        
        String mysqlHost = resourceServer.getHost();
        String mysqlPort = resourceServer.getPort();
        return mysqlAcctMngService.newMysqlDatasource(mysqlHost, mysqlPort, dbUsername, password);
    }

    public List<List<String>> queryS3Files(int packageId) {
        List<PluginInstance> pluginInstances = pluginInstanceRepository.findByPackageId(packageId);
        if(pluginInstances == null || pluginInstances.size()==0) {
            throw new WecubeCoreException(String.format("Can not find out plugin instance for packageId:%d", packageId));
        }
        
        String bucketName = null;
        for(PluginInstance ps:pluginInstances) {
            if(ps.getS3ResourceItem() != null) {
                bucketName = ps.getS3ResourceItem().getName();
                break;
            }
        }
        
        if(Strings.isNullOrEmpty(bucketName)) {
            throw new WecubeCoreException(String.format("Can not find out bucket name for packageId:%d", packageId));
        }
        
        List<S3ObjectSummary> s3Objs = s3client.listObjects(bucketName);
        List<List<String>> response = new LinkedList<>();
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(S3ObjectSummary s3ObjSum:s3Objs) {
            List<String> rowVal = new ArrayList<>(4);
            String key = s3ObjSum.getKey();
            int lastSplitPos = key.lastIndexOf("/");
            String path = "";
            String fileName = "";
            if(lastSplitPos > 0) {
                path = key.substring(0,lastSplitPos+1);
                fileName = key.substring(lastSplitPos+1);
            }else {
                path = "/";
                fileName = key;
            }
            rowVal.add(fileName);
            rowVal.add(path);
            rowVal.add(s3ObjSum.getETag());
            rowVal.add(datetimeFormat.format(s3ObjSum.getLastModified()));
            response.add(rowVal);
        }
        return response;
    }
    
}
