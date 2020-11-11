package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.dto.PluginPackageInfoDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.utils.DateUtils;

public class PluginPackageMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    public static final String PLATFORM_NAME = "platform";

    @Autowired
    private ScpService scpService;
    @Autowired
    private CommandService commandService;
    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    public List<PluginPackageInfoDto> getPluginPackages() {
        List<PluginPackageInfoDto> pluginPackageInfoDtos = new ArrayList<>();

        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper.selectAll();
        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return pluginPackageInfoDtos;
        }

        for (PluginPackages entity : pluginPackageEntities) {
            PluginPackageInfoDto dto = buildPluginPackageInfoDto(entity);

            pluginPackageInfoDtos.add(dto);
        }

        return pluginPackageInfoDtos;
    }

    public List<PluginPackageInfoDto> getDistinctPluginPackages() {
        List<PluginPackageInfoDto> pluginPackageInfoDtos = new ArrayList<>();
        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper.selectAllDistinctPackages();
        
        if(pluginPackageEntities == null || pluginPackageEntities.isEmpty()){
            return pluginPackageInfoDtos;
        }
        
        for (PluginPackages entity : pluginPackageEntities) {
            PluginPackageInfoDto dto = buildPluginPackageInfoDto(entity);

            pluginPackageInfoDtos.add(dto);
        }

        return pluginPackageInfoDtos;
        
    }
    
    private PluginPackageInfoDto buildPluginPackageInfoDto(PluginPackages entity){
        PluginPackageInfoDto dto = new PluginPackageInfoDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setUiPackageIncluded(entity.getUiPackageIncluded());
        dto.setUploadTimestamp(DateUtils.dateToString(entity.getUploadTimestamp()));
        dto.setVersion(entity.getVersion());
        
        return dto;
    }

}
