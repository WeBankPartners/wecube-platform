package com.webank.wecube.platform.core.service.plugin;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.REGISTERED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.dto.PluginPackageInfoDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAuthoritiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDependenciesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AsRoleAuthoritiesDto;
import com.webank.wecube.platform.core.utils.DateUtils;

public class PluginPackageMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    public static final String PLATFORM_NAME = "platform";

    public static final List<String> PLUGIN_PACKAGE_ACTIVE_STATUS = Lists.newArrayList(PluginPackages.REGISTERED,
            PluginPackages.RUNNING, PluginPackages.STOPPED);

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginPackageDependenciesMapper pluginPackageDependenciesMapper;

    @Autowired
    private PluginPackageAuthoritiesMapper pluginPackageAuthoritiesMapper;

    private VersionComparator versionComparator = new VersionComparator();

    public List<PluginPackageInfoDto> getPluginPackages() {
        List<PluginPackageInfoDto> pluginPackageInfoDtos = new ArrayList<>();

        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper.selectAll();
        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return pluginPackageInfoDtos;
        }

        for (PluginPackages entity : pluginPackageEntities) {
            PluginPackageInfoDto dto = buildPluginPackageInfoDto(entity);

            pluginPackageInfoDtos.add(dto);
        }

        return pluginPackageInfoDtos;
    }

    public List<PluginPackageInfoDto> getDistinctPluginPackages() {
        List<PluginPackageInfoDto> pluginPackageInfoDtos = new ArrayList<>();
        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper.selectAllDistinctPackages();

        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return pluginPackageInfoDtos;
        }

        for (PluginPackages entity : pluginPackageEntities) {
            PluginPackageInfoDto dto = buildPluginPackageInfoDto(entity);

            pluginPackageInfoDtos.add(dto);
        }

        return pluginPackageInfoDtos;

    }

    public PluginPackage registerPluginPackage(String pluginPackageId) {

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        validatePackageDependencies(pluginPackageEntity);

        ensurePluginPackageIsAllowedToRegister(pluginPackageEntity);

        ensureNoMoreThanTwoActivePackages(pluginPackageEntity);

        createRolesIfNotExistInSystem(pluginPackageEntity);

        bindRoleToMenuWithAuthority(pluginPackage);

        updateSystemVariableStatus(pluginPackage);

        deployPluginUiResourcesIfRequired(pluginPackage);

        pluginPackage.setStatus(REGISTERED);

        return pluginPackageRepository.save(pluginPackage);
    }
    
    private void bindRoleToMenuWithAuthority(PluginPackage pluginPackage) {
        final Set<PluginPackageAuthority> pluginPackageAuthorities = pluginPackage.getPluginPackageAuthorities();
        final List<String> selfPkgMenuCodeList = pluginPackage.getPluginPackageMenus().stream()
                .map(PluginPackageMenu::getCode).collect(Collectors.toList());
        if (null != pluginPackageAuthorities && pluginPackageAuthorities.size() > 0) {
            pluginPackageAuthorities.forEach(pluginPackageAuthority -> {
                final String roleName = pluginPackageAuthority.getRoleName();
                final String menuCode = pluginPackageAuthority.getMenuCode();

                // create role menu binding
                if (!selfPkgMenuCodeList.contains(menuCode)) {
                    String msg = String.format(
                            "The declared menu code: [%s] in <authorities> field doesn't declared in <menus> field of register.xml",
                            menuCode);
                    log.error(msg);
                    throw new WecubeCoreException("3104", msg, menuCode);
                }
                this.roleMenuService.createRoleMenuBinding(roleName, menuCode);

                // grant authority to role and send request to auth server
                AsAuthorityDto grantAuthority = new AsAuthorityDto();
                grantAuthority.setCode(menuCode);
                AsRoleAuthoritiesDto grantAuthorityToRole = new AsRoleAuthoritiesDto();
                grantAuthorityToRole.setRoleName(roleName);
                grantAuthorityToRole.setAuthorities(Collections.singletonList(grantAuthority));
                this.authServerRestClient.configureRoleAuthoritiesWithRoleName(grantAuthorityToRole);
            });
        }
    }

    private void createRolesIfNotExistInSystem(PluginPackages pluginPackage) {
        List<PluginPackageAuthorities> pluginPackageAuthoritiesEntities = pluginPackageAuthoritiesMapper
                .selectAllByPackage(pluginPackage.getId());

        if (pluginPackageAuthoritiesEntities == null || pluginPackageAuthoritiesEntities.isEmpty()) {
            return;
        }
        List<RoleDto> roleDtos = userManagementService.retrieveAllRoles();
        Set<String> existingRoleNames = new HashSet<>();
        if (roleDtos != null) {
            existingRoleNames = roleDtos.stream().map(roleDto -> roleDto.getName()).collect(Collectors.toSet());
        }
        Set<String> roleNamesInPlugin = pluginPackageAuthoritiesEntities.stream()
                .map(authority -> authority.getRoleName()).collect(Collectors.toSet());
        Set<String> roleNamesDefinedInPluginButNotExistInSystem = Sets.difference(roleNamesInPlugin, existingRoleNames);
        if (roleNamesDefinedInPluginButNotExistInSystem.isEmpty()) {
            return;
        }
        roleNamesDefinedInPluginButNotExistInSystem.forEach(it -> {
            RoleDto rd = new RoleDto();
            rd.setName(it);
            rd.setDisplayName(it);
            userManagementService.registerLocalRole(rd);
        });
    }

    private void ensureNoMoreThanTwoActivePackages(PluginPackages pluginPackage) {
        List<PluginPackages> allActivePluginPackageEntities = pluginPackagesMapper
                .selectAllByNameAndStatuses(pluginPackage.getName(), PLUGIN_PACKAGE_ACTIVE_STATUS);

        if (allActivePluginPackageEntities == null || allActivePluginPackageEntities.isEmpty()) {
            return;
        }
        if (allActivePluginPackageEntities.size() > 1) {
            String activePackagesString = allActivePluginPackageEntities.stream()
                    .map(it -> String.join(":", it.getName(), it.getVersion(), it.getStatus()))
                    .collect(Collectors.joining(","));
            String msg = String.format("Not allowed to register more packages. Current active packages: [%s]",
                    activePackagesString);
            throw new WecubeCoreException("3106", msg, activePackagesString);
        }
    }

    private void ensurePluginPackageIsAllowedToRegister(PluginPackages pluginPackage) {
        if (!PluginPackages.UNREGISTERED.equalsIgnoreCase(pluginPackage.getStatus())) {
            String errorMessage = String.format(
                    "Failed to register PluginPackage[%s/%s] as it is not in UNREGISTERED status [%s]",
                    pluginPackage.getName(), pluginPackage.getVersion(), pluginPackage.getStatus());
            log.warn(errorMessage);
            throw new WecubeCoreException("3105", errorMessage, pluginPackage.getName(), pluginPackage.getVersion(),
                    pluginPackage.getStatus());
        }
    }

    private void validatePackageDependencies(PluginPackages pluginPackageEntity) {
        List<PluginPackageDependencies> pluginPackageDependenciesEntities = pluginPackageDependenciesMapper
                .selectAllByPackage(pluginPackageEntity.getId());
        if (pluginPackageDependenciesEntities == null || pluginPackageDependenciesEntities.isEmpty()) {
            log.info("{} has no dependencies and no need to validate.", pluginPackageEntity.getId());
            return;
        }

        for (PluginPackageDependencies pluginPackageDependency : pluginPackageDependenciesEntities) {
            if (isPlatformDependency(pluginPackageDependency)) {
                validatePlatformDependency(pluginPackageDependency);
                continue;
            }

            if (!hasNewerDependencyPluginPackageActiveStatus(pluginPackageDependency)) {
                log.info("dependended plugin package {} {} is not in active status.",
                        pluginPackageDependency.getDependencyPackageName(),
                        pluginPackageDependency.getDependencyPackageVersion());
                String msg = String.format(
                        "Plugin dependency validation failed:make sure dependency packege %s %s is in active status.",
                        pluginPackageDependency.getDependencyPackageName(),
                        pluginPackageDependency.getDependencyPackageVersion());
                throw new WecubeCoreException("3310", msg, pluginPackageDependency.getDependencyPackageName(),
                        pluginPackageDependency.getDependencyPackageVersion());
            }
        }
    }

    private boolean isPlatformDependency(PluginPackageDependencies pluginPackageDependency) {
        if (PLATFORM_NAME.equalsIgnoreCase(pluginPackageDependency.getDependencyPackageName())) {
            return true;
        }

        return false;
    }

    private boolean hasNewerDependencyPluginPackageActiveStatus(PluginPackageDependencies pluginPackageDependency) {
        log.info("try to find newer active dependency plugin package: name={}, version={}",
                pluginPackageDependency.getDependencyPackageName(),
                pluginPackageDependency.getDependencyPackageVersion());

        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper
                .selectAllByName(pluginPackageDependency.getDependencyPackageName());
        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return false;
        }

        String baseVersion = pluginPackageDependency.getDependencyPackageVersion();

        for (PluginPackages lazyPluginPackage : pluginPackageEntities) {
            if (!isLazyActiveStatus(lazyPluginPackage.getStatus())) {
                continue;
            }

            //
            String version = lazyPluginPackage.getVersion();
            int compare = versionComparator.compare(version, baseVersion);
            if (compare >= 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isLazyActiveStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        for (String activeStatus : PLUGIN_PACKAGE_ACTIVE_STATUS) {
            if (activeStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }

        return false;
    }

    private void validatePlatformDependency(PluginPackageDependencies pluginPackageDependency) {
        String platformVersion = applicationVersionInfo.getVersion();
        int compare = versionComparator.compare(platformVersion, pluginPackageDependency.getDependencyPackageVersion());
        if (compare < 0) {
            String msg = String.format(
                    "Platform version does not match.At least %s required but currently the version is %s.",
                    pluginPackageDependency.getDependencyPackageVersion(), platformVersion);
            throw new WecubeCoreException(msg);
        }

    }

    private PluginPackageInfoDto buildPluginPackageInfoDto(PluginPackages entity) {
        PluginPackageInfoDto dto = new PluginPackageInfoDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setUiPackageIncluded(entity.getUiPackageIncluded());
        dto.setUploadTimestamp(DateUtils.dateToString(entity.getUploadTimestamp()));
        dto.setVersion(entity.getVersion());

        return dto;
    }

}
