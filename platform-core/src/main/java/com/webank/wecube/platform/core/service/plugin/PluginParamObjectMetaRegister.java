package com.webank.wecube.platform.core.service.plugin;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectMetaRegister extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectMetaRegister.class);

    /**
     * 
     * @param xmlParamObjects
     * @param packageName
     * @param packageVersion
     * @param configId
     */
    public void registerParamObjects(ParamObjectsType xmlParamObjects, String packageName, String packageVersion,
            String configId) {
        log.info("try to register param objects for {} {}", packageName, packageVersion);
        if (xmlParamObjects == null) {
            return;
        }

        List<ParamObjectType> xmlParamObjectList = xmlParamObjects.getParamObject();
        if (xmlParamObjectList == null || xmlParamObjectList.isEmpty()) {
            return;
        }

        log.info("total {} param objects to register for {} {}", xmlParamObjectList.size(), packageName,
                packageVersion);

        for (ParamObjectType xmlParamObject : xmlParamObjectList) {
            tryRegisterSingleParamObject(xmlParamObject, packageName, packageVersion, configId);
        }

    }

    private void tryRegisterSingleParamObject(ParamObjectType xmlParamObject, String packageName, String packageVersion,
            String configId) {
        log.info("try register param object {}", xmlParamObject.getName());
        String paramObjectName = xmlParamObject.getName();
        CoreObjectMeta objectMetaEntity = coreObjectMetaMapper.selectOneByPackageNameAndObjectNameAndConfig(packageName,
                paramObjectName, configId);

        if (objectMetaEntity == null) {
            tryCreateSingleParamObject(xmlParamObject, packageName, packageVersion, configId);
            return;
        } else {
            tryUpdateSingleParamObject(xmlParamObject, packageName, packageVersion, objectMetaEntity);
            return;
        }

    }

    private void tryUpdateSingleParamObject(ParamObjectType xmlParamObject, String packageName, String packageVersion,
            CoreObjectMeta objectMetaEntity) {
        List<ParamPropertyType> xmlPropertyList = xmlParamObject.getProperty();
        if (xmlPropertyList == null) {
            return;
        }

        List<CoreObjectPropertyMeta> propertyMetas = coreObjectPropertyMetaMapper
                .selectAllByObjectMeta(objectMetaEntity.getId());

        for (ParamPropertyType xmlPropertyType : xmlPropertyList) {
            CoreObjectPropertyMeta propertyMeta = findOutObjectPropertyMetaByPropertyName(propertyMetas,
                    xmlPropertyType.getName());
            if (propertyMeta == null) {
                CoreObjectPropertyMeta propertyMetaEntity = new CoreObjectPropertyMeta();
                propertyMetaEntity.setId(LocalIdGenerator.generateId());
                propertyMetaEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                propertyMetaEntity.setCreatedTime(new Date());
                propertyMetaEntity.setDataType(xmlPropertyType.getDataType());
                propertyMetaEntity.setMapExpr(xmlPropertyType.getMapExpr());
                propertyMetaEntity.setMapType(xmlPropertyType.getMapType());
                propertyMetaEntity.setName(xmlPropertyType.getName());
                propertyMetaEntity.setObjectMetaId(objectMetaEntity.getId());
                propertyMetaEntity.setObjectName(objectMetaEntity.getName());
                propertyMetaEntity.setPackageName(objectMetaEntity.getPackageName());
                propertyMetaEntity.setRefType(xmlPropertyType.getRefType());
                propertyMetaEntity.setRefName(xmlPropertyType.getRefName());
                propertyMetaEntity.setSource(packageVersion);

                boolean sensitive = false;
                if (StringUtils.isNoneBlank(xmlPropertyType.getSensitiveData())) {
                    if ("Y".equalsIgnoreCase(xmlPropertyType.getSensitiveData())) {
                        sensitive = true;
                    }
                }
                propertyMetaEntity.setSensitive(sensitive);

                coreObjectPropertyMetaMapper.insert(propertyMetaEntity);
            }
        }

        return;
    }

    private CoreObjectPropertyMeta findOutObjectPropertyMetaByPropertyName(List<CoreObjectPropertyMeta> propertyMetas,
            String propertyName) {
        if (propertyMetas == null) {
            return null;
        }

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            if (propertyName.equals(propertyMeta.getName())) {
                return propertyMeta;
            }
        }

        return null;
    }

    private void tryCreateSingleParamObject(ParamObjectType xmlParamObject, String packageName, String packageVersion,
            String configId) {
        String paramObjectName = xmlParamObject.getName();
        CoreObjectMeta objectMetaEntity = new CoreObjectMeta();
        objectMetaEntity.setId(LocalIdGenerator.generateId());
        objectMetaEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        objectMetaEntity.setCreatedTime(new Date());
        objectMetaEntity.setPackageName(packageName);
        objectMetaEntity.setName(paramObjectName);
        objectMetaEntity.setSource(packageVersion);
        objectMetaEntity.setConfigId(configId);

        log.info("there is not param object {} existed and try to create one.", paramObjectName);
        coreObjectMetaMapper.insert(objectMetaEntity);

        List<ParamPropertyType> xmlPropertyList = xmlParamObject.getProperty();
        if (xmlPropertyList == null || xmlPropertyList.isEmpty()) {
            return;
        }

        for (ParamPropertyType xmlProperty : xmlPropertyList) {
            CoreObjectPropertyMeta propertyMetaEntity = new CoreObjectPropertyMeta();
            propertyMetaEntity.setId(LocalIdGenerator.generateId());
            propertyMetaEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            propertyMetaEntity.setCreatedTime(new Date());
            propertyMetaEntity.setDataType(xmlProperty.getDataType());
            propertyMetaEntity.setMapExpr(xmlProperty.getMapExpr());
            propertyMetaEntity.setMapType(xmlProperty.getMapType());
            propertyMetaEntity.setName(xmlProperty.getName());
            propertyMetaEntity.setObjectMetaId(objectMetaEntity.getId());
            propertyMetaEntity.setObjectName(objectMetaEntity.getName());
            propertyMetaEntity.setPackageName(objectMetaEntity.getPackageName());
            propertyMetaEntity.setRefType(xmlProperty.getRefType());
            propertyMetaEntity.setRefName(xmlProperty.getRefName());
            propertyMetaEntity.setSource(packageVersion);
            propertyMetaEntity.setConfigId(configId);

            boolean sensitive = false;
            if (StringUtils.isNoneBlank(xmlProperty.getSensitiveData())) {
                if ("Y".equalsIgnoreCase(xmlProperty.getSensitiveData())) {
                    sensitive = true;
                }
            }
            propertyMetaEntity.setSensitive(sensitive);

            coreObjectPropertyMetaMapper.insert(propertyMetaEntity);
        }

    }

}
