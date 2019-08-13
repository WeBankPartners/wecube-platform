package com.webank.wecube.core.parser;

import com.google.common.io.Resources;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class PluginConfigXmlParserTest {

    @Test
    public void parsePluginPackage() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        InputSource inputSource = new InputSource(Resources.getResource("plugin/sample-plugin-config.xml").openStream());
        PluginConfigXmlParser pluginConfigXmlParser = PluginConfigXmlParser.newInstance(inputSource);
        PluginPackage pluginPackage = pluginConfigXmlParser.parsePluginPackage();

        assertThat(pluginPackage).isNotNull();
        assertThat(pluginPackage.getName()).isEqualTo("qcloud");
        assertThat(pluginPackage.getVersion()).isEqualTo("v1");
        assertThat(pluginPackage.getDockerImageFile()).isEqualTo("wecube-plugins.image.tar");
        assertThat(pluginPackage.getDockerImageRepository()).isEqualTo("wecube-plugins");
        assertThat(pluginPackage.getDockerImageTag()).isEqualTo("201904191234");
        assertThat(pluginPackage.getContainerPort()).isEqualTo("8080");
        assertThat(pluginPackage.getContainerConfigDirectory()).isEqualTo("/home/app/conf");
        assertThat(pluginPackage.getContainerLogDirectory()).isEqualTo("/home/app/log");

        assertThat(pluginPackage.getPluginConfigs())
                .extracting("name")
                .containsExactly("vpc","peering-connection", "security-group", "route-table", "subnet", "vm", "storage", "nat-gateway", "mysql-vm");
        assertThat(pluginPackage.getPluginConfigs().get(0).getInterfaces())
                .extracting("name", "serviceName", "path")
                .containsExactly(
                        Tuple.tuple("create", "qcloud/vpc/create", "/v1/qcloud/vpc/create"),
                        Tuple.tuple("terminate", "qcloud/vpc/terminate", "/v1/qcloud/vpc/terminate")
                );
        assertThat(pluginPackage.getPluginConfigs().get(0).getInterfaces().get(1).getInputParameters())
                .extracting("name", "type", "datatype")
                .containsExactly(
                        Tuple.tuple("provider_params", "INPUT", "string"),
                        Tuple.tuple("id", "INPUT", "string")
                );
    }
}
