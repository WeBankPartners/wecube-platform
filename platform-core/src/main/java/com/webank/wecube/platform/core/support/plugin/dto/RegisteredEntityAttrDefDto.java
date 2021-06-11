package com.webank.wecube.platform.core.support.plugin.dto;

public class RegisteredEntityAttrDefDto {
    private String id;
    private String name;
    private String description;
    private String dataType;
    private boolean mandatory = false;

    private String refPackageName;
    private String refEntityName;
    private String refAttrName;

    private String referenceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getRefPackageName() {
        return refPackageName;
    }

    public void setRefPackageName(String refPackageName) {
        this.refPackageName = refPackageName;
    }

    public String getRefEntityName() {
        return refEntityName;
    }

    public void setRefEntityName(String refEntityName) {
        this.refEntityName = refEntityName;
    }

    public String getRefAttrName() {
        return refAttrName;
    }

    public void setRefAttrName(String refAttrName) {
        this.refAttrName = refAttrName;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RegisteredEntityAttrDefDto [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", description=");
        builder.append(description);
        builder.append(", dataType=");
        builder.append(dataType);
        builder.append(", mandatory=");
        builder.append(mandatory);
        builder.append(", refPackageName=");
        builder.append(refPackageName);
        builder.append(", refEntityName=");
        builder.append(refEntityName);
        builder.append(", refAttrName=");
        builder.append(refAttrName);
        builder.append(", referenceId=");
        builder.append(referenceId);
        builder.append("]");
        return builder.toString();
    }

}
