package com.webank.wecube.platform.core.service.plugin;

import java.util.Map;

import com.webank.wecube.platform.core.entity.plugin.ExecutionJobs;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

public class BatchExecutionContext {

    private PluginConfigInterfaces pluginConfigInterface;
    private Map<String, Object> pluginInputParamMap;
    private ExecutionJobs exeJob;

    private ResultData<?> exeResult;

    public PluginConfigInterfaces getPluginConfigInterface() {
        return pluginConfigInterface;
    }

    public void setPluginConfigInterface(PluginConfigInterfaces pluginConfigInterface) {
        this.pluginConfigInterface = pluginConfigInterface;
    }

    public Map<String, Object> getPluginInputParamMap() {
        return pluginInputParamMap;
    }

    public void setPluginInputParamMap(Map<String, Object> pluginInputParamMap) {
        this.pluginInputParamMap = pluginInputParamMap;
    }

    public ExecutionJobs getExeJob() {
        return exeJob;
    }

    public void setExeJob(ExecutionJobs exeJob) {
        this.exeJob = exeJob;
    }

    public ResultData<?> getExeResult() {
        return exeResult;
    }

    public void setExeResult(ResultData<?> exeResult) {
        this.exeResult = exeResult;
    }

}
