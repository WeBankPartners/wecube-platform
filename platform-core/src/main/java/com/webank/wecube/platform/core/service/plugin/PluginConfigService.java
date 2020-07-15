package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.entity;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.system_variable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.dto.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.PluginConfigRoleRequestDto;
import com.webank.wecube.platform.core.dto.TargetEntityFilterRuleDto;
import com.webank.wecube.platform.core.jpa.PluginAuthRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInputParameterType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInputParametersType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigInterfaceType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigOutputParameterType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigOutputParametersType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginConfigsType;
import com.webank.wecube.platform.core.service.plugin.xmltype.PluginPackageType;
import com.webank.wecube.platform.core.utils.JaxbUtils;

@Service
@Transactional
public class PluginConfigService {
    private final static Logger log = LoggerFactory.getLogger(PluginConfigService.class);

    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    private PluginConfigRepository pluginConfigRepository;
    @Autowired
    private PluginConfigInterfaceRepository pluginConfigInterfaceRepository;
    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    private PluginPackageDataModelRepository dataModelRepository;
    
    @Autowired
    private PluginAuthRepository pluginAuthRepository;

    public PluginRegistryInfo exportPluginRegistersForOnePackage(String pluginPackageId) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("Plugin package ID cannot be blank.");
        }

        Optional<PluginPackage> pluginPackageEntityOpt = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageEntityOpt.isPresent()) {
            throw new WecubeCoreException("Bad plugin package ID,such package does not exist.");
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
            throw new WecubeCoreException("Such package ID has no plugin configs.");
        }

        List<PluginConfig> pluginConfigs = pluginConfigsOpt.get();

        PluginConfigsType xmlPluginConfigs = buildXmlPluginConfigs(pluginPackage, pluginConfigs);
        xmlPluginPackage.setPlugins(xmlPluginConfigs);

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
            throw new WecubeCoreException("Plugin package ID cannot be blank.");
        }

        if (StringUtils.isBlank(registersAsXml)) {
            throw new WecubeCoreException("XML data is blank.");
        }

        if (log.isDebugEnabled()) {
            log.debug("IMPORT:{}", registersAsXml);
        }

        Optional<PluginPackage> pluginPackageEntityOpt = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageEntityOpt.isPresent()) {
            throw new WecubeCoreException("Bad plugin package ID,such package does not exist.");
        }

        PluginPackage pluginPackage = pluginPackageEntityOpt.get();

        PluginPackageType xmlPluginPackage = JaxbUtils.convertToObject(registersAsXml, PluginPackageType.class);

        if (xmlPluginPackage == null) {
            throw new WecubeCoreException("Bad xml contents.");
        }

        performImportPluginRegistersForOnePackage(pluginPackage, xmlPluginPackage);
    }

    public List<PluginConfigInterface> getPluginConfigInterfaces(String pluginConfigId) {
        return pluginConfigRepository.findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfigId);
    }

    public PluginConfigDto savePluginConfig(PluginConfigDto pluginConfigDto) throws WecubeCoreException {
        if (pluginConfigDto.getId() == null) {
            return createPluginConfig(pluginConfigDto);
        }
        return updatePluginConfig(pluginConfigDto);
    }

    public PluginConfigDto createPluginConfig(PluginConfigDto pluginConfigDto) throws WecubeCoreException {
        String packageId = pluginConfigDto.getPluginPackageId();
        PluginPackage pluginPackage = pluginPackageRepository.findById(packageId).get();

        ensurePluginConfigRegisterNameNotExisted(pluginConfigDto);
        PluginConfig pluginConfig = pluginConfigDto.toDomain(pluginPackage);
        ensurePluginConfigIdNotExisted(pluginConfig);

        pluginConfig.setStatus(DISABLED);
        PluginConfig savedPluginConfig = pluginConfigRepository.save(pluginConfig);
        
        //store permission and roles

        return PluginConfigDto.fromDomain(savedPluginConfig);
    }
    
    @SuppressWarnings("unused")
    public void updateProcRoleBinding(String procId, PluginConfigRoleRequestDto pluginConfigRoleRequestDto) throws WecubeCoreException {
//        String permissionStr = pluginConfigRoleRequestDto.getPermission();
//        List<String> roleIdList = pluginConfigRoleRequestDto.getRoles();
//        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
//
//        // check if user's roles has permission to manage this process
//        checkPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);
//        batchSaveData(procId, roleIdList, permissionStr);
    }

    public void deleteProcRoleBinding(String procId, PluginConfigRoleRequestDto pluginConfigRoleRequestDto) throws WecubeCoreException {
//        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(
//                pluginConfigRoleRequestDto.getPermission());

        // check if the current user has the role to manage such process
//        checkPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);
//
//        // assure corresponding data has at least one row of MGMT permission
//        if (ProcRoleBindingEntity.permissionEnum.MGMT.equals(permissionEnum)) {
//            Optional<List<ProcRoleBindingEntity>> foundMgmtData = this.procRoleBindingRepository
//                    .findAllByProcIdAndPermission(procId, permissionEnum);
//            foundMgmtData.ifPresent(procRoleBindingEntities -> {
//                if (procRoleBindingEntities.size() <= pluginConfigRoleRequestDto.getRoleIdList().size()) {
//                    String msg = "The process's management permission should have at least one role.";
//                    logger.info(String.format(
//                            "The DELETE management roles operation was blocked, the process id is [%s].", procId));
//                    throw new WecubeCoreException(msg);
//                }
//            });
//        }
//
//        for (String roleId : procRoleRequestDto.getRoleIdList()) {
//            this.procRoleBindingRepository.deleteByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);
//        }
    }

    private void ensurePluginConfigIdNotExisted(PluginConfig pluginConfig) {
        pluginConfig.initId();
        if (pluginConfigRepository.existsById(pluginConfig.getId())) {
            throw new WecubeCoreException(String.format("PluginConfig[%s] already exist", pluginConfig.getId()));
        }
    }

    private void ensurePluginConfigRegisterNameNotExisted(PluginConfigDto pluginConfigDto) {
        if (pluginConfigRepository.existsByPluginPackage_idAndNameAndRegisterName(pluginConfigDto.getPluginPackageId(),
                pluginConfigDto.getName(), pluginConfigDto.getRegisterName())) {
            throw new WecubeCoreException(
                    String.format("PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                            pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                            pluginConfigDto.getRegisterName()));
        }
    }

    private void ensurePluginConfigUnique(PluginConfigDto pluginConfigDto) {
        Optional<List<PluginConfig>> existedPluginConfigListOptional = pluginConfigRepository
                .findAllByPluginPackage_idAndNameAndRegisterName(pluginConfigDto.getPluginPackageId(),
                        pluginConfigDto.getName(), pluginConfigDto.getRegisterName());
        if (existedPluginConfigListOptional.isPresent()) {
            List<PluginConfig> existedPluginConfigList = existedPluginConfigListOptional.get();
            existedPluginConfigList.forEach(existedPluginConfig -> {
                if (!existedPluginConfig.getId().equals(pluginConfigDto.getId())) {
                    throw new WecubeCoreException(
                            String.format("PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                                    pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                                    pluginConfigDto.getRegisterName()));
                }
            });
        }
    }

    public PluginConfigDto updatePluginConfig(PluginConfigDto pluginConfigDto) throws WecubeCoreException {
        ensurePluginConfigIsValid(pluginConfigDto);
        String packageId = pluginConfigDto.getPluginPackageId();
        PluginPackage pluginPackage = pluginPackageRepository.findById(packageId).get();

        PluginConfig pluginConfig = pluginConfigDto.toDomain(pluginPackage);
        PluginConfig pluginConfigFromDatabase = pluginConfigRepository.findById(pluginConfigDto.getId()).get();
        if (ENABLED == pluginConfigFromDatabase.getStatus()) {
            throw new WecubeCoreException("Not allow to update plugin with status: ENABLED");
        }
        pluginConfig.setStatus(DISABLED);

        PluginConfig savedPluginConfig = pluginConfigRepository.save(pluginConfig);
        return PluginConfigDto.fromDomain(savedPluginConfig);
    }

    private void ensurePluginConfigIsValid(PluginConfigDto pluginConfigDto) {
        if (StringUtils.isBlank(pluginConfigDto.getPluginPackageId())
                || !pluginPackageRepository.existsById(pluginConfigDto.getPluginPackageId())) {
            throw new WecubeCoreException(String.format("Cannot find PluginPackage with id=%s in PluginConfig",
                    pluginConfigDto.getPluginPackageId()));
        }
        if (StringUtils.isBlank(pluginConfigDto.getId())) {
            throw new WecubeCoreException("Invalid pluginConfig with id: " + pluginConfigDto.getId());
        }

        if (!pluginConfigRepository.existsById(pluginConfigDto.getId())) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfigDto.getId());
        }
        ensurePluginConfigUnique(pluginConfigDto);

        ensureEntityIsValid(pluginConfigDto.getName(), pluginConfigDto.getTargetPackage(),
                pluginConfigDto.getTargetEntity());
    }

    private void ensureEntityIsValid(String pluginConfigName, String targetPackage, String targetEntity) {
        if (StringUtils.isNotBlank(targetPackage) && StringUtils.isNotBlank(targetEntity)) {
            Optional<PluginPackageDataModel> dataModelOptional = dataModelRepository
                    .findLatestDataModelByPackageName(targetPackage);
            if (!dataModelOptional.isPresent()) {
                throw new WecubeCoreException("Data model not exists for package name [%s]");
            }

            Integer dataModelVersion = dataModelOptional.get().getVersion();
            if (!pluginPackageEntityRepository.existsByPackageNameAndNameAndDataModelVersion(targetPackage,
                    targetEntity, dataModelVersion)) {
                String errorMessage = String.format(
                        "PluginPackageEntity not found for packageName:dataModelVersion:entityName [%s:%s:%s] for plugin config: %s",
                        targetPackage, dataModelVersion, targetEntity, pluginConfigName);
                log.error(errorMessage);
                throw new WecubeCoreException(errorMessage);
            }
        }
    }

    public PluginConfigDto enablePlugin(String pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfigId);
        }

        PluginConfig pluginConfig = pluginConfigRepository.findById(pluginConfigId).get();

        if (pluginConfig.getPluginPackage() == null || UNREGISTERED == pluginConfig.getPluginPackage().getStatus()
                || DECOMMISSIONED == pluginConfig.getPluginPackage().getStatus()) {
            throw new WecubeCoreException(
                    "Plugin package is not in valid status [REGISTERED, RUNNING, STOPPED] to enable plugin.");
        }

        if (ENABLED == pluginConfig.getStatus()) {
            throw new WecubeCoreException("Not allow to enable pluginConfig with status: ENABLED");
        }

        ensureEntityIsValid(pluginConfig.getName(), pluginConfig.getTargetPackage(), pluginConfig.getTargetEntity());

        checkMandatoryParameters(pluginConfig);

        pluginConfig.setStatus(ENABLED);
        return PluginConfigDto.fromDomain(pluginConfigRepository.save(pluginConfig));
    }

    private void checkMandatoryParameters(PluginConfig pluginConfig) {
        Set<PluginConfigInterface> interfaces = pluginConfig.getInterfaces();
        if (null != interfaces && interfaces.size() > 0) {
            interfaces.forEach(intf -> {
                Set<PluginConfigInterfaceParameter> inputParameters = intf.getInputParameters();
                if (null != inputParameters && inputParameters.size() > 0) {
                    inputParameters.forEach(inputParameter -> {
                        if ("Y".equalsIgnoreCase(inputParameter.getRequired())) {
                            if (system_variable.name().equals(inputParameter.getMappingType())
                                    && inputParameter.getMappingSystemVariableName() == null) {
                                throw new WecubeCoreException(String.format(
                                        "System variable is required for parameter [%s]", inputParameter.getId()));
                            }
                            if (entity.name().equals(inputParameter.getMappingType())
                                    && StringUtils.isBlank(inputParameter.getMappingEntityExpression())) {
                                throw new WecubeCoreException(String.format(
                                        "Entity expression is required for parameter [%s]", inputParameter.getId()));
                            }
                        }
                    });
                }
                Set<PluginConfigInterfaceParameter> outputParameters = intf.getOutputParameters();
                if (null != outputParameters && outputParameters.size() > 0) {
                    outputParameters.forEach(outputParameter -> {
                        if ("Y".equalsIgnoreCase(outputParameter.getRequired())) {
                            if (entity.name().equals(outputParameter.getMappingType())
                                    && StringUtils.isBlank(outputParameter.getMappingEntityExpression())) {
                                throw new WecubeCoreException(String.format(
                                        "Entity expression is required for parameter [%s]", outputParameter.getId()));
                            }
                        }
                    });
                }
            });
        }
    }

    public PluginConfigDto disablePlugin(String pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfigId);
        }

        PluginConfig pluginConfig = pluginConfigRepository.findById(pluginConfigId).get();

        pluginConfig.setStatus(DISABLED);
        return PluginConfigDto.fromDomain(pluginConfigRepository.save(pluginConfig));
    }

    public PluginConfigInterface getPluginConfigInterfaceByServiceName(String serviceName) {
        Optional<PluginConfigInterface> pluginConfigInterface = pluginConfigRepository
                .findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(serviceName);
        if (!pluginConfigInterface.isPresent()) {
            throw new WecubeCoreException(
                    String.format("Plugin interface not found for serviceName [%s].", serviceName));
        }
        return pluginConfigInterface.get();
    }

    public List<PluginConfigInterfaceDto> queryAllLatestEnabledPluginConfigInterface() {
        Optional<List<PluginConfigInterface>> pluginConfigsOptional = pluginConfigRepository
                .findAllLatestEnabledForAllActivePackages();
        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = newArrayList();
        if (pluginConfigsOptional.isPresent()) {
            List<PluginConfigInterface> pluginConfigInterfaces = pluginConfigsOptional.get();
            pluginConfigInterfaces.forEach(pluginConfigInterface -> pluginConfigInterfaceDtos
                    .add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        return pluginConfigInterfaceDtos;
    }
    
    public List<PluginConfigInterfaceDto> queryAllEnabledPluginConfigInterfaceForEntityByFilterRule(
            TargetEntityFilterRuleDto filterRuleDto) {
        return distinctPluginConfigInfDto(queryAllEnabledPluginConfigInterfaceForEntity(filterRuleDto.getPkgName(), filterRuleDto.getEntityName(),
                filterRuleDto));
    }

    @SuppressWarnings("unchecked")
    public List<PluginConfigInterfaceDto> queryAllEnabledPluginConfigInterfaceForEntity(String packageName,
            String entityName, TargetEntityFilterRuleDto filterRuleDto) {
        Optional<PluginPackageDataModel> dataModelOptional = dataModelRepository
                .findLatestDataModelByPackageName(packageName);
        if (!dataModelOptional.isPresent()) {
            log.info("No data model found for package [{}]", packageName);
            return Collections.EMPTY_LIST;
        }
        Set<PluginPackageEntity> pluginPackageEntities = dataModelOptional.get().getPluginPackageEntities();
        if (null != pluginPackageEntities && pluginPackageEntities.size() > 0) {
            if (!pluginPackageEntities.stream().filter(entity -> entity.getName().equals(entityName)).findAny()
                    .isPresent()) {
                log.info("No entity found with name [{}}] for package [{}}]", entityName, packageName);
                return Collections.EMPTY_LIST;
            }
        }

        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = newArrayList();
        if (filterRuleDto == null) {
            Optional<List<PluginConfigInterface>> allEnabledInterfacesOptional = pluginConfigInterfaceRepository
                    .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_Status(
                            packageName, entityName, ENABLED);
            if (allEnabledInterfacesOptional.isPresent()) {
                pluginConfigInterfaceDtos.addAll(allEnabledInterfacesOptional.get().stream()
                        .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                        .collect(Collectors.toList()));
            }
        } else {
            if (filterRuleDto.getTargetEntityFilterRule() == null
                    || filterRuleDto.getTargetEntityFilterRule().isEmpty()) {
                Optional<List<PluginConfigInterface>> filterRuleIsNullEnabledInterfacesOptional = pluginConfigInterfaceRepository
                        .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_StatusAndPluginConfig_TargetEntityFilterRuleIsNull(
                                packageName, entityName, ENABLED);
                if (filterRuleIsNullEnabledInterfacesOptional.isPresent()) {
                    pluginConfigInterfaceDtos.addAll(filterRuleIsNullEnabledInterfacesOptional.get().stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
                Optional<List<PluginConfigInterface>> filterRuleIsEmptyEnabledInterfacesOptional = pluginConfigInterfaceRepository
                        .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_TargetEntityFilterRuleAndPluginConfig_Status(
                                packageName, entityName, "", ENABLED);
                if (filterRuleIsEmptyEnabledInterfacesOptional.isPresent()) {
                    pluginConfigInterfaceDtos.addAll(filterRuleIsEmptyEnabledInterfacesOptional.get().stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
            } else {
                Optional<List<PluginConfigInterface>> allEnabledInterfacesOptional = pluginConfigInterfaceRepository
                        .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_TargetEntityFilterRuleAndPluginConfig_Status(
                                packageName, entityName, filterRuleDto.getTargetEntityFilterRule(), ENABLED);
                if (allEnabledInterfacesOptional.isPresent()) {
                    pluginConfigInterfaceDtos.addAll(allEnabledInterfacesOptional.get().stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
            }
        }

        Optional<List<PluginConfigInterface>> allEnabledWithEntityNameNullOptional = pluginConfigInterfaceRepository
                .findAllEnabledWithEntityNameNull();
        if (allEnabledWithEntityNameNullOptional.isPresent()) {
            pluginConfigInterfaceDtos.addAll(allEnabledWithEntityNameNullOptional.get().stream()
                    .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                    .collect(Collectors.toList()));
        }

        return pluginConfigInterfaceDtos;
    }

    @SuppressWarnings("unchecked")
    public List<PluginConfigInterfaceDto> distinctPluginConfigInfDto(List<PluginConfigInterfaceDto> dto) {
        dto = dto.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(PluginConfigInterfaceDto::getServiceName))),
                        ArrayList::new));
        return dto;
    }

    public void disableAllPluginsForPluginPackage(String pluginPackageId) {
        Optional<List<PluginConfig>> pluginConfigsOptional = pluginConfigRepository
                .findByPluginPackage_idOrderByName(pluginPackageId);
        if (pluginConfigsOptional.isPresent()) {
            List<PluginConfig> pluginConfigs = pluginConfigsOptional.get();
            pluginConfigs.forEach(pluginConfig -> pluginConfig.setStatus(DISABLED));
            pluginConfigRepository.saveAll(pluginConfigs);
        }
    }

    public List<PluginConfigInterfaceDto> queryPluginConfigInterfaceByConfigId(String configId) {
        Optional<List<PluginConfigInterface>> pluginConfigsOptional = pluginConfigInterfaceRepository
                .findAllByPluginConfig_Id(configId);
        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = newArrayList();
        if (pluginConfigsOptional.isPresent()) {
            List<PluginConfigInterface> pluginConfigInterfaces = pluginConfigsOptional.get();
            pluginConfigInterfaces.forEach(pluginConfigInterface -> pluginConfigInterfaceDtos
                    .add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        return pluginConfigInterfaceDtos;
    }

    public void deletePluginConfigById(String configId) {
        Optional<PluginConfig> cfgOptional = pluginConfigRepository.findById(configId);
        if (cfgOptional.isPresent()) {
            PluginConfig cfg = cfgOptional.get();
            if (!cfg.getStatus().equals(PluginConfig.Status.DISABLED)) {
                throw new WecubeCoreException(
                        String.format("Can not delete [%s] status PluginConfig", cfg.getStatus()));
            }
            PluginPackage pkg = cfg.getPluginPackage();
            pkg.getPluginConfigs().remove(cfg);
            pluginPackageRepository.save(pkg);
        } else {
            throw new WecubeCoreException(String.format("Can not found PluginConfig[%s]", configId));
        }
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

        return xmlPluginConfig;

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
                throw new WecubeCoreException("Action of interface cannot be blank.");
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
        return toUpdatePluginConfig;
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
        return savedPluginConfig;

    }

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

        Map<String, PluginConfig> pluginConfigDefs = pickoutPluginConfigDefinitions(pluginPackage);
        if (log.isDebugEnabled()) {
            log.debug("total {} plugin config declarations found.", pluginConfigDefs.size());
        }

        for (PluginConfigType xmlPluginConfig : xmlPluginConfigList) {
            if (StringUtils.isBlank(xmlPluginConfig.getName())) {
                throw new WecubeCoreException("Plugin config name cannot be blank.");
            }

            if (StringUtils.isBlank(xmlPluginConfig.getRegisterName())) {
                throw new WecubeCoreException("Register name is blank for " + xmlPluginConfig.getName());
            }

            PluginConfig pluginConfigDef = pluginConfigDefs.get(xmlPluginConfig.getName());
            handlePluginConfig(pluginPackage, xmlPluginConfig, pluginConfigDef);
        }

        pluginPackageRepository.saveAndFlush(pluginPackage);

        log.info("finished importing plugin registries for {} {} from {} {}", pluginPackage.getName(),
                pluginPackage.getVersion(), xmlPluginPackage.getName(), xmlPluginPackage.getVersion());
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
}
