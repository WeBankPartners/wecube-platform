package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.system_variable;
import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PluginConfigControllerTest extends AbstractControllerTest {

    public static final String NON_EXIST_ENTITY_ID = "999";
    public static final String EXISTING_ENTITY_ID = "1";
    public static final String NON_EXIST_PLUGIN_CONFIG_ID = "999";
    public static final String PLUGIN_CONFIG_ID_WITHOUT_ENTITY = "99";
    @Autowired
    private PluginConfigController pluginConfigController;
    @Autowired
    private PluginConfigRepository pluginConfigRepository;
    @Autowired
    private PluginPackageRepository packageRepository;

    @Test
    public void givenEntityIdNotExistWhenSaveThenReturnError() {
        mockMultipleVersionPluginConfig();

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById("31");
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(NON_EXIST_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(PluginConfigDto.fromDomain(pluginConfig))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is(String.format("PluginPackageEntity not found for id: [%s] for plugin config: Vpc Management", NON_EXIST_ENTITY_ID))))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenEntityIdIsNullWhenSaveThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(PLUGIN_CONFIG_ID_WITHOUT_ENTITY);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        assertThat(pluginConfig.getEntityId()).isNull();

        try {
            mvc.perform(post("/v1/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(PluginConfigDto.fromDomain(pluginConfig))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.id", is(PLUGIN_CONFIG_ID_WITHOUT_ENTITY)))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenNormalEntitySetWhenSaveThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        String existingPluginConfigId = "41";
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        assertThat(pluginConfig.getEntityId()).isNull();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(PluginConfigDto.fromDomain(pluginConfig))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.name", is("Vpc Management")))
                    .andExpect(jsonPath("$.data.entityId", is(EXISTING_ENTITY_ID)))
                    .andExpect(jsonPath("$.data.status", is("DISABLED")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Optional<PluginConfig> savedPluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        PluginConfig savedPluginConfig = savedPluginConfigOptional.get();
        assertThat(savedPluginConfig.getEntityId()).isEqualTo(EXISTING_ENTITY_ID);
    }

    @Test
    public void givenPluginConfigIsEnabledAlreadyWhenSaveThenReturnError() {
        mockMultipleVersionPluginConfig();

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById("11");
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(PluginConfigDto.fromDomain(pluginConfig))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Not allow to update plugin with status: ENABLED")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenPluginConfigIsEnabledAlreadyWheEnableThenReturnError() {
        mockMultipleVersionPluginConfig();

        String enabledPluginConfigId = "12";
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(enabledPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins/enable/" + enabledPluginConfigId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Not allow to enable pluginConfig with status: ENABLED")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageIsUnregisteredWhenEnablePluginConfigThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        String existingPluginConfigId = "32";
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins/enable/"+existingPluginConfigId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Plugin package is not in valid status [REGISTERED, RUNNING, STOPPED] to enable plugin.")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void givenNormalEntitySetWhenEnableThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        String existingPluginConfigId = "33";
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/plugins/enable/"+existingPluginConfigId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.name", is("Vpc Management")))
                    .andExpect(jsonPath("$.data.entityId", is(EXISTING_ENTITY_ID)))
                    .andExpect(jsonPath("$.data.status", is("ENABLED")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Optional<PluginConfig> savedPluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        PluginConfig savedPluginConfig = savedPluginConfigOptional.get();
        assertThat(savedPluginConfig.getEntityId()).isEqualTo(EXISTING_ENTITY_ID);
    }

    @Test
    public void givenNonExistPluginConfigIdWheDisableThenReturnError() {
        mockMultipleVersionPluginConfig();

        try {
            mvc.perform(post("/v1/plugins/disable/" + NON_EXIST_PLUGIN_CONFIG_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("PluginConfig not found for id: " + NON_EXIST_PLUGIN_CONFIG_ID)))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenPluginConfigIdNotExistWheDisableThenReturnError() {
        mockMultipleVersionPluginConfig();

        assertThat(pluginConfigRepository.existsById(NON_EXIST_PLUGIN_CONFIG_ID)).isFalse();

        try {
            mvc.perform(post("/v1/plugins/disable/" + NON_EXIST_PLUGIN_CONFIG_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("PluginConfig not found for id: " + NON_EXIST_PLUGIN_CONFIG_ID)))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void givenNormalPluginConfigWhenDisableThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        String existingPluginConfigId = "31";
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();
        assertThat(pluginConfig.getStatus()).isNotEqualTo(PluginConfig.Status.ENABLED);

        try {
            mvc.perform(post("/v1/plugins/disable/" + existingPluginConfigId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Optional<PluginConfig> savedPluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        PluginConfig savedPluginConfig = savedPluginConfigOptional.get();
        assertThat(savedPluginConfig.getStatus()).isEqualTo(PluginConfig.Status.DISABLED);

    }

    @Test
    public void givenMultiplePluginConfigWhenQueryAllENABLEDOnesThenShouldReturnOnlyTheENABLEDOnes() {
        mockMultipleVersionPluginConfig();

        try {
            mvc.perform(get("/v1/plugins/interfaces/enabled"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.length()", is(2)))
                    .andExpect(jsonPath("$.data[*].action", containsInAnyOrder("create", "update")))
                    .andExpect(jsonPath("$.data[*].serviceName", containsInAnyOrder("service-management/task/create", "service-management/service_request/update")))
                    .andExpect(jsonPath("$.data[*].path", containsInAnyOrder("/service-management/tasks", "/service-management/service-requests/{service-request-id}/done")))
                    .andExpect(jsonPath("$.data[*].httpMethod", containsInAnyOrder("POST", "PUT")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenMultiplePluginConfigWhenQueryAllENABLEDOnesForEntityIdThenShouldReturnCorrectResult() {
        mockMultipleVersionPluginConfig();

        try {
            mvc.perform(get("/v1/plugins/interfaces/entity/1/enabled"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.length()", is(1)))
                    .andExpect(jsonPath("$.data[*].action", contains("create")))
                    .andExpect(jsonPath("$.data[*].serviceName", contains("service-management/task/create")))
                    .andExpect(jsonPath("$.data[*].path", contains("/service-management/tasks")))
                    .andExpect(jsonPath("$.data[*].httpMethod", contains("POST")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenMandatoryAttributeIsMissingWhenRegisterThenThrowException() {
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setName("wecmdb");
        pluginPackage.setVersion("v0.1");
        pluginPackage.setStatus(PluginPackage.Status.REGISTERED);
        pluginPackage.setUiPackageIncluded(false);
        pluginPackage.setUploadTimestamp(new Timestamp(System.currentTimeMillis()));

        PluginConfig pluginConfig = new PluginConfig();
        pluginPackage.setPluginConfigs(newHashSet(pluginConfig));

        pluginConfig.setPluginPackage(pluginPackage);
        pluginConfig.setName("Confirmation");
        pluginConfig.setEntityName(null);
        pluginConfig.setEntityId(null);
        pluginConfig.setStatus(PluginConfig.Status.DISABLED);

        PluginConfigInterface configInterface = new PluginConfigInterface();

        configInterface.setPluginConfig(pluginConfig);
        pluginConfig.setInterfaces(newHashSet(configInterface));

        configInterface.setAction("Confirm");
        configInterface.setPath("/wecmdb/confirm");
        configInterface.setServiceName("Confirm:/confirm");
        configInterface.setHttpMethod("POST");
        configInterface.setServiceDisplayName("Confirm:/confirm");
        configInterface.setIsAsyncProcessing("N");

        PluginConfigInterfaceParameter inputParameter = new PluginConfigInterfaceParameter();
        inputParameter.setName("name");
        inputParameter.setType(PluginConfigInterfaceParameter.TYPE_INPUT);
        inputParameter.setDataType("string");
        inputParameter.setRequired("Y");
        inputParameter.setMappingType(system_variable.name());
        inputParameter.setMappingSystemVariableId(null);
        inputParameter.setMappingEntityExpression(null);

        PluginConfigInterfaceParameter outputStatus = new PluginConfigInterfaceParameter();
        outputStatus.setType(PluginConfigInterfaceParameter.TYPE_OUTPUT);
        outputStatus.setName("status");
        outputStatus.setDataType("string");

        PluginConfigInterfaceParameter outputMessage = new PluginConfigInterfaceParameter();
        outputMessage.setType(PluginConfigInterfaceParameter.TYPE_OUTPUT);
        outputMessage.setName("message");
        outputMessage.setDataType("string");

        configInterface.setInputParameters(newHashSet(inputParameter));
        configInterface.setOutputParameters(newHashSet(outputMessage, outputStatus));

        PluginPackage savedPluginPackage = packageRepository.save(pluginPackage);

        assertThat(savedPluginPackage.getName()).isEqualTo("wecmdb");
        assertThat(savedPluginPackage.getVersion()).isEqualTo("v0.1");
        assertThat(savedPluginPackage.getStatus()).isEqualTo(PluginPackage.Status.REGISTERED);

        Set<PluginConfig> pluginConfigs = savedPluginPackage.getPluginConfigs();
        assertThat(pluginConfigs).isNotNull();
        assertThat(pluginConfigs).hasSize(1);

        PluginConfig config = pluginConfigs.iterator().next();
        assertThat(config.getName()).isEqualTo("Confirmation");
        assertThat(config.getStatus()).isEqualTo(PluginConfig.Status.DISABLED);

        Set<PluginConfigInterface> interfaces = config.getInterfaces();
        assertThat(interfaces).isNotNull();
        assertThat(interfaces).hasSize(1);

        PluginConfigInterface pluginConfigInterface = interfaces.iterator().next();
        assertThat(pluginConfigInterface.getAction()).isEqualTo("Confirm");

        Set<PluginConfigInterfaceParameter> inputParameters = pluginConfigInterface.getInputParameters();
        assertThat(inputParameters).isNotNull();
        assertThat(inputParameters).hasSize(1);

        PluginConfigInterfaceParameter pluginConfigInterfaceParameter = inputParameters.iterator().next();
        assertThat(pluginConfigInterfaceParameter).isNotNull();
        assertThat(pluginConfigInterfaceParameter.getName()).isEqualTo("name");
        assertThat(pluginConfigInterfaceParameter.getRequired()).isEqualTo("Y");

        String id = config.getId();
        assertThat(id).isNotNull();

        try {
            mvc.perform(post("/v1/plugins/enable/" + id))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            assertThat(e instanceof WecubeCoreException).isTrue();
            assertThat(e.getMessage()).isNotNull();
            assertThat(e.getMessage()).isEqualTo("System variable is required for parameter [1]");
        }
    }

    private void mockMultipleVersionPluginConfig() {

        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  ('1', 'servicemanagement', 'v1.0', 'UNREGISTERED')\n" +
                " ,('2', 'servicemanagement', 'v1.1', 'UNREGISTERED')\n" +
                " ,('3', 'servicemanagement', 'v1.2', 'UNREGISTERED')\n" +
                " ,('4', 'servicemanagement', 'v2.0', 'UNREGISTERED')\n" +
                " ,('5', 'servicemanagement', 'v2.1', 'REGISTERED');\n" +
                "\n" +
                "insert into plugin_configs (id, plugin_package_id, name, entity_id, status) values\n" +
                " ('11', '1', 'task', 1, 'ENABLED')\n" +
                ",('12', '5', 'service_request', 2, 'ENABLED')\n" +
                ",('21', '2', 'Vpc Management', 17, 'DISABLED')\n" +
                ",('31', '3', 'Vpc Management', 16, 'DISABLED')\n" +
                ",('32', '4', 'Vpc Management', 16, 'DISABLED')\n" +
                ",('33', '5', 'Vpc Management', 16, 'DISABLED');\n" +
                "\n" +
                "insert into plugin_configs (id, plugin_package_id, name, status) values\n" +
                " ('41', '3', 'Vpc Management', 'DISABLED')\n" +
                ",('99', '3', 'Vpc Management', 'DISABLED');\n" +
                "\n" +
                "insert into plugin_config_interfaces (id, plugin_config_id, action, service_name, service_display_name, path, http_method) values " +
                " ('1', '11', 'create', 'service-management/task/create', 'service-management/task/create', '/service-management/tasks', 'POST')" +
                ",('2', '12', 'update', 'service-management/service_request/update', 'service-management/service_request/update', '/service-management/service-requests/{service-request-id}/done', 'PUT');" +
                "\n" +
                "insert into plugin_config_interface_parameters(id, plugin_config_interface_id, type, name, data_type, mapping_type, mapping_entity_expression, mapping_system_variable_id, required) values " +
                " ('1', '1', 'INPUT', 'operatorRoleId', 'string', 'entity', 'name_xxx', null, 'Y') " +
                ", ('2', '1', 'INPUT', 'reporter', 'string', 'context', null, null, 'Y') " +
                ", ('3', '1', 'OUTPUT', 'status', 'string', '', null, null, '') " +
                ", ('4', '1', 'OUTPUT', 'message', 'string', '', null, null, '') " +
                ", ('5', '2', 'INPUT', 'service-request-id', 'string', 'system_variable', null, 1, 'Y') " +
                ", ('6', '2', 'INPUT', 'result', 'string', 'context', null, null, 'Y') " +
                ", ('7', '2', 'OUTPUT', 'status', 'string', '', null, null, '') " +
                ", ('8', '2', 'OUTPUT', 'message', 'string', '', null, null, ''); " +
                "INSERT INTO plugin_package_data_model(id, version, package_name) VALUES " +
                "  ('1', 1, 'servicemanagement') " +
                ", ('2', 2, 'servicemanagement') " +
                ";\n" +
                "INSERT INTO plugin_package_entities(id, data_model_id, data_model_version, package_name, name, display_name, description) VALUES " +
                " ('1', '2', 2, 'servicemanagement', 'entity_1', 'entity_1', 'entity_1_description')\n" +
                ",('2', '2', 2, 'servicemanagement', 'entity_2', 'entity_2', 'entity_2_description')\n" +
                ",('3', '2', 2, 'servicemanagement', 'entity_3', 'entity_3', 'entity_3_description');\n" +
                "\n" +
                "INSERT INTO plugin_package_attributes(id, entity_id, reference_id, name, description, data_type) VALUES\n" +
                " ('1', '1', NULL, 'attribute_1', 'attribute_1_description', 'INT')\n" +
                " ,('2', '1', NULL, 'attribute_2', 'attribute_2_description', 'INT')\n" +
                " ,('3', '1', '1', 'attribute_3', 'attribute_3_description', 'INT')\n" +
                " ,('4', '1', '1', 'attribute_4', 'attribute_4_description', 'REF')\n" +
                " ,('5', '2', '2', 'attribute_5', 'attribute_5_description', 'REF')\n" +
                " ,('6', '3', NULL, 'attribute_6', 'attribute_6_description', 'REF')" +
                ";");
    }
}
