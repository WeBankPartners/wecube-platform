package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.service.plugin.PluginPackageService;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class PluginPackageControllerTest extends AbstractControllerTest {
    @Autowired
    private PluginPackageController pluginPackageController;
    @Mock
    private PluginPackageService pluginPackageService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void givenNullPluginPackageWhenUploadThenThrowException() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);

        pluginPackageController.uploadPluginPackage(null);
    }

    @Test
    public void givenEmptyPluginPackageWhenUploadThenThrowException() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);

        MockMultipartFile mockMultipartFile = new MockMultipartFile("Empty file", new byte[0]);

        pluginPackageController.uploadPluginPackage(mockMultipartFile);
    }

    @Test
    public void givenPluginPackageNormalWhenUploadThenReturnSuccess() {
        PluginPackageDto pluginPackageV2 = Mockito.mock(PluginPackageDto.class);
        String testPackageName = "service-manage-v0.1.zip";
        File testPackage = new File("src/test/resources/testpackage/service-manage-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = null;
        try {
            mockPluginPackageFile = new MockMultipartFile(testPackageName, FileUtils.readFileToByteArray(testPackage));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            when(pluginPackageService.uploadPackage(mockPluginPackageFile)).thenReturn(pluginPackageV2);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertThat(testPackage.exists()).isTrue();
        try {
            JsonResponse responseForMockedFile = pluginPackageController.uploadPluginPackage(mockPluginPackageFile);
            assertThat(responseForMockedFile.getStatus()).isEqualTo(JsonResponse.STATUS_OK);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}