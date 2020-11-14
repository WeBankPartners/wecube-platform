package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginConfigMgmtService extends AbstractPluginMgmtService {
    private final static Logger log = LoggerFactory.getLogger(PluginConfigMgmtService.class);

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;

    @Autowired
    private PluginConfigRolesMapper pluginConfigRolesMapper;

    public PluginConfigDto createOrUpdatePluginConfig(PluginConfigDto pluginConfigDto) {
        validatePermission(pluginConfigDto.getPermissionToRole());
        if (pluginConfigDto.getId() == null) {
            return createPluginConfig(pluginConfigDto);
        }
        return updatePluginConfig(pluginConfigDto);
    }

    private PluginConfigDto createPluginConfig(PluginConfigDto pluginConfigDto) {
        String pluginPackageId = pluginConfigDto.getPluginPackageId();

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        ensurePluginConfigRegisterNameNotExists(pluginConfigDto);
        PluginConfigs pluginConfigsEntity = buildPluginConfigsEntity(pluginConfigDto,pluginPackageEntity);

        pluginConfigsEntity.setStatus(PluginConfigs.DISABLED);
        pluginConfigsMapper.insert(pluginConfigsEntity);

        PluginConfigDto results = buildPluginConfigDto(pluginConfigsEntity);

        Map<String, List<String>> addedPermissionToRole = processCreatePluginConfigRoleBindings(
                pluginConfigsEntity.getId(), pluginConfigDto.getPermissionToRole());

        results.addAllPermissionToRole(addedPermissionToRole);

        return results;
    }
    
    public  PluginConfigDto buildPluginConfigDto(PluginConfigs entity) {
        PluginConfigDto dto = new PluginConfigDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setTargetEntityWithFilterRule(entity.getTargetEntityWithFilterRule());
        dto.setRegisterName(entity.getRegisterName());
        dto.setPluginPackageId(entity.getPluginPackage().getId());
        dto.setStatus(entity.getStatus().name());
        List<PluginConfigInterfaceDto> interfaces = newArrayList();
        //TODO
        if (null != entity.getInterfaces() && entity.getInterfaces().size() > 0) {
            entity.getInterfaces().forEach(pluginConfigInterface -> interfaces
                    .add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        dto.setInterfaces(interfaces);
        return dto;
    }
    
    public PluginConfigs buildPluginConfigsEntity(PluginConfigDto pluginConfigDto,PluginPackages pluginPackage) {
        PluginConfig pluginConfig = new PluginConfig();
        if (getId() != null) {
            pluginConfig.setId(getId());
        }
        pluginConfig.setPluginPackage(pluginPackage);

        pluginConfig.setName(getName());
        if (StringUtils.isNotBlank(getTargetPackage())) {
            pluginConfig.setTargetPackage(getTargetPackage());
        } else {
            pluginConfig.setTargetPackage(pluginPackage.getName());
        }

        if (StringUtils.isNotBlank(getTargetEntity())) {
            pluginConfig.setTargetEntity(getTargetEntity());
        }

        if (StringUtils.isNotBlank(getFilterRule())) {
            pluginConfig.setTargetEntityFilterRule(getFilterRule());
        }
        pluginConfig.setRegisterName(getRegisterName());
        Set<PluginConfigInterface> pluginConfigInterfaces = newLinkedHashSet();
        if (null != getInterfaces() && getInterfaces().size() > 0) {
            getInterfaces().forEach(interfaceDto -> pluginConfigInterfaces.add(interfaceDto.toDomain(pluginConfig)));
        }
        pluginConfig.setInterfaces(pluginConfigInterfaces);

        return pluginConfig;
    }

    private void ensurePluginConfigRegisterNameNotExists(PluginConfigDto pluginConfigDto) {
        List<PluginConfigs> pluginConfigsEntities = pluginConfigsMapper.selectAllByPluginPackageAndNameAndRegisterName(
                pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(), pluginConfigDto.getRegisterName());

        if (pluginConfigsEntities == null || pluginConfigsEntities.isEmpty()) {
            return;
        }
        throw new WecubeCoreException("3043",
                String.format("PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                        pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                        pluginConfigDto.getRegisterName()),
                pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(), pluginConfigDto.getRegisterName());
    }

    private PluginConfigDto updatePluginConfig(PluginConfigDto pluginConfigDto) throws WecubeCoreException {
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

    private void validatePermission(Map<String, List<String>> permissionToRole) {
        if (permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("3036", "Permission configuration should provide.");
        }
        List<String> mgmtRoleIds = permissionToRole.get(PluginConfigRoles.PERM_TYPE_MGMT);
        if (mgmtRoleIds == null || mgmtRoleIds.isEmpty()) {
            throw new WecubeCoreException("3037", "At least one management role should provide.");
        }

        return;
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
                    PluginConfigRoles pluginAuthEntity = new PluginConfigRoles();
                    pluginAuthEntity.setId(LocalIdGenerator.uuid());
                    pluginAuthEntity.setIsActive(true);
                    pluginAuthEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                    pluginAuthEntity.setCreatedTime(new Date());
                    pluginAuthEntity.setPermType(permission);
                    pluginAuthEntity.setPluginCfgId(pluginConfigId);
                    pluginAuthEntity.setRoleId(roleId);
                    pluginAuthEntity.setRoleName(roleDto.getName());
                    pluginConfigRolesMapper.insert(pluginAuthEntity);

                    addedRoleIds.add(roleId);
                }

                log.info("plugin config roles bound:{}, {}, {}", pluginConfigId, permission, addedRoleIds.size());
                boundPermissionToRole.put(permission, addedRoleIds);
            }

        }

        return boundPermissionToRole;
    }

}
