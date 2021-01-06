package com.webank.wecube.platform.core.service.dme;

class StandardEntityQueryCacheData {
    private Object data;
    private long timestamp;

    public StandardEntityQueryCacheData() {
        super();
    }

    public StandardEntityQueryCacheData(Object data, long timestamp) {
        super();
        this.data = data;
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
