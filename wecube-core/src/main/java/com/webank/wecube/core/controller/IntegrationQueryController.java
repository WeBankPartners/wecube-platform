package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import static com.webank.wecube.core.domain.JsonResponse.error;
import static com.webank.wecube.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.core.domain.MenuItem.*;

@RestController
@Slf4j
@RequestMapping("/cmdb")
public class IntegrationQueryController {
    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_EXECUTION, MENU_IMPLEMENTATION_BATCH_JOB})
    @GetMapping("/intQuery/{queryId}/header")
    @ResponseBody
    public JsonResponse queryIntHeader(@PathVariable(name = "queryId") int queryId) {
        return okayWithData(cmdbServiceV2Stub.queryIntHeader(queryId));
    }

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT })
    @PostMapping("/intQuery/{queryId}/update")
    @ResponseBody
    public JsonResponse updateIntQuery(@PathVariable("queryId") int queryId, @RequestBody IntegrationQueryDto intQueryDto) {
        return okayWithData(cmdbServiceV2Stub.updateIntQuery(queryId, intQueryDto));
    }

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT })
    @PostMapping("/intQuery/ciType/{ciTypeId}/{queryId}/delete")
    public JsonResponse deleteQuery(@PathVariable("ciTypeId") Integer ciTypeId, @PathVariable("queryId") int queryId) {
        return okayWithData(cmdbServiceV2Stub.deleteQuery(ciTypeId,queryId));
    }

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT })
    @PostMapping("/intQuery/ciType/{ciTypeId}/{queryName}/save")
    public JsonResponse saveIntQuery(@PathVariable("ciTypeId") Integer ciTypeId, @PathVariable("queryName") String queryName, @RequestBody IntegrationQueryDto intQueryDto) {
        return okayWithData(cmdbServiceV2Stub.saveIntQuery(ciTypeId, queryName, intQueryDto));
    }


    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_EXECUTION, MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT, MENU_IMPLEMENTATION_BATCH_JOB})
    @GetMapping("/intQuery/ciType/{ciTypeId}/search")
    public JsonResponse searchIntQuery(@PathVariable("ciTypeId") Integer ciTypeId, @RequestParam(value = "name", required = false) String name) {
        return okayWithData(cmdbServiceV2Stub.searchIntQuery(ciTypeId, name));
    }

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT })
    @GetMapping("/intQuery/ciType/{ciTypeId}/{queryId}")
    public JsonResponse getIntQueryByName(@PathVariable("ciTypeId") Integer ciTypeId, @PathVariable(value = "queryId") Integer queryId) {
        return okayWithData(cmdbServiceV2Stub.getIntQueryByName(ciTypeId, queryId));
    }

    @RolesAllowed({ MENU_DESIGNING_CI_INTEGRATED_QUERY_EXECUTION, MENU_IMPLEMENTATION_BATCH_JOB})
    @PostMapping("/intQuery/{queryId}/execute")
    public JsonResponse excuteIntQuery(@PathVariable("queryId") int queryId, @RequestBody PaginationQuery aggRequest) {
        return okayWithData(cmdbServiceV2Stub.excuteIntQuery(queryId, aggRequest));
    }

}



