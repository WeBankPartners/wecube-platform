package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PluginConfigCopyHelper {

    public void copyPluginConfigs(final PluginPackage sourcePackage, final PluginPackage targetPackage) {
        log.info("About to copy plugin configurations from package[%s/%s] to package[%s/%s].", sourcePackage.getName(), sourcePackage.getVersion(), targetPackage.getName(), targetPackage.getVersion());
        Map<String, PluginConfig> sourceConfigMap = CollectionUtils.asMap(sourcePackage.getPluginConfigs(), PluginConfig::getName);
        for (PluginConfig targetPlugin : targetPackage.getPluginConfigs()) {
            String pluginName = targetPlugin.getName();
            if (targetPlugin.getStatus() != PluginConfig.Status.UNREGISTERED) {
                log.info("Configuration of plugin[%s/%s] has already been configured. Skipped.", pluginName, sourcePackage.getVersion());
                continue;
            }
            PluginConfig sourcePlugin = sourceConfigMap.get(pluginName);
            if (sourcePlugin == null) {
                log.info("Configuration of plugin[%s/%s] not found. Skipped.", pluginName, targetPackage.getVersion());
            }
        }
    }


}
