package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

public class EntityDataRecord {
    private String id;
    private List<EntityDataAttr> attrs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<EntityDataAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<EntityDataAttr> attrs) {
        this.attrs = attrs;
    }

    public void addAttrs(EntityDataAttr... attrs) {
        for (EntityDataAttr a : attrs) {
            this.attrs.add(a);
        }
    }
}
