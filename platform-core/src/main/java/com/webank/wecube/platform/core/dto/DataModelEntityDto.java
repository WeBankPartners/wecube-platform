package com.webank.wecube.platform.core.dto;

import java.util.*;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;

public class DataModelEntityDto extends PluginPackageEntityDto {

    private List<BindedInterfaceEntityDto> bindedInterfaceEntityList = new ArrayList<>();

    public List<BindedInterfaceEntityDto> getBindedInterfaceEntityList() {
        return bindedInterfaceEntityList;
    }

    public void setBindedInterfaceEntityList(List<BindedInterfaceEntityDto> bindedInterfaceEntityList) {
        this.bindedInterfaceEntityList = bindedInterfaceEntityList;
    }

    public static DataModelEntityDto fromDomain(PluginPackageEntity pluginPackageEntity) {
        DataModelEntityDto dataModelEntityDto = new DataModelEntityDto();
        dataModelEntityDto.setId(pluginPackageEntity.getId());
        dataModelEntityDto.setPackageName(pluginPackageEntity.getPluginPackageDataModel().getPackageName());
        dataModelEntityDto.setName(pluginPackageEntity.getName());
        dataModelEntityDto.setDisplayName(pluginPackageEntity.getDisplayName());
        dataModelEntityDto.setDescription(pluginPackageEntity.getDescription());
        dataModelEntityDto.setDataModelVersion(pluginPackageEntity.getPluginPackageDataModel().getVersion());
        if (pluginPackageEntity.getPluginPackageAttributeList() != null) {
            pluginPackageEntity.getPluginPackageAttributeList().forEach(pluginPackageAttribute -> dataModelEntityDto
                    .getAttributes().add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        }
        return dataModelEntityDto;
    }
}
