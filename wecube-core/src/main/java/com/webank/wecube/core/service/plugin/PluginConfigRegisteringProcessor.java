package com.webank.wecube.core.service.plugin;


import com.google.common.collect.Lists;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.*;
import com.webank.wecube.core.domain.plugin.PluginRegisteringModel.FilteringRuleConfig;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.IntQueryOperateAggRequestDto;
import com.webank.wecube.core.support.cmdb.dto.v2.IntQueryOperateAggRequestDto.Criteria;
import com.webank.wecube.core.support.cmdb.dto.v2.IntQueryOperateAggRequestDto.CriteriaNode;
import com.webank.wecube.core.support.cmdb.dto.v2.IntQueryOperateAggResponseDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.CONFIGURED;
import static com.webank.wecube.core.domain.plugin.PluginRegisteringModel.pathToString;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class PluginConfigRegisteringProcessor {

    private CmdbServiceV2Stub cmdbServiceV2Stub;

    private PluginConfig pluginConfig;

    private Map<String, PluginConfigFilteringRule> filteringRuleMap = new HashMap<>();
    private Map<Integer, PluginConfigInterface> interfaceMap = new HashMap<>();
    private Map<String, PluginConfigInterfaceParameter> parameterMap = new HashMap<>();

    public PluginConfigRegisteringProcessor(CmdbServiceV2Stub cmdbServiceV2Stub, PluginConfig pluginConfig) {
        this.cmdbServiceV2Stub = cmdbServiceV2Stub;
        this.pluginConfig = pluginConfig;

        init();
    }

    private void init() {
        for (PluginConfigInterface pluginConfigInterface : pluginConfig.getInterfaces()) {
            interfaceMap.put(pluginConfigInterface.getId(), pluginConfigInterface);
            for (PluginConfigInterfaceParameter parameter : pluginConfigInterface.getInputParameters()) {
                parameterMap.put(String.valueOf(parameter.getId()), parameter);
            }
            for (PluginConfigInterfaceParameter parameter : pluginConfigInterface.getOutputParameters()) {
                parameterMap.put(String.valueOf(parameter.getId()), parameter);
            }
        }
        for (PluginConfigFilteringRule filteringRule : pluginConfig.getFilteringRules()) {
            filteringRuleMap.put(String.valueOf(filteringRule.getCmdbAttributeId()), filteringRule);
        }
    }

    public void process(int cmdbCiTypeId, String cmdbCiTypeName, PluginRegisteringModel registeringModel) {
        for (FilteringRuleConfig ruleConfig : registeringModel.getFilteringRuleConfigs()) {
            if (!filteringRuleMap.containsKey(String.valueOf(ruleConfig.getCmdbAttributeId()))) {
                PluginConfigFilteringRule filteringRule = new PluginConfigFilteringRule();
                filteringRule.setPluginConfig(pluginConfig);
                filteringRule.setCmdbAttributeId(ruleConfig.getCmdbAttributeId());
                filteringRule.setFilteringValues(ruleConfig.getFilteringValues());

                pluginConfig.addPluginConfigFilteringRule(filteringRule);
                filteringRuleMap.put(String.valueOf(ruleConfig.getCmdbAttributeId()), filteringRule);
            }
        }

        List<IntQueryOperateAggRequestDto> intQueryOperateAggRequests = new ArrayList<>();
        for (PluginRegisteringModel.InterfaceConfig interfaceConfig : registeringModel.getInterfaceConfigs()) {
            Integer interfaceId = interfaceConfig.getInterfaceId();
            PluginConfigInterface inf = interfaceMap.get(interfaceId);
            if (inf == null) {
                throw new WecubeCoreException("Plugin config interface id not found, id = " + interfaceId);
            }
            inf.setFilterStatus(interfaceConfig.getInterfaceFilterStatus());
            inf.setResultStatus(interfaceConfig.getInterfaceResultStatus());

            for (PluginRegisteringModel.InputParameterMapping parameterMapping : interfaceConfig.getInputParameterMappings()) {
                PluginConfigInterfaceParameter parameter = getPluginConfigInterfaceParameter(parameterMap, String.valueOf(parameterMapping.getParameterId()));
                
                if (PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE.equals(parameterMapping.getMappingType())) {
                	parameter.setCmdbCitypeId(parameterMapping.getCmdbCiTypeId());
                    parameter.setCmdbAttributeId(parameterMapping.getCmdbAttributeId());
                    parameter.setCmdbCitypePath(pathToString(converRoutine(parameterMapping.getRoutine())));
                } else if (PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_ENUM_CODE.equals(parameterMapping.getMappingType())) {
                	parameter.setCmdbEnumCode(parameterMapping.getCmdbEnumCode());
                } else if (PluginConfigInterfaceParameter.MAPPING_TYPE_RUNTIME.equals(parameterMapping.getMappingType())) {
                	//do nothing
                } else {
                	throw new WecubeCoreException("Unsupported mapping type: " + parameterMapping.getMappingType());
                }
                
                parameter.setMappingType(parameterMapping.getMappingType());
                parameter.setCmdbColumnSource(parameterMapping.getCmdbColumnSource());
            }

            for (PluginRegisteringModel.OutputParameterMapping parameterMapping : interfaceConfig.getOutputParameterMappings()) {
                PluginConfigInterfaceParameter parameter = getPluginConfigInterfaceParameter(parameterMap, String.valueOf(parameterMapping.getParameterId()));
                parameter.setCmdbColumnSource(parameterMapping.getCmdbColumnSource());
                parameter.setCmdbColumnName(parameterMapping.getCmdbColumnSource());
                parameter.setCmdbCitypeId(cmdbCiTypeId);
                parameter.setCmdbAttributeId(parameterMapping.getCmdbAttributeId());
            }

            intQueryOperateAggRequests.add(prepareIntQueryOperateAggRequest(cmdbCiTypeId, cmdbCiTypeName, registeringModel.getFilteringRuleConfigs(), inf, interfaceConfig));
        }

        List<IntQueryOperateAggResponseDto> responseIntegrateTemplates = cmdbServiceV2Stub.operateIntegratedQueryTemplate(cmdbCiTypeId, intQueryOperateAggRequests);
        handlerIntegrateTemplateOperationResponse(responseIntegrateTemplates);
        pluginConfig.setStatus(CONFIGURED);
    }

    private IntQueryOperateAggRequestDto prepareIntQueryOperateAggRequest(int cmdbCiTypeId,
                                                                          String cmdbCiTypeName,
                                                                          List<FilteringRuleConfig> filteringRuleConfigList,
                                                                          PluginConfigInterface inf,
                                                                          PluginRegisteringModel.InterfaceConfig interfaceConfig) {
        IntQueryOperateAggRequestDto intQueryOperateAggRequestDto = new IntQueryOperateAggRequestDto();
        Integer cmdbQueryTemplateId = inf.getCmdbQueryTemplateId();
        if (cmdbQueryTemplateId == null) {
            intQueryOperateAggRequestDto.setQueryId(0);
            intQueryOperateAggRequestDto.setOperation("create");
        } else {
            intQueryOperateAggRequestDto.setQueryId(cmdbQueryTemplateId);
            intQueryOperateAggRequestDto.setOperation("update");
        }
        intQueryOperateAggRequestDto.setQueryName(buildCmdbIntegrateTemplateName(cmdbCiTypeName, inf));
        intQueryOperateAggRequestDto.setQueryDesc(buildCmdbIntegrateTemplateDescription(cmdbCiTypeName, inf));

        Map<String, Criteria> criteriaMap = filteringRuleConfigList.stream()
                .map(config -> createCmdbIntQueryOperateAggRequestCriteria(cmdbCiTypeId, cmdbCiTypeName, config))
                .collect(Collectors.toMap(this::keyOfCiTypeAndAttributeAndPath, identity()));
        for (PluginRegisteringModel.InputParameterMapping parameterMapping : interfaceConfig.getInputParameterMappings()) {
        	if (PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE.equals(parameterMapping.getMappingType())) {
        		Criteria criteria = createCmdbIntQueryOperateAggRequestCriteria(parameterMapping);
        		String keyOfCriteria = keyOfCiTypeAndAttributeAndPath(criteria.getCiTypeId(), criteria.getAttribute().getAttrId(), converRoutine(criteria.getRoutine()));
        		if (criteriaMap.containsKey(keyOfCriteria)) {
        			Criteria existingCriteria = criteriaMap.get(keyOfCriteria);
        			existingCriteria.setBranchId(existingCriteria.getBranchId() + "-" + criteria.getBranchId());
        		} else {
        			criteriaMap.put(keyOfCriteria, criteria);
        		}
        	}
        }

        intQueryOperateAggRequestDto.setCriterias(new ArrayList<>(criteriaMap.values()));

        return intQueryOperateAggRequestDto;
    }

    private List<Integer> converRoutine(List<CriteriaNode> routine) {
        List<Integer> idRoutines = new LinkedList<>();
        routine.forEach(x -> {
            idRoutines.add(x.getCiTypeId());
            if (x.getParentRs() != null) {
                idRoutines.add(x.getParentRs().getAttrId());
            }
        });
        return idRoutines;
    }

    private String keyOfCiTypeAndAttributeAndPath(Criteria criteria) {
        return keyOfCiTypeAndAttributeAndPath(criteria.getCiTypeId(), criteria.getAttribute().getAttrId(), converRoutine(criteria.getRoutine()));
    }

    private String keyOfCiTypeAndAttributeAndPath(int ciTypeId, int attributeId, List<Integer> idRoutines) {
        return String.format("%d-%d-%s", ciTypeId, attributeId, pathToString(idRoutines));
    }

    private Criteria createCmdbIntQueryOperateAggRequestCriteria(PluginRegisteringModel.InputParameterMapping parameterMapping) {
        Criteria criteria = new Criteria();
        criteria.setBranchId("P" + parameterMapping.getParameterId());
        criteria.setCiTypeId(parameterMapping.getCmdbCiTypeId());
        criteria.setCiTypeName(parameterMapping.getCmdbCiTypeName());
        criteria.setAttribute(new IntQueryOperateAggRequestDto.CriteriaCiTypeAttr(parameterMapping.getCmdbAttributeId(), true, true));
        criteria.setRoutine(parameterMapping.getRoutine());

        return criteria;
    }

    private Criteria createCmdbIntQueryOperateAggRequestCriteria(int cmdbCiTypeId, String cmdbCiTypeName, FilteringRuleConfig filteringRuleConfig) {
        Criteria criteria = new Criteria();
        criteria.setBranchId("R" + filteringRuleConfig.getCmdbAttributeId());
        criteria.setCiTypeId(cmdbCiTypeId);
        criteria.setCiTypeName(cmdbCiTypeName);
        criteria.setAttribute(new IntQueryOperateAggRequestDto.CriteriaCiTypeAttr(filteringRuleConfig.getCmdbAttributeId(), true, true));
        criteria.setRoutine(Lists.newArrayList(new CriteriaNode(cmdbCiTypeId)));
        return criteria;
    }


    private void handlerIntegrateTemplateOperationResponse(List<IntQueryOperateAggResponseDto> responseIntegrateTemplates) {
        for (IntQueryOperateAggResponseDto responseIntegrateTemplate : responseIntegrateTemplates) {
            Integer interfaceId = getInterfaceId(responseIntegrateTemplate.getQueryName());
            PluginConfigInterface pluginConfigInterface = interfaceMap.get(interfaceId);
            if (pluginConfigInterface == null) {
                throw new WecubeCoreException("Plugin config interface id not found, id = " + interfaceId);
            }
            pluginConfigInterface.setCmdbQueryTemplateId(responseIntegrateTemplate.getQueryId());

            List<IntQueryOperateAggResponseDto.AggBranch> branches = responseIntegrateTemplate.getBranchs();
            if (isNotEmpty(branches)) {
                for (IntQueryOperateAggResponseDto.AggBranch branch : branches) {
                    if (branch != null) {
                        handleBranch(branch);
                    }
                }
            }
        }
    }

    private void handleBranch(IntQueryOperateAggResponseDto.AggBranch branch) {
        if (branch == null) return;
        List<String> branchIdList = getBranchIdList(branch);
        for (String branchId : branchIdList) {
            if (branchId.startsWith("P")) {
                getPluginConfigInterfaceParameter(parameterMap, branchId.substring(1)).setCmdbColumnName(branch.getAlias());
            } else if (branchId.startsWith("R")) {
                getPluginConfigFilteringRule(filteringRuleMap, branchId.substring(1)).setCmdbColumnName(branch.getAlias());
            } else {
                throw new WecubeCoreException("Invalid id " + branchId + " in " + branch.getBranchId());
            }
        }
    }

    private List<String> getBranchIdList(IntQueryOperateAggResponseDto.AggBranch branch) {
        if (branch != null && StringUtils.isNotEmpty(branch.getBranchId())) {
            return Arrays.asList(branch.getBranchId().split("-"));
        }
        return Lists.newArrayList();
    }

    private PluginConfigInterfaceParameter getPluginConfigInterfaceParameter(Map<String, PluginConfigInterfaceParameter> parameterMap, String parameterId) {
        PluginConfigInterfaceParameter parameter = parameterMap.get(parameterId);
        if (parameter == null) {
            throw new WecubeCoreException("Plugin config interface parameter id not found, id = " + parameterId);
        }
        return parameter;
    }

    private PluginConfigFilteringRule getPluginConfigFilteringRule(Map<String, PluginConfigFilteringRule> filteringRuleMap, String keyOfRule) {
        PluginConfigFilteringRule filteringRule = filteringRuleMap.get(keyOfRule);
        if (filteringRule == null) {
            throw new WecubeCoreException("Plugin config filtering rule not found, key = " + keyOfRule);
        }
        return filteringRule;
    }


    private String buildCmdbIntegrateTemplateName(String cmdbCiTypeName, PluginConfigInterface pluginConfigInterface) {
        String format = "%s-%d-%d-%d";
        PluginConfig pluginConfig = pluginConfigInterface.getPluginConfig();
        PluginPackage pluginPackage = pluginConfig.getPluginPackage();
        return String.format(format, cmdbCiTypeName, pluginPackage.getId(), pluginConfig.getId(), pluginConfigInterface.getId());
    }

    private String buildCmdbIntegrateTemplateDescription(String cmdbCiTypeName, PluginConfigInterface pluginConfigInterface) {
        String format = "%s-%s-%s-%s-%s";
        PluginConfig pluginConfig = pluginConfigInterface.getPluginConfig();
        PluginPackage pluginPackage = pluginConfig.getPluginPackage();
        return String.format(format, cmdbCiTypeName, pluginPackage.getName(), pluginPackage.getVersion(), pluginConfig.getName(), pluginConfigInterface.getName());
    }

    private Integer getInterfaceId(String queryTemplateName) {
        int lastIndexOfHyphen = queryTemplateName.lastIndexOf("-");
        if (lastIndexOfHyphen < 0)
            throw new WecubeCoreException("Invalid integrate query template name : " + queryTemplateName);
        String interfaceId = queryTemplateName.substring(lastIndexOfHyphen + 1);
        if (!NumberUtils.isCreatable(interfaceId))
            throw new WecubeCoreException("Invalid integrate query template name : " + queryTemplateName);
        return NumberUtils.createInteger(interfaceId);
    }
}
