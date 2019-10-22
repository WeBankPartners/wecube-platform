package com.webank.wecube.platform.core.jpa.helper;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.utils.VersionUtils;

public class PluginConfigUtil {

    public static List<PluginConfigInterface> uniqueByServiceName(List<PluginConfigInterface> pluginConfigInterfaces) {
        if (isNotEmpty(pluginConfigInterfaces)) {
            Map<String, PluginConfigInterface> pluginConfigInterfaceMap = new TreeMap<>();
            for (PluginConfigInterface inf : pluginConfigInterfaces) {
                String serviceName = inf.getServiceName();
                if (pluginConfigInterfaceMap.containsKey(serviceName)) {
                    PluginConfigInterface duplicatedInf = pluginConfigInterfaceMap.get(serviceName);
                    if (VersionUtils.isLeftVersionGreatThanRightVersion(
                            inf.getPluginConfig().getPluginPackage().getVersion(),
                            duplicatedInf.getPluginConfig().getPluginPackage().getVersion())) {
                        pluginConfigInterfaceMap.put(serviceName, inf);
                    } else {
                        continue;
                    }
                } else {
                    pluginConfigInterfaceMap.put(serviceName, inf);
                }
            }
            return Lists.newArrayList(pluginConfigInterfaceMap.values());
        }
        return Collections.emptyList();
    }

}
