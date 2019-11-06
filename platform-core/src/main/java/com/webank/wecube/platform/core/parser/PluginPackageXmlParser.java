package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.commons.XPathEvaluator;
import com.webank.wecube.platform.core.domain.*;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.*;
import static org.apache.commons.lang3.StringUtils.trim;

public class PluginPackageXmlParser {
    private final static String SEPARATOR_OF_NAMES = "/";

    public static PluginPackageXmlParser newInstance(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        return new PluginPackageXmlParser(new InputSource(inputStream));
    }

    public static PluginPackageXmlParser newInstance(InputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        return new PluginPackageXmlParser(inputSource);
    }

    private XPathEvaluator xPathEvaluator;

    private PluginPackageXmlParser(InputSource inputSource) throws IOException, SAXException, ParserConfigurationException {
        xPathEvaluator = XPathEvaluator.newInstance(inputSource);
    }

    public PluginPackageDto parsePluginPackage() throws XPathExpressionException {
        PluginPackageDto pluginPackageDto = new PluginPackageDto();
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackageDto.setPluginPackage(pluginPackage);

        String package_name = ensureNotNull(trim(xPathEvaluator.getString("/package/@name")), "Package name");
        pluginPackageDto.setName(package_name);
        pluginPackage.setName(package_name);
        String trimmedVersion = trim(xPathEvaluator.getString("/package/@version"));
        pluginPackageDto.setVersion(trimmedVersion);
        pluginPackage.setVersion(trimmedVersion);
        pluginPackage.setStatus(PluginPackage.Status.UNREGISTERED);
        pluginPackage.setUploadTimestamp(new Timestamp(System.currentTimeMillis()));

        NodeList packageDependencyNodes = xPathEvaluator.getNodeList("/package/packageDependencies/packageDependency");
        if (null != packageDependencyNodes && packageDependencyNodes.getLength() > 0) {
            pluginPackage.setPluginPackageDependencies(parsePackageDependencies(packageDependencyNodes, pluginPackage));
        }

        NodeList menuNodes = xPathEvaluator.getNodeList("/package/menus/menu");
        if (null != menuNodes && menuNodes.getLength() > 0) {
            pluginPackage.setPluginPackageMenus(parseMenus(menuNodes, pluginPackage));
        }

        Node dataModelNode = xPathEvaluator.getNode("/package/dataModel");
        if (null != dataModelNode) {
            pluginPackageDto.setPluginPackageDataModelDto(parseDataModel(dataModelNode, pluginPackageDto));
        }

        NodeList systemVariableNodes = xPathEvaluator.getNodeList("/package/systemParameters/systemParameter");
        if (null != systemVariableNodes && systemVariableNodes.getLength() > 0) {
            pluginPackage.setSystemVariables(parseSystemVariables(systemVariableNodes, pluginPackage));
        }

        NodeList authorityNodes = xPathEvaluator.getNodeList("/package/authorities/authority");
        if (null != authorityNodes && authorityNodes.getLength() > 0) {
            pluginPackage.setPluginPackageAuthorities(parseAuthorities(authorityNodes, pluginPackage));
        }


        NodeList dockerNodes = xPathEvaluator.getNodeList("/package/resourceDependencies/docker");

        if (null != dockerNodes && dockerNodes.getLength() > 0) {
            Set<PluginPackageRuntimeResourcesDocker> dockers = new LinkedHashSet<>();
            for (int i = 0; i < dockerNodes.getLength(); i++) {
                Node dockerNode = dockerNodes.item(i);

                PluginPackageRuntimeResourcesDocker docker = new PluginPackageRuntimeResourcesDocker();
                docker.setImageName(getNonNullStringAttribute(dockerNode, "./@imageName", "Docker image name"));
                docker.setContainerName(getNonNullStringAttribute(dockerNode, "./@containerName", "Docker container name"));
                docker.setPortBindings(getStringAttribute(dockerNode, "./@portBindings"));
                docker.setVolumeBindings(getStringAttribute(dockerNode, "./@volumeBindings"));
                docker.setEnvVariables(getStringAttribute(dockerNode, "./@envVariables"));

                docker.setPluginPackage(pluginPackage);

                dockers.add(docker);

            }
            pluginPackage.setPluginPackageRuntimeResourcesDocker(dockers);
        }

        NodeList mysqlNodes = xPathEvaluator.getNodeList("/package/resourceDependencies/mysql");
        if (null != mysqlNodes && mysqlNodes.getLength() > 0) {
            Set<PluginPackageRuntimeResourcesMysql> mySqlSet = new LinkedHashSet<>();
            for (int i = 0; i < mysqlNodes.getLength(); i++) {
                Node mysqlNode = mysqlNodes.item(i);

                PluginPackageRuntimeResourcesMysql mysql = new PluginPackageRuntimeResourcesMysql();
                mysql.setSchemaName(getNonNullStringAttribute(mysqlNode, "./@schema", "Mysql schema"));
                mysql.setInitFileName(getStringAttribute(mysqlNode, "./@initFileName"));
                mysql.setUpgradeFileName(getStringAttribute(mysqlNode, "./@upgradeFileName"));

                mysql.setPluginPackage(pluginPackage);

                mySqlSet.add(mysql);
            }

            pluginPackage.setPluginPackageRuntimeResourcesMysql(mySqlSet);
        }

        NodeList s3Nodes = xPathEvaluator.getNodeList("/package/resourceDependencies/s3");
        if (null != s3Nodes && s3Nodes.getLength() > 0) {
            Set<PluginPackageRuntimeResourcesS3> s3s = new LinkedHashSet<>();
            for (int i = 0; i < s3Nodes.getLength(); i++) {
                Node s3Node = s3Nodes.item(i);

                PluginPackageRuntimeResourcesS3 s3 = new PluginPackageRuntimeResourcesS3();
                s3.setBucketName(getNonNullStringAttribute(s3Node, "./@bucketName", "S3 bucket name"));
                s3.setPluginPackage(pluginPackage);

                s3s.add(s3);
            }

            pluginPackage.setPluginPackageRuntimeResourcesS3(s3s);
        }

        NodeList pluginConfigNodes = xPathEvaluator.getNodeList("/package/plugins/plugin");
        if (null != pluginConfigNodes && pluginConfigNodes.getLength() > 0) {
            pluginPackage.setPluginConfigs(parsePluginConfigs(pluginConfigNodes, pluginPackage));
        }

        return pluginPackageDto;
    }

