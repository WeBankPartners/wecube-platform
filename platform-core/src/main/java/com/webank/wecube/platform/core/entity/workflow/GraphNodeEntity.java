package com.webank.wecube.platform.core.entity.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.webank.wecube.platform.core.entity.BaseTraceableEntity;

@Entity
@Table(name = "CORE_RU_GRAPH_NODE")
public class GraphNodeEntity extends BaseTraceableEntity{
	private static final String IDS_DELIMITER = ",";

	@Id
    @Column(name = "ID")
    @GeneratedValue
	private Integer id;
	
	@Column(name = "PKG_NAME")
	private String packageName;
	
	@Column(name = "ENTITY_NAME")
    private String entityName;
	
	@Column(name = "DATA_ID")
    private String dataId;
	
	@Column(name = "DISPLAY_NAME")
    private String displayName;

	@Column(name = "G_NODE_ID")
    private String graphNodeId;
	
	@Column(name = "PREV_IDS")
    private String previousIds;
	
	@Column(name = "SUCC_IDS")
    private String succeedingIds;
	
	@Column(name = "PROC_SESS_ID")
	private String processSessionId;
	
	@Column(name = "PROC_INST_ID")
	private Integer procInstId;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getDataId() {
		return dataId;
	}
	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getGraphNodeId() {
		return graphNodeId;
	}
	public void setGraphNodeId(String graphNodeId) {
		this.graphNodeId = graphNodeId;
	}
	public String getPreviousIds() {
		return previousIds;
	}
	public void setPreviousIds(String previousIds) {
		this.previousIds = previousIds;
	}
	public String getSucceedingIds() {
		return succeedingIds;
	}
	public void setSucceedingIds(String succeedingIds) {
		this.succeedingIds = succeedingIds;
	}
	public String getProcessSessionId() {
		return processSessionId;
	}
	public void setProcessSessionId(String processSessionId) {
		this.processSessionId = processSessionId;
	}
	public Integer getProcInstId() {
		return procInstId;
	}
	public void setProcInstId(Integer procInstId) {
		this.procInstId = procInstId;
	}
	
	public static String convertIdsListToString(List<String> ids) {
		if(ids == null) {
			return null;
		}
		
		if(ids.isEmpty()) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String id : ids ) {
			sb.append(id).append(IDS_DELIMITER);
		}
		
		return sb.toString();
	}
	
	public static List<String>  convertIdsStringToList(String idString){
		List<String> ids = new ArrayList<>();
		if(idString == null || idString.trim().length() <= 0) {
			return ids;
		}
		
		String [] idStringParts = idString.split(IDS_DELIMITER);
		for(String idStringPart :idStringParts) {
			ids.add(idStringPart);
		}
		
		return ids;
	}
}
