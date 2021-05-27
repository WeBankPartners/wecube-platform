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

@Service
public class PluginParamObjectMetaStorage extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectMetaStorage.class);

    /**
     * 
     * @param coreObjectMetaDto
     */
    @Transactional
    public void updateObjectMeta(CoreObjectMetaDto coreObjectMetaDto) {
        doUpdateObjectMeta(coreObjectMetaDto);
    }

    /**
     * 
     * @param packageName
     * @param coreObjectName
     * @return
     */
    public CoreObjectMeta fetchAssembledCoreObjectMeta(String packageName, String coreObjectName) {
        List<CoreObjectMeta> objectMetaList = new LinkedList<>();
        CoreObjectMeta objectMetaEntity = doFetchAssembledCoreObjectMeta(packageName, coreObjectName, objectMetaList);

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
                        propertyMetaEntity.getRefName(), cachedObjectMetaList);
                propertyMetaEntity.setRefObjectMeta(refObjectMetaEntity);
            }

            objectMetaEntity.addPropertyMeta(propertyMetaEntity);
        }

        return objectMetaEntity;

    }

    private CoreObjectMeta doFetchAssembledCoreObjectMeta(String packageName, String coreObjectName,
            List<CoreObjectMeta> cachedObjectMetaList) {
        CoreObjectMeta cachedObjectMetaEntity = findoutFromCachedObjetMetaEntityList(cachedObjectMetaList, packageName,
                coreObjectName);
        if (cachedObjectMetaEntity != null) {
            return cachedObjectMetaEntity;
        }
        CoreObjectMeta objectMetaEntity = coreObjectMetaMapper.selectOneByPackageNameAndObjectName(packageName,
                coreObjectName);
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
                        propertyMetaEntity.getRefName(), cachedObjectMetaList);
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

    private void doUpdateObjectMeta(CoreObjectMetaDto coreObjectMetaDto) {
        if (StringUtils.isBlank(coreObjectMetaDto.getId())) {
            log.info("object meta id is blank.");
            return;
        }

        CoreObjectMeta coreObjectMeta = coreObjectMetaMapper.selectByPrimaryKey(coreObjectMetaDto.getId());
        if (coreObjectMeta == null) {
            throw new WecubeCoreException("Such object meta does not exist with ID:" + coreObjectMetaDto.getId());
        }

        List<CoreObjectPropertyMetaDto> propertyMetaDtos = coreObjectMetaDto.getPropertyMetas();
        if (propertyMetaDtos == null || propertyMetaDtos.isEmpty()) {
            log.info("object property meta from dto is empty for {}", coreObjectMetaDto.getId());
            return;
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
            propertyMeta.setMapType(propertyMetaDto.getMapType());
            propertyMeta.setMapExpr(propertyMetaDto.getMapExpr());
            propertyMeta.setDataType(propertyMetaDto.getDataType());

            boolean sensitive = false;
            if ("Y".equals(propertyMetaDto.getSensitive())) {
                sensitive = true;
            }
            propertyMeta.setSensitive(sensitive);

            coreObjectPropertyMetaMapper.updateByPrimaryKeySelective(propertyMeta);

            if (propertyMetaDto.getRefObjectMeta() != null) {
                log.info("property {} has reference object meta and about to update recursively.", propertyMetaDto.getName());
                doUpdateObjectMeta(propertyMetaDto.getRefObjectMeta());
            }
        }
    }

}
