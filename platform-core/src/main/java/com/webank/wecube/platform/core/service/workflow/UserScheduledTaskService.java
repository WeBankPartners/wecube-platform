package com.webank.wecube.platform.core.service.workflow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskProcInstanceQueryDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskProcessInstanceDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskQueryDto;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.UserScheduledTaskEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.UserScheduledTaskMapper;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class UserScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(UserScheduledTaskService.class);

    @Autowired
    private UserScheduledTaskMapper userScheduledTaskMapper;

    @Autowired
    private ProcInstInfoMapper procInstInfoMapper;

    @Autowired
    private WorkflowDataService workflowDataService;

    @Autowired
    private WorkflowProcInstService workflowProcInstService;

    /**
     * 
     * @param taskDto
     * @return
     */
    public UserScheduledTaskDto createUserScheduledTask(UserScheduledTaskDto taskDto) {
        if (taskDto == null) {
            return null;
        }

        UserScheduledTaskEntity taskEntity = new UserScheduledTaskEntity();
        taskEntity.setId(LocalIdGenerator.generateId());
        taskEntity.setCreatedTime(new Date());
        taskEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

        String owner = taskDto.getOwner();
        if (StringUtils.isBlank(owner)) {
            owner = AuthenticationContextHolder.getCurrentUsername();
        }

        taskEntity.setOwner(owner);

        taskEntity.setProcDefId(taskDto.getProcDefId());
        taskEntity.setProcDefName(taskDto.getProcDefName());

        taskEntity.setEntityDataId(taskDto.getEntityDataId());
        taskEntity.setEntityDataName(taskDto.getEntityDataName());

        taskEntity.setScheduleMode(taskDto.getScheduleMode());

        String scheduleExpr = taskDto.getScheduleExpr();
        taskEntity.setScheduleExpr(scheduleExpr);
        taskEntity.setStatus(Constants.SCHEDULE_TASK_READY);

        userScheduledTaskMapper.insert(taskEntity);

        taskDto.setId(taskEntity.getId());
        taskDto.setOwner(owner);

        return taskDto;
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void stopUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_STOPPED);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<UserScheduledTaskProcessInstanceDto> fetchUserScheduledTaskProcessInstances(
            UserScheduledTaskProcInstanceQueryDto queryDto) {
        List<UserScheduledTaskProcessInstanceDto> instDtos = new ArrayList<>();
        if(queryDto == null) {
            return instDtos;
        }
        
        Date startTime = parseDate(queryDto.getStartTime());
        Date endTime = parseDate(queryDto.getEndTime());

        String procDefName = queryDto.getProcDefName();
//        String entityDataId = queryDto.getEntityDataId();
//        entityDataName = queryDto.getEntityDataName();

//        String owner = queryDto.getOwner();
        String procStatus = queryDto.getProcInstanceStatus();
        String userTaskId = queryDto.getUserTaskId();
        
        List<ProcInstInfoEntity> procInsts = procInstInfoMapper.selectAllByProcBatchKey(procDefName, procStatus, startTime, endTime, userTaskId);
        if(procInsts == null || procInsts.isEmpty()) {
            return instDtos;
        }
        
        for(ProcInstInfoEntity procInst : procInsts) {
            UserScheduledTaskProcessInstanceDto dto = new UserScheduledTaskProcessInstanceDto();
            dto.setExecTime(formatStatiticsDate(procInst.getCreatedTime()));
            dto.setProcDefId(procInst.getProcDefId());
            dto.setProcDefName(procInst.getProcDefName());
            dto.setProcInstId(procInst.getId());
            dto.setStatus(procInst.getStatus());
            
            instDtos.add(dto);
        }

        return instDtos;
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<UserScheduledTaskDto> fetchUserScheduledTasks(UserScheduledTaskQueryDto queryDto) {
        Date startTime = null;
        Date endTime = null;

        String procDefName = null;
        String entityDataId = null;
        String scheduleMode = null;
//        String entityDataName = null;

        String owner = null;

        if (queryDto != null) {
            startTime = parseDate(queryDto.getStartTime());
            endTime = parseDate(queryDto.getEndTime());

            procDefName = queryDto.getProcDefName();
            entityDataId = queryDto.getEntityDataId();
//            entityDataName = queryDto.getEntityDataName();

            owner = queryDto.getOwner();
            scheduleMode = queryDto.getScheduleMode();
        }

        List<UserScheduledTaskEntity> userTasks = userScheduledTaskMapper
                .selectAllAvailableTasksWithFilters(procDefName, entityDataId, owner, scheduleMode, startTime, endTime);

        List<UserScheduledTaskDto> resultDtos = new ArrayList<>();
        if (userTasks == null) {
            return resultDtos;
        }

        for (UserScheduledTaskEntity userTask : userTasks) {
            UserScheduledTaskDto taskDto = new UserScheduledTaskDto();
            taskDto.setId(userTask.getId());
            taskDto.setEntityDataId(userTask.getEntityDataId());
            taskDto.setEntityDataName(userTask.getEntityDataName());
            taskDto.setOwner(userTask.getOwner());
            taskDto.setProcDefId(userTask.getProcDefId());
            taskDto.setProcDefName(userTask.getProcDefName());
            taskDto.setScheduleExpr(userTask.getScheduleExpr());
            taskDto.setScheduleMode(userTask.getScheduleMode());
            taskDto.setStatus(userTask.getStatus());

            String createdTime = formatStatiticsDate(userTask.getCreatedTime());
            taskDto.setCreatedTime(createdTime);

            int totalCompletedInstances = countTriggeredProcInstances(procDefName, ProcInstInfoEntity.COMPLETED_STATUS,
                    startTime, endTime, userTask.getId());
            int totalFaultedInstances = countTriggeredProcInstances(procDefName,
                    ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS, startTime, endTime, userTask.getId());

            taskDto.setTotalCompletedInstances(totalCompletedInstances);
            taskDto.setTotalFaultedInstances(totalFaultedInstances);

            resultDtos.add(taskDto);
        }

        return resultDtos;
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    @Transactional
    public List<UserScheduledTaskDto> updateUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return null;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());
            taskEntity.setScheduleMode(taskDto.getScheduleMode());
            taskEntity.setScheduleExpr(taskDto.getScheduleExpr());

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
        return taskDtos;
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void deleteUserSchecduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_DELETED);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
    }

    /**
     * 
     * @param taskDtos
     */
    @Transactional
    public void resumeUserScheduledTasks(List<UserScheduledTaskDto> taskDtos) {
        if (taskDtos == null || taskDtos.isEmpty()) {
            return;
        }

        for (UserScheduledTaskDto taskDto : taskDtos) {
            UserScheduledTaskEntity taskEntity = userScheduledTaskMapper.selectByPrimaryKey(taskDto.getId());
            if (taskEntity == null) {
                continue;
            }

            taskEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskEntity.setUpdatedTime(new Date());

            taskEntity.setStatus(Constants.SCHEDULE_TASK_READY);

            userScheduledTaskMapper.updateByPrimaryKeySelective(taskEntity);
        }
    }

    /**
     * 
     */
    public void execute() {
        if (log.isTraceEnabled()) {
            log.trace("About to execute user scheduled tasks.");
        }

        try {
            doExecute();
        } catch (Exception e) {
            log.info("Errors while executing user scheduled tasks.", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("Finished executing user scheduled tasks.");
        }
    }

    protected void doExecute() {

        List<UserScheduledTaskEntity> outstandingTasks = scanReadyUserTasks();

        if (outstandingTasks == null || outstandingTasks.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is not outstanding user scheduled tasks to handle.");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Total {} outstanding user scheduled tasks to handle.", outstandingTasks.size());
        }

        for (UserScheduledTaskEntity outstandingTask : outstandingTasks) {
            tryHandleSingleUserScheduledTask(outstandingTask);
        }

    }

    protected void tryHandleSingleUserScheduledTask(UserScheduledTaskEntity outstandingTask) {
        boolean meetExecution = determineExecution(outstandingTask);
        if (!meetExecution) {
            return;
        }

        try {
            performExecution(outstandingTask);
        } catch (Exception e) {
            String errMsg = String.format("Errors while perform user scheduled task:%s", outstandingTask.getId());
            log.info(errMsg, e);
        }

        postPerformExecution(outstandingTask);
    }

    protected List<UserScheduledTaskEntity> scanReadyUserTasks() {
        List<UserScheduledTaskEntity> outstandingTasks = userScheduledTaskMapper.selectAllOutstandingTasks();
        return outstandingTasks;
    }

    protected boolean determineExecution(UserScheduledTaskEntity userTask) {
        // step 1 check if meets any execution
        boolean meetExecution = false;
        String scheduleMode = userTask.getScheduleMode();
        if (StringUtils.isBlank(scheduleMode)) {
            return false;
        }

        String status = userTask.getStatus();
        if (!Constants.SCHEDULE_TASK_READY.equalsIgnoreCase(status)) {
            return false;
        }

        if (Constants.SCHEDULE_MODE_MONTHLY.equalsIgnoreCase(scheduleMode)) {
            meetExecution = meetMonthlyExecution(userTask);
        } else if (Constants.SCHEDULE_MODE_WEEKLY.equalsIgnoreCase(scheduleMode)) {
            meetExecution = meetWeeklyExecution(userTask);
        } else if (Constants.SCHEDULE_MODE_DAILY.equalsIgnoreCase(scheduleMode)) {
            meetExecution = meetDailyExecution(userTask);
        } else if (Constants.SCHEDULE_MODE_HOURLY.equalsIgnoreCase(scheduleMode)) {
            meetExecution = meetHourlyExecution(userTask);
        } else {
            //
        }

        if (!meetExecution) {
            return false;
        }

        // step 2 try to update status and lock
        int expectedRev = userTask.getRev();
        int newRev = expectedRev + 1;
        if (newRev >= Integer.MAX_VALUE) {
            newRev = 0;
        }
        userTask.setExecStartTime(new Date());
        userTask.setStatus(Constants.SCHEDULE_TASK_RUNNING);
        userTask.setRev(newRev);
        int updateResult = userScheduledTaskMapper.updateByPrimaryKeySelectiveCas(userTask, expectedRev);
        if (updateResult > 0) {
            return true;
        } else {
            log.info("Failed to get lock for user scheduled task:{}", userTask.getId());
            return false;
        }

    }

    protected boolean meetMonthlyExecution(UserScheduledTaskEntity userTask) {
        if (!Constants.SCHEDULE_MODE_MONTHLY.equalsIgnoreCase(userTask.getScheduleMode())) {
            return false;
        }

        if (StringUtils.isBlank(userTask.getScheduleExpr())) {
            return false;
        }

        Date curDate = new Date();
        Calendar curCal = Calendar.getInstance();
        curCal.setTime(curDate);
        int curYear = curCal.get(Calendar.YEAR);
        int curMon = curCal.get(Calendar.MONTH);

        // 1 check last execution time
        Date lastExecTime = userTask.getExecStartTime();
        if (lastExecTime != null) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTime(lastExecTime);
            int lastYear = lastCal.get(Calendar.YEAR);
            int lastMon = lastCal.get(Calendar.MONTH);

            if ((lastYear == curYear) && (lastMon == curMon)) {
                return false;
            }
        }

        // 2 check current time
        String scheduleExpr = userTask.getScheduleExpr();
        String[] scheduleExprParts = scheduleExpr.split(":");
        int exprDayOfMonth = Integer.parseInt(scheduleExprParts[0]);
        int exprHour = Integer.parseInt(scheduleExprParts[1]);
        int exprMin = Integer.parseInt(scheduleExprParts[2]);
        int exprSecond = Integer.parseInt(scheduleExprParts[3]);
        Calendar exprCal = Calendar.getInstance();
        exprCal.setTime(curDate);
        exprCal.set(Calendar.DAY_OF_MONTH, exprDayOfMonth);
        exprCal.set(Calendar.HOUR_OF_DAY, exprHour);
        exprCal.set(Calendar.MINUTE, exprMin);
        exprCal.set(Calendar.SECOND, exprSecond);

        if (curCal.compareTo(exprCal) >= 0) {
            return true;
        }

        return false;
    }

    protected boolean meetWeeklyExecution(UserScheduledTaskEntity userTask) {
        if (!Constants.SCHEDULE_MODE_WEEKLY.equalsIgnoreCase(userTask.getScheduleMode())) {
            return false;
        }

        if (StringUtils.isBlank(userTask.getScheduleExpr())) {
            return false;
        }

        Date curDate = new Date();
        Calendar curCal = Calendar.getInstance();
        curCal.setTime(curDate);
        int curYear = curCal.get(Calendar.YEAR);
        int curMon = curCal.get(Calendar.MONTH);
        int currWeek = curCal.get(Calendar.WEEK_OF_YEAR);
        // int curDay = curCal.get(Calendar.DAY_OF_MONTH);

        // 1 check last execution time
        Date lastExecTime = userTask.getExecStartTime();
        if (lastExecTime != null) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTime(lastExecTime);
            int lastYear = lastCal.get(Calendar.YEAR);
            int lastMon = lastCal.get(Calendar.MONTH);
            int lastWeek = lastCal.get(Calendar.WEEK_OF_YEAR);

            if ((lastYear == curYear) && (lastMon == curMon) && (lastWeek == currWeek)) {
                return false;
            }
        }

        // 2 check current time
        String scheduleExpr = userTask.getScheduleExpr();
        String[] scheduleExprParts = scheduleExpr.split(":");
        int exprDayOfWeek = Integer.parseInt(scheduleExprParts[0]);
        int exprHour = Integer.parseInt(scheduleExprParts[1]);
        int exprMin = Integer.parseInt(scheduleExprParts[2]);
        int exprSecond = Integer.parseInt(scheduleExprParts[3]);
        Calendar exprCal = Calendar.getInstance();
        exprCal.setTime(curDate);
        exprCal.set(Calendar.DAY_OF_WEEK, exprDayOfWeek);
        exprCal.set(Calendar.HOUR_OF_DAY, exprHour);
        exprCal.set(Calendar.MINUTE, exprMin);
        exprCal.set(Calendar.SECOND, exprSecond);

        if (curCal.compareTo(exprCal) >= 0) {
            return true;
        }

        return false;
    }

    protected boolean meetDailyExecution(UserScheduledTaskEntity userTask) {
        if (!Constants.SCHEDULE_MODE_DAILY.equalsIgnoreCase(userTask.getScheduleMode())) {
            return false;
        }

        if (StringUtils.isBlank(userTask.getScheduleExpr())) {
            return false;
        }

        Date curDate = new Date();
        Calendar curCal = Calendar.getInstance();
        curCal.setTime(curDate);
        int curYear = curCal.get(Calendar.YEAR);
        int curMon = curCal.get(Calendar.MONTH);
        int curDay = curCal.get(Calendar.DAY_OF_MONTH);

        // 1 check last execution time
        Date lastExecTime = userTask.getExecStartTime();
        if (lastExecTime != null) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTime(lastExecTime);
            int lastYear = lastCal.get(Calendar.YEAR);
            int lastMon = lastCal.get(Calendar.MONTH);
            int lastDay = lastCal.get(Calendar.DAY_OF_MONTH);

            if ((lastYear == curYear) && (lastMon == curMon) && (lastDay == curDay)) {
                return false;
            }
        }

        // 2 check current time
        String scheduleExpr = userTask.getScheduleExpr();
        String[] scheduleExprParts = scheduleExpr.split(":");
        int exprHour = Integer.parseInt(scheduleExprParts[0]);
        int exprMin = Integer.parseInt(scheduleExprParts[1]);
        int exprSecond = Integer.parseInt(scheduleExprParts[2]);
        Calendar exprCal = Calendar.getInstance();
        exprCal.setTime(curDate);
        exprCal.set(Calendar.HOUR_OF_DAY, exprHour);
        exprCal.set(Calendar.MINUTE, exprMin);
        exprCal.set(Calendar.SECOND, exprSecond);

        if (curCal.compareTo(exprCal) >= 0) {
            return true;
        }

        return false;
    }

    protected boolean meetHourlyExecution(UserScheduledTaskEntity userTask) {
        if (!Constants.SCHEDULE_MODE_HOURLY.equalsIgnoreCase(userTask.getScheduleMode())) {
            return false;
        }

        if (StringUtils.isBlank(userTask.getScheduleExpr())) {
            return false;
        }

        Date curDate = new Date();
        Calendar curCal = Calendar.getInstance();
        curCal.setTime(curDate);
        int curYear = curCal.get(Calendar.YEAR);
        int curMon = curCal.get(Calendar.MONTH);
        int curDay = curCal.get(Calendar.DAY_OF_MONTH);
        int curHour = curCal.get(Calendar.HOUR_OF_DAY);

        // 1 check last execution time
        Date lastExecTime = userTask.getExecStartTime();
        if (lastExecTime != null) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTime(lastExecTime);
            int lastYear = lastCal.get(Calendar.YEAR);
            int lastMon = lastCal.get(Calendar.MONTH);
            int lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
            int lastHour = lastCal.get(Calendar.HOUR_OF_DAY);

            if ((lastYear == curYear) && (lastMon == curMon) && (lastDay == curDay) && (lastHour == curHour)) {
                return false;
            }
        }

        // 2 check current time
        String scheduleExpr = userTask.getScheduleExpr();
        String[] scheduleExprParts = scheduleExpr.split(":");
        int exprMin = Integer.parseInt(scheduleExprParts[0]);
        int exprSecond = Integer.parseInt(scheduleExprParts[1]);
        Calendar exprCal = Calendar.getInstance();
        exprCal.setTime(curDate);
        exprCal.set(Calendar.MINUTE, exprMin);
        exprCal.set(Calendar.SECOND, exprSecond);

        if (curCal.compareTo(exprCal) >= 0) {
            return true;
        }

        return false;
    }

    protected void performExecution(UserScheduledTaskEntity userTask) {
        log.info("try perform user scheduled task:{}", userTask.getId());
        String procDefId = userTask.getProcDefId();
        String rootEntityDataId = userTask.getEntityDataId();
        ProcessDataPreviewDto previewDto = workflowDataService.generateProcessDataPreview(procDefId, rootEntityDataId);

        StartProcInstRequestDto startProcInstRequestDto = new StartProcInstRequestDto();
        startProcInstRequestDto.setEntityDataId(rootEntityDataId);
        startProcInstRequestDto.setEntityDisplayName(userTask.getEntityDataName());

        String entityTypeId = null;// ?
        startProcInstRequestDto.setEntityTypeId(entityTypeId);
        startProcInstRequestDto.setProcDefId(procDefId);
        startProcInstRequestDto.setProcessSessionId(previewDto.getProcessSessionId());
        startProcInstRequestDto.setProcBatchKey(userTask.getId());

        ProcInstInfoDto procInstInfoDto = workflowProcInstService.createProcessInstance(startProcInstRequestDto);
        log.info("Process created:{}", procInstInfoDto);
    }

    protected void postPerformExecution(UserScheduledTaskEntity userTask) {

        int execTimes = userTask.getExecTimes();
        int newExecTimes = execTimes + 1;

        int expectedRev = userTask.getRev();
        int newRev = expectedRev + 1;
        if (newRev >= Integer.MAX_VALUE) {
            newRev = 0;
        }
        userTask.setExecEndTime(new Date());
        userTask.setStatus(Constants.SCHEDULE_TASK_READY);
        userTask.setRev(newRev);
        userTask.setExecTimes(newExecTimes);
        int updateResult = userScheduledTaskMapper.updateByPrimaryKeySelectiveCas(userTask, expectedRev);
        if (updateResult > 0) {
            log.debug("Post perform execution succeed:{}", userTask.getId());
        } else {
            log.debug("Post perform execution failed:{}", userTask.getId());
        }
    }

    private Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);

        try {
            Date date = df.parse(dateStr);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    private String formatStatiticsDate(Date date) {
        if (date == null) {
            return null;
        }

        String pattern = "yyyy-MM-dd HH:mm:ss SSS";
        DateFormat df = new SimpleDateFormat(pattern);
        String sDate = df.format(date);
        return sDate;
    }

    private int countTriggeredProcInstances(String procDefId, String status, Date startDate, Date endDate,
            String procBatchKey) {
        if (StringUtils.isBlank(procBatchKey)) {
            throw new WecubeCoreException("Process batch key can not be blank.");
        }
        int count = procInstInfoMapper.countByProcBatchKey(procDefId, status, startDate, endDate, procBatchKey);
        return count;
    }
}
