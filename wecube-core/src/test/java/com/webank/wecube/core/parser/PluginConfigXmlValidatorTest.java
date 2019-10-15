package com.webank.wecube.core.parser;

import com.google.common.io.Resources;
import com.webank.wecube.core.commons.WecubeCoreException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginConfigXmlValidatorTest {
    @Test
    public void parsePluginPackageShouldSuccess() {
        InputStream inputStream = null;
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        validator.validate(inputSource);
    }

    @Test
    public void parsePluginPackageShouldFailed() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Invalid content was found starting with element 'plugins'. One of '{resourceDependencies}' is expected.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed2() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false2.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'packageDependency'");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed3() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false3.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'code' must appear on element 'menu'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed4() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false4.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'name' must appear on element 'entity'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed5() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false5.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'imageName' must appear on element 'docker'.");
        }
    }

    @Test
    public void parsePluginPackageShouldFailed6() {
        InputStream inputStream = null;
        // false 1
        try {
            inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false6.xml").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = null;
        try {
            validator = new PluginConfigXmlValidator();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert validator != null;
        try {
            validator.validate(inputSource);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Attribute 'type' must appear on element 'resourceDependency'.");
        }
    }
}
