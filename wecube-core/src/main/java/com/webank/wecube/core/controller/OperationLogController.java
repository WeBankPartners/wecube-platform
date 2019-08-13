package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import static com.webank.wecube.core.domain.JsonResponse.okay;
import static com.webank.wecube.core.domain.MenuItem.MENU_ADMIN_OPERATION_LOG;

@RestController
@Slf4j
@RequestMapping("/operation-log")
@RolesAllowed({MENU_ADMIN_OPERATION_LOG})
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @PostMapping("/query")
    @ResponseBody
    public JsonResponse queryOperationLog(@RequestBody QueryRequest queryRequest) {
        return okay().withData(operationLogService.query(queryRequest));
    }

}



