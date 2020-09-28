package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowProcDefServiceTest {

    @Autowired
    WorkflowProcDefService service;

    @Before
    public void setUp() {
        String username = "";
        ***REMOVED***
        Collection<String> authorities = new HashSet<>();
        AuthenticatedUser loginUser = new AuthenticatedUser(username, token, authorities);

        AuthenticationContextHolder.setAuthenticatedUser(loginUser);
    }

    @Test
    public void testDraftProcessDefinition() {
        // Given
        ProcDefInfoDto inputProcDefDto = buildRequestProcDefInfoDto();

        // When
        ProcDefInfoDto result = service.draftProcessDefinition(inputProcDefDto);

        // Then
        System.out.println(result);
    }
    
    private ProcDefInfoDto buildRequestProcDefInfoDto() {
        ProcDefInfoDto inputProcDefDto = new ProcDefInfoDto();
        Map<String, List<String>> permissionToRole = new HashMap<>();
        permissionToRole.put("MGMT", Arrays.asList("2c9280827019695c017019ac974f001c"));
        permissionToRole.put("USE", Arrays.asList("2c9280827019695c017019ac974f001c"));

        inputProcDefDto.setPermissionToRole(permissionToRole);
        
        
        String procDefData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">  <bpmn2:process id=\"wecube1600756811797\" isExecutable=\"true\">    <bpmn2:startEvent id=\"StartEvent_0r57qmt\" name=\"Start\">      <bpmn2:outgoing>SequenceFlow_1go3uzz</bpmn2:outgoing>    </bpmn2:startEvent>    <bpmn2:subProcess id=\"SubProcess_0he1iqq\" name=\"cvm\">      <bpmn2:incoming>SequenceFlow_1go3uzz</bpmn2:incoming>      <bpmn2:outgoing>SequenceFlow_1hx17v5</bpmn2:outgoing>    </bpmn2:subProcess>    <bpmn2:sequenceFlow id=\"SequenceFlow_1go3uzz\" sourceRef=\"StartEvent_0r57qmt\" targetRef=\"SubProcess_0he1iqq\" />    <bpmn2:subProcess id=\"SubProcess_0g7iluw\" name=\"confirm\">      <bpmn2:incoming>SequenceFlow_1hx17v5</bpmn2:incoming>    </bpmn2:subProcess>    <bpmn2:sequenceFlow id=\"SequenceFlow_1hx17v5\" sourceRef=\"SubProcess_0he1iqq\" targetRef=\"SubProcess_0g7iluw\" />  </bpmn2:process>  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"wecube1600756811797\">      <bpmndi:BPMNShape id=\"StartEvent_0r57qmt_di\" bpmnElement=\"StartEvent_0r57qmt\">        <dc:Bounds x=\"213\" y=\"149\" width=\"36\" height=\"36\" />        <bpmndi:BPMNLabel>          <dc:Bounds x=\"219\" y=\"192\" width=\"24\" height=\"14\" />        </bpmndi:BPMNLabel>      </bpmndi:BPMNShape>      <bpmndi:BPMNShape id=\"SubProcess_0he1iqq_di\" bpmnElement=\"SubProcess_0he1iqq\">        <dc:Bounds x=\"299\" y=\"127\" width=\"100\" height=\"80\" />      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id=\"SequenceFlow_1go3uzz_di\" bpmnElement=\"SequenceFlow_1go3uzz\">        <di:waypoint x=\"249\" y=\"167\" />        <di:waypoint x=\"299\" y=\"167\" />      </bpmndi:BPMNEdge>      <bpmndi:BPMNShape id=\"SubProcess_0g7iluw_di\" bpmnElement=\"SubProcess_0g7iluw\">        <dc:Bounds x=\"449\" y=\"127\" width=\"100\" height=\"80\" />      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id=\"SequenceFlow_1hx17v5_di\" bpmnElement=\"SequenceFlow_1hx17v5\">        <di:waypoint x=\"399\" y=\"167\" />        <di:waypoint x=\"449\" y=\"167\" />      </bpmndi:BPMNEdge>    </bpmndi:BPMNPlane>  </bpmndi:BPMNDiagram></bpmn2:definitions>";
        inputProcDefDto.setProcDefData(procDefData);
        String procDefId =  "sbiszTww2Bs";
        inputProcDefDto.setProcDefId(procDefId);
        String procDefKey = "";
        inputProcDefDto.setProcDefKey(procDefKey);
        String procDefName = "default";
        inputProcDefDto.setProcDefName(procDefName);
        String rootEntity =  "wecmdb:host_resource_instance";
        
        inputProcDefDto.setRootEntity(rootEntity);
        
        TaskNodeDefInfoDto taskNodeInfoCvm = new TaskNodeDefInfoDto();
        taskNodeInfoCvm.setNodeId("SubProcess_0he1iqq");
        taskNodeInfoCvm.setNodeName("cvm");
        taskNodeInfoCvm.setNodeType(null);
        taskNodeInfoCvm.setNodeDefId("sbiszZSw2Bt");
        taskNodeInfoCvm.setProcDefKey("");
        taskNodeInfoCvm.setProcDefId("sbiszTww2Bs");
        taskNodeInfoCvm.setServiceId("qcloud/vm(resource)/create");
        taskNodeInfoCvm.setServiceName("qcloud/vm(resource)/create");
        taskNodeInfoCvm.setRoutineExpression("wecmdb:host_resource_instance");
        taskNodeInfoCvm.setRoutineRaw("wecmdb:host_resource_instance");
        taskNodeInfoCvm.setDescription("");
        taskNodeInfoCvm.setTimeoutExpression("30");
        taskNodeInfoCvm.setStatus("draft");
        taskNodeInfoCvm.setOrderedNo(null);
        taskNodeInfoCvm.setTaskCategory("SSTN");
        
        List<TaskNodeDefParamDto> paramInfosCvm = new ArrayList<>();
        taskNodeInfoCvm.setParamInfos(paramInfosCvm);
        inputProcDefDto.addTaskNodeInfo(taskNodeInfoCvm);
        
        
        TaskNodeDefInfoDto taskNodeInfoConfirm = new TaskNodeDefInfoDto();
        
        taskNodeInfoConfirm.setNodeId("SubProcess_0g7iluw");
        taskNodeInfoConfirm.setNodeName("confirm");
        taskNodeInfoConfirm.setNodeType(null);
        taskNodeInfoConfirm.setNodeDefId("sbiyNJDw2Bu");
        taskNodeInfoConfirm.setProcDefKey("");
        taskNodeInfoConfirm.setProcDefId("sbiszTww2Bs");
        taskNodeInfoConfirm.setServiceId("wecmdb/ci-data-confirm(confirm-dml)/confirm");
        taskNodeInfoConfirm.setServiceName("wecmdb/ci-data-confirm(confirm-dml)/confirm");
        taskNodeInfoConfirm.setRoutineExpression("wecmdb:host_resource_instance");
        taskNodeInfoConfirm.setRoutineRaw("wecmdb:host_resource_instance");
        taskNodeInfoConfirm.setDescription("");
        taskNodeInfoConfirm.setTimeoutExpression("30");
        taskNodeInfoConfirm.setStatus("draft");
        taskNodeInfoConfirm.setOrderedNo(null);
        taskNodeInfoConfirm.setTaskCategory("SUTN");
        
        List<TaskNodeDefParamDto> paramInfosConfirm = new ArrayList<>();
        taskNodeInfoConfirm.setParamInfos(paramInfosConfirm);
        inputProcDefDto.addTaskNodeInfo(taskNodeInfoConfirm);
        
        
        return inputProcDefDto;
    }

}
