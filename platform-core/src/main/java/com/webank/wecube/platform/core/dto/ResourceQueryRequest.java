package com.webank.wecube.platform.core.dto;

public class ResourceQueryRequest {
    private Pageable pageable = new Pageable();
    
    public Pageable getPageable() {
        return pageable;
    }
    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

}
