package com.webank.wecube.platform.core.utils;

public class StringUtils {

    public static boolean containsOnlyAlphanumericOrHyphen(String urlPathSegment) {
        if (urlPathSegment == null) return false;
        return urlPathSegment.matches("[a-zA-Z0-9\\-]+");
    }
    
    public static boolean isValidIp(String ip) {
        if (ip != null && !ip.isEmpty()) {
            String ipValidityRegularExpression = "^(([1-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))((\\.([0-9]|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))){3})$";
            return ip.matches(ipValidityRegularExpression);
        }
        return false;
    }
}