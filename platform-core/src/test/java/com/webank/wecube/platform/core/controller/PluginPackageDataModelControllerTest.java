package com.webank.wecube.platform.core.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.controller.plugin.PluginPackageController;
import com.webank.wecube.platform.core.controller.plugin.PluginPackageDataModelController;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.service.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import com.webank.wecube.platform.core.support.FakeS3Client;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.constant.DataModelDataType;

//@WithMockUser(username = "test", authorities = {ROLE_PREFIX + MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginPackageDataModelControllerTest extends AbstractControllerTest {

    public static final String PACKAGE_NAME_BY_UPLOAD = "service-management";
    @Autowired
    private PluginPackageService pluginPackageService;
    @Autowired
    private PluginPackageDataModelRepository dataModelRepository;
    @Autowired
    private PluginPackageDataModelService dataModelService;
    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private PluginPackageDataModelController dataModelController;
    @Autowired
    private PluginPackageController pluginPackageController;

    private MockRestServiceServer server;

    @Before
    public void setup() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
        mvc = MockMvcBuilders.standaloneSetup(pluginPackageController, dataModelController).build();
    }

    @Test
    public void getAllDataModels() throws Exception {
        mockDataModel();
        mvc.perform(get("/v1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].packageName", containsInAnyOrder("package1", "package2")))
                .andExpect(jsonPath("$.data[*].version", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$.data[*].pluginPackageEntities[*].name", containsInAnyOrder("entity_1", "entity_2", "entity_3", "entity_4", "entity_5", "entity_6")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void getDataModelByPackageName() throws Exception {
        mockDataModel();
        String packageName = "package1";
        mvc.perform(get("/v1/packages/" + packageName + "/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.packageName", is(packageName)))
                .andExpect(jsonPath("$.data.version", is(1)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].packageName", containsInAnyOrder(packageName, packageName, packageName)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].name", containsInAnyOrder("entity_1", "entity_2", "entity_3")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Ignore
    @Test
    public void getDataModelByMockedPackage() throws Exception {
        uploadCorrectPackage();
        final int MOCK_DATA_MODEL_NUMBER = 5;
        mvc.perform(get("/v1/packages/" + PACKAGE_NAME_BY_UPLOAD + "/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.pluginPackageEntities", is(iterableWithSize(MOCK_DATA_MODEL_NUMBER))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }

    @Ignore
    @Test
    public void getRefByInfoByMockedPackage() throws Exception {
        uploadCorrectPackage();
        final int REF_BY_COUNT = 1;
        mvc.perform(get("/v1/models/package/" + PACKAGE_NAME_BY_UPLOAD + "/entity/" + "service_catalogue" + "/refById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is(iterableWithSize(REF_BY_COUNT))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/v1/models/package/" + PACKAGE_NAME_BY_UPLOAD + "/entity/" + "service_pipeline" + "/refById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is(iterableWithSize(REF_BY_COUNT))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/v1/models/package/" + PACKAGE_NAME_BY_UPLOAD + "/entity/" + "service_request_template" + "/refById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is(iterableWithSize(REF_BY_COUNT))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/v1/models/package/" + PACKAGE_NAME_BY_UPLOAD + "/entity/" + "service_request" + "/refById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is(iterableWithSize(REF_BY_COUNT))))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    }


    private void mockDataModel() {
        String sqlStr =
                "INSERT INTO plugin_packages (id, name, version, status, ui_package_included) VALUES " +
                        "  ('1', 'package1', '1.0', 'UNREGISTERED', 0) " +
                        ", ('2', 'package2', '1.1', 'UNREGISTERED', 0) " +
                        ";\n" +
                        "INSERT INTO plugin_package_data_model(id, version, package_name, is_dynamic) VALUES " +
                        "  ('1', 1, 'package1', false) " +
                        ", ('2', 1, 'package2', false) " +
                        ", ('3', 2, 'package2', false) " +
                        ";\n" +
                        "INSERT INTO plugin_package_entities(id, data_model_id, data_model_version, package_name, name, display_name, description) VALUES " +
                        "  ('1', '1', 1, 'package1', 'entity_1', 'entity_1', 'entity_1_description') " +
                        ", ('2', '1', 1, 'package1', 'entity_2', 'entity_2', 'entity_2_description') " +
                        ", ('3', '1', 1, 'package1', 'entity_3', 'entity_3', 'entity_3_description') " +

                        ", ('4', '2', 1, 'package2', 'entity_4', 'entity_4', 'entity_4_description') " +
                        ", ('5', '2', 1, 'package2', 'entity_5', 'entity_5', 'entity_5_description') " +
                        ", ('6', '2', 1, 'package2', 'entity_6', 'entity_6', 'entity_6_description') " +

                        ", ('7', '3', 2, 'package2', 'entity_4', 'entity_4', 'entity_4_description') " +
                        ", ('8', '3', 2, 'package2', 'entity_5', 'entity_5', 'entity_5_description') " +
                        ", ('9', '3', 2, 'package2', 'entity_6', 'entity_6', 'entity_6_description') " +
                        ";\n" +
                        "INSERT INTO plugin_package_attributes(id, entity_id, reference_id, name, description, data_type) VALUES " +
                        "  ('1', '1', NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", ('2', '1', NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", ('3', '1', '1', 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", ('4', '1', '1', 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", ('5', '2', '2', 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", ('6', '3', NULL, 'attribute_6', 'attribute_6_description', 'REF')" +

                        ", ('7', '4', NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", ('8', '4', NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", ('9', '4', '1', 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", ('10', '4', '1', 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", ('11', '5', '2', 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", ('12', '6', NULL, 'attribute_6', 'attribute_6_description', 'REF')" +

                        ", ('13', '7', NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ", ('14', '7', NULL, 'attribute_2', 'attribute_2_description', 'INT') " +
                        ", ('15', '7', '1', 'attribute_3', 'attribute_3_description', 'INT') " +
                        ", ('16', '7', '1', 'attribute_4', 'attribute_4_description', 'REF') " +
                        ", ('17', '8', '2', 'attribute_5', 'attribute_5_description', 'REF') " +
                        ", ('18', '9', '3', 'attribute_6', 'attribute_6_description', 'REF')" +
                        ";\n";
        executeSql(sqlStr);

    }

    @Test
    public void givenDynamicDataModelConfirmedWhenRegisterThenNewDataModelShouldBeApplied() throws Exception {
        mockSimpleDataModel();

        String packageName = "package1";
        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(packageName);
        assertThat(latestDataModelByPackageName.isPresent()).isTrue();

        Optional<PluginPackage> latestVersionByName = pluginPackageRepository.findLatestVersionByName(packageName);
        assertThat(latestVersionByName.isPresent()).isTrue();
        assertThat(latestVersionByName.get().getStatus()).isEqualTo(PluginPackage.Status.REGISTERED);

        PluginPackageDataModelDto pluginPackageDataModelDto = dataModelService.packageView(packageName);

        // clean all the IDs so that no key violation.
        pluginPackageDataModelDto.setId(null);
        pluginPackageDataModelDto.getPluginPackageEntities().forEach(entity -> entity.setId(null));
        pluginPackageDataModelDto.getPluginPackageEntities().forEach(entity -> entity.getAttributes().forEach(attribute -> attribute.setId(null)));

        PluginPackageEntityDto entity = pluginPackageDataModelDto.getPluginPackageEntities().iterator().next();
        PluginPackageAttributeDto pluginPackageAttributeDto = new PluginPackageAttributeDto();
        pluginPackageAttributeDto.setPackageName(packageName);
        pluginPackageAttributeDto.setEntityName(entity.getName());
        pluginPackageAttributeDto.setName("dynamicAttribute");
        pluginPackageAttributeDto.setDataType(DataModelDataType.String.getCode());
        pluginPackageAttributeDto.setDescription("Dynamic attribute for test");

        entity.getAttributes().add(pluginPackageAttributeDto);

        mvc.perform(post("/v1/models").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJsonString(pluginPackageDataModelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.packageName", is(packageName)))
                .andExpect(jsonPath("$.data.id", is("DataModel__package1__2")))
                .andExpect(jsonPath("$.data.version", is(2)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].packageName", containsInAnyOrder(packageName)))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].name", containsInAnyOrder("entity_1")))
                .andExpect(jsonPath("$.data.pluginPackageEntities[*].attributes[*].name", containsInAnyOrder("attribute_1", "dynamicAttribute")))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();


    }

    private void mockSimpleDataModel() {
        String sqlStr =
                "INSERT INTO plugin_packages (id, name, version, status, ui_package_included) VALUES " +
                        "  ('1', 'package1', '1.0', 'REGISTERED', 0) " +
                        ";\n" +
                        "INSERT INTO plugin_package_data_model(id, version, package_name, is_dynamic, update_method, update_path) VALUES " +
                        "  ('1', 1, 'package1', 1, 'GET', '/data-model') " +
                        ";\n" +
                        "INSERT INTO plugin_package_entities(id, data_model_id, data_model_version, package_name, name, display_name, description) VALUES " +
                        "  ('1', '1', 1, 'package1', 'entity_1', 'entity_1', 'entity_1_description') " +
                        ";\n" +
                        "INSERT INTO plugin_package_attributes(id, entity_id, reference_id, name, description, data_type) VALUES " +
                        "  ('1', '1', NULL, 'attribute_1', 'attribute_1_description', 'INT') " +
                        ";\n";
        executeSql(sqlStr);

    }

    @Test
    public void givenDynamicDataModelWhenPullThenReturnNewDataModel() {
        mockSimpleDataModel();

        String packageName = "package1";
        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(packageName);
        assertThat(latestDataModelByPackageName.isPresent()).isTrue();

        Optional<PluginPackage> latestVersionByName = pluginPackageRepository.findLatestVersionByName(packageName);
        assertThat(latestVersionByName.isPresent()).isTrue();
        assertThat(latestVersionByName.get().getStatus()).isEqualTo(PluginPackage.Status.REGISTERED);

        PluginPackageDataModelDto pluginPackageDataModelDto = dataModelService.packageView(packageName);

        // clean all the IDs so that no key violation.
        pluginPackageDataModelDto.setId(null);
        pluginPackageDataModelDto.getPluginPackageEntities().forEach(entity -> entity.setId(null));
        pluginPackageDataModelDto.getPluginPackageEntities().forEach(entity -> entity.getAttributes().forEach(attribute -> attribute.setId(null)));

        PluginPackageEntityDto entity = pluginPackageDataModelDto.getPluginPackageEntities().iterator().next();
        PluginPackageAttributeDto pluginPackageAttributeDto = new PluginPackageAttributeDto();
        pluginPackageAttributeDto.setPackageName(packageName);
        pluginPackageAttributeDto.setEntityName(entity.getName());
        pluginPackageAttributeDto.setName("dynamicAttribute");
        pluginPackageAttributeDto.setDataType(DataModelDataType.String.getCode());
        pluginPackageAttributeDto.setDescription("Dynamic attribute for test");
        entity.getAttributes().add(pluginPackageAttributeDto);

        server.expect(ExpectedCount.manyTimes(), requestTo("http://localhost:9999/" + packageName + "/data-model"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JsonUtils.toJsonString(CommonResponseDto.okayWithData(pluginPackageDataModelDto.getPluginPackageEntities())), MediaType.APPLICATION_JSON));

        try {
            mvc.perform(get("/v1/models/package/" + packageName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data.packageName", is(packageName)))
                    .andExpect(jsonPath("$.data.id", is(nullValue())))
                    .andExpect(jsonPath("$.data.version", is(2)))
                    .andExpect(jsonPath("$.data.updateSource", is("DATA_MODEL_ENDPOINT")))
                    .andExpect(jsonPath("$.data.pluginPackageEntities[*].packageName", containsInAnyOrder(packageName)))
                    .andExpect(jsonPath("$.data.pluginPackageEntities[*].name", containsInAnyOrder("entity_1")))
                    .andExpect(jsonPath("$.data.pluginPackageEntities[*].attributes[*].name", containsInAnyOrder("attribute_1", "dynamicAttribute")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail("Failed when pulling new data model: " + e.getMessage());
        }
    }

    private void uploadCorrectPackage() throws Exception {
        pluginPackageService.setS3Client(new FakeS3Client());
        File testPackage = new File("src/test/resources/testpackage/service-management-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = new MockMultipartFile("zip-file", FileUtils.readFileToByteArray(testPackage));
        mvc.perform(MockMvcRequestBuilders.multipart("/v1/packages").file(mockPluginPackageFile));
    }
}
