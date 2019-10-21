package com.webank.wecube.platform.core.service.workflow;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcessDefinitionInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInfoDto;
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;
import com.webank.wecube.platform.workflow.parse.BpmnParseAttachment;
import com.webank.wecube.platform.workflow.parse.BpmnProcessModelCustomizer;
import com.webank.wecube.platform.workflow.parse.SubProcessAdditionalInfo;

@Service
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    private static final String BPMN_SUFFIX = ".bpmn20.xml";

    private String encoding = "UTF-8";

    @Autowired
    private RepositoryService repositoryService;

    public ProcessDefinition deployProcessDefinition(ProcessDefinitionInfoDto procDefDto) {
        try {
            return doDeployProcessDefinition(procDefDto);
        } catch (Exception e) {
            log.error("errors while deploy process definition", e);
            throw new BpmnCustomizationException(e.getMessage());
        }
    }

    protected ProcessDefinition doDeployProcessDefinition(ProcessDefinitionInfoDto procDefDto) {
        String fileName = procDefDto.getProcDefName() + BPMN_SUFFIX;
        BpmnParseAttachment bpmnParseAttachment = buildBpmnParseAttachment(procDefDto.getTaskNodeInfos());

        BpmnProcessModelCustomizer customizer = new BpmnProcessModelCustomizer(fileName, procDefDto.getProcDefData(),
                encoding);
        customizer.setBpmnParseAttachment(bpmnParseAttachment);
        BpmnModelInstance procModelInstance = customizer.build();

        DeploymentWithDefinitions deployment = repositoryService.createDeployment()
                .addModelInstance(fileName, procModelInstance).deployWithResult();
        List<ProcessDefinition> processDefs = deployment.getDeployedProcessDefinitions();

        if (processDefs == null || processDefs.isEmpty()) {
            log.error("abnormally to parse process definition,request={}", procDefDto);
            throw new WecubeCoreException("process deploying failed");
        }

        ProcessDefinition processDef = processDefs.get(0);

        return processDef;
    }

    private BpmnParseAttachment buildBpmnParseAttachment(List<TaskNodeInfoDto> taskNodeInfoDtos) {
        BpmnParseAttachment bpmnParseAttachment = new BpmnParseAttachment();

        for (TaskNodeInfoDto dto : taskNodeInfoDtos) {
            SubProcessAdditionalInfo info = new SubProcessAdditionalInfo();
            info.setSubProcessNodeId(dto.getNodeId());
            info.setSubProcessNodeName(dto.getNodeName());
            info.setTimeoutExpression(convertIsoTimeFormat(dto.getTimeoutExpression()));

            bpmnParseAttachment.addSubProcessAddtionalInfo(info);
        }

        return bpmnParseAttachment;
    }

    private String convertIsoTimeFormat(String timeoutExpression) {
        if (StringUtils.isBlank(timeoutExpression)) {
            return timeoutExpression;
        }

        return "PT" + timeoutExpression.trim() + "M";
    }
}
