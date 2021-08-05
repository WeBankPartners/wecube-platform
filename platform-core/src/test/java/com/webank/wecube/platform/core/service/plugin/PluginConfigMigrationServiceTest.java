package com.webank.wecube.platform.core.service.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginConfigMigrationServiceTest {

    @Autowired
    PluginConfigMigrationService service;

    String filename = "plugin_2021-7-4_11_24_32.xml";

    @Test
    public void testImportPluginRegistersForOnePackage() throws IOException {

        String pluginPackageId = "999";
        File file = new File(PluginConfigMigrationServiceTest.class.getResource(filename).getFile());
        String registersAsXml = FileUtils.readFileToString(file, Charset.forName("UTF-8"));

        System.out.println(registersAsXml);
        
        service.importPluginRegistersForOnePackage(pluginPackageId, registersAsXml);
    }

}
