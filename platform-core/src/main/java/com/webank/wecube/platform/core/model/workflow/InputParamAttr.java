package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.service.plugin.PluginParamObject;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JsonUtils;

public class InputParamAttr {

    private String name; // parameter name
    private String type; // string, number, object
    private String multiple;
    private String mapType; // entity, context, constant, object
    private List<Object> values = new ArrayList<>(); // raw object values
    private boolean sensitive;
    private PluginConfigInterfaceParameters paramDef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public void addValueObjects(Object... values) {
        for (Object v : values) {
            this.values.add(v);
        }
    }

    public void addValues(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        for (Object v : values) {
            this.values.add(v);
        }
    }

    public Object getExpectedValue() {
        // #2226

        if (isMultiple()) {
            List<Object> clonedListValues = new ArrayList<>();
            if (values == null || values.isEmpty()) {
                return clonedListValues;
            }

            for (Object val : values) {
                if (val instanceof PluginParamObject) {
                    PluginParamObject objVal = (PluginParamObject) val;
                    PluginParamObject clonedObjVal = PluginParamObject.wipeOffObjectIdAndClone(objVal);
                    clonedListValues.add(clonedObjVal);
                } else {
                    clonedListValues.add(val);
                }
            }

            return clonedListValues;
        } else {

            if (values == null || values.isEmpty()) {
                return determineBasicEmptyValue();
            }
            
            if (values.size() == 1) {
                Object val = values.get(0);
                if (val == null) {
                    return val;
                }

                if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(type)) {
                    if(val instanceof String){
                        return (String)val;
                    }
                    
                    if(val instanceof Integer){
                        return String.valueOf(val);
                    }
                    
                    return JsonUtils.toJsonString(val);
                }

                if (val instanceof PluginParamObject) {
                    PluginParamObject objVal = (PluginParamObject) val;
                    return PluginParamObject.wipeOffObjectIdAndClone(objVal);
                }

                return val;
            }

            if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(type)) {
                return assembleValueList(values);
            }

            return values;
        }
    }

    protected String assembleValueList(List<Object> retDataValues) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");

        for (Object dv : retDataValues) {
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }

            // TODO
            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

    private Object determineBasicEmptyValue() {
        if (Constants.DATA_TYPE_STRING.equalsIgnoreCase(type)) {
            return Constants.DEFAULT_VALUE_DATA_TYPE_STRING;
        }

        if (Constants.DATA_TYPE_NUMBER.equalsIgnoreCase(type)) {
            return Constants.DEFAULT_VALUE_DATA_TYPE_NUMBER;
        }

        return Constants.DEFAULT_VALUE_DATA_TYPE_STRING;
    }

    public String getValuesAsString() {
        if (values == null || values.isEmpty()) {
            return String.valueOf(determineBasicEmptyValue());
        }

        if (values.size() == 1) {
            Object v = values.get(0);
            if (v == null) {
                return String.valueOf(determineBasicEmptyValue());
            }

            if ((v instanceof PluginParamObject) || (v instanceof Map) || (v instanceof List)) {
                return JsonUtils.toJsonString(v);
            }

            return String.valueOf(v);
        }

        StringBuilder sb = new StringBuilder();
        for (Object v : values) {

            sb.append(v == null ? String.valueOf(determineBasicEmptyValue()) : convertToString(v)).append(",");
        }

        return sb.toString();
    }

    public static String convertToString(Object v) {
        if (v == null) {
            return null;
        }
        
        if(v instanceof String){
            return (String)v;
        }
        
        //TODO add prefix of type
        if( (v instanceof PluginParamObject ) || (v instanceof Map) || (v instanceof List)){
            return JsonUtils.toJsonString(v);
        }

        return String.valueOf(v);
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public boolean isMultiple() {
        return Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
    }

    public PluginConfigInterfaceParameters getParamDef() {
        return paramDef;
    }

    public void setParamDef(PluginConfigInterfaceParameters paramDef) {
        this.paramDef = paramDef;
    }
    
    

}
