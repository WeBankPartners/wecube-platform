package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertTrue;

public class PluginConfigXmlValidatorTest {
    private PluginConfigXmlValidator validator;

    @Before
    public void start() throws IOException, SAXException {
        validator = new PluginConfigXmlValidator();
    }

    @Test
    public void givenXmlWithOptionalNullBlocksWhenValidateThenShouldSucceed() {
        try {
            validator.validate("/plugin/sample-plugin-config-null-blocks.xml");
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
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
    public void parseCmdbPluginPackageShouldSuccess() {
        try {
            validator.validate("/plugin/sample-plugin-config-cmdb.xml");
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void parsePluginPackageWithoutDataModelShouldSuccess() {
        try {
            validator.validate("/plugin/sample-plugin-config-without-data-model-entity.xml");
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
            assertThat(ex.getMessage()).contains("'{resourceDependencies}'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed2() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false2.xml");
            fail("Test should fail as 'name' missing on 'packageDependency'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("'packageDependency'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed3() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false3.xml");
            fail("Test should fail as 'code' missing on 'menu'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("'menu'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed4() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false4.xml");
            fail("Test should fail as 'name' missing on 'entity'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("'entity'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed5() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false5.xml");
            fail("Test should fail as 'imageName' missing on 'docker'");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("'docker'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed6() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-false6.xml");
            fail("Test should fail as 'docker' missing");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("'{docker}'");
        }
    }

    @Test
    public void parsePluginPackageWithIsAsyncProcessingShouldSuccess() {
        try {
            validator.validate("/plugin/sample-plugin-config-v2-with-async-processing-interface.xml");
            assertTrue(true);
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void parseXmlWithConstantParamShouldSuccess() {
        try {
            validator.validate("/plugin/register-with-constant-params.xml");
            assertTrue(true);
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void parseXmlWithValidInterfaceTypeShouldSuccess() {
        try {
            validator.validate("/plugin/register-with-valid-interface-type.xml");
            assertTrue(true);
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void parseXmlWithInvalidInterfaceTypeShouldFailed() {
        try {
            validator.validate("/plugin/register-with-invalid-interface-type.xml");
            assertTrue(false);
        } catch (WecubeCoreException e) {
            assertThat(e.getMessage()).contains("XML validation failed");
        }
    }

    @Test
    public void parseXmlWithRegisterNameShouldSuccess() {
        try {
            validator.validate("/plugin/register-with-register-name.xml");
            assertTrue(true);
        } catch (WecubeCoreException e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

}
