package com.webank.wecube.platform.core.parser;

import com.google.common.io.Resources;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageXmlParserTest {

    @Test
    public void givenNormalRegisterXmlWhenParseThenReturnPluginPackageDto() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        InputSource inputSource = new InputSource(Resources.getResource("register-for-parser-test.xml").openStream());
        PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

        assertThat(pluginPackageDto.getName()).isEqualTo("service-management");
        assertThat(pluginPackageDto.getVersion()).isEqualTo("v0.1");

        PluginPackage pluginPackage = pluginPackageDto.getPluginPackage();
        assertThat(pluginPackage.getPluginPackageDependencies()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageMenus()).hasSize(2);
        assertThat(pluginPackage.getSystemVariables()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageAuthorities()).hasSize(3);
        Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDocker = pluginPackage.getPluginPackageRuntimeResourcesDocker();
        assertThat(pluginPackageRuntimeResourcesDocker).hasSize(1);
        PluginPackageRuntimeResourcesDocker resourcesDocker = pluginPackageRuntimeResourcesDocker.iterator().next();
        assertThat(resourcesDocker.getImageName()).isEqualTo("service_management");
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesMysql()).hasSize(1);
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesS3()).hasSize(1);
        assertThat(pluginPackage.getPluginConfigs()).hasSize(2);

        assertThat(pluginPackageDto.getPluginPackageDataModelDto().isDynamic()).isTrue();
        assertThat(pluginPackageDto.getPluginPackageDataModelDto().getPluginPackageEntities()).hasSize(5);
    }

}