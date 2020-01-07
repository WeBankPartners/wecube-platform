package com.webank.wecube.platform.auth.server.common.util;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author howechen
 */
public class ValidateUtils {
    public static boolean isEmailValid(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress)) {
            return false;
        }
        boolean flag;
        try {
            String check = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(emailAddress);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}
