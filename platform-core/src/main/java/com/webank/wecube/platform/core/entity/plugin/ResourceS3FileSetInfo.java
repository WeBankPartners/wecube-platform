package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.List;

public class ResourceS3FileSetInfo {
    
    private List<ResourceS3FileInfo> files = new ArrayList<>();

    public List<ResourceS3FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<ResourceS3FileInfo> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceS3FileSetInfo [files=");
        builder.append(files);
        builder.append("]");
        return builder.toString();
    }
    
    
}
