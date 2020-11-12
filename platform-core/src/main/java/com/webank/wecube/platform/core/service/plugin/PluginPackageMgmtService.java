package com.webank.wecube.platform.core.service.plugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.PluginPackageInfoDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAuthoritiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDependenciesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.user.RoleMenuService;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AsRoleAuthoritiesDto;
import com.webank.wecube.platform.core.utils.DateUtils;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.core.utils.SystemUtils;

public class PluginPackageMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    public static final String PLATFORM_NAME = "platform";

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginPackageDependenciesMapper pluginPackageDependenciesMapper;

    @Autowired
    private PluginPackageAuthoritiesMapper pluginPackageAuthoritiesMapper;

    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;

    @Autowired
    private RoleMenuService roleMenuService;

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

    @Transactional
    public PluginPackageInfoDto registerPluginPackage(String pluginPackageId) {

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        validatePackageDependencies(pluginPackageEntity);

        ensurePluginPackageIsAllowedToRegister(pluginPackageEntity);

        ensureNoMoreThanTwoActivePackages(pluginPackageEntity);

        createRolesIfNotExistInSystem(pluginPackageEntity);

        bindRoleToMenuWithAuthority(pluginPackageEntity);

        updateSystemVariableStatus(pluginPackageEntity);

        deployPluginUiResourcesIfRequired(pluginPackageEntity);

        pluginPackageEntity.setStatus(PluginPackages.REGISTERED);
        pluginPackagesMapper.updateByPrimaryKeySelective(pluginPackageEntity);

        PluginPackageInfoDto result = buildPluginPackageInfoDto(pluginPackageEntity);

        return result;
    }
    
    private void deployPluginUiResourcesIfRequired(PluginPackages pluginPackage) {
        if (!pluginPackage.getUiPackageIncluded()) {
            return;
        }
        // download UI package from MinIO
        String tmpFolderName = SystemUtils.getTempFolderPath()
                + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String downloadUiZipPath = tmpFolderName + File.separator + pluginProperties.getUiFile();
        log.info("Download UI.zip from S3 to " + downloadUiZipPath);

        String s3UiPackagePath = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getUiFile();
        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3UiPackagePath, downloadUiZipPath);

        String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
                + File.separator + pluginPackage.getVersion() + File.separator;
        log.info("Upload UI.zip from local to static server:" + remotePath);

        // get all static resource hosts
        List<String> staticResourceIps = StringUtilsEx.splitByComma(pluginProperties.getStaticResourceServerIp());

        for (String remoteIp : staticResourceIps) {

            // mkdir at remote host
            if (!remotePath.equals("/") && !remotePath.equals(".")) {
                String mkdirCmd = String.format("rm -rf %s && mkdir -p %s", remotePath, remotePath);

                try {
                    commandService.runAtRemote(remoteIp, pluginProperties.getStaticResourceServerUser(),
                            pluginProperties.getStaticResourceServerPassword(),
                            pluginProperties.getStaticResourceServerPort(), mkdirCmd);
                } catch (Exception e) {
                    log.error("Run command [mkdir] meet error: ", e.getMessage());
                    throw new WecubeCoreException("3110",
                            String.format("Run remote command meet error: %s", e.getMessage()), e.getMessage());
                }
            }

            // scp UI.zip to Static Resource Server
            try {
                scpService.put(remoteIp, pluginProperties.getStaticResourceServerPort(),
                        pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(), downloadUiZipPath, remotePath);
            } catch (Exception e) {
                throw new WecubeCoreException("3111",
                        String.format("Put file to remote host meet error: ", e.getMessage()));
            }
            log.info("scp UI.zip to Static Resource Server - Done");

            // unzip file
            String unzipCmd = String.format("cd %s && unzip %s", remotePath, pluginProperties.getUiFile());
            try {
                commandService.runAtRemote(remoteIp, pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(),
                        pluginProperties.getStaticResourceServerPort(), unzipCmd);
            } catch (Exception e) {
                log.error("Run command [unzip] meet error: ", e.getMessage());
                throw new WecubeCoreException("3112",
                        String.format("Run remote command meet error: %s", e.getMessage()));
            }
        }

        log.info("UI package deployment has done...");

    }

    private void updateSystemVariableStatus(PluginPackages pluginPackage) {
        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllByName(pluginPackage.getName());

        List<String> pluginPackageIds = new ArrayList<String>();
        
        for(PluginPackages p : pluginPackagesEntities){
            pluginPackageIds.add(p.getId());
        }
        

        List<SystemVariables> systemVariablesEntities = systemVariablesMapper.selectAllByPluginPackages(pluginPackageIds);
        for(SystemVariables systemVariableEntity : systemVariablesEntities){
            if (SystemVariables.ACTIVE.equals(systemVariableEntity.getStatus())
                    && !pluginPackage.getId().equals(systemVariableEntity.getSource())) {
                systemVariableEntity.setStatus(SystemVariables.INACTIVE);
                systemVariablesMapper.updateByPrimaryKeySelective(systemVariableEntity);
            }
            
            if (SystemVariables.INACTIVE.equals(systemVariableEntity.getStatus())
                    && pluginPackage.getId().equals(systemVariableEntity.getSource())) {
                systemVariableEntity.setStatus(SystemVariables.ACTIVE);
                systemVariablesMapper.updateByPrimaryKeySelective(systemVariableEntity);
            }
        }
    }

    private PluginPackageMenus pickoutAnyPluginPackageMenusByMenuCode(String menuCode,
            List<PluginPackageMenus> pluginPackageMenusEntities) {
        for (PluginPackageMenus m : pluginPackageMenusEntities) {
            if (menuCode.equals(m.getCode())) {
                return m;
            }
        }

        return null;
    }

    private void bindRoleToMenuWithAuthority(PluginPackages pluginPackage) {
        List<PluginPackageAuthorities> pluginPackageAuthoritiesEntities = pluginPackageAuthoritiesMapper
                .selectAllByPackage(pluginPackage.getId());

        if (pluginPackageAuthoritiesEntities == null || pluginPackageAuthoritiesEntities.isEmpty()) {
            return;
        }

        List<PluginPackageMenus> pluginPackageMenusEntities = pluginPackageMenusMapper
                .selectAllMenusByPackage(pluginPackage.getId());

        if (pluginPackageMenusEntities == null || pluginPackageMenusEntities.isEmpty()) {
            throw new WecubeCoreException(
                    "There is not any plugin package menus configured for package:" + pluginPackage.getName());
        }

        for (PluginPackageAuthorities pluginPackageAuthoritiesEntity : pluginPackageAuthoritiesEntities) {
            String roleName = pluginPackageAuthoritiesEntity.getRoleName();
            String menuCode = pluginPackageAuthoritiesEntity.getMenuCode();

            PluginPackageMenus pickedOutPluginPackageMenuEntity = pickoutAnyPluginPackageMenusByMenuCode(menuCode,
                    pluginPackageMenusEntities);

            if (pickedOutPluginPackageMenuEntity == null) {
                String msg = String.format(
                        "The declared menu code: [%s] in <authorities> field doesn't declared in <menus> field of register.xml",
                        menuCode);
                log.error(msg);
                throw new WecubeCoreException("3104", msg, menuCode);
            }

            roleMenuService.createRoleMenuBinding(roleName, menuCode);
            AsAuthorityDto grantAuthority = new AsAuthorityDto();
            grantAuthority.setCode(menuCode);
            AsRoleAuthoritiesDto grantAuthorityToRole = new AsRoleAuthoritiesDto();
            grantAuthorityToRole.setRoleName(roleName);
            grantAuthorityToRole.setAuthorities(Collections.singletonList(grantAuthority));
            this.authServerRestClient.configureRoleAuthoritiesWithRoleName(grantAuthorityToRole);
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
                .selectAllByNameAndStatuses(pluginPackage.getName(), PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);

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
        for (String activeStatus : PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES) {
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
