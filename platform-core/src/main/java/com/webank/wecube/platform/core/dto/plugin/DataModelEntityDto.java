package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class DataModelEntityDto extends PluginPackageEntityDto {

    private LeafEntityList leafEntityList;

    public class LeafEntityList {

        private List<BoundInterfaceEntityDto> referenceToEntityList = new ArrayList<>();
        private List<BoundInterfaceEntityDto> referenceByEntityList = new ArrayList<>();

        public List<BoundInterfaceEntityDto> getReferenceToEntityList() {
            return referenceToEntityList;
        }

        public void setReferenceToEntityList(List<BoundInterfaceEntityDto> referenceToEntityList) {
            this.referenceToEntityList = referenceToEntityList;
        }

        public List<BoundInterfaceEntityDto> getReferenceByEntityList() {
            return referenceByEntityList;
        }

        public void setReferenceByEntityList(List<BoundInterfaceEntityDto> referenceByEntityList) {
            this.referenceByEntityList = referenceByEntityList;
        }

        public LeafEntityList(List<BoundInterfaceEntityDto> referenceToEntityList,
                List<BoundInterfaceEntityDto> referenceByEntityList) {
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
