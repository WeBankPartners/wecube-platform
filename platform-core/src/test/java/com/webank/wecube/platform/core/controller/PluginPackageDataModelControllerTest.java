package com.webank.wecube.platform.core.controller;


import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import com.webank.wecube.platform.core.support.FakeS3Client;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static com.webank.wecube.platform.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;
import static com.webank.wecube.platform.core.domain.MenuItem.ROLE_PREFIX;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WithMockUser(username = "test", authorities = {ROLE_PREFIX + MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelControllerTest extends AbstractControllerTest {

    @Autowired
    PluginPackageService pluginPackageService;

    @Test
    public void getAllDataModels() throws Exception {
        mockDataModel();
        mvc.perform(get("/v1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].packageName", contains("package_1", "package_2")))
                .andExpect(jsonPath("$.data[*].version", contains(1, 2)))
                .andExpect(jsonPath("$.data[0].pluginPackageEntities[*].name", containsInAnyOrder("entity_1", "entity_2", "entity_3")))
                .andExpect(jsonPath("$.data[1].pluginPackageEntities[*].name", containsInAnyOrder("entity_4", "entity_5", "entity_6")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void getDataModelByPackageName() throws Exception {
        mockDataModel();
        String packageName = "package_1";
        mvc.perform(get("/v1/packages/" + packageName + "/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(JsonResponse.STATUS_OK)))
                .andExpect(jsonPath("$.message", is(JsonResponse.SUCCESS)))
                .andExpect(jsonPath("$.data.packageName", is(packageName)))
                .andExpect(jsonPath("$.data.version", is(1)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].packageName", containsInAnyOrder(packageName, packageName, packageName)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].name", containsInAnyOrder("entity_1", "entity_2", "entity_3")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void getDataModelByMockedPackage() throws Exception {
        uploadCorrectPackage();
        final int MOCK_DATA_MODEL_NUMBER = 5;
        mvc.perform(get("/v1/packages/" + "service-management" + "/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.pluginPackageEntities", is(iterableWithSize(MOCK_DATA_MODEL_NUMBER))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    private void mockDataModel() {
        String sqlStr =
                "INSERT INTO plugin_packages (name, version) VALUES " +
                        "  ('package_1', '1.0') " +
                        ", ('package_2', '1.1') " +
                        ";\n" +
                "INSERT INTO plugin_package_data_model(id, version, package_name) VALUES " +
                        "  (1, 1, 'package_1') " +
                        ", (2, 1, 'package_2') " +
                        ", (3, 2, 'package_2') " +
                        ";\n" +
                "INSERT INTO plugin_package_entities(id, data_model_id, data_model_version, package_name, name, display_name, description) VALUES " +
                        "  (1, 1, 1, 'package_1', 'entity_1', 'entity_1', 'entity_1_description') " +
                        ", (2, 1, 1, 'package_1', 'entity_2', 'entity_2', 'entity_2_description') " +
                        ", (3, 1, 1, 'package_1', 'entity_3', 'entity_3', 'entity_3_description') " +

                        ", (4, 2, 1, 'package_2', 'entity_4', 'entity_4', 'entity_4_description') " +
                        ", (5, 2, 1, 'package_2', 'entity_5', 'entity_5', 'entity_5_description') " +
                        ", (6, 2, 1, 'package_2', 'entity_6', 'entity_6', 'entity_6_description') " +

                        ", (7, 3, 2, 'package_2', 'entity_4', 'entity_4', 'entity_4_description') " +
                        ", (8, 3, 2, 'package_2', 'entity_5', 'entity_5', 'entity_5_description') " +
                        ", (9, 3, 2, 'package_2', 'entity_6', 'entity_6', 'entity_6_description') " +
                        ";\n" +
                "INSERT INTO plugin_package_attributes(id, entity_id, reference_id, name, description, data_type) VALUES " +
                        "  (1, 1, NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", (2, 1, NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", (3, 1, 1, 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", (4, 1, 1, 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", (5, 2, 2, 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", (6, 3, NULL, 'attribute_6', 'attribute_6_description', 'REF')" +

                        ", (7, 4, NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", (8, 4, NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", (9, 4, 1, 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", (10, 4, 1, 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", (11, 5, 2, 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", (12, 6, NULL, 'attribute_6', 'attribute_6_description', 'REF')" +

                        ", (13, 7, NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", (14, 7, NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", (15, 7, 1, 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", (16, 7, 1, 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", (17, 8, 2, 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", (18, 9, 3, 'attribute_6', 'attribute_6_description', 'REF')" +
                        ";\n"
                ;
        executeSql(sqlStr);

    }

    private void uploadCorrectPackage() throws Exception {
        pluginPackageService.setS3Client(new FakeS3Client());
        File testPackage = new File("src/test/resources/testpackage/service-management-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = new MockMultipartFile("zip-file", FileUtils.readFileToByteArray(testPackage));
        mvc.perform(MockMvcRequestBuilders.multipart("/v1/packages").file(mockPluginPackageFile));
    }
}
