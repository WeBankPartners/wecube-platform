package com.webank.wecube.platform.core.service.workflow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

@Service
public class PluginInvocationProcessor {

    private static final Logger log = LoggerFactory.getLogger(PluginInvocationProcessor.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void process(PluginInvocationOperation operation) {

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                operation.operate();
            }

        });
    }

    public static class PluginInvocationOperation implements PluginOperation {
        private PluginServiceStub pluginServiceStub;
        private BiConsumer<PluginInterfaceInvocationResult, PluginInterfaceInvocationContext> callback;
        private List<Map<String, Object>> pluginParameters;
        private String interfacePath;
        private String instanceHost;

        private String requestId;

        private PluginInterfaceInvocationContext pluginInterfaceInvocationContext;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public PluginInterfaceInvocationContext getPluginInterfaceInvocationContext() {
            return pluginInterfaceInvocationContext;
        }

        public void setPluginInterfaceInvocationContext(
                PluginInterfaceInvocationContext pluginInterfaceInvocationContext) {
            this.pluginInterfaceInvocationContext = pluginInterfaceInvocationContext;
        }

        public PluginServiceStub getPluginServiceStub() {
            return pluginServiceStub;
        }

        public void setPluginServiceStub(PluginServiceStub pluginServiceStub) {
            this.pluginServiceStub = pluginServiceStub;
        }

        public BiConsumer<PluginInterfaceInvocationResult, PluginInterfaceInvocationContext> getCallback() {
            return callback;
        }

        public void setCallback(
                BiConsumer<PluginInterfaceInvocationResult, PluginInterfaceInvocationContext> callback) {
            this.callback = callback;
        }

        public List<Map<String, Object>> getPluginParameters() {
            return pluginParameters;
        }

        public void setPluginParameters(List<Map<String, Object>> pluginParameters) {
            this.pluginParameters = pluginParameters;
        }

        public String getInterfacePath() {
            return interfacePath;
        }

        public void setInterfacePath(String interfacePath) {
            this.interfacePath = interfacePath;
        }

        public String getInstanceHost() {
            return instanceHost;
        }

        public void setInstanceHost(String instanceHost) {
            this.instanceHost = instanceHost;
        }

        public PluginInvocationOperation withInstanceHost(String instanceHost) {
            this.instanceHost = instanceHost;
            return this;
        }

        public PluginInvocationOperation withInterfacePath(String interfacePath) {
            this.interfacePath = interfacePath;
            return this;
        }

        public PluginInvocationOperation withPluginParameters(List<Map<String, Object>> pluginParameters) {
            this.pluginParameters = pluginParameters;
            return this;
        }

        public PluginInvocationOperation withCallback(
                BiConsumer<PluginInterfaceInvocationResult, PluginInterfaceInvocationContext> callback) {
            this.callback = callback;
            return this;
        }

        public PluginInvocationOperation withPluginServiceStub(PluginServiceStub pluginServiceStub) {
            this.pluginServiceStub = pluginServiceStub;
            return this;
        }

        public PluginInvocationOperation withPluginInterfaceInvocationContext(PluginInterfaceInvocationContext ctx) {
            this.pluginInterfaceInvocationContext = ctx;
            return this;
        }

        public PluginInvocationOperation withRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        @Override
        public void operate() {
            if (log.isDebugEnabled()) {
                log.debug("call {} {} - {}", getInstanceHost(), getInterfacePath(), Thread.currentThread().getName());
            }

            ResultData<Object> responseData = null;

            try {
                responseData = getPluginServiceStub().callPluginInterface(getInstanceHost(), getInterfacePath(),
                        getPluginParameters());
            } catch (Exception e) {
                log.error("errors while operating {} {}", getInstanceHost(), getInterfacePath(), e);
                PluginInterfaceInvocationResult errResult = new PluginInterfaceInvocationResult();
                errResult.setErrMsg(e.getMessage());
                errResult.setSuccess(false);

                handleResult(errResult);

                return;
            }

            if (responseData == null) {
                log.error("response data is null, {} {}", getInstanceHost(), getInterfacePath());
                PluginInterfaceInvocationResult nullResult = new PluginInterfaceInvocationResult();
                nullResult.setErrMsg("response data is null.");
                nullResult.setSuccess(true);
                nullResult.setResultData(null);
                handleResult(nullResult);

                return;
            }

            List<Object> resultData = responseData.getOutputs();

            PluginInterfaceInvocationResult result = new PluginInterfaceInvocationResult();
            result.setResultData(resultData);
            result.setSuccess(true);

            handleResult(result);

        }

        private void handleResult(PluginInterfaceInvocationResult result) {
            if (getCallback() != null) {
                getCallback().accept(result, pluginInterfaceInvocationContext);
            }
        }

    }

    public static class PluginInterfaceInvocationContext {
        private ProcInstInfoEntity procInstEntity;
        private TaskNodeInstInfoEntity taskNodeInstEntity;
        private PluginConfigInterface pluginConfigInterface;
        private List<ProcExecBindingEntity> nodeObjectBindings;
        private List<Map<String, Object>> pluginParameters;
        private String interfacePath;
        private String instanceHost;

        private String requestId;

        private TaskNodeExecRequestEntity taskNodeExecRequestEntity;
        private TaskNodeDefInfoEntity taskNodeDefEntity;
        private PluginInvocationCommand pluginInvocationCommand;

        public ProcInstInfoEntity getProcInstEntity() {
            return procInstEntity;
        }

        public void setProcInstEntity(ProcInstInfoEntity procInstEntity) {
            this.procInstEntity = procInstEntity;
        }

        public TaskNodeInstInfoEntity getTaskNodeInstEntity() {
            return taskNodeInstEntity;
        }

        public void setTaskNodeInstEntity(TaskNodeInstInfoEntity taskNodeInstEntity) {
            this.taskNodeInstEntity = taskNodeInstEntity;
        }

        public PluginConfigInterface getPluginConfigInterface() {
            return pluginConfigInterface;
        }

        public void setPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
            this.pluginConfigInterface = pluginConfigInterface;
        }

        public List<ProcExecBindingEntity> getNodeObjectBindings() {
            return nodeObjectBindings;
        }

        public void setNodeObjectBindings(List<ProcExecBindingEntity> nodeObjectBindings) {
            this.nodeObjectBindings = nodeObjectBindings;
        }

        public List<Map<String, Object>> getPluginParameters() {
            return pluginParameters;
        }

        public void setPluginParameters(List<Map<String, Object>> pluginParameters) {
            this.pluginParameters = pluginParameters;
        }

        public String getInterfacePath() {
            return interfacePath;
        }

        public void setInterfacePath(String interfacePath) {
            this.interfacePath = interfacePath;
        }

        public String getInstanceHost() {
            return instanceHost;
        }

        public void setInstanceHost(String instanceHost) {
            this.instanceHost = instanceHost;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public TaskNodeDefInfoEntity getTaskNodeDefEntity() {
            return taskNodeDefEntity;
        }

        public void setTaskNodeDefEntity(TaskNodeDefInfoEntity taskNodeDefEntity) {
            this.taskNodeDefEntity = taskNodeDefEntity;
        }

        public TaskNodeExecRequestEntity getTaskNodeExecRequestEntity() {
            return taskNodeExecRequestEntity;
        }

        public void setTaskNodeExecRequestEntity(TaskNodeExecRequestEntity taskNodeExecRequestEntity) {
            this.taskNodeExecRequestEntity = taskNodeExecRequestEntity;
        }

        public PluginInvocationCommand getPluginInvocationCommand() {
            return pluginInvocationCommand;
        }

        public void setPluginInvocationCommand(PluginInvocationCommand pluginInvocationCommand) {
            this.pluginInvocationCommand = pluginInvocationCommand;
        }

        public PluginInterfaceInvocationContext withNodeObjectBindings(List<ProcExecBindingEntity> nodeObjectBindings) {
            this.nodeObjectBindings = nodeObjectBindings;
            return this;
        }

        public PluginInterfaceInvocationContext withPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
            this.pluginConfigInterface = pluginConfigInterface;
            return this;
        }

        public PluginInterfaceInvocationContext withProcInstEntity(ProcInstInfoEntity procInstEntity) {
            this.procInstEntity = procInstEntity;
            return this;
        }

        public PluginInterfaceInvocationContext withTaskNodeInstEntity(TaskNodeInstInfoEntity taskNodeInstEntity) {
            this.taskNodeInstEntity = taskNodeInstEntity;
            return this;
        }

        public PluginInterfaceInvocationContext withTaskNodeExecRequestEntity(
                TaskNodeExecRequestEntity taskNodeExecRequestEntity) {
            this.taskNodeExecRequestEntity = taskNodeExecRequestEntity;
            return this;
        }

        public PluginInterfaceInvocationContext withTaskNodeDefEntity(TaskNodeDefInfoEntity taskNodeDefEntity) {
            this.taskNodeDefEntity = taskNodeDefEntity;
            return this;
        }

        public PluginInterfaceInvocationContext withPluginInvocationCommand(
                PluginInvocationCommand pluginInvocationCommand) {
            this.pluginInvocationCommand = pluginInvocationCommand;
            return this;
        }

    }

    public static class PluginInterfaceInvocationResult {
        private List<Object> resultData;
        private boolean success;
        private String errMsg;
        private Exception error;

        public List<Object> getResultData() {
            return resultData;
        }

        public void setResultData(List<Object> resultData) {
            this.resultData = resultData;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

        public Exception getError() {
            return error;
        }

        public void setError(Exception error) {
            this.error = error;
        }

        public boolean hasErrors() {
            return !(error == null);
        }

    }

}
