package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskDto;
import com.webank.wecube.platform.core.dto.workflow.UserScheduledTaskQueryDto;
import com.webank.wecube.platform.core.service.workflow.UserScheduledTaskService;

@RestController
@RequestMapping("/v1")
public class UserScheduledTaskController {

    @Autowired
    private UserScheduledTaskService userScheduledTaskService;

    /**
     * 
     * @param taskDto
     * @return
     */
    @PostMapping("/user-scheduled-tasks/create")
    public CommonResponseDto createUserScheduledTask(@RequestBody UserScheduledTaskDto taskDto) {
        UserScheduledTaskDto resultDto = userScheduledTaskService.createUserScheduledTask(taskDto);
        return CommonResponseDto.okayWithData(resultDto);
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    @PostMapping("/user-scheduled-tasks/stop")
    public CommonResponseDto stopUserScheduledTasks(@RequestBody List<UserScheduledTaskDto> taskDtos) {
        userScheduledTaskService.stopUserScheduledTasks(taskDtos);
        return CommonResponseDto.okay();

    }

    /**
     * 
     * @param queryDto
     * @return
     */
    @PostMapping("/user-scheduled-tasks/query")
    public CommonResponseDto fetchUserScheduledTasks(@RequestBody UserScheduledTaskQueryDto queryDto) {
        List<UserScheduledTaskDto> taskDtos = userScheduledTaskService.fetchUserScheduledTasks(queryDto);
        return CommonResponseDto.okayWithData(taskDtos);
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    @PostMapping("/user-scheduled-tasks/update")
    public CommonResponseDto updateUserSchecduledTasks(@RequestBody List<UserScheduledTaskDto> taskDtos) {
        List<UserScheduledTaskDto> resultTaskDtos = userScheduledTaskService.updateUserSchecduledTasks(taskDtos);
        return CommonResponseDto.okayWithData(resultTaskDtos);
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    @PostMapping("/user-scheduled-tasks/delete")
    public CommonResponseDto deleteUserSchecduledTasks(@RequestBody List<UserScheduledTaskDto> taskDtos) {
        userScheduledTaskService.deleteUserSchecduledTasks(taskDtos);
        return CommonResponseDto.okay();
    }

    /**
     * 
     * @param taskDtos
     * @return
     */
    @PostMapping("/user-scheduled-tasks/resume")
    public CommonResponseDto resumeUserScheduledTasks(@RequestBody List<UserScheduledTaskDto> taskDtos) {
        userScheduledTaskService.resumeUserScheduledTasks(taskDtos);
        return CommonResponseDto.okay();
    }

}
