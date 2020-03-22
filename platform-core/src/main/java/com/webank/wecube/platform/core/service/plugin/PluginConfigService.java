package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.dto.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.TargetEntityFilterRuleDto;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

import com.webank.wecube.platform.core.jpa.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.*;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.entity;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.system_variable;

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

    private void ensurePluginConfigIsValid(PluginConfigDto pluginConfig) {
        if (StringUtils.isBlank(pluginConfig.getPluginPackageId())
                || !pluginPackageRepository.existsById(pluginConfig.getPluginPackageId())) {
            throw new WecubeCoreException(String.format("Cannot find PluginPackage with id=%s in PluginConfig",
                    pluginConfig.getPluginPackageId()));
        }
        if (StringUtils.isBlank(pluginConfig.getId())) {
            throw new WecubeCoreException("Invalid pluginConfig with id: " + pluginConfig.getId());
        }

        if (!pluginConfigRepository.existsById(pluginConfig.getId())) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfig.getId());
        }

        ensureEntityIsValid(pluginConfig.getName(), pluginConfig.getTargetPackage(), pluginConfig.getTargetEntity());
    }

    private void ensureEntityIsValid(String pluginConfigName, String targetPackage, String targetEntity) {
        if (StringUtils.isNotBlank(targetPackage) && StringUtils.isNotBlank(targetEntity)) {
            Optional<PluginPackageDataModel> dataModelOptional = dataModelRepository.findLatestDataModelByPackageName(targetPackage);
            if (!dataModelOptional.isPresent()){
                throw new WecubeCoreException("Data model not exists for package name [%s]");
            }

            Integer dataModelVersion = dataModelOptional.get().getVersion();
            if (!pluginPackageEntityRepository.existsByPackageNameAndNameAndDataModelVersion(targetPackage, targetEntity, dataModelVersion)) {
                String errorMessage = String.format("PluginPackageEntity not found for packageName:dataModelVersion:entityName [%s:%s:%s] for plugin config: %s",
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

    public List<PluginConfigInterfaceDto> queryAllEnabledPluginConfigInterfaceForEntity(String packageName,
            String entityName, TargetEntityFilterRuleDto filterRule) {
        Optional<PluginPackageDataModel> dataModelOptional = dataModelRepository.findLatestDataModelByPackageName(packageName);
        if (!dataModelOptional.isPresent()) {
            log.info("No data model found for package [{}]", packageName);
            return Collections.EMPTY_LIST;
        }
        Set<PluginPackageEntity> pluginPackageEntities = dataModelOptional.get().getPluginPackageEntities();
        if (null != pluginPackageEntities && pluginPackageEntities.size() > 0) {
            if (!pluginPackageEntities.stream().filter(entity -> entity.getName().equals(entityName)).findAny().isPresent()) {
                log.info("No entity found with name [{}}] for package [{}}]", entityName, packageName);
                return Collections.EMPTY_LIST;
            }
        }

        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = newArrayList();
        if (filterRule == null) {
            Optional<List<PluginConfigInterface>> allEnabledInterfacesOptional = pluginConfigInterfaceRepository
                    .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_Status(
                            packageName, entityName, ENABLED);
            if (allEnabledInterfacesOptional.isPresent()) {
                pluginConfigInterfaceDtos.addAll(allEnabledInterfacesOptional.get().stream()
                        .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                        .collect(Collectors.toList()));
            }
        } else {
            Optional<List<PluginConfigInterface>> allEnabledInterfacesOptional = pluginConfigInterfaceRepository
                    .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_TargetEntityFilterRuleAndPluginConfig_Status(
                            packageName, entityName, filterRule.getTargetEntityFilterRule(), ENABLED);
            if (allEnabledInterfacesOptional.isPresent()) {
                pluginConfigInterfaceDtos.addAll(allEnabledInterfacesOptional.get().stream()
                        .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                        .collect(Collectors.toList()));
            }
        }
        Optional<List<PluginConfigInterface>> allEnabledWithEntityNameNullOptional = pluginConfigInterfaceRepository.findAllEnabledWithEntityNameNull();
        if (allEnabledWithEntityNameNullOptional.isPresent()) {
            pluginConfigInterfaceDtos.addAll(allEnabledWithEntityNameNullOptional.get().stream().map(pluginConfigInterface->PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)).collect(Collectors.toList()));
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
                throw new WecubeCoreException(String.format("Can not delete [%s] status PluginConfig", cfg.getStatus()));
            }
            PluginPackage pkg = cfg.getPluginPackage();
            pkg.getPluginConfigs().remove(cfg);
            pluginPackageRepository.save(pkg);
        } else {
            throw new WecubeCoreException(String.format("Can not found PluginConfig[%s]", configId));
        }
    }
}
