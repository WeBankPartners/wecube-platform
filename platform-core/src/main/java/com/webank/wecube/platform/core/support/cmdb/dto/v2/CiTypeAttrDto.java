package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class CiTypeAttrDto extends AbstractResourceDto {
    private Integer ciTypeAttrId;
    private Integer ciTypeId;
    private String description;
    private Integer displaySeqNo;
    private Boolean isDisplayed;
    private Boolean isEditable;
    private Boolean isHidden;
    private Boolean isNullable;
    private Boolean isUnique;
    private String inputType;
    private Boolean isDefunct;
    private Boolean isSystem;
    private Integer length;
    private String name;
    private String propertyName;
    private String propertyType;
    private Integer referenceId;
    private Integer searchSeqNo;
    private String specialLogic;
    private String status;
    private String referenceName;
    private Integer referenceType;
    private Boolean isAccessControlled;
    private Boolean isAuto;
    private String autoFillRule;
    private String filterRule;
    private Boolean isRefreshable;

    private CiTypeDto ciType;

    public CiTypeAttrDto() {
    }

    public CiTypeAttrDto(Integer ciTypeAttrId) {
        this.ciTypeAttrId = ciTypeAttrId;
    }

    public Integer getCiTypeAttrId() {
        return ciTypeAttrId;
    }

    public void setCiTypeAttrId(Integer admCiTypeAttrId) {
        this.ciTypeAttrId = admCiTypeAttrId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplaySeqNo() {
        return displaySeqNo;
    }

    public void setDisplaySeqNo(Integer displaySeqNo) {
        this.displaySeqNo = displaySeqNo;
    }

    public Boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public Boolean getIsDefunct() {
        return isDefunct;
    }

    public void setIsDefunct(Boolean isDefunct) {
        this.isDefunct = isDefunct;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getSearchSeqNo() {
        return searchSeqNo;
    }

    public void setSearchSeqNo(Integer searchSeqNo) {
        this.searchSeqNo = searchSeqNo;
    }

    public String getSpecialLogic() {
        return specialLogic;
    }

    public void setSpecialLogic(String specialLogic) {
        this.specialLogic = specialLogic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public Integer getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(Integer referenceType) {
        this.referenceType = referenceType;
    }

    public Boolean getIsAccessControlled() {
        return isAccessControlled;
    }

    public void setIsAccessControlled(Boolean isAccessControlled) {
        this.isAccessControlled = isAccessControlled;
    }


    public CiTypeDto getCiType() {
        return ciType;
    }

    public void setCiType(CiTypeDto ciType) {
        this.ciType = ciType;
    }
    public Boolean getIsAuto() {
        return isAuto;
    }

    public void setIsAuto(Boolean isAuto) {
        this.isAuto = isAuto;
    }
    
    public Boolean getIsDisplayed() {
        return isDisplayed;
    }

    public void setIsDisplayed(Boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public Boolean getIsRefreshable() {
        return isRefreshable;
    }

    public void setIsRefreshable(Boolean isRefreshable) {
        this.isRefreshable = isRefreshable;
    }
}
