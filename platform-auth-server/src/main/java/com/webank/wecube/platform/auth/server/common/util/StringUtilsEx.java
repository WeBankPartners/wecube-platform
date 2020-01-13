package com.webank.wecube.platform.auth.server.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author gavin
 *
 */
public class StringUtilsEx {
    private static final String VALID_EMAIL_ADDR_FORMAT = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";
    public static String encodeBase64String(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    public static boolean isEmailValid(String emailAddress) {
        if (StringUtils.isBlank(emailAddress)) {
            return false;
        }
        Pattern regex = Pattern.compile(VALID_EMAIL_ADDR_FORMAT);
        Matcher matcher = regex.matcher(emailAddress);
        return matcher.matches();
    }
}
