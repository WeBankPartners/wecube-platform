package com.webank.wecube.platform.core.parser;

import com.google.common.io.Resources;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageXmlParserTest {

    
    @Test
    public void givenNormalRegisterXmlWhenParseThenReturnPluginPackageDto() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        InputSource inputSource = new InputSource(Resources.getResource("plugin/register-for-parser-test.xml").openStream());
        PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(inputSource).parsePluginPackage();

        assertThat(pluginPackageDto.getName()).isEqualTo("service-management");
        assertThat(pluginPackageDto.getVersion()).isEqualTo("v0.1");

        PluginPackage pluginPackage = pluginPackageDto.getPluginPackage();
        assertThat(pluginPackage.getPluginPackageDependencies()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageMenus()).hasSize(3);
        assertThat(pluginPackage.getSystemVariables()).hasSize(2);
        assertThat(pluginPackage.getPluginPackageAuthorities()).hasSize(3);
        Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDocker = pluginPackage.getPluginPackageRuntimeResourcesDocker();
        assertThat(pluginPackageRuntimeResourcesDocker).hasSize(1);
        PluginPackageRuntimeResourcesDocker resourcesDocker = pluginPackageRuntimeResourcesDocker.iterator().next();
        assertThat(resourcesDocker.getImageName()).isEqualTo("service_management");
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesMysql()).hasSize(1);
        assertThat(pluginPackage.getPluginPackageRuntimeResourcesS3()).hasSize(1);
        assertThat(pluginPackage.getPluginConfigs()).hasSize(2);

        List<String> localDisplayNames = pluginPackage.getPluginPackageMenus().stream().map(menu -> menu.getLocalDisplayName()).sorted().collect(Collectors.toList());
        assertThat(localDisplayNames).hasSize(3);
        assertThat(localDisplayNames.get(0)).isEqualTo("Menu without Chinese Name");
        assertThat(localDisplayNames.get(1)).isEqualTo("任务管理");
        assertThat(localDisplayNames.get(2)).isEqualTo("服务类型管理");

        PluginPackageDataModelDto pluginPackageDataModelDto = pluginPackageDto.getPluginPackageDataModelDto();
        assertThat(pluginPackageDataModelDto.isDynamic()).isTrue();
        assertThat(pluginPackageDataModelDto.getUpdatePath()).isEqualTo("/data-model");
        assertThat(pluginPackageDataModelDto.getUpdateMethod()).isEqualTo("GET");
        assertThat(pluginPackageDataModelDto.getUpdateSource()).isEqualTo(PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name());
        assertThat(pluginPackageDataModelDto.getUpdateTime()).isGreaterThan(0);
        assertThat(pluginPackageDataModelDto.getPackageName()).isEqualTo(pluginPackageDto.getName());
        assertThat(pluginPackageDataModelDto.getVersion()).isEqualTo(1);
        assertThat(pluginPackageDataModelDto.getPluginPackageEntities()).hasSize(5);

        assertThat(pluginPackage.getSystemVariables().iterator().next().getStatus()).isEqualTo(SystemVariable.INACTIVE);
    }

}