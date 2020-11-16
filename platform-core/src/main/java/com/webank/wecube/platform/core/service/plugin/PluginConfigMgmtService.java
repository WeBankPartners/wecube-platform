package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.dto.PluginConfigRoleRequestDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceParameterDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.RichPluginConfigInterfaces;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDataModelMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.utils.CollectionUtils;
import com.webank.wecube.platform.core.utils.VersionUtils;
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

    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;

    @Autowired
    private PluginConfigInterfaceParametersMapper pluginConfigInterfaceParametersMapper;

    @Autowired
    private PluginPackageDataModelMapper pluginPackageDataModelMapper;

    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntitiesMapper;
    
    
    public List<PluginConfigInterfaceDto> queryAllLatestEnabledPluginConfigInterface() {
        List<PluginConfigInterfaceDto> resultIntfDtos = fetchAllAuthorizedLatestEnabledIntfs();
        return resultIntfDtos;
        
        //--------------------------
        Optional<List<PluginConfigInterface>> pluginConfigsOptional = pluginConfigRepository
                .findAllLatestEnabledForAllActivePackages();
        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = newArrayList();
        if (pluginConfigsOptional.isPresent()) {
            List<PluginConfigInterface> pluginConfigInterfaces = pluginConfigsOptional.get();
            pluginConfigInterfaces.forEach(pluginConfigInterface -> pluginConfigInterfaceDtos
                    .add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }

        return filterDtoWithPermissionValidation(pluginConfigInterfaceDtos, PluginConfigRoles.PERM_TYPE_USE);
    }
    
    protected List<PluginConfigInterfaceDto> fetchAllAuthorizedLatestEnabledIntfs(){
        
    }

    /**
     * 
     * @param serviceName
     * @return
     */
    public PluginConfigInterfaces getPluginConfigInterfaceByServiceName(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            throw new WecubeCoreException("Service name cannot be blank to query plugin interface.");
        }
        return fetchLatestPluginConfigInterfacesByServiceName(serviceName);
    }

    protected PluginConfigInterfaces fetchLatestPluginConfigInterfacesByServiceName(String serviceName) {
        List<RichPluginConfigInterfaces> richIntfEntities = pluginConfigInterfacesMapper
                .selectAllByServiceNameAndConfigStatus(serviceName, PluginConfigs.ENABLED);

        if (richIntfEntities == null || richIntfEntities.isEmpty()) {
            throw new WecubeCoreException("3058",
                    String.format("Plugin interface not found for serviceName [%s].", serviceName), serviceName);
        }

        richIntfEntities.sort(new Comparator<RichPluginConfigInterfaces>() {

            @Override
            public int compare(RichPluginConfigInterfaces o1, RichPluginConfigInterfaces o2) {
                return VersionUtils.compare(o1.getPluginPackageVersion(), o2.getPluginPackageVersion());
            }
        });

        RichPluginConfigInterfaces latestIntfEntity = richIntfEntities.get(richIntfEntities.size() - 1);

        PluginConfigInterfaces resultIntfEntity = fetchRichPluginConfigInterfacesById(latestIntfEntity.getId());
        if (resultIntfEntity == null) {
            throw new WecubeCoreException("3058",
                    String.format("Plugin interface not found for serviceName [%s].", serviceName), serviceName);
        }
        return resultIntfEntity;

    }

    protected PluginConfigInterfaces fetchRichPluginConfigInterfacesById(String intfId) {
        PluginConfigInterfaces intfEntity = pluginConfigInterfacesMapper.selectByPrimaryKey(intfId);
        if (intfEntity == null) {
            return null;
        }

        PluginConfigs pluginConfigEntity = pluginConfigsMapper.selectByPrimaryKey(intfEntity.getPluginConfigId());
        if (pluginConfigEntity != null) {
            intfEntity.setPluginConfig(pluginConfigEntity);
            PluginPackages pluginPackageEntity = pluginPackagesMapper
                    .selectByPrimaryKey(pluginConfigEntity.getPluginPackageId());
            pluginConfigEntity.setPluginPackage(pluginPackageEntity);
        }

        List<PluginConfigInterfaceParameters> inputParamEntities = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intfEntity.getId(), PluginConfigInterfaceParameters.TYPE_INPUT);
        if(inputParamEntities != null){
            for(PluginConfigInterfaceParameters inputParam : inputParamEntities){
                inputParam.setPluginConfigInterface(intfEntity);
                intfEntity.addInputParameters(inputParam);
            }
        }

        List<PluginConfigInterfaceParameters> outputParamEntities = pluginConfigInterfaceParametersMapper
                .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                        PluginConfigInterfaceParameters.TYPE_OUTPUT);

        if(outputParamEntities != null){
            for(PluginConfigInterfaceParameters outputParam : outputParamEntities){
                outputParam.setPluginConfigInterface(intfEntity);
                intfEntity.addOutputParameters(outputParam);
            }
        }

        return intfEntity;

    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    public PluginConfigDto disablePlugin(String pluginConfigId) {

        PluginConfigs pluginConfigsEntity = pluginConfigsMapper.selectByPrimaryKey(pluginConfigId);

        if (pluginConfigsEntity == null) {
            throw new WecubeCoreException("3057", "PluginConfig not found for id: " + pluginConfigId);
        }

        validateCurrentUserPermission(pluginConfigId, PluginConfigRoles.PERM_TYPE_MGMT);

        pluginConfigsEntity.setStatus(PluginConfigs.DISABLED);
        pluginConfigsMapper.updateByPrimaryKeySelective(pluginConfigsEntity);

        PluginPackages pluginPackageEntity = pluginPackagesMapper
                .selectByPrimaryKey(pluginConfigsEntity.getPluginPackageId());

        PluginConfigDto resultPluginConfigDto = buildPluginConfigDto(pluginConfigsEntity, pluginPackageEntity);
        return resultPluginConfigDto;
    }

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    public PluginConfigDto enablePlugin(String pluginConfigId) {
        PluginConfigs pluginConfigEntity = pluginConfigsMapper.selectByPrimaryKey(pluginConfigId);

        if (pluginConfigEntity == null) {
            throw new WecubeCoreException("3051", "PluginConfig not found for id: " + pluginConfigId);
        }

        if (PluginConfigs.ENABLED.equals(pluginConfigEntity.getStatus())) {
            throw new WecubeCoreException("3053", "Not allow to enable pluginConfig with status: ENABLED");
        }

        PluginPackages pluginPackageEntity = pluginPackagesMapper
                .selectByPrimaryKey(pluginConfigEntity.getPluginPackageId());

        if (pluginPackageEntity == null || PluginPackages.UNREGISTERED.equals(pluginPackageEntity.getStatus())
                || PluginPackages.DECOMMISSIONED.equals(pluginPackageEntity.getStatus())) {
            throw new WecubeCoreException("3052",
                    "Plugin package is not in valid status [REGISTERED, RUNNING, STOPPED] to enable plugin.");
        }

        validateCurrentUserPermission(pluginConfigId, PluginConfigRoles.PERM_TYPE_MGMT);

        validateTargetPackageAndTargetEntity(pluginConfigEntity.getName(), pluginConfigEntity.getTargetPackage(),
                pluginConfigEntity.getTargetEntity());

        // checkMandatoryParameters(pluginConfig);

        pluginConfigEntity.setStatus(PluginConfigs.ENABLED);
        pluginConfigsMapper.updateByPrimaryKeySelective(pluginConfigEntity);

        PluginConfigDto resultPluginConfigDto = buildPluginConfigDto(pluginConfigEntity, pluginPackageEntity);
        return resultPluginConfigDto;
    }

    /**
     * 
     * @param pluginConfigId
     * @param pluginConfigRoleRequestDto
     * @throws WecubeCoreException
     */
    public void deletePluginConfigRoleBinding(String pluginConfigId,
            PluginConfigRoleRequestDto pluginConfigRoleRequestDto) throws WecubeCoreException {

        String permission = pluginConfigRoleRequestDto.getPermission();
        List<String> inputRoleIds = pluginConfigRoleRequestDto.getRoleIds();

        validateCurrentUserPermission(pluginConfigId, PluginConfigRoles.PERM_TYPE_MGMT);

        if (inputRoleIds == null || inputRoleIds.isEmpty()) {
            return;
        }

        deletePluginConfigRoleBindings(pluginConfigId, permission, inputRoleIds);
    }

    /**
     * 
     * @param pluginConfigId
     * @param pluginConfigRoleRequestDto
     */
    public void updatePluginConfigRoleBinding(String pluginConfigId,
            PluginConfigRoleRequestDto pluginConfigRoleRequestDto) {
        if (log.isDebugEnabled()) {
            log.debug("start to update plugin config role binding:{},{}", pluginConfigId, pluginConfigRoleRequestDto);
        }
        String permission = pluginConfigRoleRequestDto.getPermission();
        List<String> inputRoleIds = pluginConfigRoleRequestDto.getRoleIds();
        validateCurrentUserPermission(pluginConfigId, PluginConfigRoles.PERM_TYPE_MGMT);

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

    /**
     * 
     * @param pluginConfigDto
     * @return
     */
    public PluginConfigDto createOrUpdatePluginConfig(PluginConfigDto pluginConfigDto) {
        validatePermission(pluginConfigDto.getPermissionToRole());

        PluginConfigDto resultPluginConfigDto = null;
        if (StringUtils.isBlank(pluginConfigDto.getId())) {
            resultPluginConfigDto = createPluginConfig(pluginConfigDto);
            return resultPluginConfigDto;
        } else {
            resultPluginConfigDto = updatePluginConfig(pluginConfigDto);
            return resultPluginConfigDto;
        }
    }

    protected PluginConfigs fetchRichPluginConfigsEntity(String pluginConfigId) {
        PluginConfigs pluginConfigsEntity = pluginConfigsMapper.selectByPrimaryKey(pluginConfigId);
        if (pluginConfigsEntity == null) {
            return null;
        }

        List<PluginConfigInterfaces> intfEntities = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigsEntity.getId());

        if (intfEntities != null) {
            for (PluginConfigInterfaces intfEntity : intfEntities) {
                List<PluginConfigInterfaceParameters> inputParamEntities = pluginConfigInterfaceParametersMapper
                        .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                                PluginConfigInterfaceParameters.TYPE_INPUT);
                intfEntity.setInputParameters(inputParamEntities);

                List<PluginConfigInterfaceParameters> outputParamEntities = pluginConfigInterfaceParametersMapper
                        .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                                PluginConfigInterfaceParameters.TYPE_OUTPUT);

                intfEntity.setOutputParameters(outputParamEntities);

                pluginConfigsEntity.addPluginConfigInterfaces(intfEntity);
            }
        }

        return pluginConfigsEntity;
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

        List<PluginConfigRoles> pluginAuthConfigEntities = this.pluginConfigRolesMapper
                .selectAllByPluginConfigAndPerm(pluginConfigId, permission);

        if (pluginAuthConfigEntities == null || pluginAuthConfigEntities.isEmpty()) {
            throw new WecubeCoreException("3040", "None plugin authority configured for [%s] [%s]", pluginConfigId,
                    permission);
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

        if (!hasAuthority) {
            StringBuilder rolesStr = new StringBuilder();
            for (PluginConfigRoles auth : pluginAuthConfigEntities) {
                rolesStr.append(auth.getRoleName());
            }
            String errorMsg = String.format(
                    "Current user do not have privilege to update [%s].Must have one of the roles:%s", pluginConfigId,
                    rolesStr.toString());
            throw new WecubeCoreException("3041", errorMsg, pluginConfigId, rolesStr.toString());
        }
    }

    private PluginConfigDto updatePluginConfig(PluginConfigDto pluginConfigDto) {
        String pluginPackageId = pluginConfigDto.getPluginPackageId();
        if (StringUtils.isBlank(pluginPackageId)) {
            throw new WecubeCoreException("Plugin package ID cannot be blank.");
        }

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        PluginConfigs pluginConfigEntity = pluginConfigsMapper.selectByPrimaryKey(pluginConfigDto.getId());
        if (pluginConfigEntity == null) {
            throw new WecubeCoreException("3048", "PluginConfig not found for id: " + pluginConfigDto.getId(),
                    pluginConfigDto.getId());
        }

        if (PluginConfigs.ENABLED.equals(pluginConfigEntity.getStatus())) {
            throw new WecubeCoreException("3045", "Not allow to update plugin with status: ENABLED");
        }

        List<PluginConfigs> sameNamePluginConfigEntities = pluginConfigsMapper
                .selectAllByPluginPackageAndNameAndRegisterName(pluginPackageEntity.getId(), pluginConfigDto.getName(),
                        pluginConfigDto.getRegisterName());

        if (sameNamePluginConfigEntities != null && !sameNamePluginConfigEntities.isEmpty()) {
            for (PluginConfigs pluginConfig : sameNamePluginConfigEntities) {
                if (!pluginConfig.getId().equals(pluginConfigEntity.getId())) {
                    String msg = String.format(
                            "PluginPackage[%s] already have this PluginConfig[%s] with RegisterName[%s]",
                            pluginConfigDto.getPluginPackageId(), pluginConfigDto.getName(),
                            pluginConfigDto.getRegisterName());
                    throw new WecubeCoreException("3044", msg, pluginConfigDto.getPluginPackageId(),
                            pluginConfigDto.getName(), pluginConfigDto.getRegisterName());
                }
            }
        }

        updatePluginConfigsEntity(pluginConfigDto, pluginPackageEntity, pluginConfigEntity);
        pluginConfigEntity.setStatus(PluginConfigs.DISABLED);

        pluginConfigsMapper.updateByPrimaryKeySelective(pluginConfigEntity);
        PluginConfigDto results = buildPluginConfigDto(pluginConfigEntity, pluginPackageEntity);

        Map<String, List<String>> addedPermissionToRole = processUpdatePluginConfigRoleBindings(
                pluginConfigEntity.getId(), pluginConfigDto.getPermissionToRole());

        results.addAllPermissionToRole(addedPermissionToRole);
        return results;
    }

    private PluginConfigs updatePluginConfigsEntity(PluginConfigDto pluginConfigDto, PluginPackages pluginPackage,
            PluginConfigs pluginConfigEntity) {

        pluginConfigEntity.setName(pluginConfigDto.getName());
        if (StringUtils.isNotBlank(pluginConfigDto.getTargetPackage())) {
            pluginConfigEntity.setTargetPackage(pluginConfigDto.getTargetPackage());
        } else {
            pluginConfigEntity.setTargetPackage(pluginPackage.getName());
        }

        if (StringUtils.isNotBlank(pluginConfigDto.getTargetEntity())) {
            pluginConfigEntity.setTargetEntity(pluginConfigDto.getTargetEntity());
        }

        if (StringUtils.isNotBlank(pluginConfigDto.getFilterRule())) {
            pluginConfigEntity.setTargetEntityFilterRule(pluginConfigDto.getFilterRule());
        }
        pluginConfigEntity.setRegisterName(pluginConfigDto.getRegisterName());

        List<PluginConfigInterfaces> intfEntities = pluginConfigInterfacesMapper
                .selectAllByPluginConfig(pluginConfigEntity.getId());

        if (intfEntities != null) {
            for (PluginConfigInterfaces intfEntity : intfEntities) {
                List<PluginConfigInterfaceParameters> inputParamEntities = pluginConfigInterfaceParametersMapper
                        .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                                PluginConfigInterfaceParameters.TYPE_INPUT);
                intfEntity.setInputParameters(inputParamEntities);

                List<PluginConfigInterfaceParameters> outputParamEntities = pluginConfigInterfaceParametersMapper
                        .selectAllByConfigInterfaceAndParamType(intfEntity.getId(),
                                PluginConfigInterfaceParameters.TYPE_OUTPUT);

                intfEntity.setOutputParameters(outputParamEntities);

                pluginConfigEntity.addPluginConfigInterfaces(intfEntity);
            }
        }

        return pluginConfigEntity;
    }

    private PluginConfigDto createPluginConfig(PluginConfigDto pluginConfigDto) {
        String pluginPackageId = pluginConfigDto.getPluginPackageId();

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            throw new WecubeCoreException("3109",
                    String.format("Plugin package id not found for id [%s] ", pluginPackageId), pluginPackageId);
        }

        ensurePluginConfigRegisterNameNotExists(pluginConfigDto);
        PluginConfigs pluginConfigsEntity = buildPluginConfigsEntity(pluginConfigDto, pluginPackageEntity);

        pluginConfigsEntity.setStatus(PluginConfigs.DISABLED);
        pluginConfigsMapper.insert(pluginConfigsEntity);

        PluginConfigDto results = buildPluginConfigDto(pluginConfigsEntity, pluginPackageEntity);

        Map<String, List<String>> addedPermissionToRole = processCreatePluginConfigRoleBindings(
                pluginConfigsEntity.getId(), pluginConfigDto.getPermissionToRole());

        results.addAllPermissionToRole(addedPermissionToRole);

        return results;
    }

    private PluginConfigDto buildPluginConfigDto(PluginConfigs entity, PluginPackages pluginPackageEntity11) {
        PluginConfigDto dto = new PluginConfigDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setTargetEntityWithFilterRule(entity.getTargetEntityWithFilterRule());
        dto.setRegisterName(entity.getRegisterName());
        dto.setPluginPackageId(entity.getPluginPackageId());
        dto.setStatus(entity.getStatus());

        List<PluginConfigInterfaces> interfaceEntities = entity.getInterfaces();
        if (interfaceEntities != null) {
            for (PluginConfigInterfaces intfEntity : interfaceEntities) {
                PluginConfigInterfaceDto intfDto = buildPluginConfigInterfaceDto(intfEntity);

                dto.addInterfaces(intfDto);
            }

        }

        return dto;
    }

    private PluginConfigs buildPluginConfigsEntity(PluginConfigDto pluginConfigDto, PluginPackages pluginPackage) {
        PluginConfigs pluginConfig = new PluginConfigs();
        pluginConfig.setId(LocalIdGenerator.generateId());
        pluginConfig.setPluginPackageId(pluginPackage.getId());
        if (StringUtils.isNotBlank(pluginConfigDto.getTargetPackage())) {
            pluginConfig.setTargetPackage(pluginConfigDto.getTargetPackage());
        } else {
            pluginConfig.setTargetPackage(pluginPackage.getName());
        }

        pluginConfig.setName(pluginConfigDto.getName());

        pluginConfig.setTargetEntity(pluginConfigDto.getTargetEntity());

        pluginConfig.setTargetEntityFilterRule(pluginConfigDto.getFilterRule());
        pluginConfig.setRegisterName(pluginConfigDto.getRegisterName());

        List<PluginConfigInterfaceDto> interfaceDtos = pluginConfigDto.getInterfaces();
        if (interfaceDtos != null) {
            for (PluginConfigInterfaceDto intfDto : interfaceDtos) {
                PluginConfigInterfaces newIntfEntity = buildPluginConfigInterfaces(intfDto, pluginConfig,
                        pluginPackage);
                pluginConfig.addPluginConfigInterfaces(newIntfEntity);
            }
        }

        return pluginConfig;
    }

    private PluginConfigInterfaces buildPluginConfigInterfaces(PluginConfigInterfaceDto intfDto,
            PluginConfigs pluginConfig, PluginPackages pluginPackage) {
        PluginConfigInterfaces intfEntity = new PluginConfigInterfaces();
        intfEntity.setId(LocalIdGenerator.generateId());
        intfEntity.setPluginConfigId(pluginConfig.getId());
        intfEntity.setAction(intfDto.getAction());

        intfEntity.setPath(intfDto.getPath());
        intfEntity.setHttpMethod(intfDto.getHttpMethod());
        intfEntity.setFilterRule(intfDto.getFilterRule());

        intfEntity.setServiceName(intfEntity.generateServiceName(pluginPackage, pluginConfig));
        intfEntity.setServiceDisplayName(intfEntity.generateServiceName(pluginPackage, pluginConfig));
        intfEntity.setIsAsyncProcessing(intfDto.getIsAsyncProcessing());

        pluginConfigInterfacesMapper.insert(intfEntity);

        List<PluginConfigInterfaceParameterDto> inputParameterDtos = intfDto.getInputParameters();

        if (inputParameterDtos != null) {
            List<PluginConfigInterfaceParameters> inputParamEntities = new ArrayList<>();
            for (PluginConfigInterfaceParameterDto paramDto : inputParameterDtos) {
                PluginConfigInterfaceParameters inputParamEntity = buildPluginConfigInterfaceParameters(
                        PluginConfigInterfaceParameters.TYPE_INPUT, paramDto, pluginPackage, pluginConfig, intfEntity);
                inputParamEntities.add(inputParamEntity);
            }

            intfEntity.setInputParameters(inputParamEntities);
        }

        List<PluginConfigInterfaceParameterDto> outputParameterDtos = intfDto.getOutputParameters();

        if (outputParameterDtos != null) {
            List<PluginConfigInterfaceParameters> outputParamEntities = new ArrayList<>();
            for (PluginConfigInterfaceParameterDto paramDto : outputParameterDtos) {
                PluginConfigInterfaceParameters outputParamEntity = buildPluginConfigInterfaceParameters(
                        PluginConfigInterfaceParameters.TYPE_OUTPUT, paramDto, pluginPackage, pluginConfig, intfEntity);
                outputParamEntities.add(outputParamEntity);
            }

            intfEntity.setOutputParameters(outputParamEntities);
        }

        return intfEntity;
    }

    private PluginConfigInterfaceParameters buildPluginConfigInterfaceParameters(String type,
            PluginConfigInterfaceParameterDto paramDto, PluginPackages pluginPackage, PluginConfigs pluginConfig,
            PluginConfigInterfaces intfEntity) {
        PluginConfigInterfaceParameters paramEntity = new PluginConfigInterfaceParameters();

        paramEntity.setId(LocalIdGenerator.generateId());
        paramEntity.setPluginConfigInterfaceId(intfEntity.getId());

        paramEntity.setName(paramDto.getName());
        paramEntity.setType(type);
        paramEntity.setDataType(paramDto.getDataType());
        paramEntity.setMappingType(paramDto.getMappingType());
        paramEntity.setMappingEntityExpression(paramDto.getMappingEntityExpression());
        paramEntity.setMappingSystemVariableName(paramDto.getMappingSystemVariableName());
        paramEntity.setRequired(paramDto.getRequired());

        paramEntity.setSensitiveData(paramDto.getSensitiveData());

        pluginConfigInterfaceParametersMapper.insert(paramEntity);

        return paramEntity;
    }

    private PluginConfigInterfaceDto buildPluginConfigInterfaceDto(PluginConfigInterfaces intfEntity) {
        PluginConfigInterfaceDto dto = new PluginConfigInterfaceDto();
        dto.setId(intfEntity.getId());
        dto.setPluginConfigId(intfEntity.getPluginConfigId());

        dto.setPath(intfEntity.getPath());
        dto.setServiceName(intfEntity.getServiceName());
        dto.setServiceDisplayName(intfEntity.getServiceDisplayName());
        dto.setAction(intfEntity.getAction());
        dto.setHttpMethod(intfEntity.getHttpMethod());
        dto.setIsAsyncProcessing(intfEntity.getIsAsyncProcessing());
        dto.setFilterRule(intfEntity.getFilterRule());

        List<PluginConfigInterfaceParameters> inputParameterEntities = intfEntity.getInputParameters();
        if (inputParameterEntities != null) {
            List<PluginConfigInterfaceParameterDto> inputParamDtos = new ArrayList<>();
            for (PluginConfigInterfaceParameters paramEntity : inputParameterEntities) {
                PluginConfigInterfaceParameterDto paramDto = buildPluginConfigInterfaceParameterDto(paramEntity);
                inputParamDtos.add(paramDto);
            }

            dto.setInputParameters(inputParamDtos);
        }

        List<PluginConfigInterfaceParameters> outputParameterEntities = intfEntity.getOutputParameters();
        if (outputParameterEntities != null) {
            List<PluginConfigInterfaceParameterDto> outputParamDtos = new ArrayList<>();
            for (PluginConfigInterfaceParameters paramEntity : inputParameterEntities) {
                PluginConfigInterfaceParameterDto paramDto = buildPluginConfigInterfaceParameterDto(paramEntity);
                outputParamDtos.add(paramDto);
            }

            dto.setOutputParameters(outputParamDtos);
        }

        return dto;
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

    private PluginConfigInterfaceParameterDto buildPluginConfigInterfaceParameterDto(
            PluginConfigInterfaceParameters entity) {
        PluginConfigInterfaceParameterDto dto = new PluginConfigInterfaceParameterDto();
        dto.setId(entity.getId());
        dto.setPluginConfigInterfaceId(entity.getPluginConfigInterfaceId());
        dto.setType(entity.getType());
        dto.setName(entity.getName());
        dto.setDataType(entity.getDataType());
        dto.setMappingType(entity.getMappingType());
        dto.setMappingEntityExpression(entity.getMappingEntityExpression());
        dto.setMappingSystemVariableName(entity.getMappingSystemVariableName());
        dto.setRequired(entity.getRequired());
        dto.setSensitiveData(entity.getSensitiveData());
        return dto;
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

    private List<String> getExistRoleIdsOfPluginConfigAndPermission(String pluginConfigId, String permission) {
        List<String> existRoleIds = new ArrayList<String>();
        List<PluginConfigRoles> entities = this.pluginConfigRolesMapper.selectAllByPluginConfigAndPerm(pluginConfigId,
                permission);
        for (PluginConfigRoles e : entities) {
            existRoleIds.add(e.getRoleId());
        }

        return existRoleIds;
    }

    private void addPluginConfigRoleBindings(String pluginConfigId, String permission, List<String> roleIdsToAdd) {
        if (log.isDebugEnabled()) {
            log.debug("roles to add for {} {}:{}", pluginConfigId, permission, roleIdsToAdd);
        }

        for (String roleId : roleIdsToAdd) {
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
        }

    }

    private void deletePluginConfigRoleBindings(String pluginConfigId, String permission,
            List<String> roleIdsToRemove) {
        if (log.isDebugEnabled()) {
            log.debug("roles to remove for {} {}:{}", pluginConfigId, permission, roleIdsToRemove);
        }
        List<PluginConfigRoles> entities = this.pluginConfigRolesMapper.selectAllByPluginConfigAndPerm(pluginConfigId,
                permission);
        for (String roleId : roleIdsToRemove) {
            PluginConfigRoles entity = pickoutPluginAuthEntityByRoleId(entities, roleId);
            if (entity != null) {
                this.pluginConfigRolesMapper.deleteByPrimaryKey(entity.getId());
            }
        }

    }

    private PluginConfigRoles pickoutPluginAuthEntityByRoleId(List<PluginConfigRoles> entities, String roleId) {
        for (PluginConfigRoles entity : entities) {
            if (roleId.equals(entity.getRoleId())) {
                return entity;
            }
        }

        return null;
    }

    private void validateTargetPackageAndTargetEntity(String pluginConfigName, String targetPackage,
            String targetEntity) {
        if (StringUtils.isNotBlank(targetPackage) || StringUtils.isNotBlank(targetEntity)) {
            return;
        }
        PluginPackageDataModel dataModelEntity = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(targetPackage);
        if (dataModelEntity == null) {
            throw new WecubeCoreException("3049", "Data model not exists for package name [%s]");
        }

        Integer dataModelVersion = dataModelEntity.getVersion();
        List<PluginPackageEntities> pluginPackageEntitiesList = pluginPackageEntitiesMapper
                .selectAllByPackageNameAndEntityNameAndDataModelVersion(targetPackage, targetEntity, dataModelVersion);

        if (pluginPackageEntitiesList == null || pluginPackageEntitiesList.isEmpty()) {
            String errorMessage = String.format(
                    "PluginPackageEntity not found for packageName:dataModelVersion:entityName [%s:%s:%s] for plugin config: %s",
                    targetPackage, dataModelVersion, targetEntity, pluginConfigName);
            log.error(errorMessage);
            throw new WecubeCoreException("3050", errorMessage, targetPackage, dataModelVersion, targetEntity,
                    pluginConfigName);
        }
    }

}
