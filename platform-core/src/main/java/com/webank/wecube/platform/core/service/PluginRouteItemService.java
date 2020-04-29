package com.webank.wecube.platform.core.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.dto.PluginRouteItemDto;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;

@Service
public class PluginRouteItemService {
	private static final Logger log = LoggerFactory.getLogger(PluginRouteItemService.class);
	
	public static final String HTTP_SCHEME = "http";

    @Autowired
    private PluginInstanceRepository pluginInstanceRepository;
    
    @Autowired
    private PluginConfigInterfaceRepository pluginConfigInterfaceRepository;

    public List<PluginRouteItemDto> getAllPluginRouteItems() {

        List<PluginRouteItemDto> resultList = new LinkedList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository.findAllByContainerStatus(PluginInstance.CONTAINER_STATUS_RUNNING);

        //1 assemble default route for each context
        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getPluginPackage().getName());

                resultList.add(d);
            });
        }
        
        //2 assemble routes for each interface
        List<PluginRouteItemDto> interfaceRoutes = tryCalculateInterfaceRoutes();
        if(interfaceRoutes != null && !interfaceRoutes.isEmpty()) {
        	resultList.addAll(interfaceRoutes);
        }
        
        if(log.isInfoEnabled()) {
        	log.info("total {} routes got to push.", resultList.size());
        }

        return resultList;
    }
    
    private List<PluginRouteItemDto> tryCalculateInterfaceRoutes(){
    	List<PluginRouteItemDto> routeItemList = new LinkedList<>();
    	
    	List<PluginConfigInterface> interfaces = pluginConfigInterfaceRepository.findAllEnabledInterfaces();
    	if(interfaces == null || interfaces.isEmpty()) {
    		return routeItemList;
    	}
    	
    	for(PluginConfigInterface intf : interfaces) {
    		if(intf == null) {
    			continue;
    		}
    		
    		PluginRouteItemDto routeItem = tryCalculatePluginRouteItem(intf);
    		routeItemList.add(routeItem);
    	}
    	
    	return routeItemList;
    }
    
    private PluginRouteItemDto tryCalculatePluginRouteItem(PluginConfigInterface intf) {
    	PluginConfig pluginConfig = intf.getPluginConfig();
    	
    	
    	
    	return null;
    }

    public List<PluginRouteItemDto> getPluginRouteItemsByName(String name) {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository
                .findAllByContainerStatusAndInstanceName(PluginInstance.CONTAINER_STATUS_RUNNING, name);

        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getInstanceName());

                dtos.add(d);
            });
        }

        return dtos;
    }

}
