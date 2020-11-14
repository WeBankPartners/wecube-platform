package com.webank.wecube.platform.core.dto;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;

public class FilterDto {
    @NotNull
    private String name;
    @NotNull
    private String operator;
    @NotNull
    private Object value;

    public FilterDto() {
    }

    public FilterDto(String name, String operator, Object value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean checkNull() throws IllegalAccessException {
        for (Field f : getClass().getDeclaredFields()) {
            if (f.get(this) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Filter [name=");
        builder.append(name);
        builder.append(", operator=");
        builder.append(operator);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }
    
    
}
