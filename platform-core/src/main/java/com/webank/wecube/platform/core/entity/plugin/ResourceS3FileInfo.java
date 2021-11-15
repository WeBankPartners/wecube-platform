package com.webank.wecube.platform.core.entity.plugin;

public class ResourceS3FileInfo {
    private String source;
    private String toFile;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getToFile() {
        return toFile;
    }

    public void setToFile(String toFile) {
        this.toFile = toFile;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceS3FileInfo [source=");
        builder.append(source);
        builder.append(", toFile=");
        builder.append(toFile);
        builder.append("]");
        return builder.toString();
    }
    
    
}
