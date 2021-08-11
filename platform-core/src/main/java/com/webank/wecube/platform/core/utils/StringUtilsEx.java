package com.webank.wecube.platform.core.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class StringUtilsEx {
    private static final String VALID_IP_PATTERN = "^(([1-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))((\\.([0-9]|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))){3})$";

    public static boolean isValidIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return ip.matches(VALID_IP_PATTERN);
    }

    public static List<String> findSystemVariableString(String str) {

        List<String> returnVarName = Lists.newArrayList();
        Pattern pattern = Pattern.compile("\\{{2}(.*?)}}");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            returnVarName.add(matcher.group());
        }

        return returnVarName;
    }

    public static List<String> splitByComma(String ipsString) {
        String[] ips = ipsString.split(",");
        return Lists.newArrayList(ips);
    }
    
    public static String encodeBase64String(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

}