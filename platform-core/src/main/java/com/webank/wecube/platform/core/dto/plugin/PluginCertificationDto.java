package com.webank.wecube.platform.core.dto.plugin;

public class PluginCertificationDto {
    private String plugin;
    private String lpk;
    private String data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[plugin=");
        builder.append(plugin);
        builder.append(", lpk=");
        builder.append(lpk);
        builder.append(", data=");
        builder.append(data);
        builder.append(", signature=");
        builder.append(signature);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }
}
