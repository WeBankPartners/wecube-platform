package com.webank.wecube.platform.core.service.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.parser.PluginConfigXmlValidator;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.core.utils.StringUtilsEx;


public class PluginArtifactsMgmtServiceTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testParseRegisterXmlFile() throws IOException, WecubeCoreException, SAXException {
        InputStream input = PluginArtifactsMgmtServiceTest.class.getClassLoader().getResourceAsStream("plugin/register-cmdb-object.xml");
        
        String xmlFileDataStr = IOUtils.toString(input, Charset.forName("UTF-8"));
        
        InputStream input2 = PluginArtifactsMgmtServiceTest.class.getClassLoader().getResourceAsStream("plugin/register-cmdb-object.xml");
        new PluginConfigXmlValidator().validate(input2);
        PackageType xmlPackage = JaxbUtils.convertToObject(xmlFileDataStr, PackageType.class);
        
        Assert.assertNotNull(xmlPackage);
        System.out.println(xmlPackage.getName());
        
        String envString = xmlPackage.getResourceDependencies().getDocker().get(0).getEnvVariables();
        
        System.out.println(envString);
        
        List<String> envs = StringUtilsEx.findSystemVariableString(envString);
        
        
        System.out.println(envs.size());
        for(String s : envs){
            System.out.println(s);
        }
        
        IOUtils.closeQuietly(input);
    }

}
