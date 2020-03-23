package com.webank.wecube.platform.core.dto;

import java.util.*;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;

public class DataModelEntityDto extends PluginPackageEntityDto {

    private LeafEntityList leafEntityList;

    public class LeafEntityList {
        
        private List<BindedInterfaceEntityDto> referenceToEntityList = new ArrayList<>();
        private List<BindedInterfaceEntityDto> referenceByEntityList = new ArrayList<>();

        public List<BindedInterfaceEntityDto> getReferenceToEntityList() {
            return referenceToEntityList;
        }

        public void setReferenceToEntityList(List<BindedInterfaceEntityDto> referenceToEntityList) {
            this.referenceToEntityList = referenceToEntityList;
        }

        public List<BindedInterfaceEntityDto> getReferenceByEntityList() {
            return referenceByEntityList;
        }

        public void setReferenceByEntityList(List<BindedInterfaceEntityDto> referenceByEntityList) {
            this.referenceByEntityList = referenceByEntityList;
        }

        public LeafEntityList(List<BindedInterfaceEntityDto> referenceToEntityList,
                List<BindedInterfaceEntityDto> referenceByEntityList) {
            super();
            this.referenceToEntityList = referenceToEntityList;
            this.referenceByEntityList = referenceByEntityList;
        }

        public LeafEntityList() {
            super();
            this.referenceToEntityList = new ArrayList<>();
            this.referenceByEntityList = new ArrayList<>();
        }
    }

    public LeafEntityList getLeafEntityList() {
        return leafEntityList;
    }

    public void setLeafEntityList(LeafEntityList leafEntityList) {
        this.leafEntityList = leafEntityList;
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

    public DataModelEntityDto() {
        super();
        this.leafEntityList = new LeafEntityList();
    }
}
