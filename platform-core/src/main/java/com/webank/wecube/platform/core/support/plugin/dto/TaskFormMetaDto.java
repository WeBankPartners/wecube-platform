package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskFormMetaDto {
    private String formMetaId;

    private List<TaskFormItemMetaDto> formItemMetas = new ArrayList<>();

    public List<TaskFormItemMetaDto> getFormItemMetas() {
        return formItemMetas;
    }

    public void setFormItemMetas(List<TaskFormItemMetaDto> formItemMetas) {
        this.formItemMetas = formItemMetas;
    }

    public void addFormItemMeta(TaskFormItemMetaDto formItemMeta) {
        if (formItemMeta == null) {
            return;
        }

        if (this.formItemMetas == null) {
            this.formItemMetas = new ArrayList<>();
        }

        this.formItemMetas.add(formItemMeta);

    }

    public String getFormMetaId() {
        return formMetaId;
    }

    public void setFormMetaId(String formMetaId) {
        this.formMetaId = formMetaId;
    }

}
