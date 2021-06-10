package com.webank.wecube.platform.core.service.plugin;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CoreObjectMetaDto;
import com.webank.wecube.platform.core.dto.plugin.CoreObjectPropertyMetaDto;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectMetaStorage extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectMetaStorage.class);

    /**
     * 
     * @param coreObjectMetaDto
     */
    @Transactional
    public void updateOrCreateObjectMeta(CoreObjectMetaDto coreObjectMetaDto, String configId) {
        doUpdateOrCreateObjectMeta(coreObjectMetaDto, configId);
    }

    /**
     * 
     * @param packageName
     * @param coreObjectName
     * @return
     */
    public CoreObjectMeta fetchAssembledCoreObjectMeta(String packageName, String coreObjectName, String configId) {
        List<CoreObjectMeta> objectMetaList = new LinkedList<>();
        CoreObjectMeta objectMetaEntity = doFetchAssembledCoreObjectMeta(packageName, coreObjectName, objectMetaList,
                configId);

        return objectMetaEntity;
    }

    /**
     * 
     * @param objectMetaId
     * @return
     */
    public CoreObjectMeta fetchAssembledCoreObjectMetaById(String objectMetaId) {
        CoreObjectMeta objectMetaEntity = coreObjectMetaMapper.selectByPrimaryKey(objectMetaId);
        if (objectMetaEntity == null) {
            return null;
        }

        String configId = objectMetaEntity.getConfigId();
        List<CoreObjectMeta> cachedObjectMetaList = new LinkedList<>();
        cachedObjectMetaList.add(objectMetaEntity);

        List<CoreObjectPropertyMeta> propertyMetaEntities = coreObjectPropertyMetaMapper
                .selectAllByObjectMeta(objectMetaEntity.getId());
        if (propertyMetaEntities == null || propertyMetaEntities.isEmpty()) {
            return objectMetaEntity;
        }

        for (CoreObjectPropertyMeta propertyMetaEntity : propertyMetaEntities) {
            if (Constants.DATA_TYPE_OBJECT.equals(propertyMetaEntity.getRefType())) {

                CoreObjectMeta refObjectMetaEntity = doFetchAssembledCoreObjectMeta(objectMetaEntity.getPackageName(),
                        propertyMetaEntity.getRefName(), cachedObjectMetaList, configId);
                propertyMetaEntity.setRefObjectMeta(refObjectMetaEntity);
            }

            objectMetaEntity.addPropertyMeta(propertyMetaEntity);
        }

        return objectMetaEntity;

    }

    private CoreObjectMeta doFetchAssembledCoreObjectMeta(String packageName, String coreObjectName,
            List<CoreObjectMeta> cachedObjectMetaList, String configId) {
        CoreObjectMeta cachedObjectMetaEntity = findoutFromCachedObjetMetaEntityList(cachedObjectMetaList, packageName,
                coreObjectName);
        if (cachedObjectMetaEntity != null) {
            return cachedObjectMetaEntity;
        }
        CoreObjectMeta objectMetaEntity = coreObjectMetaMapper.selectOneByPackageNameAndObjectNameAndConfig(packageName,
                coreObjectName, configId);
        if (objectMetaEntity == null) {
            return null;
        }

        cachedObjectMetaList.add(objectMetaEntity);

        List<CoreObjectPropertyMeta> propertyMetaEntities = coreObjectPropertyMetaMapper
                .selectAllByObjectMeta(objectMetaEntity.getId());
        if (propertyMetaEntities == null || propertyMetaEntities.isEmpty()) {
            return objectMetaEntity;
        }

        for (CoreObjectPropertyMeta propertyMetaEntity : propertyMetaEntities) {
            if (Constants.DATA_TYPE_OBJECT.equals(propertyMetaEntity.getRefType())) {

                CoreObjectMeta refObjectMetaEntity = doFetchAssembledCoreObjectMeta(packageName,
                        propertyMetaEntity.getRefName(), cachedObjectMetaList, configId);
                propertyMetaEntity.setRefObjectMeta(refObjectMetaEntity);
            }

            objectMetaEntity.addPropertyMeta(propertyMetaEntity);
        }

        return objectMetaEntity;
    }

    private CoreObjectMeta findoutFromCachedObjetMetaEntityList(List<CoreObjectMeta> objectMetaList, String packageName,
            String coreObjectName) {
        for (CoreObjectMeta m : objectMetaList) {
            if (packageName.equals(m.getPackageName()) && coreObjectName.equals(m.getName())) {
                return m;
            }
        }

        return null;
    }

    private CoreObjectMetaDto doCreateObjectMeta(CoreObjectMetaDto coreObjectMetaDto, String configId) {
        CoreObjectMeta coreObjectMeta = coreObjectMetaMapper.selectOneByPackageNameAndObjectNameAndConfig(
                coreObjectMetaDto.getPackageName(), coreObjectMetaDto.getName(), configId);

        if (coreObjectMeta == null) {
            coreObjectMeta = new CoreObjectMeta();
            coreObjectMeta.setConfigId(configId);
            coreObjectMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            coreObjectMeta.setCreatedTime(new Date());
            coreObjectMeta.setId(LocalIdGenerator.generateId());
            coreObjectMeta.setLatestSource(coreObjectMetaDto.getLatestSource());
            coreObjectMeta.setName(coreObjectMetaDto.getName());
            coreObjectMeta.setPackageName(coreObjectMetaDto.getPackageName());
            coreObjectMetaMapper.insert(coreObjectMeta);
        }

        coreObjectMetaDto.setId(coreObjectMeta.getId());

        int recordCount = coreObjectPropertyMetaMapper.deleteByObjectMeta(coreObjectMeta.getId());

        log.info("total {} object property metas had been deleted.", recordCount);

        List<CoreObjectPropertyMetaDto> propertyMetaDtos = coreObjectMetaDto.getPropertyMetas();
        if (propertyMetaDtos == null || propertyMetaDtos.isEmpty()) {
            log.info("object property meta from dto is empty for {}", coreObjectMetaDto.getId());
            return coreObjectMetaDto;
        }

        for (CoreObjectPropertyMetaDto propertyMetaDto : propertyMetaDtos) {

            CoreObjectPropertyMeta propertyMeta = new CoreObjectPropertyMeta();
            propertyMeta.setId(LocalIdGenerator.generateId());
            propertyMeta.setConfigId(configId);
            propertyMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            propertyMeta.setCreatedTime(new Date());
            propertyMeta.setMapExpr(propertyMetaDto.getMappingEntityExpression());
            propertyMeta.setMapType(propertyMetaDto.getMappingType());
            propertyMeta.setName(propertyMetaDto.getName());
            propertyMeta.setObjectMetaId(coreObjectMeta.getId());
            propertyMeta.setObjectName(propertyMetaDto.getObjectName());
            propertyMeta.setPackageName(propertyMetaDto.getPackageName());
            propertyMeta.setRefName(propertyMetaDto.getRefName());
            propertyMeta.setRefType(propertyMetaDto.getRefType());
            boolean sensitive = false;
            if (CoreObjectPropertyMetaDto.SENSITIVE_YES.equals(propertyMetaDto.getSensitiveData())) {
                sensitive = true;
            }
            propertyMeta.setSensitive(sensitive);
            propertyMeta.setSource(propertyMetaDto.getSource());

            propertyMeta.setObjectMeta(coreObjectMeta);

            coreObjectPropertyMetaMapper.insert(propertyMeta);

            propertyMetaDto.setId(propertyMeta.getId());
            if (propertyMetaDto.getRefObjectMeta() != null) {
                log.info("property {} has reference object meta and about to create recursively.",
                        propertyMetaDto.getName());
                CoreObjectMetaDto objectMetaDto = doCreateObjectMeta(propertyMetaDto.getRefObjectMeta(), configId);
                propertyMetaDto.setRefObjectMeta(objectMetaDto);
            }
        }

        return coreObjectMetaDto;
    }

    private CoreObjectMetaDto doUpdateObjectMeta(CoreObjectMetaDto coreObjectMetaDto, String configId) {
        CoreObjectMeta coreObjectMeta = coreObjectMetaMapper.selectByPrimaryKey(coreObjectMetaDto.getId());
        if (coreObjectMeta == null) {
            throw new WecubeCoreException("Such object meta does not exist with ID:" + coreObjectMetaDto.getId());
        }

        List<CoreObjectPropertyMetaDto> propertyMetaDtos = coreObjectMetaDto.getPropertyMetas();
        if (propertyMetaDtos == null || propertyMetaDtos.isEmpty()) {
            log.info("object property meta from dto is empty for {}", coreObjectMetaDto.getId());
            return coreObjectMetaDto;
        }

        for (CoreObjectPropertyMetaDto propertyMetaDto : propertyMetaDtos) {
            if (StringUtils.isBlank(propertyMetaDto.getId())) {
                log.info("object property meta does not provide : {}", propertyMetaDto.getName());
                continue;
            }

            CoreObjectPropertyMeta propertyMeta = coreObjectPropertyMetaMapper
                    .selectByPrimaryKey(propertyMetaDto.getId());
            if (propertyMeta == null) {
                log.info("object property meta does not exist with ID {}", propertyMetaDto.getId());
                continue;
            }

            propertyMeta.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            propertyMeta.setUpdatedTime(new Date());
            propertyMeta.setMapType(propertyMetaDto.getMappingType());
            propertyMeta.setMapExpr(propertyMetaDto.getMappingEntityExpression());
            propertyMeta.setDataType(propertyMetaDto.getDataType());

            boolean sensitive = false;
            if (CoreObjectPropertyMetaDto.SENSITIVE_YES.equals(propertyMetaDto.getSensitiveData())) {
                sensitive = true;
            }
            propertyMeta.setSensitive(sensitive);

            coreObjectPropertyMetaMapper.updateByPrimaryKeySelective(propertyMeta);

            if (propertyMetaDto.getRefObjectMeta() != null) {
                log.info("property {} has reference object meta and about to update recursively.",
                        propertyMetaDto.getName());
                doUpdateObjectMeta(propertyMetaDto.getRefObjectMeta(), configId);
            }
        }

        return coreObjectMetaDto;
    }

    private CoreObjectMetaDto doUpdateOrCreateObjectMeta(CoreObjectMetaDto coreObjectMetaDto, String configId) {
        if (StringUtils.isBlank(configId)) {
            throw new WecubeCoreException("Configuration ID to update object metadata cannot be blank.");
        }

        if (StringUtils.isBlank(coreObjectMetaDto.getId()) || (!configId.equals(coreObjectMetaDto.getConfigId()))) {
            return doCreateObjectMeta(coreObjectMetaDto, configId);
        } else {
            return doUpdateObjectMeta(coreObjectMetaDto, configId);
        }
    }

}
