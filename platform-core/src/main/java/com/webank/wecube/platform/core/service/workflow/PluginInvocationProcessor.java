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
import com.webank.wecube.platform.core.support.plugin.PluginInvocationRestClient;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

/**
 * 
 * @author gavin
 *
 */
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
		private PluginInvocationRestClient pluginInvocationRestClient;
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

		public PluginInvocationOperation withPluginInvocationRestClient(PluginInvocationRestClient restClient) {
			this.pluginInvocationRestClient = restClient;
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

			PluginResponse<Object> response = null;

			try {
				List<String> allowedOptions = getAllowedOptions();
				response = getPluginInvocationRestClient().callPluginService(getInstanceHost(), getInterfacePath(),
						getPluginParameters(), this.requestId, allowedOptions);
			} catch (Exception e) {
				log.warn("errors while operating {} {}", getInstanceHost(), getInterfacePath(), e);
				PluginInterfaceInvocationResult errResult = new PluginInterfaceInvocationResult();
				errResult.setErrMsg(e.getMessage());
				errResult.setSuccess(false);
				errResult.setResultData(null);
				errResult.setResultCode(PluginResponse.RESULT_CODE_FAIL);

				handleResult(errResult);

				return;
			}

			if (response == null) {
				log.warn("Plugin call failure due to no response.");
				PluginInterfaceInvocationResult errResult = new PluginInterfaceInvocationResult();
				errResult.setErrMsg("Plugin call failure due to no response.");
				errResult.setSuccess(false);
				errResult.setResultData(null);
				errResult.setResultCode(PluginResponse.RESULT_CODE_FAIL);

				handleResult(errResult);

				return;
			}

			ResultData<Object> responseData = response.getResultData();
			String resultCode = response.getResultCode();

			if (PluginResponse.RESULT_CODE_FAIL.equalsIgnoreCase(resultCode)) {
				log.warn("Plugin service processing failed with code:{}", response.getResultCode());
				PluginInterfaceInvocationResult errResult = new PluginInterfaceInvocationResult();
				errResult.setErrMsg(response.getResultMessage());
				errResult.setSuccess(false);
				errResult.setResultData(responseData == null ? null : responseData.getOutputs());
				errResult.setResultCode(PluginResponse.RESULT_CODE_FAIL);

				handleResult(errResult);

				return;
			}

			if (responseData == null) {
				log.warn("response data is null, {} {}", getInstanceHost(), getInterfacePath());
				PluginInterfaceInvocationResult nullResult = new PluginInterfaceInvocationResult();
				nullResult.setErrMsg("response data is null.");
				nullResult.setSuccess(true);
				nullResult.setResultData(null);
				nullResult.setResultCode(resultCode);
				handleResult(nullResult);

				return;
			}

			List<Object> resultData = responseData.getOutputs();

			PluginInterfaceInvocationResult result = new PluginInterfaceInvocationResult();
			result.setErrMsg(response.getResultMessage());
			result.setResultData(resultData);
			result.setSuccess(true);
			result.setResultCode(resultCode);

			handleResult(result);

		}

		private List<String> getAllowedOptions() {
			if (pluginInterfaceInvocationContext == null) {
				return null;
			}

			if (pluginInterfaceInvocationContext.getPluginInvocationCommand() == null) {
				return null;
			}

			return pluginInterfaceInvocationContext.getPluginInvocationCommand().getAllowedOptions();
		}

		private void handleResult(PluginInterfaceInvocationResult result) {
			if (getCallback() != null) {
				getCallback().accept(result, pluginInterfaceInvocationContext);
			}
		}

		public PluginInvocationRestClient getPluginInvocationRestClient() {
			return pluginInvocationRestClient;
		}

		public void setPluginInvocationRestClient(PluginInvocationRestClient pluginInvocationRestClient) {
			this.pluginInvocationRestClient = pluginInvocationRestClient;
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

		public PluginInterfaceInvocationContext withRequestId(String requestId) {
			this.requestId = requestId;
			return this;
		}

	}

	public static class PluginInterfaceInvocationResult {
		private List<Object> resultData;
		private boolean success;
		private String errMsg;
		private String resultCode;
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

		public String getResultCode() {
			return resultCode;
		}

		public void setResultCode(String resultCode) {
			this.resultCode = resultCode;
		}
	}

}
