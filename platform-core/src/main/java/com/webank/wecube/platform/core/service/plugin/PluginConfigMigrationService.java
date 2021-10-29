package com.webank.wecube.platform.core.service.plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectMetaMapper;
import com.webank.wecube.platform.core.repository.plugin.CoreObjectPropertyMetaMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.service.plugin.xmltype.ParamObjectType;
import com.webank.wecube.platform.core.service.plugin.xmltype.ParamObjectsType;
import com.webank.wecube.platform.core.service.plugin.xmltype.ParamPropertyType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInputParameterType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInputParametersType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInterfaceType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigOutputParameterType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigOutputParametersType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigsType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginPackageType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginRoleBindingType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginRoleBindingsType;
import com.webank.wecube.platform.core.service.plugin.xmltype.SystemParameterType;
import com.webank.wecube.platform.core.service.plugin.xmltype.SystemParametersType;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * 
 * @author gavin
 *
 */
@Service
public class PluginConfigMigrationService {
    private static final Logger log = LoggerFactory.getLogger(PluginConfigMigrationService.class);
    public static final String DEFAULT_TARGET_ENTITY_FILTER_RULE = "";
    public static final String DEFAULT_FILTER_RULE_FOR_INTERFACE = "";
    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;
    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;
    @Autowired
    private PluginConfigRolesMapper pluginConfigRolesMapper;
    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;
    @Autowired
    private PluginConfigInterfaceParametersMapper pluginConfigInterfaceParametersMapper;
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    protected CoreObjectMetaMapper coreObjectMetaMapper;

    @Autowired
    protected CoreObjectPropertyMetaMapper coreObjectPropertyMetaMapper;

