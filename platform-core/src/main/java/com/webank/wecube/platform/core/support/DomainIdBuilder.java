package com.webank.wecube.platform.core.support;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.domain.BatchExecutionJob;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class DomainIdBuilder {

    public static String buildDomainId(String... fields) {
        return buildDomainId(Lists.newArrayList(fields));
    }

    public static String buildDomainId(List<String> fields) {
        if (fields != null && fields.size() > 0) {
            return fields.stream()
                    .filter(field-> StringUtils.isNotBlank(field))
                    .map(field->field.replaceAll("\\s+", "_"))
                    .collect(Collectors.joining(Constants.KEY_COLUMN_DELIMITER));
        }
        return null;
    }

    public static String buildDomainId(PluginPackage pluginPackage) {
        return StringUtils.isNotBlank(pluginPackage.getId())
                ? pluginPackage.getId()
                : buildDomainId(pluginPackage.getName(), pluginPackage.getVersion()
        );
    }

    public static String buildDomainId(PluginPackageDependency pluginPackageDependency) {
        return StringUtils.isNotBlank(pluginPackageDependency.getId())
                ? pluginPackageDependency.getId()
                : buildDomainId(
                null != pluginPackageDependency.getPluginPackage() ? pluginPackageDependency.getPluginPackage().getName() : null,
                null != pluginPackageDependency.getPluginPackage() ? pluginPackageDependency.getPluginPackage().getVersion() : null,
                pluginPackageDependency.getDependencyPackageName(),
                pluginPackageDependency.getDependencyPackageVersion()
        );
    }

    public static String buildDomainId(PluginPackageMenu pluginPackageMenu) {
        return StringUtils.isNotBlank(pluginPackageMenu.getId())
                ? pluginPackageMenu.getId()
                : buildDomainId(
                null != pluginPackageMenu.getPluginPackage() ? pluginPackageMenu.getPluginPackage().getName() : null,
                null != pluginPackageMenu.getPluginPackage() ? pluginPackageMenu.getPluginPackage().getVersion() : null,
                pluginPackageMenu.getCategory(),
                pluginPackageMenu.getCode()
        );
    }

    public static String buildDomainId(PluginPackageDataModel pluginPackageDataModel) {
        return StringUtils.isNotBlank(pluginPackageDataModel.getId())
                ? pluginPackageDataModel.getId()
                : buildDomainId(
                "DataModel",
                pluginPackageDataModel.getPackageName(),
                String.valueOf(pluginPackageDataModel.getVersion())
        );
    }

    public static String buildDomainId(PluginPackageEntity pluginPackageEntity) {
        return StringUtils.isNotBlank(pluginPackageEntity.getId())
                ? pluginPackageEntity.getId()
                : buildDomainId(
                pluginPackageEntity.getPackageName(),
                String.valueOf(pluginPackageEntity.getDataModelVersion()),
                pluginPackageEntity.getName()
        );
    }

    public static String buildDomainId(PluginPackageAttribute pluginPackageAttribute) {
        return StringUtils.isNotBlank(pluginPackageAttribute.getId())
                ? pluginPackageAttribute.getId()
                : buildDomainId(
                null != pluginPackageAttribute.getPluginPackageEntity() ? pluginPackageAttribute.getPluginPackageEntity().getPackageName() : null,
                null != pluginPackageAttribute.getPluginPackageEntity() ? String.valueOf(pluginPackageAttribute.getPluginPackageEntity().getDataModelVersion()) : null,
                null != pluginPackageAttribute.getPluginPackageEntity() ? pluginPackageAttribute.getPluginPackageEntity().getName() : null,
                pluginPackageAttribute.getName()
        );
    }

    public static String buildDomainId(SystemVariable systemVariable) {
        return StringUtils.isNotBlank(systemVariable.getId())
                ? systemVariable.getId()
                : buildDomainId(
                systemVariable.getSource(),
                systemVariable.getScope(),
                systemVariable.getName()
        );
    }

    public static String buildDomainId(PluginPackageAuthority pluginPackageAuthority) {
        return StringUtils.isNotBlank(pluginPackageAuthority.getId())
                ? pluginPackageAuthority.getId()
                : buildDomainId(
                null != pluginPackageAuthority.getPluginPackage() ? pluginPackageAuthority.getPluginPackage().getName() : null,
                null != pluginPackageAuthority.getPluginPackage() ? pluginPackageAuthority.getPluginPackage().getVersion() : null,
                pluginPackageAuthority.getRoleName(),
                pluginPackageAuthority.getMenuCode()
        );
    }

    public static String buildDomainId(PluginPackageRuntimeResourcesDocker runtimeResourcesDocker) {
        return StringUtils.isNotBlank(runtimeResourcesDocker.getId())
                ? runtimeResourcesDocker.getId()
                : buildDomainId(
                "Docker",
                null != runtimeResourcesDocker.getPluginPackage() ? runtimeResourcesDocker.getPluginPackage().getName() : null,
                null != runtimeResourcesDocker.getPluginPackage() ? runtimeResourcesDocker.getPluginPackage().getVersion() : null,
                runtimeResourcesDocker.getImageName()
        );
    }

    public static String buildDomainId(PluginPackageRuntimeResourcesMysql runtimeResourcesMysql) {
        return StringUtils.isNotBlank(runtimeResourcesMysql.getId())
                ? runtimeResourcesMysql.getId()
                : buildDomainId(
                "MySql",
                null != runtimeResourcesMysql.getPluginPackage() ? runtimeResourcesMysql.getPluginPackage().getName() : null,
                null != runtimeResourcesMysql.getPluginPackage() ? runtimeResourcesMysql.getPluginPackage().getVersion() : null,
                runtimeResourcesMysql.getSchemaName()
        );
    }

    public static String buildDomainId(PluginPackageRuntimeResourcesS3 runtimeResourcesS3) {
        return StringUtils.isNotBlank(runtimeResourcesS3.getId())
                ? runtimeResourcesS3.getId()
                : buildDomainId(
                "S3",
                null != runtimeResourcesS3.getPluginPackage() ? runtimeResourcesS3.getPluginPackage().getName() : null,
                null != runtimeResourcesS3.getPluginPackage() ? runtimeResourcesS3.getPluginPackage().getVersion() : null,
                runtimeResourcesS3.getBucketName()
        );
    }

    public static String buildDomainId(PluginConfig pluginConfig) {
        return StringUtils.isNotBlank(pluginConfig.getId())
                ? pluginConfig.getId()
                : buildDomainId(
                null != pluginConfig.getPluginPackage() ? pluginConfig.getPluginPackage().getName() : null,
                null != pluginConfig.getPluginPackage() ? pluginConfig.getPluginPackage().getVersion() : null,
                pluginConfig.getName(),
                pluginConfig.getRegisterName()
        );
    }

    public static String buildDomainId(PluginConfigInterface pluginConfigInterface) {
        return StringUtils.isNotBlank(pluginConfigInterface.getId())
                ? pluginConfigInterface.getId()
                : buildDomainId(
                null != pluginConfigInterface.getPluginConfig() ? (null != pluginConfigInterface.getPluginConfig().getPluginPackage() ? pluginConfigInterface.getPluginConfig().getPluginPackage().getName() : null) : null,
                null != pluginConfigInterface.getPluginConfig() ? (null != pluginConfigInterface.getPluginConfig().getPluginPackage() ? pluginConfigInterface.getPluginConfig().getPluginPackage().getVersion() : null) : null,
                null != pluginConfigInterface.getPluginConfig() ? pluginConfigInterface.getPluginConfig().getName() : null,
                null != pluginConfigInterface.getPluginConfig() ? pluginConfigInterface.getPluginConfig().getRegisterName() : null,
                pluginConfigInterface.getAction(),
                null != pluginConfigInterface.getPluginConfig() ? pluginConfigInterface.getPluginConfig().getTargetEntity() : null
        );
    }

    public static String buildDomainId(PluginConfigInterfaceParameter interfaceParameter) {
        return StringUtils.isNotBlank(interfaceParameter.getId())
                ? interfaceParameter.getId()
                : buildDomainId(
                null != interfaceParameter.getPluginConfigInterface() ? interfaceParameter.getPluginConfigInterface().getId() : null,
                interfaceParameter.getType(),
                interfaceParameter.getName()
        );
    }

    public static String buildDomainId(PluginInstance pluginInstance) {
        return StringUtils.isNotBlank(pluginInstance.getId())
                ? pluginInstance.getId()
                : buildDomainId(
                pluginInstance.getContainerName(),
                pluginInstance.getHost(),
                String.valueOf(pluginInstance.getPort())
        );
    }

    public static String buildDomainId(PluginMysqlInstance mysqlInstance) {
        return StringUtils.isNotBlank(mysqlInstance.getId())
                ? mysqlInstance.getId()
                : buildDomainId(
                null != mysqlInstance.getPluginPackage() ? mysqlInstance.getPluginPackage().getId() : null,
                mysqlInstance.getSchemaName(),
                mysqlInstance.getUsername()
        );
    }

    public static String buildDomainId(MenuItem menuItem) {
        return StringUtils.isNotBlank(menuItem.getId())
                ? menuItem.getId()
                : buildDomainId(
                menuItem.getParentCode(),
                menuItem.getCode()
        );
    }

    public static String buildDomainId(ResourceServer resourceServer) {
        return StringUtils.isNotBlank(resourceServer.getId())
                ? resourceServer.getId()
                : buildDomainId(
                resourceServer.getHost(),
                resourceServer.getType(),
                resourceServer.getName()
        );
    }

    public static String buildDomainId(ResourceItem resourceItem) {
        return StringUtils.isNotBlank(resourceItem.getId())
                ? resourceItem.getId()
                : buildDomainId(
                null != resourceItem.getResourceServerId() ? resourceItem.getResourceServerId(): null,
                resourceItem.getType(),
                resourceItem.getName()
        );
    }

    public static String buildDomainId(BatchExecutionJob batchExecutionJob) throws ParseException {
        return StringUtils.isNotBlank(batchExecutionJob.getId()) ? batchExecutionJob.getId()
                : buildDomainId(Long.toString(System.currentTimeMillis()));
    }

}
