package com.webank.wecube.platform.core.parser;

import static com.webank.wecube.platform.core.utils.Constants.MYSQL_SCHEMA_NAMING_PATTERN;
import static com.webank.wecube.platform.core.utils.Constants.PACKAGE_NAMING_PATTERN;
import static com.webank.wecube.platform.core.utils.Constants.PACKAGE_VERSION_PATTERN;
import static com.webank.wecube.platform.core.utils.Constants.S3_BUCKET_NAMING_PATTERN;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.service.plugin.xml.register.DockerType;
import com.webank.wecube.platform.core.service.plugin.xml.register.MysqlType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ResourceDependenciesType;
import com.webank.wecube.platform.core.service.plugin.xml.register.S3Type;

public class PluginPackageValidator {
    
    public void validatePackage(PackageType xmlPackage){
        validatePackageName(xmlPackage);
        validatePackageVersionPattern(xmlPackage);
        validateResourceDependencies(xmlPackage);
    }
    
    private void validatePackageName(PackageType xmlPackage){
        if (!Pattern.matches(PACKAGE_NAMING_PATTERN, xmlPackage.getName())) {
            throw new WecubeCoreException("3273",
                    String.format("Invalid plugin package name [%s] - Only alphanumeric and hyphen('-') are allowed. ",
                            xmlPackage.getName()));
        }
    }
    
    private void validatePackageVersionPattern(PackageType xmlPackage){
        if (!Pattern.matches(PACKAGE_VERSION_PATTERN, xmlPackage.getVersion())) {
            throw new WecubeCoreException("3278",String.format("Invalid plugin package version [%s].", xmlPackage.getVersion()));
        }
    }
    
    private void validateResourceDependencies(PackageType xmlPackage){
        ResourceDependenciesType xmlResourceDependencies = xmlPackage.getResourceDependencies();
        if(xmlResourceDependencies == null){
            return;
        }
        
        validateS3(xmlResourceDependencies);
        validateMysql(xmlResourceDependencies);
        validateDocker(xmlResourceDependencies);
    }
    
    private void validateS3(ResourceDependenciesType xmlResourceDependencies){
        List<S3Type> xmlS3List = xmlResourceDependencies.getS3();
        if(xmlS3List == null || xmlS3List.isEmpty()){
            return;
        }
        
        for(S3Type xmlS3Type : xmlS3List){
            if (!Pattern.matches(S3_BUCKET_NAMING_PATTERN, xmlS3Type.getBucketName())) {
                throw new WecubeCoreException("3274",String.format("Invalid bucket name [%s] - Only alphanumeric and hyphen('-') are allowed with length {3,63}.", xmlS3Type.getBucketName()));
            }
        }
    }
    
    private void validateMysql(ResourceDependenciesType xmlResourceDependencies){
        List<MysqlType> xmlMysqlList = xmlResourceDependencies.getMysql();
        if(xmlMysqlList == null || xmlMysqlList.isEmpty()){
            return;
        }
        
        for(MysqlType xmlMysql : xmlMysqlList){
            if (!Pattern.matches(MYSQL_SCHEMA_NAMING_PATTERN, xmlMysql.getSchema())) {
                throw new WecubeCoreException("3275",String.format("Invalid bucket name [%s] - Only alphanumeric and underscore('_') are allowed with length {3,64}.", xmlMysql.getSchema()));
            }
        }
    }
    
    private void validateDocker(ResourceDependenciesType xmlResourceDependencies){
        List<DockerType> xmlDockerList = xmlResourceDependencies.getDocker();
        if(xmlDockerList == null || xmlDockerList.isEmpty()){
            return;
        }
        
        for(DockerType xmlDocker : xmlDockerList){
            String portBindings = xmlDocker.getPortBindings();
            if (StringUtils.isNotEmpty(portBindings) && portBindings.indexOf(":") < 0) {
                throw new WecubeCoreException("3276",String.format("portBindings attribute [%s] for docker should contains semi-colon (':')", portBindings));
            }
            String volumeBindings = xmlDocker.getVolumeBindings();
            if (StringUtils.isNotEmpty(volumeBindings) && volumeBindings.indexOf(":") < 0) {
                throw new WecubeCoreException("3277",String.format("volumeBindings attribute [%s] for docker should contains semi-colon (':')", volumeBindings));
            }
        }
    }
}
