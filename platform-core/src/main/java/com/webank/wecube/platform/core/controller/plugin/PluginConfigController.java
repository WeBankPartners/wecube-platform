package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.CoreObjectMetaDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigRoleRequestDto;
import com.webank.wecube.platform.core.dto.plugin.TargetEntityFilterRuleDto;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMgmtService;

@RestController
@RequestMapping("/v1")
public class PluginConfigController {

    @Autowired
    private PluginConfigMgmtService pluginConfigMgmtService;

    /**
     * 
     * @param pluginConfigDto
     * @return
     */
    @PostMapping("/plugins")
    public CommonResponseDto createOrUpdatePluginConfig(@RequestBody PluginConfigDto pluginConfigDto) {
        return okayWithData(pluginConfigMgmtService.createOrUpdatePluginConfig(pluginConfigDto));
    }

    /**
     * 
     * @return
     */
    @GetMapping("/plugins/interfaces/enabled")
    public CommonResponseDto queryAllEnabledPluginConfigInterface() {
        return okayWithData(pluginConfigMgmtService.queryAllLatestEnabledPluginConfigInterface());
    }

    /**
     * 
     * @param packageName
     * @param entityName
     * @return
     */
    @GetMapping("/plugins/interfaces/package/{package-name}/entity/{entity-name}/enabled")
    public CommonResponseDto queryAllEnabledPluginConfigInterfaceForEntityName(
            @PathVariable(value = "package-name") String packageName,
            @PathVariable(value = "entity-name") String entityName) {
        return okayWithData(
                pluginConfigMgmtService.queryAllEnabledPluginConfigInterfaceForEntity(packageName, entityName, null));
    }

    /**
     * 
     * @param packageName
     * @param entityName
     * @return
     */
    @PostMapping("/plugins/configs/{plugin-config-id}/interfaces/objectmetas/{object-meta-id}")
    public CommonResponseDto updateObjectMeta(@PathVariable(value = "plugin-config-id") String pluginConfigId,
            @PathVariable(value = "object-meta-id") String objectMetaId,
            @RequestBody CoreObjectMetaDto coreObjectMetaDto) {
        pluginConfigMgmtService.updateObjectMeta(pluginConfigId, objectMetaId, coreObjectMetaDto);
        return okay();
    }

    /**
     * 
     * @param filterRuleDto
     * @return
     */
    @PostMapping("/plugins/interfaces/enabled/query-by-target-entity-filter-rule")
    public CommonResponseDto queryAllEnabledPluginConfigInterfaceByEntityNameAndFilterRule(
            @RequestBody TargetEntityFilterRuleDto filterRuleDto) {
        return okayWithData(
                pluginConfigMgmtService.queryAllEnabledPluginConfigInterfaceForEntityByFilterRule(filterRuleDto));
    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    @PostMapping("/plugins/enable/{plugin-config-id:.+}")
    public CommonResponseDto enablePlugin(@PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigMgmtService.enablePlugin(pluginConfigId));
    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    @PostMapping("/plugins/disable/{plugin-config-id:.+}")
    public CommonResponseDto disablePlugin(@PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigMgmtService.disablePlugin(pluginConfigId));
    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    @GetMapping("/plugins/interfaces/{plugin-config-id:.+}")
    public CommonResponseDto queryPluginConfigInterfaceByConfigId(
            @PathVariable(value = "plugin-config-id") String pluginConfigId) {
        return okayWithData(pluginConfigMgmtService.queryPluginConfigInterfacesByConfigId(pluginConfigId));
    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    @DeleteMapping("/plugins/configs/{plugin-config-id:.+}")
    public CommonResponseDto deletePluginConfigByConfigId(
            @PathVariable(value = "plugin-config-id") String pluginConfigId) {
        pluginConfigMgmtService.deletePluginConfigById(pluginConfigId);
        return okay();
    }

    /**
     * 
     * @param pluginConfigId
     * @param pluginConfigRoleRequestDto
     * @return
     */
    @PostMapping("/plugins/roles/configs/{plugin-config-id:.+}")
    public CommonResponseDto updatePluginConfigRoleBinding(@PathVariable("plugin-config-id") String pluginConfigId,
            @RequestBody PluginConfigRoleRequestDto pluginConfigRoleRequestDto) {
        pluginConfigMgmtService.updatePluginConfigRoleBinding(pluginConfigId, pluginConfigRoleRequestDto);
        return CommonResponseDto.okay();
    }

    /**
     * 
     * @param pluginConfigId
     * @param pluginConfigRoleRequestDto
     * @return
     */
    @DeleteMapping("/plugins/roles/configs/{plugin-config-id:.+}")
    public CommonResponseDto deletePluginConfigRoleBinding(@PathVariable("plugin-config-id") String pluginConfigId,
            @RequestBody PluginConfigRoleRequestDto pluginConfigRoleRequestDto) {
        pluginConfigMgmtService.deletePluginConfigRoleBinding(pluginConfigId, pluginConfigRoleRequestDto);
        return CommonResponseDto.okay();
    }

}
