package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.NOT_CONFIGURED;
import static com.webank.wecube.platform.core.utils.CollectionUtils.asMap;

import java.util.Map;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigFilteringRule;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginConfigCopyHelper {

    public void copyPluginConfigs(final PluginPackage sourcePackage, final PluginPackage targetPackage) {
        log.info("About to copy plugin configurations from package[%s/%s] to package[%s/%s].", sourcePackage.getName(), sourcePackage.getVersion(), targetPackage.getName(), targetPackage.getVersion());
        Map<String, PluginConfig> sourceConfigMap = asMap(sourcePackage.getPluginConfigs(), PluginConfig::getName);
        for (PluginConfig targetPlugin : targetPackage.getPluginConfigs()) {
            String pluginName = targetPlugin.getName();
            if (targetPlugin.getStatus() != NOT_CONFIGURED) {
                log.info("Configuration of plugin[%s/%s] has already been configured. Skipped.", pluginName, sourcePackage.getVersion());
                continue;
            }
            PluginConfig sourcePlugin = sourceConfigMap.get(pluginName);
            if (sourcePlugin == null) {
                log.info("Configuration of plugin[%s/%s] not found. Skipped.", pluginName, targetPackage.getVersion());
            } else if (sourcePlugin.getCmdbCiTypeId() == null || sourcePlugin.getStatus() == NOT_CONFIGURED){
                log.info("Configuration of plugin[%s/%s] has not been setup. Skipped.", pluginName, targetPackage.getVersion());
            } else {
                copyPluginConfig(sourceConfigMap.get(pluginName), targetPlugin);
            }
        }
    }

    private void copyPluginConfig(final PluginConfig sourceConfig, final PluginConfig targetConfig) {
        log.info("Copying config of plugin[%s]", targetConfig.getName());
        targetConfig.setCmdbCiTypeId(sourceConfig.getCmdbCiTypeId());
        sourceConfig.getFilteringRules().stream()
                .map(rule -> {
                    PluginConfigFilteringRule newRule = cloneFilteringRule(rule);
                    newRule.setPluginConfig(targetConfig);
                    return newRule;
                }).forEach(targetConfig::addPluginConfigFilteringRule);
        Map<String, PluginConfigInterface> sourceInterfaceMap = asMap(sourceConfig.getInterfaces(), PluginConfigInterface::getName);
        for (PluginConfigInterface targetInterface : targetConfig.getInterfaces()) {
            String interfaceName = targetInterface.getName();
            if (sourceInterfaceMap.containsKey(interfaceName)) {
                copyInterfaceConfig(sourceInterfaceMap.get(interfaceName), targetInterface);
            } else {
                log.info("Configuration of plugin interface [%s] not found. Skipped.", interfaceName);
            }
        }
    }

    private void copyInterfaceConfig(final PluginConfigInterface sourceInterface, final PluginConfigInterface targetInterface) {
        log.info("Copying config of plugin interface [%s]", targetInterface.getName());
        targetInterface.setFilterStatus(sourceInterface.getFilterStatus());
        targetInterface.setResultStatus(sourceInterface.getResultStatus());
        Map<String, PluginConfigInterfaceParameter> sourceInputParameters = asMap(sourceInterface.getInputParameters(), PluginConfigInterfaceParameter::getName);
        for (PluginConfigInterfaceParameter targetParameter : targetInterface.getInputParameters()) {
            String parameterName = targetParameter.getName();
            if (sourceInputParameters.containsKey(parameterName)) {
                copyParameterConfig(sourceInputParameters.get(parameterName), targetParameter);
            } else {
                log.info("Input parameter[%s] of plugin interface [%s] not found.", parameterName, targetInterface.getName());
            }
        }

        Map<String, PluginConfigInterfaceParameter> sourceOutputParameters = asMap(sourceInterface.getOutputParameters(), PluginConfigInterfaceParameter::getName);
        for (PluginConfigInterfaceParameter targetParameter : targetInterface.getOutputParameters()) {
            String parameterName = targetParameter.getName();
            if (sourceOutputParameters.containsKey(parameterName)) {
                copyParameterConfig(sourceOutputParameters.get(parameterName), targetParameter);
            } else {
                log.info("Output parameter[%s] of plugin interface [%s] not found. Skipped.", parameterName, targetInterface.getName());
            }
        }
    }

    private void copyParameterConfig(final PluginConfigInterfaceParameter sourceParameter, final PluginConfigInterfaceParameter targetParameter) {
        targetParameter.setMappingType(sourceParameter.getMappingType());
        targetParameter.setCmdbAttributeId(sourceParameter.getCmdbAttributeId());
        targetParameter.setCmdbCitypeId(sourceParameter.getCmdbCitypeId());
        targetParameter.setCmdbCitypePath(sourceParameter.getCmdbCitypePath());
        targetParameter.setCmdbColumnSource(sourceParameter.getCmdbColumnSource());
        targetParameter.setCmdbColumnName(sourceParameter.getCmdbColumnName());
        targetParameter.setCmdbEnumCode(sourceParameter.getCmdbEnumCode());
    }

    private PluginConfigFilteringRule cloneFilteringRule(PluginConfigFilteringRule rule) {
        PluginConfigFilteringRule newRule = new PluginConfigFilteringRule();
        newRule.setCmdbAttributeId(rule.getCmdbAttributeId());
        newRule.setFilteringValues(rule.getFilteringValues());
        newRule.setCmdbColumnName(rule.getCmdbColumnName());
        return newRule;
    }


}
