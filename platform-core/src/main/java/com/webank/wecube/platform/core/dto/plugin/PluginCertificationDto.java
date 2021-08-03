package com.webank.wecube.platform.core.dto.plugin;

public class PluginCertificationDto {
    private String id;
    private String plugin;
    private String lpk;
    private String encryptData;
    private String signature;
    private String description;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getLpk() {
        return lpk;
    }

    public void setLpk(String lpk) {
        this.lpk = lpk;
    }

    public String getEncryptData() {
        return encryptData;
    }

    public void setEncryptData(String encryptData) {
        this.encryptData = encryptData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[id=");
        builder.append(id);
        builder.append(", plugin=");
        builder.append(plugin);
        builder.append(", lpk=");
        builder.append(lpk);
        builder.append(", encryptData=");
        builder.append(encryptData);
        builder.append(", signature=");
        builder.append(signature);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }

}
