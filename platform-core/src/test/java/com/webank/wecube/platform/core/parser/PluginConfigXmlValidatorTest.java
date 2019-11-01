package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PluginConfigXmlValidatorTest {
    private PluginConfigXmlValidator validator;

    @Before
    public void start() throws IOException, SAXException {
        validator = new PluginConfigXmlValidator();
    }

    @Test
    public void parsePluginPackageShouldSuccess() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2.xml");
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void parsePluginPackageShouldFailed() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false.xml");
            fail("Test should fail as missing resourceDependencies error");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Invalid content was found starting with element 'plugins'. One of '{resourceDependencies}' is expected.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed2() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false2.xml");
            fail("Test should fail as 'name' missing on 'packageDependency'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'packageDependency'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed3() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false3.xml");
            fail("Test should fail as 'code' missing on 'menu'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'code' must appear on element 'menu'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed4() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false4.xml");
            fail("Test should fail as 'name' missing on 'entity'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'entity'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed5() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false5.xml");
            fail("Test should fail as 'imageName' missing on 'docker'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'imageName' must appear on element 'docker'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed6() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false6.xml");
            fail("Test should fail as 'docker' missing");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Invalid content was found starting with element 'mysql'. One of '{docker}' is expected.");
        }
    }
}
