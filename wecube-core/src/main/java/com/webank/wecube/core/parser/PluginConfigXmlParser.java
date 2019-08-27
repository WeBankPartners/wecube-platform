package com.webank.wecube.core.parser;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.NOT_CONFIGURED;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.MAPPING_TYPE_NOT_AVAILABLE;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_INPUT;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_OUTPUT;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.commons.XPathEvaluator;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.core.domain.plugin.PluginPackage;

public class PluginConfigXmlParser {
    private final static String SEPARATOR_OF_NAMES = "/";

    public static PluginConfigXmlParser newInstance(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        return new PluginConfigXmlParser(new InputSource(inputStream));
    }

    public static PluginConfigXmlParser newInstance(InputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        return new PluginConfigXmlParser(inputSource);
    }

    private XPathEvaluator xPathEvaluator;

    private PluginConfigXmlParser(InputSource inputSource) throws IOException, SAXException, ParserConfigurationException {
        xPathEvaluator = XPathEvaluator.newInstance(inputSource);
    }

    public PluginPackage parsePluginPackage() throws XPathExpressionException {
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setName(validateName(trim(xPathEvaluator.getString("/package/@name")), "Package name"));
        pluginPackage.setVersion(trim(xPathEvaluator.getString("/package/@version")));
        pluginPackage.setDockerImageFile(trim(xPathEvaluator.getString("/package/docker-image-file")));
        pluginPackage.setDockerImageRepository(trim(xPathEvaluator.getString("/package/docker-image-repository")));
        pluginPackage.setDockerImageTag(trim(xPathEvaluator.getString("/package/docker-image-tag")));
        pluginPackage.setContainerPort(trim(xPathEvaluator.getString("/package/container-port")));
        pluginPackage.setContainerConfigDirectory(trim(xPathEvaluator.getString("/package/container-config-directory")));
        pluginPackage.setContainerLogDirectory(trim(xPathEvaluator.getString("/package/container-log-directory")));
        pluginPackage.setContainerStartParam(trim(xPathEvaluator.getString("/package/container-start-param")));

        NodeList pluginNodeList = xPathEvaluator.getNodeList("/package/plugin");
        if (pluginNodeList != null && pluginNodeList.getLength() > 0) {
            pluginPackage.setPluginConfigs(parsePluginConfigs(pluginNodeList, pluginPackage));
        }
        return pluginPackage;
    }

    private List<PluginConfig> parsePluginConfigs(NodeList pluginNodeList, PluginPackage pluginPackage) throws XPathExpressionException {
        List<PluginConfig> pluginConfigs = new ArrayList<>();

        for (int i = 0; i < pluginNodeList.getLength(); i++) {
            Node pluginConfigNode = pluginNodeList.item(i);

            PluginConfig pluginConfig = new PluginConfig();
            pluginConfig.setPluginPackage(pluginPackage);
            pluginConfig.setStatus(NOT_CONFIGURED);
            pluginConfigs.add(pluginConfig);

            pluginConfig.setName(validateName(trim(xPathEvaluator.getString("./@name", pluginConfigNode)), "Plugin name"));
            NodeList interfaceNodeList = xPathEvaluator.getNodeList("./interface", pluginConfigNode);
            if (interfaceNodeList != null && interfaceNodeList.getLength() > 0) {
                pluginConfig.setInterfaces(parsePluginConfigInterfaces(interfaceNodeList, pluginConfig));
            }
        }
        return pluginConfigs;
    }

    private List<PluginConfigInterface> parsePluginConfigInterfaces(NodeList interfaceNodeList, PluginConfig pluginConfig) throws XPathExpressionException {
        List<PluginConfigInterface> pluginConfigInterfaces = new ArrayList<>();

        for (int i = 0; i < interfaceNodeList.getLength(); i++) {
            Node interfaceNode = interfaceNodeList.item(i);

            PluginConfigInterface pluginConfigInterface = new PluginConfigInterface();
            pluginConfigInterface.setPluginConfig(pluginConfig);
            pluginConfigInterfaces.add(pluginConfigInterface);

            pluginConfigInterface.setName(validateName(trim(xPathEvaluator.getString("./@name", interfaceNode)), "Plugin interface name"));
            String serviceName = createServiceName(pluginConfig.getPluginPackage().getName(), pluginConfig.getName(), pluginConfigInterface.getName());
            pluginConfigInterface.setServiceName(serviceName);
            pluginConfigInterface.setServiceDisplayName(serviceName);
            pluginConfigInterface.setPath(trim(xPathEvaluator.getString("./@path", interfaceNode)));
            NodeList inputParameterNodeList = xPathEvaluator.getNodeList("./input-parameters/parameter", interfaceNode);
            if (inputParameterNodeList != null && inputParameterNodeList.getLength() > 0) {
                pluginConfigInterface.setInputParameters(parsePluginConfigInterfaceParameters(inputParameterNodeList, pluginConfigInterface, TYPE_INPUT, MAPPING_TYPE_CMDB_CI_TYPE));
            }
            NodeList outputParameterNodeList = xPathEvaluator.getNodeList("./output-parameters/parameter", interfaceNode);
            if (outputParameterNodeList != null && outputParameterNodeList.getLength() > 0) {
                pluginConfigInterface.setOutputParameters(parsePluginConfigInterfaceParameters(outputParameterNodeList, pluginConfigInterface, TYPE_OUTPUT, MAPPING_TYPE_NOT_AVAILABLE));
            }
        }

        return pluginConfigInterfaces;
    }

    private Set<PluginConfigInterfaceParameter> parsePluginConfigInterfaceParameters(NodeList parameterNodeList, PluginConfigInterface pluginConfigInterface, String parameterType, String mappingType) throws XPathExpressionException {
        Set<PluginConfigInterfaceParameter> pluginConfigInterfaceParameters = new LinkedHashSet<>();
        for (int i = 0; i < parameterNodeList.getLength(); i++) {
            Node parameterNode = parameterNodeList.item(i);

            PluginConfigInterfaceParameter pluginConfigInterfaceParameter = new PluginConfigInterfaceParameter();
            pluginConfigInterfaceParameter.setPluginConfigInterface(pluginConfigInterface);
            pluginConfigInterfaceParameter.setType(parameterType);
            pluginConfigInterfaceParameter.setMappingType(mappingType);
            pluginConfigInterfaceParameters.add(pluginConfigInterfaceParameter);

            pluginConfigInterfaceParameter.setName(trim(parameterNode.getTextContent()));
            pluginConfigInterfaceParameter.setDatatype(trim(xPathEvaluator.getString("./@datatype", parameterNode)));
        }
        return pluginConfigInterfaceParameters;
    }

    private String validateName(String name, String description) {
        if (name == null) throw new WecubeCoreException(description + " is required.");
        if (name.contains(SEPARATOR_OF_NAMES))
            throw new WecubeCoreException(String.format("Illegal character[%s] detected in %s[%s]", SEPARATOR_OF_NAMES, description, name));
        return name;
    }

    private String createServiceName(String packageName, String pluginName, String interfaceName) {
        return packageName + SEPARATOR_OF_NAMES + pluginName + SEPARATOR_OF_NAMES + interfaceName;
    }
}
