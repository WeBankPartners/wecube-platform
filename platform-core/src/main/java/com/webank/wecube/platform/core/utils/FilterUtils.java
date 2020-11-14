package com.webank.wecube.platform.core.utils;

import com.webank.wecube.platform.core.dto.FilterDto;
import com.webank.wecube.platform.core.dto.FilterOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilterUtils {

    private static Predicate<Map<String, Object>> equalTo(String key, Object value) {
        return stringObjectMap -> {
            if (stringObjectMap.get(key) != null) {
                return stringObjectMap.get(key).equals(value);
            } else {
                return null == value;
            }
        };
    }

    private static Predicate<Map<String, Object>> notEqualTo(String key, Object value) {
        return stringObjectMap -> {
            if (stringObjectMap.get(key) != null) {
                return !stringObjectMap.get(key).equals(value);
            } else {
                return null != value;
            }
        };
    }


    public static List<Predicate<Map<String, Object>>> getPredicateList(List<FilterDto> filterList) throws IllegalAccessException {
        List<Predicate<Map<String, Object>>> result = new ArrayList<>();
        for (FilterDto filter : filterList) {
            if (!filter.checkNull()) {
                switch (FilterOperator.fromCode(filter.getOperator())) {
                    case EQUAL:
                        result.add(equalTo(filter.getName(), filter.getValue()));
                        break;
                    case NOT_EQUAL:
                        result.add(notEqualTo(filter.getName(), filter.getValue()));
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }
}
