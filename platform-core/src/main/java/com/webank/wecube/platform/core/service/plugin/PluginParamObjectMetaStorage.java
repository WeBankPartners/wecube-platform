package com.webank.wecube.platform.core.service.plugin;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.utils.Constants;

@Service
public class PluginParamObjectMetaStorage extends AbstractPluginParamObjectService {
    
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

    private CoreObjectMeta doFetchAssembledCoreObjectMeta(String packageName, String coreObjectName, List<CoreObjectMeta> cachedObjectMetaList) {
        CoreObjectMeta cachedObjectMetaEntity = findoutFromCachedObjetMetaEntityList(cachedObjectMetaList, packageName, coreObjectName);
        if(cachedObjectMetaEntity != null){
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
    
    private CoreObjectMeta findoutFromCachedObjetMetaEntityList(List<CoreObjectMeta> objectMetaList, String packageName, String coreObjectName){
        for(CoreObjectMeta m : objectMetaList){
            if(packageName.equals(m.getPackageName()) && coreObjectName.equals(m.getName())){
                return m;
            }
        }
        
        return null;
    }

}
