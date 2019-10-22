package com.webank.wecube.platform.core.utils;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

public class ClassUtils {
    private static Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static Object toObject(Class<?> clazz, Object value) {
        if (value == null)
            return null;
        if (clazz.equals(value.getClass()) || clazz.isAssignableFrom(value.getClass())) {
            return value;
        }
        String strVal = String.valueOf(value);
        if (Strings.isNullOrEmpty(strVal))
            return null;

        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(strVal);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(strVal);

        Object numberValue = numberToObject(clazz, strVal);
        if (numberValue != null) return numberValue;

        if (Timestamp.class == clazz || java.util.Date.class == clazz) {
            return new Timestamp(DateUtils.convertToTimestamp((String) value).getTime());
        }
        logger.warn("Can not convert from value [{}] to class [{}], just return value.", value, clazz.toString());
        return value;
    }

    private static Object numberToObject(Class<?> clazz, String strVal) {
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(strVal);
        if (Integer.class == clazz || int.class == clazz) {
            try {
                return Integer.parseInt(strVal);
            } catch (NumberFormatException numEx) {
                return Boolean.TRUE.equals(Boolean.parseBoolean(strVal)) ? 1 : 0;
            }
        }
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(strVal);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(strVal);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(strVal);

        return null;
    }

}
