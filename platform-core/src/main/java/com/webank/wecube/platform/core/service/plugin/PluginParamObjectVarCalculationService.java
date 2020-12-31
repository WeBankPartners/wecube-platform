package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginParamObjectVarCalculationService extends AbstractPluginParamObjectService {
    private static final Logger log = LoggerFactory.getLogger(PluginParamObjectVarCalculationService.class);

    public static final String PREFIX_OBJECT_VAR_ID = "objvar";

    public static final String PREFIX_PROPERTY_VAR_ID = "attrvar";

    public static final String PREFIX_LIST_VAR_ID = "listvar";

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    public CoreObjectVar calculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {
       
        CoreObjectVar rootObjectVar = doCalculateCoreObjectVar(objectMeta, ctx);
        // TODO store object var
        return rootObjectVar;
    }

    private CoreObjectVar doCalculateCoreObjectVar(CoreObjectMeta objectMeta, CoreObjectVarCalculationContext ctx) {
        if (objectMeta == null) {
            return null;
        }
        CoreObjectVar rootObjectVar = new CoreObjectVar();
        rootObjectVar.setId(LocalIdGenerator.generateId(PREFIX_OBJECT_VAR_ID));
        rootObjectVar.setName(objectMeta.getName());
        rootObjectVar.setObjectMeta(objectMeta);
        rootObjectVar.setObjectMetaId(objectMeta.getId());
        rootObjectVar.setPackageName(objectMeta.getPackageName());

        List<CoreObjectPropertyMeta> propertyMetas = objectMeta.getPropertyMetas();
        for (CoreObjectPropertyMeta propertyMeta : propertyMetas) {
            CoreObjectPropertyVar propertyVar = new CoreObjectPropertyVar();
            propertyVar.setId(LocalIdGenerator.generateId(PREFIX_PROPERTY_VAR_ID));
            propertyVar.setName(propertyMeta.getName());
            propertyVar.setObjectMetaId(rootObjectVar.getObjectMetaId());
            propertyVar.setDataType(propertyMeta.getDataType());

            Object dataValueObject = calculateDataValueObject(propertyMeta, ctx);
            String dataValue = convertPropertyValueToString(propertyMeta, dataValueObject);
            propertyVar.setDataValueObject(dataValueObject);
            propertyVar.setDataValue(dataValue);
            propertyVar.setPropertyMeta(propertyMeta);
            propertyVar.setSensitive(propertyMeta.getSensitive());
            propertyVar.setObjectVar(rootObjectVar);
            propertyVar.setObjectVarId(rootObjectVar.getId());

            rootObjectVar.addPropertyVar(propertyVar);
        }

        return rootObjectVar;
    }

    private String convertPropertyValueToString(CoreObjectPropertyMeta propertyMeta, Object dataValueObject) {
        if (dataValueObject == null) {
            return null;
        }
        
        String dataType = propertyMeta.getDataType();
        if(isStringDataType(dataType)){
            return dataValueObject.toString();
        }
        
        if(isNumberDataType(dataType)){
            return dataValueObject.toString();
        }
        
        if(isObjectDataType(dataType)){
            CoreObjectVar objVar = (CoreObjectVar)dataValueObject;
            return objVar.getId();
        }
        
        if(isListDataType(dataType)){
            List<CoreObjectListVar> listVars = (List<CoreObjectListVar>)dataValueObject;
            StringBuilder sb = new StringBuilder();
            for(CoreObjectListVar v : listVars){
                sb.append(v.getId()).append(",");
            }
            
            return sb.toString();
        }

        return null;
    }

    private Object calculateDataValueObject(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        String dataType = propertyMeta.getDataType();
        Object dataObjectValue = null;

        if (isStringDataType(dataType)) {
            dataObjectValue = calculateStringPropertyValue(propertyMeta, ctx);
        } else if (isNumberDataType(dataType)) {
            dataObjectValue = calculateNumberPropertyValue(propertyMeta, ctx);
        } else if (isObjectDataType(dataType)) {
            dataObjectValue = calculateObjectPropertyValue(propertyMeta, ctx);
        } else if (isListDataType(dataType)) {
            dataObjectValue = calculateListPropertyValue(propertyMeta, ctx);
        }

        return dataObjectValue;
    }

    private String calculateStringPropertyValue(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (isStringDataType(propertyMeta.getDataType())) {
            // TODO
            return String.valueOf(System.currentTimeMillis());
        }
        return null;
    }

    private Integer calculateNumberPropertyValue(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (isNumberDataType(propertyMeta.getDataType())) {
            // TODO
            return Integer.valueOf(1000);
        }
        return null;
    }

    private CoreObjectVar calculateObjectPropertyValue(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (isObjectDataType(propertyMeta.getDataType())) {
            CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
            // TODO
            CoreObjectVar refObjectVar = calculateCoreObjectVar(refObjectMeta, ctx);
            return refObjectVar;
        }
        return null;
    }
    
    private List<String> calculateListStringVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx){
        //TODO
        List<String> rawObjectValues = new ArrayList<>();
        String rawObjectValue = String.valueOf(System.currentTimeMillis());
        rawObjectValues.add(rawObjectValue);
        
        return rawObjectValues;
    }
     
    private List<Integer> calculateListNumberVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx){
        List<Integer> rawObjectValues = new ArrayList<>();
        Integer rawObjectValue = 1111111;
        rawObjectValues.add(rawObjectValue);
        
        return rawObjectValues;
    }

    private List<CoreObjectListVar> calculateListPropertyValue(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        if (isListDataType(propertyMeta.getDataType())) {
            if (isStringDataType(propertyMeta.getRefType())) {
                // TODO
                List<String> rawObjectValues = calculateListStringVars(propertyMeta, ctx);

                List<CoreObjectListVar> stringListVars = new ArrayList<>();
                for (String rawObject : rawObjectValues) {
                    CoreObjectListVar stringListVar = new CoreObjectListVar();
                    stringListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    stringListVar.setDataType(propertyMeta.getRefType());
                    // TODO
                    String dataValue = rawObject;

                    stringListVar.setDataValue(dataValue);
                    stringListVar.setRawObjectValue(rawObject);
                    stringListVar.setSensitive(propertyMeta.getSensitive());
                    stringListVar.setObjectPropertyMeta(propertyMeta);

                    stringListVars.add(stringListVar);
                }

                return stringListVars;
            }

            if (isNumberDataType(propertyMeta.getRefType())) {
                List<Integer> rawObjectValues = calculateListNumberVars(propertyMeta, ctx);

                List<CoreObjectListVar> numberListVars = new ArrayList<>();
                for (Integer rawObject : rawObjectValues) {
                    CoreObjectListVar numberListVar = new CoreObjectListVar();
                    numberListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    numberListVar.setDataType(propertyMeta.getRefType());
                    // TODO
                    String dataValue = String.valueOf(rawObject);

                    numberListVar.setDataValue(dataValue);
                    numberListVar.setRawObjectValue(rawObject);
                    numberListVar.setSensitive(propertyMeta.getSensitive());
                    numberListVar.setObjectPropertyMeta(propertyMeta);

                    numberListVars.add(numberListVar);
                }

                return numberListVars;
            }

            if (isObjectDataType(propertyMeta.getRefType())) {
                List<CoreObjectVar> rawObjectValues = calculateListObjectVars(propertyMeta, ctx);

                // TODO
                List<CoreObjectListVar> objectListVars = new ArrayList<>();
                for (CoreObjectVar objectVar : rawObjectValues) {
                    CoreObjectListVar objectListVar = new CoreObjectListVar();
                    objectListVar.setId(LocalIdGenerator.generateId(PREFIX_LIST_VAR_ID));
                    objectListVar.setDataType(propertyMeta.getRefType());

                    // TODO
                    String dataValue = objectVar.getId();

                    objectListVar.setDataValue(dataValue);
                    objectListVar.setRawObjectValue(objectVar);
                    objectListVar.setSensitive(propertyMeta.getSensitive());
                    objectListVar.setObjectPropertyMeta(propertyMeta);

                    objectListVars.add(objectListVar);
                }

                return objectListVars;
            }
        }
        return null;
    }

    private List<CoreObjectVar> doCalculateCoreObjectVarList(CoreObjectMeta objectMeta,
            CoreObjectVarCalculationContext ctx) {
        List<CoreObjectVar> rawObjectValues = new ArrayList<>();
        
        Map<String,List<CoreObjectPropertyVar>> propertyMetaVarsMap = new HashMap<String,List<CoreObjectPropertyVar>>();
        

        CoreObjectVar objectVar = new CoreObjectVar();

        rawObjectValues.add(objectVar);

        return rawObjectValues;
    }

    private List<CoreObjectVar> calculateListObjectVars(CoreObjectPropertyMeta propertyMeta, CoreObjectVarCalculationContext ctx) {
        // TODO
        CoreObjectMeta refObjectMeta = propertyMeta.getRefObjectMeta();
        List<CoreObjectVar> rawObjectValues = doCalculateCoreObjectVarList(refObjectMeta, ctx);

        return rawObjectValues;

    }

}
