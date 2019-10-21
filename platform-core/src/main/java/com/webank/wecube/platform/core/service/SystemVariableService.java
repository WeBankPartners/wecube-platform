package com.webank.wecube.platform.core.service;

import static com.webank.wecube.platform.core.domain.SystemVariable.ACTIVE;
import static com.webank.wecube.platform.core.domain.SystemVariable.SCOPE_TYPE_GLOBAL;
import static com.webank.wecube.platform.core.domain.SystemVariable.SCOPE_TYPE_PLUGIN_PACKAGE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.jpa.SystemVariableRepository;

@Service
@Transactional
public class SystemVariableService {

    @Autowired
    SystemVariableRepository systemVariableRepository;
    
    public List<String> getSupportedScopeTypes() {
        return Lists.newArrayList(SCOPE_TYPE_GLOBAL, SCOPE_TYPE_PLUGIN_PACKAGE);
    }
    
    public List<SystemVariable> getAllSystemVariables(String status) {
        if (status == null) {
            return Lists.newArrayList(systemVariableRepository.findAll());
        } else {
            return systemVariableRepository.findAllByStatus(status);
        }
    }
    
    public List<SystemVariable> getGlobalSystemVariables(String status) {
        if (status == null) {
            return systemVariableRepository.findAllByScopeType(SCOPE_TYPE_GLOBAL);
        } else {
            return systemVariableRepository.findAllByScopeTypeAndStatus(SCOPE_TYPE_GLOBAL, status);
        }
    }
    
    public List<SystemVariable> getSystemVariables(String scopeType, String scopeValue, String status) {
        if (status == null) {
            return systemVariableRepository.findAllByScopeTypeAndScopeValue(scopeType, scopeValue);
        } else {
            return systemVariableRepository.findAllByScopeTypeAndScopeValueAndStatus(scopeType, scopeValue, status);
        }
    }
    
    public SystemVariable getSystemVariableById(int varId) {
        Optional<SystemVariable> systemVariable = systemVariableRepository.findById(varId);
        if (systemVariable.isPresent()) {
            return systemVariable.get();
        } else {
            throw new WecubeCoreException("System Variable not found for id: " + varId);
        }
    }

    public List<SystemVariable> saveSystemVariables(List<SystemVariable> variables) {
        if (isNotEmpty(variables)) {
            return variables.stream().map(p -> {
                    if (p.getStatus()==null) p.setStatus(ACTIVE);
                    return systemVariableRepository.save(p);
                }).collect(Collectors.toList());
        }
        return variables;
    }

    public void deleteSystemVariables(List<Integer> variableIds) {
        if (isNotEmpty(variableIds)) {
            variableIds.forEach(systemVariableRepository::deleteById);
        }
    }

    public void enableSystemVariables(List<Integer> variableIds) {
        if (isNotEmpty(variableIds)) {
            for(Integer varId : variableIds) {
                SystemVariable variable = getSystemVariableById(varId);
                variable.setStatus(SystemVariable.ACTIVE);
                systemVariableRepository.save(variable);
            }
        }
    }
    
    public void disableSystemVariables(List<Integer> variableIds) {
        if (isNotEmpty(variableIds)) {
            for(Integer varId : variableIds) {
                SystemVariable variable = getSystemVariableById(varId);
                variable.setStatus(SystemVariable.INACTIVE);
                systemVariableRepository.save(variable);
            }
        }
    }
}
