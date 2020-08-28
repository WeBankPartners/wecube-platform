package com.webank.wecube.platform.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.webank.wecube.platform.core.controller.plugin.PluginPackageController;
import com.webank.wecube.platform.core.handler.GlobalExceptionHandler;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import com.webank.wecube.platform.core.support.FakeS3Client;

public class PluginPackageControllerTest extends AbstractControllerTest {
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private PluginPackageService pluginPackageService;

    @Autowired
    private PluginPackageController pluginPackageController;

    @Autowired
    private ApplicationHomeController homeController;
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(pluginPackageController, homeController, globalExceptionHandler).build();
    }

    @BeforeClass
    public static void setupJunitTemporaryFolderSoThatTheContentsInTheFolderWillBeRemovedAfterTests() {
        try {
            System.setProperty("java.io.tmpdir", folder.newFolder().getCanonicalPath());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getMyMenusShouldReturnSuccess() {
        mockMenus();
        final int CURRENT_USER_USABLE_MENUS = 8; // because current user is not authorized to use any menu
        try {
            mvc.perform(get("/v1/my-menus").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(CURRENT_USER_USABLE_MENUS))))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void givenZeroPluginPackageWhenQueryAllThenReturnSuccessWithZeroPluginPackage() {
        try {
            mvc.perform(get("/v1/packages").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail("Failed to query all plugin packages in PluginPackageController: " + e.getMessage());
        }
    }


    @Test
    public void givenEmptyPluginPackageWhenUploadThenThrowException() {
        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/packages").contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Required request part 'zip-file' is not present")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }

        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/packages").contentType(MediaType.MULTIPART_FORM_DATA).content(new byte[0]))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Required request part 'zip-file' is not present")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }

        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/packages").contentType(MediaType.MULTIPART_FORM_DATA).content(new MockMultipartFile("zip-file", new byte[0]).getBytes()))
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Required request part 'zip-file' is not present")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }
    }

    @Ignore
    @Test
    public void givenPluginPackageNormalAndFakeS3ClientWhenUploadThenReturnSuccess() {
        pluginPackageService.setS3Client(new FakeS3Client());

        File testPackage = new File("src/test/resources/testpackage/service-management-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = null;
        try {
            mockPluginPackageFile = new MockMultipartFile("zip-file", FileUtils.readFileToByteArray(testPackage));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(testPackage.exists()).isTrue();
        try {
            mvc.perform(MockMvcRequestBuilders.multipart("/v1/packages").file(mockPluginPackageFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("status", is("OK")))
                    .andExpect(jsonPath("message", is("Success")))
                    .andExpect(jsonPath("$.data.name", is("service-management")))
                    .andExpect(jsonPath("$.data.version", is("v0.1")))
                    .andExpect(jsonPath("$.data.uiPackageIncluded", is(true)))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }
    }

    @Test
    public void givenFourPackagesWhenQueryThenReturnAllPackages() {
        mockMultipleVersionPluginPackage();

        try {
            mvc.perform(get("/v1/packages").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", contains("cmdb__v1.0", "cmdb__v1.1", "cmdb__v1.2", "cmdb__v1.3")))
                    .andExpect(jsonPath("$.data[*].name", contains("cmdb", "cmdb", "cmdb", "cmdb")))
                    .andExpect(jsonPath("$.data[*].version", contains("v1.0", "v1.1", "v1.2", "v1.3")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }
    }

    @Test
    public void givenFourPackagesWhenQueryWithDistinctNameShouldReturnNameList() {
        mockMultipleVersionPluginPackage();
        final int DISTINCT_PACKAGE_NAME_SIZE = 1;
        try {
            mvc.perform(get("/v1/packages?distinct=true").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(iterableWithSize(DISTINCT_PACKAGE_NAME_SIZE))))
                    .andExpect(jsonPath("$.data[*]", contains("cmdb")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }
    }

//    @Test
//    public void givenPluginPackageIsUNREGISTEREDWhenRegisterThenReturnSuccessful() {
//        mockMultipleVersionPluginPackage();
//
//        try {
//            String packageId = "cmdb__v1.0";
//            mvc.perform(post("/v1/packages/register/" + packageId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status", is("OK")))
//                    .andExpect(jsonPath("$.message", is("Success")))
//                    .andDo(print())
//                    .andReturn().getResponse();
//        } catch (Exception e) {
//            fail("Failed to decommission plugin package in PluginPackageController: " + e.getMessage());
//        }
//    }

//    @Test
//    public void givenPluginPackageIsRUNNINGWhenRegisterThenReturnError() {
//        mockMultipleVersionPluginPackage();
//
//        try {
//            String packageId = "cmdb__v1.0";
//            mvc.perform(post("/v1/packages/register/" + packageId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status", is("OK")))
//                    .andExpect(jsonPath("$.message", is("Success")))
//                    .andDo(print())
//                    .andReturn().getResponse();
//        } catch (Exception e) {
//            fail("Failed to decommission plugin package in PluginPackageController: " + e.getMessage());
//        }
//    }

    @Test
    public void givenNoConnectedReferenceWhenDecommissionThenReturnSuccessful() {
        mockMultipleVersionPluginPackage();

        try {
            mvc.perform(post("/v1/packages/decommission/cmdb__v1.0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to decommission plugin package in PluginPackageController: " + e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageIsRunningWhenDecommissionThenReturnFailed() {
        mockMultipleVersionPluginPackageWithReference();

        try {
            String pluginPackageIdInRUNNINGStatus = "cmdb__v1.3";
            mvc.perform(post("/v1/packages/decommission/" + pluginPackageIdInRUNNINGStatus))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private void mockMenus() {
        executeSql("delete from menu_items;\n" +
                "insert into menu_items (id,parent_code,code,source,menu_order,description) values\n" +
                "('JOBS',null,'JOBS','SYSTEM', 1, '')\n" +
                ",('DESIGNING',null,'DESIGNING','SYSTEM', 2, '')\n" +
                ",('IMPLEMENTATION',null,'IMPLEMENTATION','SYSTEM', 3, '')\n" +
                ",('MONITORING',null,'MONITORING','SYSTEM', 4, '')\n" +
                ",('ADJUSTMENT',null,'ADJUSTMENT','SYSTEM', 5, '')\n" +
                ",('INTELLIGENCE_OPS',null,'INTELLIGENCE_OPS','SYSTEM', 6, '')\n" +
                ",('COLLABORATION',null,'COLLABORATION','SYSTEM', 7, '')\n" +
                ",('ADMIN',null,'ADMIN','SYSTEM', 8, '')\n" +
                ",('IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION','IMPLEMENTATION','IMPLEMENTATION_WORKFLOW_EXECUTION','SYSTEM', 9, '')\n" +
                ",('COLLABORATION__COLLABORATION_PLUGIN_MANAGEMENT','COLLABORATION','COLLABORATION_PLUGIN_MANAGEMENT','SYSTEM', 10, '')\n" +
                ",('COLLABORATION__COLLABORATION_WORKFLOW_ORCHESTRATION','COLLABORATION','COLLABORATION_WORKFLOW_ORCHESTRATION','SYSTEM', 11, '')\n" +
                ",('ADMIN__ADMIN_BASE_DATA_MANAGEMENT','ADMIN','ADMIN_BASE_DATA_MANAGEMENT','SYSTEM', 12, '');");
    }

    private void mockMultipleVersionPluginPackage() {
        executeSql("insert into plugin_packages (id, name, version, status, ui_package_included) values\n" +
                "  ('cmdb__v1.0', 'cmdb', 'v1.0', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.1', 'cmdb', 'v1.1', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.2', 'cmdb', 'v1.2', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.3', 'cmdb', 'v1.3', 'RUNNING', 0)\n" +
                ";");
    }

    private void mockMultipleVersionPluginPackageWithReference() {
        executeSql("insert into plugin_packages (id, name, version, status, ui_package_included) values\n" +
                "  ('cmdb__v1.0', 'cmdb', 'v1.0', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.1', 'cmdb', 'v1.1', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.2', 'cmdb', 'v1.2', 'UNREGISTERED', 0)\n" +
                " ,('cmdb__v1.3', 'cmdb', 'v1.3', 'RUNNING', 0)\n" +
                ";\n" +
                "insert into plugin_configs (id, plugin_package_id, name, target_entity, status) values\n" +
                " ('11', 'cmdb__v1.0', 'Vpc Management', 'entity_1', 'ENABLED')\n" +
                ",('21', 'cmdb__v1.1', 'Vpc Management', 'entity_2', 'ENABLED')\n" +
                ",('31', 'cmdb__v1.2', 'Vpc Management', 'entity_3', 'DISABLED')\n" +
                ";" +
                "INSERT INTO plugin_instances (id, host, container_name, port, container_status, package_id, docker_instance_resource_id, instance_name, plugin_mysql_instance_resource_id, s3bucket_resource_id) VALUES\n" +
                " ('cmdb__v1.3__cmdb__127.0.0.1__20003', '127.0.0.1', 'cmdb', 20003, 'RUNNING', 'cmdb__v1.3', NULL, 'wecmdb', NULL, NULL);\n");
    }

    @Test
    public void getDependenciesByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String packageName = "service-management__v0.1";

        try {
            mvc.perform(get(String.format("/v1/packages/%s/dependencies", packageName)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.packageName", is("service-management")))
                    .andExpect(jsonPath("$.data.dependencies[*].packageName", contains("xxx", "xxx233")))
                    .andExpect(jsonPath("$.data.dependencies[*].version", contains("1.0", "1.5")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getDependenciesByWrongPackageIdShouldReturnError() {
        String wrongQueryId = "nonexistentpackage__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/dependencies", wrongQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
//                    .andExpect(jsonPath("$.message", is(String.format("Cannot find package by id: [%s]", wrongQueryId))))
                    .andExpect(jsonPath("$.data", is(nullValue())))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Ignore
    @Test
    public void getMenuByCorrectPackageIdShouldReturnSuccess() {
        mockMenus();
        final int MENU_NUM_WITH_BOTH_SYS_AND_CORE = 14;
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "service-management__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/menus", correctQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(MENU_NUM_WITH_BOTH_SYS_AND_CORE))))
                    .andExpect(jsonPath("$.data[*].code", containsInAnyOrder(
                            "JOBS", "JOBS_SERVICE_CATALOG_MANAGEMENT", "DESIGNING", "JOBS_TASK_MANAGEMENT",
                            "IMPLEMENTATION", "MONITORING", "ADJUSTMENT", "INTELLIGENCE_OPS", "COLLABORATION",
                            "ADMIN", "IMPLEMENTATION_WORKFLOW_EXECUTION", "COLLABORATION_PLUGIN_MANAGEMENT",
                            "COLLABORATION_WORKFLOW_ORCHESTRATION", "ADMIN_BASE_DATA_MANAGEMENT")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getMenuByWrongPackageIdShouldReturnError() {
        String wrongQueryId = "nonexistentpackage__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/dependencies", wrongQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
//                    .andExpect(jsonPath("$.message", is(String.format("Cannot find package by id: [%s]", wrongQueryId))))
                    .andExpect(jsonPath("$.data", is(nullValue())))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Ignore
    @Test
    public void getSystemParamsByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "service-management__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/system-parameters", correctQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", contains("service-management__v0.1__global__globalVar", "service-management__v0.1__service-management__pluginVar")))
                    .andExpect(jsonPath("$.data[*].name", contains("globalVar", "pluginVar")))
                    .andExpect(jsonPath("$.data[*].defaultValue", contains("xxxx", "xxxx")))
                    .andExpect(jsonPath("$.data[*].scope", contains("global", "service-management")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getAuthoritiesByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "service-management__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/authorities", correctQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("service-management__v0.1__admin__JOBS_SERVICE_CATALOG_MANAGEMENT", "service-management__v0.1__admin__JOBS_TASK_MANAGEMENT", "service-management__v0.1__wecube_operator__JOBS_TASK_MANAGEMENT")))
                    .andExpect(jsonPath("$.data[*].roleName", contains("admin", "admin", "wecube_operator")))
                    .andExpect(jsonPath("$.data[*].menuCode", contains("JOBS_SERVICE_CATALOG_MANAGEMENT", "JOBS_TASK_MANAGEMENT", "JOBS_TASK_MANAGEMENT")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getRuntimeResourcesByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "service-management__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/runtime-resources", correctQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.docker[0].id", is("Docker__service-management__v0.1__service_management")))
                    .andExpect(jsonPath("$.data.mysql[0].id", is("MySql__service-management__v0.1__service_management")))
                    .andExpect(jsonPath("$.data.mysql[0].schemaName", is("service_management")))
                    .andExpect(jsonPath("$.data.mysql[0].initFileName", is("init.sql")))
                    .andExpect(jsonPath("$.data.mysql[0].upgradeFileName", is("upgrade.sql")))
                    .andExpect(jsonPath("$.data.s3[0].id", is("S3__service-management__v0.1__service_management")))
                    .andExpect(jsonPath("$.data.s3[0].bucketName", is("service_management")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getPluginsByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String packageId = "service-management__v0.1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/plugins", packageId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].id", containsInAnyOrder("service-management__v0.1__service_request", "service-management__v0.1__task","service-management__v0.1__task__alarm")))
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].targetPackage", containsInAnyOrder("service-management", "service-management","wecube-monitor")))
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].targetEntity", containsInAnyOrder("service_request", "task", "alarm")))
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].name", containsInAnyOrder("task", "service_request", "task")))
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].status", containsInAnyOrder("DISABLED", "DISABLED", "DISABLED")))
                    .andExpect(jsonPath("$.data[*].pluginConfigDtoList[*].pluginPackageId", contains("service-management__v0.1", "service-management__v0.1", "service-management__v0.1")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    private void uploadCorrectPackage() throws Exception {
        pluginPackageService.setS3Client(new FakeS3Client());
        File testPackage = new File("src/test/resources/testpackage/service-management-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = new MockMultipartFile("zip-file", FileUtils.readFileToByteArray(testPackage));
        mvc.perform(MockMvcRequestBuilders.multipart("/v1/packages").file(mockPluginPackageFile));
    }

}