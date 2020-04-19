package com.webank.wecube.platform.core.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class StringUtils {

    public static boolean isValidIp(String ip) {
        if (ip != null && !ip.isEmpty()) {
            String ipValidityRegularExpression = "^(([1-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))((\\.([0-9]|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))){3})$";
            return ip.matches(ipValidityRegularExpression);
        }
        return false;
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
    
    public static List<String> splitByComma(String ipsString){
        String[] ips= ipsString.split(",");
        return Lists.newArrayList(ips);
    }

}