package com.webank.wecube.platform.core.service.plugin;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectMetaMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectPropertyMetaMapper;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectSupportService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectSupportService.class);

    @Autowired
    private CoreObjectMetaMapper coreObjectMetaMapper;

    @Autowired
    private CoreObjectPropertyMetaMapper coreObjectPropertyMetaMapper;

    public void registerParamObjects(ParamObjectsType xmlParamObjects, String packageName, String packageVersion) {
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
            tryRegisterSingleParamObject(xmlParamObject, packageName, packageVersion);
        }

    }

    private void tryRegisterSingleParamObject(ParamObjectType xmlParamObject, String packageName,
            String packageVersion) {
        log.info("try register param object {}", xmlParamObject.getName());
        String paramObjectName = xmlParamObject.getName();
        CoreObjectMeta objectMetaEntity = coreObjectMetaMapper.selectOneByPackageNameAndObjectName(packageName,
                paramObjectName);

        if (objectMetaEntity == null) {
            tryCreateSingleParamObject(xmlParamObject, packageName, packageVersion);
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

        // to implement in future
        return;
    }

    private void tryCreateSingleParamObject(ParamObjectType xmlParamObject, String packageName, String packageVersion) {
        String paramObjectName = xmlParamObject.getName();
        CoreObjectMeta objectMetaEntity = new CoreObjectMeta();
        objectMetaEntity.setId(LocalIdGenerator.generateId());
        objectMetaEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        objectMetaEntity.setCreatedTime(new Date());
        objectMetaEntity.setPackageName(packageName);
        objectMetaEntity.setName(paramObjectName);
        objectMetaEntity.setSource(packageVersion);

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
            propertyMetaEntity.setSource(packageVersion);
            
            boolean sensitive = false;
            if(StringUtils.isNoneBlank(xmlProperty.getSensitiveData())){
                if("Y".equalsIgnoreCase(xmlProperty.getSensitiveData())){
                    sensitive = true;
                }
            }
            propertyMetaEntity.setSensitive(sensitive);
            
            coreObjectPropertyMetaMapper.insert(propertyMetaEntity);
        }

    }

}
