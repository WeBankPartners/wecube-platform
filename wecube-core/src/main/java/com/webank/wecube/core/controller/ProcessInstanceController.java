package com.webank.wecube.core.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.domain.workflow.ProcessInstanceOutline;
import com.webank.wecube.core.domain.workflow.ProcessInstanceStartResponse;
import com.webank.wecube.core.domain.workflow.ProcessInstanceVO;
import com.webank.wecube.core.domain.workflow.ProcessTransactionVO;
import com.webank.wecube.core.domain.workflow.RestartProcessInstanceRequest;
import com.webank.wecube.core.domain.workflow.StartProcessInstaceWithCiDataReq;
import com.webank.wecube.core.domain.workflow.StartProcessInstaceWithCiDataReqChunk;
import com.webank.wecube.core.service.workflow.ProcessInstanceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
@RequestMapping("/process")
public class ProcessInstanceController {

	private static final Logger log = LoggerFactory.getLogger(ProcessInstanceController.class);

	@Autowired
	private ProcessInstanceService instanceService;
	
	@GetMapping("/process-transactions")
	@ResponseBody
	public JsonResponse listProcessTransactions() {
		
		try {
			List<ProcessTransactionVO> resps = instanceService.listProcessTransactions();
			return JsonResponse.okayWithData(resps);
		
		} catch (Exception e) {
			log.error("errors list process transactions", e);
			throw e;
		}
	}
	
	@GetMapping("/process-transactions/{process-transaction-id}/outlines")
	public JsonResponse refreshStatusesProcessTransactions(@PathVariable("process-transaction-id") int processTransactionId) {
	    
	    try {
	        List<ProcessInstanceOutline> outlines = instanceService.refreshStatusesProcessTransactions(processTransactionId);
	        return JsonResponse.okayWithData(outlines);
	    }catch(Exception e) {
	        log.error("errors while refresh statuses of process transactions", e);
	        throw e;
	    }
	}
	
	@GetMapping("/instances/{business-key}/outline")
    public JsonResponse refreshProcessInstanceStatus(@PathVariable("business-key") String businessKey) {
        
        try {
            ProcessInstanceOutline outlines = instanceService.refreshProcessInstanceStatus(businessKey);
            return JsonResponse.okayWithData(outlines);
        }catch(Exception e) {
            log.error("errors while refresh statuses of process transactions", e);
            throw e;
        }
    }
	
	
	@PostMapping("/inbatch/instances")
	@ResponseBody
	public JsonResponse startProcessInstancesWithCiDataInbatch(@RequestBody StartProcessInstaceWithCiDataReqChunk requestChunk) {
		log.debug("start a process instances with CI datas,size={}", requestChunk.getRequests().size());
		
		try {
			List<ProcessInstanceStartResponse> resps = instanceService.startProcessInstancesWithCiDataInbatch(requestChunk);
			return JsonResponse.okayWithData(resps);
		
		} catch (Exception e) {
			log.error("errors to start process instance", e);
			throw e;
		}
	}
	
	@PostMapping("/instances")
	@ResponseBody
	public JsonResponse startProcessInstanceWithCiData(@RequestBody StartProcessInstaceWithCiDataReq request) {
		log.debug("start a process instance with CI data");
		
		try {
			ProcessInstanceStartResponse resp = instanceService.startProcessInstanceWithCiData(request);
			return JsonResponse.okayWithData(resp);
		
		} catch (Exception e) {
			log.error("errors to start process instance", e);
			throw e;
		}
	}
	
	@ApiOperation(value = "restart process instance")
	@PostMapping("/instances/restart")
	public JsonResponse restartProcessInstance(@RequestBody RestartProcessInstanceRequest request) {
	    ProcessInstanceStartResponse resp = instanceService.restartProcessInstance(request);
	    return JsonResponse.okayWithData(resp);
	}

	@ApiOperation(value = "get all process instances")
	@GetMapping(value = "/instances/{process-definition-id}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public JsonResponse listProcessInstances(@PathVariable("process-definition-id") String processDefinitionId) {
		log.debug("list process instances,processDefinitionId={}", processDefinitionId);
		try {
			List<ProcessInstanceVO> instances = instanceService.getProcessInstancesOfDefinition(processDefinitionId);
			return JsonResponse.okayWithData(instances);
		} catch (Exception e) {
			log.error("errors while list process instances", e);
			throw e;
		}
	}

}
