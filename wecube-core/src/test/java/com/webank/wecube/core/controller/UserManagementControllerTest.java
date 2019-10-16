package com.webank.wecube.core.controller;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.MenuItem.MENU_ADMIN_PERMISSION_MANAGEMENT;
import static com.webank.wecube.core.domain.MenuItem.ROLE_PREFIX;
import static com.webank.wecube.core.utils.JsonUtils.toJsonString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WithMockUser(username="test", authorities = {ROLE_PREFIX + MENU_ADMIN_PERMISSION_MANAGEMENT})
public class UserManagementControllerTest extends AbstractControllerTest {

    @Test
    public void getAllMenuItems() throws Exception {
        mvc.perform(get("/admin/menus").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].code", hasItems("JOBS","DESIGNING","IMPLEMENTATION","MONITORING","ADJUSTMENT","INTELLIGENCE_OPS","COLLABORATION","ADMIN","JOBS_INITIATOR","JOBS_EXECUTOR","DESIGNING_PLANNING","DESIGNING_RESOURCE_PLANNING","DESIGNING_APPLICATION_ARCHITECTURE","DESIGNING_APPLICATION_DEPLOYMENT","DESIGNING_CI_DATA_MANAGEMENT","DESIGNING_CI_DATA_ENQUIRY","DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT","DESIGNING_CI_INTEGRATED_QUERY_EXECUTION","DESIGNING_ENUM_MANAGEMENT","DESIGNING_ENUM_ENQUIRY","IMPLEMENTATION_ARTIFACT_MANAGEMENT","IMPLEMENTATION_APPLICATION_DEPLOYMENT","IMPLEMENTATION_BATCH_JOB","IMPLEMENTATION_HIGH_RISK_INSTRUCTION_MANAGEMENT","IMPLEMENTATION_WORKFLOW_EXECUTION","MONITORING_BASIC_MONITOR_MANAGEMENT","MONITORING_APPLICATION_MONITOR_MANAGEMENT","MONITORING_CONTROL_PANEL_SETTING","MONITORING_DISCOVERY","MONITORING_CONSISTENCE_MANAGEMENT","ADJUSTMENT_TENDENCY","ADJUSTMENT_ROOT_CAUSE_INVESTIGATION","ADJUSTMENT_EXPANSION","ADJUSTMENT_RECOVERY","INTELLIGENCE_OPS_MODELING","INTELLIGENCE_OPS_DATA_SYNCHRONIZATION","COLLABORATION_PLUGIN_MANAGEMENT","COLLABORATION_WORKFLOW_ORCHESTRATION","COLLABORATION_SERVICE_CHANNEL","ADMIN_CMDB_MODEL_MANAGEMENT","ADMIN_PERMISSION_MANAGEMENT","ADMIN_BASE_DATA_MANAGEMENT")));
    }

    @Test
    public void getMenuPermissionsByRoleId() throws Exception {
        mvc.perform(get("/admin/roles/1/menu-permissions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data", hasItems("JOBS","DESIGNING","IMPLEMENTATION","MONITORING","ADJUSTMENT","INTELLIGENCE_OPS","COLLABORATION","ADMIN","JOBS_INITIATOR","JOBS_EXECUTOR","DESIGNING_PLANNING","DESIGNING_RESOURCE_PLANNING","DESIGNING_APPLICATION_ARCHITECTURE","DESIGNING_APPLICATION_DEPLOYMENT","DESIGNING_CI_DATA_MANAGEMENT","DESIGNING_CI_DATA_ENQUIRY","DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT","DESIGNING_CI_INTEGRATED_QUERY_EXECUTION","DESIGNING_ENUM_MANAGEMENT","DESIGNING_ENUM_ENQUIRY","IMPLEMENTATION_ARTIFACT_MANAGEMENT","IMPLEMENTATION_APPLICATION_DEPLOYMENT","IMPLEMENTATION_BATCH_JOB","IMPLEMENTATION_HIGH_RISK_INSTRUCTION_MANAGEMENT","IMPLEMENTATION_WORKFLOW_EXECUTION","MONITORING_BASIC_MONITOR_MANAGEMENT","MONITORING_APPLICATION_MONITOR_MANAGEMENT","MONITORING_CONTROL_PANEL_SETTING","MONITORING_DISCOVERY","MONITORING_CONSISTENCE_MANAGEMENT","ADJUSTMENT_TENDENCY","ADJUSTMENT_ROOT_CAUSE_INVESTIGATION","ADJUSTMENT_EXPANSION","ADJUSTMENT_RECOVERY","INTELLIGENCE_OPS_MODELING","INTELLIGENCE_OPS_DATA_SYNCHRONIZATION","COLLABORATION_PLUGIN_MANAGEMENT","COLLABORATION_WORKFLOW_ORCHESTRATION","COLLABORATION_SERVICE_CHANNEL","ADMIN_CMDB_MODEL_MANAGEMENT","ADMIN_PERMISSION_MANAGEMENT","ADMIN_BASE_DATA_MANAGEMENT")));
    }


    @Test
    public void assignAndRemoveMenuPermissionForRole() throws Exception {
        mvc.perform(get("/admin/roles/2/menu-permissions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.length()", is(0)));

        mvc.perform(post("/admin/roles/2/menu-permissions").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList("MOCK_MENU1", "MOCK_MENU2"))))
                .andExpect(jsonPath("$.status", is("OK")));

        mvc.perform(get("/admin/roles/2/menu-permissions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data", hasItems("MOCK_MENU1", "MOCK_MENU2")));

        mvc.perform(delete("/admin/roles/2/menu-permissions").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList("MOCK_MENU1", "MOCK_MENU2"))))
                .andExpect(jsonPath("$.status", is("OK")));

        mvc.perform(get("/admin/roles/2/menu-permissions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.length()", is(0)));
    }

    
    @Test
    public void removeMenuPermissionForRole() throws Exception {
        mvc.perform(delete("/admin/roles/1/menu-permissions").contentType(MediaType.APPLICATION_JSON)
        .content(toJsonString(newArrayList("ADMIN_PERMISSION_MANAGEMENT"))))
        .andExpect(jsonPath("$.status", is("ERROR")))
        .andExpect(jsonPath("$.message", is("Cannot be deleted as this is Admin permission menu.")));
    }

}
