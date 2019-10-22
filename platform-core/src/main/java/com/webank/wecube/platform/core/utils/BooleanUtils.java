package com.webank.wecube.platform.core.utils;

import com.google.common.collect.Sets;

import java.util.Set;

public class BooleanUtils {

    private static final Set<String> EXTENDED_WORDS_FOR_TRUE = Sets.newHashSet("是");

    public static final boolean isTrue(String value){
        if (EXTENDED_WORDS_FOR_TRUE.contains(value)) return true;
        Boolean booleanObject = org.apache.commons.lang3.BooleanUtils.toBooleanObject(value);
        if (booleanObject == null) {
            return false;
        } else {
            return booleanObject.booleanValue();
        }
    }

    public static final <T> T derive(String condition, T trueResult, T failResult) {
        return (isTrue(condition))? trueResult : failResult;
    }
}
