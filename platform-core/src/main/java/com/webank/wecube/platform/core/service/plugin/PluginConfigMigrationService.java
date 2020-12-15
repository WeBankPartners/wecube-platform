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
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
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
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginConfigMigrationService {
    private static final Logger log = LoggerFactory.getLogger(PluginConfigMigrationService.class);
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
        List<SystemVariables> sysVars = this.systemVariablesMapper.selectAllBySource(pluginPackage.getId());
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
        return xmlPluginConfig;

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

        PluginConfigInputParametersType xmlInputParameters = new PluginConfigInputParametersType();
        xmlIntf.setInputParameters(xmlInputParameters);

        List<PluginConfigInterfaceParameters> inputParameters = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intf.getId(), PluginConfigInterfaceParameters.TYPE_INPUT);
        if (inputParameters != null) {
            for (PluginConfigInterfaceParameters inputParameter : inputParameters) {
                PluginConfigInputParameterType xmlInputParameter = buildXmlInputParameter(inputParameter);
                xmlInputParameters.getParameter().add(xmlInputParameter);
            }
        }

        PluginConfigOutputParametersType xmlOutputParameters = new PluginConfigOutputParametersType();
        xmlIntf.setOutputParameters(xmlOutputParameters);

        List<PluginConfigInterfaceParameters> outputParameters = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intf.getId(), PluginConfigInterfaceParameters.TYPE_OUTPUT);
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

        return xmlParam;
    }

    private PluginConfigInterfaces tryUpdatePluginConfigInterface(PluginPackages pluginPackage,
            PluginConfigs existPluginConfig, PluginConfigInterfaces toUpdateIntf, PluginConfigInterfaceType xmlIntf) {
        if (xmlIntf == null) {
            return toUpdateIntf;
        }

        toUpdateIntf.setFilterRule(xmlIntf.getFilterRule());

        List<PluginConfigInterfaceParameters> inputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(toUpdateIntf.getId(),
                        PluginConfigInterfaceParameters.TYPE_INPUT);
        if (inputParameters != null) {
            for (PluginConfigInterfaceParameters inputParam : inputParameters) {
                PluginConfigInputParameterType xmlInputParam = pickoutPluginConfigInputParameterType(xmlIntf,
                        inputParam.getName());
                tryUpdatePluginConfigInterfaceInputParameter(existPluginConfig, toUpdateIntf, inputParam,
                        xmlInputParam);
            }
        }

        List<PluginConfigInterfaceParameters> outputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(toUpdateIntf.getId(),
                        PluginConfigInterfaceParameters.TYPE_OUTPUT);
        if (outputParameters != null) {
            for (PluginConfigInterfaceParameters outputParam : outputParameters) {
                PluginConfigOutputParameterType xmlOutputParam = pickoutPluginConfigOutputParameterType(xmlIntf,
                        outputParam.getName());
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

        List<PluginConfigInterfaceType> xmlPluginInterfaceList = xmlPluginConfig.getPluginInterface();
        List<PluginConfigInterfaces> defInterfaces = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigDef.getId());
        List<PluginConfigInterfaces> createdInterfaces = new ArrayList<PluginConfigInterfaces>();

        if (defInterfaces == null || defInterfaces.isEmpty()) {
            return pluginConfig;
        }

        for (PluginConfigInterfaces defIntf : defInterfaces) {
            if (StringUtils.isBlank(defIntf.getAction())) {
                log.info("The action is blank for {} {}", defIntf.getId(), defIntf.getPath());
                continue;
            }

            Map<String, PluginConfigInterfaceType> actionAndXmlIntfs = pickoutPluginConfigInterfaceTypeByPath(
                    xmlPluginInterfaceList, defIntf.getPath());
            if (actionAndXmlIntfs.isEmpty()) {
                PluginConfigInterfaces intf = tryCreatePluginConfigInterface(pluginPackage, pluginConfig, null,
                        defIntf);
                // pluginConfigInterfacesMapper.insert(intf);
                createdInterfaces.add(intf);
            } else {
                for (PluginConfigInterfaceType xmlIntf : actionAndXmlIntfs.values()) {
                    PluginConfigInterfaces intf = tryCreatePluginConfigInterface(pluginPackage, pluginConfig, xmlIntf,
                            defIntf);
                    // pluginConfigInterfacesMapper.insert(intf);
                    createdInterfaces.add(intf);
                }
            }

        }

        pluginConfig.setInterfaces(createdInterfaces);

        pluginConfigsMapper.insert(pluginConfig);
        // PluginConfigs savedPluginConfig =
        // pluginConfigRepository.saveAndFlush(pluginConfig);

        tryCreatePluginConfigRoleBinds(xmlPluginConfig, pluginConfig);
        return pluginConfig;

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
                    xmlSysVar.getName(), xmlSysVar.getScopeType(), pluginPackage.getId());
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
                sysVarEntity.setSource(pluginPackage.getId());
                sysVarEntity.setStatus(xmlSysVar.getStatus());
                sysVarEntity.setValue(xmlSysVar.getValue());

                systemVariablesMapper.insert(sysVarEntity);
            } else {
                sysVarEntity.setDefaultValue(xmlSysVar.getDefaultValue());
                sysVarEntity.setValue(xmlSysVar.getValue());
                sysVarEntity.setStatus(xmlSysVar.getStatus());

                systemVariablesMapper.updateByPrimaryKeySelective(sysVarEntity);
            }

            // systemVariableRepository.saveAndFlush(sysVarEntity);
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

        Map<String, PluginConfigs> regNamedPluginConfigDefs = pickoutPluginConfigDefinitions(pluginPackage);
        if (log.isDebugEnabled()) {
            log.debug("total {} plugin config declarations found.", regNamedPluginConfigDefs.size());
        }

        for (PluginConfigType xmlPluginConfig : xmlPluginConfigList) {
            if (StringUtils.isBlank(xmlPluginConfig.getName())) {
                throw new WecubeCoreException("3233", "Plugin config name cannot be blank.");
            }

            if (StringUtils.isBlank(xmlPluginConfig.getRegisterName())) {
                throw new WecubeCoreException("3234",
                        String.format("Register name is blank for %s", xmlPluginConfig.getName()),
                        xmlPluginConfig.getName());
            }

            PluginConfigs pluginConfigDef = regNamedPluginConfigDefs.get(xmlPluginConfig.getName());
            handlePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        pluginPackagesMapper.updateByPrimaryKeySelective(pluginPackage);

        log.info("finished importing plugin registries for {} {} from {} {}", pluginPackage.getName(),
                pluginPackage.getVersion(), xmlPluginPackage.getName(), xmlPluginPackage.getVersion());
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
        Map<String, PluginConfigs> pluginConfigDefs = new HashMap<String, PluginConfigs>();
        if (dbPluginConfigs == null || dbPluginConfigs.isEmpty()) {
            return pluginConfigDefs;
        }

        for (PluginConfigs pc : dbPluginConfigs) {
            if (StringUtils.isBlank(pc.getRegisterName())) {
                pluginConfigDefs.put(pc.getName(), pc);
            }
        }

        return pluginConfigDefs;
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
                PluginConfigInterfaces defIntf = pickoutDefPluginConfigInterface(pluginConfigDef, xmlIntf.getPath());
                if (defIntf != null) {
                    PluginConfigInterfaces newIntf = tryCreatePluginConfigInterface(pluginPackage, toUpdatePluginConfig,
                            xmlIntf, defIntf);

                    log.info("created {} {}", PluginConfigInterfaces.class.getSimpleName(), newIntf.getId());
                }
            } else {
                log.debug("interface exists and try to update,{} {}", toUpdatePluginConfig.getId(),
                        xmlIntf.getAction());
                tryUpdatePluginConfigInterface(pluginPackage, toUpdatePluginConfig, toUpdateIntf, xmlIntf);
            }
        }

        pluginConfigsMapper.updateByPrimaryKeySelective(toUpdatePluginConfig);
        // toUpdatePluginConfig =
        // pluginConfigRepository.saveAndFlush(toUpdatePluginConfig);
        log.debug("plugin config updated : {} {} {} {}", toUpdatePluginConfig.getId(),
                toUpdatePluginConfig.getTargetEntity(), toUpdatePluginConfig.getTargetEntityFilterRule(),
                toUpdatePluginConfig.getTargetPackage());
        //
        tryUpdatePluginConfigRoleBinds(xmlPluginConfig, pluginConfigDef, toUpdatePluginConfig);
        return toUpdatePluginConfig;
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
            PluginConfigs pluginConfig, PluginConfigInterfaceType xmlIntf, PluginConfigInterfaces defIntf) {
        PluginConfigInterfaces intf = new PluginConfigInterfaces();
        intf.setId(LocalIdGenerator.generateId());
        intf.setPluginConfigId(pluginConfig.getId());
        intf.setPath(defIntf.getPath());

        if (xmlIntf != null) {
            intf.setAction(xmlIntf.getAction());
            intf.setFilterRule(xmlIntf.getFilterRule());
            intf.setHttpMethod(xmlIntf.getHttpMethod());
            intf.setIsAsyncProcessing(xmlIntf.getIsAsyncProcessing());

            intf.setType(xmlIntf.getType());
        } else {
            intf.setAction(defIntf.getAction());
            intf.setFilterRule(defIntf.getFilterRule());
            intf.setHttpMethod(defIntf.getHttpMethod());
            intf.setIsAsyncProcessing(defIntf.getIsAsyncProcessing());

            intf.setType(defIntf.getType());
        }

        List<PluginConfigInterfaceParameters> defInputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(defIntf.getId(), PluginConfigInterfaceParameters.TYPE_INPUT);
        List<PluginConfigInterfaceParameters> inputParameters = new ArrayList<>();
        if (defInputParameters != null) {
            for (PluginConfigInterfaceParameters defInputParam : defInputParameters) {
                PluginConfigInputParameterType xmlInputParam = pickoutPluginConfigInputParameterType(xmlIntf,
                        defInputParam.getName());
                PluginConfigInterfaceParameters inputParam = tryCreateInputParameter(intf, xmlInputParam,
                        defInputParam);
                inputParameters.add(inputParam);
            }
        }

        intf.setInputParameters(inputParameters);

        List<PluginConfigInterfaceParameters> defOutputParameters = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(defIntf.getId(), PluginConfigInterfaceParameters.TYPE_OUTPUT);
        List<PluginConfigInterfaceParameters> outputParameters = new ArrayList<>();

        if (defOutputParameters != null) {
            for (PluginConfigInterfaceParameters defOutputParam : defOutputParameters) {
                PluginConfigOutputParameterType xmlOutputParam = pickoutPluginConfigOutputParameterType(xmlIntf,
                        defOutputParam.getName());
                PluginConfigInterfaceParameters outputParam = tryCreateOutputParameter(intf, xmlOutputParam,
                        defOutputParam);
                outputParameters.add(outputParam);
            }
        }

        // intf.setOutputParameters(outputParameters);

        intf.setServiceDisplayName(intf.generateServiceName(pluginPackage, pluginConfig));
        intf.setServiceName(intf.generateServiceName(pluginPackage, pluginConfig));
        intf.setOutputParameters(outputParameters);

        pluginConfigInterfacesMapper.insert(intf);

        return intf;
    }

    private PluginConfigInterfaceParameters tryCreateInputParameter(PluginConfigInterfaces intf,
            PluginConfigInputParameterType xmlInputParam, PluginConfigInterfaceParameters defInputParam) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();

        param.setId(LocalIdGenerator.generateId());
        param.setName(defInputParam.getName());
        param.setDataType(defInputParam.getDataType());
        param.setType(PluginConfigInterfaceParameters.TYPE_INPUT);
        param.setPluginConfigInterfaceId(intf.getId());
        param.setRequired(defInputParam.getRequired());

        if (xmlInputParam != null) {
            param.setMappingEntityExpression(xmlInputParam.getMappingEntityExpression());
            param.setMappingSystemVariableName(xmlInputParam.getMappingSystemVariableName());
            param.setMappingType(xmlInputParam.getMappingType());
            param.setSensitiveData(xmlInputParam.getSensitiveData());
        } else {
            param.setMappingEntityExpression(defInputParam.getMappingEntityExpression());
            param.setMappingSystemVariableName(defInputParam.getMappingSystemVariableName());
            param.setMappingType(defInputParam.getMappingType());
            param.setSensitiveData(defInputParam.getSensitiveData());
        }

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;

    }

    private PluginConfigInterfaceParameters tryCreateOutputParameter(PluginConfigInterfaces intf,
            PluginConfigOutputParameterType xmlOutputParam, PluginConfigInterfaceParameters defOutputParam) {
        PluginConfigInterfaceParameters param = new PluginConfigInterfaceParameters();
        param.setId(LocalIdGenerator.generateId());
        param.setName(defOutputParam.getName());
        param.setDataType(defOutputParam.getDataType());
        param.setType(PluginConfigInterfaceParameters.TYPE_OUTPUT);
        param.setPluginConfigInterfaceId(intf.getId());

        if (xmlOutputParam != null) {
            param.setMappingEntityExpression(xmlOutputParam.getMappingEntityExpression());
            param.setMappingType(xmlOutputParam.getMappingType());
            param.setSensitiveData(xmlOutputParam.getSensitiveData());
        } else {
            param.setMappingEntityExpression(defOutputParam.getMappingEntityExpression());
            param.setMappingType(defOutputParam.getMappingType());
            param.setSensitiveData(defOutputParam.getSensitiveData());
        }

        pluginConfigInterfaceParametersMapper.insert(param);

        return param;
    }

    private PluginConfigInputParameterType pickoutPluginConfigInputParameterType(PluginConfigInterfaceType xmlIntf,
            String paramName) {
        if (xmlIntf == null) {
            return null;
        }

        PluginConfigInputParametersType inputParameters = xmlIntf.getInputParameters();
        if (inputParameters == null) {
            return null;
        }

        for (PluginConfigInputParameterType p : inputParameters.getParameter()) {
            if (paramName.equals(p.getValue())) {
                return p;
            }
        }

        return null;

    }

    private PluginConfigOutputParameterType pickoutPluginConfigOutputParameterType(PluginConfigInterfaceType xmlIntf,
            String paramName) {
        if (xmlIntf == null) {
            return null;
        }

        PluginConfigOutputParametersType outputParameters = xmlIntf.getOutputParameters();

        if (outputParameters == null) {
            return null;
        }

        for (PluginConfigOutputParameterType p : outputParameters.getParameter()) {
            if (paramName.equals(p.getValue())) {
                return p;
            }
        }

        return null;
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
        // if (pc != null) {
        // pluginPackage.addPluginConfig(pc);
        // }

    }

    private PluginConfigInterfaces pickoutDefPluginConfigInterface(PluginConfigs pluginConfigDef, String path) {
        List<PluginConfigInterfaces> defIntfs = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigDef.getId());
        if (defIntfs == null || defIntfs.isEmpty()) {
            return null;
        }

        for (PluginConfigInterfaces defIntf : defIntfs) {
            if (path.equals(defIntf.getPath())) {
                return defIntf;
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
