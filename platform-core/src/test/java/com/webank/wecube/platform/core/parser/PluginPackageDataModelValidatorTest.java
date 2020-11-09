package com.webank.wecube.platform.core.parser;

import com.google.common.io.Resources;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PluginPackageDataModelValidatorTest {

    @Test
    public void givenPluginPackageWithoutDataModelWhenValidateThenShouldSucceed() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-without-data-model-entity.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
        } catch (Exception e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageParameterWithMappingTypeWhenValidateThenShouldSucceed() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-output-parameter-with-mappingtype.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
        } catch (Exception e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }

    @Test
    public void givenPluginPackageWithDataModelButAttributeWithoutDataTypeWhenValidateThenShouldThrowException() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-with-data-model-entity-attribute-without-datatype.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
            fail("Validator should throw exception but succeed");
        } catch (WecubeCoreException e) {
            assertThat(e.getMessage().contains("The DataType should not be empty or null")).isTrue();
        } catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException e) {
            fail("Xml should be correct without exception");
        }
    }

    @Test
    public void givenPluginPackageWithDataModelButAttributeWithoutDescriptionWhenValidateThenShouldSucceed() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-with-data-model-entity-attribute-without-description.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
        } catch (Exception e) {
            fail("Xml should be correct without exception");
        }
    }

    @Test
    public void givenPluginPackageWithDataModelButRefAttributeNameMissingWhenValidateThenShouldThrowException() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-with-data-model-entity-but-ref-attribute-name-missing.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
            fail("Validator should throw exception but succeed");
        } catch (WecubeCoreException e) {
            assertThat(e.getMessage().contains("Field [ref] should be specified when [dataType] is set to [\"ref\"]")).isTrue();
        } catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException e) {
            fail("Xml should be correct without exception");
        }
    }

    @Test
    public void givenPluginPackageWithCorrectDataModelWhenValidateThenShouldSucceed() {
        try {
            InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config-with-data-model-entity.xml").openStream());
            PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

            new PluginPackageDataModelValidator().validate(pluginPackageDto.getPluginPackageDataModelDto());
        } catch (Exception e) {
            fail("Validator should succeed here but got error message: " + e.getMessage());
        }
    }
}