    private Set<PluginPackageAuthority> parseAuthorities(NodeList authorityNodes, PluginPackage pluginPackage) throws XPathExpressionException {
        Set<PluginPackageAuthority> pluginPackageAuthorities = new LinkedHashSet<>();
        for (int i = 0; i < authorityNodes.getLength(); i++) {
            Node authorityNode = authorityNodes.item(i);

            String system_role_name = getNonNullStringAttribute(authorityNode, "./@systemRoleName", "System role name");

            NodeList roleMenuNodes = xPathEvaluator.getNodeList("./menu", authorityNode);
            if (null != roleMenuNodes && roleMenuNodes.getLength() > 0) {
                for (int j = 0; j < roleMenuNodes.getLength(); j++) {
                    PluginPackageAuthority pluginPackageAuthority = new PluginPackageAuthority();
                    pluginPackageAuthority.setRoleName(system_role_name);

                    Node roleMenuNode = roleMenuNodes.item(j);

                    String menu_code_in_authority = getNonNullStringAttribute(roleMenuNode, "./@code", "Menu code in authority");
                    pluginPackageAuthority.setMenuCode(menu_code_in_authority);

                    pluginPackageAuthority.setPluginPackage(pluginPackage);

                    pluginPackageAuthorities.add(pluginPackageAuthority);
                }
            }
        }

        return pluginPackageAuthorities;
    }

