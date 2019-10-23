package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
@JsonInclude(Include.NON_EMPTY)
public class CiTypeDto extends AbstractResourceDto {
	private Integer ciTypeId;
	private Integer ciGlobalUniqueId;
	private Integer catalogId;
	private String description;
	private Integer tenementId;
	private Integer layerId;
	private String name;
	private Integer seqNo;
	private String tableName;
	private String status;
	private Integer zoomLevelId;
	private Integer imageFileId;
	private List<CiTypeAttrDto> attributes = new LinkedList<>();

	public void setCiTypeId(Integer ciTypeId) {
		this.ciTypeId = ciTypeId;
	}

	public Integer getCiGlobalUniqueId() {
		return ciGlobalUniqueId;
	}

	public void setCiGlobalUniqueId(Integer ciGlobalUniqueId) {
		this.ciGlobalUniqueId = ciGlobalUniqueId;
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(Integer ciType) {
		this.catalogId = ciType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getTenementId() {
		return tenementId;
	}

	public void setTenementId(Integer idTenement) {
		this.tenementId = idTenement;
	}

	public Integer getLayerId() {
		return layerId;
	}

	public void setLayerId(Integer layerId) {
		this.layerId = layerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getZoomLevelId() {
		return zoomLevelId;
	}

	public void setZoomLevelId(Integer zoomLevelId) {
		this.zoomLevelId = zoomLevelId;
	}

	public List<CiTypeAttrDto> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CiTypeAttrDto> attributes) {
		this.attributes = attributes;
	}

	public Integer getCiTypeId() {
		return ciTypeId;
	}

	public Integer getImageFileId() {
		return imageFileId;
	}

	public void setImageFileId(Integer imageFileId) {
		this.imageFileId = imageFileId;
	}

}
