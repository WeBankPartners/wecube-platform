package com.webank.wecube.platform.workflow.parse;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.calendar.DurationBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.DurationHelper;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;

/**
 * 
 * @author gavin
 *
 */
public class CustomDurationBusinessCalendar extends DurationBusinessCalendar {
    private final static EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;
    public final static String FIX_TIME_PATTERN = "^(0\\d{1}|1\\d{1}|2[0-3]):[0-5]\\d{1}:([0-5]\\d{1})$";

    public Date resolveDuedate(String duedate, Date startDate) {
        if (Pattern.matches(FIX_TIME_PATTERN, duedate)) {
            return resolveFixDuedate(duedate);
        }
        
        try {
            DurationHelper dh = new DurationHelper(duedate, startDate);
            return dh.getDateAfter(startDate);
        } catch (Exception e) {
            throw LOG.exceptionWhileResolvingDuedate(duedate, e);
        }
    }

    protected Date resolveFixDuedate(String duedate) {
        String timeParts[] = duedate.split(":");
        Calendar calendar = new GregorianCalendar();
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        int second = Integer.parseInt(timeParts[2]);
        
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        
        return calendar.getTime();
    }
}
