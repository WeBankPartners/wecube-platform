package com.webank.wecube.core.parser;

import com.google.common.io.Resources;
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
    public void parsePluginPackage() throws IOException, SAXException {
        InputStream inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2.xml").getInputStream();
        StreamSource inputSource = new StreamSource(inputStream);

        PluginConfigXmlValidator validator = new PluginConfigXmlValidator();
        boolean returnResult = validator.validate(inputSource);
        assertThat(returnResult).isEqualTo(true);

        inputStream = new ClassPathResource("/plugin/sample-plugin-config-v2-false.xml").getInputStream();
        StreamSource inputSource2 = new StreamSource(inputStream);
        try {
            validator.validate(inputSource2);
        } catch (IOException | SAXException ignored) {
        }


    }
}
