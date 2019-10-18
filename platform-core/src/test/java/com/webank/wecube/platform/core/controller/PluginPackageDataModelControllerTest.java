package com.webank.wecube.platform.core.controller;


import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static com.webank.wecube.platform.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;
import static com.webank.wecube.platform.core.domain.MenuItem.ROLE_PREFIX;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WithMockUser(username = "test", authorities = {ROLE_PREFIX + MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelControllerTest extends AbstractControllerTest {

    @Test
    public void getAllDataModels() throws Exception {
        mockDataModel();
        mvc.perform(get("/v1/api/models").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", contains("entity_1", "entity_2", "entity_3")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void getDataModelById() throws Exception {
        mockDataModel();
        mvc.perform(get("/v1/api/packages/1/models").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].packageName", contains("package_1", "package_1", "package_1")))
                .andExpect(jsonPath("$.data[*].packageVersion", contains("1.0", "1.0", "1.0")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    private void mockDataModel() {
        String sqlStr = "INSERT INTO plugin_packages (name, version)\n" +
                "VALUES ('package_1',\n" +
                "        '1.0');\n" +
                "\n" +
                "INSERT INTO plugin_package_entities(plugin_package_id, name, display_name, description)\n" +
                "VALUES (1, 'entity_1', 'entity_1', 'entity_1_description');\n" +
                "INSERT INTO plugin_package_entities(plugin_package_id, name, display_name, description)\n" +
                "VALUES (1, 'entity_2', 'entity_2', 'entity_2_description');\n" +
                "INSERT INTO plugin_package_entities(plugin_package_id, name, display_name, description)\n" +
                "VALUES (1, 'entity_3', 'entity_3', 'entity_3_description');\n" +
                "\n" +
                "\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (1, NULL, 'attribute_1', 'attribute_1_description', 'INT');\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (1, NULL, 'attribute_2', 'attribute_2_description', 'INT');\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (1, 1, 'attribute_3', 'attribute_3_description', 'INT');\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (1, 1, 'attribute_4', 'attribute_4_description', 'REF');\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (2, 2, 'attribute_5', 'attribute_5_description', 'REF');\n" +
                "INSERT INTO plugin_package_attributes(entity_id, reference_id, name, description, data_type)\n" +
                "VALUES (3, NULL, 'attribute_6', 'attribute_6_description', 'REF');";
        executeSql(sqlStr);

    }
}
