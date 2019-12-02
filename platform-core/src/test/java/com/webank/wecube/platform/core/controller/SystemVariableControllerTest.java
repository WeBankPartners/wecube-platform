package com.webank.wecube.platform.core.controller;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.*;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;
import static com.webank.wecube.platform.core.utils.JsonUtils.toObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

public class SystemVariableControllerTest extends AbstractControllerTest {
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Test
    public void getGlobalSystemVariables() throws Exception {
        mockSystemVariables();
        String reqJson = toJsonString(QueryRequest.defaultQueryObject().addEqualsFilter("scopeType", SystemVariable.SCOPE_TYPE_GLOBAL));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propA", "propB")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueX", "valueY")));

        reqJson = toJsonString(QueryRequest.defaultQueryObject()
                .addEqualsFilter("scopeType", SystemVariable.SCOPE_TYPE_GLOBAL)
                .addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propA")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueX")));
    }

    @Test
    public void getSystemVariables() throws Exception {
        mockSystemVariables();
        String reqJson = toJsonString(QueryRequest.defaultQueryObject()
                .addEqualsFilter("scopeType", SystemVariable.SCOPE_TYPE_PLUGIN_PACKAGE)
                .addEqualsFilter("scopeValue", "qcloud"));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propC", "propC")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueZ", "valuez")));

        reqJson = toJsonString(QueryRequest.defaultQueryObject()
                .addEqualsFilter("scopeType", SystemVariable.SCOPE_TYPE_PLUGIN_PACKAGE)
                .addEqualsFilter("scopeValue", "qcloud")
                .addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propC")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valuez")));
    }

    @Test
    public void getAllSystemVariables() throws Exception {
        mockSystemVariables();

        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents.[*].name", contains("propA", "propB", "propC", "propC")))
                .andExpect(jsonPath("$.data.contents.[*].value", contains("valueX", "valueY", "valueZ", "valuez")));

        String reqJson = toJsonString(QueryRequest.defaultQueryObject()
                .addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents.[*].name", contains("propA", "propC")))
                .andExpect(jsonPath("$.data.contents.[*].value", contains("valueX", "valuez")));
    }

    @Test
    public void curd() throws Exception {
        // create
        SystemVariable variable = new SystemVariable();
        variable.setName("mockVariable");
        variable.setValue("mockVariableValue");
        variable.setScopeType("global");

        mvc.perform(post("/v1/system-variables/create").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].name", contains("mockVariable")))
                .andExpect(jsonPath("$.data.[*].value", contains("mockVariableValue")))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        JsonResponse jsonResponse = toObject(result.getResponse().getContentAsString(), JsonResponse.class);
                        Map prop = (Map) ((List) jsonResponse.getData()).get(0);
                        variable.setId(String.valueOf(prop.get("id")));
                    }
                });

        assertThat(variable.getId()).isNotBlank();

        // create-verify
        String reqJson = toJsonString(QueryRequest.defaultQueryObject()
                .addEqualsFilter("id", variable.getId()));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].name", is("mockVariable")))
                .andExpect(jsonPath("$.data.contents[0].value", is("mockVariableValue")))
                .andExpect(jsonPath("$.data.contents[0].status", is("active")));

        // update
        variable.setValue("UpdatedVariableValue");

        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")));

        // update-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].name", is("mockVariable")))
                .andExpect(jsonPath("$.data.contents[0].value", is("UpdatedVariableValue")));

        // disable
        variable.setStatus(SystemVariable.INACTIVE);
        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")));

        // disable-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].status", is("inactive")));

        // enable
        variable.setStatus(SystemVariable.ACTIVE);
        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")));

        // enable-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].status", is("active")));

        // delete
        mvc.perform(post("/v1/system-variables/delete").contentType(MediaType.APPLICATION_JSON).content(toJsonString(newArrayList(variable))))
                .andExpect(jsonPath("$.status", is("OK")));

        // delete-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.pageInfo.totalRows", is(0)));
    }

    private void mockSystemVariables() {
        PluginPackage pluginPackage = mockPluginPackage("mockPluginPackage", "v1");
        pluginPackageRepository.save(pluginPackage);
        executeSql("INSERT INTO plugin_packages (id, name, version, status, ui_package_included) VALUES " +
                "  ('1', 'package1', '1.0', 'UNREGISTERED', 0); " +
                "insert into system_variables (id, plugin_package_id, name, value, scope_type, scope_value, seq_no, status) values\n" +
                " ('1', '1', 'propA', 'valueX', 'global', null,  1, 'active')\n" +
                ",('2', '1', 'propB', 'valueY', 'global', null,  2, 'inactive')\n" +
                ",('3', '1', 'propC', 'valueZ', 'plugin-package', 'qcloud', 3, 'inactive')\n" +
                ",('4', '1', 'propC', 'valuez', 'plugin-package', 'qcloud', 4, 'active')\n" +
                ";");
    }

    private PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, UNREGISTERED, new Timestamp(System.currentTimeMillis()), false,
                newLinkedHashSet(), newLinkedHashSet(), null, newLinkedHashSet(),
                newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, "mockEntity",
                PluginConfig.Status.DISABLED, newLinkedHashSet());
        mockPlugin.setInterfaces(newLinkedHashSet(mockPluginConfigInterface(mockPlugin)));
        mockPluginPackage.addPluginConfig(mockPlugin);

        Long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, 1, mockPluginPackage.getName(), false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);
        mockPluginPackage.setPluginPackageDataModel(mockPluginPackageDataModel);

        return mockPluginPackage;
    }

    private PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create",
                "Qcloud_vpc_create", "/v1/qcloud/vpc/create", "POST", newLinkedHashSet(), newLinkedHashSet());
        PluginConfigInterfaceParameter inputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "provider_params", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        PluginConfigInterfaceParameter inputParameter2 = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "name", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface.setInputParameters(Sets.newHashSet(inputParameter, inputParameter2));
        PluginConfigInterfaceParameter outputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_OUTPUT, "id", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface.setOutputParameters(Sets.newHashSet(outputParameter));
        return pluginConfigInterface;
    }
}
