package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.dme.TreeNode;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;

@Service
public class WorkflowDataService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    protected TaskNodeExecParamRepository taskNodeExecParamRepository;

    @Autowired
    protected TaskNodeExecRequestRepository taskNodeExecRequestRepository;

    public TaskNodeExecContextDto getTaskNodeContextInfo(Integer procInstId, Integer nodeInstId) {
        Optional<TaskNodeInstInfoEntity> nodeEntityOpt = taskNodeInstInfoRepository.findById(nodeInstId);
        if (!nodeEntityOpt.isPresent()) {
            throw new WecubeCoreException("Invalid node instance id:" + nodeInstId);
        }

        TaskNodeInstInfoEntity nodeEntity = nodeEntityOpt.get();

        TaskNodeExecContextDto result = new TaskNodeExecContextDto();
        result.setNodeDefId(nodeEntity.getNodeDefId());
        result.setNodeId(nodeEntity.getNodeId());
        result.setNodeInstId(nodeEntity.getId());
        result.setNodeName(nodeEntity.getNodeName());
        result.setNodeType(nodeEntity.getNodeType());
        if (StringUtils.isNotBlank(nodeEntity.getErrorMessage())) {
            result.setErrorMessage(nodeEntity.getErrorMessage());
        }

        TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
                .findCurrentEntityByNodeInstId(nodeEntity.getId());

        if (requestEntity == null) {
            return result;
        }

        result.setRequestId(requestEntity.getRequestId());
        result.setErrorCode(requestEntity.getErrorCode());
        if (StringUtils.isNotBlank(result.getErrorMessage())) {
            result.setErrorMessage(result.getErrorMessage() + "|" + requestEntity.getErrorMessage());
        } else {
            result.setErrorMessage(requestEntity.getErrorMessage());
        }

        List<TaskNodeExecParamEntity> requestParamEntities = taskNodeExecParamRepository.findAllByRequestIdAndParamType(
                requestEntity.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);

        List<TaskNodeExecParamEntity> responseParamEntities = taskNodeExecParamRepository
                .findAllByRequestIdAndParamType(requestEntity.getRequestId(),
                        TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);

        List<RequestObjectDto> requestObjects = calculateRequestObjectDtos(requestParamEntities, responseParamEntities);

        requestObjects.forEach(result::addRequestObjects);

        return result;
    }

    private boolean isSensitiveData(TaskNodeExecParamEntity respParamEntity) {
        if (respParamEntity == null) {
            return false;
        }

        if (respParamEntity.getSensitive() == null) {
            return false;
        }

        if (Boolean.TRUE.equals(respParamEntity.getSensitive())) {
            return true;

        }

        return false;

    }

    private Map<String, Map<String, String>> calculateRespParamsByObjectId(
            List<TaskNodeExecParamEntity> requestParamEntities, List<TaskNodeExecParamEntity> responseParamEntities) {
        Map<String, Map<String, String>> respParamsByObjectId = new HashMap<String, Map<String, String>>();
        if (responseParamEntities != null) {
            for (TaskNodeExecParamEntity respParamEntity : responseParamEntities) {
                Map<String, String> respParamsMap = respParamsByObjectId.get(respParamEntity.getObjectId());
                if (respParamsMap == null) {
                    respParamsMap = new HashMap<String, String>();
                    respParamsByObjectId.put(respParamEntity.getObjectId(), respParamsMap);
                }
                if (isSensitiveData(respParamEntity)) {
                    respParamsMap.put(respParamEntity.getParamName(), "***MASK***");
                } else {
                    respParamsMap.put(respParamEntity.getParamName(), respParamEntity.getParamDataValue());
                }

            }
        }

        return respParamsByObjectId;
    }

    private Map<String, RequestObjectDto> calculateRequestObjects(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        Map<String, RequestObjectDto> objs = new HashMap<>();
        for (TaskNodeExecParamEntity rp : requestParamEntities) {
            RequestObjectDto ro = objs.get(rp.getObjectId());
            if (ro == null) {
                ro = new RequestObjectDto();
                objs.put(rp.getObjectId(), ro);
            }

            if (isSensitiveData(rp)) {
                ro.addInput(rp.getParamName(), "***MASK***");
            } else {
                ro.addInput(rp.getParamName(), rp.getParamDataValue());
            }
        }

        return objs;
    }

    private List<RequestObjectDto> calculateRequestObjectDtos(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        List<RequestObjectDto> requestObjects = new ArrayList<>();

        if (requestParamEntities == null) {
            return requestObjects;
        }

        Map<String, Map<String, String>> respParamsByObjectId = calculateRespParamsByObjectId(requestParamEntities,
                responseParamEntities);

        Map<String, RequestObjectDto> objs = calculateRequestObjects(requestParamEntities, responseParamEntities);

        for (String objectId : objs.keySet()) {
            RequestObjectDto obj = objs.get(objectId);
            Map<String, String> respParamsMap = respParamsByObjectId.get(objectId);
            if (respParamsMap != null) {
                respParamsMap.forEach((k, v) -> {
                    obj.addOutput(k, v);
                });
            }

            requestObjects.add(obj);
        }

        return requestObjects;
    }

    public List<InterfaceParameterDto> getTaskNodeParameters(String procDefId, String nodeDefId) {
        List<InterfaceParameterDto> result = new ArrayList<>();
        Optional<TaskNodeDefInfoEntity> entityOptional = taskNodeDefInfoRepository.findById(nodeDefId);
        if (!entityOptional.isPresent()) {
            return result;
        }

        TaskNodeDefInfoEntity e = entityOptional.get();
        String serviceId = e.getServiceId();

        if (StringUtils.isBlank(serviceId)) {
            log.debug("service id is present for {}", nodeDefId);
            return result;
        }

        PluginConfigInterface pci = pluginConfigService.getPluginConfigInterfaceByServiceName(serviceId);
        Set<PluginConfigInterfaceParameter> inputParameters = pci.getInputParameters();
        Set<PluginConfigInterfaceParameter> outputParameters = pci.getOutputParameters();

        inputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        outputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        return result;
    }

    private InterfaceParameterDto buildInterfaceParameterDto(PluginConfigInterfaceParameter p) {
        InterfaceParameterDto d = new InterfaceParameterDto();
        d.setType(p.getType());
        d.setName(p.getName());
        d.setDataType(p.getDataType());

        return d;
    }

    public List<GraphNodeDto> getProcessDataPreview(String procDefId, String dataId) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(dataId)) {
            throw new WecubeCoreException("Process definition ID or entity ID is not provided.");
        }

        ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

        if (procDefOutline == null) {
            log.debug("process definition with id {} does not exist.", procDefId);
            throw new WecubeCoreException(String.format("Such process definition {%s} does not exist.", procDefId));
        }

        return doFetchProcessPreviewData(procDefOutline, dataId);

    }

    protected List<GraphNodeDto> doFetchProcessPreviewData(ProcDefOutlineDto outline, String dataId) {

        List<GraphNodeDto> result = new ArrayList<>();

        for (FlowNodeDefDto f : outline.getFlowNodes()) {
            String nodeType = f.getNodeType();

            if (!"subProcess".equals(nodeType)) {
                continue;
            }

            String routineExpr = calculateDataModelExpression(f);

            if (StringUtils.isBlank(routineExpr)) {
                log.info("the routine expression is blank for {} {}", f.getNodeDefId(), f.getNodeName());
                continue;
            }

            log.info("About to fetch data for node {} {}", f.getNodeDefId(), f.getNodeName());

            log.info("About to fetch data with expression {} and data id {}", routineExpr, dataId);
            EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, dataId);
            List<TreeNode> nodes = null;
            try {
                nodes = standardEntityOperationService.generatePreviewTree(condition);
            } catch (Exception e) {
                log.error("errors while fetching data with expr {} and data id {}", routineExpr, dataId, e);
                throw new WecubeCoreException(e.getMessage());
            }

            if (nodes == null || nodes.isEmpty()) {
                log.warn("None data returned for {} and {}", routineExpr, dataId);
                continue;
            }

            log.info("total {} records returned for {} and {}", nodes.size(), routineExpr, dataId);

            for (TreeNode tn : nodes) {
                String treeNodeId = buildId(tn);
                GraphNodeDto currNode = findGraphNodeDtoById(result, treeNodeId);
                if (currNode == null) {
                    currNode = new GraphNodeDto();
                    currNode.setDataId(tn.getRootId().toString());
                    currNode.setPackageName(tn.getPackageName());
                    currNode.setEntityName(tn.getEntityName());
                    currNode.setDisplayName(tn.getDisplayName() == null ? null : tn.getDisplayName().toString());

                    addToResult(result, currNode);
                }

                TreeNode parentTreeNode = tn.getParent();
                if (parentTreeNode != null) {
                    String parentTreeNodeId = buildId(parentTreeNode);
                    currNode.addPreviousIds(parentTreeNodeId);
                }

                List<TreeNode> childrenTreeNodes = tn.getChildren();
                if (childrenTreeNodes != null) {
                    for (TreeNode ctn : childrenTreeNodes) {
                        String ctnId = buildId(ctn);
                        currNode.addSucceedingIds(ctnId);
                    }
                }
            }
        }

        return result;

    }

    private String calculateDataModelExpression(FlowNodeDefDto f) {
        if (StringUtils.isBlank(f.getRoutineExpression())) {
            return null;
        }

        String expr = f.getRoutineExpression();

        if (StringUtils.isBlank(f.getServiceId())) {
            return expr;
        }

        PluginConfigInterface inter = pluginConfigService.getPluginConfigInterfaceByServiceName(f.getServiceId());
        if (inter == null) {
            return expr;
        }

        if (StringUtils.isBlank(inter.getFilterRule())) {
            return expr;
        }

        return expr + inter.getFilterRule();
    }

    private void addToResult(List<GraphNodeDto> result, GraphNodeDto... nodes) {
        for (GraphNodeDto n : nodes) {
            if (result.contains(n)) {
                continue;
            }

            GraphNodeDto exist = findGraphNodeDtoById(result, n.getId());
            if (exist == null) {
                result.add(n);
            }
        }
    }

    private GraphNodeDto findGraphNodeDtoById(List<GraphNodeDto> result, String id) {
        for (GraphNodeDto n : result) {
            if (n.getId().equals(id)) {
                return n;
            }
        }

        return null;
    }

    private String buildId(TreeNode n) {
        return String.format("%s:%s:%s", n.getPackageName(), n.getEntityName(), n.getRootId());
    }

}
