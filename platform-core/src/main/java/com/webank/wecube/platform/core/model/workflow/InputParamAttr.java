package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

public class InputParamAttr {
    
    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";
    
    public static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    public static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;

    private String name;
    private String type; //string, number
    private String mapType; //entity, context, constant
    private List<Object> values = new ArrayList<>();

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

    public void addValues(Object... values) {
        for (Object v : values) {
            if (v != null) {
                this.values.add(v);
            }
        }
    }
    
    public void addValues(List<Object> values){
        if(values == null || values.isEmpty()){
            return;
        }
        
        for(Object v : values){
            if(v != null){
                this.values.add(v);
            }
        }
    }
    
    public Object getExpectedValue(){
        if(values == null || values.isEmpty()){
            return determineEmptyValue();
        }
        
        if(values.size() == 1){
            return values.get(0);
        }
        
        return values;
    }
    
    private Object determineEmptyValue(){
        if(DATA_TYPE_STRING.equalsIgnoreCase(type)){
            return DEFAULT_VALUE_DATA_TYPE_STRING;
        }
        
        if(DATA_TYPE_NUMBER.equalsIgnoreCase(type)){
            return DEFAULT_VALUE_DATA_TYPE_NUMBER;
        }
        
        return DEFAULT_VALUE_DATA_TYPE_STRING;
    }
    
    public String getValuesAsString(){
        if(values == null || values.isEmpty()){
            return null;
        }
        
        if(values.size() == 1){
            Object v = values.get(0);
            return v.toString();
        }
        
        StringBuilder sb = new StringBuilder();
        for(Object v : values){
            sb.append(v.toString()).append(",");
        }
        
        return sb.toString();
    }

}
