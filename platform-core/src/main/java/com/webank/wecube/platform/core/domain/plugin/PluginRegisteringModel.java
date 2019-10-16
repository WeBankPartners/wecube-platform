package com.webank.wecube.platform.core.domain.plugin;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.webank.wecube.core.support.cmdb.dto.v2.IntQueryOperateAggRequestDto.CriteriaNode;

import lombok.Data;

@Data
public class PluginRegisteringModel {
    private final static String PATH_DELIMITER = ", ";

    private List<FilteringRuleConfig> filteringRuleConfigs = new ArrayList<>();
    private List<InterfaceConfig> interfaceConfigs = new ArrayList<>();

    @Data
    public static class InterfaceConfig {
        Integer interfaceId;
        String interfaceName;
        String interfaceFilterStatus;
        String interfaceResultStatus;
        List<InputParameterMapping> inputParameterMappings;
        List<OutputParameterMapping> outputParameterMappings;
    }

    @Data
    public static class FilteringRuleConfig {
        Integer cmdbAttributeId;
        String filteringValues;
    }

    @Data
    public static class InputParameterMapping{
        Integer parameterId;
        
        String mappingType;

        Integer cmdbCiTypeId;
        String cmdbCiTypeName;
        Integer cmdbAttributeId;
        String cmdbColumnSource;
        
        Integer cmdbEnumCode;
        List<CriteriaNode> routine;
    }

    @Data
    public static class OutputParameterMapping{
        Integer parameterId;

        Integer cmdbAttributeId;
        String cmdbColumnSource;
    }

    public static String pathToString(List<Integer> path) {
        if (isNotEmpty(path)) {
            return path.stream().map(String::valueOf).collect(Collectors.joining(PATH_DELIMITER));
        }
        return null;
    }

    public static List<Integer> pathToList(String path) {
        if (StringUtils.isNotEmpty(path)) {
            String[] nodes = path.split(PATH_DELIMITER);
            return Arrays.stream(nodes).map(NumberUtils::createInteger).collect(Collectors.toList());
        }
        return null;
    }

    public static List<Integer> getCiTypeIds(List<Integer> pathIds) {
        List<Integer> ciTypeIds = new ArrayList<>();
        if (isNotEmpty(pathIds)) {
            for(int i=0; i < pathIds.size(); i+=2) {
                ciTypeIds.add(pathIds.get(i));
            }
        }
        return ciTypeIds;
    }

    public static List<Integer> getCiTypeAttributeIds(List<Integer> pathIds) {
        List<Integer> ciTypeAttributeIds = new ArrayList<>();
        if (isNotEmpty(pathIds)) {
            for(int i=1; i < pathIds.size(); i+=2) {
                ciTypeAttributeIds.add(pathIds.get(i));
            }
        }
        return ciTypeAttributeIds;
    }
}
