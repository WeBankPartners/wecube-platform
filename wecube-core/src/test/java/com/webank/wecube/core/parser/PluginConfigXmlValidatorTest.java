package com.webank.wecube.core.parser;

import com.webank.wecube.core.commons.WecubeCoreException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginConfigXmlValidatorTest {
    private PluginConfigXmlValidator validator;

    @Before
    public void start() throws IOException, SAXException {
        validator = new PluginConfigXmlValidator();
    }

    @Test
    public void parsePluginPackageShouldSuccess() {
        validator.validate("/plugin/sample-plugin-config-v2.xml");
    }

    @Test
    public void parsePluginPackageShouldFailed() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Invalid content was found starting with element 'plugins'. One of '{resourceDependencies}' is expected.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed2() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false2.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'packageDependency'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed3() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false3.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'code' must appear on element 'menu'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed4() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false4.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'entity'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed5() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false5.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'imageName' must appear on element 'docker'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed6() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false6.xml");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Invalid content was found starting with element 'mysql'. One of '{docker}' is expected.");
        }
    }
}
