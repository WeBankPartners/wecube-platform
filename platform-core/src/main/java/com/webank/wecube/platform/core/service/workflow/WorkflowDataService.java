package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;
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
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.service.DataModelExpressionService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.support.datamodel.TreeNode;

@Service
public class WorkflowDataService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepo;

    @Autowired
    private DataModelExpressionService dataModelExpressionService;

    @Autowired
    private PluginConfigService pluginConfigService;

    public List<InterfaceParameterDto> getTaskNodeParameters(String procDefId, String nodeDefId) {
        List<InterfaceParameterDto> result = new ArrayList<>();
        Optional<TaskNodeDefInfoEntity> entityOptional = taskNodeDefInfoRepo.findById(nodeDefId);
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
            
            log.info("About to fetch data for node {} {}", f.getNodeDefId(), f.getNodeName());

            log.info("About to fetch data with expression {} and data id {}", f.getRoutineExpression(), dataId);
            DataModelExpressionToRootData expr = new DataModelExpressionToRootData(f.getRoutineExpression(), dataId);
            List<TreeNode> nodes = null;
            try {
                nodes = dataModelExpressionService.getPreviewTree(expr);
            } catch (Exception e) {
                log.error("errors while fetching data with expr {} and data id {}", f.getRoutineExpression(), dataId,e);
                throw new WecubeCoreException(e.getMessage());
            }

            if (nodes == null || nodes.isEmpty()) {
                log.warn("None data returned for {} and {}", f.getRoutineExpression(), dataId);
                continue;
            }
            
            log.info("total {} records returned for {} and {}", f.getRoutineExpression(), dataId);

            for (TreeNode tn : nodes) {
                String treeNodeId = buildId(tn);
                GraphNodeDto currNode = findGraphNodeDtoById(result, treeNodeId);
                if (currNode == null) {
                    currNode = new GraphNodeDto();
                    currNode.setDataId(tn.getRootId().toString());
                    currNode.setPackageName(tn.getPackageName());
                    currNode.setEntityName(tn.getEntityName());

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
