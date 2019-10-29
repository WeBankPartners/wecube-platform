package com.webank.wecube.platform.core.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.PackageDomain;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiDataDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiDataTreeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery.Dialect;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery.Sorting;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQueryResult;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.DefaultPluginRequest;
import com.webank.wecube.platform.core.support.S3Client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Setter
@Slf4j
public class ArtifactService {
    private static final String CONSTANT_FIX_DATE = "fixed_date";
    private static final String S3_BUCKET_NAME_FOR_ARTIFACT = "wecube-artifact";
    private static final String S3_KEY_DELIMITER = "_";

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    private PluginInstanceService pluginInstanceService;

    @Autowired
    CmdbDataProperties cmdbDataProperties;

    @Autowired
    PluginProperties pluginProperties;

    @Autowired
    ApplicationProperties.S3Properties s3Properties;

    @Autowired
    private S3Client s3Client;

    public String uploadPackageToS3(File file) {
        if (file == null) {
            throw new WecubeCoreException("Upload package file is required.");
        }

        String s3Key = genMd5Value(file) + S3_KEY_DELIMITER + file.getName();
        String url = s3Client.uploadFile(S3_BUCKET_NAME_FOR_ARTIFACT, s3Key, file);
        return url.substring(0, url.indexOf("?"));
    }

    public List<CiDataDto> savePackageToCmdb(File file, String unitDesignId, String uploadUser, String url) {
        Map<String, Object> pkg = ImmutableMap.<String, Object>builder().put("name", file.getName()).put("url", url)
                .put("md5_value", genMd5Value(file)).put("description", file.getName()).put("upload_user", uploadUser)
                .put("upload_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .put("unit_design", unitDesignId).build();

        return cmdbServiceV2Stub.createCiData(cmdbDataProperties.getCiTypeIdOfPackage(), pkg);
    }

    public void deactive(String packageId) {
        updateState(packageId, cmdbDataProperties.getEnumCodeDestroyedOfCiStateOfCreate());
    }

    public void active(String packageId) {
        updateState(packageId, cmdbDataProperties.getEnumCodeChangeOfCiStateOfCreate());
    }

    public void saveConfigFiles(String packageId, PackageDomain packageDomain) {
        String files = String.join("|", packageDomain.getConfigFilesWithPath());
        Map<String, Object> pkg = ImmutableMap.<String, Object>builder().put("guid", packageId)
                .put("deploy_file", packageDomain.getDeployFile()).put("start_file", packageDomain.getStartFile())
                .put("stop_file", packageDomain.getStopFile()).put("diff_conf_file", files).build();
        cmdbServiceV2Stub.updateCiData(cmdbDataProperties.getCiTypeIdOfPackage(), pkg);
    }

    public Object getCurrentDirs(String packageId, String currentDir) {
        List<PluginInstance> instances = pluginInstanceService
                .getRunningPluginInstances(pluginProperties.getPluginPackageNameOfDeploy());

        DefaultPluginRequest request = new DefaultPluginRequest();
        List<Map<String, Object>> inputs = new ArrayList<>();
        inputs.add(
                ImmutableMap.<String, Object>builder().put("endpoint", retrieveS3EndpointWithKeyByPackageId(packageId))
                        .put("accessKey", s3Properties.getAccessKey()).put("secretKey", s3Properties.getSecretKey())
                        .put("currentDir", currentDir).build());
        request.setInputs(inputs);

        return pluginServiceStub.getPluginReleasedPackageFilesByCurrentDir(
                pluginInstanceService.getInstanceAddress(instances.get(0)), request);
    }

    public Object getPropertyKeys(String packageId, String filePath) {
        List<PluginInstance> instances = pluginInstanceService
                .getRunningPluginInstances(pluginProperties.getPluginPackageNameOfDeploy());

        DefaultPluginRequest request = new DefaultPluginRequest();
        List<Map<String, Object>> inputs = new ArrayList<>();
        inputs.add(
                ImmutableMap.<String, Object>builder().put("endpoint", retrieveS3EndpointWithKeyByPackageId(packageId))
                        .put("accessKey", s3Properties.getAccessKey()).put("secretKey", s3Properties.getSecretKey())
                        .put("file_path", filePath).build());
        request.setInputs(inputs);

        return pluginServiceStub.getPluginReleasedPackagePropertyKeysByFilePath(
                pluginInstanceService.getInstanceAddress(instances.get(0)), request);
    }

    private String retrieveS3EndpointWithKeyByPackageId(String packageId) {
        PaginationQuery queryObject = PaginationQuery.defaultQueryObject().addEqualsFilter("guid", packageId);
        PaginationQueryResult<Object> result = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfPackage(),
                queryObject);
        if (result == null || result.getContents().isEmpty()) {
            throw new WecubeCoreException(String.format("Package with ID [%s] not found.", packageId));
        }

        Map pkgData = (Map) result.getContents().get(0);
        Map pkg = (Map) pkgData.get("data");
        String s3Key = pkg.get("md5_value") + S3_KEY_DELIMITER + pkg.get("name");
        String endpointWithKey = s3Properties.getEndpoint() + "/" + S3_BUCKET_NAME_FOR_ARTIFACT + "/" + s3Key;
        return endpointWithKey;
    }

