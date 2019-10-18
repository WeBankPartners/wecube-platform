package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;


import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class PluginPackageControllerTest extends AbstractControllerTest {
    @ClassRule
    public static TemporaryFolder folder= new TemporaryFolder();

    @Autowired
    private PluginPackageController pluginPackageController;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @BeforeClass
    public static void setupStatic() {
        try {
            System.setProperty("java.io.tmpdir", folder.newFolder().getCanonicalPath());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

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
        String testPackageName = "service-manage-v0.1.zip";
        File testPackage = new File("src/test/resources/testpackage/service-manage-v0.1.zip");
        MockMultipartFile mockPluginPackageFile = null;
        try {
            mockPluginPackageFile = new MockMultipartFile(testPackageName, FileUtils.readFileToByteArray(testPackage));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(testPackage.exists()).isTrue();
        try {
            JsonResponse responseForMockedFile = pluginPackageController.uploadPluginPackage(mockPluginPackageFile);
            assertThat(responseForMockedFile.getStatus()).isEqualTo(JsonResponse.STATUS_OK);
            assertThat(responseForMockedFile.getMessage()).isEqualTo(JsonResponse.SUCCESS);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Iterable<PluginPackage> pluginPackages = pluginPackageRepository.findAll();
        PluginPackage pluginPackage = pluginPackages.iterator().next();
        assertThat(pluginPackage.getName()).isEqualTo("service-management");
        assertThat(pluginPackage.getVersion()).isEqualTo("v0.1");

        assertThat(pluginPackage.getPluginPackageDependencies()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageMenus()).hasSize(2);
        assertThat(pluginPackage.getSystemVariables()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageAuthorities()).hasSize(3);
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesDocker()).hasSize(1);
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesMysql()).hasSize(1);
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesS3()).hasSize(1);
        assertThat(pluginPackage.getPluginConfigs()).hasSize(2);

        Set<PluginPackageEntity> pluginPackageEntities = newLinkedHashSet(pluginPackageEntityRepository.findAll());
        assertThat(pluginPackageEntities).hasSize(5);
    }

}