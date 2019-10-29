package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;

import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PluginConfigControllerTest extends AbstractControllerTest {

    public static final int NON_EXIST_ENTITY_ID = 999;
    public static final int EXISTING_ENTITY_ID = 1;
    public static final int NON_EXIST_PLUGIN_CONFIG_ID = 999;
    @Autowired
    private PluginConfigController pluginConfigController;
    @Autowired
    private PluginConfigRepository pluginConfigRepository;

    @Test
    public void givenEntityIdNotExistWhenSaveThenReturnError() {
        mockMultipleVersionPluginConfig();

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(31);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(NON_EXIST_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(pluginConfig)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("PluginPackageEntity not found for id: " + NON_EXIST_ENTITY_ID)))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenNormalEntitySetWhenSaveThenReturnSuccess() {
        mockMultipleVersionPluginConfig();

        int existingPluginConfigId = 41;
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        assertThat(pluginConfig.getEntityId()).isNull();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(pluginConfig)))
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

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(11);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins").contentType(MediaType.APPLICATION_JSON).content(toJsonString(pluginConfig)))
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

        int enabledPluginConfigId = 12;
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(enabledPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins/enable/" + enabledPluginConfigId))
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

        int existingPluginConfigId = 32;
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins/enable/"+existingPluginConfigId))
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

        int existingPluginConfigId = 33;
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();

        pluginConfig.setEntityId(EXISTING_ENTITY_ID);

        try {
            mvc.perform(post("/v1/api/plugins/enable/"+existingPluginConfigId))
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
            mvc.perform(post("/v1/api/plugins/disable/" + NON_EXIST_PLUGIN_CONFIG_ID))
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
            mvc.perform(post("/v1/api/plugins/disable/" + NON_EXIST_PLUGIN_CONFIG_ID))
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

        int existingPluginConfigId = 31;
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(existingPluginConfigId);
        assertThat(pluginConfigOptional.isPresent()).isTrue();

        PluginConfig pluginConfig = pluginConfigOptional.get();
        assertThat(pluginConfig.getStatus()).isNotEqualTo(PluginConfig.Status.ENABLED);

        try {
            mvc.perform(post("/v1/api/plugins/disable/" + existingPluginConfigId))
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

    private void mockMultipleVersionPluginConfig() {

        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  (1, 'cmdb', 'v1.0', 'UNREGISTERED')\n" +
                " ,(2, 'cmdb', 'v1.1', 'UNREGISTERED')\n" +
                " ,(3, 'cmdb', 'v1.2', 'UNREGISTERED')\n" +
                " ,(4, 'cmdb', 'v2.0', 'UNREGISTERED')\n" +
                " ,(5, 'cmdb', 'v2.1', 'REGISTERED');\n" +
                "\n" +
                "insert into plugin_configs (id, plugin_package_id, name, entity_id, status) values\n" +
                " (11, 1, 'Vpc Management', 1, 'ENABLED')\n" +
                ",(12, 5, 'Vpc Management2', 1, 'ENABLED')\n" +
                ",(21, 2, 'Vpc Management', 17, 'DISABLED')\n" +
                ",(31, 3, 'Vpc Management', 16, 'DISABLED')\n" +
                ",(32, 4, 'Vpc Management', 16, 'DISABLED')\n" +
                ",(33, 5, 'Vpc Management', 16, 'DISABLED');\n" +
                "\n" +
                "insert into plugin_configs (id, plugin_package_id, name, status) values\n" +
                "(41, 3, 'Vpc Management', 'DISABLED');\n" +
                "\n" +
                "INSERT INTO plugin_package_entities(id, plugin_package_id, name, display_name, description) VALUES\n" +
                " (1, 1, 'entity_1', 'entity_1', 'entity_1_description')\n" +
                ",(2, 1, 'entity_2', 'entity_2', 'entity_2_description')\n" +
                ",(3, 1, 'entity_3', 'entity_3', 'entity_3_description');\n" +
                "\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type) VALUES\n" +
                " (1, NULL, 'attribute_1', 'attribute_1_description', 'INT')\n" +
                " ,(1, NULL, 'attribute_2', 'attribute_2_description', 'INT')\n" +
                " ,(1, 1, 'attribute_3', 'attribute_3_description', 'INT')\n" +
                " ,(1, 1, 'attribute_4', 'attribute_4_description', 'REF')\n" +
                " ,(2, 2, 'attribute_5', 'attribute_5_description', 'REF')\n" +
                " ,(3, NULL, 'attribute_6', 'attribute_6_description', 'REF')" +
                ";");
    }
}
