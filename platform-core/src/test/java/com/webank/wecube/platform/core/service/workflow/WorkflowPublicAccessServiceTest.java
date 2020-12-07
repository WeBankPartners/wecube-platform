package com.webank.wecube.platform.core.service.workflow;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowPublicAccessServiceTest {

    @Autowired
    WorkflowPublicAccessService workflowPublicAccessService;
    
    @Before
    public void setUp(){
        List<String> roles = new ArrayList<String>();
        roles.add("SUPER_ADMIN");
        AuthenticatedUser u = new AuthenticatedUser("umadmin", "test token", roles);
        AuthenticationContextHolder.setAuthenticatedUser(u);
    }

    @Test
    public void testFetchLatestReleasedWorkflowDefs() {

        List<WorkflowDefInfoDto> procDefInfos = workflowPublicAccessService.fetchLatestReleasedWorkflowDefs();
        procDefInfos.forEach(System.out::println);
    }

    @Test
    public void testFetchWorkflowTasknodeInfos() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateNewWorkflowInstance() {
        fail("Not yet implemented");
    }

}
