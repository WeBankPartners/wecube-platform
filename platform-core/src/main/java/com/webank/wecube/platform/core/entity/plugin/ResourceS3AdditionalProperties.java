package com.webank.wecube.platform.core.entity.plugin;

public class ResourceS3AdditionalProperties {
    
    private ResourceS3FileSetInfo fileSet;

    public ResourceS3FileSetInfo getFileSet() {
        return fileSet;
    }

    public void setFileSet(ResourceS3FileSetInfo fileSet) {
        this.fileSet = fileSet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceS3AdditionalProperties [fileSet=");
        builder.append(fileSet);
        builder.append("]");
        return builder.toString();
    }
    

    
}
