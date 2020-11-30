package com.webank.wecube.platform.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.PluginPackageEntityDto;

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

    public DataModelEntityDto() {
        super();
        this.leafEntityList = new LeafEntityList();
    }
}
