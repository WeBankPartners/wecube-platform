package com.webank.wecube.platform.core.model.workflow;

import java.util.List;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;

public class DmeOutputParamAttr {
    private PluginConfigInterfaces interf;
    private PluginConfigInterfaceParameters interfParam;
    private String paramName;
    private String paramExpr;
    private Object retVal;
    private boolean processed;
    private List<EntityQueryExprNodeInfo> exprNodeInfos;
    public PluginConfigInterfaces getInterf() {
        return interf;
    }
    public void setInterf(PluginConfigInterfaces interf) {
        this.interf = interf;
    }
    public PluginConfigInterfaceParameters getInterfParam() {
        return interfParam;
    }
    public void setInterfParam(PluginConfigInterfaceParameters interfParam) {
        this.interfParam = interfParam;
    }
    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public String getParamExpr() {
        return paramExpr;
    }
    public void setParamExpr(String paramExpr) {
        this.paramExpr = paramExpr;
    }
    public Object getRetVal() {
        return retVal;
    }
    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }
    public boolean isProcessed() {
        return processed;
    }
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    public List<EntityQueryExprNodeInfo> getExprNodeInfos() {
        return exprNodeInfos;
    }
    public void setExprNodeInfos(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        this.exprNodeInfos = exprNodeInfos;
    }
    
    public boolean isRootEntityAttr(){
        if(exprNodeInfos != null && exprNodeInfos.size() == 1){
            return true;
        }
        
        return false;
    }

}
