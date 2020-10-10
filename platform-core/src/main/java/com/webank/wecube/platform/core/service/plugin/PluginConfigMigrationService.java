package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.PluginAuthEntity;
import com.webank.wecube.platform.core.jpa.PluginAuthRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.SystemVariableRepository;
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

@Service
public class PluginConfigMigrationService {
    private static final Logger log = LoggerFactory.getLogger(PluginConfigMigrationService.class);
    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    private PluginConfigRepository pluginConfigRepository;
    @Autowired
    private PluginAuthRepository pluginAuthRepository;
    @Autowired
    private SystemVariableRepository systemVariableRepository;

    @Autowired
    private UserManagementService userManagementService;

    public PluginRegistryInfo exportPluginRegistersForOnePackage(String pluginPackageId) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("3226","Plugin package ID cannot be blank.");
        }

        Optional<PluginPackage> pluginPackageEntityOpt = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageEntityOpt.isPresent()) {
            throw new WecubeCoreException("3227","Bad plugin package ID,such package does not exist.");
        }

        PluginPackage pluginPackage = pluginPackageEntityOpt.get();

        log.info("start to export plugin package registry,{} {} {}", pluginPackageId, pluginPackage.getName(),
                pluginPackage.getVersion());

        PluginPackageType xmlPluginPackage = new PluginPackageType();
        xmlPluginPackage.setName(pluginPackage.getName());
        xmlPluginPackage.setVersion(pluginPackage.getVersion());

        Optional<List<PluginConfig>> pluginConfigsOpt = pluginConfigRepository
                .findByPluginPackage_idOrderByName(pluginPackageId);

        if (!pluginConfigsOpt.isPresent()) {
            log.info("Such package ID has no plugin configs.PluginPackageId={}", pluginPackageId);
        }else {

        	List<PluginConfig> pluginConfigs = pluginConfigsOpt.get();

        	PluginConfigsType xmlPluginConfigs = buildXmlPluginConfigs(pluginPackage, pluginConfigs);
        	xmlPluginPackage.setPlugins(xmlPluginConfigs);
        }

        SystemParametersType xmlSystemVariables = buildSystemParametersType(pluginPackage);
        xmlPluginPackage.setSystemParameters(xmlSystemVariables);

        String xmlContent = JaxbUtils.convertToXml(xmlPluginPackage);

        if (log.isDebugEnabled()) {
            log.debug("EXPORT:{}", xmlContent);
        }

        String comments = buildXmlComments(pluginPackage);

        PluginRegistryInfo prInfo = new PluginRegistryInfo();
        prInfo.setPluginPackageData(xmlContent + comments);
        prInfo.setPluginPackageName(pluginPackage.getName());
        prInfo.setPluginPackageVersion(pluginPackage.getVersion());

        return prInfo;
    }

    @Transactional
    public void importPluginRegistersForOnePackage(String pluginPackageId, String registersAsXml) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("3229","Plugin package ID cannot be blank.");
        }

        if (StringUtils.isBlank(registersAsXml)) {
            throw new WecubeCoreException("3230","XML data is blank.");
        }

        if (log.isDebugEnabled()) {
            log.debug("IMPORT:{}", registersAsXml);
        }

        Optional<PluginPackage> pluginPackageEntityOpt = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageEntityOpt.isPresent()) {
            throw new WecubeCoreException("3231","Bad plugin package ID,such package does not exist.");
        }

        PluginPackage pluginPackage = pluginPackageEntityOpt.get();

        PluginPackageType xmlPluginPackage = JaxbUtils.convertToObject(registersAsXml, PluginPackageType.class);

        if (xmlPluginPackage == null) {
            throw new WecubeCoreException("3232","Bad xml contents.");
        }
        
        String xmlPackageName = xmlPluginPackage.getName();
        String packageName = pluginPackage.getName();
        if(!packageName.equals(xmlPackageName)) {
        	throw new WecubeCoreException("3312",
        			String.format("Plugin packages do not match.The name from XML is %s but the package you chose is %s.", 
        					xmlPackageName, 
        					packageName),
        			xmlPackageName, 
        			packageName);
        }

        performImportPluginRegistersForOnePackage(pluginPackage, xmlPluginPackage);
        performImportSystemParametersForOnePackage(pluginPackage, xmlPluginPackage);
    }

    private SystemParametersType buildSystemParametersType(PluginPackage pluginPackage) {
        SystemParametersType xmlSystemParameters = new SystemParametersType();
        List<SystemVariable> sysVars = systemVariableRepository.findBySource(pluginPackage.getId());
        if (sysVars == null || sysVars.isEmpty()) {
            return xmlSystemParameters;
        }

        for (SystemVariable sysVar : sysVars) {
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

    private PluginConfigsType buildXmlPluginConfigs(PluginPackage pluginPackage, List<PluginConfig> pluginConfigs) {
        PluginConfigsType xmlPluginConfigs = new PluginConfigsType();
        for (PluginConfig pluginConfig : pluginConfigs) {
            if (StringUtils.isBlank(pluginConfig.getRegisterName())) {
                continue;
            }

            PluginConfigType xmlPluginConfig = buildXmlPluginConfig(pluginPackage, pluginConfig);
            xmlPluginConfigs.getPlugin().add(xmlPluginConfig);
        }

        return xmlPluginConfigs;
    }

    private PluginConfigType buildXmlPluginConfig(PluginPackage pluginPackage, PluginConfig pluginConfig) {
        PluginConfigType xmlPluginConfig = new PluginConfigType();
        xmlPluginConfig.setName(pluginConfig.getName());
        xmlPluginConfig.setRegisterName(pluginConfig.getRegisterName());
        xmlPluginConfig.setStatus(pluginConfig.getStatus().name());
        xmlPluginConfig.setTargetEntity(pluginConfig.getTargetEntity());
        xmlPluginConfig.setTargetEntityFilterRule(pluginConfig.getTargetEntityFilterRule());
        xmlPluginConfig.setTargetPackage(pluginConfig.getTargetPackage());

        Set<PluginConfigInterface> intfs = pluginConfig.getInterfaces();
        if (intfs != null) {
            for (PluginConfigInterface intf : intfs) {
                PluginConfigInterfaceType xmlIntf = buildXmlPluginConfigInterface(pluginPackage, pluginConfig, intf);
                xmlPluginConfig.getPluginInterface().add(xmlIntf);
            }
        }

        PluginRoleBindingsType xmlRoleBinds = buildXmlPluginRoleBindingsType(pluginConfig);
        xmlPluginConfig.setRoleBinds(xmlRoleBinds);
        return xmlPluginConfig;

    }

    private PluginRoleBindingsType buildXmlPluginRoleBindingsType(PluginConfig pluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = new PluginRoleBindingsType();

        List<PluginAuthEntity> authEntities = pluginAuthRepository.findAllByPluginConfigId(pluginConfig.getId());
        if (authEntities == null || authEntities.isEmpty()) {
            return xmlRoleBinds;
        }

        for (PluginAuthEntity entity : authEntities) {
            PluginRoleBindingType xmlRoleBind = new PluginRoleBindingType();
            xmlRoleBind.setPermission(entity.getPermissionType());
            xmlRoleBind.setRoleName(entity.getRoleName());

            xmlRoleBinds.getRoleBind().add(xmlRoleBind);
        }

        return xmlRoleBinds;
    }

    private PluginConfigInterfaceType buildXmlPluginConfigInterface(PluginPackage pluginPackage,
            PluginConfig pluginConfig, PluginConfigInterface intf) {
        PluginConfigInterfaceType xmlIntf = new PluginConfigInterfaceType();
        xmlIntf.setAction(intf.getAction());
        xmlIntf.setFilterRule(intf.getFilterRule());
        xmlIntf.setHttpMethod(intf.getHttpMethod());
        xmlIntf.setIsAsyncProcessing(intf.getIsAsyncProcessing());
        xmlIntf.setPath(intf.getPath());
        xmlIntf.setType(intf.getType());

        PluginConfigInputParametersType xmlInputParameters = new PluginConfigInputParametersType();
        xmlIntf.setInputParameters(xmlInputParameters);

        Set<PluginConfigInterfaceParameter> inputParameters = intf.getInputParameters();
        if (inputParameters != null) {
            for (PluginConfigInterfaceParameter inputParameter : inputParameters) {
                PluginConfigInputParameterType xmlInputParameter = buildXmlInputParameter(inputParameter);
                xmlInputParameters.getParameter().add(xmlInputParameter);
            }
        }

        PluginConfigOutputParametersType xmlOutputParameters = new PluginConfigOutputParametersType();
        xmlIntf.setOutputParameters(xmlOutputParameters);

        Set<PluginConfigInterfaceParameter> outputParameters = intf.getOutputParameters();
        if (outputParameters != null) {
            for (PluginConfigInterfaceParameter outputParameter : outputParameters) {
                PluginConfigOutputParameterType xmlOutputParameter = buildXmlOutputParameter(outputParameter);
                xmlOutputParameters.getParameter().add(xmlOutputParameter);
            }
        }

        return xmlIntf;
    }

    private PluginConfigOutputParameterType buildXmlOutputParameter(PluginConfigInterfaceParameter outputParameter) {
        PluginConfigOutputParameterType xmlParam = new PluginConfigOutputParameterType();
        xmlParam.setDatatype(outputParameter.getDataType());
        xmlParam.setMappingEntityExpression(outputParameter.getMappingEntityExpression());
        xmlParam.setMappingType(outputParameter.getMappingType());
        xmlParam.setValue(outputParameter.getName());
        xmlParam.setSensitiveData(outputParameter.getSensitiveData());

        return xmlParam;
    }

    private PluginConfigInputParameterType buildXmlInputParameter(PluginConfigInterfaceParameter inputParameter) {
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

    private PluginConfigInterface tryUpdatePluginConfigInterface(PluginConfig existPluginConfig,
            PluginConfigInterface toUpdateIntf, PluginConfigInterfaceType xmlIntf) {
        if (xmlIntf == null) {
            return toUpdateIntf;
        }

        toUpdateIntf.setFilterRule(xmlIntf.getFilterRule());

        Set<PluginConfigInterfaceParameter> inputParameters = toUpdateIntf.getInlineInputParameters();
        if (inputParameters != null) {
            for (PluginConfigInterfaceParameter inputParam : inputParameters) {
                PluginConfigInputParameterType xmlInputParam = pickoutPluginConfigInputParameterType(xmlIntf,
                        inputParam.getName());
                tryUpdatePluginConfigInterfaceInputParameter(existPluginConfig, toUpdateIntf, inputParam,
                        xmlInputParam);
            }
        }

        Set<PluginConfigInterfaceParameter> outputParameters = toUpdateIntf.getInlineOutputParameters();
        if (outputParameters != null) {
            for (PluginConfigInterfaceParameter outputParam : outputParameters) {
                PluginConfigOutputParameterType xmlOutputParam = pickoutPluginConfigOutputParameterType(xmlIntf,
                        outputParam.getName());
                tryUpdatePluginConfigInterfaceOutputParameter(existPluginConfig, toUpdateIntf, outputParam,
                        xmlOutputParam);
            }
        }

        toUpdateIntf.setServiceDisplayName(toUpdateIntf.generateServiceName());
        toUpdateIntf.setServiceName(toUpdateIntf.generateServiceName());

        return toUpdateIntf;
    }

    private PluginConfigInterfaceParameter tryUpdatePluginConfigInterfaceInputParameter(PluginConfig existPluginConfig,
            PluginConfigInterface intf, PluginConfigInterfaceParameter param, PluginConfigInputParameterType xmlParam) {
        if (xmlParam == null) {
            return param;
        }
        param.setMappingType(xmlParam.getMappingType());
        param.setSensitiveData(xmlParam.getSensitiveData());
        param.setMappingEntityExpression(xmlParam.getMappingEntityExpression());
        param.setMappingSystemVariableName(xmlParam.getMappingSystemVariableName());

        return param;
    }

    private PluginConfigInterfaceParameter tryUpdatePluginConfigInterfaceOutputParameter(PluginConfig existPluginConfig,
            PluginConfigInterface intf, PluginConfigInterfaceParameter param,
            PluginConfigOutputParameterType xmlParam) {
        if (xmlParam == null) {
            return param;
        }
        param.setMappingType(xmlParam.getMappingType());
        param.setSensitiveData(xmlParam.getSensitiveData());
        param.setMappingEntityExpression(xmlParam.getMappingEntityExpression());

        return param;

    }

    private PluginConfig tryCreatePluginConfig(PluginPackage pluginPackage, PluginConfigType xmlPluginConfig,
            PluginConfig pluginConfigDef) {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setName(xmlPluginConfig.getName());
        pluginConfig.setRegisterName(xmlPluginConfig.getRegisterName());
        pluginConfig.setStatus(DISABLED);
        pluginConfig.setTargetEntity(xmlPluginConfig.getTargetEntity());
        pluginConfig.setTargetEntityFilterRule(xmlPluginConfig.getTargetEntityFilterRule());
        pluginConfig.setTargetPackage(xmlPluginConfig.getTargetPackage());
        pluginConfig.setPluginPackage(pluginPackage);

        List<PluginConfigInterfaceType> xmlPluginInterfaceList = xmlPluginConfig.getPluginInterface();
        Set<PluginConfigInterface> defInterfaces = pluginConfigDef.getInterfaces();
        Set<PluginConfigInterface> createdInterfaces = new HashSet<PluginConfigInterface>();

        if (defInterfaces == null || defInterfaces.isEmpty()) {
            return pluginConfig;
        }

        for (PluginConfigInterface defIntf : defInterfaces) {
            if (StringUtils.isBlank(defIntf.getAction())) {
                log.info("The action is blank for {} {}", defIntf.getId(), defIntf.getPath());
                continue;
            }

            Map<String, PluginConfigInterfaceType> actionAndXmlIntfs = pickoutPluginConfigInterfaceTypeByPath(
                    xmlPluginInterfaceList, defIntf.getPath());
            if (actionAndXmlIntfs.isEmpty()) {
                PluginConfigInterface intf = tryCreatePluginConfigInterface(pluginConfig, null, defIntf);
                createdInterfaces.add(intf);
            } else {
                for (PluginConfigInterfaceType xmlIntf : actionAndXmlIntfs.values()) {
                    PluginConfigInterface intf = tryCreatePluginConfigInterface(pluginConfig, xmlIntf, defIntf);
                    createdInterfaces.add(intf);
                }
            }

        }

        pluginConfig.setInterfaces(createdInterfaces);

        PluginConfig savedPluginConfig = pluginConfigRepository.saveAndFlush(pluginConfig);

        tryCreatePluginConfigRoleBinds(xmlPluginConfig, savedPluginConfig);
        return savedPluginConfig;

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

    private void tryCreatePluginConfigRoleBinds(PluginConfigType xmlPluginConfig, PluginConfig savedPluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = xmlPluginConfig.getRoleBinds();
        if (xmlRoleBinds == null || xmlRoleBinds.getRoleBind().isEmpty()) {
            return;
        }

        for (PluginRoleBindingType xmlRoleBind : xmlRoleBinds.getRoleBind()) {
            PluginAuthEntity entity = new PluginAuthEntity();
            entity.setActive(true);
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());
            entity.setPermissionType(xmlRoleBind.getPermission());
            entity.setPluginConfigId(savedPluginConfig.getId());
            RoleDto roleDto = fetchRoleWithRoleName(xmlRoleBind.getRoleName());
            if (roleDto != null) {
                entity.setRoleId(roleDto.getId());
            }
            entity.setRoleName(xmlRoleBind.getRoleName());

            pluginAuthRepository.saveAndFlush(entity);
        }
    }

    private void performImportSystemParametersForOnePackage(PluginPackage pluginPackage,
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

            SystemVariable sysVarEntity = null;
            List<SystemVariable> existSysVars = systemVariableRepository
                    .findByNameAndScopeAndSource(xmlSysVar.getName(), xmlSysVar.getScopeType(), pluginPackage.getId());
            if (existSysVars != null && !existSysVars.isEmpty()) {
                sysVarEntity = existSysVars.get(0);
            }

            if (sysVarEntity == null) {
                sysVarEntity = new SystemVariable();
                sysVarEntity.setDefaultValue(xmlSysVar.getDefaultValue());
                sysVarEntity.setPackageName(xmlSysVar.getPackageName());
                sysVarEntity.setName(xmlSysVar.getName());
                sysVarEntity.setScope(xmlSysVar.getScopeType());
                sysVarEntity.setSource(pluginPackage.getId());
                sysVarEntity.setStatus(xmlSysVar.getStatus());
                sysVarEntity.setValue(xmlSysVar.getValue());
            } else {
                sysVarEntity.setDefaultValue(xmlSysVar.getDefaultValue());
                sysVarEntity.setValue(xmlSysVar.getValue());
                sysVarEntity.setStatus(xmlSysVar.getStatus());
            }

            systemVariableRepository.saveAndFlush(sysVarEntity);
        }
    }

    private void performImportPluginRegistersForOnePackage(PluginPackage pluginPackage,
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

        Map<String, PluginConfig> regNamedPluginConfigDefs = pickoutPluginConfigDefinitions(pluginPackage);
        if (log.isDebugEnabled()) {
            log.debug("total {} plugin config declarations found.", regNamedPluginConfigDefs.size());
        }

        for (PluginConfigType xmlPluginConfig : xmlPluginConfigList) {
            if (StringUtils.isBlank(xmlPluginConfig.getName())) {
                throw new WecubeCoreException("3233","Plugin config name cannot be blank.");
            }

            if (StringUtils.isBlank(xmlPluginConfig.getRegisterName())) {
                throw new WecubeCoreException("3234",String.format("Register name is blank for %s" , xmlPluginConfig.getName()), xmlPluginConfig.getName());
            }

            PluginConfig pluginConfigDef = regNamedPluginConfigDefs.get(xmlPluginConfig.getName());
            handlePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        pluginPackageRepository.saveAndFlush(pluginPackage);

        log.info("finished importing plugin registries for {} {} from {} {}", pluginPackage.getName(),
                pluginPackage.getVersion(), xmlPluginPackage.getName(), xmlPluginPackage.getVersion());
    }

    private String buildXmlComments(PluginPackage pluginPackage) {
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

    private Map<String, PluginConfig> pickoutPluginConfigDefinitions(PluginPackage pluginPackage) {
        Set<PluginConfig> dbPluginConfigs = pluginPackage.getPluginConfigs();
        Map<String, PluginConfig> pluginConfigDefs = new HashMap<String, PluginConfig>();
        if (dbPluginConfigs == null || dbPluginConfigs.isEmpty()) {
            return pluginConfigDefs;
        }

        for (PluginConfig pc : dbPluginConfigs) {
            if (StringUtils.isBlank(pc.getRegisterName())) {
                pluginConfigDefs.put(pc.getName(), pc);
            }
        }

        return pluginConfigDefs;
    }

    private PluginConfig tryUpdatePluginConfig(PluginPackage pluginPackage, PluginConfig toUpdatePluginConfig,
            PluginConfigType xmlPluginConfig, PluginConfig pluginConfigDef) {
        toUpdatePluginConfig.setTargetEntity(xmlPluginConfig.getTargetEntity());
        toUpdatePluginConfig.setTargetEntityFilterRule(xmlPluginConfig.getTargetEntityFilterRule());
        toUpdatePluginConfig.setTargetPackage(xmlPluginConfig.getTargetPackage());

        List<PluginConfigInterfaceType> xmlIntfList = xmlPluginConfig.getPluginInterface();
        if (xmlIntfList == null || xmlIntfList.isEmpty()) {
            return toUpdatePluginConfig;
        }

        Set<PluginConfigInterface> toUpdateInterfaces = toUpdatePluginConfig.getInterfaces();

        for (PluginConfigInterfaceType xmlIntf : xmlIntfList) {
            if (StringUtils.isBlank(xmlIntf.getAction())) {
                throw new WecubeCoreException("3235","Action of interface cannot be blank.");
            }
            PluginConfigInterface toUpdateIntf = pickoutPluginConfigInterface(toUpdateInterfaces, xmlIntf.getAction(),
                    xmlIntf.getPath());
            if (toUpdateIntf == null) {
                log.debug("interface doesnot exist and try to create one,{} {}", toUpdatePluginConfig.getId(),
                        xmlIntf.getAction());
                PluginConfigInterface defIntf = pickoutDefPluginConfigInterface(pluginConfigDef, xmlIntf.getPath());
                if (defIntf != null) {
                    PluginConfigInterface newIntf = tryCreatePluginConfigInterface(toUpdatePluginConfig, xmlIntf,
                            defIntf);
                    toUpdatePluginConfig.addPluginConfigInterface(newIntf);
                }
            } else {
                log.debug("interface exists and try to update,{} {}", toUpdatePluginConfig.getId(),
                        xmlIntf.getAction());
                tryUpdatePluginConfigInterface(toUpdatePluginConfig, toUpdateIntf, xmlIntf);
            }
        }

        toUpdatePluginConfig = pluginConfigRepository.saveAndFlush(toUpdatePluginConfig);
        log.debug("plugin config updated : {} {} {} {}", toUpdatePluginConfig.getId(),
                toUpdatePluginConfig.getTargetEntity(), toUpdatePluginConfig.getTargetEntityFilterRule(),
                toUpdatePluginConfig.getTargetPackage());
        //
        tryUpdatePluginConfigRoleBinds(xmlPluginConfig, pluginConfigDef, toUpdatePluginConfig);
        return toUpdatePluginConfig;
    }

    private void tryUpdatePluginConfigRoleBinds(PluginConfigType xmlPluginConfig, PluginConfig pluginConfigDef,
            PluginConfig toUpdatePluginConfig) {
        PluginRoleBindingsType xmlRoleBinds = xmlPluginConfig.getRoleBinds();
        if (xmlRoleBinds == null || xmlRoleBinds.getRoleBind().isEmpty()) {
            return;
        }

        String pluginConfigId = toUpdatePluginConfig.getId();
        for (PluginRoleBindingType xmlRoleBind : xmlRoleBinds.getRoleBind()) {
            List<PluginAuthEntity> existsEntities = pluginAuthRepository
                    .findAllByPluginConfigIdAndPermissionAndRoleName(pluginConfigId, xmlRoleBind.getPermission(),
                            xmlRoleBind.getRoleName());

            if (existsEntities != null && !existsEntities.isEmpty()) {
                continue;
            }

            PluginAuthEntity entity = new PluginAuthEntity();
            entity.setActive(true);
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());
            entity.setPermissionType(xmlRoleBind.getPermission());
            entity.setPluginConfigId(pluginConfigId);
            RoleDto roleDto = fetchRoleWithRoleName(xmlRoleBind.getRoleName());
            if (roleDto != null) {
                entity.setRoleId(roleDto.getId());
            }
            entity.setRoleName(xmlRoleBind.getRoleName());

            pluginAuthRepository.saveAndFlush(entity);
        }
    }

    private PluginConfigInterface pickoutPluginConfigInterface(Set<PluginConfigInterface> toUpdateInterfaces,
            String action, String path) {
        if (toUpdateInterfaces == null) {
            return null;
        }

        for (PluginConfigInterface intf : toUpdateInterfaces) {
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

    private PluginConfigInterface tryCreatePluginConfigInterface(PluginConfig pluginConfig,
            PluginConfigInterfaceType xmlIntf, PluginConfigInterface defIntf) {
        PluginConfigInterface intf = new PluginConfigInterface();
        intf.setPluginConfig(pluginConfig);
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

        Set<PluginConfigInterfaceParameter> inputParameters = new HashSet<>();
        Set<PluginConfigInterfaceParameter> defInputParameters = defIntf.getInlineInputParameters();

        if (defInputParameters != null) {
            for (PluginConfigInterfaceParameter defInputParam : defInputParameters) {
                PluginConfigInputParameterType xmlInputParam = pickoutPluginConfigInputParameterType(xmlIntf,
                        defInputParam.getName());
                PluginConfigInterfaceParameter inputParam = tryCreateInputParameter(intf, xmlInputParam, defInputParam);
                inputParameters.add(inputParam);
            }
        }

        intf.setInputParameters(inputParameters);

        Set<PluginConfigInterfaceParameter> defOutputParameters = defIntf.getInlineOutputParameters();
        Set<PluginConfigInterfaceParameter> outputParameters = new HashSet<>();

        if (defOutputParameters != null) {
            for (PluginConfigInterfaceParameter defOutputParam : defOutputParameters) {
                PluginConfigOutputParameterType xmlOutputParam = pickoutPluginConfigOutputParameterType(xmlIntf,
                        defOutputParam.getName());
                PluginConfigInterfaceParameter outputParam = tryCreateOutputParameter(intf, xmlOutputParam,
                        defOutputParam);
                outputParameters.add(outputParam);
            }
        }

        intf.setOutputParameters(outputParameters);

        intf.setServiceDisplayName(intf.generateServiceName());
        intf.setServiceName(intf.generateServiceName());

        return intf;
    }

    private PluginConfigInterfaceParameter tryCreateInputParameter(PluginConfigInterface intf,
            PluginConfigInputParameterType xmlInputParam, PluginConfigInterfaceParameter defInputParam) {
        PluginConfigInterfaceParameter param = new PluginConfigInterfaceParameter();

        param.setName(defInputParam.getName());
        param.setDataType(defInputParam.getDataType());
        param.setType(PluginConfigInterfaceParameter.TYPE_INPUT);
        param.setPluginConfigInterface(intf);
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

        return param;

    }

    private PluginConfigInterfaceParameter tryCreateOutputParameter(PluginConfigInterface intf,
            PluginConfigOutputParameterType xmlOutputParam, PluginConfigInterfaceParameter defOutputParam) {
        PluginConfigInterfaceParameter param = new PluginConfigInterfaceParameter();
        param.setName(defOutputParam.getName());
        param.setDataType(defOutputParam.getDataType());
        param.setType(PluginConfigInterfaceParameter.TYPE_OUTPUT);
        param.setPluginConfigInterface(intf);

        if (xmlOutputParam != null) {
            param.setMappingEntityExpression(xmlOutputParam.getMappingEntityExpression());
            param.setMappingType(xmlOutputParam.getMappingType());
            param.setSensitiveData(xmlOutputParam.getSensitiveData());
        } else {
            param.setMappingEntityExpression(defOutputParam.getMappingEntityExpression());
            param.setMappingType(defOutputParam.getMappingType());
            param.setSensitiveData(defOutputParam.getSensitiveData());
        }

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

    private void handlePluginConfig(PluginPackage pluginPackage, PluginConfigType xmlPluginConfig,
            PluginConfig pluginConfigDef) {

        if (log.isDebugEnabled()) {
            log.debug("start to handle plugin config {} {}", xmlPluginConfig.getName(),
                    xmlPluginConfig.getRegisterName());
        }

        if (pluginConfigDef == null) {
            log.info("There is no plugin definition found for {} {} {}", pluginPackage.getId(),
                    xmlPluginConfig.getName(), xmlPluginConfig.getRegisterName());
            return;
        }

        Set<PluginConfig> pluginConfigs = pluginPackage.getPluginConfigs();

        PluginConfig existPluginConfig = pickoutPluginConfigWithRegisterName(pluginConfigs, xmlPluginConfig.getName(),
                xmlPluginConfig.getRegisterName());

        PluginConfig pc = null;
        if (existPluginConfig != null) {
            log.debug("such plugin config already exist and try to update,{} {}", pluginPackage.getId(),
                    existPluginConfig.getRegisterName());
            pc = tryUpdatePluginConfig(pluginPackage, existPluginConfig, xmlPluginConfig, pluginConfigDef);
        } else {
            log.debug("try to create a new plugin config for {} {}", pluginPackage.getId(),
                    xmlPluginConfig.getRegisterName());
            pc = tryCreatePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        if (pc != null) {
            pluginPackage.addPluginConfig(pc);
        }

    }

    private PluginConfigInterface pickoutDefPluginConfigInterface(PluginConfig pluginConfigDef, String path) {
        Set<PluginConfigInterface> defIntfs = pluginConfigDef.getInterfaces();
        if (defIntfs == null || defIntfs.isEmpty()) {
            return null;
        }

        for (PluginConfigInterface defIntf : defIntfs) {
            if (path.equals(defIntf.getPath())) {
                return defIntf;
            }
        }

        return null;
    }

    private PluginConfig pickoutPluginConfigWithRegisterName(Set<PluginConfig> pluginConfigs, String name,
            String registerName) {
        if (pluginConfigs == null || pluginConfigs.isEmpty()) {
            return null;
        }

        for (PluginConfig pc : pluginConfigs) {
            if (registerName.equals(pc.getRegisterName()) && name.equals(pc.getName())) {
                return pc;
            }
        }

        return null;
    }

}