    private Set<SystemVariable> parseSystemVariables(NodeList systemVariableNodes, PluginPackage pluginPackage) throws XPathExpressionException {
        Set<SystemVariable> systemVariables = new LinkedHashSet<>();

        for (int i = 0; i < systemVariableNodes.getLength(); i++) {
            Node systemVariableNode = systemVariableNodes.item(i);

            SystemVariable systemVariable = new SystemVariable();

            systemVariable.setName(getNonNullStringAttribute(systemVariableNode, "./@name", "System variable name"));
            systemVariable.setDefaultValue(getStringAttribute(systemVariableNode, "./@defaultValue"));
            systemVariable.setScopeType(getStringAttribute(systemVariableNode, "./@scopeType"));

            systemVariable.setPluginPackage(pluginPackage);

            systemVariables.add(systemVariable);
        }
        return systemVariables;
    }

    private PluginPackageDataModelDto parseDataModel(Node dataModelNode, PluginPackageDto pluginPackageDto) throws XPathExpressionException {
        PluginPackageDataModelDto pluginPackageDataModelDto = new PluginPackageDataModelDto();
        pluginPackageDataModelDto.setPackageName(pluginPackageDto.getName());
        pluginPackageDataModelDto.setPackageVersion(pluginPackageDto.getVersion());
        Boolean isDynamic = getBooleanAttribute(dataModelNode, "./@isDynamic");
        pluginPackageDataModelDto.setDynamic(isDynamic);
        pluginPackageDataModelDto.setUpdatePath(getStringAttribute(dataModelNode, "./@path"));
        pluginPackageDataModelDto.setUpdateMethod(getStringAttribute(dataModelNode, "./@method"));
        pluginPackageDataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name());
        pluginPackageDataModelDto.setUpdateTimestamp(PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name());

        NodeList entityNodes = xPathEvaluator.getNodeList("./entity", dataModelNode);

        if (null != entityNodes && entityNodes.getLength() > 0) {
            pluginPackageDataModelDto.setPluginPackageEntities(parseDataModelEntities(entityNodes, pluginPackageDataModelDto));
        }

