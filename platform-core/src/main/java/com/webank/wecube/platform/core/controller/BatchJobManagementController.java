package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.platform.core.domain.MenuItem.MENU_IMPLEMENTATION_BATCH_JOB;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.BatchJobService;
import com.webank.wecube.platform.core.service.batchjob.CreateBatchJobRequest;
import com.webank.wecube.platform.core.service.batchjob.CreateBatchJobResponse;
import com.webank.wecube.platform.core.service.batchjob.GetContextRequest;
import com.webank.wecube.platform.core.service.batchjob.SearchTextProcessorRequest;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/batch-job")
@RolesAllowed({MENU_IMPLEMENTATION_BATCH_JOB})
public class BatchJobManagementController {
    @Autowired
    private BatchJobService batchJobService;

    @PostMapping("/create")
    @ResponseBody
    public JsonResponse createBatchJob(@RequestBody CreateBatchJobRequest request) throws IOException {
        CreateBatchJobResponse resp = batchJobService.createBatchJob(request);
        return okayWithData(resp);
    }

    @PostMapping("/{batch-job-id}/execute")
    @ResponseBody
    public JsonResponse executeBatchJob(@PathVariable(value = "batch-job-id") Integer batchJobId) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    batchJobService.executeBatchJob(batchJobId);
                } catch (IOException e) {
                    log.error("executeBatchJob meet error: ", e);
                }
            }
        };
        thread.start();
        return okay();
    }

    @PostMapping("/search-text")
    @ResponseBody
    public JsonResponse searchTextProcessor(@RequestBody SearchTextProcessorRequest request) {
        Object resp = batchJobService.searchTextProcessor(request);
        return okayWithData(resp);
    }

    @PostMapping("/get-context")
    @ResponseBody
    public JsonResponse getContext(@RequestBody GetContextRequest request) {
        Object resp = batchJobService.getContext(request);
        return okayWithData(resp);
    }
}