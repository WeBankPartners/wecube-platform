package com.webank.wecube.platform.workflow.parse;

import java.util.ArrayList;
import java.util.List;

public class BpmnParseAttachment {
    private List<SubProcessAdditionalInfo> subProcessAddtionalInfos = new ArrayList<SubProcessAdditionalInfo>();

    public List<SubProcessAdditionalInfo> getSubProcessAddtionalInfos() {
        return subProcessAddtionalInfos;
    }

    public void setSubProcessAddtionalInfos(List<SubProcessAdditionalInfo> subProcessAddtionalInfos) {
        this.subProcessAddtionalInfos = subProcessAddtionalInfos;
    }
    
    public BpmnParseAttachment addSubProcessAddtionalInfo(SubProcessAdditionalInfo subProcessAddtionalInfo){
        if(subProcessAddtionalInfo == null){
            return this;
        }
        
        if(subProcessAddtionalInfos == null){
            subProcessAddtionalInfos  = new ArrayList<SubProcessAdditionalInfo>();
        }
        
        subProcessAddtionalInfos.add(subProcessAddtionalInfo);
        return this;
    }
}
