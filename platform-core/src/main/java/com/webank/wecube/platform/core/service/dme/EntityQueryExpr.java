package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;

public class EntityQueryExpr {
	
	private List<EntityQueryExprNodeInfo> exprNodeInfos = new ArrayList<>();
	
	private String exprOperation;
	
	private String rawExpr;

	public List<EntityQueryExprNodeInfo> getExprNodeInfos() {
		return exprNodeInfos;
	}

	public void setExprNodeInfos(List<EntityQueryExprNodeInfo> exprNodeInfos) {
		this.exprNodeInfos = exprNodeInfos;
	}

	public String getExprOperation() {
		return exprOperation;
	}

	public void setExprOperation(String exprOperation) {
		this.exprOperation = exprOperation;
	}
	
	public String getRawExpr() {
		return rawExpr;
	}

	public void setRawExpr(String rawExpr) {
		this.rawExpr = rawExpr;
	}

	public EntityQueryExpr addExprNode(EntityQueryExprNodeInfo exprNodeInfo) {
		if(exprNodeInfo == null) {
			return this;
		}
		
		exprNodeInfos.add(exprNodeInfo);
		
		return this;
	}

}
