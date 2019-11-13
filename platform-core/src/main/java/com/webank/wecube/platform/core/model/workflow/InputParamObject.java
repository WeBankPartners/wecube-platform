package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class InputParamObject {

    private String entityId;

    private List<String> attrNames = new ArrayList<>();

    private List<InputParamAttr> attrs = new ArrayList<>();
    
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
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

}
