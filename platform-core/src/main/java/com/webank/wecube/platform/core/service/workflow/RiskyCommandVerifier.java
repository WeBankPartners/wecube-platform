package com.webank.wecube.platform.core.service.workflow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.plugin.ItsDangerConfirmResultDto;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.core.support.itsdanger.ItsDanerResultDataInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckReqDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerCheckRespDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerInstanceInfoDto;
import com.webank.wecube.platform.core.support.itsdanger.ItsDangerRestClient;
import com.webank.wecube.platform.workflow.WorkflowConstants;

@Service
public class RiskyCommandVerifier extends AbstractPluginInvocationService{
    private static final Logger log = LoggerFactory.getLogger(RiskyCommandVerifier.class);
    
    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;

    @Autowired
    private ItsDangerRestClient itsDangerRestClient;
    
    
    public boolean needPerformDangerousCommandsChecking(TaskNodeInstInfoEntity taskNodeInstEntity,
            TaskNodeDefInfoEntity taskNodeDefEntity) {
        String preCheckResult = taskNodeInstEntity.getPreCheckRet();

        if (StringUtils.isNoneBlank(preCheckResult)) {
            log.info("Such task node already performed risk commands checking:{}:{}", taskNodeInstEntity.getId(),
                    preCheckResult);
            return false;
        }

        if (!TaskNodeDefInfoEntity.PRE_CHECK_YES.equalsIgnoreCase(taskNodeDefEntity.getPreCheck())) {
            log.debug("Task node {} is defined no need to perform high risk commands checking.",
                    taskNodeDefEntity.getId());
            return false;
        }
        
        if (TaskNodeInstInfoEntity.RISKY_STATUS.equals(taskNodeInstEntity.getStatus())) {
            log.debug("Task node {} is already risky status and no need to perform high risk commands checking.",
                    taskNodeDefEntity.getId());
            return false;
        }

        int countRunningPluginInstances = pluginInstancesMapper
                .countAllRunningPluginInstancesByPackage(PLUGIN_NAME_ITSDANGEROUS);
        if (countRunningPluginInstances < 1) {
            log.info(
                    "There is not any running instance currently of package :{}, and no need to perform high risk commands checking.",
                    PLUGIN_NAME_ITSDANGEROUS);
            return false;
        }

        return true;
    }
    
    public ItsDangerConfirmResultDto performDangerousCommandsChecking(PluginInterfaceInvocationContext ctx,
            List<Map<String, Object>> pluginParameters) {
        if (pluginParameters == null || pluginParameters.isEmpty()) {
            log.debug("plugin input parameter is blank and no need to perform risk checking.");
            return null;
        }

        List<ProcExecBindingEntity> nodeObjectBindings = ctx.getNodeObjectBindings();
        if (nodeObjectBindings == null || nodeObjectBindings.isEmpty()) {
            log.debug("object bindings is blank and no need to perform risk checking.");
            return null;
        }
        ItsDangerCheckReqDto req = new ItsDangerCheckReqDto();
        req.setOperator(WorkflowConstants.DEFAULT_USER);
        req.setEntityType(nodeObjectBindings.get(0).getEntityTypeId());
        req.setServiceName(ctx.getPluginConfigInterface().getServiceName());
        req.setServicePath(ctx.getPluginConfigInterface().getPath());

        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {

            ItsDangerInstanceInfoDto instance = new ItsDangerInstanceInfoDto();
            instance.setId(nodeObjectBinding.getEntityDataId());
            instance.setDisplayName(nodeObjectBinding.getEntityDataName());
            req.getEntityInstances().add(instance);

        }

        req.setInputParams(pluginParameters);

        ItsDangerCheckRespDto resp = itsDangerRestClient.checkFromBackend(req);

        if (resp == null) {
            log.debug("response is null.");
            return null;
        }

        ItsDanerResultDataInfoDto respData = resp.getData();
        if (respData == null) {
            log.debug("response data is null.");
            return null;
        }

        List<Object> checkData = respData.getData();

        if (checkData == null || checkData.isEmpty()) {
            log.debug("check data is null.");
            return null;
        }

        ItsDangerConfirmResultDto itsDangerConfirmResultDto = new ItsDangerConfirmResultDto();
        itsDangerConfirmResultDto.setMessage(respData.getText());
        itsDangerConfirmResultDto.setStatus("CONFIRM");

        return itsDangerConfirmResultDto;
    }

}
