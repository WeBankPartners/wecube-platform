package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

public class InputParamAttr {

    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";

    public static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    public static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;

    private String name;
    private String type; // string, number, object
    private String mapType; // entity, context, constant
    private List<Object> values = new ArrayList<>();
    private boolean sensitive;

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
        //TODO
        //#2226
        if (values == null || values.isEmpty()) {
            return determineEmptyValue();
        }

        if (values.size() == 1) {
            Object val = values.get(0);
            if(val == null) {
                return val;
            }
            
            if(DATA_TYPE_STRING.equalsIgnoreCase(type)) {
                return String.valueOf(val);
            }
            
            return val;
        }

        if (DATA_TYPE_STRING.equalsIgnoreCase(type)) {
            return assembleValueList(values);
        }

        return values;
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

            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

    private Object determineEmptyValue() {
        if (DATA_TYPE_STRING.equalsIgnoreCase(type)) {
            return DEFAULT_VALUE_DATA_TYPE_STRING;
        }

        if (DATA_TYPE_NUMBER.equalsIgnoreCase(type)) {
            return DEFAULT_VALUE_DATA_TYPE_NUMBER;
        }

        return DEFAULT_VALUE_DATA_TYPE_STRING;
    }

    public String getValuesAsString() {
        if (values == null || values.isEmpty()) {
            return String.valueOf(determineEmptyValue());
        }

        if (values.size() == 1) {
            Object v = values.get(0);
            if (v == null) {
                return String.valueOf(determineEmptyValue());
            }
            return String.valueOf(v);
        }

        StringBuilder sb = new StringBuilder();
        for (Object v : values) {

            sb.append(v == null ? String.valueOf(determineEmptyValue()) : String.valueOf(v)).append(",");
        }

        return sb.toString();
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

}
