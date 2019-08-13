package com.webank.wecube.core.jpa.helper;

import com.google.common.collect.Lists;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.utils.VersionUtils;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

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
