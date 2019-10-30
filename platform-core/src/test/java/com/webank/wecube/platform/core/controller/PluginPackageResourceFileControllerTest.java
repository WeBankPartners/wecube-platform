package com.webank.wecube.platform.core.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PluginPackageResourceFileControllerTest extends AbstractControllerTest{

    @Autowired
    private PluginPackageResourceFileController pluginPackageResourceFileController;

    @Test
    public void givenNoUiPackageWhenQueryResourceFileListThenReturnEmptyList() {
        mockPluginPackagesOnly();
        try{
            mvc.perform(get("/v1/api/resource-files"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data", is(nullValue())))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageWithUiPackageContainingMultipleResourceFilesWhenQueryResourceFileListThenReturnFileList() {
        mockPluginPackageWithMultipleResourceFiles();
        try{
            mvc.perform(get("/v1/api/resource-files"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data[*].id", contains(5, 6)))
                    .andExpect(jsonPath("$.data[*].source", contains("ui.zip", "ui.zip")))
                    .andExpect(jsonPath("$.data[*].relatedPath",
                            contains("cmdb/dist/index.html"
                                    ,"cmdb/dist/js/app.78880a99.js")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void mockPluginPackagesOnly() {

        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  (1, 'cmdb', 'v1.0', 'UNREGISTERED')\n" +
                " ,(2, 'cmdb', 'v1.1', 'UNREGISTERED')\n" +
                " ,(3, 'cmdb', 'v1.2', 'UNREGISTERED')\n" +
                " ,(4, 'cmdb', 'v2.0', 'UNREGISTERED')\n" +
                " ,(5, 'cmdb', 'v2.1', 'REGISTERED');\n");
    }

    private void mockPluginPackageWithMultipleResourceFiles() {
        executeSql("insert into plugin_packages (id, name, version, status) values\n" +
                "  (1, 'cmdb', 'v1.0', 'UNREGISTERED')\n" +
                " ,(2, 'cmdb', 'v1.1', 'UNREGISTERED')\n" +
                " ,(3, 'cmdb', 'v1.2', 'UNREGISTERED')\n" +
                " ,(4, 'cmdb', 'v2.0', 'UNREGISTERED')\n" +
                " ,(5, 'cmdb', 'v2.1', 'REGISTERED');\n" +
                "\n" +
                "INSERT INTO plugin_package_resource_files(id, plugin_package_id, package_name, package_version, source, related_path) VALUES\n" +
                " (1, 1, 'cmdb', 'v1.0', 'ui.zip', 'cmdb/dist/css/chunk-vendors.76ee8001.css')\n" +
                ",(2, 2, 'cmdb', 'v1.1', 'ui.zip', 'cmdb/dist/favicon.ico')\n" +
                ",(3, 3, 'cmdb', 'v1.2', 'ui.zip', 'cmdb/dist/fonts/ionicons.143146fa.woff2')\n" +
                ",(4, 4, 'cmdb', 'v2.0', 'ui.zip', 'cmdb/dist/img/ionicons.a2c4a261.svg')\n" +
                ",(5, 5, 'cmdb', 'v2.1', 'ui.zip', 'cmdb/dist/index.html')\n" +
                ",(6, 5, 'cmdb', 'v2.1', 'ui.zip', 'cmdb/dist/js/app.78880a99.js')\n" +
                ";");
    }
}