    private void updateState(String packageId, String operation) {
        List<OperateCiDto> operateCiDtos = new ArrayList<>();
        operateCiDtos.add(new OperateCiDto(packageId, cmdbDataProperties.getCiTypeIdOfPackage()));
        cmdbServiceV2Stub.operateCiForState(operateCiDtos, operation);
    }

    private String genMd5Value(File file) {
        if (file == null) {
            return null;
        }

        String md5Value = null;

        try {
            md5Value = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file));
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Fail to generateMd5 value for file [%s]", file.getName()), e);
        }
        return md5Value;
    }

    public Object getArtifactSystemDesignTree(String systemDesignId) {
        List<CiDataTreeDto> tree = new ArrayList<>();
        PaginationQuery queryObject = new PaginationQuery();
        Dialect dialect = new Dialect();
        dialect.setShowCiHistory(true);
        queryObject.setDialect(dialect);
        queryObject.addEqualsFilter("guid", systemDesignId);
        PaginationQueryResult<Object> ciData = cmdbServiceV2Stub
                .queryCiData(cmdbDataProperties.getCiTypeIdOfSystemDesign(), queryObject);

        if (ciData == null || ciData.getContents() == null || ciData.getContents().isEmpty()) {
            throw new WecubeCoreException(String.format("Can not find ci data for guid [%s]", systemDesignId));
        }

        Object fixedDate = ((Map) ((Map) ciData.getContents().get(0)).get("data")).get(CONSTANT_FIX_DATE);
        if (fixedDate != null) {
            List<CiDataTreeDto> dtos = cmdbServiceV2Stub.getCiDataDetailForVersion(
                    cmdbDataProperties.getCiTypeIdOfSystemDesign(), cmdbDataProperties.getCiTypeIdOfUnitDesign(),
                    fixedDate.toString());

            dtos.forEach(dto -> {
                if (systemDesignId.equals(((Map) dto.getData()).get("guid"))) {
                    tree.add(dto);
                }
            });
        }
        return tree;
    }

    public PaginationQueryResult<Object> getSystemDesignVersions() {
        PaginationQueryResult<Object> queryResult = new PaginationQueryResult<>();

        PaginationQuery queryObject = new PaginationQuery();
        Dialect dialect = new Dialect();
        dialect.setShowCiHistory(true);
        queryObject.setDialect(dialect);
        queryObject.addNotNullFilter(CONSTANT_FIX_DATE);
        queryObject.addNotEqualsFilter(CONSTANT_FIX_DATE, "");
        queryObject.setSorting(new Sorting(false, CONSTANT_FIX_DATE));

        PaginationQueryResult<Object> ciDatas = cmdbServiceV2Stub
                .queryCiData(cmdbDataProperties.getCiTypeIdOfSystemDesign(), queryObject);

        queryResult.setContents(extractedLatestVersionSystemDesigns(ciDatas));

        return queryResult;
    }

    private List<Object> extractedLatestVersionSystemDesigns(PaginationQueryResult<Object> ciDatas) {
        List<Object> finalCiDatas = new ArrayList<>();
        ciDatas.getContents().forEach(ciData -> {
            if (ciData instanceof Map) {
                Map map = (Map) ciData;
                if (!isExist(finalCiDatas, map.get("data"))) {
                    finalCiDatas.add(ciData);
                }
            }
        });
        return finalCiDatas;
    }

    private boolean isExist(List<Object> results, Object systemName) {
        for (Object result : results) {
            Map m = (Map) result;
            Object existName = ((Map) m.get("data")).get("name");
            Object newName = ((Map) systemName).get("name");
            if (existName != null && existName.equals(newName)) {
                return true;
            }
        }
        return false;
    }

    public void saveDiffConfigEnumCodes(CatCodeDto requestCode) {
        CategoryDto cat = cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryNameOfDiffConf());
        if (cat == null) {
            throw new WecubeCoreException(String.format("Can not find cat with name [%s].",
                    cmdbDataProperties.getEnumCategoryNameOfDiffConf()));
        }

        CatCodeDto code = new CatCodeDto();
        code.setCatId(cat.getCatId());
        code.setCode(requestCode.getCode());
        code.setValue(requestCode.getValue());
        cmdbServiceV2Stub.createEnumCodes(code);
    }

    public List<CatCodeDto> getDiffConfigEnumCodes() {
        CategoryDto cat = cmdbServiceV2Stub.getEnumCategoryByName(cmdbDataProperties.getEnumCategoryNameOfDiffConf());
        if (cat == null) {
            throw new WecubeCoreException(String.format("Can not find cat with name [%s].",
                    cmdbDataProperties.getEnumCategoryNameOfDiffConf()));
        }
        return cmdbServiceV2Stub.getEnumCodesByCategoryId(cat.getCatId());
    }
}
