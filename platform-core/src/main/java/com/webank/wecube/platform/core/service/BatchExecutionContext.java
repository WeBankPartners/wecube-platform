package com.webank.wecube.platform.core.service;

import java.util.Map;

import com.webank.wecube.platform.core.domain.ExecutionJob;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

public class BatchExecutionContext {

    private PluginConfigInterface pluginConfigInterface;
    private Map<String, Object> pluginInputParamMap;
    private ExecutionJob exeJob;

    private ResultData<?> exeResult;

    public PluginConfigInterface getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
    }

    public Map<String, Object> getPluginInputParamMap() {
        return pluginInputParamMap;
    }

    public void setPluginInputParamMap(Map<String, Object> pluginInputParamMap) {
        this.pluginInputParamMap = pluginInputParamMap;
    }

    public ExecutionJob getExeJob() {
        return exeJob;
    }

    public void setExeJob(ExecutionJob exeJob) {
        this.exeJob = exeJob;
    }

    public ResultData<?> getExeResult() {
        return exeResult;
    }

    public void setExeResult(ResultData<?> exeResult) {
        this.exeResult = exeResult;
    }

}
