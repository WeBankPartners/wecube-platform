package com.webank.wecube.core.controller;

import static com.webank.wecube.core.domain.JsonResponse.okayWithData;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionDeployRequest;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionDeployResponse;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionPreviewVO;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionVO;
import com.webank.wecube.core.domain.workflow.ServiceTaskBindInfoVO;
import com.webank.wecube.core.domain.workflow.ServiceTaskVO;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewVO;
import com.webank.wecube.core.service.workflow.ProcessDefinitionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
@RequestMapping("/process/definitions")
public class ProcessDefinitionController {
	private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionController.class);

	@Autowired
	private ProcessDefinitionService processService;

	@ApiOperation(value = "deploy a process definition")
	@PostMapping(value = "", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public JsonResponse deployProcessDefinition(@RequestBody ProcessDefinitionDeployRequest request) {
		log.debug("try to deploy a process definition, request={}", request);

		try {
			ProcessDefinitionDeployResponse resp = processService.deployProcessDefinition(request);
			return JsonResponse.okayWithData(resp);
		} catch (Exception e) {
			log.error("errors while deploying process definition", e);
			throw e;
		}

	}

	@ApiOperation(value = "list all process definitions")
	@GetMapping(value = "", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public JsonResponse listProcessDefinitions() {
		log.debug("try to list all process definitions");

		List<ProcessDefinitionVO> pds = processService.listProcessDefinitions();

		return JsonResponse.okayWithData(pds);
	}

	@ApiOperation(value = "get detail of one process definition with id")
	@GetMapping(value = "/definition/{process-definition-id}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public JsonResponse detailProcessDefinition(
			@PathVariable(name = "process-definition-id") String processDefinitionId) {
		log.debug("try to get detail of one process definition with id,processDefinitionId={}", processDefinitionId);

		try {
			ProcessDefinitionVO definitionVo = processService.findProcessDefinition(processDefinitionId);
			return JsonResponse.okayWithData(definitionVo);
		} catch (Exception e) {
			log.error("errors while detail process definition", e);
			throw e;
		}
	}

    @ApiOperation(value = "evaluate input parameters")
    @GetMapping(value = "/definition/{process-id}/input-parameters")
    public JsonResponse evaluateInputParameters(@PathVariable(name = "process-id") String processId) {
        return okayWithData(processService.evaluateRequiredInputParameters(processId));
    }
    
    @ApiOperation(value ="preview process definitions")
    @PostMapping(value = "/preview")
    public JsonResponse previewProcessDefinitions(@RequestBody List<ProcessDefinitionPreviewVO> definitionsPreviewVOs) {
    	if(definitionsPreviewVOs == null || definitionsPreviewVOs.isEmpty()) {
    		log.warn("definition infos to preview should provide");
    		return JsonResponse.error("unkown process infomations");
    	}
    	
    	return okayWithData(processService.outlineProcessDefinitions(definitionsPreviewVOs));
    }
    
    @ApiOperation(value ="preview process definition")
    @PostMapping(value = "/definition/preview")
    public JsonResponse previewSingleProcessDefinition(@RequestBody ProcessDefinitionPreviewVO definitionsPreviewVO) {
        if(definitionsPreviewVO == null) {
            log.warn("definition infos to preview should provide");
            return JsonResponse.error("unkown process infomations");
        }
        
        return okayWithData(processService.outlineSingleProcessDefinition(definitionsPreviewVO));
    }
    
    @ApiOperation(value ="preview task node of process definition")
    @PostMapping(value = "/definition/tasknodes/tasknode/preview")
    public JsonResponse previewTaskNodeDefinition(@RequestBody TaskNodeDefinitionPreviewVO request) {
        if(request == null) {
            return JsonResponse.error("unknown task node definition preview infomations");
        }
        
        return okayWithData(processService.previewTaskNodeDefinition(request));
    }
    
    @ApiOperation(value ="preview input parameters")
    @PostMapping(value = "/definition/input-parameters/preview")
    public JsonResponse previewInputParameters(@RequestBody List<ServiceTaskBindInfoVO> serviceTaskBindInfos) {
    	if(serviceTaskBindInfos == null || serviceTaskBindInfos.isEmpty()) {
    		log.warn("service bind infomations as input arguments should be provided");
    		return JsonResponse.error("service bind infomations as input arguments should be provided");
    	}
    	List<ServiceTaskVO> serviceTasks = new ArrayList<ServiceTaskVO>();
    	for(ServiceTaskBindInfoVO vo : serviceTaskBindInfos) {
    		ServiceTaskVO st = new ServiceTaskVO();
    		st.setCiLocateExpression(vo.getCiRoutineExp());
    		st.setId(vo.getNodeId());
    		st.setName(vo.getNodeName());
    		st.setServiceCode(vo.getServiceName());
    		
    		serviceTasks.add(st);
    	}
    	return okayWithData(processService.evaluateRequiredInputParameters(serviceTasks));
    }
}
