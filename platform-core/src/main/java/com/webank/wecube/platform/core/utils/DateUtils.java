package com.webank.wecube.platform.core.utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.common.base.Strings;
import com.webank.wecube.platform.core.commons.WecubeCoreException;

public final class DateUtils {
    
    public static final String DEF_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEF_DATE_PATTERN = "yyyy-MM-dd";
    
    public static String dateToString(Date date){
        return dateToString(date, DEF_DATE_TIME_PATTERN);
    }
    
    public static String dateToString(Date date, String pattern){
        if(date == null){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static java.util.Date convertToTimestamp(String value) {
        if(Strings.isNullOrEmpty(value))
            return null;
        
        String timestampPattern =  "yyyy-MM-dd HH:mm:ss";
        String datePattern = "yyyy-MM-dd";

        String parsePattern=null;
        java.util.Date date = null;
        if(value.length() == timestampPattern.length()) {
            parsePattern = timestampPattern;
        }else if(value.length() == datePattern.length()) {
            parsePattern = datePattern;
        }
        
        if(!Strings.isNullOrEmpty(parsePattern)) {
            SimpleDateFormat dateFmt = new SimpleDateFormat(parsePattern);
            dateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                date = dateFmt.parse(value);
            } catch (ParseException e) {
                throw new WecubeCoreException(String.format("Failed to parse date string [%s].",value),e);
            }
        }else {
            throw new WecubeCoreException("Only support 'yyyy-MM-dd HH:mm:ss' and 'yyyy-MM-dd' for datetime.");
        }
        
        return date;
    }
}
