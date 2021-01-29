package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.dto.plugin.PluginPackageResourceFileDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageResourceFilesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;

@Service
@Transactional
public class PluginPackageResourceFileService {
    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginPackageResourceFilesMapper pluginPackageResourceFilesMapper;

    public List<PluginPackageResourceFileDto> getAllPluginPackageResourceFiles() {
        List<PluginPackageResourceFileDto> resultDtos = new ArrayList<>();

        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper
                .selectAllLatestUploadedPackages(PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);

        if (pluginPackagesEntities == null || pluginPackagesEntities.isEmpty()) {
            return resultDtos;
        }

        for (PluginPackages pluginPackageEntity : pluginPackagesEntities) {
            List<PluginPackageResourceFiles> resourceFilesEntities = pluginPackageResourceFilesMapper
                    .selectAllByPluginPackage(pluginPackageEntity.getId());
            if(resourceFilesEntities == null){
                continue;
            }
            
            for(PluginPackageResourceFiles resourceFilesEntity : resourceFilesEntities){
                PluginPackageResourceFileDto dto = buildPluginPackageResourceFileDto(resourceFilesEntity);
                resultDtos.add(dto);
            }
        }
        
        return resultDtos;

    }
    
    private PluginPackageResourceFileDto buildPluginPackageResourceFileDto(PluginPackageResourceFiles entity){
        PluginPackageResourceFileDto dto = new PluginPackageResourceFileDto();
        dto.setId(entity.getId());
        dto.setPackageName(entity.getPackageName());
        dto.setPackageVersion(entity.getPackageVersion());
        dto.setRelatedPath(entity.getRelatedPath());
        dto.setSource(entity.getSource());
        
        return dto;
    }
}
