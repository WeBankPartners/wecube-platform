package com.webank.wecube.platform.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.RoleMenu;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.QueryResponse;
import com.webank.wecube.platform.core.dto.SystemVariableDto;
import com.webank.wecube.platform.core.jpa.EntityRepository;
import com.webank.wecube.platform.core.jpa.SystemVariableRepository;
import com.webank.wecube.platform.core.jpa.user.RoleMenuRepository;
import com.webank.wecube.platform.core.utils.StringUtilsEx;

@Service
@Transactional
public class SystemVariableService {
    public static final String MENU_CODE_ADMIN_SYSTEM_PARAMS = "ADMIN_SYSTEM_PARAMS";
    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private SystemVariableRepository systemVariableRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    public QueryResponse<SystemVariableDto> retrieveSystemVariables(QueryRequest queryRequest) {
        QueryResponse<SystemVariable> queryResponse = entityRepository.query(SystemVariable.class, queryRequest);
        List<SystemVariableDto> systemVariableDto = Lists.transform(queryResponse.getContents(),
                x -> SystemVariableDto.fromDomain(x));
        return new QueryResponse<>(queryResponse.getPageInfo(), systemVariableDto);
    }

    @Transactional
    public List<SystemVariableDto> createSystemVariables(List<SystemVariableDto> resourceSystemVariables) {
        Iterable<SystemVariable> savedDomains = systemVariableRepository
                .saveAll(convertVariableDtoToDomain(resourceSystemVariables));
        return convertVariableDomainToDto(savedDomains);
    }

    @Transactional
    public List<SystemVariableDto> updateSystemVariables(List<SystemVariableDto> resourceSystemVariables) {
        Iterable<SystemVariable> savedDomains = systemVariableRepository
                .saveAll(convertVariableDtoToDomain(resourceSystemVariables));
        return convertVariableDomainToDto(savedDomains);
    }

    @Transactional
    public void deleteSystemVariables(List<SystemVariableDto> systemVariableDtos) {
        validateIfSystemVariablesAreExists(systemVariableDtos);
        systemVariableRepository.deleteAll(convertVariableDtoToDomain(systemVariableDtos));
    }

    private void validateIfSystemVariablesAreExists(List<SystemVariableDto> systemVariableDtos) {
        systemVariableDtos.forEach(dto -> {
            if (dto.getId() == null && !systemVariableRepository.existsById(dto.getId())) {
                throw new WecubeCoreException("3025",String.format("Can not find variable with id [%s].", dto.getId()), dto.getId());
            }
        });
    }

    private List<SystemVariable> convertVariableDtoToDomain(List<SystemVariableDto> systemVariableDtos) {
        List<SystemVariable> domains = new ArrayList<>();
        systemVariableDtos.forEach(dto -> {
            SystemVariable existingSystemVariable = null;
            if (dto.getId() != null) {
                Optional<SystemVariable> existingSystemVariableOpt = systemVariableRepository.findById(dto.getId());
                if (existingSystemVariableOpt.isPresent()) {
                    existingSystemVariable = existingSystemVariableOpt.get();
                }
            }
            SystemVariable domain = toDomain(dto, existingSystemVariable);
            domains.add(domain);
        });
        return domains;
    }

    public SystemVariable toDomain(SystemVariableDto dto, SystemVariable existedSystemVariable) {
        SystemVariable systemVariable = existedSystemVariable;
        if (systemVariable == null) {
            systemVariable = new SystemVariable();
        }

        if (dto.getId() != null) {
            systemVariable.setId(dto.getId());
        }

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
            systemVariable.setStatus(SystemVariable.ACTIVE);
        }

        return systemVariable;
    }

    private List<SystemVariableDto> convertVariableDomainToDto(Iterable<SystemVariable> savedDomains) {
        List<SystemVariableDto> dtos = new ArrayList<>();
        savedDomains.forEach(domain -> dtos.add(SystemVariableDto.fromDomain(domain)));
        return dtos;
    }

    public SystemVariable getSystemVariableById(String varId) {
        Optional<SystemVariable> systemVariable = systemVariableRepository.findById(varId);
        if (systemVariable.isPresent()) {
            return systemVariable.get();
        } else {
            throw new WecubeCoreException("3026","System Variable not found for id: " + varId, varId);
        }
    }

    public SystemVariable getSystemVariableByPackageNameAndName(String packageName, String varName) {
        List<SystemVariable> pluginSystemVariables = getPluginSystemVariableByPackageNameAndName(packageName, varName);
        if (null != pluginSystemVariables && pluginSystemVariables.size() > 0) {
            return pluginSystemVariables.get(0);
        }
        pluginSystemVariables = getGlobalSystemVariableByName(varName);
        if (null != pluginSystemVariables && pluginSystemVariables.size() > 0) {
            return pluginSystemVariables.get(0);
        }
        return null;
    }

    public List<SystemVariable> getPluginSystemVariableByPackageNameAndName(String packageName, String varName) {
        return systemVariableRepository.findByNameAndScopeAndStatus(varName, packageName, SystemVariable.ACTIVE);
    }

    public List<SystemVariable> getGlobalSystemVariableByName(String varName) {
        return systemVariableRepository.findByNameAndScopeAndStatus(varName, SystemVariable.SCOPE_GLOBAL,
                SystemVariable.ACTIVE);
    }

    public String variableReplacement(String packageName, String originalString) {
        List<String> varList = StringUtilsEx.findSystemVariableString(originalString);
        for (int i = 0; i < varList.size(); i++) {
            String varValue = "";
            String varString = varList.get(i);
            String varName = varString.substring(2, varString.length() - 2);
            List<SystemVariable> varObjects = getPluginSystemVariableByPackageNameAndName(packageName, varName);
            if (varObjects.size() == 0) {
                varObjects = getGlobalSystemVariableByName(varName);
            }

            if (varObjects.size() != 0) {
                SystemVariable varObject = varObjects.get(0);
                varValue = varObject.getValue() == null || varObject.getValue().isEmpty()
                        ? (varObject.getDefaultValue() == null ? "" : varObject.getDefaultValue())
                        : varObject.getValue();
            }

            originalString = originalString.replace(varString, varValue);
        }
        return originalString;
    }

    public Iterable<SystemVariable> getAllSystemVariable() {
        return systemVariableRepository.findAll();
    }

    public List<SystemVariable> getPluginSystemVariableByPackageId(String packageId) {
        List<SystemVariable> systemVariables = systemVariableRepository.findBySource(packageId);
        if (systemVariables != null) {
            return systemVariables;
        }
        return Collections.emptyList();
    }

    public List<String> retrieveSystemVariableScope() {
        return systemVariableRepository.findDistinctScope();
    }

    public void validatePermission() {
        AuthenticatedUser currentUser = AuthenticationContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getAuthorities() == null || currentUser.getAuthorities().isEmpty()) {
            throw new WecubeCoreException("Current user does not logged in.");
        }
        List<RoleMenu> roleMenus = roleMenuRepository.findAllByMenuCode(MENU_CODE_ADMIN_SYSTEM_PARAMS);

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
}
