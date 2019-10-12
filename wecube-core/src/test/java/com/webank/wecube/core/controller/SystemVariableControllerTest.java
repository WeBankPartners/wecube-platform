package com.webank.wecube.core.controller;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.MenuItem.MENU_ADMIN_BASE_DATA_MANAGEMENT;
import static com.webank.wecube.core.domain.MenuItem.ROLE_PREFIX;
import static com.webank.wecube.core.utils.JsonUtils.toJsonString;
import static com.webank.wecube.core.utils.JsonUtils.toObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.domain.SystemVariable;

@WithMockUser(username = "test", authorities = { ROLE_PREFIX + MENU_ADMIN_BASE_DATA_MANAGEMENT })
public class SystemVariableControllerTest extends AbstractControllerTest {

    @Test
    public void getGlobalSystemVariables() throws Exception {
        mockSystemVariables();

        mvc.perform(get("/system-variables/global").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propA", "propB")))
                .andExpect(jsonPath("$.data[*].value", contains("valueX", "valueY")));

        mvc.perform(get("/system-variables/global?status=active").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propA")))
                .andExpect(jsonPath("$.data[*].value", contains("valueX")));
    }

    @Test
    public void getSystemVariables() throws Exception {
        mockSystemVariables();

        mvc.perform(get("/system-variables?scope-type=plugin-package&scope-value=qcloud").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propC", "propC")))
                .andExpect(jsonPath("$.data[*].value", contains("valueZ", "valuez")));

        mvc.perform(get("/system-variables?scope-type=plugin-package&scope-value=qcloud&status=active").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propC")))
                .andExpect(jsonPath("$.data[*].value", contains("valuez")));
    }
    
    @Test
    public void getAllSystemVariables() throws Exception {
        mockSystemVariables();

        mvc.perform(get("/system-variables/all").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propA", "propB", "propC", "propC")))
                .andExpect(jsonPath("$.data[*].value", contains("valueX", "valueY", "valueZ", "valuez")));

        mvc.perform(get("/system-variables/all?status=active").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("propA", "propC")))
                .andExpect(jsonPath("$.data[*].value", contains("valueX", "valuez")));
    }

    @Test
    public void curd() throws Exception {
        // create
        SystemVariable variable = new SystemVariable();
        variable.setName("mockVariable");
        variable.setValue("mockVariableValue");
        variable.setScopeType("global");

        mvc.perform(post("/system-variables/save").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[*].name", contains("mockVariable")))
                .andExpect(jsonPath("$.data[*].value", contains("mockVariableValue")))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        JsonResponse jsonResponse = toObject(result.getResponse().getContentAsString(), JsonResponse.class);
                        Map prop = (Map) ((List) jsonResponse.getData()).get(0);
                        variable.setId((int) prop.get("id"));
                    }
                });

        assertThat(variable.getId()).isGreaterThan(0);

        // create-verify
        mvc.perform(get("/system-variables/" + variable.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.name", is("mockVariable")))
                .andExpect(jsonPath("$.data.value", is("mockVariableValue")))
                .andExpect(jsonPath("$.data.status", is("active")));

        // update
        variable.setValue("UpdatedVariableValue");

        mvc.perform(post("/system-variables/save").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")));

        // update-verify
        mvc.perform(get("/system-variables/" + variable.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.name", is("mockVariable")))
                .andExpect(jsonPath("$.data.value", is("UpdatedVariableValue")));

        // disable
        mvc.perform(post("/system-variables/disable").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable.getId()))))
                .andExpect(jsonPath("$.status", is("OK")));
        
        // disable-verify
        mvc.perform(get("/system-variables/" + variable.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.status", is("inactive")));
        
        // enable
        mvc.perform(post("/system-variables/enable").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable.getId()))))
                .andExpect(jsonPath("$.status", is("OK")));
        
        // enable-verify
        mvc.perform(get("/system-variables/" + variable.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.status", is("active")));
        
        // delete
        mvc.perform(post("/system-variables/delete").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable.getId()))))
                .andExpect(jsonPath("$.status", is("OK")));

        // delete-verify
        mvc.perform(get("/system-variables/" + variable.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("System Variable not found for id: " + variable.getId())));
    }

    private void mockSystemVariables() {
        executeSql("insert into system_variables (id, name, value, scope_type, scope_value, seq_no, status) values\n" +
                " (1, 'propA', 'valueX', 'global', null,  1, 'active')\n" +
                ",(2, 'propB', 'valueY', 'global', null,  2, 'inactive')\n" +
                ",(3, 'propC', 'valueZ', 'plugin-package', 'qcloud', 3, 'inactive')\n" +
                ",(4, 'propC', 'valuez', 'plugin-package', 'qcloud', 4, 'active')\n" +
                ";");
    }
}
