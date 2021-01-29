package com.webank.wecube.platform.core.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.webank.wecube.platform.core.controller.plugin.PluginPackageController;
import com.webank.wecube.platform.core.controller.plugin.PluginPackageResourceFileController;
import com.webank.wecube.platform.core.handler.GlobalExceptionHandler;

@Ignore
public class PluginPackageResourceFileControllerTest extends AbstractControllerTest{

    @Autowired
    private PluginPackageResourceFileController pluginPackageResourceFileController;
    @Autowired
    private PluginPackageController pluginPackageController;
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(pluginPackageController, pluginPackageResourceFileController, globalExceptionHandler).build();
    }

    @Test
    public void givenNoUiPackageWhenQueryResourceFileListThenReturnEmptyList() {
        mockPluginPackagesOnly();
        try{
            mvc.perform(get("/v1/resource-files"))
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
            mvc.perform(get("/v1/resource-files"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("OK")))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("5", "6")))
                    .andExpect(jsonPath("$.data[*].source", contains("ui.zip", "ui.zip")))
                    .andExpect(jsonPath("$.data[*].relatedPath",
                            contains("cmdb/v2.1/dist/index.html"
                                    ,"cmdb/v2.1/dist/js/app.78880a99.js")))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void mockPluginPackagesOnly() {

        executeSql("insert into plugin_packages (id, name, version, status, ui_package_included, upload_timestamp) values\n" +
                "  ('1', 'cmdb', 'v1.0', 'UNREGISTERED', 0, '2019-12-03 15:18:12')\n" +
                " ,('2', 'cmdb', 'v1.1', 'UNREGISTERED', 0, '2019-12-03 15:28:12')\n" +
                " ,('3', 'cmdb', 'v1.2', 'UNREGISTERED', 0, '2019-12-03 15:38:12')\n" +
                " ,('4', 'cmdb', 'v2.0', 'UNREGISTERED', 0, '2019-12-03 15:48:12')\n" +
                " ,('5', 'cmdb', 'v2.1', 'REGISTERED', 0, '2019-12-03 15:58:12');\n");
    }

    private void mockPluginPackageWithMultipleResourceFiles() {
        executeSql("insert into plugin_packages (id, name, version, status, ui_package_included, upload_timestamp) values\n" +
                "  ('1', 'cmdb', 'v1.0', 'UNREGISTERED', 1, '2019-12-03 15:18:12')\n" +
                " ,('2', 'cmdb', 'v1.1', 'UNREGISTERED', 1, '2019-12-03 15:28:12')\n" +
                " ,('3', 'cmdb', 'v1.2', 'UNREGISTERED', 1, '2019-12-03 15:38:12')\n" +
                " ,('4', 'cmdb', 'v2.0', 'UNREGISTERED', 1, '2019-12-03 15:48:12')\n" +
                " ,('5', 'cmdb', 'v2.1', 'REGISTERED', 1, '2019-12-03 15:58:12');\n" +
                "\n" +
                "INSERT INTO plugin_package_resource_files(id, plugin_package_id, package_name, package_version, source, related_path) VALUES\n" +
                " ('1', '1', 'cmdb', 'v1.0', 'ui.zip', 'cmdb/v1.0/dist/css/chunk-vendors.76ee8001.css')\n" +
                ",('2', '2', 'cmdb', 'v1.1', 'ui.zip', 'cmdb/v1.1/dist/favicon.ico')\n" +
                ",('3', '3', 'cmdb', 'v1.2', 'ui.zip', 'cmdb/v1.2/dist/fonts/ionicons.143146fa.woff2')\n" +
                ",('4', '4', 'cmdb', 'v2.0', 'ui.zip', 'cmdb/v2.0/dist/img/ionicons.a2c4a261.svg')\n" +
                ",('5', '5', 'cmdb', 'v2.1', 'ui.zip', 'cmdb/v2.1/dist/index.html')\n" +
                ",('6', '5', 'cmdb', 'v2.1', 'ui.zip', 'cmdb/v2.1/dist/js/app.78880a99.js')\n" +
                ";");
    }
}