package com.webank.wecube.platform.core.utils;

public class StringUtils {

    public static boolean containsOnlyAlphanumericOrHyphen(String urlPathSegment) {
        if (urlPathSegment == null) return false;
        return urlPathSegment.matches("[a-zA-Z0-9\\-]+");
    }
}