package com.webank.wecube.core.controller;


import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static com.webank.wecube.core.domain.MenuItem.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WithMockUser(username="test", authorities = {ROLE_PREFIX + MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginManagementControllerTest extends AbstractControllerTest {

    @Test
    public void getUniqueOnlinePluginInterfaces() throws Exception {
        mockMultipleVersionPlugin();

        mvc.perform(get("/plugin/latest-online-interfaces").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].id", contains(211)))
                .andExpect(jsonPath("$.data[*].serviceName", contains("qcloud-resource-management/Vpc Management/create")))
        ;

        mvc.perform(get("/plugin/latest-online-interfaces?ci-type-id=16").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].id", contains(111)))
                .andExpect(jsonPath("$.data[*].serviceName", contains("qcloud-resource-management/Vpc Management/create")))
        ;
    }

    private void mockMultipleVersionPlugin() {
        executeSql("insert into plugin_packages (id, name, version) values\n" +
                " (1, 'qcloud-resource-management', 'v1.0')\n" +
                ",(2, 'qcloud-resource-management', 'v1.1')\n" +
                ",(3, 'qcloud-resource-management', 'v1.2')\n" +
                ";\n" +
                "insert into plugin_configs (id, package_id, name, cmdb_ci_type_id, status) values\n" +
                " (11, 1, 'Vpc Management', 16, 'ONLINE')\n" +
                ",(21, 2, 'Vpc Management', 17, 'ONLINE')\n" +
                ",(31, 3, 'Vpc Management', 16, 'NOT_CONFIGURED')\n" +
                ";\n" +
                "insert into plugin_cfg_interfaces (id, config_id, name, service_name) values\n" +
                " (111, 11, 'create', 'qcloud-resource-management/Vpc Management/create')\n" +
                ",(211, 21, 'create', 'qcloud-resource-management/Vpc Management/create')\n" +
                ",(311, 31, 'create', 'qcloud-resource-management/Vpc Management/create')\n" +
                ";");
    }

}