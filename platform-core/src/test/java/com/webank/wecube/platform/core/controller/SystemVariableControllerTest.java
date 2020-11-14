package com.webank.wecube.platform.core.controller;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_INPUT;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_OUTPUT;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;
import static com.webank.wecube.platform.core.utils.JsonUtils.toObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

@Ignore
public class SystemVariableControllerTest extends AbstractControllerTest {
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    private SystemVariableController systemVariableController;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(systemVariableController).build();
    }

    @Test
    public void getGlobalSystemVariables() throws Exception {
        mockSystemVariables();
        String reqJson = toJsonString(
                QueryRequestDto.defaultQueryObject().addEqualsFilter("scope", SystemVariable.SCOPE_GLOBAL));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propA", "propB")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueX", "valueY")));

        reqJson = toJsonString(QueryRequestDto.defaultQueryObject().addEqualsFilter("scope", SystemVariable.SCOPE_GLOBAL)
                .addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propA")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueX")));
    }

    @Test
    public void getSystemVariables() throws Exception {
        mockSystemVariables();
        String reqJson = toJsonString(QueryRequestDto.defaultQueryObject().addEqualsFilter("scope", "qcloud")
                .addEqualsFilter("source", "qcloud:v1"));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propC", "propC")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valueZ", "valuez")));

        reqJson = toJsonString(QueryRequestDto.defaultQueryObject().addEqualsFilter("scope", "qcloud")
                .addEqualsFilter("source", "qcloud:v1").addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[*].name", contains("propC")))
                .andExpect(jsonPath("$.data.contents[*].value", contains("valuez")))
                .andExpect(jsonPath("$.data.contents[*].scope", contains("qcloud")));
    }

    @Test
    public void getAllSystemVariables() throws Exception {
        mockSystemVariables();

        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents.[*].name", contains("propA", "propB", "propC", "propC")))
                .andExpect(jsonPath("$.data.contents.[*].value", contains("valueX", "valueY", "valueZ", "valuez")));

        String reqJson = toJsonString(
                QueryRequestDto.defaultQueryObject().addEqualsFilter("status", SystemVariable.ACTIVE));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents.[*].name", contains("propA", "propC")))
                .andExpect(jsonPath("$.data.contents.[*].value", contains("valueX", "valuez")));
    }

    @Test
    public void getSystemVariableWithoutGivenPagingInfo() throws Exception {
        mockSystemVariables();
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status", is("OK"))).andExpect(jsonPath("$.data.pageInfo.totalRows", is(4)))
                .andExpect(jsonPath("$.data.contents", hasSize(4)));
    }

    @Test
    public void getSystemVariableWithGivenPagingInfo() throws Exception {
        mockSystemVariables();
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON)
                .content("{\"filters\":[],\"pageable\":{\"pageSize\":1,\"startIndex\":0},\"paging\":true}"))
                .andExpect(jsonPath("$.status", is("OK"))).andExpect(jsonPath("$.data.pageInfo.totalRows", is(4)))
                .andExpect(jsonPath("$.data.contents", hasSize(1)));
    }

    @Test
    public void curd() throws Exception {
        // create
        SystemVariable variable = new SystemVariable();
        variable.setName("mockVariable");
        variable.setValue("mockVariableValue");
        variable.setScope("global");

        mvc.perform(post("/v1/system-variables/create").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(newArrayList(variable)))).andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].name", contains("mockVariable")))
                .andExpect(jsonPath("$.data.[*].value", contains("mockVariableValue"))).andDo(new ResultHandler() {
                    @SuppressWarnings("rawtypes")
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        CommonResponseDto jsonResponse = toObject(result.getResponse().getContentAsString(),
                                CommonResponseDto.class);
                        Map prop = (Map) ((List) jsonResponse.getData()).get(0);
                        variable.setId(String.valueOf(prop.get("id")));
                    }
                });

        assertThat(variable.getId()).isNotBlank();

        // create-verify
        String reqJson = toJsonString(QueryRequestDto.defaultQueryObject().addEqualsFilter("id", variable.getId()));
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].name", is("mockVariable")))
                .andExpect(jsonPath("$.data.contents[0].value", is("mockVariableValue")))
                .andExpect(jsonPath("$.data.contents[0].status", is("active")));

        // update
        variable.setValue("UpdatedVariableValue");

        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(newArrayList(variable)))).andExpect(jsonPath("$.status", is("OK")));

        // update-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].name", is("mockVariable")))
                .andExpect(jsonPath("$.data.contents[0].value", is("UpdatedVariableValue")));

        // disable
        variable.setStatus(SystemVariable.INACTIVE);
        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(newArrayList(variable)))).andExpect(jsonPath("$.status", is("OK")));

        // disable-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].status", is("inactive")));

        // enable
        variable.setStatus(SystemVariable.ACTIVE);
        mvc.perform(post("/v1/system-variables/update").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(newArrayList(variable)))).andExpect(jsonPath("$.status", is("OK")));

        // enable-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.contents[0].status", is("active")));

        // delete
        mvc.perform(post("/v1/system-variables/delete").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(newArrayList(variable)))).andExpect(jsonPath("$.status", is("OK")));

        // delete-verify
        mvc.perform(post("/v1/system-variables/retrieve").contentType(MediaType.APPLICATION_JSON).content(reqJson))
                .andExpect(jsonPath("$.status", is("OK"))).andExpect(jsonPath("$.data.pageInfo.totalRows", is(0)));
    }

    private void mockSystemVariables() {
        PluginPackage pluginPackage = mockPluginPackage("qcloud", "v1");
        pluginPackageRepository.save(pluginPackage);
        executeSql("INSERT INTO plugin_packages (id, name, version, status, ui_package_included) VALUES "
                + "  ('1', 'package1', '1.0', 'REGISTERED', 0); "
                + "insert into system_variables (id, package_name, name, value, scope, source, status) values\n"
                + " ('1', 'qcloud', 'propA', 'valueX', 'global', 'qcloud:v1', 'active')\n"
                + ",('2', 'qcloud', 'propB', 'valueY', 'global', 'qcloud:v1', 'inactive')\n"
                + ",('3', 'qcloud', 'propC', 'valueZ', 'qcloud', 'qcloud:v1', 'inactive')\n"
                + ",('4', 'qcloud', 'propC', 'valuez', 'qcloud', 'qcloud:v1', 'active')\n" + ";");
    }

    private PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, UNREGISTERED,
                new Timestamp(System.currentTimeMillis()), false, newLinkedHashSet(), newLinkedHashSet(), null,
                newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(),
                newLinkedHashSet(), newLinkedHashSet());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, "mockEntity",
                PluginConfig.Status.DISABLED, newLinkedHashSet());
        mockPlugin.setInterfaces(newLinkedHashSet(mockPluginConfigInterface(mockPlugin)));
        mockPluginPackage.addPluginConfig(mockPlugin);

        Long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, 1,
                mockPluginPackage.getName(), false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(),
                now, null);
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
