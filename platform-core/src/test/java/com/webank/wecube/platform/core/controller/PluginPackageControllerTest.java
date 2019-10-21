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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PluginPackageControllerTest extends AbstractControllerTest {
    @ClassRule
    public static TemporaryFolder folder= new TemporaryFolder();

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
    public void givenZeroPluginPackageWhenQueryAllThenReturnSuccessWithZeroPluginPackage() {
        try {
            mvc.perform(get("/v1/api/packages").contentType(MediaType.APPLICATION_JSON).content("{}"))
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
    public void givenEmptyPluginPackageWhenUploadThenThrowException() throws Exception {
        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/api/packages").contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is4xxClientError())
                    .andDo(print())
                    .andReturn().getResponse();
            assertThat(response.getErrorMessage()).isEqualTo("Required request part 'zip-file' is not present");
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }

        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/api/packages").contentType(MediaType.MULTIPART_FORM_DATA).content(new byte[0]))
                    .andExpect(status().is4xxClientError())
                    .andDo(print())
                    .andReturn().getResponse();
            assertThat(response.getErrorMessage()).isEqualTo("Required request part 'zip-file' is not present");
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }

        try {
            MockHttpServletResponse response = mvc.perform(post("/v1/api/packages").contentType(MediaType.MULTIPART_FORM_DATA).content(new MockMultipartFile("zip-file", new byte[0]).getBytes()))
                    .andExpect(status().is4xxClientError())
                    .andDo(print())
                    .andReturn().getResponse();
            assertThat(response.getErrorMessage()).isEqualTo("Required request part 'zip-file' is not present");
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
            mvc.perform(MockMvcRequestBuilders.multipart("/v1/api/packages").file(mockPluginPackageFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("status", is("OK")))
                    .andExpect(jsonPath("message", is("Success")))
                    .andExpect(jsonPath("$.data.name", is("service-management")))
                    .andExpect(jsonPath("$.data.version", is("v0.1")))
                    .andExpect(jsonPath("$.data.pluginPackageImageUrl", is("https://localhost:9000/s3/wecube-plugin-package-bucket/service-management/v0.1/image.tar")))
                    .andExpect(jsonPath("$.data.uiPackageUrl", is("https://localhost:9000/s3/wecube-plugin-package-bucket/service-management/v0.1/ui.zip")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to upload plugin package in PluginPackageController: " + e.getMessage());
        }

    }

}