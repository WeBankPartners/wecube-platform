package com.webank.wecube.core.service.workflow;

import com.google.common.collect.Lists;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.core.domain.workflow.ServiceTaskVO;
import com.webank.wecube.core.jpa.PluginConfigRepository;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.*;
import static java.util.Optional.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessDefinitionServiceTest {

    @InjectMocks
    ProcessDefinitionService processDefinitionService;

    @Mock
    PluginConfigRepository pluginConfigRepository;

    @Mock
    CmdbServiceV2Stub cmdbServiceV2Stub;

    @Test
    public void evaluateRequiredInputParameters() {
        List<ServiceTaskVO> serviceTasks = newArrayList(
                mockServiceTask("1"), mockServiceTask("2"), mockServiceTask("3"), mockServiceTask("4"), mockServiceTask("5")
        );

        when(pluginConfigRepository.findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters("1")).thenReturn(of(mockPluginConfigInterface(1, newLinkedHashSet(1,3,6,8), newLinkedHashSet(2,4), "10002, 91, 10003, 99")));
        when(pluginConfigRepository.findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters("2")).thenReturn(of(mockPluginConfigInterface(2, newLinkedHashSet(2,6), newLinkedHashSet(6,9), null)));
        when(pluginConfigRepository.findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters("3")).thenReturn(of(mockPluginConfigInterface(3, newLinkedHashSet(2,5,8), newLinkedHashSet(8,12), null)));
        when(pluginConfigRepository.findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters("4")).thenReturn(of(mockPluginConfigInterface(4, newLinkedHashSet(1,9,10), newLinkedHashSet(11), null)));
        when(pluginConfigRepository.findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters("5")).thenReturn(of(mockPluginConfigInterface(5, newLinkedHashSet(7,10,11), newLinkedHashSet(13), null)));

        CiTypeDto mockCiType = new CiTypeDto();
        mockCiType.setCiTypeId(10001);
        when(cmdbServiceV2Stub.getCiTypes(any(), anyBoolean())).thenReturn(Lists.newArrayList(mockCiType));

        Map<String, Object> requiredInputParameters = processDefinitionService.evaluateRequiredInputParameters(serviceTasks);

        verify(cmdbServiceV2Stub).getCiTypes(Lists.newArrayList(10001, 10002, 10003), true);

        assertThat(requiredInputParameters).isNotNull();
        assertThat(requiredInputParameters.get("ci-types")).isEqualTo(Lists.newArrayList(mockCiType));
        assertThat(requiredInputParameters.get("required-input-parameters")).isEqualTo(newLinkedHashSet(1,3,5,7,10, 91,99));
    }

    private ServiceTaskVO mockServiceTask(String serviceCode) {
        ServiceTaskVO serviceTask = new ServiceTaskVO();
        serviceTask.setServiceCode(serviceCode);
        return serviceTask;
    }

    private PluginConfigInterface mockPluginConfigInterface(int id, Set<Integer> inputParameters, Set<Integer> outputParameters, String cmdbCitypePath) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface();
        pluginConfigInterface.setId(id);
        pluginConfigInterface.setInputParameters(inputParameters.stream()
                .map(paramId -> new PluginConfigInterfaceParameter(paramId, pluginConfigInterface, TYPE_INPUT, "param"+paramId, "string", MAPPING_TYPE_CMDB_CI_TYPE, "cmdbColumnName", "cmdbColumnSource", 10001, paramId, cmdbCitypePath, null))
                .collect(Collectors.toSet())
        );
        pluginConfigInterface.setOutputParameters(outputParameters.stream()
                .map(paramId -> new PluginConfigInterfaceParameter(paramId, pluginConfigInterface, TYPE_OUTPUT, "param"+paramId, "string", MAPPING_TYPE_CMDB_CI_TYPE, "cmdbColumnName", "cmdbColumnSource", 10001, paramId, null, null))
                .collect(Collectors.toSet())
        );
        return pluginConfigInterface;
    }


}
