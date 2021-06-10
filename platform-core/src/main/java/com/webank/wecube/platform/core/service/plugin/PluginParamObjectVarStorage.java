package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.workflow.WorkflowConstants;

@Service
public class PluginParamObjectVarStorage extends AbstractPluginParamObjectService {

    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectVarStorage.class);

    @Autowired
    private PluginParamObjectMetaStorage pluginParamObjectMetaStorage;

    /**
     * 
     * @param objectVar
     */
    public void storeCoreObjectVar(CoreObjectVar objectVar) {
        if (objectVar == null) {
            log.debug("object var to store is null.");
            return;
        }
        doStoreCoreObjectVar(objectVar);
    }

    /**
     * 
     * @param objectId
     * @return
     */
    public CoreObjectVar fetchCoreObjectVar(String objectId) {
        if (StringUtils.isBlank(objectId)) {
            return null;
        }

        CoreObjectVar objectVar = doFetchCoreObjectVar(objectId, null);

        return objectVar;

    }

    private CoreObjectVar doFetchCoreObjectVar(String objectId, CoreObjectMeta objectMeta) {
        if (log.isInfoEnabled()) {
            log.info("about to fetch core object variable with id : {}", objectId);
        }

        if (StringUtils.isBlank(objectId)) {
            return null;
        }

        CoreObjectVar objectVar = coreObjectVarMapper.selectByPrimaryKey(objectId);

        if (objectVar == null) {
            log.info("such object variable does not exist with object ID:{}", objectId);
            return null;
        }

        CoreObjectMeta assembledObjectMeta = null;
        if (objectMeta == null) {
            objectMeta = coreObjectMetaMapper.selectByPrimaryKey(objectVar.getObjectMetaId());
            assembledObjectMeta = pluginParamObjectMetaStorage.fetchAssembledCoreObjectMeta(objectMeta.getPackageName(),
                    objectMeta.getName(), objectMeta.getConfigId());
        } else {
            assembledObjectMeta = objectMeta;
        }

        objectVar.setObjectMeta(assembledObjectMeta);
        assembleCoreObjectVar(objectVar);

        return objectVar;

    }

    private void assembleCoreObjectVar(CoreObjectVar objectVar) {
        if (objectVar.getObjectMeta() == null) {
            CoreObjectMeta objectMeta = coreObjectMetaMapper.selectByPrimaryKey(objectVar.getObjectMetaId());
            CoreObjectMeta assembledObjectMeta = pluginParamObjectMetaStorage
                    .fetchAssembledCoreObjectMeta(objectMeta.getPackageName(), objectMeta.getName(), objectMeta.getConfigId());
            objectVar.setObjectMeta(assembledObjectMeta);
        }

        List<CoreObjectPropertyVar> propertyVars = coreObjectPropertyVarMapper.selectAllByObjectVar(objectVar.getId());

        List<CoreObjectPropertyMeta> propertyMetas = objectVar.getObjectMeta().getPropertyMetas();
        if (propertyMetas == null || propertyMetas.isEmpty()) {
            return;
        }

        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            CoreObjectPropertyVar propertyVar = findCoreObjectPropertyVar(propertyMeta.getName(), propertyVars);
            if (propertyVar == null) {
                continue;
            }

            assembleCoreObjectPropertyVar(propertyVar, objectVar, propertyMeta);
            objectVar.addPropertyVar(propertyVar);

        }
    }

    private CoreObjectPropertyVar findCoreObjectPropertyVar(String propertyName,
            List<CoreObjectPropertyVar> propertyVars) {
        if (propertyVars == null || propertyVars.isEmpty()) {
            return null;
        }

        CoreObjectPropertyVar propertyVar = null;
        for (CoreObjectPropertyVar v : propertyVars) {
            if (propertyName.equals(v.getName())) {
                propertyVar = v;
                break;
            }
        }

        return propertyVar;
    }

    private void assembleCoreObjectPropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar objectVar,
            CoreObjectPropertyMeta propertyMeta) {
        propertyVar.setObjectVar(objectVar);
        propertyVar.setPropertyMeta(propertyMeta);
        Object dataValueObject = fetchDataValueObject(propertyVar, propertyMeta);
        propertyVar.setDataValueObject(dataValueObject);

    }

    private Object fetchDataValueObject(CoreObjectPropertyVar propertyVar, CoreObjectPropertyMeta propertyMeta) {
        String dataType = propertyMeta.getDataType();
        if (isBasicDataType(dataType)) {
            return convertStringToBasicPropertyValue(dataType, propertyVar.getDataValue());
        }

        if (isListDataType(dataType)) {
            return fetchListPropertyVarAsCoreObjectListVars(propertyVar, propertyMeta);
        }

        if (isObjectDataType(dataType)) {
            return fetchObjectPropertyVarAsCoreObjectVar(propertyVar, propertyMeta);
        }

        return null;
    }

    private CoreObjectVar fetchObjectPropertyVarAsCoreObjectVar(CoreObjectPropertyVar propertyVar,
            CoreObjectPropertyMeta propertyMeta) {
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();

        CoreObjectVar objectVar = doFetchCoreObjectVar(propertyVar.getDataValue(), refObjectMeta);

        return objectVar;

    }

    private List<CoreObjectListVar> fetchListPropertyVarAsCoreObjectListVars(CoreObjectPropertyVar propertyVar,
            CoreObjectPropertyMeta propertyMeta) {
        List<CoreObjectListVar> listVars = new ArrayList<>();
        String[] listVarIds = propertyVar.getDataValue().split(",");
        for (String listVarId : listVarIds) {
            CoreObjectListVar listVar = coreObjectListVarMapper.selectByPrimaryKey(listVarId);
            if (listVar == null) {
                continue;
            }

            assembleCoreObjectListVar(listVar, propertyVar, propertyMeta);
            listVars.add(listVar);
        }

        return listVars;
    }

    private void assembleCoreObjectListVar(CoreObjectListVar listVar, CoreObjectPropertyVar propertyVar,
            CoreObjectPropertyMeta propertyMeta) {
        listVar.setObjectPropertyMeta(propertyMeta);
        Object rawObjectValue = fetchListVarRawObjectValue(listVar, propertyVar, propertyMeta);
        listVar.setRawObjectValue(rawObjectValue);
    }
    
    private Object fetchListVarRawObjectValue(CoreObjectListVar listVar, CoreObjectPropertyVar propertyVar,
            CoreObjectPropertyMeta propertyMeta){
        String dataType = listVar.getDataType();
        if(isBasicDataType(dataType)){
            return convertStringToBasicPropertyValue(dataType,listVar.getDataValue());
        }
        
        if(isObjectDataType(dataType)){
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            CoreObjectVar objectVar = doFetchCoreObjectVar(listVar.getDataValue(), refObjectMeta);
            return objectVar;
        }
        
        return null;
    }

    private void doStoreCoreObjectVar(CoreObjectVar objectVar) {
        objectVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        objectVar.setCreatedTime(new Date());

        coreObjectVarMapper.insert(objectVar);

        List<CoreObjectPropertyVar> propertyVars = objectVar.getPropertyVars();

        if (propertyVars == null || propertyVars.isEmpty()) {
            log.debug("there is not properties to store for {}", objectVar.getId());
            return;
        }

        for (CoreObjectPropertyVar propertyVar : propertyVars) {
            storeCorePropertyVar(propertyVar, objectVar);
        }
    }

    private void storeCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar objectVar) {
        if (isBasicDataType(propertyVar.getDataType())) {
            storeBasicCorePropertyVar(propertyVar, objectVar);
            return;
        }

        if (isListDataType(propertyVar.getDataType())) {
            storeListCorePropertyVar(propertyVar, objectVar);
        }

        if (isObjectDataType(propertyVar.getDataType())) {
            storeObjectCorePropertyVar(propertyVar, objectVar);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeListCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {
        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());

        coreObjectPropertyVarMapper.insert(propertyVar);

        List<CoreObjectListVar> listVars = (List<CoreObjectListVar>) propertyVar.getDataValueObject();

        if (listVars == null || listVars.isEmpty()) {
            log.debug("there is not list vars to store for property {}", propertyVar.getId());
            return;
        }

        for (CoreObjectListVar listVar : listVars) {
            storeCoreObjectListVar(listVar, propertyVar);
        }
    }

    private void storeCoreObjectListVar(CoreObjectListVar listVar, CoreObjectPropertyVar propertyVar) {
        listVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        listVar.setCreatedTime(new Date());
        coreObjectListVarMapper.insert(listVar);

        if (isBasicDataType(listVar.getDataType())) {
            return;
        }

        if (isListDataType(listVar.getDataType())) {
            //
            log.debug("such data type {} is not currently supported.", listVar.getDataType());
            return;
        }

        if (isObjectDataType(listVar.getDataType())) {
            CoreObjectVar objectVar = (CoreObjectVar) listVar.getRawObjectValue();
            storeCoreObjectVar(objectVar);
        }
    }

    private void storeObjectCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {

        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());

        coreObjectPropertyVarMapper.insert(propertyVar);

        CoreObjectVar objectVar = (CoreObjectVar) propertyVar.getDataValueObject();

        storeCoreObjectVar(objectVar);
    }

    private void storeBasicCorePropertyVar(CoreObjectPropertyVar propertyVar, CoreObjectVar parentObjectVar) {
        propertyVar.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        propertyVar.setCreatedTime(new Date());
        coreObjectPropertyVarMapper.insert(propertyVar);
    }
}