    @Autowired
    private PluginParamObjectMetaRegister pluginParamObjectSupportService;

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    public PluginRegistryInfo exportPluginRegistersForOnePackage(String pluginPackageId) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("3226", "Plugin package ID cannot be blank.");
        }

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3227", "Bad plugin package ID,such package does not exist.");
        }

        log.info("start to export plugin package registry,{} {} {}", pluginPackageId, pluginPackageEntity.getName(),
                pluginPackageEntity.getVersion());

        PluginPackageType xmlPluginPackage = new PluginPackageType();
        xmlPluginPackage.setName(pluginPackageEntity.getName());
        xmlPluginPackage.setVersion(pluginPackageEntity.getVersion());

        List<PluginConfigs> pluginConfigEntities = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackageId);

        if (pluginConfigEntities == null) {
            log.info("Such package ID has no plugin configs.PluginPackageId={}", pluginPackageId);
        } else {

            PluginConfigsType xmlPluginConfigs = buildXmlPluginConfigs(pluginPackageEntity, pluginConfigEntities);
            xmlPluginPackage.setPlugins(xmlPluginConfigs);
        }

        SystemParametersType xmlSystemVariables = buildSystemParametersType(pluginPackageEntity);
        xmlPluginPackage.setSystemParameters(xmlSystemVariables);

        String xmlContent = JaxbUtils.convertToXml(xmlPluginPackage);

        if (log.isDebugEnabled()) {
            log.debug("EXPORT:{}", xmlContent);
        }

        String comments = buildXmlComments(pluginPackageEntity);

        PluginRegistryInfo prInfo = new PluginRegistryInfo();
        prInfo.setPluginPackageData(xmlContent + comments);
        prInfo.setPluginPackageName(pluginPackageEntity.getName());
        prInfo.setPluginPackageVersion(pluginPackageEntity.getVersion());

        return prInfo;
    }

    /**
     * 
     * @param pluginPackageId
     * @param registersAsXml
     */
    @Transactional
    public void importPluginRegistersForOnePackage(String pluginPackageId, String registersAsXml) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("3229", "Plugin package ID cannot be blank.");
        }

        if (StringUtils.isBlank(registersAsXml)) {
            throw new WecubeCoreException("3230", "XML data is blank.");
        }

        if (log.isDebugEnabled()) {
            log.debug("IMPORT:{}", registersAsXml);
        }

        PluginPackages pluginPackage = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackage == null) {
            throw new WecubeCoreException("3231", "Bad plugin package ID,such package does not exist.");
        }

        PluginPackageType xmlPluginPackage = JaxbUtils.convertToObject(registersAsXml, PluginPackageType.class);

        if (xmlPluginPackage == null) {
            throw new WecubeCoreException("3232", "Bad xml contents.");
        }

        String xmlPackageName = xmlPluginPackage.getName();
        String packageName = pluginPackage.getName();
        if (!packageName.equals(xmlPackageName)) {
            throw new WecubeCoreException("3312",
                    String.format(
                            "Plugin packages do not match.The name from XML is %s but the package you chose is %s.",
                            xmlPackageName, packageName),
                    xmlPackageName, packageName);
        }

        performImportPluginRegistersForOnePackage(pluginPackage, xmlPluginPackage);
        performImportSystemParametersForOnePackage(pluginPackage, xmlPluginPackage);
    }

    private SystemParametersType buildSystemParametersType(PluginPackages pluginPackage) {
        SystemParametersType xmlSystemParameters = new SystemParametersType();
        List<SystemVariables> sysVars = this.systemVariablesMapper
                .selectAllBySource(PluginPackages.buildSystemVariableSource(pluginPackage));
        if (sysVars == null || sysVars.isEmpty()) {
            return xmlSystemParameters;
        }

        for (SystemVariables sysVar : sysVars) {
            SystemParameterType xmlSysVar = new SystemParameterType();
            xmlSysVar.setDefaultValue(sysVar.getDefaultValue());
            xmlSysVar.setName(sysVar.getName());
            xmlSysVar.setPackageName(sysVar.getPackageName());
            xmlSysVar.setScopeType(sysVar.getScope());
            xmlSysVar.setSource(sysVar.getSource());
            xmlSysVar.setStatus(sysVar.getStatus());
            xmlSysVar.setValue(sysVar.getValue());

            xmlSystemParameters.getSystemParameter().add(xmlSysVar);
        }

        return xmlSystemParameters;
    }

    private PluginConfigsType buildXmlPluginConfigs(PluginPackages pluginPackage, List<PluginConfigs> pluginConfigs) {
        PluginConfigsType xmlPluginConfigs = new PluginConfigsType();
        for (PluginConfigs pluginConfig : pluginConfigs) {
            if (StringUtils.isBlank(pluginConfig.getRegisterName())) {
                continue;
            }

            PluginConfigType xmlPluginConfig = buildXmlPluginConfig(pluginPackage, pluginConfig);
            xmlPluginConfigs.getPlugin().add(xmlPluginConfig);
        }

        return xmlPluginConfigs;
    }

    private PluginConfigType buildXmlPluginConfig(PluginPackages pluginPackage, PluginConfigs pluginConfig) {
        PluginConfigType xmlPluginConfig = new PluginConfigType();
        xmlPluginConfig.setName(pluginConfig.getName());
        xmlPluginConfig.setRegisterName(pluginConfig.getRegisterName());
        xmlPluginConfig.setStatus(pluginConfig.getStatus());
        xmlPluginConfig.setTargetEntity(pluginConfig.getTargetEntity());
        xmlPluginConfig.setTargetEntityFilterRule(pluginConfig.getTargetEntityFilterRule());
        xmlPluginConfig.setTargetPackage(pluginConfig.getTargetPackage());

        List<PluginConfigInterfaces> intfs = pluginConfigInterfacesMapper.selectAllByPluginConfig(pluginConfig.getId());
        if (intfs != null) {
            for (PluginConfigInterfaces intf : intfs) {
                PluginConfigInterfaceType xmlIntf = buildXmlPluginConfigInterface(pluginPackage, pluginConfig, intf);
                xmlPluginConfig.getPluginInterface().add(xmlIntf);
            }
        }

        PluginRoleBindingsType xmlRoleBinds = buildXmlPluginRoleBindingsType(pluginConfig);
        xmlPluginConfig.setRoleBinds(xmlRoleBinds);

        List<CoreObjectMeta> objectMetas = coreObjectMetaMapper.selectAllByConfig(pluginConfig.getId());
        if (objectMetas != null && !objectMetas.isEmpty()) {
            List<ParamObjectType> xmlParamObjectTypes = new ArrayList<>();
            for (CoreObjectMeta objectMeta : objectMetas) {
                ParamObjectType xmlParamObjectType = buildXmlParamObjectMetaType(pluginPackage, pluginConfig,
                        objectMeta);
                xmlParamObjectTypes.add(xmlParamObjectType);
            }

            xmlPluginConfig.setParamObject(xmlParamObjectTypes);
        }

        return xmlPluginConfig;

    }

    private ParamObjectType buildXmlParamObjectMetaType(PluginPackages pluginPackage, PluginConfigs pluginConfig,
            CoreObjectMeta objectMeta) {
        ParamObjectType xmlParamObjectType = new ParamObjectType();
        xmlParamObjectType.setLatestSource(objectMeta.getLatestSource());
        xmlParamObjectType.setName(objectMeta.getName());
        xmlParamObjectType.setPackageName(objectMeta.getPackageName());
        xmlParamObjectType.setSource(objectMeta.getSource());

        List<CoreObjectPropertyMeta> objectPropertyMetas = coreObjectPropertyMetaMapper
                .selectAllByObjectMeta(objectMeta.getId());
        if (objectPropertyMetas != null) {
            for (CoreObjectPropertyMeta objectPropertyMeta : objectPropertyMetas) {
                ParamPropertyType xmlParamPropertyType = buildXmlParamPropertyType(pluginPackage, pluginConfig,
                        objectMeta, objectPropertyMeta);
                xmlParamObjectType.getProperty().add(xmlParamPropertyType);
            }
        }

        return xmlParamObjectType;

    }

    private ParamPropertyType buildXmlParamPropertyType(PluginPackages pluginPackage, PluginConfigs pluginConfig,
            CoreObjectMeta objectMeta, CoreObjectPropertyMeta objectPropertyMeta) {
        ParamPropertyType xmlParamPropertyType = new ParamPropertyType();

        xmlParamPropertyType.setName(objectPropertyMeta.getName());
        xmlParamPropertyType.setDataType(objectPropertyMeta.getDataType());
        xmlParamPropertyType.setMapExpr(objectPropertyMeta.getMapExpr());
        xmlParamPropertyType.setMapType(objectPropertyMeta.getMapType());
        xmlParamPropertyType.setRefObjectName(objectPropertyMeta.getRefObjectName());
        xmlParamPropertyType.setMultiple(objectPropertyMeta.getMultiple());

        String sensitiveData = null;
        if (objectPropertyMeta.getSensitive() == null) {
            sensitiveData = "N";
        } else {
            sensitiveData = objectPropertyMeta.getSensitive() ? "Y" : "N";
        }
        xmlParamPropertyType.setSensitiveData(sensitiveData);

        return xmlParamPropertyType;
    }

    private PluginRoleBindingsType buildXmlPluginRoleBindingsType(PluginConfigs pluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = new PluginRoleBindingsType();

        List<PluginConfigRoles> authEntities = pluginConfigRolesMapper.selectAllByPluginConfig(pluginConfig.getId());
        if (authEntities == null || authEntities.isEmpty()) {
            return xmlRoleBinds;
        }

        for (PluginConfigRoles entity : authEntities) {
            PluginRoleBindingType xmlRoleBind = new PluginRoleBindingType();
            xmlRoleBind.setPermission(entity.getPermType());
            xmlRoleBind.setRoleName(entity.getRoleName());

            xmlRoleBinds.getRoleBind().add(xmlRoleBind);
        }

        return xmlRoleBinds;
    }

    private PluginConfigInterfaceType buildXmlPluginConfigInterface(PluginPackages pluginPackage,
            PluginConfigs pluginConfig, PluginConfigInterfaces intf) {
        PluginConfigInterfaceType xmlIntf = new PluginConfigInterfaceType();
        xmlIntf.setAction(intf.getAction());
        xmlIntf.setFilterRule(intf.getFilterRule());
        xmlIntf.setHttpMethod(intf.getHttpMethod());
        xmlIntf.setIsAsyncProcessing(intf.getIsAsyncProcessing());
        xmlIntf.setPath(intf.getPath());
        xmlIntf.setType(intf.getType());
        xmlIntf.setDescription(intf.getDescription());

        PluginConfigInputParametersType xmlInputParameters = new PluginConfigInputParametersType();
        xmlIntf.setInputParameters(xmlInputParameters);

        List<PluginConfigInterfaceParameters> inputParameters = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intf.getId(), Constants.TYPE_INPUT);
        if (inputParameters != null) {
            for (PluginConfigInterfaceParameters inputParameter : inputParameters) {
                PluginConfigInputParameterType xmlInputParameter = buildXmlInputParameter(inputParameter);
                xmlInputParameters.getParameter().add(xmlInputParameter);
            }
        }

        PluginConfigOutputParametersType xmlOutputParameters = new PluginConfigOutputParametersType();
        xmlIntf.setOutputParameters(xmlOutputParameters);

        List<PluginConfigInterfaceParameters> outputParameters = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intf.getId(), Constants.TYPE_OUTPUT);
        if (outputParameters != null) {
            for (PluginConfigInterfaceParameters outputParameter : outputParameters) {
                PluginConfigOutputParameterType xmlOutputParameter = buildXmlOutputParameter(outputParameter);
                xmlOutputParameters.getParameter().add(xmlOutputParameter);
            }
        }

        return xmlIntf;
    }

    private PluginConfigOutputParameterType buildXmlOutputParameter(PluginConfigInterfaceParameters outputParameter) {
        PluginConfigOutputParameterType xmlParam = new PluginConfigOutputParameterType();
        xmlParam.setDatatype(outputParameter.getDataType());
        xmlParam.setMappingEntityExpression(outputParameter.getMappingEntityExpression());
        xmlParam.setMappingType(outputParameter.getMappingType());
        xmlParam.setValue(outputParameter.getName());
        xmlParam.setSensitiveData(outputParameter.getSensitiveData());
        xmlParam.setDescription(outputParameter.getDescription());
        xmlParam.setMappingSystemVariableName(outputParameter.getMappingSystemVariableName());
        xmlParam.setMappingValue(outputParameter.getMappingValue());
        xmlParam.setMultiple(outputParameter.getMultiple());
        xmlParam.setRefObjectName(outputParameter.getRefObjectName());

        return xmlParam;
    }

    private PluginConfigInputParameterType buildXmlInputParameter(PluginConfigInterfaceParameters inputParameter) {
        PluginConfigInputParameterType xmlParam = new PluginConfigInputParameterType();
        xmlParam.setDatatype(inputParameter.getDataType());
        xmlParam.setMappingEntityExpression(inputParameter.getMappingEntityExpression());
        xmlParam.setMappingSystemVariableName(inputParameter.getMappingSystemVariableName());
        xmlParam.setMappingType(inputParameter.getMappingType());
        xmlParam.setRequired(inputParameter.getRequired());
        xmlParam.setSensitiveData(inputParameter.getSensitiveData());
        xmlParam.setValue(inputParameter.getName());
        xmlParam.setDescription(inputParameter.getDescription());
        xmlParam.setMappingValue(inputParameter.getMappingValue());

        xmlParam.setMultiple(inputParameter.getMultiple());
        xmlParam.setRefObjectName(inputParameter.getRefObjectName());

        return xmlParam;
    }

    private List<PluginConfigInputParameterType> getXmlInputParameters(PluginConfigInterfaceType xmlIntf) {
        if (xmlIntf.getInputParameters() == null) {
            return new ArrayList<>();
        }

        return xmlIntf.getInputParameters().getParameter();
    }

    private List<PluginConfigOutputParameterType> getXmlOutputParameters(PluginConfigInterfaceType xmlIntf) {
        if (xmlIntf.getOutputParameters() == null) {
            return new ArrayList<>();
        }

        return xmlIntf.getOutputParameters().getParameter();
    }

    private PluginConfigInterfaces tryUpdatePluginConfigInterface(PluginPackages pluginPackage,
            PluginConfigs existPluginConfig, PluginConfigInterfaces toUpdateIntf, PluginConfigInterfaceType xmlIntf) {
        if (xmlIntf == null) {
            return toUpdateIntf;
        }

        toUpdateIntf.setFilterRule(xmlIntf.getFilterRule());

        List<PluginConfigInputParameterType> xmlInputParameters = getXmlInputParameters(xmlIntf);

        List<PluginConfigInterfaceParameters> inputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(toUpdateIntf.getId(), Constants.TYPE_INPUT);

        for(PluginConfigInterfaceParameters inputParam : inputParameters) {
            PluginConfigInputParameterType xmlInputParam = tryPickoutXmlInputParam(xmlInputParameters, inputParam.getName());
            if(xmlInputParam != null) {
                tryUpdatePluginConfigInterfaceInputParameter(existPluginConfig, toUpdateIntf, inputParam,
                        xmlInputParam);
            }
        }

        List<PluginConfigOutputParameterType> xmlOutputParameters = getXmlOutputParameters(xmlIntf);
        List<PluginConfigInterfaceParameters> outputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(toUpdateIntf.getId(), Constants.TYPE_OUTPUT);

        
        for(PluginConfigInterfaceParameters outputParam : outputParameters) {
            PluginConfigOutputParameterType xmlOutputParam = tryPickoutXmlOutputParam(xmlOutputParameters, outputParam.getName());
            if(xmlOutputParam != null) {
                tryUpdatePluginConfigInterfaceOutputParameter(existPluginConfig, toUpdateIntf, outputParam,
                        xmlOutputParam);
            }
        }

        toUpdateIntf.setServiceDisplayName(toUpdateIntf.generateServiceName(pluginPackage, existPluginConfig));
        toUpdateIntf.setServiceName(toUpdateIntf.generateServiceName(pluginPackage, existPluginConfig));

        pluginConfigInterfacesMapper.updateByPrimaryKeySelective(toUpdateIntf);

        return toUpdateIntf;
    }

    private PluginConfigInterfaceParameters tryUpdatePluginConfigInterfaceInputParameter(
            PluginConfigs existPluginConfig, PluginConfigInterfaces intf, PluginConfigInterfaceParameters param,
            PluginConfigInputParameterType xmlParam) {
        if (xmlParam == null) {
            return param;
        }
        param.setMappingType(xmlParam.getMappingType());
        param.setSensitiveData(xmlParam.getSensitiveData());
        param.setMappingEntityExpression(xmlParam.getMappingEntityExpression());
        param.setMappingSystemVariableName(xmlParam.getMappingSystemVariableName());
        param.setMappingValue(xmlParam.getMappingValue());

        pluginConfigInterfaceParametersMapper.updateByPrimaryKeySelective(param);

        return param;
    }

    private PluginConfigInterfaceParameters tryUpdatePluginConfigInterfaceOutputParameter(
            PluginConfigs existPluginConfig, PluginConfigInterfaces intf, PluginConfigInterfaceParameters param,
            PluginConfigOutputParameterType xmlParam) {
        if (xmlParam == null) {
            return param;
        }
        param.setMappingType(xmlParam.getMappingType());
        param.setSensitiveData(xmlParam.getSensitiveData());
        param.setMappingEntityExpression(xmlParam.getMappingEntityExpression());
        param.setMappingSystemVariableName(xmlParam.getMappingSystemVariableName());
        param.setMappingValue(xmlParam.getMappingValue());

        pluginConfigInterfaceParametersMapper.updateByPrimaryKeySelective(param);
        return param;

    }

    private PluginConfigs tryCreatePluginConfig(PluginPackages pluginPackage, PluginConfigType xmlPluginConfig,
            PluginConfigs pluginConfigDef) {
        PluginConfigs pluginConfig = new PluginConfigs();
        pluginConfig.setId(LocalIdGenerator.generateId());
        pluginConfig.setName(xmlPluginConfig.getName());
        pluginConfig.setRegisterName(xmlPluginConfig.getRegisterName());
        pluginConfig.setStatus(PluginConfigs.DISABLED);
        pluginConfig.setTargetEntity(xmlPluginConfig.getTargetEntity());
        pluginConfig.setTargetEntityFilterRule(xmlPluginConfig.getTargetEntityFilterRule());
        pluginConfig.setTargetPackage(xmlPluginConfig.getTargetPackage());
        pluginConfig.setPluginPackageId(pluginPackage.getId());

        pluginConfigsMapper.insert(pluginConfig);

        List<PluginConfigInterfaceType> xmlPluginInterfaceList = xmlPluginConfig.getPluginInterface();
        List<PluginConfigInterfaces> interfDefs = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigDef.getId());
        List<PluginConfigInterfaces> createdInterfaces = new ArrayList<PluginConfigInterfaces>();

        if (interfDefs == null || interfDefs.isEmpty()) {
            return pluginConfig;
        }

        for (PluginConfigInterfaces interfDef : interfDefs) {
            if (StringUtils.isBlank(interfDef.getAction())) {
                log.info("The action is blank for {} {}", interfDef.getId(), interfDef.getPath());
                continue;
            }

            Map<String, PluginConfigInterfaceType> actionAndXmlInterfs = pickoutPluginConfigInterfaceTypeByPath(
                    xmlPluginInterfaceList, interfDef.getPath());
            if (actionAndXmlInterfs.isEmpty()) {
                PluginConfigInterfaces intf = tryCreatePluginConfigInterface(pluginPackage, pluginConfig, null,
                        interfDef);

                if (intf != null) {
                    createdInterfaces.add(intf);
                }
            } else {
                for (PluginConfigInterfaceType xmlIntf : actionAndXmlInterfs.values()) {
                    PluginConfigInterfaces intf = tryCreatePluginConfigInterface(pluginPackage, pluginConfig, xmlIntf,
                            interfDef);

                    if (intf != null) {
                        createdInterfaces.add(intf);
                    }
                }
            }

        }

        pluginConfig.setInterfaces(createdInterfaces);

        tryCreatePluginConfigRoleBinds(xmlPluginConfig, pluginConfig);

        // #2109
        tryCreateObjectMetas(xmlPluginConfig, pluginConfig);
        return pluginConfig;

    }

    private void tryCreateObjectMetas(PluginConfigType xmlPluginConfig, PluginConfigs savedPluginConfig) {
        List<ParamObjectType> xmlParamObjects = xmlPluginConfig.getParamObject();
        if (xmlParamObjects == null || xmlParamObjects.isEmpty()) {
            return;
        }

        for (ParamObjectType xmlParamObjectType : xmlParamObjects) {
            CoreObjectMeta objectMeta = new CoreObjectMeta();
            objectMeta.setId(LocalIdGenerator.generateId());
            objectMeta.setConfigId(savedPluginConfig.getId());
            objectMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            objectMeta.setCreatedTime(new Date());
            objectMeta.setLatestSource(xmlParamObjectType.getLatestSource());
            objectMeta.setName(xmlParamObjectType.getName());
            objectMeta.setPackageName(xmlParamObjectType.getPackageName());
            objectMeta.setSource(xmlParamObjectType.getSource());

            coreObjectMetaMapper.insert(objectMeta);

            tryCreateObjectPropertyMetas(xmlPluginConfig, savedPluginConfig, xmlParamObjectType, objectMeta);

        }
    }

    private void tryCreateObjectPropertyMetas(PluginConfigType xmlPluginConfig, PluginConfigs savedPluginConfig,
            ParamObjectType xmlParamObjectType, CoreObjectMeta objectMeta) {
        List<ParamPropertyType> xmlPropertyMetas = xmlParamObjectType.getProperty();
        if (xmlPropertyMetas == null || xmlPropertyMetas.isEmpty()) {
            return;
        }

        for (ParamPropertyType xmlPropertyMeta : xmlPropertyMetas) {
            CoreObjectPropertyMeta propertyMeta = new CoreObjectPropertyMeta();
            propertyMeta.setConfigId(savedPluginConfig.getId());
            propertyMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            propertyMeta.setCreatedTime(new Date());
            propertyMeta.setDataType(xmlPropertyMeta.getDataType());
            propertyMeta.setId(LocalIdGenerator.generateId());
            propertyMeta.setMapExpr(xmlPropertyMeta.getMapExpr());
            propertyMeta.setMapType(xmlPropertyMeta.getMapType());
            propertyMeta.setName(xmlPropertyMeta.getName());
            propertyMeta.setObjectName(objectMeta.getName());
            propertyMeta.setObjectMetaId(objectMeta.getId());
            propertyMeta.setPackageName(objectMeta.getPackageName());
            propertyMeta.setRefObjectName(xmlPropertyMeta.getRefObjectName());
            propertyMeta.setMultiple(xmlPropertyMeta.getMultiple());
            boolean sensitive = false;
            if (Constants.DATA_SENSITIVE.equalsIgnoreCase(xmlPropertyMeta.getSensitiveData())) {
                sensitive = true;
            }
            propertyMeta.setSensitive(sensitive);
            propertyMeta.setSource(objectMeta.getSource());

            coreObjectPropertyMetaMapper.insert(propertyMeta);

        }

    }

    private RoleDto fetchRoleWithRoleName(String roleName) {
        RoleDto role = null;
        try {
            role = userManagementService.retrieveRoleByRoleName(roleName);
        } catch (Exception e) {
            log.warn("errors while fetch role with role name:{}", roleName);
            role = null;
        }

        return role;
    }

    private void tryCreatePluginConfigRoleBinds(PluginConfigType xmlPluginConfig, PluginConfigs savedPluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = xmlPluginConfig.getRoleBinds();
        if (xmlRoleBinds == null || xmlRoleBinds.getRoleBind().isEmpty()) {
            return;
        }

        for (PluginRoleBindingType xmlRoleBind : xmlRoleBinds.getRoleBind()) {
            PluginConfigRoles entity = new PluginConfigRoles();
            entity.setId(LocalIdGenerator.generateId());
            entity.setIsActive(true);
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());
            entity.setPermType(xmlRoleBind.getPermission());
            entity.setPluginCfgId(savedPluginConfig.getId());
            RoleDto roleDto = fetchRoleWithRoleName(xmlRoleBind.getRoleName());
            if (roleDto != null) {
                entity.setRoleId(roleDto.getId());
            }
            entity.setRoleName(xmlRoleBind.getRoleName());

            pluginConfigRolesMapper.insert(entity);
        }
    }

    private void performImportSystemParametersForOnePackage(PluginPackages pluginPackage,
            PluginPackageType xmlPluginPackage) {
        SystemParametersType xmlSystemParameters = xmlPluginPackage.getSystemParameters();
        if (xmlSystemParameters == null || xmlSystemParameters.getSystemParameter() == null
                || xmlSystemParameters.getSystemParameter().isEmpty()) {
            return;
        }

        for (SystemParameterType xmlSysVar : xmlSystemParameters.getSystemParameter()) {
            if (StringUtils.isBlank(xmlSysVar.getName())) {
                log.info("System variable name is blank:{} ", xmlSysVar);
                continue;
            }

            if (StringUtils.isBlank(xmlSysVar.getScopeType())) {
                log.info("System variable scope is blank:{}", xmlSysVar);
                continue;
            }

            SystemVariables sysVarEntity = null;
            List<SystemVariables> existSysVars = systemVariablesMapper.selectAllByNameAndScopeAndSource(
                    xmlSysVar.getName(), xmlSysVar.getScopeType(),
                    PluginPackages.buildSystemVariableSource(pluginPackage));
            if (existSysVars == null || existSysVars.isEmpty()) {
                existSysVars = systemVariablesMapper.selectAllByNameAndScopeAndSource(xmlSysVar.getName(),
                        xmlSysVar.getScopeType(), pluginPackage.getId());
            }

            if (existSysVars != null && !existSysVars.isEmpty()) {
                sysVarEntity = existSysVars.get(0);
            }

            if (sysVarEntity == null) {
                sysVarEntity = new SystemVariables();
                sysVarEntity.setId(LocalIdGenerator.generateId());
                sysVarEntity.setDefaultValue(xmlSysVar.getDefaultValue());
                sysVarEntity.setPackageName(xmlSysVar.getPackageName());
                sysVarEntity.setName(xmlSysVar.getName());
                sysVarEntity.setScope(xmlSysVar.getScopeType());
                sysVarEntity.setSource(PluginPackages.buildSystemVariableSource(pluginPackage));
                sysVarEntity.setStatus(xmlSysVar.getStatus());
                sysVarEntity.setValue(xmlSysVar.getValue());

                systemVariablesMapper.insert(sysVarEntity);
            } else {
                sysVarEntity.setDefaultValue(xmlSysVar.getDefaultValue());
                sysVarEntity.setValue(xmlSysVar.getValue());
                sysVarEntity.setStatus(xmlSysVar.getStatus());

                systemVariablesMapper.updateByPrimaryKeySelective(sysVarEntity);
            }

        }
    }

    private void performImportPluginRegistersForOnePackage(PluginPackages pluginPackage,
            PluginPackageType xmlPluginPackage) {
        log.info("start to import plugin registries for {} {} from {} {}", pluginPackage.getName(),
                pluginPackage.getVersion(), xmlPluginPackage.getName(), xmlPluginPackage.getVersion());

        PluginConfigsType xmlPlugins = xmlPluginPackage.getPlugins();
        if (xmlPlugins == null) {
            return;
        }

        List<PluginConfigType> xmlPluginConfigList = xmlPlugins.getPlugin();
        if (xmlPluginConfigList == null || xmlPluginConfigList.isEmpty()) {
            return;
        }

        Map<String, PluginConfigs> nameAndPluginConfigDefs = pickoutPluginConfigDefinitions(pluginPackage);
        if (log.isDebugEnabled()) {
            log.debug("total {} plugin config declarations found.", nameAndPluginConfigDefs.size());
        }

        // try to update plugin config definition here
        String xmlPackageVersion = xmlPluginPackage.getVersion();
        if (pluginPackage.getVersion().equalsIgnoreCase(xmlPackageVersion)) {
            log.info("The version from XML matches and try to update plugin configuration definition for {} {} {}",
                    pluginPackage.getId(), pluginPackage.getName(), pluginPackage.getVersion());

            tryCreateOrUpdatePluginConfigDefinitions(pluginPackage, xmlPluginPackage, nameAndPluginConfigDefs,
                    xmlPluginConfigList);
        }

        for (PluginConfigType xmlPluginConfig : xmlPluginConfigList) {
            if (StringUtils.isBlank(xmlPluginConfig.getName())) {
                throw new WecubeCoreException("3233", "Plugin config name cannot be blank.");
            }

            if (StringUtils.isBlank(xmlPluginConfig.getRegisterName())) {
                String msg = String.format("Register name is blank for %s and ignored", xmlPluginConfig.getName());
                log.info(msg);
                continue;
            }

            PluginConfigs pluginConfigDef = nameAndPluginConfigDefs.get(xmlPluginConfig.getName());
            handlePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        pluginPackagesMapper.updateByPrimaryKeySelective(pluginPackage);

        log.info("finished importing plugin registries for {} {} from {} {}", pluginPackage.getName(),
                pluginPackage.getVersion(), xmlPluginPackage.getName(), xmlPluginPackage.getVersion());
    }

    private void tryProcessParamObjectDefinitions(PluginPackages pluginPackage, PluginPackageType xmlPluginPackage,
            String configId) {
        ParamObjectsType xmlParamObjects = xmlPluginPackage.getParamObjects();
        if (xmlParamObjects == null) {
            return;
        }

        List<ParamObjectType> xmlParamObjectList = xmlParamObjects.getParamObject();
        if (xmlParamObjectList == null || xmlParamObjectList.isEmpty()) {
            return;
        }

        com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType regParamObjectsType = convertRegParamObjectsType(
                xmlParamObjects);
        pluginParamObjectSupportService.registerParamObjects(regParamObjectsType, pluginPackage.getName(),
                pluginPackage.getVersion(), configId);
    }

    private com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType convertRegParamObjectsType(
            ParamObjectsType xmlParamObjects) {
        com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType regParamObjectsType = new com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType();

        List<ParamObjectType> xmlParamObjectList = xmlParamObjects.getParamObject();
        for (ParamObjectType xmlParamObject : xmlParamObjectList) {
            com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType regParamObjectType = convertParamObjectType(
                    xmlParamObject);
            regParamObjectsType.getParamObject().add(regParamObjectType);
        }

        return regParamObjectsType;
    }

    private com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType convertParamObjectType(
            ParamObjectType xmlParamObject) {
        com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType regParamObjectType = new com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectType();
        regParamObjectType.setName(xmlParamObject.getName());
        regParamObjectType.setMapExpr(xmlParamObject.getMapExpr());

        for (ParamPropertyType xmlProperty : xmlParamObject.getProperty()) {
            com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType regProperty = convertParamPropertyType(
                    xmlProperty);
            regParamObjectType.getProperty().add(regProperty);
        }

        return regParamObjectType;
    }

    private com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType convertParamPropertyType(
            ParamPropertyType xmlProperty) {
        com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType regProperty = new com.webank.wecube.platform.core.service.plugin.xml.register.ParamPropertyType();
        regProperty.setDataType(xmlProperty.getDataType());
        regProperty.setMapExpr(xmlProperty.getMapExpr());
        String mapType = xmlProperty.getMapType();
        if (StringUtils.isBlank(mapType)) {
            mapType = Constants.MAPPING_TYPE_CONSTANT;
        }
        regProperty.setMapType(mapType);
        regProperty.setMultiple(xmlProperty.getMultiple());
        regProperty.setName(xmlProperty.getName());
        regProperty.setRefObjectName(xmlProperty.getRefObjectName());
        regProperty.setSensitiveData(xmlProperty.getSensitiveData());
        regProperty.setValue(xmlProperty.getValue());

        return regProperty;
    }

    private void tryCreateOrUpdatePluginConfigDefinitions(PluginPackages pluginPackage,
            PluginPackageType xmlPluginPackage, Map<String, PluginConfigs> nameAndPluginConfigDefs,
            List<PluginConfigType> xmlPluginConfigList) {
        List<PluginConfigType> xmlPluginConfigDefs = new ArrayList<>();
        for (PluginConfigType xmlPluginConfig : xmlPluginConfigList) {
            if (StringUtils.isBlank(xmlPluginConfig.getName())) {
                continue;
            }
            if (StringUtils.isBlank(xmlPluginConfig.getRegisterName())) {
                xmlPluginConfigDefs.add(xmlPluginConfig);
            }
        }

        if (xmlPluginConfigDefs.isEmpty()) {
            log.info("There is not XML plugin configuration definition for {}", pluginPackage.getName());
            return;
        }

        log.info("Total {} XML plugin configuration definitions found for {}", xmlPluginConfigDefs.size(),
                pluginPackage.getName());

        for (PluginConfigType xmlPluginConfigDef : xmlPluginConfigDefs) {
            PluginConfigs dbPluginConfigDef = nameAndPluginConfigDefs.get(xmlPluginConfigDef.getName());
            if (dbPluginConfigDef == null) {
                dbPluginConfigDef = tryCreateSinglePluginConfigDefinition(pluginPackage, xmlPluginPackage,
                        xmlPluginConfigDef);
                nameAndPluginConfigDefs.put(dbPluginConfigDef.getName(), dbPluginConfigDef);
            } else {

                tryUpdateSinglePluginConfigDefinition(pluginPackage, xmlPluginPackage, dbPluginConfigDef,
                        xmlPluginConfigDef);
            }

            tryProcessParamObjectDefinitions(pluginPackage, xmlPluginPackage, dbPluginConfigDef.getId());
        }
    }

    private PluginConfigs tryCreateSinglePluginConfigDefinition(PluginPackages pluginPackage,
            PluginPackageType xmlPluginPackage, PluginConfigType xmlPluginConfigDef) {

        PluginConfigs pluginConfig = new PluginConfigs();
        pluginConfig.setId(LocalIdGenerator.generateId());
        pluginConfig.setName(xmlPluginConfigDef.getName());
        pluginConfig.setRegisterName(xmlPluginConfigDef.getRegisterName());
        pluginConfig.setStatus(PluginConfigs.ENABLED);
        pluginConfig.setTargetEntity(xmlPluginConfigDef.getTargetEntity());
        pluginConfig.setTargetEntityFilterRule(xmlPluginConfigDef.getTargetEntityFilterRule());
        pluginConfig.setTargetPackage(xmlPluginConfigDef.getTargetPackage());
        pluginConfig.setPluginPackageId(pluginPackage.getId());

        pluginConfigsMapper.insert(pluginConfig);

        List<PluginConfigInterfaceType> xmlPluginInterfaceList = xmlPluginConfigDef.getPluginInterface();

        List<PluginConfigInterfaces> createdInterfaces = new ArrayList<PluginConfigInterfaces>();

        if (xmlPluginInterfaceList == null || xmlPluginInterfaceList.isEmpty()) {
            return pluginConfig;
        }

        for (PluginConfigInterfaceType xmlDefIntf : xmlPluginInterfaceList) {
            if (StringUtils.isBlank(xmlDefIntf.getAction())) {
                log.info("The action is blank for {}:{}", xmlPluginConfigDef.getName(), xmlDefIntf.getPath());
                continue;
            }

            PluginConfigInterfaces intf = tryCreatePluginConfigInterfaceDefinition(pluginPackage, pluginConfig,
                    xmlDefIntf);
            createdInterfaces.add(intf);
        }

        pluginConfig.setInterfaces(createdInterfaces);

//        tryCreatePluginConfigDefinitionRoleBinds(xmlPluginConfigDef, pluginConfig);

        return pluginConfig;
    }

    private PluginConfigInterfaces tryCreatePluginConfigInterfaceDefinition(PluginPackages pluginPackage,
            PluginConfigs pluginConfig, PluginConfigInterfaceType xmlIntf) {
        PluginConfigInterfaces intf = new PluginConfigInterfaces();
        intf.setId(LocalIdGenerator.generateId());
        intf.setPluginConfigId(pluginConfig.getId());
        intf.setPath(xmlIntf.getPath());

        String filterRule = xmlIntf.getFilterRule();
        if (StringUtils.isBlank(filterRule)) {
            filterRule = DEFAULT_FILTER_RULE_FOR_INTERFACE;
        }
        intf.setFilterRule(filterRule);

        String httpMethod = xmlIntf.getHttpMethod();
        if (StringUtils.isBlank(httpMethod)) {
            httpMethod = "POST";
        }
        intf.setHttpMethod(httpMethod);
        String asyncProcessing = xmlIntf.getIsAsyncProcessing();
        if (StringUtils.isBlank(asyncProcessing)) {
            asyncProcessing = PluginConfigInterfaces.DEFAULT_IS_ASYNC_PROCESSING_VALUE;
        }
        intf.setIsAsyncProcessing(asyncProcessing);
        intf.setPath(xmlIntf.getPath());

        String interfaceType = xmlIntf.getType();
        if (StringUtils.isBlank(interfaceType)) {
            interfaceType = PluginConfigInterfaces.DEFAULT_INTERFACE_TYPE;
        }

        intf.setAction(xmlIntf.getAction());

        intf.setIsAsyncProcessing(xmlIntf.getIsAsyncProcessing());

        intf.setType(xmlIntf.getType());
        intf.setDescription(xmlIntf.getDescription());

        intf.setServiceDisplayName(intf.generateServiceName(pluginPackage, pluginConfig));
        intf.setServiceName(intf.generateServiceName(pluginPackage, pluginConfig));

        pluginConfigInterfacesMapper.insert(intf);

        PluginConfigInputParametersType xmlInputParametersType = xmlIntf.getInputParameters();
        List<PluginConfigInputParameterType> xmlInputParameters = null;
        if (xmlInputParametersType != null) {
            xmlInputParameters = xmlInputParametersType.getParameter();
        }
        List<PluginConfigInterfaceParameters> inputParameters = new ArrayList<>();
        if (xmlInputParameters != null) {
            for (PluginConfigInputParameterType xmlInputParam : xmlInputParameters) {
                if (StringUtils.isBlank(xmlInputParam.getValue())) {
                    continue;
                }

                PluginConfigInterfaceParameters inputParam = tryCreateInputParameterDefinition(intf, xmlInputParam);

                inputParameters.add(inputParam);
            }
        }

        intf.setInputParameters(inputParameters);

        PluginConfigOutputParametersType xmlOutputParametersType = xmlIntf.getOutputParameters();
        List<PluginConfigOutputParameterType> xmlOutputParameters = null;
        if (xmlOutputParametersType != null) {
            xmlOutputParameters = xmlOutputParametersType.getParameter();
        }
        List<PluginConfigInterfaceParameters> outputParameters = new ArrayList<>();

        if (xmlOutputParameters != null) {
            for (PluginConfigOutputParameterType xmlOutputParam : xmlOutputParameters) {
                if (StringUtils.isBlank(xmlOutputParam.getValue())) {
                    continue;
                }
                PluginConfigInterfaceParameters outputParam = tryCreateOutputParameterDefinition(intf, xmlOutputParam);
                outputParameters.add(outputParam);
            }
        }

        intf.setOutputParameters(outputParameters);

        pluginConfigInterfacesMapper.updateByPrimaryKeySelective(intf);

        return intf;
    }

    private PluginConfigInterfaceParameters tryCreateOutputParameterDefinition(PluginConfigInterfaces intf,
            PluginConfigOutputParameterType xmlOutputParam) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();
        param.setId(LocalIdGenerator.generateId());
        param.setName(xmlOutputParam.getValue());
        param.setDataType(xmlOutputParam.getDatatype());
        param.setType(Constants.TYPE_OUTPUT);
        param.setPluginConfigInterfaceId(intf.getId());
        param.setDescription(xmlOutputParam.getDescription());
        param.setMappingEntityExpression(xmlOutputParam.getMappingEntityExpression());
        param.setMappingType(xmlOutputParam.getMappingType());
        param.setSensitiveData(xmlOutputParam.getSensitiveData());
        param.setMappingSystemVariableName(xmlOutputParam.getMappingSystemVariableName());
        param.setMappingValue(xmlOutputParam.getMappingValue());
        param.setMultiple(xmlOutputParam.getMultiple());
        param.setRefObjectName(xmlOutputParam.getRefObjectName());

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;
    }

    private PluginConfigInterfaceParameters tryCreateInputParameterDefinition(PluginConfigInterfaces intf,
            PluginConfigInputParameterType xmlInputParam) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();

        param.setId(LocalIdGenerator.generateId());
        param.setName(xmlInputParam.getValue());
        param.setDataType(xmlInputParam.getDatatype());
        param.setType(Constants.TYPE_INPUT);
        param.setPluginConfigInterfaceId(intf.getId());
        param.setDescription(xmlInputParam.getDescription());
        param.setMappingEntityExpression(xmlInputParam.getMappingEntityExpression());
        param.setMappingSystemVariableName(xmlInputParam.getMappingSystemVariableName());
        param.setMappingType(xmlInputParam.getMappingType());
        param.setSensitiveData(xmlInputParam.getSensitiveData());
        param.setMappingValue(xmlInputParam.getMappingValue());
        param.setRefObjectName(xmlInputParam.getRefObjectName());
        param.setMultiple(xmlInputParam.getMultiple());

        param.setRequired(xmlInputParam.getRequired());

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;

    }

    private void tryUpdateSinglePluginConfigDefinition(PluginPackages pluginPackage, PluginPackageType xmlPluginPackage,
            PluginConfigs dbPluginConfigDef, PluginConfigType xmlPluginConfigDef) {

        List<PluginConfigInterfaceType> xmlIntfList = xmlPluginConfigDef.getPluginInterface();
        if (xmlIntfList == null || xmlIntfList.isEmpty()) {
            return;
        }

        List<PluginConfigInterfaces> toUpdateInterfaces = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(dbPluginConfigDef.getId());

        for (PluginConfigInterfaceType xmlIntf : xmlIntfList) {
            if (StringUtils.isBlank(xmlIntf.getAction())) {
                throw new WecubeCoreException("3235", "Action of interface cannot be blank.");
            }
            PluginConfigInterfaces toUpdateIntf = pickoutPluginConfigInterface(toUpdateInterfaces, xmlIntf.getAction(),
                    xmlIntf.getPath());
            if (toUpdateIntf == null) {
                log.info("interface doesnot exist and try to create one,{} {}", dbPluginConfigDef.getId(),
                        xmlIntf.getAction());

                toUpdateIntf = tryCreatePluginConfigInterfaceDefinition(pluginPackage, dbPluginConfigDef, xmlIntf);

            } else {
                log.info("interface exists and try to update,{} {}", dbPluginConfigDef.getId(), xmlIntf.getAction());
                tryUpdatePluginConfigInterface(pluginPackage, dbPluginConfigDef, toUpdateIntf, xmlIntf);
            }
        }
    }

    private String buildXmlComments(PluginPackages pluginPackage) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sDate = df.format(new Date());
        String user = AuthenticationContextHolder.getCurrentUsername();
        if (user == null) {
            user = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("<!--\n");
        sb.append("\n");
        sb.append("**THIS FILE WAS GENERATED BY WECUBE PLATFORM PROGRAM**\n");
        sb.append("**Date:").append(sDate).append("\n");
        sb.append("**UserID:").append(user).append("\n");
        sb.append("\n");
        sb.append("**PluginPackageID:").append(pluginPackage.getId()).append("\n");
        sb.append("**PluginName:").append(pluginPackage.getName()).append("\n");
        sb.append("**PluginVersion:").append(pluginPackage.getVersion()).append("\n");

        sb.append("\n-->");

        return sb.toString();
    }

    private Map<String, PluginConfigs> pickoutPluginConfigDefinitions(PluginPackages pluginPackage) {

        List<PluginConfigs> dbPluginConfigs = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackage.getId());
        Map<String, PluginConfigs> nameAndPluginConfigDefs = new HashMap<String, PluginConfigs>();
        if (dbPluginConfigs == null || dbPluginConfigs.isEmpty()) {
            return nameAndPluginConfigDefs;
        }

        for (PluginConfigs pc : dbPluginConfigs) {
            if (StringUtils.isBlank(pc.getRegisterName())) {
                nameAndPluginConfigDefs.put(pc.getName(), pc);
            }
        }

        return nameAndPluginConfigDefs;
    }

    private PluginConfigs tryUpdatePluginConfig(PluginPackages pluginPackage, PluginConfigs toUpdatePluginConfig,
            PluginConfigType xmlPluginConfig, PluginConfigs pluginConfigDef) {
        toUpdatePluginConfig.setTargetEntity(xmlPluginConfig.getTargetEntity());
        toUpdatePluginConfig.setTargetEntityFilterRule(xmlPluginConfig.getTargetEntityFilterRule());
        toUpdatePluginConfig.setTargetPackage(xmlPluginConfig.getTargetPackage());

        List<PluginConfigInterfaceType> xmlIntfList = xmlPluginConfig.getPluginInterface();
        if (xmlIntfList == null || xmlIntfList.isEmpty()) {
            return toUpdatePluginConfig;
        }

        List<PluginConfigInterfaces> toUpdateInterfaces = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(toUpdatePluginConfig.getId());

        for (PluginConfigInterfaceType xmlIntf : xmlIntfList) {
            if (StringUtils.isBlank(xmlIntf.getAction())) {
                throw new WecubeCoreException("3235", "Action of interface cannot be blank.");
            }
            PluginConfigInterfaces toUpdateIntf = pickoutPluginConfigInterface(toUpdateInterfaces, xmlIntf.getAction(),
                    xmlIntf.getPath());
            if (toUpdateIntf == null) {
                log.debug("interface doesnot exist and try to create one,{} {}", toUpdatePluginConfig.getId(),
                        xmlIntf.getAction());
                PluginConfigInterfaces interfDef = pickoutPluginConfigInterfaceDef(pluginConfigDef, xmlIntf.getPath());
                if (interfDef != null) {
                    PluginConfigInterfaces newIntf = tryCreatePluginConfigInterface(pluginPackage, toUpdatePluginConfig,
                            xmlIntf, interfDef);

                    log.info("created {} {}", PluginConfigInterfaces.class.getSimpleName(), newIntf.getId());
                }
            } else {
                log.debug("interface exists and try to update,{} {}", toUpdatePluginConfig.getId(),
                        xmlIntf.getAction());
                tryUpdatePluginConfigInterface(pluginPackage, toUpdatePluginConfig, toUpdateIntf, xmlIntf);
            }
        }

        pluginConfigsMapper.updateByPrimaryKeySelective(toUpdatePluginConfig);
        log.debug("plugin config updated : {} {} {} {}", toUpdatePluginConfig.getId(),
                toUpdatePluginConfig.getTargetEntity(), toUpdatePluginConfig.getTargetEntityFilterRule(),
                toUpdatePluginConfig.getTargetPackage());
        //
        tryUpdatePluginConfigRoleBinds(xmlPluginConfig, pluginConfigDef, toUpdatePluginConfig);

        // #2109
        tryUpdateObjectMetas(xmlPluginConfig, toUpdatePluginConfig);
        return toUpdatePluginConfig;
    }

    private void tryUpdateObjectMetas(PluginConfigType xmlPluginConfig, PluginConfigs savedPluginConfig) {
        List<ParamObjectType> xmlParamObjects = xmlPluginConfig.getParamObject();
        if (xmlParamObjects == null || xmlParamObjects.isEmpty()) {
            return;
        }

        for (ParamObjectType xmlParamObjectType : xmlParamObjects) {
            CoreObjectMeta objectMeta = coreObjectMetaMapper.selectOneByPackageNameAndObjectNameAndConfig(
                    xmlParamObjectType.getPackageName(), xmlParamObjectType.getName(), savedPluginConfig.getId());

            if (objectMeta == null) {
                objectMeta = new CoreObjectMeta();
                objectMeta.setId(LocalIdGenerator.generateId());
                objectMeta.setConfigId(savedPluginConfig.getId());
                objectMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                objectMeta.setCreatedTime(new Date());
                objectMeta.setLatestSource(xmlParamObjectType.getLatestSource());
                objectMeta.setName(xmlParamObjectType.getName());
                objectMeta.setPackageName(xmlParamObjectType.getPackageName());
                objectMeta.setSource(xmlParamObjectType.getSource());

                coreObjectMetaMapper.insert(objectMeta);
            } else {
                objectMeta.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                objectMeta.setUpdatedTime(new Date());
                coreObjectMetaMapper.updateByPrimaryKeySelective(objectMeta);
            }

            tryUpdateObjectPropertyMetas(xmlPluginConfig, savedPluginConfig, xmlParamObjectType, objectMeta);

        }
    }

    private void tryUpdateObjectPropertyMetas(PluginConfigType xmlPluginConfig, PluginConfigs savedPluginConfig,
            ParamObjectType xmlParamObjectType, CoreObjectMeta objectMeta) {
        List<ParamPropertyType> xmlPropertyMetas = xmlParamObjectType.getProperty();
        if (xmlPropertyMetas == null || xmlPropertyMetas.isEmpty()) {
            return;
        }

        List<CoreObjectPropertyMeta> existsPropertyMetas = coreObjectPropertyMetaMapper
                .selectAllByObjectMeta(objectMeta.getId());

        for (ParamPropertyType xmlPropertyMeta : xmlPropertyMetas) {
            String propertyName = xmlPropertyMeta.getName();
            if (StringUtils.isBlank(propertyName)) {
                continue;
            }
            CoreObjectPropertyMeta propertyMeta = findExistsPropertyMetas(existsPropertyMetas, propertyName);

            if (propertyMeta == null) {
                propertyMeta = new CoreObjectPropertyMeta();
                propertyMeta.setConfigId(savedPluginConfig.getId());
                propertyMeta.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                propertyMeta.setCreatedTime(new Date());
                propertyMeta.setDataType(xmlPropertyMeta.getDataType());
                propertyMeta.setId(LocalIdGenerator.generateId());
                propertyMeta.setMapExpr(xmlPropertyMeta.getMapExpr());
                propertyMeta.setMapType(xmlPropertyMeta.getMapType());
                propertyMeta.setName(xmlPropertyMeta.getName());
                propertyMeta.setObjectName(objectMeta.getName());
                propertyMeta.setObjectMetaId(objectMeta.getId());
                propertyMeta.setPackageName(objectMeta.getPackageName());
                propertyMeta.setRefObjectName(xmlPropertyMeta.getRefObjectName());
                propertyMeta.setMultiple(xmlPropertyMeta.getMultiple());
                boolean sensitive = false;
                if (Constants.DATA_SENSITIVE.equalsIgnoreCase(xmlPropertyMeta.getSensitiveData())) {
                    sensitive = true;
                }
                propertyMeta.setSensitive(sensitive);
                propertyMeta.setSource(objectMeta.getSource());

                coreObjectPropertyMetaMapper.insert(propertyMeta);
            } else {
                propertyMeta.setConfigId(savedPluginConfig.getId());
                propertyMeta.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                propertyMeta.setUpdatedTime(new Date());
                propertyMeta.setDataType(xmlPropertyMeta.getDataType());
                propertyMeta.setMapExpr(xmlPropertyMeta.getMapExpr());
                propertyMeta.setMapType(xmlPropertyMeta.getMapType());
                propertyMeta.setRefObjectName(xmlPropertyMeta.getRefObjectName());
                propertyMeta.setMultiple(xmlPropertyMeta.getMultiple());
                boolean sensitive = false;
                if (Constants.DATA_SENSITIVE.equalsIgnoreCase(xmlPropertyMeta.getSensitiveData())) {
                    sensitive = true;
                }
                propertyMeta.setSensitive(sensitive);

                coreObjectPropertyMetaMapper.updateByPrimaryKeySelective(propertyMeta);
            }

        }

    }

    private CoreObjectPropertyMeta findExistsPropertyMetas(List<CoreObjectPropertyMeta> existsPropertyMetas,
            String propertyName) {
        if (StringUtils.isBlank(propertyName)) {
            return null;
        }

        if (existsPropertyMetas == null) {
            return null;
        }

        for (CoreObjectPropertyMeta m : existsPropertyMetas) {
            if (propertyName.equals(m.getName())) {
                return m;
            }
        }

        return null;
    }

    private void tryUpdatePluginConfigRoleBinds(PluginConfigType xmlPluginConfig, PluginConfigs pluginConfigDef,
            PluginConfigs toUpdatePluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = xmlPluginConfig.getRoleBinds();
        if (xmlRoleBinds == null || xmlRoleBinds.getRoleBind().isEmpty()) {
            return;
        }

        String pluginConfigId = toUpdatePluginConfig.getId();
        for (PluginRoleBindingType xmlRoleBind : xmlRoleBinds.getRoleBind()) {
            List<PluginConfigRoles> existsEntities = pluginConfigRolesMapper
                    .selectAllByPluginConfigIdAndPermissionAndRoleName(pluginConfigId, xmlRoleBind.getPermission(),
                            xmlRoleBind.getRoleName());

            if (existsEntities != null && !existsEntities.isEmpty()) {
                continue;
            }

            PluginConfigRoles entity = new PluginConfigRoles();
            entity.setId(LocalIdGenerator.generateId());
            entity.setIsActive(true);
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());
            entity.setPermType(xmlRoleBind.getPermission());
            entity.setPluginCfgId(pluginConfigId);
            RoleDto roleDto = fetchRoleWithRoleName(xmlRoleBind.getRoleName());
            if (roleDto != null) {
                entity.setRoleId(roleDto.getId());
            }
            entity.setRoleName(xmlRoleBind.getRoleName());

            pluginConfigRolesMapper.insert(entity);
        }
    }

    private PluginConfigInterfaces pickoutPluginConfigInterface(List<PluginConfigInterfaces> toUpdateInterfaces,
            String action, String path) {
        if (toUpdateInterfaces == null) {
            return null;
        }

        for (PluginConfigInterfaces intf : toUpdateInterfaces) {
            if (action.equals(intf.getAction()) && path.equals(intf.getPath())) {
                return intf;
            }
        }

        return null;

    };

    private Map<String, PluginConfigInterfaceType> pickoutPluginConfigInterfaceTypeByPath(
            List<PluginConfigInterfaceType> xmlPluginInterfaceList, String path) {

        Map<String, PluginConfigInterfaceType> xmlIntfs = new HashMap<String, PluginConfigInterfaceType>();

        if (xmlPluginInterfaceList == null || xmlPluginInterfaceList.isEmpty()) {
            return xmlIntfs;
        }

        for (PluginConfigInterfaceType xmlIntf : xmlPluginInterfaceList) {
            if (path.equals(xmlIntf.getPath())) {
                xmlIntfs.put(xmlIntf.getAction(), xmlIntf);
            }
        }

        return xmlIntfs;
    }

    private PluginConfigInterfaces tryCreatePluginConfigInterface(PluginPackages pluginPackage,
            PluginConfigs pluginConfig, PluginConfigInterfaceType xmlInterf, PluginConfigInterfaces interfDef) {
        if (xmlInterf == null) {
            return null;
        }

        if (interfDef == null) {
            log.info("Can not find such interfce definition for {}", xmlInterf.getAction());
            return null;
        }

        PluginConfigInterfaces newIntf = new PluginConfigInterfaces();
        newIntf.setId(LocalIdGenerator.generateId());
        newIntf.setPluginConfigId(pluginConfig.getId());
        newIntf.setPath(xmlInterf.getPath());

        newIntf.setAction(xmlInterf.getAction());
        newIntf.setFilterRule(xmlInterf.getFilterRule());
        newIntf.setHttpMethod(xmlInterf.getHttpMethod());
        newIntf.setIsAsyncProcessing(xmlInterf.getIsAsyncProcessing());

        newIntf.setType(xmlInterf.getType());
        newIntf.setDescription(xmlInterf.getDescription());
        newIntf.setServiceDisplayName(newIntf.generateServiceName(pluginPackage, pluginConfig));
        newIntf.setServiceName(newIntf.generateServiceName(pluginPackage, pluginConfig));

        pluginConfigInterfacesMapper.insert(newIntf);

        List<PluginConfigInterfaceParameters> inputParamDefs = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(interfDef.getId(), Constants.TYPE_INPUT);
        PluginConfigInputParametersType xmlInputParametersType = xmlInterf.getInputParameters();
        List<PluginConfigInputParameterType> xmlInputParameters = null;
        if (xmlInputParametersType != null) {
            xmlInputParameters = xmlInputParametersType.getParameter();
        }

        if (inputParamDefs == null) {
            inputParamDefs = new ArrayList<>();
        }

        List<PluginConfigInterfaceParameters> inputParameters = new ArrayList<>();

        for (PluginConfigInterfaceParameters inputParamDef : inputParamDefs) {
            PluginConfigInputParameterType xmlInputParam = tryPickoutXmlInputParam(xmlInputParameters,
                    inputParamDef.getName());

            PluginConfigInterfaceParameters inputParam = tryCreateInputParameter(newIntf, xmlInputParam, inputParamDef);

            inputParameters.add(inputParam);
        }

        newIntf.setInputParameters(inputParameters);

        List<PluginConfigInterfaceParameters> outputParamDefs = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(interfDef.getId(), Constants.TYPE_OUTPUT);
        PluginConfigOutputParametersType xmlOutputParametersType = xmlInterf.getOutputParameters();
        List<PluginConfigOutputParameterType> xmlOutputParameters = null;
        if (xmlOutputParametersType != null) {
            xmlOutputParameters = xmlOutputParametersType.getParameter();
        }
        List<PluginConfigInterfaceParameters> outputParameters = new ArrayList<>();

        if (outputParamDefs == null) {
            outputParamDefs = new ArrayList<>();
        }

        for (PluginConfigInterfaceParameters outputParamDef : outputParamDefs) {

            PluginConfigOutputParameterType xmlOutputParam = tryPickoutXmlOutputParam(xmlOutputParameters,
                    outputParamDef.getName());
            PluginConfigInterfaceParameters outputParam = tryCreateOutputParameter(newIntf, xmlOutputParam,
                    outputParamDef);
            outputParameters.add(outputParam);
        }

        newIntf.setOutputParameters(outputParameters);

        pluginConfigInterfacesMapper.updateByPrimaryKeySelective(newIntf);

        return newIntf;
    }

    private PluginConfigInputParameterType tryPickoutXmlInputParam(List<PluginConfigInputParameterType> xmlInputParams,
            String paramName) {

        if (xmlInputParams == null || xmlInputParams.isEmpty()) {
            return null;
        }

        for (PluginConfigInputParameterType xmlParam : xmlInputParams) {
            if (paramName.equals(xmlParam.getValue())) {
                return xmlParam;
            }
        }

        return null;
    }

    private PluginConfigOutputParameterType tryPickoutXmlOutputParam(
            List<PluginConfigOutputParameterType> xmlOutputParams, String paramName) {
        if (xmlOutputParams == null || xmlOutputParams.isEmpty()) {
            return null;
        }

        for (PluginConfigOutputParameterType xmlParam : xmlOutputParams) {
            if (paramName.equals(xmlParam.getValue())) {
                return xmlParam;
            }
        }

        return null;
    }

    private PluginConfigInterfaceParameters tryCreateInputParameter(PluginConfigInterfaces intf,
            PluginConfigInputParameterType xmlInputParam, PluginConfigInterfaceParameters inputParamDef) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();

        param.setId(LocalIdGenerator.generateId());
        param.setType(Constants.TYPE_INPUT);
        param.setPluginConfigInterfaceId(intf.getId());
        param.setRequired(inputParamDef.getRequired());

        if (xmlInputParam != null) {
            param.setName(xmlInputParam.getValue());
            param.setDataType(xmlInputParam.getDatatype());
            param.setDescription(xmlInputParam.getDescription());
            param.setMappingEntityExpression(xmlInputParam.getMappingEntityExpression());
            param.setMappingSystemVariableName(xmlInputParam.getMappingSystemVariableName());
            param.setMappingType(xmlInputParam.getMappingType());
            param.setSensitiveData(xmlInputParam.getSensitiveData());
            param.setMappingValue(xmlInputParam.getMappingValue());
            param.setRefObjectName(xmlInputParam.getRefObjectName());
            param.setMultiple(xmlInputParam.getMultiple());
        } else {
            param.setName(inputParamDef.getName());
            param.setDataType(inputParamDef.getDataType());
            param.setDescription(inputParamDef.getDescription());
            param.setMappingEntityExpression(inputParamDef.getMappingEntityExpression());
            param.setMappingSystemVariableName(inputParamDef.getMappingSystemVariableName());
            param.setMappingType(inputParamDef.getMappingType());
            param.setSensitiveData(inputParamDef.getSensitiveData());
            param.setMappingValue(inputParamDef.getMappingValue());
            param.setRefObjectName(inputParamDef.getRefObjectName());
            param.setMultiple(inputParamDef.getMultiple());
        }

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;

    }

    private PluginConfigInterfaceParameters tryCreateOutputParameter(PluginConfigInterfaces intf,
            PluginConfigOutputParameterType xmlOutputParam, PluginConfigInterfaceParameters outputParamDef) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();
        param.setId(LocalIdGenerator.generateId());
        param.setType(Constants.TYPE_OUTPUT);
        param.setPluginConfigInterfaceId(intf.getId());

        if (xmlOutputParam != null) {
            param.setName(xmlOutputParam.getValue());
            param.setDataType(xmlOutputParam.getDatatype());
            param.setDescription(xmlOutputParam.getDescription());
            param.setMappingEntityExpression(xmlOutputParam.getMappingEntityExpression());
            param.setMappingType(xmlOutputParam.getMappingType());
            param.setSensitiveData(xmlOutputParam.getSensitiveData());
            param.setMappingSystemVariableName(xmlOutputParam.getMappingSystemVariableName());
            param.setMappingValue(xmlOutputParam.getMappingValue());
            param.setMultiple(xmlOutputParam.getMultiple());
            param.setRefObjectName(xmlOutputParam.getRefObjectName());
        } else {
            param.setName(outputParamDef.getName());
            param.setDataType(outputParamDef.getDataType());
            param.setDescription(outputParamDef.getDescription());
            param.setMappingEntityExpression(outputParamDef.getMappingEntityExpression());
            param.setMappingType(outputParamDef.getMappingType());
            param.setSensitiveData(outputParamDef.getSensitiveData());
            param.setMappingSystemVariableName(outputParamDef.getMappingSystemVariableName());
            param.setMappingValue(outputParamDef.getMappingValue());
            param.setMultiple(outputParamDef.getMultiple());
            param.setRefObjectName(outputParamDef.getRefObjectName());
        }

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;
    }

    private void handlePluginConfig(PluginPackages pluginPackage, PluginConfigType xmlPluginConfig,
            PluginConfigs pluginConfigDef) {

        if (log.isDebugEnabled()) {
            log.debug("start to handle plugin config {} {}", xmlPluginConfig.getName(),
                    xmlPluginConfig.getRegisterName());
        }

        if (pluginConfigDef == null) {
            log.info("There is no plugin definition found for {} {} {}", pluginPackage.getId(),
                    xmlPluginConfig.getName(), xmlPluginConfig.getRegisterName());
            return;
        }

        List<PluginConfigs> pluginConfigs = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackage.getId());

        PluginConfigs existPluginConfig = pickoutPluginConfigWithRegisterName(pluginConfigs, xmlPluginConfig.getName(),
                xmlPluginConfig.getRegisterName());

        PluginConfigs pc = null;
        if (existPluginConfig != null) {
            log.debug("such plugin config already exist and try to update,{} {}", pluginPackage.getId(),
                    existPluginConfig.getRegisterName());
            pc = tryUpdatePluginConfig(pluginPackage, existPluginConfig, xmlPluginConfig, pluginConfigDef);
        } else {
            log.debug("try to create a new plugin config for {} {}", pluginPackage.getId(),
                    xmlPluginConfig.getRegisterName());
            pc = tryCreatePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        log.debug("operations take effect into {} {}", PluginConfigs.class.getName(), pc.getId());

    }

    private PluginConfigInterfaces pickoutPluginConfigInterfaceDef(PluginConfigs pluginConfigDef, String path) {
        List<PluginConfigInterfaces> intfDefs = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigDef.getId());
        if (intfDefs == null || intfDefs.isEmpty()) {
            return null;
        }

        for (PluginConfigInterfaces intfDef : intfDefs) {
            if (path.equals(intfDef.getPath())) {
                return intfDef;
            }
        }

        return null;
    }

    private PluginConfigs pickoutPluginConfigWithRegisterName(List<PluginConfigs> pluginConfigs, String name,
            String registerName) {
        if (pluginConfigs == null || pluginConfigs.isEmpty()) {
            return null;
        }

        for (PluginConfigs pc : pluginConfigs) {
            if (registerName.equals(pc.getRegisterName()) && name.equals(pc.getName())) {
                return pc;
            }
        }

        return null;
    }

}
