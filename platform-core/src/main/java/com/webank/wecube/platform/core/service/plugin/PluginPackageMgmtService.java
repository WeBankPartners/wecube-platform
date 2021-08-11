package com.webank.wecube.platform.core.service.plugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigGroupByNameDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceParameterDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigOutlineDto;
import com.webank.wecube.platform.core.dto.plugin.PluginDeclarationDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageAuthoritiesDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageInfoDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageRuntimeResourcesDockerDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageRuntimeResourcesMysqlDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageRuntimeResourcesS3Dto;
import com.webank.wecube.platform.core.dto.plugin.SystemVariableDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.MenuItems;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.MenuItemsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAuthoritiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDependenciesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesDockerMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesMysqlMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesS3Mapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.user.RoleMenuService;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AsRoleAuthoritiesDto;
import com.webank.wecube.platform.core.utils.CollectionUtils;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.DateUtils;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.core.utils.SystemUtils;

@Service
public class PluginPackageMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    public static final String PLATFORM_NAME = "platform";

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;

    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;

    @Autowired
    private PluginPackageDependenciesMapper pluginPackageDependenciesMapper;

    @Autowired
    private PluginPackageAuthoritiesMapper pluginPackageAuthoritiesMapper;

    @Autowired
    private MenuItemsMapper menuItemsMapper;

    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;

    @Autowired
    private PluginPackageRuntimeResourcesDockerMapper pluginPackageRuntimeResourcesDockerMapper;

    @Autowired
    private PluginPackageRuntimeResourcesMysqlMapper pluginPackageRuntimeResourcesMysqlMapper;

    @Autowired
    private PluginPackageRuntimeResourcesS3Mapper pluginPackageRuntimeResourcesS3Mapper;

    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;

    @Autowired
    private PluginConfigInterfaceParametersMapper pluginConfigInterfaceParametersMapper;

    @Autowired
    private PluginConfigRolesMapper pluginConfigRolesMapper;

    @Autowired
    private RoleMenuService roleMenuService;

    private VersionComparator versionComparator = new VersionComparator();

    /**
     * 
     * @param packageName
     * @return
     */
    public PluginPackages fetchLatestVersionPluginPackage(String packageName) {
        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllByNameAndStatuses(packageName, PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);
        if (pluginPackagesEntities == null || pluginPackagesEntities.isEmpty()) {
            return null;
        }

        PluginPackages latestVersionPluginPackagesEntity = null;
        for (PluginPackages lazyPluginPackage : pluginPackagesEntities) {
            if (latestVersionPluginPackagesEntity == null) {
                latestVersionPluginPackagesEntity = lazyPluginPackage;
            } else {
                String formerVersion = latestVersionPluginPackagesEntity.getVersion();
                int compare = versionComparator.compare(lazyPluginPackage.getVersion(), formerVersion);
                if (compare > 0) {
                    latestVersionPluginPackagesEntity = lazyPluginPackage;
                }
            }
        }

        return latestVersionPluginPackagesEntity;
    }

    /**
     * Fetch all plugin packages including decommissioned ones.
     * 
     * @return
     */
    public List<PluginPackageInfoDto> fetchAllPluginPackages() {
        List<PluginPackageInfoDto> pluginPackageInfoDtos = new ArrayList<>();

        List<PluginPackages> pluginPackageEntities = pluginPackagesMapper.selectAll();
        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return pluginPackageInfoDtos;
        }

        for (PluginPackages entity : pluginPackageEntities) {
            PluginPackageInfoDto dto = buildPluginPackageInfoDto(entity);

            pluginPackageInfoDtos.add(dto);
        }

        Collections.sort(pluginPackageInfoDtos, new Comparator<PluginPackageInfoDto>() {

            @Override
            public int compare(PluginPackageInfoDto o1, PluginPackageInfoDto o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });

        return pluginPackageInfoDtos;
    }

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    @Transactional
    public PluginPackageInfoDto registerPluginPackage(String pluginPackageId) {

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            String errMsg = String.format("Plugin package does not exist for id [%s] ", pluginPackageId);
            throw new WecubeCoreException("3109", errMsg, pluginPackageId);
        }

        try {

            PluginPackageInfoDto result = doRegisterPluginPackage(pluginPackageEntity);

            if (log.isInfoEnabled()) {
                log.info("Plugin package {} registered successfully.", pluginPackageId);
            }

            return result;
        } catch (Exception e) {
            log.error("Errors to register plugin package.", e);
            throw new WecubeCoreException("3322", "Failed to register plugin package."+e.getMessage(), e.getMessage());
        }
    }

    /**
     * 
     * @param pluginPackageId
     */
    public void decommissionPluginPackage(String pluginPackageId) {
        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        ensureNoPluginInstanceIsRunning(pluginPackageId);

        disableAllPluginConfigs(pluginPackageEntity);

        deactivateSystemVariables(pluginPackageEntity);

        removeLocalDockerImageFiles(pluginPackageEntity);

        removePluginUiResourcesIfRequired(pluginPackageEntity);

        pluginPackageEntity.setStatus(PluginPackages.DECOMMISSIONED);
        pluginPackagesMapper.updateByPrimaryKeySelective(pluginPackageEntity);
    }

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    public PluginPackageDependencyDto fetchPluginPackageDependencies(String pluginPackageId) {
        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        PluginPackageDependencyDto dependencyDto = new PluginPackageDependencyDto();
        dependencyDto.setPackageName(pluginPackageEntity.getName());
        dependencyDto.setVersion(pluginPackageEntity.getVersion());

        List<PluginPackageDependencies> dependencyEntities = pluginPackageDependenciesMapper
                .selectAllByPackage(pluginPackageId);

        if (dependencyEntities == null || dependencyEntities.isEmpty()) {
            return dependencyDto;
        }

        List<PluginPackageDependencyDto> totalDependencies = new ArrayList<>();
        totalDependencies.add(dependencyDto);

        for (PluginPackageDependencies dependencyEntity : dependencyEntities) {
            appendPackageDependencies(dependencyDto, totalDependencies, dependencyEntity);
        }

        return dependencyDto;
    }

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    public List<MenuItemDto> getMenusByPackageId(String pluginPackageId) {
        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        List<MenuItemDto> resultMenuItemDtos = new ArrayList<>();

        List<MenuItemDto> allSysMenuItemDtos = fetchAllSysMenuItems();
        resultMenuItemDtos.addAll(allSysMenuItemDtos);

        List<PluginPackageMenus> pluginPackageMenusEntities = pluginPackageMenusMapper
                .selectAllMenusByPackage(pluginPackageId);

        if (pluginPackageMenusEntities == null) {
            Collections.sort(resultMenuItemDtos);
            return resultMenuItemDtos;
        }

        for (PluginPackageMenus pluginPackageMenusEntity : pluginPackageMenusEntities) {
            MenuItems sysMenusItemEntity = menuItemsMapper.selectByMenuCode(pluginPackageMenusEntity.getCategory());
            if (sysMenusItemEntity == null) {
                String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                        pluginPackageMenusEntity.getCategory());
                log.error(msg);
                throw new WecubeCoreException("3101", msg, pluginPackageMenusEntity.getCategory());
            }

            MenuItemDto pluginPackageMenuItemDto = buildPackageMenuItemDto(pluginPackageMenusEntity,
                    sysMenusItemEntity);
            resultMenuItemDtos.add(pluginPackageMenuItemDto);
        }

        Collections.sort(resultMenuItemDtos);
        return resultMenuItemDtos;
    }

    /**
     * 
     * @param packageId
     * @return
     */
    public List<SystemVariableDto> getSystemVarsByPackageId(String packageId) {
        List<SystemVariableDto> resultDtos = new ArrayList<>();
        
        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(packageId);
        
        if(pluginPackageEntity == null){
            return resultDtos;
        }
        
        String source = PluginPackages.buildSystemVariableSource(pluginPackageEntity);

        List<SystemVariables> systemVariablesEntities = systemVariablesMapper.selectAllBySource(source);

        if (systemVariablesEntities == null || systemVariablesEntities.isEmpty()) {
            return resultDtos;
        }

        for (SystemVariables systemVarEntity : systemVariablesEntities) {
            SystemVariableDto dto = buildSystemVariableDto(systemVarEntity);
            resultDtos.add(dto);
        }

        return resultDtos;
    }

    /**
     * 
     * @param packageId
     * @return
     */
    public List<PluginPackageAuthoritiesDto> getAuthoritiesByPackageId(String pluginPackageId) {
        List<PluginPackageAuthoritiesDto> resultDtos = new ArrayList<>();
        
        

        List<PluginPackageAuthorities> authoritiesEntities = pluginPackageAuthoritiesMapper
                .selectAllByPackage(pluginPackageId);

        if (authoritiesEntities == null || authoritiesEntities.isEmpty()) {
            return resultDtos;
        }

        for (PluginPackageAuthorities entity : authoritiesEntities) {
            PluginPackageAuthoritiesDto dto = new PluginPackageAuthoritiesDto();
            dto.setId(entity.getId());
            dto.setMenuCode(entity.getMenuCode());
            dto.setPluginPackageId(entity.getPluginPackageId());
            dto.setRoleName(entity.getRoleName());

            resultDtos.add(dto);
        }

        return resultDtos;

    }

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    public PluginPackageRuntimeResouceDto getResourcesByPackageId(String pluginPackageId) {

        PluginPackageRuntimeResouceDto resultDto = new PluginPackageRuntimeResouceDto();
        List<PluginPackageRuntimeResourcesDocker> dockerEntities = pluginPackageRuntimeResourcesDockerMapper
                .selectAllByPackage(pluginPackageId);

        List<PluginPackageRuntimeResourcesDockerDto> dockerDtos = new ArrayList<>();
        if (dockerEntities != null) {
            for (PluginPackageRuntimeResourcesDocker entity : dockerEntities) {
                PluginPackageRuntimeResourcesDockerDto dto = new PluginPackageRuntimeResourcesDockerDto();
                dto.setContainerName(entity.getContainerName());
                dto.setEnvVariables(entity.getEnvVariables());
                dto.setId(entity.getId());
                dto.setImageName(entity.getImageName());
                dto.setPluginPackageId(entity.getPluginPackageId());
                dto.setPortBindings(entity.getPortBindings());
                dto.setVolumeBindings(entity.getVolumeBindings());

                dockerDtos.add(dto);
            }
        }

        resultDto.setDockerSet(dockerDtos);

        List<PluginPackageRuntimeResourcesMysqlDto> mysqlDtos = new ArrayList<>();
        List<PluginPackageRuntimeResourcesMysql> mysqlEntities = pluginPackageRuntimeResourcesMysqlMapper
                .selectAllByPackage(pluginPackageId);
        if (mysqlEntities != null) {
            for (PluginPackageRuntimeResourcesMysql entity : mysqlEntities) {
                PluginPackageRuntimeResourcesMysqlDto dto = new PluginPackageRuntimeResourcesMysqlDto();
                dto.setId(entity.getId());
                dto.setInitFileName(entity.getInitFileName());
                dto.setPluginPackageId(entity.getPluginPackageId());
                dto.setSchemaName(entity.getSchemaName());
                dto.setUpgradeFileName(dto.getUpgradeFileName());

                mysqlDtos.add(dto);
            }
        }

        resultDto.setMysqlSet(mysqlDtos);

        List<PluginPackageRuntimeResourcesS3Dto> s3Dtos = new ArrayList<>();

        List<PluginPackageRuntimeResourcesS3> s3Entities = pluginPackageRuntimeResourcesS3Mapper
                .selectAllByPackage(pluginPackageId);

        if (s3Entities != null) {
            for (PluginPackageRuntimeResourcesS3 entity : s3Entities) {
                PluginPackageRuntimeResourcesS3Dto dto = new PluginPackageRuntimeResourcesS3Dto();
                dto.setBucketName(entity.getBucketName());
                dto.setId(entity.getId());
                dto.setPluginPackageId(entity.getPluginPackageId());

                s3Dtos.add(dto);
            }
        }

        resultDto.setS3Set(s3Dtos);

        return resultDto;
    }

    /**
     * 
     * @param packageId
     * @return
     */
    public List<PluginConfigGroupByNameDto> getRichPluginConfigsByPackageId(String pluginPackageId) {
        List<PluginConfigGroupByNameDto> resultDtos = new ArrayList<>();

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            return resultDtos;
        }

        List<PluginConfigs> pluginConfigsEntities = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackageId);
        if (pluginConfigsEntities == null || pluginConfigsEntities.isEmpty()) {
            return resultDtos;
        }

        Map<String, PluginConfigGroupByNameDto> nameAndConfigMap = new HashMap<String, PluginConfigGroupByNameDto>();
        for (PluginConfigs pluginConfigsEntity : pluginConfigsEntities) {
            PluginConfigDto pluginConfigDto = buildRichPluginConfigDto(pluginConfigsEntity, pluginPackageEntity);
            Map<String, List<String>> permToRoles = fetchPermissionToRoles(pluginConfigsEntity);
            pluginConfigDto.addAllPermissionToRole(permToRoles);

            String name = pluginConfigDto.getName();
            PluginConfigGroupByNameDto groupedDto = nameAndConfigMap.get(name);
            if (groupedDto == null) {
                groupedDto = new PluginConfigGroupByNameDto();
                groupedDto.setPluginConfigName(name);

                nameAndConfigMap.put(name, groupedDto);
            }

            groupedDto.addPluginConfigDto(pluginConfigDto);
        }

        resultDtos.addAll(nameAndConfigMap.values());
        return resultDtos;
    }

    /**
     * 
     * @param packageId
     * @return
     */
    public List<PluginConfigGroupByNameDto> getPluginConfigsByPackageId(String pluginPackageId) {
        List<PluginConfigGroupByNameDto> resultDtos = new ArrayList<>();

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            return resultDtos;
        }

        List<PluginConfigs> pluginConfigsEntities = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackageId);
        if (pluginConfigsEntities == null || pluginConfigsEntities.isEmpty()) {
            return resultDtos;
        }

        Map<String, PluginConfigGroupByNameDto> nameAndConfigMap = new HashMap<String, PluginConfigGroupByNameDto>();
        for (PluginConfigs pluginConfigsEntity : pluginConfigsEntities) {
            PluginConfigDto pluginConfigDto = buildPluginConfigDto(pluginConfigsEntity, pluginPackageEntity);
            Map<String, List<String>> permToRoles = fetchPermissionToRoles(pluginConfigsEntity);
            pluginConfigDto.addAllPermissionToRole(permToRoles);

            String name = pluginConfigDto.getName();
            PluginConfigGroupByNameDto groupedDto = nameAndConfigMap.get(name);
            if (groupedDto == null) {
                groupedDto = new PluginConfigGroupByNameDto();
                groupedDto.setPluginConfigName(name);

                nameAndConfigMap.put(name, groupedDto);
            }

            groupedDto.addPluginConfigDto(pluginConfigDto);
        }

        resultDtos.addAll(nameAndConfigMap.values());
        
        Collections.sort(resultDtos, new Comparator<PluginConfigGroupByNameDto>() {

            @Override
            public int compare(PluginConfigGroupByNameDto o1, PluginConfigGroupByNameDto o2) {
                return o1.getPluginConfigName().compareTo(o2.getPluginConfigName());
            }
            
        });
        return resultDtos;
    }

    /**
     * 
     * @param packageId
     * @return
     */
    public List<PluginDeclarationDto> getPluginConfigOutlinesByPackageId(String pluginPackageId) {
        List<PluginDeclarationDto> pluginDeclarationDtos = new ArrayList<>();

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        List<PluginConfigs> configEntities = pluginConfigsMapper.selectAllByPackageAndRegNameIsNull(pluginPackageId);
        if (configEntities == null || configEntities.isEmpty()) {
            return pluginDeclarationDtos;
        }

        for (PluginConfigs configEntity : configEntities) {
            PluginDeclarationDto pdDto = buildPluginDeclarationDto(configEntity, pluginPackageEntity);
            List<PluginConfigs> pluginConfigsWithNameEntities = pluginConfigsMapper
                    .selectAllByPackageAndNameAndRegNameIsNotNull(configEntity.getPluginPackageId(),
                            configEntity.getName());

            List<PluginConfigOutlineDto> childPdDtos = new ArrayList<>();
            if (pluginConfigsWithNameEntities != null) {
                for (PluginConfigs pluginConfigsWithNameEntity : pluginConfigsWithNameEntities) {
                    PluginConfigOutlineDto pcDto = buildEnablePluginConfigDto(pluginConfigsWithNameEntity,
                            pluginPackageEntity);
                    childPdDtos.add(pcDto);
                }
            }
            pdDto.setPluginConfigs(childPdDtos);
            pluginDeclarationDtos.add(pdDto);
        }

        return pluginDeclarationDtos;
    }

    /**
     * Enable plug-in configurations in batch.
     * 
     * @param pluginPackageId
     * @param pluginConfigDtos
     */
    public void enablePluginConfigsInBatchByPackageId(String pluginPackageId,
            List<PluginDeclarationDto> pluginConfigDtos) {
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("3087", "Package ID is blank.");
        }
        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3088",
                    String.format("Such plugin package with ID %s does not exist.", pluginPackageId));
        }
        if (PluginPackages.UNREGISTERED.equalsIgnoreCase(pluginPackageEntity.getStatus())
                || PluginPackages.DECOMMISSIONED.equalsIgnoreCase(pluginPackageEntity.getStatus())) {
            throw new WecubeCoreException("3089",
                    "Plugin package is not in valid status [REGISTERED, RUNNING, STOPPED] to enable plugin.");
        }

        List<PluginConfigOutlineDto> privilegedPluginConfigDtos = new ArrayList<>();

        for (PluginDeclarationDto pluginConfigDto : pluginConfigDtos) {
            List<PluginConfigOutlineDto> pluginConfigOutlineDto = pluginConfigDto.getPluginConfigs();
            for (PluginConfigOutlineDto configOutlineDto : pluginConfigOutlineDto) {
                if (configOutlineDto.getHasMgmtPermission()) {
                    privilegedPluginConfigDtos.add(configOutlineDto);
                }
            }
        }

        if (privilegedPluginConfigDtos.isEmpty()) {
            return;
        }

        for (PluginConfigOutlineDto privilegedPluginConfigDto : privilegedPluginConfigDtos) {
            if (!validateCurrentUserPermission(privilegedPluginConfigDto.getId(), PluginConfigRoles.PERM_TYPE_MGMT)) {
                throw new WecubeCoreException("3090", "Lack of privilege to perform such operation.");
            }
        }

        for (PluginConfigOutlineDto privilegedPluginConfigDto : privilegedPluginConfigDtos) {
            PluginConfigs pluginConfigEntity = pluginConfigsMapper
                    .selectByPrimaryKey(privilegedPluginConfigDto.getId());
            if (pluginConfigEntity != null) {
                pluginConfigEntity.setStatus(privilegedPluginConfigDto.getStatus());
                pluginConfigsMapper.updateByPrimaryKeySelective(pluginConfigEntity);
            }
        }

    }

    private PluginConfigOutlineDto buildEnablePluginConfigDto(PluginConfigs pluginConfig,
            PluginPackages pluginPackageEntity) {
        PluginConfigOutlineDto enablePluginConfigDto = new PluginConfigOutlineDto();
        enablePluginConfigDto.setId(pluginConfig.getId());
        enablePluginConfigDto.setPluginPackageId(pluginPackageEntity.getId());
        enablePluginConfigDto.setName(pluginConfig.getName());
        enablePluginConfigDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
        enablePluginConfigDto.setRegisterName(pluginConfig.getRegisterName());
        enablePluginConfigDto.setStatus(pluginConfig.getStatus());
        enablePluginConfigDto.setHasMgmtPermission(
                validateCurrentUserPermission(pluginConfig.getId(), PluginConfigRoles.PERM_TYPE_MGMT));
        return enablePluginConfigDto;
    }

    private Boolean validateCurrentUserPermission(String pluginConfigId, String permission) {
        String currentUsername = AuthenticationContextHolder.getCurrentUsername();
        if (StringUtils.isBlank(currentUsername)) {
            return false;
        }

        Set<String> currUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoles == null || currUserRoles.isEmpty()) {
            return false;
        }

        List<PluginConfigRoles> pluginAuthConfigEntities = this.pluginConfigRolesMapper
                .selectAllByPluginConfigAndPerm(pluginConfigId, permission);

        if (pluginAuthConfigEntities == null || pluginAuthConfigEntities.isEmpty()) {
            return false;
        }

        boolean hasAuthority = false;
        for (PluginConfigRoles auth : pluginAuthConfigEntities) {
            String authRole = auth.getRoleName();
            if (StringUtils.isBlank(authRole)) {
                continue;
            }
            if (CollectionUtils.collectionContains(currUserRoles, authRole)) {
                hasAuthority = true;
                break;
            }
        }

        return hasAuthority;
    }

    private PluginDeclarationDto buildPluginDeclarationDto(PluginConfigs pluginConfig,
            PluginPackages pluginPackageEntity) {
        PluginDeclarationDto pluginDeclarationDto = new PluginDeclarationDto();
        pluginDeclarationDto.setId(pluginConfig.getId());
        pluginDeclarationDto.setPluginPackageId(pluginPackageEntity.getId());
        pluginDeclarationDto.setName(pluginConfig.getName());
        pluginDeclarationDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
        pluginDeclarationDto.setRegisterName(pluginConfig.getRegisterName());
        pluginDeclarationDto.setStatus(pluginConfig.getStatus());
        return pluginDeclarationDto;
    }

    private PluginConfigDto buildPluginConfigDto(PluginConfigs configEntity, PluginPackages pluginPackageEntity) {
        PluginConfigDto resultDto = new PluginConfigDto();
        resultDto.setId(configEntity.getId());
        resultDto.setName(configEntity.getName());
        resultDto.setTargetEntityWithFilterRule(configEntity.getTargetEntityWithFilterRule());
        resultDto.setRegisterName(configEntity.getRegisterName());
        resultDto.setPluginPackageId(pluginPackageEntity.getId());
        resultDto.setStatus(configEntity.getStatus());
        return resultDto;
    }

    private PluginConfigDto buildRichPluginConfigDto(PluginConfigs configEntity, PluginPackages pluginPackageEntity) {
        PluginConfigDto resultDto = new PluginConfigDto();
        resultDto.setId(configEntity.getId());
        resultDto.setName(configEntity.getName());
        resultDto.setTargetEntityWithFilterRule(configEntity.getTargetEntityWithFilterRule());
        resultDto.setRegisterName(configEntity.getRegisterName());
        resultDto.setPluginPackageId(pluginPackageEntity.getId());
        resultDto.setStatus(configEntity.getStatus());

        List<PluginConfigInterfaces> intfEntities = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(configEntity.getId());

        if (intfEntities != null) {
            List<PluginConfigInterfaceDto> intfDtos = new ArrayList<>();
            for (PluginConfigInterfaces intfEntity : intfEntities) {
                PluginConfigInterfaceDto intfDto = buildRichPluginConfigInterfaceDto(intfEntity, configEntity);
                intfDtos.add(intfDto);
            }

            resultDto.setInterfaces(intfDtos);
        }
        return resultDto;
    }

    private PluginConfigInterfaceDto buildRichPluginConfigInterfaceDto(PluginConfigInterfaces intfEntity,
            PluginConfigs pluginConfigsEntity) {

        PluginConfigInterfaceDto resultDto = new PluginConfigInterfaceDto();
        resultDto.setId(intfEntity.getId());
        resultDto.setPluginConfigId(pluginConfigsEntity.getId());

        resultDto.setPath(intfEntity.getPath());
        resultDto.setServiceName(intfEntity.getServiceName());
        resultDto.setServiceDisplayName(intfEntity.getServiceDisplayName());
        resultDto.setAction(intfEntity.getAction());
        resultDto.setHttpMethod(intfEntity.getHttpMethod());
        resultDto.setIsAsyncProcessing(intfEntity.getIsAsyncProcessing());
        resultDto.setFilterRule(intfEntity.getFilterRule());
        resultDto.setDescription(intfEntity.getDescription());

        List<PluginConfigInterfaceParameters> inputParamEntities = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intfEntity.getId(), Constants.TYPE_INPUT);

        if (inputParamEntities != null) {
            List<PluginConfigInterfaceParameterDto> inputParamDtos = new ArrayList<>();
            for (PluginConfigInterfaceParameters paramEntity : inputParamEntities) {
                PluginConfigInterfaceParameterDto inputParamDto = buildRichPluginConfigInterfaceParameterDto(
                        paramEntity, intfEntity);
                inputParamDtos.add(inputParamDto);
            }

            resultDto.setInputParameters(inputParamDtos);
        }

        List<PluginConfigInterfaceParameters> outputParamEntities = this.pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                        Constants.TYPE_OUTPUT);

        if (outputParamEntities != null) {
            List<PluginConfigInterfaceParameterDto> outputParamDtos = new ArrayList<>();
            for (PluginConfigInterfaceParameters paramEntity : outputParamEntities) {
                PluginConfigInterfaceParameterDto outputParamDto = buildRichPluginConfigInterfaceParameterDto(
                        paramEntity, intfEntity);
                outputParamDtos.add(outputParamDto);
            }

            resultDto.setOutputParameters(outputParamDtos);
        }

        return resultDto;
    }

    private PluginConfigInterfaceParameterDto buildRichPluginConfigInterfaceParameterDto(
            PluginConfigInterfaceParameters entity, PluginConfigInterfaces pluginConfigIntfEntity) {
        PluginConfigInterfaceParameterDto dto = new PluginConfigInterfaceParameterDto();
        dto.setId(entity.getId());
        dto.setPluginConfigInterfaceId(pluginConfigIntfEntity.getId());
        dto.setType(entity.getType());
        dto.setName(entity.getName());
        dto.setDataType(entity.getDataType());
        dto.setMappingType(entity.getMappingType());
        dto.setMappingEntityExpression(entity.getMappingEntityExpression());
        dto.setMappingSystemVariableName(entity.getMappingSystemVariableName());
        dto.setRequired(entity.getRequired());
        dto.setSensitiveData(entity.getSensitiveData());
        
        dto.setDescription(entity.getDescription());
        dto.setMappingValue(entity.getMappingValue());
        dto.setMultiple(entity.getMultiple());
        
        return dto;
    }

    private Map<String, List<String>> fetchPermissionToRoles(PluginConfigs pluginConfig) {
        Map<String, List<String>> permissionToRoles = new HashMap<String, List<String>>();
        if (pluginConfig == null) {
            return permissionToRoles;
        }

        if (StringUtils.isBlank(pluginConfig.getId())) {
            return permissionToRoles;
        }

        List<PluginConfigRoles> configRolesEntities = pluginConfigRolesMapper
                .selectAllByPluginConfig(pluginConfig.getId());
        if (configRolesEntities == null || configRolesEntities.isEmpty()) {
            return permissionToRoles;
        }

        for (PluginConfigRoles permEntity : configRolesEntities) {
            List<String> roleNamesOfPerm = permissionToRoles.get(permEntity.getPermType());
            if (roleNamesOfPerm == null) {
                roleNamesOfPerm = new ArrayList<String>();
                permissionToRoles.put(permEntity.getPermType(), roleNamesOfPerm);
            }
            roleNamesOfPerm.add(permEntity.getRoleName());
        }
        return permissionToRoles;
    }

    private SystemVariableDto buildSystemVariableDto(SystemVariables entity) {
        SystemVariableDto dto = new SystemVariableDto();
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPackageName(entity.getPackageName());
        dto.setScope(entity.getScope());
        dto.setSource(entity.getSource());
        dto.setStatus(entity.getStatus());
        dto.setValue(entity.getValue());

        return dto;
    }

    private MenuItemDto buildPackageMenuItemDto(PluginPackageMenus packageMenu, MenuItems menuItem) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(packageMenu.getId());
        pluginPackageMenuDto.setCategory(packageMenu.getCategory());
        pluginPackageMenuDto.setCode(packageMenu.getCode());
        pluginPackageMenuDto.setSource(packageMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(menuItem.getMenuOrder() * 10000 + packageMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
        pluginPackageMenuDto.setLocalDisplayName(packageMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(packageMenu.getPath());
        pluginPackageMenuDto.setActive(packageMenu.getActive());
        return pluginPackageMenuDto;
    }

    private List<MenuItemDto> fetchAllSysMenuItems() {
        List<MenuItemDto> sysMenuItemDtos = new ArrayList<>();

        List<MenuItems> sysMenuItemsEntities = menuItemsMapper.selectAll();

        if (sysMenuItemsEntities == null || sysMenuItemsEntities.isEmpty()) {
            return sysMenuItemDtos;
        }

        for (MenuItems sysMenuItemsEntity : sysMenuItemsEntities) {
            MenuItemDto systemMenuDto = buildSystemMenuItem(sysMenuItemsEntity);
            sysMenuItemDtos.add(systemMenuDto);
        }

        return sysMenuItemDtos;
    }

    private MenuItemDto buildSystemMenuItem(MenuItems systemMenu) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(systemMenu.getId());
        String category = systemMenu.getParentCode();
        if (category != null) {
            pluginPackageMenuDto.setCategory(category);
        }
        pluginPackageMenuDto.setCode(systemMenu.getCode());
        pluginPackageMenuDto.setSource(systemMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(systemMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(systemMenu.getDescription());
        pluginPackageMenuDto.setLocalDisplayName(systemMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(null);
        pluginPackageMenuDto.setActive(true);
        return pluginPackageMenuDto;
    }

    private void appendPackageDependencies(PluginPackageDependencyDto parentDependencyDto,
            List<PluginPackageDependencyDto> totalDependenciesDtos, PluginPackageDependencies depEntity) {
        if (depEntity == null) {
            return;
        }

        PluginPackageDependencyDto dependencyDto = pickoutDependencyByNameAndVersion(
                depEntity.getDependencyPackageName(), depEntity.getDependencyPackageVersion(), totalDependenciesDtos);
        if (dependencyDto == null) {
            dependencyDto = new PluginPackageDependencyDto();
            dependencyDto.setPackageName(depEntity.getDependencyPackageName());
            dependencyDto.setVersion(depEntity.getDependencyPackageVersion());

            totalDependenciesDtos.add(dependencyDto);

        }

        parentDependencyDto.addDependency(dependencyDto);

        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllByNameAndVersion(
                depEntity.getDependencyPackageName(), depEntity.getDependencyPackageVersion());

        if (pluginPackagesEntities == null || pluginPackagesEntities.isEmpty()) {
            return;
        }

        PluginPackages pluginPackgesEntity = pluginPackagesEntities.get(0);

        List<PluginPackageDependencies> subDependencyEntities = pluginPackageDependenciesMapper
                .selectAllByPackage(pluginPackgesEntity.getId());

        if (subDependencyEntities == null || subDependencyEntities.isEmpty()) {
            return;
        }

        for (PluginPackageDependencies subDepEntity : subDependencyEntities) {
            appendPackageDependencies(dependencyDto, totalDependenciesDtos, subDepEntity);
        }

    }

    private PluginPackageDependencyDto pickoutDependencyByNameAndVersion(String name, String version,
            List<PluginPackageDependencyDto> totalDependencies) {
        for (PluginPackageDependencyDto d : totalDependencies) {
            if (name.equals(d.getPackageName()) && version.equals(d.getVersion())) {
                return d;
            }
        }

        return null;
    }

    private void removePluginUiResourcesIfRequired(PluginPackages pluginPackage) {
        if (pluginPackage.getUiPackageIncluded() == null || (!pluginPackage.getUiPackageIncluded())) {
            return;
        }

        String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
                + File.separator + pluginPackage.getVersion() + File.separator;

        if (!remotePath.equals("/") && !remotePath.equals(".")) {
            String mkdirCmd = String.format("rm -rf %s", remotePath);
            try {
                List<String> staticResourceIps = StringUtilsEx
                        .splitByComma(pluginProperties.getStaticResourceServerIp());
                for (String staticResourceIp : staticResourceIps) {
                    commandService.runAtRemote(staticResourceIp, pluginProperties.getStaticResourceServerUser(),
                            pluginProperties.getStaticResourceServerPassword(),
                            pluginProperties.getStaticResourceServerPort(), mkdirCmd);
                }
            } catch (Exception e) {
                log.error("errors while remove plugin resources:{}", remotePath, e);
                log.error("Run command [rm] meet error: ", e.getMessage());
                throw new WecubeCoreException("3113", String.format("Run command [rm] meet error: %s", e.getMessage()),
                        e.getMessage());
            }
        }
    }

    private void removeLocalDockerImageFiles(PluginPackages pluginPackage) {
        // Remove related docker image file
        String versionPath = SystemUtils.getTempFolderPath() + pluginPackage.getName() + "-"
                + pluginPackage.getVersion() + "/";
        File versionDirectory = new File(versionPath);
        try {
            log.info("Delete directory: {}", versionPath);
            FileUtils.deleteDirectory(versionDirectory);
        } catch (IOException e) {
            log.error("Remove plugin package file failed: {}", e);
            throw new WecubeCoreException("3107", "Remove plugin package file failed.");
        }
    }

    private void deactivateSystemVariables(PluginPackages pluginPackage) {
        List<SystemVariables> systemVariablesEntities = systemVariablesMapper
                .selectAllBySource(PluginPackages.buildSystemVariableSource(pluginPackage));
        if (systemVariablesEntities == null || systemVariablesEntities.isEmpty()) {
            return;
        }

        for (SystemVariables systemVariablesEntity : systemVariablesEntities) {
            if (SystemVariables.ACTIVE.equalsIgnoreCase(systemVariablesEntity.getStatus())
                    && pluginPackage.getName().equals(systemVariablesEntity.getScope())) {
                systemVariablesEntity.setStatus(SystemVariables.INACTIVE);
                systemVariablesMapper.updateByPrimaryKeySelective(systemVariablesEntity);
            }
        }
    }

    private void disableAllPluginConfigs(PluginPackages pluginPackageEntity) {
        List<PluginConfigs> pluginConfigsEntities = pluginConfigsMapper
                .selectAllByPackageAndOrderByConfigName(pluginPackageEntity.getId());

        if (pluginConfigsEntities == null || pluginConfigsEntities.isEmpty()) {
            return;
        }

        for (PluginConfigs pluginConfigsEntity : pluginConfigsEntities) {
            pluginConfigsEntity.setStatus(PluginConfigs.DISABLED);
            pluginConfigsMapper.updateByPrimaryKeySelective(pluginConfigsEntity);
        }
    }

    private void ensureNoPluginInstanceIsRunning(String pluginPackageId) {
        List<PluginInstances> pluginInstanceEntities = pluginInstancesMapper.selectAllByPluginPackage(pluginPackageId);
        if (pluginInstanceEntities == null || pluginInstanceEntities.isEmpty()) {
            return;
        }

        for (PluginInstances pluginInstanceEntity : pluginInstanceEntities) {
            if (PluginInstances.CONTAINER_STATUS_RUNNING.equalsIgnoreCase(pluginInstanceEntity.getContainerStatus())) {
                throw new WecubeCoreException("3108",
                        String.format(
                                "Decommission plugin package [%s] failure. There are still plugin instances are running",
                                pluginPackageId));
            }
        }
    }

    private void deployPluginUiResourcesIfRequired(PluginPackages pluginPackage) {
        if (pluginPackage.getUiPackageIncluded() == null || !pluginPackage.getUiPackageIncluded()) {
            log.info("Such package {} does not include UI resources and no need to remote copy.",
                    pluginPackage.getId());
            return;
        }
        // download UI package from MinIO
        String tmpFolderName = SystemUtils.getTempFolderPath()
                + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

        try {
            downloadAndRemoteCopyUiResourceFiles(pluginPackage, tmpFolderName);
        } finally {
            FileUtils.deleteQuietly(new File(tmpFolderName));
        }

    }

    private void downloadAndRemoteCopyUiResourceFiles(PluginPackages pluginPackage, String tmpFolderName) {
        String tmpDownloadUiZipPath = tmpFolderName + File.separator + pluginProperties.getUiFile();
        log.info("Download UI.zip from S3 to " + tmpDownloadUiZipPath);

        String s3UiPackagePath = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getUiFile();
        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3UiPackagePath, tmpDownloadUiZipPath);

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
                log.info("Scp files from {} to {}", tmpDownloadUiZipPath, remotePath);
                scpService.put(remoteIp, pluginProperties.getStaticResourceServerPort(),
                        pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(), tmpDownloadUiZipPath, remotePath);
            } catch (Exception e) {
                log.error("errors to remotely copy file to :{}", remoteIp, e);
                throw new WecubeCoreException("3111",
                        String.format("Put file to remote host meet error: ", e.getMessage()));
            }
            log.info("scp UI.zip to Static Resource Server - Done");

            // unzip file
            String unzipCmd = String.format("cd %s && unzip %s", remotePath, pluginProperties.getUiFile());
            try {
                log.info("To run ssh command at remote:{}", unzipCmd);
                commandService.runAtRemote(remoteIp, pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(),
                        pluginProperties.getStaticResourceServerPort(), unzipCmd);
            } catch (Exception e) {
                log.error("errors to remotely execute command :{}", unzipCmd, e);
                log.error("Run command [unzip] meet error: ", e.getMessage());
                throw new WecubeCoreException("3112",
                        String.format("Run remote command meet error: %s", e.getMessage()));
            }
        }

        log.info("UI package {} deployment has done...", pluginPackage.getId());
    }

    private PluginPackageInfoDto doRegisterPluginPackage(PluginPackages pluginPackageEntity) {
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

    private void updateSystemVariableStatus(PluginPackages pluginPackage) {
        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllByName(pluginPackage.getName());

        List<String> sourceList = new ArrayList<String>();

        for (PluginPackages p : pluginPackagesEntities) {
            sourceList.add(PluginPackages.buildSystemVariableSource(p));
        }

        List<SystemVariables> systemVariablesEntities = systemVariablesMapper
                .selectAllBySourceList(sourceList);
        for (SystemVariables systemVariableEntity : systemVariablesEntities) {
            String systemVarSource = PluginPackages.buildSystemVariableSource(pluginPackage);
            if (SystemVariables.ACTIVE.equals(systemVariableEntity.getStatus())
                    && !systemVarSource.equals(systemVariableEntity.getSource())) {
                systemVariableEntity.setStatus(SystemVariables.INACTIVE);
                systemVariablesMapper.updateByPrimaryKeySelective(systemVariableEntity);
            }

            if (SystemVariables.INACTIVE.equals(systemVariableEntity.getStatus())
                    && systemVarSource.equals(systemVariableEntity.getSource())) {
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
            throw new WecubeCoreException(msg);
        }
    }

    private void ensurePluginPackageIsAllowedToRegister(PluginPackages pluginPackage) {
        if (!PluginPackages.UNREGISTERED.equalsIgnoreCase(pluginPackage.getStatus())) {
            String errorMessage = String.format(
                    "Failed to register PluginPackage[%s/%s] as it is not in UNREGISTERED status [%s]",
                    pluginPackage.getName(), pluginPackage.getVersion(), pluginPackage.getStatus());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
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
        dto.setUiPackageIncluded(entity.getUiPackageIncluded() == null ? false : entity.getUiPackageIncluded());
        dto.setUploadTimestamp(DateUtils.dateToString(entity.getUploadTimestamp()));
        dto.setVersion(entity.getVersion());

        return dto;
    }

}
