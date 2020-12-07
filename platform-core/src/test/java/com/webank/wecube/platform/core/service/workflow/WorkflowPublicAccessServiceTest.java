package com.webank.wecube.platform.core.service.workflow;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowPublicAccessServiceTest {

    @Autowired
    WorkflowPublicAccessService workflowPublicAccessService;

    ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
            true).setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    @Before
    public void setUp() {
        List<String> roles = new ArrayList<String>();
        roles.add("SUPER_ADMIN");
        AuthenticatedUser u = new AuthenticatedUser("umadmin", "test token", roles);
        AuthenticationContextHolder.setAuthenticatedUser(u);
    }

    @Test
    public void testFetchLatestReleasedWorkflowDefs() throws IOException {

        List<WorkflowDefInfoDto> procDefInfos = workflowPublicAccessService.fetchLatestReleasedWorkflowDefs();
        procDefInfos.forEach(System.out::println);

        String data = objectMapper.writeValueAsString(procDefInfos);

        String fileName = "procDefInfos" + System.currentTimeMillis() + ".json";
        FileUtils.writeStringToFile(new File(fileName), data, Charset.forName("UTF-8"));
    }

    @Test
    public void testFetchWorkflowTasknodeInfos() throws IOException {

        String procDefId = "rWMKoelC2BR";
        List<WorkflowNodeDefInfoDto> nodeDefInfos = workflowPublicAccessService.fetchWorkflowTasknodeInfos(procDefId);
        nodeDefInfos.forEach(System.out::println);
        String data = objectMapper.writeValueAsString(nodeDefInfos);

        String fileName = "tasknodeInfos" + System.currentTimeMillis() + ".json";
        FileUtils.writeStringToFile(new File(fileName), data, Charset.forName("UTF-8"));
    }

    @Test
    public void testCreateNewWorkflowInstance() {
        fail("Not yet implemented");
    }

}
