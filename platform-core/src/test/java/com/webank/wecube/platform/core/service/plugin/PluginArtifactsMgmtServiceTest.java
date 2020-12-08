package com.webank.wecube.platform.core.service.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.utils.JaxbUtils;


@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginArtifactsMgmtServiceTest {

    @Test
    public void testParseRegisterXmlFile() throws IOException {
        InputStream input = PluginArtifactsMgmtServiceTest.class.getClassLoader().getResourceAsStream("plugin/register-cmdb.xml");
        String xmlFileDataStr = IOUtils.toString(input, Charset.forName("UTF-8"));
        PackageType xmlPackage = JaxbUtils.convertToObject(xmlFileDataStr, PackageType.class);
        
        Assert.assertNotNull(xmlPackage);
        System.out.println(xmlPackage.getName());
        
        IOUtils.closeQuietly(input);
    }

}
