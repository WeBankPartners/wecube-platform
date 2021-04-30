package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class UserTaskFormTemplateMetaDto {
    private String formTemplateId;
    
    private List<UserTaskFormItemTemplateMetaDto> formItemTemplateMetas = new ArrayList<>();

    public String getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(String formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public List<UserTaskFormItemTemplateMetaDto> getFormItemTemplateMetas() {
        return formItemTemplateMetas;
    }

    public void setFormItemTemplateMetas(List<UserTaskFormItemTemplateMetaDto> formItemTemplateMetas) {
        this.formItemTemplateMetas = formItemTemplateMetas;
    }
    
    public void addFormItemTemplateMeta(UserTaskFormItemTemplateMetaDto formItemTemplateMeta){
        if(formItemTemplateMeta == null){
            return;
        }
        
        if(this.formItemTemplateMetas == null){
            this.formItemTemplateMetas = new ArrayList<>(); 
        }
        
        this.formItemTemplateMetas.add(formItemTemplateMeta);
        
    }

}
