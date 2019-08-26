package com.webank.wecube.core.service.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.utils.EncryptionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MysqlDatabaseManagementService implements ResourceItemService {

    @Autowired
    private CmdbResourceService cmdbResourceService;

    @Autowired
    private MysqlAccountManagementService mysqlAccountManagementService;

    public DriverManagerDataSource newMysqlDatasource(String host, String port, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "?characterEncoding=utf8&serverTimezone=UTC");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("CREATE SCHEMA %s", item.getName()));
            mysqlAccountManagementService.createItem(item);
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to delete schema [%s], meet error [%s].", item.getName(), e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage, e);
        }
        return item;
    }

    private DriverManagerDataSource newDatasource(ResourceItem item) {
        String password;
        try {
            password = EncryptionUtils.decryptWithAes(item.getResourceServer().getLoginPassword(), cmdbResourceService.getSeedFromSystemEnum(), item.getResourceServer().getName());
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Failed to decrypt the login password of server [%s].", item.getResourceServer()), e);
        }

        DriverManagerDataSource dataSource = newMysqlDatasource(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                password);
        return dataSource;
    }

    @Override
    public int deleteItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            if (hasTables(connection, item.getName())) {
                throw new WecubeCoreException(String.format("Can not delete database [%s] : Database is not empty.", item.getName()));
            }
            mysqlAccountManagementService.deleteItem(item);
            statement.executeUpdate(String.format("DROP SCHEMA %s", item.getName()));
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to delete schema [%s], meet error [%s].", item.getName(), e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage, e);
        }
        return 1;
    }

    private boolean hasTables(Connection connection, String dbName) {
        boolean hasTable = false;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(String.format("SHOW TABLES FROM %s", dbName));) {
            resultSet.last();
            hasTable = resultSet.getRow() > 0;
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to query tables, meet error [%s].", e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }
        return hasTable;
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }
}
