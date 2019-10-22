package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class AttachVO {
    private List<AttachItemVO> attachItems = new ArrayList<AttachItemVO>();

    public List<AttachItemVO> getAttachItems() {
        return attachItems;
    }

    public void setAttachItems(List<AttachItemVO> attachItems) {
        this.attachItems = attachItems;
    }
    
    public void addAttachItem(AttachItemVO item) {
        if(item == null) {
            return;
        }
        
        if(attachItems == null) {
            attachItems = new ArrayList<AttachItemVO>();
        }
        
        attachItems.add(item);
    }
}
