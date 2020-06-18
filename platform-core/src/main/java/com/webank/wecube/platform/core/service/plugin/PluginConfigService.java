package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.entity;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.system_variable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.dto.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.TargetEntityFilterRuleDto;
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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

    public String exportPluginRegistersForOnePackage(String pluginPackageId) {
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

        List<PluginConfig> pluginConfigs = pluginConfigRepository.findByPluginPackage_idOrderByName(pluginPackageId)
                .get();

        PluginConfigsType xmlPluginConfigs = buildXmlPluginConfigs(pluginPackage, pluginConfigs);
        xmlPluginPackage.setPlugins(xmlPluginConfigs);

        String xmlContent = JaxbUtils.convertToXml(xmlPluginPackage);

        return xmlContent;
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

        return xmlParam;
    }

    private PluginConfigInputParameterType buildXmlInputParameter(PluginConfigInterfaceParameter inputParameter) {
        PluginConfigInputParameterType xmlParam = new PluginConfigInputParameterType();
        xmlParam.setDataType(inputParameter.getDataType());
        xmlParam.setMappingEntityExpression(inputParameter.getMappingEntityExpression());
        xmlParam.setMappingSystemVariableName(inputParameter.getMappingSystemVariableName());
        xmlParam.setMappingType(inputParameter.getMappingType());
        xmlParam.setRequired(inputParameter.getRequired());
        xmlParam.setSensitiveData(inputParameter.getSensitiveData());
        xmlParam.setValue(inputParameter.getName());

        return xmlParam;
    }

    public void importPluginRegistersForOnePackage(String pluginPackageId, String registersAsXml) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("Plugin package ID cannot be blank.");
        }

        if (StringUtils.isBlank(registersAsXml)) {
            throw new WecubeCoreException("XML data is blank.");
        }

        // TODO

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

        return PluginConfigDto.fromDomain(savedPluginConfig);
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
}
