package com.webank.wecube.platform.core.dto.plugin;

public class PluginConfigOutlineDto {

    private String id;
    private String pluginPackageId;
    private String name;
    private String targetEntityWithFilterRule;
    private String registerName;
    private String status;
    private Boolean hasMgmtPermission;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetEntityWithFilterRule() {
        return targetEntityWithFilterRule;
    }

    public void setTargetEntityWithFilterRule(String targetEntityWithFilterRule) {
        this.targetEntityWithFilterRule = targetEntityWithFilterRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public Boolean getHasMgmtPermission() {
        return hasMgmtPermission;
    }

    public void setHasMgmtPermission(Boolean hasMgmtPermission) {
        this.hasMgmtPermission = hasMgmtPermission;
    }

//    public static PluginConfigOutlineDto fromDomain(PluginConfig pluginConfig) {
//        PluginConfigOutlineDto pluginConfigDto = new PluginConfigOutlineDto();
//        pluginConfigDto.setId(pluginConfig.getId());
//        pluginConfigDto.setName(pluginConfig.getName());
//        pluginConfigDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
//        pluginConfigDto.setRegisterName(pluginConfig.getRegisterName());
//        pluginConfigDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
//        pluginConfigDto.setStatus(pluginConfig.getStatus());
//        return pluginConfigDto;
//    }
}
