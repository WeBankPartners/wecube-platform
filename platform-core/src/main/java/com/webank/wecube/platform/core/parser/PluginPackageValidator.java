package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesS3;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

import static com.webank.wecube.platform.core.utils.Constants.*;

public class PluginPackageValidator {

    public void validate(PluginPackage pluginPackage) {
        if (!Pattern.matches(PACKAGE_NAMING_PATTERN, pluginPackage.getName())) {
            throw new WecubeCoreException("3273",
                    String.format("Invalid plugin package name [%s] - Only alphanumeric and hyphen('-') are allowed. ",
                            pluginPackage.getName()));
        }
        validatePackageVersion(pluginPackage.getVersion());

        Set<PluginPackageRuntimeResourcesS3> pluginPackageRuntimeResourcesS3s = pluginPackage.getPluginPackageRuntimeResourcesS3();
        if (null != pluginPackageRuntimeResourcesS3s && pluginPackageRuntimeResourcesS3s.size() > 1) {
            PluginPackageRuntimeResourcesS3 pluginPackageRuntimeResourcesS3 = pluginPackageRuntimeResourcesS3s.iterator().next();
            if (!Pattern.matches(S3_BUCKET_NAMING_PATTERN, pluginPackageRuntimeResourcesS3.getBucketName())) {
                throw new WecubeCoreException("3274",String.format("Invalid bucket name [%s] - Only alphanumeric and hyphen('-') are allowed with length {3,63}.", pluginPackageRuntimeResourcesS3.getBucketName()));
            }
        }
        Set<PluginPackageRuntimeResourcesMysql> pluginPackageRuntimeResourcesMysqls = pluginPackage.getPluginPackageRuntimeResourcesMysql();
        if (null != pluginPackageRuntimeResourcesMysqls && pluginPackageRuntimeResourcesMysqls.size() > 0) {
            PluginPackageRuntimeResourcesMysql packageRuntimeResourcesMysql = pluginPackageRuntimeResourcesMysqls.iterator().next();
            if (!Pattern.matches(MYSQL_SCHEMA_NAMING_PATTERN, packageRuntimeResourcesMysql.getSchemaName())) {
                throw new WecubeCoreException("3275",String.format("Invalid bucket name [%s] - Only alphanumeric and underscore('_') are allowed with length {3,64}.", packageRuntimeResourcesMysql.getSchemaName()));
            }
        }
        Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDockers = pluginPackage.getPluginPackageRuntimeResourcesDocker();
        if (null != pluginPackageRuntimeResourcesDockers && pluginPackageRuntimeResourcesDockers.size() > 0) {
            PluginPackageRuntimeResourcesDocker runtimeResourcesDocker = pluginPackageRuntimeResourcesDockers.iterator().next();
            String portBindings = runtimeResourcesDocker.getPortBindings();
            if (StringUtils.isNotEmpty(portBindings) && portBindings.indexOf(":") < 0) {
                throw new WecubeCoreException("3276",String.format("portBindings attribute [%s] for docker should contains semi-colon (':')", portBindings));
            }
            String volumeBindings = runtimeResourcesDocker.getVolumeBindings();
            if (StringUtils.isNotEmpty(volumeBindings) && volumeBindings.indexOf(":") < 0) {
                throw new WecubeCoreException("3277",String.format("volumeBindings attribute [%s] for docker should contains semi-colon (':')", volumeBindings));
            }
        }
    }

    public static void validatePackageVersion(String packageVersion) {
        if (!Pattern.matches(PACKAGE_VERSION_PATTERN, packageVersion)) {
            throw new WecubeCoreException("3278",String.format("Invalid plugin package version [%s].", packageVersion));
        }
    }
}