        return pluginPackageDataModelDto;
    }

    private Set<PluginPackageEntityDto> parseDataModelEntities(NodeList entityNodes, PluginPackageDataModelDto dataModelDto) throws XPathExpressionException {
        Set<PluginPackageEntityDto> pluginPackageEntities = new LinkedHashSet<>();

        for (int i = 0; i < entityNodes.getLength(); i++) {
            Node entityNode = entityNodes.item(i);

            PluginPackageEntityDto pluginPackageEntity = new PluginPackageEntityDto();

            pluginPackageEntity.setPackageName(dataModelDto.getPackageName());
            pluginPackageEntity.setPackageVersion(dataModelDto.getPackageVersion());

            pluginPackageEntity.setName(getNonNullStringAttribute(entityNode, "./@name", "Entity name"));
            pluginPackageEntity.setDisplayName(getNonNullStringAttribute(entityNode, "./@displayName", "Entity display name"));
            pluginPackageEntity.setDescription(getNonNullStringAttribute(entityNode, "./@description", "Entity description"));

            NodeList entityAttributeNodes = xPathEvaluator.getNodeList("./attribute", entityNode);
            if (null != entityAttributeNodes && entityAttributeNodes.getLength() > 0) {
                pluginPackageEntity.setAttributes(parseDataModelEntityAttributes(entityAttributeNodes, pluginPackageEntity));
            }

            pluginPackageEntities.add(pluginPackageEntity);
        }
        return pluginPackageEntities;
    }

    private List<PluginPackageAttributeDto> parseDataModelEntityAttributes(NodeList entityAttributeNodes, PluginPackageEntityDto pluginPackageEntity) throws XPathExpressionException {
        List<PluginPackageAttributeDto> pluginPackageAttributes = new ArrayList<>();
        for (int i = 0; i < entityAttributeNodes.getLength(); i++) {
            Node attributeNode = entityAttributeNodes.item(i);

            PluginPackageAttributeDto pluginPackageAttribute = new PluginPackageAttributeDto();

            pluginPackageAttribute.setPackageName(pluginPackageEntity.getPackageName());
            pluginPackageAttribute.setPackageVersion(pluginPackageEntity.getPackageVersion());
            pluginPackageAttribute.setEntityName(pluginPackageEntity.getName());

            pluginPackageAttribute.setName(getNonNullStringAttribute(attributeNode, "./@name", "Entity attribute name"));
            pluginPackageAttribute.setDataType(getNonNullStringAttribute(attributeNode, "./@datatype", "Entity attribute data type"));
            pluginPackageAttribute.setDescription(getNonNullStringAttribute(attributeNode, "./@description", "Entity attribute description"));

            pluginPackageAttribute.setRefPackageName(getStringAttribute(attributeNode, "./@refPackage"));
            pluginPackageAttribute.setRefEntityName(getStringAttribute(attributeNode, "./@refEntity"));
            pluginPackageAttribute.setRefAttributeName(getStringAttribute(attributeNode, "./@ref"));

            pluginPackageAttributes.add(pluginPackageAttribute);
        }

        return pluginPackageAttributes;
    }

    private Set<PluginPackageMenu> parseMenus(NodeList menuNodes, PluginPackage pluginPackage) throws XPathExpressionException {
        Set<PluginPackageMenu> pluginPackageMenus = new LinkedHashSet<>();

        for (int i = 0; i < menuNodes.getLength(); i++) {
            Node pluginPackageMenuNode = menuNodes.item(i);

            PluginPackageMenu pluginPackageMenu = new PluginPackageMenu();
            pluginPackageMenu.setCode(getNonNullStringAttribute(pluginPackageMenuNode, "./@code", "Plugin package menu code"));
            pluginPackageMenu.setCategory(getNonNullStringAttribute(pluginPackageMenuNode, "./@cat", "Plugin package menu category"));
            pluginPackageMenu.setDisplayName(getNonNullStringAttribute(pluginPackageMenuNode, "./@displayName", "Plugin package menu display name"));
            pluginPackageMenu.setPath(pluginPackageMenuNode.getTextContent());

            pluginPackageMenu.setPluginPackage(pluginPackage);

            pluginPackageMenus.add(pluginPackageMenu);
        }

        return pluginPackageMenus;
    }

    private Set<PluginPackageDependency> parsePackageDependencies(NodeList packageDependencyNodes, PluginPackage pluginPackage) throws XPathExpressionException {
        Set<PluginPackageDependency> pluginPackageDependencies = new LinkedHashSet<>();
        for (int i = 0; i < packageDependencyNodes.getLength(); i++) {
            Node pluginPackageDependencyNode = packageDependencyNodes.item(i);

            PluginPackageDependency pluginPackageDependency = new PluginPackageDependency();
            pluginPackageDependency.setPluginPackage(pluginPackage);

            pluginPackageDependency.setDependencyPackageName(getNonNullStringAttribute(pluginPackageDependencyNode, "./@name", "Package dependency name"));
            pluginPackageDependency.setDependencyPackageVersion(getNonNullStringAttribute(pluginPackageDependencyNode, "./@version", "Package dependency version"));

            pluginPackageDependency.setPluginPackage(pluginPackage);

            pluginPackageDependencies.add(pluginPackageDependency);
        }
        return pluginPackageDependencies;
    }

    private String getNonNullStringAttribute(Node attributeNode, String attributeExpression, String attributeDescription) throws XPathExpressionException {
        return ensureNotNull(getStringAttribute(attributeNode, attributeExpression), attributeDescription);
    }

    private String getStringAttribute(Node attributeNode, String attributeExpression) throws XPathExpressionException {
        return xPathEvaluator.getString(attributeExpression, attributeNode);
    }

    private Boolean getBooleanAttribute(Node attributeNode, String attributeExpression) throws XPathExpressionException {
        return xPathEvaluator.getBoolean(attributeExpression, attributeNode);
    }

    private Set<PluginConfig> parsePluginConfigs(NodeList pluginNodeList, PluginPackage pluginPackage) throws XPathExpressionException {
        Set<PluginConfig> pluginConfigs = new LinkedHashSet<>();

        for (int i = 0; i < pluginNodeList.getLength(); i++) {
            Node pluginConfigNode = pluginNodeList.item(i);

            PluginConfig pluginConfig = new PluginConfig();
            pluginConfig.setPluginPackage(pluginPackage);
            pluginConfig.setStatus(DISABLED);
            pluginConfig.setName(getNonNullStringAttribute(pluginConfigNode, "./@name", "Plugin name"));
            pluginConfig.setEntityName(getNonNullStringAttribute(pluginConfigNode, "./@entity", "Entity name"));

            NodeList pluginConfigInterfaceNodes = xPathEvaluator.getNodeList("./interface", pluginConfigNode);
            if (pluginConfigInterfaceNodes != null && pluginConfigInterfaceNodes.getLength() > 0) {
                pluginConfig.setInterfaces(new HashSet<>(parsePluginConfigInterfaces(pluginConfigInterfaceNodes, pluginConfig)));
            }

            pluginConfigs.add(pluginConfig);
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

            pluginConfigInterface.setAction(ensureNotNull(trim(xPathEvaluator.getString("./@name", interfaceNode)), "Plugin interface name"));
            String serviceName = createServiceName(pluginConfig.getPluginPackage().getName(), pluginConfig.getName(), pluginConfigInterface.getAction());
            pluginConfigInterface.setServiceName(serviceName);
            pluginConfigInterface.setServiceDisplayName(serviceName);
            pluginConfigInterface.setPath(getStringAttribute(interfaceNode, "./@path"));
            pluginConfigInterface.setHttpMethod(getStringAttribute(interfaceNode, "./@httpMethod"));
            
            NodeList inputParameterNodeList = xPathEvaluator.getNodeList("./inputParameters/parameter", interfaceNode);
            if (inputParameterNodeList != null && inputParameterNodeList.getLength() > 0) {
                pluginConfigInterface.setInputParameters(parsePluginConfigInterfaceParameters(inputParameterNodeList, pluginConfigInterface, TYPE_INPUT, MAPPING_TYPE_CMDB_CI_TYPE));
            }
            NodeList outputParameterNodeList = xPathEvaluator.getNodeList("./outputParameters/parameter", interfaceNode);
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

            pluginConfigInterfaceParameter.setName(trim(parameterNode.getTextContent()));
            pluginConfigInterfaceParameter.setDataType(getStringAttribute(parameterNode, "./@datatype"));
            pluginConfigInterfaceParameter.setMappingType(getStringAttribute(parameterNode, "./@mappingType"));
            String mappingSystemVariableIdString = getStringAttribute(parameterNode, "./@mappingSystemVariableId");
            if (StringUtils.isNotEmpty(mappingSystemVariableIdString)) {
                pluginConfigInterfaceParameter.setMappingSystemVariableId(Integer.parseInt(mappingSystemVariableIdString));
            }
            String mappingEntityExpression = getStringAttribute(parameterNode, "./@mappingEntityExpression");
            if (StringUtils.isNotEmpty(mappingEntityExpression)) {
                pluginConfigInterfaceParameter.setMappingEntityExpression(mappingEntityExpression);
            }
            pluginConfigInterfaceParameter.setRequired(getStringAttribute(parameterNode, "./@required"));

            pluginConfigInterfaceParameters.add(pluginConfigInterfaceParameter);
        }
        return pluginConfigInterfaceParameters;
    }

    private String ensureNotNull(String name, String description) {
        name = trim(name);
        if (name == null) throw new WecubeCoreException(description + " is required.");
        if (name.contains(SEPARATOR_OF_NAMES))
            throw new WecubeCoreException(String.format("Illegal character[%s] detected in %s[%s]", SEPARATOR_OF_NAMES, description, name));
        return name;
    }

    private String createServiceName(String packageName, String pluginName, String interfaceName) {
        return packageName + SEPARATOR_OF_NAMES + pluginName + SEPARATOR_OF_NAMES + interfaceName;
    }
}
