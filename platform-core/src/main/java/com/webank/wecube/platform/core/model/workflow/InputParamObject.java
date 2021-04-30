package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class InputParamObject {

    private String entityTypeId;

    private String entityDataId;

    private String fullEntityDataId;

    private List<String> attrNames = new ArrayList<>();

    private List<InputParamAttr> attrs = new ArrayList<>();

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getEntityDataId() {
        return entityDataId;
    }

    public void setEntityDataId(String entityDataId) {
        this.entityDataId = entityDataId;
    }

    public List<String> getAttrNames() {
        return attrNames;
    }

    public void setAttrNames(List<String> attrNames) {
        this.attrNames = attrNames;
    }

    public List<InputParamAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<InputParamAttr> attrs) {
        this.attrs = attrs;
    }

    public void addAttrNames(String... attrNames) {
        for (String a : attrNames) {
            if (!StringUtils.isBlank(a)) {
                this.attrNames.add(a);
            }
        }
    }

    public void addAttrs(InputParamAttr... attrs) {
        for (InputParamAttr a : attrs) {

            if (a != null) {
                this.attrs.add(a);
            }
        }
    }

    public String getFullEntityDataId() {
        return fullEntityDataId;
    }

    public void setFullEntityDataId(String fullEntityDataId) {
        this.fullEntityDataId = fullEntityDataId;
    }

}
