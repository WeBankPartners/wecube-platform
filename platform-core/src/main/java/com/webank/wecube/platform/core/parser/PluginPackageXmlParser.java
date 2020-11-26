//package com.webank.wecube.platform.core.parser;
//
//import com.google.common.collect.Lists;
//import com.webank.wecube.platform.core.commons.WecubeCoreException;
//import com.webank.wecube.platform.core.commons.XPathEvaluator;
//import com.webank.wecube.platform.core.domain.*;
//import com.webank.wecube.platform.core.domain.plugin.*;
//import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
//import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
//import com.webank.wecube.platform.core.dto.PluginPackageDto;
//import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
//import com.webank.wecube.platform.core.utils.constant.DataModelDataType;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.core.log.LogDelegateFactory;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.XPathExpressionException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Timestamp;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
//import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.*;
//import static com.webank.wecube.platform.core.utils.Constants.GLOBAL_SYSTEM_VARIABLES;
//import static org.apache.commons.lang3.StringUtils.trim;
//
//public class PluginPackageXmlParser {
//    private final static String SEPARATOR_OF_NAMES = "/";
//    public static final String DEFAULT_DATA_MODEL_UPDATE_PATH = "/data-model";
//    public static final String DEFAULT_DATA_MODEL_UPDATE_METHOD = "GET";
//    public static final String DEFAULT_REQUIRED = "N";
//    public static final String DEFAULT_SENSITIVE_DATA = "N";
//    public static final String DEFAULT_TARGET_ENTITY_FILTER_RULE = "";
//    public static final String DEFAULT_FILTER_RULE_FOR_INTERFACE = "";
//
//    public static PluginPackageXmlParser newInstance(InputStream inputStream)
//            throws ParserConfigurationException, SAXException, IOException {
//        return new PluginPackageXmlParser(new InputSource(inputStream));
//    }
//
//    public static PluginPackageXmlParser newInstance(InputSource inputSource)
//            throws ParserConfigurationException, SAXException, IOException {
//        return new PluginPackageXmlParser(inputSource);
//    }
//
//    private XPathEvaluator xPathEvaluator;
//
//    private PluginPackageXmlParser(InputSource inputSource)
//            throws IOException, SAXException, ParserConfigurationException {
//        xPathEvaluator = XPathEvaluator.newInstance(inputSource);
//    }
//
//    private boolean equalGlobalSystemVariableName(String systemVariableName) {
//        return Lists.newArrayList(GLOBAL_SYSTEM_VARIABLES).stream().filter(x -> systemVariableName.equals(x))
//                .collect(Collectors.toList()).size() > 0;
//    }
//
//    public PluginPackageDto parsePluginPackage() throws XPathExpressionException {
//        PluginPackageDto pluginPackageDto = new PluginPackageDto();
//        PluginPackage pluginPackage = new PluginPackage();
//        pluginPackageDto.setPluginPackage(pluginPackage);
//
//        String package_name = ensureNotNull(trim(xPathEvaluator.getString("/package/@name")), "Package name");
//        pluginPackageDto.setName(package_name);
//        pluginPackage.setName(package_name);
//        String trimmedVersion = trim(xPathEvaluator.getString("/package/@version"));
//        pluginPackageDto.setVersion(trimmedVersion);
//        pluginPackage.setVersion(trimmedVersion);
//        pluginPackage.setStatus(PluginPackage.Status.UNREGISTERED);
//        pluginPackage.setUploadTimestamp(new Timestamp(System.currentTimeMillis()));
//
//        NodeList packageDependencyNodes = xPathEvaluator.getNodeList("/package/packageDependencies/packageDependency");
//        if (null != packageDependencyNodes && packageDependencyNodes.getLength() > 0) {
//            pluginPackage.setPluginPackageDependencies(parsePackageDependencies(packageDependencyNodes, pluginPackage));
//        }
//
//        NodeList menuNodes = xPathEvaluator.getNodeList("/package/menus/menu");
//        if (null != menuNodes && menuNodes.getLength() > 0) {
//            pluginPackage.setPluginPackageMenus(parseMenus(menuNodes, pluginPackage));
//        }
//
//        Node dataModelNode = xPathEvaluator.getNode("/package/dataModel");
//        if (null != dataModelNode) {
//            pluginPackageDto.setPluginPackageDataModelDto(parseDataModel(dataModelNode, pluginPackageDto));
//        }
//
//        NodeList systemVariableNodes = xPathEvaluator.getNodeList("/package/systemParameters/systemParameter");
//        if (null != systemVariableNodes && systemVariableNodes.getLength() > 0) {
//            pluginPackage.setSystemVariables(parseSystemVariables(systemVariableNodes, pluginPackage));
//        }
//
//        NodeList authorityNodes = xPathEvaluator.getNodeList("/package/authorities/authority");
//        if (null != authorityNodes && authorityNodes.getLength() > 0) {
//            pluginPackage.setPluginPackageAuthorities(parseAuthorities(authorityNodes, pluginPackage));
//        }
//
//        NodeList dockerNodes = xPathEvaluator.getNodeList("/package/resourceDependencies/docker");
//
//        if (null != dockerNodes && dockerNodes.getLength() > 0) {
//            Set<PluginPackageRuntimeResourcesDocker> dockers = new LinkedHashSet<>();
//            for (int i = 0; i < dockerNodes.getLength(); i++) {
//                Node dockerNode = dockerNodes.item(i);
//
//                PluginPackageRuntimeResourcesDocker docker = new PluginPackageRuntimeResourcesDocker();
//                docker.setImageName(getNonNullStringAttribute(dockerNode, "./@imageName", "Docker image name"));
//                docker.setContainerName(
//                        getNonNullStringAttribute(dockerNode, "./@containerName", "Docker container name"));
//                docker.setPortBindings(getStringAttribute(dockerNode, "./@portBindings"));
//                docker.setVolumeBindings(getStringAttribute(dockerNode, "./@volumeBindings"));
//                docker.setEnvVariables(getStringAttribute(dockerNode, "./@envVariables"));
//
//                docker.setPluginPackage(pluginPackage);
//
//                dockers.add(docker);
//
//            }
//            pluginPackage.setPluginPackageRuntimeResourcesDocker(dockers);
//        }
//
//        NodeList mysqlNodes = xPathEvaluator.getNodeList("/package/resourceDependencies/mysql");
//        if (null != mysqlNodes && mysqlNodes.getLength() > 0) {
//            Set<PluginPackageRuntimeResourcesMysql> mySqlSet = new LinkedHashSet<>();
//            for (int i = 0; i < mysqlNodes.getLength(); i++) {
//                Node mysqlNode = mysqlNodes.item(i);
//
//                PluginPackageRuntimeResourcesMysql mysql = new PluginPackageRuntimeResourcesMysql();
//                mysql.setSchemaName(getNonNullStringAttribute(mysqlNode, "./@schema", "Mysql schema"));
//                mysql.setInitFileName(getStringAttribute(mysqlNode, "./@initFileName"));
//                mysql.setUpgradeFileName(getStringAttribute(mysqlNode, "./@upgradeFileName"));
//
//                mysql.setPluginPackage(pluginPackage);
//
//                mySqlSet.add(mysql);
//            }
//
//            pluginPackage.setPluginPackageRuntimeResourcesMysql(mySqlSet);
//        }
//
//        NodeList s3Nodes = xPathEvaluator.getNodeList("/package/resourceDependencies/s3");
//        if (null != s3Nodes && s3Nodes.getLength() > 0) {
//            Set<PluginPackageRuntimeResourcesS3> s3s = new LinkedHashSet<>();
//            for (int i = 0; i < s3Nodes.getLength(); i++) {
//                Node s3Node = s3Nodes.item(i);
//
//                PluginPackageRuntimeResourcesS3 s3 = new PluginPackageRuntimeResourcesS3();
//                s3.setBucketName(getNonNullStringAttribute(s3Node, "./@bucketName", "S3 bucket name"));
//                s3.setPluginPackage(pluginPackage);
//
//                s3s.add(s3);
//            }
//
//            pluginPackage.setPluginPackageRuntimeResourcesS3(s3s);
//        }
//
//        NodeList pluginConfigNodes = xPathEvaluator.getNodeList("/package/plugins/plugin");
//        if (null != pluginConfigNodes && pluginConfigNodes.getLength() > 0) {
//            pluginPackage.setPluginConfigs(parsePluginConfigs(pluginConfigNodes, pluginPackage));
//        }
//
//        return pluginPackageDto;
//    }
//
//    private Set<PluginPackageAuthority> parseAuthorities(NodeList authorityNodes, PluginPackage pluginPackage)
//            throws XPathExpressionException {
//        Set<PluginPackageAuthority> pluginPackageAuthorities = new LinkedHashSet<>();
//        for (int i = 0; i < authorityNodes.getLength(); i++) {
//            Node authorityNode = authorityNodes.item(i);
//
//            String system_role_name = getNonNullStringAttribute(authorityNode, "./@systemRoleName", "System role name");
//
//            NodeList roleMenuNodes = xPathEvaluator.getNodeList("./menu", authorityNode);
//            if (null != roleMenuNodes && roleMenuNodes.getLength() > 0) {
//                for (int j = 0; j < roleMenuNodes.getLength(); j++) {
//                    PluginPackageAuthority pluginPackageAuthority = new PluginPackageAuthority();
//                    pluginPackageAuthority.setRoleName(system_role_name);
//
//                    Node roleMenuNode = roleMenuNodes.item(j);
//
//                    String menu_code_in_authority = getNonNullStringAttribute(roleMenuNode, "./@code",
//                            "Menu code in authority");
//                    pluginPackageAuthority.setMenuCode(menu_code_in_authority);
//
//                    pluginPackageAuthority.setPluginPackage(pluginPackage);
//
//                    pluginPackageAuthorities.add(pluginPackageAuthority);
//                }
//            }
//        }
//
//        return pluginPackageAuthorities;
//    }
//
//    private Set<SystemVariable> parseSystemVariables(NodeList systemVariableNodes, PluginPackage pluginPackage)
//            throws XPathExpressionException {
//        Set<SystemVariable> systemVariables = new LinkedHashSet<>();
//
//        for (int i = 0; i < systemVariableNodes.getLength(); i++) {
//            Node systemVariableNode = systemVariableNodes.item(i);
//
//            SystemVariable systemVariable = new SystemVariable();
//
//            systemVariable.setStatus(SystemVariable.INACTIVE);
//
//            String systemVariableName = getNonNullStringAttribute(systemVariableNode, "./@name",
//                    "System variable name");
//            if (equalGlobalSystemVariableName(systemVariableName)) {
//                String msg = String.format("Duplicated define WeCube Global System Variable [%s]", systemVariableName);
//                throw new WecubeCoreException("3299", msg, systemVariableName);
//            }
//            systemVariable.setName(systemVariableName);
//            systemVariable.setDefaultValue(getStringAttribute(systemVariableNode, "./@defaultValue"));
//            String scopeType = getStringAttribute(systemVariableNode, "./@scopeType");
//            if (SystemVariable.SCOPE_GLOBAL.equalsIgnoreCase(scopeType)) {
//                systemVariable.setScope(SystemVariable.SCOPE_GLOBAL);
//            } else {
//                systemVariable.setScope(pluginPackage.getName());
//            }
//            systemVariable.setSource(pluginPackage.getId());
//            systemVariable.setPackageName(pluginPackage.getName());
//
//            systemVariables.add(systemVariable);
//        }
//        return systemVariables;
//    }
//
//    private PluginPackageDataModelDto parseDataModel(Node dataModelNode, PluginPackageDto pluginPackageDto)
//            throws XPathExpressionException {
//        PluginPackageDataModelDto pluginPackageDataModelDto = new PluginPackageDataModelDto();
//        pluginPackageDataModelDto.setPackageName(pluginPackageDto.getName());
//        pluginPackageDataModelDto.setVersion(1);
//        Boolean isDynamic = getBooleanAttribute(dataModelNode, "./@isDynamic");
//        pluginPackageDataModelDto.setDynamic(isDynamic);
//        String updatePath = getStringAttribute(dataModelNode, "./@path");
//        if (StringUtils.isEmpty(updatePath) && isDynamic) {
//            updatePath = DEFAULT_DATA_MODEL_UPDATE_PATH;
//        }
//        pluginPackageDataModelDto.setUpdatePath(updatePath);
//        String updateMethod = getStringAttribute(dataModelNode, "./@method");
//        if (StringUtils.isEmpty(updateMethod) && isDynamic) {
//            updateMethod = DEFAULT_DATA_MODEL_UPDATE_METHOD;
//        }
//        pluginPackageDataModelDto.setUpdateMethod(updateMethod);
//        pluginPackageDataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name());
//        pluginPackageDataModelDto.setUpdateTime(System.currentTimeMillis());
//
//        NodeList entityNodes = xPathEvaluator.getNodeList("./entity", dataModelNode);
//
//        if (null != entityNodes && entityNodes.getLength() > 0) {
//            pluginPackageDataModelDto
//                    .setPluginPackageEntities(parseDataModelEntities(entityNodes, pluginPackageDataModelDto));
//        }
//
//        return pluginPackageDataModelDto;
//    }
//
//    private Set<PluginPackageEntityDto> parseDataModelEntities(NodeList entityNodes,
//            PluginPackageDataModelDto dataModelDto) throws XPathExpressionException {
//        Set<PluginPackageEntityDto> pluginPackageEntities = new LinkedHashSet<>();
//
//        for (int i = 0; i < entityNodes.getLength(); i++) {
//            Node entityNode = entityNodes.item(i);
//
//            PluginPackageEntityDto pluginPackageEntity = new PluginPackageEntityDto();
//
//            pluginPackageEntity.setPackageName(dataModelDto.getPackageName());
//            // set data model version as 1 by default, where there is version
//            // update on
//            // DataModel.version, update Entity.dataModelVersion as well.
//            pluginPackageEntity.setDataModelVersion(1);
//
//            pluginPackageEntity.setName(getNonNullStringAttribute(entityNode, "./@name", "Entity name"));
//            pluginPackageEntity
//                    .setDisplayName(getNonNullStringAttribute(entityNode, "./@displayName", "Entity display name"));
//            pluginPackageEntity.setDescription(getStringAttribute(entityNode, "./@description"));
//
//            NodeList entityAttributeNodes = xPathEvaluator.getNodeList("./attribute", entityNode);
//            if (null != entityAttributeNodes && entityAttributeNodes.getLength() > 0) {
//                pluginPackageEntity
//                        .setAttributes(parseDataModelEntityAttributes(entityAttributeNodes, pluginPackageEntity));
//            }
//
//            pluginPackageEntities.add(pluginPackageEntity);
//        }
//        return pluginPackageEntities;
//    }
//
//    private List<PluginPackageAttributeDto> parseDataModelEntityAttributes(NodeList entityAttributeNodes,
//            PluginPackageEntityDto pluginPackageEntity) throws XPathExpressionException {
//        List<PluginPackageAttributeDto> pluginPackageAttributes = new ArrayList<>();
//        for (int i = 0; i < entityAttributeNodes.getLength(); i++) {
//            Node attributeNode = entityAttributeNodes.item(i);
//
//            PluginPackageAttributeDto pluginPackageAttribute = new PluginPackageAttributeDto();
//
//            pluginPackageAttribute.setPackageName(pluginPackageEntity.getPackageName());
//            pluginPackageAttribute.setEntityName(pluginPackageEntity.getName());
//
//            pluginPackageAttribute
//                    .setName(getNonNullStringAttribute(attributeNode, "./@name", "Entity attribute name"));
//            String dataType = getNonNullStringAttribute(attributeNode, "./@datatype", "Entity attribute data type");
//            pluginPackageAttribute.setDataType(dataType);
//            pluginPackageAttribute.setDescription(getStringAttribute(attributeNode, "./@description"));
//
//            String refPackage = getStringAttribute(attributeNode, "./@refPackage");
//            if (StringUtils.isEmpty(refPackage) && DataModelDataType.Ref.getCode().equals(dataType)) {
//                refPackage = pluginPackageEntity.getPackageName();
//            }
//            pluginPackageAttribute.setRefPackageName(refPackage);
//            String refEntity = getStringAttribute(attributeNode, "./@refEntity");
//            if (StringUtils.isEmpty(refEntity) && DataModelDataType.Ref.getCode().equals(dataType)) {
//                refEntity = pluginPackageEntity.getName();
//            }
//            pluginPackageAttribute.setRefEntityName(refEntity);
//            pluginPackageAttribute.setRefAttributeName(getStringAttribute(attributeNode, "./@ref"));
//
//            pluginPackageAttributes.add(pluginPackageAttribute);
//        }
//
//        return pluginPackageAttributes;
//    }
//
//    private Set<PluginPackageMenu> parseMenus(NodeList menuNodes, PluginPackage pluginPackage)
//            throws XPathExpressionException {
//        Set<PluginPackageMenu> pluginPackageMenus = new LinkedHashSet<>();
//
//        for (int i = 0; i < menuNodes.getLength(); i++) {
//            Node pluginPackageMenuNode = menuNodes.item(i);
//
//            PluginPackageMenu pluginPackageMenu = new PluginPackageMenu();
//            pluginPackageMenu
//                    .setCode(getNonNullStringAttribute(pluginPackageMenuNode, "./@code", "Plugin package menu code"));
//            pluginPackageMenu.setCategory(
//                    getNonNullStringAttribute(pluginPackageMenuNode, "./@cat", "Plugin package menu category"));
//            String menuDisplayName = getNonNullStringAttribute(pluginPackageMenuNode, "./@displayName",
//                    "Plugin package menu display name");
//            pluginPackageMenu.setDisplayName(menuDisplayName);
//            String localDisplayName = getStringAttribute(pluginPackageMenuNode, "./@localDisplayName");
//            if (StringUtils.isNotBlank(localDisplayName)) {
//                pluginPackageMenu.setLocalDisplayName(localDisplayName);
//            } else {
//                pluginPackageMenu.setLocalDisplayName(menuDisplayName);
//            }
//            pluginPackageMenu.setPath(pluginPackageMenuNode.getTextContent());
//            pluginPackageMenu.setSource(pluginPackage.getId());
//
//            pluginPackageMenu.setPluginPackage(pluginPackage);
//
//            pluginPackageMenus.add(pluginPackageMenu);
//        }
//
//        return pluginPackageMenus;
//    }
//
//    private Set<PluginPackageDependency> parsePackageDependencies(NodeList packageDependencyNodes,
//            PluginPackage pluginPackage) throws XPathExpressionException {
//        Set<PluginPackageDependency> pluginPackageDependencies = new LinkedHashSet<>();
//        for (int i = 0; i < packageDependencyNodes.getLength(); i++) {
//            Node pluginPackageDependencyNode = packageDependencyNodes.item(i);
//
//            PluginPackageDependency pluginPackageDependency = new PluginPackageDependency();
//            pluginPackageDependency.setPluginPackage(pluginPackage);
//
//            pluginPackageDependency.setDependencyPackageName(
//                    getNonNullStringAttribute(pluginPackageDependencyNode, "./@name", "Package dependency name"));
//            pluginPackageDependency.setDependencyPackageVersion(
//                    getNonNullStringAttribute(pluginPackageDependencyNode, "./@version", "Package dependency version"));
//
//            pluginPackageDependency.setPluginPackage(pluginPackage);
//
//            pluginPackageDependencies.add(pluginPackageDependency);
//        }
//        return pluginPackageDependencies;
//    }
//
//    private String getNonNullStringAttribute(Node attributeNode, String attributeExpression,
//            String attributeDescription) throws XPathExpressionException {
//        return ensureNotNull(getStringAttribute(attributeNode, attributeExpression), attributeDescription);
//    }
//
//    private String getStringAttribute(Node attributeNode, String attributeExpression) throws XPathExpressionException {
//        return xPathEvaluator.getString(attributeExpression, attributeNode);
//    }
//
//    private Boolean getBooleanAttribute(Node attributeNode, String attributeExpression)
//            throws XPathExpressionException {
//        return xPathEvaluator.getBoolean(attributeExpression, attributeNode);
//    }
//
//    private Set<PluginConfig> parsePluginConfigs(NodeList pluginNodeList, PluginPackage pluginPackage)
//            throws XPathExpressionException {
//        Set<PluginConfig> pluginConfigs = new LinkedHashSet<>();
//
//        for (int i = 0; i < pluginNodeList.getLength(); i++) {
//            Node pluginConfigNode = pluginNodeList.item(i);
//
//            PluginConfig pluginConfig = new PluginConfig();
//            pluginConfig.setPluginPackage(pluginPackage);
//            pluginConfig.setStatus(DISABLED);
//            pluginConfig.setName(getNonNullStringAttribute(pluginConfigNode, "./@name", "Plugin name"));
//            String targetPackage = getStringAttribute(pluginConfigNode, "./@targetPackage");
//            if (StringUtils.isNotBlank(targetPackage)) {
//                pluginConfig.setTargetPackage(targetPackage);
//            } else {
//                pluginConfig.setTargetPackage(pluginPackage.getName());
//            }
//
//            String targetEntity = getStringAttribute(pluginConfigNode, "./@targetEntity");
//            if (StringUtils.isNotBlank(targetEntity)) {
//                pluginConfig.setTargetEntity(targetEntity);
//            }
//
//            String registerName = getStringAttribute(pluginConfigNode, "./@registerName");
//            if (StringUtils.isNotBlank(registerName)) {
//                pluginConfig.setRegisterName(registerName);
//            }
//
//            String targetEntityFilterRule = getStringAttribute(pluginConfigNode, "./@targetEntityFilterRule");
//
//            if (StringUtils.isNotBlank(targetEntityFilterRule)) {
//                pluginConfig.setTargetEntityFilterRule(targetEntityFilterRule);
//            } else {
//                pluginConfig.setTargetEntityFilterRule(DEFAULT_TARGET_ENTITY_FILTER_RULE);
//            }
//
//            NodeList pluginConfigInterfaceNodes = xPathEvaluator.getNodeList("./interface", pluginConfigNode);
//            if (pluginConfigInterfaceNodes != null && pluginConfigInterfaceNodes.getLength() > 0) {
//                pluginConfig.setInterfaces(
//                        new HashSet<>(parsePluginConfigInterfaces(pluginConfigInterfaceNodes, pluginConfig)));
//            }
//
//            NodeList roleBindNodes = xPathEvaluator.getNodeList("./roleBinds/roleBind", pluginConfigNode);
//            if (roleBindNodes != null && roleBindNodes.getLength() > 0) {
//                List<RoleBind> roleBinds = parseRoleBinds(roleBindNodes, pluginConfig);
//                pluginConfig.setRoleBinds(roleBinds);
//            }
//
//            pluginConfigs.add(pluginConfig);
//        }
//        return pluginConfigs;
//    }
//
//    private List<RoleBind> parseRoleBinds(NodeList roleBindNodes, PluginConfig pluginConfig)
//            throws XPathExpressionException {
//        List<RoleBind> roleBinds = new ArrayList<RoleBind>();
//        for (int i = 0; i < roleBindNodes.getLength(); i++) {
//            Node roleBindNode = roleBindNodes.item(i);
//            RoleBind roleBind = new RoleBind();
//            roleBind.setPermission(getStringAttribute(roleBindNode, "./@permission"));
//            roleBind.setRoleName(getStringAttribute(roleBindNode, "./@roleName"));
//
//            roleBinds.add(roleBind);
//        }
//
//        return roleBinds;
//    }
//
//    private List<PluginConfigInterface> parsePluginConfigInterfaces(NodeList interfaceNodeList,
//            PluginConfig pluginConfig) throws XPathExpressionException {
//        List<PluginConfigInterface> pluginConfigInterfaces = new ArrayList<>();
//
//        for (int i = 0; i < interfaceNodeList.getLength(); i++) {
//            Node interfaceNode = interfaceNodeList.item(i);
//
//            PluginConfigInterface pluginConfigInterface = new PluginConfigInterface();
//            pluginConfigInterface.setPluginConfig(pluginConfig);
//            pluginConfigInterfaces.add(pluginConfigInterface);
//
//            pluginConfigInterface.setAction(
//                    ensureNotNull(trim(xPathEvaluator.getString("./@action", interfaceNode)), "Plugin interface name"));
//            String serviceName = pluginConfigInterface.generateServiceName();
//
//            pluginConfigInterface.setServiceName(serviceName);
//            pluginConfigInterface.setServiceDisplayName(serviceName);
//            pluginConfigInterface.setPath(getStringAttribute(interfaceNode, "./@path"));
//            pluginConfigInterface.setHttpMethod(getStringAttribute(interfaceNode, "./@httpMethod"));
//            String isAsyncProcessing = getStringAttribute(interfaceNode, "./@isAsyncProcessing");
//            if (StringUtils.isNotEmpty(isAsyncProcessing)) {
//                pluginConfigInterface.setIsAsyncProcessing(isAsyncProcessing);
//            } else {
//                pluginConfigInterface.setIsAsyncProcessing(PluginConfigInterface.DEFAULT_IS_ASYNC_PROCESSING_VALUE);
//            }
//            String filterRule = getStringAttribute(interfaceNode, "./@filterRule");
//
//            if (StringUtils.isNotBlank(filterRule)) {
//                pluginConfigInterface.setFilterRule(filterRule);
//            } else {
//                pluginConfigInterface.setFilterRule(DEFAULT_FILTER_RULE_FOR_INTERFACE);
//            }
//
//            String interfaceType = getStringAttribute(interfaceNode, "./@type");
//            if (StringUtils.isNotEmpty(interfaceType)) {
//                pluginConfigInterface.setType(interfaceType);
//            } else {
//                pluginConfigInterface.setType(PluginConfigInterface.DEFAULT_INTERFACE_TYPE);
//            }
//
//            NodeList inputParameterNodeList = xPathEvaluator.getNodeList("./inputParameters/parameter", interfaceNode);
//            if (inputParameterNodeList != null && inputParameterNodeList.getLength() > 0) {
//                pluginConfigInterface.setInputParameters(parsePluginConfigInterfaceParameters(inputParameterNodeList,
//                        pluginConfigInterface, TYPE_INPUT, MAPPING_TYPE_CMDB_CI_TYPE));
//            }
//            NodeList outputParameterNodeList = xPathEvaluator.getNodeList("./outputParameters/parameter",
//                    interfaceNode);
//            if (outputParameterNodeList != null && outputParameterNodeList.getLength() > 0) {
//                pluginConfigInterface.setOutputParameters(parsePluginConfigInterfaceParameters(outputParameterNodeList,
//                        pluginConfigInterface, TYPE_OUTPUT, MAPPING_TYPE_NOT_AVAILABLE));
//            }
//        }
//
//        return pluginConfigInterfaces;
//    }
//
//    private Set<PluginConfigInterfaceParameter> parsePluginConfigInterfaceParameters(NodeList parameterNodeList,
//            PluginConfigInterface pluginConfigInterface, String parameterType, String mappingType)
//            throws XPathExpressionException {
//        Set<PluginConfigInterfaceParameter> pluginConfigInterfaceParameters = new LinkedHashSet<>();
//        for (int i = 0; i < parameterNodeList.getLength(); i++) {
//            Node parameterNode = parameterNodeList.item(i);
//
//            PluginConfigInterfaceParameter pluginConfigInterfaceParameter = new PluginConfigInterfaceParameter();
//            pluginConfigInterfaceParameter.setPluginConfigInterface(pluginConfigInterface);
//            pluginConfigInterfaceParameter.setType(parameterType);
//            pluginConfigInterfaceParameter.setMappingType(mappingType);
//
//            pluginConfigInterfaceParameter.setName(trim(parameterNode.getTextContent()));
//            pluginConfigInterfaceParameter.setDataType(getStringAttribute(parameterNode, "./@datatype"));
//            pluginConfigInterfaceParameter.setMappingType(getStringAttribute(parameterNode, "./@mappingType"));
//            String mappingSystemVariableNameString = getStringAttribute(parameterNode, "./@mappingSystemVariableName");
//            if (StringUtils.isNotEmpty(mappingSystemVariableNameString)) {
//                pluginConfigInterfaceParameter.setMappingSystemVariableName(mappingSystemVariableNameString);
//            }
//            String mappingEntityExpression = getStringAttribute(parameterNode, "./@mappingEntityExpression");
//            if (StringUtils.isNotEmpty(mappingEntityExpression)) {
//                pluginConfigInterfaceParameter.setMappingEntityExpression(mappingEntityExpression);
//            }
//            String required = getStringAttribute(parameterNode, "./@required");
//            if (StringUtils.isNoneBlank(required)) {
//                pluginConfigInterfaceParameter.setRequired(required);
//            } else {
//                pluginConfigInterfaceParameter.setRequired(DEFAULT_REQUIRED);
//            }
//
//            String sensitiveData = getStringAttribute(parameterNode, "./@sensitiveData");
//            if (StringUtils.isNoneBlank(sensitiveData)) {
//                pluginConfigInterfaceParameter.setSensitiveData(sensitiveData);
//            } else {
//                pluginConfigInterfaceParameter.setSensitiveData(DEFAULT_SENSITIVE_DATA);
//            }
//
//            pluginConfigInterfaceParameters.add(pluginConfigInterfaceParameter);
//        }
//        return pluginConfigInterfaceParameters;
//    }
//
//    private String ensureNotNull(String name, String description) {
//        name = trim(name);
//        if (name == null)
//            throw new WecubeCoreException(description + " is required.");
//        if (name.contains(SEPARATOR_OF_NAMES)){
//            String msg = String.format("Illegal character[%s] detected in %s[%s]", SEPARATOR_OF_NAMES, description, name);
//            throw new WecubeCoreException("3300", msg, SEPARATOR_OF_NAMES, description, name);
//        }
//        return name;
//    }
//}
