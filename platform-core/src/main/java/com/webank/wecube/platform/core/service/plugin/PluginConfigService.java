package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.entity;
import static com.webank.wecube.platform.core.dto.PluginConfigInterfaceParameterDto.MappingType.system_variable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.PluginAuthEntity;
import com.webank.wecube.platform.core.jpa.PluginAuthRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import com.webank.wecube.platform.core.utils.CollectionUtils;

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

    @Autowired
    private UserManagementServiceImpl userManagementService;

    public List<PluginConfigInterface> getPluginConfigInterfaces(String pluginConfigId) {
        return pluginConfigRepository.findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfigId);
    }

    public PluginConfigDto savePluginConfig(PluginConfigDto pluginConfigDto) throws WecubeCoreException {
        validatePermission(pluginConfigDto.getPermissionToRole());
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

        PluginConfigDto results = PluginConfigDto.fromDomain(savedPluginConfig);

        Map<String, List<String>> addedPermissionToRole = processCreatePluginConfigRoleBindings(
                savedPluginConfig.getId(), pluginConfigDto.getPermissionToRole());

        results.addAllPermissionToRole(addedPermissionToRole);

        return results;
    }

    private void validatePermission(Map<String, List<String>> permissionToRole) {
        if (permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("3036", "Permission configuration should provide.");
        }
        List<String> mgmtRoleIds = permissionToRole.get(PluginAuthEntity.PERM_TYPE_MGMT);
        if (mgmtRoleIds == null || mgmtRoleIds.isEmpty()) {
            throw new WecubeCoreException("3037", "At least one management role should provide.");
        }

        return;
    }

    private Map<String, List<String>> processUpdatePluginConfigRoleBindings(String pluginConfigId,
            Map<String, List<String>> permissionToRole) {
        if (permissionToRole == null || permissionToRole.isEmpty()) {
            return permissionToRole;
        }

        for (String permission : permissionToRole.keySet()) {
            List<String> existRoleIds = getExistRoleIdsOfPluginConfigAndPermission(pluginConfigId, permission);
            List<String> inputRoleIds = permissionToRole.get(permission);

            List<String> roleIdsToAdd = CollectionUtils.listMinus(inputRoleIds, existRoleIds);
            List<String> roleIdsToRemove = CollectionUtils.listMinus(inputRoleIds, existRoleIds);

            addPluginConfigRoleBindings(pluginConfigId, permission, roleIdsToAdd);
            deletePluginConfigRoleBindings(pluginConfigId, permission, roleIdsToRemove);

        }

        return permissionToRole;
    }

    private void addPluginConfigRoleBindings(String pluginConfigId, String permission, List<String> roleIdsToAdd) {
        if (log.isDebugEnabled()) {
            log.debug("roles to add for {} {}:{}", pluginConfigId, permission, roleIdsToAdd);
        }

        for (String roleId : roleIdsToAdd) {
            RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
            PluginAuthEntity pluginAuthEntity = new PluginAuthEntity();
            pluginAuthEntity.setActive(true);
            pluginAuthEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            pluginAuthEntity.setCreatedTime(new Date());
            pluginAuthEntity.setPermissionType(permission);
            pluginAuthEntity.setPluginConfigId(pluginConfigId);
            pluginAuthEntity.setRoleId(roleId);
            pluginAuthEntity.setRoleName(roleDto.getName());
            pluginAuthRepository.saveAndFlush(pluginAuthEntity);
        }

    }

    private void deletePluginConfigRoleBindings(String pluginConfigId, String permission,
            List<String> roleIdsToRemove) {
        if (log.isDebugEnabled()) {
            log.debug("roles to remove for {} {}:{}", pluginConfigId, permission, roleIdsToRemove);
        }
        List<PluginAuthEntity> entities = this.pluginAuthRepository.findAllByPluginConfigIdAndPermission(pluginConfigId,
                permission);
        for (String roleId : roleIdsToRemove) {
            PluginAuthEntity entity = pickoutPluginAuthEntityByRoleId(entities, roleId);
            if (entity != null) {
                this.pluginAuthRepository.delete(entity);
            }
        }

    }

    private PluginAuthEntity pickoutPluginAuthEntityByRoleId(List<PluginAuthEntity> entities, String roleId) {
        for (PluginAuthEntity entity : entities) {
            if (roleId.equals(entity.getRoleId())) {
                return entity;
            }
        }

        return null;
    }

    private List<String> getExistRoleIdsOfPluginConfigAndPermission(String pluginConfigId, String permission) {
        List<String> existRoleIds = new ArrayList<String>();
        List<PluginAuthEntity> entities = this.pluginAuthRepository.findAllByPluginConfigIdAndPermission(pluginConfigId,
                permission);
        for (PluginAuthEntity e : entities) {
            existRoleIds.add(e.getRoleId());
        }

        return existRoleIds;
    }

    private Map<String, List<String>> processCreatePluginConfigRoleBindings(String pluginConfigId,
            Map<String, List<String>> permissionToRole) {

        if (log.isInfoEnabled()) {
            log.info("start to create plugin config role bindings:{}, {}", pluginConfigId, permissionToRole);
        }
        Map<String, List<String>> boundPermissionToRole = new HashMap<String, List<String>>();
        if (permissionToRole == null || permissionToRole.isEmpty()) {
            log.warn("Inputted permission roles is empty for {}", pluginConfigId);
            return boundPermissionToRole;
        }

        for (String permission : permissionToRole.keySet()) {
            List<String> roleIds = permissionToRole.get(permission);
            if (roleIds != null) {
                List<String> addedRoleIds = new ArrayList<String>();
                for (String roleId : roleIds) {
                    RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
                    PluginAuthEntity pluginAuthEntity = new PluginAuthEntity();
                    pluginAuthEntity.setActive(true);
                    pluginAuthEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                    pluginAuthEntity.setCreatedTime(new Date());
                    pluginAuthEntity.setPermissionType(permission);
                    pluginAuthEntity.setPluginConfigId(pluginConfigId);
                    pluginAuthEntity.setRoleId(roleId);
                    pluginAuthEntity.setRoleName(roleDto.getName());
                    pluginAuthRepository.saveAndFlush(pluginAuthEntity);

                    addedRoleIds.add(roleId);
                }

                log.info("plugin config roles bound:{}, {}, {}", pluginConfigId, permission, addedRoleIds.size());
                boundPermissionToRole.put(permission, addedRoleIds);
            }

        }

        return boundPermissionToRole;
    }

    public void updatePluginConfigRoleBinding(String pluginConfigId,
            PluginConfigRoleRequestDto pluginConfigRoleRequestDto) throws WecubeCoreException {
        if (log.isDebugEnabled()) {
            log.debug("start to update plugin config role binding:{},{}", pluginConfigId, pluginConfigRoleRequestDto);
        }
        String permission = pluginConfigRoleRequestDto.getPermission();
        List<String> inputRoleIds = pluginConfigRoleRequestDto.getRoleIds();
        validateCurrentUserPermission(pluginConfigId, PluginAuthEntity.PERM_TYPE_MGMT);

        if (inputRoleIds == null || inputRoleIds.isEmpty()) {
            log.info("input role IDs is empty");
            return;
        }
        List<String> existRoleIds = getExistRoleIdsOfPluginConfigAndPermission(pluginConfigId, permission);
        List<String> roleIdsToAdd = new ArrayList<String>();
        for (String roleId : inputRoleIds) {
            if (existRoleIds.contains(roleId)) {
                continue;
            }

            roleIdsToAdd.add(roleId);
        }

        addPluginConfigRoleBindings(pluginConfigId, permission, roleIdsToAdd);
    }

    private void validateCurrentUserPermission(String pluginConfigId, String permission) {
        String currentUsername = AuthenticationContextHolder.getCurrentUsername();
        if (StringUtils.isBlank(currentUsername)) {
            throw new WecubeCoreException("3038", "Current user did not login in.");
        }

        Set<String> currUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoles == null || currUserRoles.isEmpty()) {
            throw new WecubeCoreException("3039", "Lack of permission to update user permission configuration.");
        }

        List<PluginAuthEntity> pluginAuthConfigEntities = this.pluginAuthRepository
                .findAllByPluginConfigIdAndPermission(pluginConfigId, permission);

        if (pluginAuthConfigEntities == null || pluginAuthConfigEntities.isEmpty()) {
            throw new WecubeCoreException("3040", "None plugin authority configured for [%s] [%s]", pluginConfigId,
                    permission);
        }

        boolean hasAuthority = false;
        for (PluginAuthEntity auth : pluginAuthConfigEntities) {
            String authRole = auth.getRoleName();
            if (StringUtils.isBlank(authRole)) {
                continue;
            }
            if (CollectionUtils.collectionContains(currUserRoles, authRole)) {
                hasAuthority = true;
                break;
            }
        }

        if (!hasAuthority) {
            StringBuilder rolesStr = new StringBuilder();
            for (PluginAuthEntity auth : pluginAuthConfigEntities) {
                rolesStr.append(auth.getRoleName());
            }
            String errorMsg = String.format(
                    "Current user do not have privilege to update [%s].Must have one of the roles:%s", pluginConfigId,
                    rolesStr.toString());
            throw new WecubeCoreException("3041", errorMsg, pluginConfigId, rolesStr.toString());
        }
    }

    public void deletePluginConfigRoleBinding(String pluginConfigId,
            PluginConfigRoleRequestDto pluginConfigRoleRequestDto) throws WecubeCoreException {

        String permission = pluginConfigRoleRequestDto.getPermission();
        List<String> inputRoleIds = pluginConfigRoleRequestDto.getRoleIds();

        validateCurrentUserPermission(pluginConfigId, PluginAuthEntity.PERM_TYPE_MGMT);

        if (inputRoleIds == null || inputRoleIds.isEmpty()) {
            return;
        }

        deletePluginConfigRoleBindings(pluginConfigId, permission, inputRoleIds);
    }

    private void ensurePluginConfigIdNotExisted(PluginConfig pluginConfig) {
        pluginConfig.initId();
        if (pluginConfigRepository.existsById(pluginConfig.getId())) {
            throw new WecubeCoreException("3042", String.format("PluginConfig[%s] already exist", pluginConfig.getId()),
                    pluginConfig.getId());
        }
    }

    private void ensurePluginConfigRegisterNameNotExisted(PluginConfigDto pluginConfigDto) {
        if (pluginConfigRepository.existsByPluginPackage_idAndNameAndRegisterName(pluginConfigDto.getPluginPackageId(),
                pluginConfigDto.getName(), pluginConfigDto.getRegisterName())) {
            throw new WecubeCoreException("3043",
                    String.format("PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                            pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                            pluginConfigDto.getRegisterName()),
                    pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(), pluginConfigDto.getRegisterName());
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
                    String msg = String.format(
                            "PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                            pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                            pluginConfigDto.getRegisterName());
                    throw new WecubeCoreException("3044", msg, pluginConfigDto.getPluginPackageId(),
                            pluginConfigDto.getName(), pluginConfigDto.getRegisterName());
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
            throw new WecubeCoreException("3045", "Not allow to update plugin with status: ENABLED");
        }
        pluginConfig.setStatus(DISABLED);

        PluginConfig savedPluginConfig = pluginConfigRepository.save(pluginConfig);
        PluginConfigDto results = PluginConfigDto.fromDomain(savedPluginConfig);

        Map<String, List<String>> addedPermissionToRole = processUpdatePluginConfigRoleBindings(
                savedPluginConfig.getId(), pluginConfigDto.getPermissionToRole());

        results.addAllPermissionToRole(addedPermissionToRole);
        return results;
    }

    private void ensurePluginConfigIsValid(PluginConfigDto pluginConfigDto) {
        if (StringUtils.isBlank(pluginConfigDto.getPluginPackageId())
                || !pluginPackageRepository.existsById(pluginConfigDto.getPluginPackageId())) {
            String msg = String.format("Cannot find PluginPackage with id=%s in PluginConfig",
                    pluginConfigDto.getPluginPackageId());
            throw new WecubeCoreException("3046", msg, pluginConfigDto.getPluginPackageId());
        }
        if (StringUtils.isBlank(pluginConfigDto.getId())) {
            throw new WecubeCoreException("3047", "Invalid pluginConfig with id: " + pluginConfigDto.getId(),
                    pluginConfigDto.getId());
        }

        if (!pluginConfigRepository.existsById(pluginConfigDto.getId())) {
            throw new WecubeCoreException("3048", "PluginConfig not found for id: " + pluginConfigDto.getId(),
                    pluginConfigDto.getId());
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
                throw new WecubeCoreException("3049", "Data model not exists for package name [%s]");
            }

            Integer dataModelVersion = dataModelOptional.get().getVersion();
            if (!pluginPackageEntityRepository.existsByPackageNameAndNameAndDataModelVersion(targetPackage,
                    targetEntity, dataModelVersion)) {
                String errorMessage = String.format(
                        "PluginPackageEntity not found for packageName:dataModelVersion:entityName [%s:%s:%s] for plugin config: %s",
                        targetPackage, dataModelVersion, targetEntity, pluginConfigName);
                log.error(errorMessage);
                throw new WecubeCoreException("3050", errorMessage, targetPackage, dataModelVersion, targetEntity,
                        pluginConfigName);
            }
        }
    }

    public PluginConfigDto enablePlugin(String pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("3051", "PluginConfig not found for id: " + pluginConfigId);
        }

        PluginConfig pluginConfig = pluginConfigRepository.findById(pluginConfigId).get();

        if (pluginConfig.getPluginPackage() == null || UNREGISTERED == pluginConfig.getPluginPackage().getStatus()
                || DECOMMISSIONED == pluginConfig.getPluginPackage().getStatus()) {
            throw new WecubeCoreException("3052",
                    "Plugin package is not in valid status [REGISTERED, RUNNING, STOPPED] to enable plugin.");
        }

        if (ENABLED == pluginConfig.getStatus()) {
            throw new WecubeCoreException("3053", "Not allow to enable pluginConfig with status: ENABLED");
        }

        validateCurrentUserPermission(pluginConfigId, PluginAuthEntity.PERM_TYPE_MGMT);

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
                                throw new WecubeCoreException("3054",
                                        String.format("System variable is required for parameter [%s]",
                                                inputParameter.getId()),
                                        inputParameter.getId());
                            }
                            if (entity.name().equals(inputParameter.getMappingType())
                                    && StringUtils.isBlank(inputParameter.getMappingEntityExpression())) {
                                throw new WecubeCoreException("3055",
                                        String.format("Entity expression is required for parameter [%s]",
                                                inputParameter.getId()),
                                        inputParameter.getId());
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
                                throw new WecubeCoreException("3056",
                                        String.format("Entity expression is required for parameter [%s]",
                                                outputParameter.getId()),
                                        outputParameter.getId());
                            }
                        }
                    });
                }
            });
        }
    }

    public PluginConfigDto disablePlugin(String pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("3057", "PluginConfig not found for id: " + pluginConfigId);
        }

        PluginConfig pluginConfig = pluginConfigRepository.findById(pluginConfigId).get();

        validateCurrentUserPermission(pluginConfigId, PluginAuthEntity.PERM_TYPE_MGMT);

        pluginConfig.setStatus(DISABLED);
        return PluginConfigDto.fromDomain(pluginConfigRepository.save(pluginConfig));
    }

    public PluginConfigInterface getPluginConfigInterfaceByServiceName(String serviceName) {
        Optional<PluginConfigInterface> pluginConfigInterface = pluginConfigRepository
                .findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(serviceName);
        if (!pluginConfigInterface.isPresent()) {
            throw new WecubeCoreException("3058",
                    String.format("Plugin interface not found for serviceName [%s].", serviceName), serviceName);
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

        return filterDtoWithPermissionValidation(pluginConfigInterfaceDtos, PluginAuthEntity.PERM_TYPE_USE);
    }

    public List<PluginConfigInterfaceDto> queryAllEnabledPluginConfigInterfaceForEntityByFilterRule(
            TargetEntityFilterRuleDto filterRuleDto) {
        return distinctPluginConfigInfDto(queryAllEnabledPluginConfigInterfaceForEntity(filterRuleDto.getPkgName(),
                filterRuleDto.getEntityName(), filterRuleDto));
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
                List<PluginConfigInterface> rawPluginIntfs = allEnabledInterfacesOptional.get();
                List<PluginConfigInterface> filteredPluginConfigIntfs = filterWithPermissionValidation(rawPluginIntfs, PluginAuthEntity.PERM_TYPE_USE);
                
                List<PluginConfigInterface> filteredLatestConfigIntfs = filterLatestPluginConfigInterfaces(filteredPluginConfigIntfs);
                
                pluginConfigInterfaceDtos.addAll(filteredLatestConfigIntfs.stream()
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
                    List<PluginConfigInterface> rawPluginIntfs = filterRuleIsNullEnabledInterfacesOptional.get();
                    List<PluginConfigInterface> filteredPluginConfigIntfs = filterWithPermissionValidation(rawPluginIntfs, PluginAuthEntity.PERM_TYPE_USE);
                    
                    List<PluginConfigInterface> filteredLatestConfigIntfs = filterLatestPluginConfigInterfaces(filteredPluginConfigIntfs);
                    pluginConfigInterfaceDtos.addAll(filteredLatestConfigIntfs.stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
                Optional<List<PluginConfigInterface>> filterRuleIsEmptyEnabledInterfacesOptional = pluginConfigInterfaceRepository
                        .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_TargetEntityFilterRuleAndPluginConfig_Status(
                                packageName, entityName, "", ENABLED);
                if (filterRuleIsEmptyEnabledInterfacesOptional.isPresent()) {
                    List<PluginConfigInterface> rawPluginIntfs = filterRuleIsNullEnabledInterfacesOptional.get();
                    List<PluginConfigInterface> filteredPluginConfigIntfs = filterWithPermissionValidation(rawPluginIntfs, PluginAuthEntity.PERM_TYPE_USE);
                    
                    List<PluginConfigInterface> filteredLatestConfigIntfs = filterLatestPluginConfigInterfaces(filteredPluginConfigIntfs);
                    pluginConfigInterfaceDtos.addAll(filteredLatestConfigIntfs.stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
            } else {
                Optional<List<PluginConfigInterface>> allEnabledInterfacesOptional = pluginConfigInterfaceRepository
                        .findPluginConfigInterfaceByPluginConfig_TargetPackageAndPluginConfig_TargetEntityAndPluginConfig_TargetEntityFilterRuleAndPluginConfig_Status(
                                packageName, entityName, filterRuleDto.getTargetEntityFilterRule(), ENABLED);
                if (allEnabledInterfacesOptional.isPresent()) {
                    List<PluginConfigInterface> rawPluginIntfs = allEnabledInterfacesOptional.get();
                    List<PluginConfigInterface> filteredPluginConfigIntfs = filterWithPermissionValidation(rawPluginIntfs, PluginAuthEntity.PERM_TYPE_USE);
                    
                    List<PluginConfigInterface> filteredLatestConfigIntfs = filterLatestPluginConfigInterfaces(filteredPluginConfigIntfs);
                    pluginConfigInterfaceDtos.addAll(filteredLatestConfigIntfs.stream()
                            .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                            .collect(Collectors.toList()));
                }
            }
        }

        Optional<List<PluginConfigInterface>> allEnabledWithEntityNameNullOpt = pluginConfigInterfaceRepository
                .findAllEnabledWithEntityNameNull();
        if (allEnabledWithEntityNameNullOpt.isPresent()) {
            List<PluginConfigInterface> rawPluginConfigIntfs = allEnabledWithEntityNameNullOpt.get();
            
            List<PluginConfigInterface> filteredPluginConfigIntfs = filterWithPermissionValidation(rawPluginConfigIntfs, PluginAuthEntity.PERM_TYPE_USE);
            
            List<PluginConfigInterface> filteredLatestConfigIntfs = filterLatestPluginConfigInterfaces(filteredPluginConfigIntfs);
            
            pluginConfigInterfaceDtos.addAll(filteredLatestConfigIntfs.stream()
                    .map(pluginConfigInterface -> PluginConfigInterfaceDto.fromDomain(pluginConfigInterface))
                    .collect(Collectors.toList()));
        }

        return pluginConfigInterfaceDtos;
    }
    
    private List<PluginConfigInterface> filterLatestPluginConfigInterfaces(List<PluginConfigInterface> pluginConfigIntfs){
        if (pluginConfigIntfs == null || pluginConfigIntfs.isEmpty()) {
            return pluginConfigIntfs;
        }
        
        Map<String,PluginConfigInterface> serviceNamedPluginConfigIntfs = new HashMap<String,PluginConfigInterface>();
        
        for(PluginConfigInterface pluginConfigIntf : pluginConfigIntfs){
            String serviceName = pluginConfigIntf.generateServiceName();
            PluginConfigInterface existIntf = serviceNamedPluginConfigIntfs.get(serviceName);
            if(existIntf == null){
                serviceNamedPluginConfigIntfs.put(serviceName, pluginConfigIntf);
            }else{
                if(isLaterThen(pluginConfigIntf, existIntf)){
                    log.info("plugin interface {} is later than plugin interface {}", pluginConfigIntf.getId(), existIntf.getId());
                    serviceNamedPluginConfigIntfs.put(serviceName, pluginConfigIntf);
                }
            }
        }
        
        List<PluginConfigInterface> filteredPluginConfigIntfs = new ArrayList<PluginConfigInterface>();
        serviceNamedPluginConfigIntfs.values().forEach(intf -> {
            filteredPluginConfigIntfs.add(intf);
        });
        
        return filteredPluginConfigIntfs;
    }
    
    private boolean isLaterThen(PluginConfigInterface intfa, PluginConfigInterface intfb){
        Timestamp timea = intfa.getPluginConfig().getPluginPackage().getUploadTimestamp();
        Timestamp timeb = intfb.getPluginConfig().getPluginPackage().getUploadTimestamp();
        
        if(timea == null || timeb == null){
            return false;
        }
        
        return timea.getTime() > timeb.getTime();
    }

    private List<PluginConfigInterface> filterWithPermissionValidation(List<PluginConfigInterface> pluginConfigIntfs,
            String permission) {
        if (pluginConfigIntfs == null || pluginConfigIntfs.isEmpty()) {
            return pluginConfigIntfs;
        }

        Set<String> currUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoles == null || currUserRoles.isEmpty()) {
            log.warn("roles of current user is empty.");
            throw new WecubeCoreException("3059", "Lack of permission to perform such operation.");
        }

        List<PluginConfigInterface> filteredPluginConfigIntfs = new ArrayList<>();
        for (PluginConfigInterface pluginConfigIntf : pluginConfigIntfs) {
            if (verifyPluginConfigInterfacePrivilege(pluginConfigIntf.getPluginConfig().getId(), permission,
                    currUserRoles)) {
                filteredPluginConfigIntfs.add(pluginConfigIntf);
            }
        }
        
        return filteredPluginConfigIntfs;
    }

    private List<PluginConfigInterfaceDto> filterDtoWithPermissionValidation(
            List<PluginConfigInterfaceDto> srcPluginConfigInterfaceDtos, String permission) {
        if (srcPluginConfigInterfaceDtos == null || srcPluginConfigInterfaceDtos.isEmpty()) {
            log.warn("interfaces is empty and return it directly.");
            return srcPluginConfigInterfaceDtos;
        }
        Set<String> currUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoles == null || currUserRoles.isEmpty()) {
            log.warn("roles of current user is empty.");
            throw new WecubeCoreException("3059", "Lack of permission to perform such operation.");
        }

        List<PluginConfigInterfaceDto> privilegedPluginConfigInterfaceDtos = new ArrayList<>();
        for (PluginConfigInterfaceDto pluginConfigInterfaceDto : srcPluginConfigInterfaceDtos) {
            if (verifyPluginConfigInterfacePrivilege(pluginConfigInterfaceDto.getPluginConfigId(), permission, currUserRoles)) {
                privilegedPluginConfigInterfaceDtos.add(pluginConfigInterfaceDto);
            }
        }

        return privilegedPluginConfigInterfaceDtos;
    }

    private boolean verifyPluginConfigInterfacePrivilege(String pluginConfigId, String permission,
            Set<String> currUserRoles) {
        List<PluginAuthEntity> entities = pluginAuthRepository.findAllByPluginConfigIdAndPermission(pluginConfigId,
                permission);
        if (entities.isEmpty()) {
            return false;
        }

        for (PluginAuthEntity entity : entities) {
            if (CollectionUtils.collectionContains(currUserRoles, entity.getRoleName())) {
                return true;
            }
        }

        return false;
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
                throw new WecubeCoreException("3061",
                        String.format("Can not delete [%s] status PluginConfig", cfg.getStatus()), cfg.getStatus());
            }

            validateCurrentUserPermission(configId, PluginAuthEntity.PERM_TYPE_MGMT);
            PluginPackage pkg = cfg.getPluginPackage();
            pkg.getPluginConfigs().remove(cfg);
            pluginPackageRepository.save(pkg);
        } else {
            throw new WecubeCoreException("3062", String.format("Can not found PluginConfig[%s]", configId), configId);
        }
    }

}
