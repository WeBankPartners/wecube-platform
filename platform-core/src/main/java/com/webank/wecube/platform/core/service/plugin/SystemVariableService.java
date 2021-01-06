package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.plugin.SystemVariableDto;
import com.webank.wecube.platform.core.entity.plugin.RoleMenu;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.repository.plugin.RoleMenuMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class SystemVariableService {
    private static final Logger log = LoggerFactory.getLogger(SystemVariableService.class);
    public static final String MENU_CODE_ADMIN_SYSTEM_PARAMS = "ADMIN_SYSTEM_PARAMS";

    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
    @Autowired
    private PluginPageableDataService systemVariableDataService;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    public QueryResponse<SystemVariableDto> retrieveSystemVariables(QueryRequestDto queryRequest) {
//        QueryResponse<SystemVariable> queryResponse = entityRepository.query(SystemVariable.class, queryRequest);
//        List<SystemVariableDto> systemVariableDto = Lists.transform(queryResponse.getContents(),
//                x -> SystemVariableDto.fromDomain(x));
//        return new QueryResponse<>(queryResponse.getPageInfo(), systemVariableDto);
        
        com.github.pagehelper.PageInfo<SystemVariables> pageInfo = systemVariableDataService.retrieveSystemVariables(queryRequest);
        
        List<SystemVariableDto> systemVariableDtos = new ArrayList<>();
        for(SystemVariables e : pageInfo.getList()){
            SystemVariableDto dto = buildSystemVariableDto(e);
            systemVariableDtos.add(dto);
        }
        
        com.webank.wecube.platform.core.dto.plugin.PageInfo respPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        respPageInfo.setPageSize(queryRequest.getPageable().getPageSize());
        respPageInfo.setStartIndex(queryRequest.getPageable().getStartIndex());
        respPageInfo.setTotalRows((int)pageInfo.getTotal());
        return new QueryResponse<>(respPageInfo, systemVariableDtos);
    }
    

   
    /**
     * 
     * @param resourceSystemVariables
     * @return
     */
    @Transactional
    public List<SystemVariableDto> createSystemVariables(List<SystemVariableDto> resourceSystemVariables) {
        List<SystemVariableDto> resultDtos = new ArrayList<>();
        if (resourceSystemVariables == null || resourceSystemVariables.isEmpty()) {
            return resultDtos;
        }

        for (SystemVariableDto sysVarDto : resourceSystemVariables) {
            if (StringUtils.isBlank(sysVarDto.getId())) {
                SystemVariables existSystemVariablesEntity = systemVariablesMapper
                        .selectByPrimaryKey(sysVarDto.getId());

                if (existSystemVariablesEntity == null) {
                    log.info("System variable with ID {} does not exist.", sysVarDto.getId());
                } else {
                    existSystemVariablesEntity = updateSystemVariablesEntity(sysVarDto, existSystemVariablesEntity);
                    systemVariablesMapper.updateByPrimaryKeySelective(existSystemVariablesEntity);
                    SystemVariableDto retDto = buildSystemVariableDto(existSystemVariablesEntity);
                    resultDtos.add(retDto);
                }
            } else {
                SystemVariables newSystemVariablesEntity = buildSystemVariablesEntity(sysVarDto);
                systemVariablesMapper.insert(newSystemVariablesEntity);
                SystemVariableDto retDto = buildSystemVariableDto(newSystemVariablesEntity);
                resultDtos.add(retDto);
            }

        }

        return resultDtos;

    }

    /**
     * 
     * @param resourceSystemVariables
     * @return
     */
    @Transactional
    public List<SystemVariableDto> updateSystemVariables(List<SystemVariableDto> resourceSystemVariables) {
        List<SystemVariableDto> resultDtos = new ArrayList<>();
        if (resourceSystemVariables == null || resourceSystemVariables.isEmpty()) {
            return resultDtos;
        }

        for (SystemVariableDto sysVarDto : resourceSystemVariables) {
            if (StringUtils.isBlank(sysVarDto.getId())) {
                SystemVariables existSystemVariablesEntity = systemVariablesMapper
                        .selectByPrimaryKey(sysVarDto.getId());

                if (existSystemVariablesEntity == null) {
                    log.info("System variable with ID {} does not exist.", sysVarDto.getId());
                } else {
                    existSystemVariablesEntity = updateSystemVariablesEntity(sysVarDto, existSystemVariablesEntity);
                    systemVariablesMapper.updateByPrimaryKeySelective(existSystemVariablesEntity);
                    SystemVariableDto retDto = buildSystemVariableDto(existSystemVariablesEntity);
                    resultDtos.add(retDto);
                }
            } else {
                SystemVariables newSystemVariablesEntity = buildSystemVariablesEntity(sysVarDto);
                systemVariablesMapper.insert(newSystemVariablesEntity);
                SystemVariableDto retDto = buildSystemVariableDto(newSystemVariablesEntity);
                resultDtos.add(retDto);
            }

        }

        return resultDtos;
    }

    /**
     * 
     * @param systemVariableDtos
     */
    @Transactional
    public void deleteSystemVariables(List<SystemVariableDto> systemVariableDtos) {
        if (systemVariableDtos == null || systemVariableDtos.isEmpty()) {
            return;
        }

        for (SystemVariableDto dto : systemVariableDtos) {
            if (!StringUtils.isBlank(dto.getId())) {
                SystemVariables existSystemVariablesEntity = systemVariablesMapper.selectByPrimaryKey(dto.getId());
                if (existSystemVariablesEntity == null) {
                    throw new WecubeCoreException("3025",
                            String.format("Can not find variable with id [%s].", dto.getId()), dto.getId());
                }

                systemVariablesMapper.deleteByPrimaryKey(dto.getId());
            }
        }

    }

    // private void validateIfSystemVariablesAreExists(List<SystemVariableDto>
    // systemVariableDtos) {
    // systemVariableDtos.forEach(dto -> {
    // if (dto.getId() == null &&
    // !systemVariableRepository.existsById(dto.getId())) {
    // throw new WecubeCoreException("3025", String.format("Can not find
    // variable with id [%s].", dto.getId()),
    // dto.getId());
    // }
    // });
    // }

    // public SystemVariable getSystemVariableById(String varId) {
    // Optional<SystemVariable> systemVariable =
    // systemVariableRepository.findById(varId);
    // if (systemVariable.isPresent()) {
    // return systemVariable.get();
    // } else {
    // throw new WecubeCoreException("3026", "System Variable not found for id:
    // " + varId, varId);
    // }
    // }

    public SystemVariables getSystemVariableByPackageNameAndName(String packageName, String varName) {
        List<SystemVariables> pluginSystemVariables = getPluginSystemVariableByPackageNameAndName(packageName, varName);
        if (null != pluginSystemVariables && pluginSystemVariables.size() > 0) {
            return pluginSystemVariables.get(0);
        }
        pluginSystemVariables = getGlobalSystemVariableByName(varName);
        if (null != pluginSystemVariables && pluginSystemVariables.size() > 0) {
            return pluginSystemVariables.get(0);
        }
        return null;
    }

    public List<SystemVariables> getPluginSystemVariableByPackageNameAndName(String packageName, String varName) {
        return systemVariablesMapper.selectAllByNameAndScopeAndStatus(varName, packageName, SystemVariables.ACTIVE);
    }

    public List<SystemVariables> getGlobalSystemVariableByName(String varName) {
        return systemVariablesMapper.selectAllByNameAndScopeAndStatus(varName, SystemVariables.SCOPE_GLOBAL,
                SystemVariables.ACTIVE);
    }

    public String variableReplacement(String packageName, String originalString) {
        List<String> varList = StringUtilsEx.findSystemVariableString(originalString);
        for (int i = 0; i < varList.size(); i++) {
            String varValue = "";
            String varString = varList.get(i);
            String varName = varString.substring(2, varString.length() - 2);
            List<SystemVariables> varObjects = getPluginSystemVariableByPackageNameAndName(packageName, varName);
            if (varObjects.size() == 0) {
                varObjects = getGlobalSystemVariableByName(varName);
            }

            if (varObjects.size() != 0) {
                SystemVariables varObject = varObjects.get(0);
                varValue = varObject.getValue() == null || varObject.getValue().isEmpty()
                        ? (varObject.getDefaultValue() == null ? "" : varObject.getDefaultValue())
                        : varObject.getValue();
            }

            log.info("replace system variable {}:{}", varString, varValue);
            originalString = originalString.replace(varString, varValue);
        }
        return originalString;
    }

    // public Iterable<SystemVariable> getAllSystemVariable() {
    // return systemVariableRepository.findAll();
    // }

    public List<SystemVariables> getPluginSystemVariableByPackageId(String packageId) {
        List<SystemVariables> systemVariables = systemVariablesMapper.selectAllBySource(packageId);
        if (systemVariables != null) {
            return systemVariables;
        }
        return Collections.emptyList();
    }

    public List<String> retrieveSystemVariableScope() {
        return systemVariablesMapper.selectAllSystemVariableScopes();
    }

    public void validatePermission() {
        AuthenticatedUser currentUser = AuthenticationContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getAuthorities() == null || currentUser.getAuthorities().isEmpty()) {
            throw new WecubeCoreException("Current user does not logged in.");
        }
        List<RoleMenu> roleMenus = roleMenuMapper.selectAllByMenuCode(MENU_CODE_ADMIN_SYSTEM_PARAMS);

        if (roleMenus == null || roleMenus.isEmpty()) {
            throw new WecubeCoreException("System variable authority is not configured currently.");
        }

        for (String userRole : currentUser.getAuthorities()) {
            for (RoleMenu roleMenu : roleMenus) {
                if (userRole.equalsIgnoreCase(roleMenu.getRoleName())) {
                    return;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (RoleMenu roleMenu : roleMenus) {
            sb.append(roleMenu.getRoleName()).append(" ");
        }

        String msg = String.format("Lack of permission to proceed operation, expected user roles:%s", sb.toString());
        throw new WecubeCoreException(msg);
    }

    // private List<SystemVariable>
    // convertVariableDtoToDomain(List<SystemVariableDto> systemVariableDtos) {
    // List<SystemVariable> domains = new ArrayList<>();
    // systemVariableDtos.forEach(dto -> {
    // SystemVariable existingSystemVariable = null;
    // if (dto.getId() != null) {
    // Optional<SystemVariable> existingSystemVariableOpt =
    // systemVariableRepository.findById(dto.getId());
    // if (existingSystemVariableOpt.isPresent()) {
    // existingSystemVariable = existingSystemVariableOpt.get();
    // }
    // }
    // SystemVariable domain = toDomain(dto, existingSystemVariable);
    // domains.add(domain);
    // });
    // return domains;
    // }

    private SystemVariableDto buildSystemVariableDto(SystemVariables entity) {
        SystemVariableDto dto = new SystemVariableDto();
        dto.setId(entity.getId());
        dto.setPackageName(entity.getPackageName());
        dto.setName(entity.getName());
        dto.setValue(entity.getValue());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setScope(entity.getScope());
        dto.setSource(entity.getSource());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private SystemVariables buildSystemVariablesEntity(SystemVariableDto dto) {
        SystemVariables entity = new SystemVariables();
        entity.setId(LocalIdGenerator.generateId());
        entity.setPackageName(dto.getPackageName());
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setScope(dto.getScope());
        entity.setSource(dto.getSource());
        if (StringUtils.isBlank(dto.getStatus())) {
            entity.setStatus(SystemVariables.ACTIVE);
        } else {
            entity.setStatus(dto.getStatus());
        }

        return entity;
    }

    private SystemVariables updateSystemVariablesEntity(SystemVariableDto dto, SystemVariables systemVariable) {
        if (dto.getPackageName() != null) {
            systemVariable.setPackageName(dto.getPackageName());
        }

        if (dto.getName() != null) {
            systemVariable.setName(dto.getName());
        }

        if (dto.getValue() != null) {
            systemVariable.setValue(dto.getValue());
        }

        if (dto.getDefaultValue() != null) {
            systemVariable.setDefaultValue(dto.getDefaultValue());
        }

        if (dto.getScope() != null) {
            systemVariable.setScope(dto.getScope());
        }

        if (dto.getSource() != null) {
            systemVariable.setSource(dto.getSource());
        }

        if (dto.getStatus() != null) {
            systemVariable.setStatus(dto.getStatus());
        } else {
            systemVariable.setStatus(SystemVariables.ACTIVE);
        }

        return systemVariable;
    }

    // private List<SystemVariableDto>
    // convertVariableDomainToDto(Iterable<SystemVariable> savedDomains) {
    // List<SystemVariableDto> dtos = new ArrayList<>();
    // savedDomains.forEach(domain ->
    // dtos.add(SystemVariableDto.fromDomain(domain)));
    // return dtos;
    // }
}
