package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import com.webank.wecube.platform.core.support.FakeS3Client;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PluginPackageControllerTest extends AbstractControllerTest {
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private PluginPackageService pluginPackageService;

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
        final int MENU_NUM_WITH_BOTH_SYS_AND_CORE = 12;
        try {
            mvc.perform(get("/v1/my-menus").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(MENU_NUM_WITH_BOTH_SYS_AND_CORE))))
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
                    .andExpect(jsonPath("$.data[*].id", contains(1, 2, 3, 4)))
                    .andExpect(jsonPath("$.data[*].name", contains("cmdb", "cmdb", "cmdb", "cmdb")))
                    .andExpect(jsonPath("$.data[*].version", contains("v1.0", "v1.1", "v1.2", "v1.3")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageIsUNREGISTEREDWhenRegisterThenReturnSuccessful() {
        mockMultipleVersionPluginPackage();

        try {
            mvc.perform(post("/v1/packages/register/1"))
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
    public void givenPluginPackageIsRUNNINGWhenRegisterThenReturnError() {
        mockMultipleVersionPluginPackage();

        try {
            mvc.perform(post("/v1/packages/register/1"))
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
    public void givenNoConnectedReferenceWhenDecommissionThenReturnSuccessful() {
        mockMultipleVersionPluginPackage();

        try {
            mvc.perform(post("/v1/packages/decommission/1"))
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
            int pluginPackageIdInRUNNINGStatus = 4;
            mvc.perform(post("/v1/packages/decommission/" + pluginPackageIdInRUNNINGStatus))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is("Failed to decommission plugin package with error message [Failed to decommission Plugin[cmdb/v1.3] due to package is RUNNING]")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private void mockMenus() {
        executeSql("delete from menu_items;\n" +
                "insert into menu_items (id,parent_id,code,description) values\n" +
                "(1,null,'JOBS','')\n" +
                ",(2,null,'DESIGNING','')\n" +
                ",(3,null,'IMPLEMENTATION','')\n" +
                ",(4,null,'MONITORING','')\n" +
                ",(5,null,'ADJUSTMENT','')\n" +
                ",(6,null,'INTELLIGENCE_OPS','')\n" +
                ",(7,null,'COLLABORATION','')\n" +
                ",(8,null,'ADMIN','')\n" +
                ",(305,3,'IMPLEMENTATION_WORKFLOW_EXECUTION','')\n" +
                ",(701,7,'COLLABORATION_PLUGIN_MANAGEMENT','')\n" +
                ",(702,7,'COLLABORATION_WORKFLOW_ORCHESTRATION','')\n" +
                ",(803,8,'ADMIN_BASE_DATA_MANAGEMENT','');");
    }

    private void mockMultipleVersionPluginPackage() {
        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  (1, 'cmdb', 'v1.0', 'UNREGISTERED')\n" +
                " ,(2, 'cmdb', 'v1.1', 'UNREGISTERED')\n" +
                " ,(3, 'cmdb', 'v1.2', 'UNREGISTERED')\n" +
                " ,(4, 'cmdb', 'v1.3', 'RUNNING')\n" +
                ";");
    }

    private void mockMultipleVersionPluginPackageWithReference() {
        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  (1, 'cmdb', 'v1.0', 'UNREGISTERED')\n" +
                " ,(2, 'cmdb', 'v1.1', 'UNREGISTERED')\n" +
                " ,(3, 'cmdb', 'v1.2', 'UNREGISTERED')\n" +
                " ,(4, 'cmdb', 'v1.3', 'RUNNING')\n" +
                ";\n" +
                "insert into plugin_configs (id, plugin_package_id, name, entity_id, status) values\n" +
                " (11, 1, 'Vpc Management', 16, 'ENABLED')\n" +
                ",(21, 2, 'Vpc Management', 17, 'ENABLED')\n" +
                ",(31, 3, 'Vpc Management', 16, 'DISABLED')\n" +
                ";");
    }

    @Test
    public void getDependenciesByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }

        try {
            mvc.perform(get("/v1/packages/1/dependencies").contentType(MediaType.APPLICATION_JSON).content("{}"))
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
        String wrongQueryId = "2";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/dependencies", wrongQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is(String.format("Cannot find package by id: [%s]", wrongQueryId))))
                    .andExpect(jsonPath("$.data", is(nullValue())))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getMenuByCorrectPackageIdShouldReturnSuccess() {
        mockMenus();
        final int MENU_NUM_WITH_BOTH_SYS_AND_CORE = 14;
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/menus", correctQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(MENU_NUM_WITH_BOTH_SYS_AND_CORE))))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getMenuByWrongPackageIdShouldReturnError() {
        String wrongQueryId = "2";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/dependencies", wrongQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ERROR")))
                    .andExpect(jsonPath("$.message", is(String.format("Cannot find package by id: [%s]", wrongQueryId))))
                    .andExpect(jsonPath("$.data", is(nullValue())))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getSystemParamsByCorrectPackageIdShouldReturnSuccess() {
        try {
            uploadCorrectPackage();
        } catch (Exception ex) {
            fail();
        }
        String correctQueryId = "1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/system-parameters", correctQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", contains(1, 2)))
                    .andExpect(jsonPath("$.data[*].name", contains("xxx", "xxx")))
                    .andExpect(jsonPath("$.data[*].defaultValue", contains("xxxx", "xxxx")))
                    .andExpect(jsonPath("$.data[*].scopeType", contains("global", "plugin-package")))
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
        String correctQueryId = "1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/authorities", correctQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", contains(1, 2, 3)))
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
        String correctQueryId = "1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/runtime-resources", correctQueryId)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.docker[0].id", is(1)))
                    .andExpect(jsonPath("$.data.mysql[0].id", is(1)))
                    .andExpect(jsonPath("$.data.mysql[0].schemaName", is("service_management")))
                    .andExpect(jsonPath("$.data.mysql[0].initFileName", is("init.sql")))
                    .andExpect(jsonPath("$.data.mysql[0].upgradeFileName", is("upgrade.sql")))
                    .andExpect(jsonPath("$.data.s3[0].id", is(1)))
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
        String correctQueryId = "1";
        try {
            mvc.perform(get(String.format("/v1/packages/%s/plugins", correctQueryId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[*].id", contains(1, 2)))
                    .andExpect(jsonPath("$.data[0].entityId", is(notNullValue())))
                    .andExpect(jsonPath("$.data[1].entityId", is(notNullValue())))
                    .andExpect(jsonPath("$.data[*].name", contains("task", "service_request")))
                    .andExpect(jsonPath("$.data[*].status", contains("DISABLED", "DISABLED")))
                    .andExpect(jsonPath("$.data[*].pluginPackageId", contains(1, 1)))
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