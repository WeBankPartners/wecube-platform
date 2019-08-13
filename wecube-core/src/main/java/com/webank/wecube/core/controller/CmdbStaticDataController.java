package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.webank.wecube.core.domain.JsonResponse.okayWithData;

@RestController
@Slf4j
@RequestMapping("/cmdb")
public class CmdbStaticDataController {
    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;
    @Autowired
    private CmdbResourceService cmdbResourceService;

    @GetMapping("/static-data/available-ci-type-table-status")
    @ResponseBody
    public JsonResponse getAvailableCiTypeTableStatus() {
        return okayWithData(cmdbServiceV2Stub.getConstantsCiStatus());
    }

    @GetMapping("/static-data/available-ci-type-attribute-input-types")
    @ResponseBody
    public JsonResponse getAvailableCiTypeAttributeInputTypes() {
        return okayWithData(cmdbServiceV2Stub.getAvailableCiTypeAttributeInputTypes());
    }

    @GetMapping("/static-data/effective-status")
    @ResponseBody
    public JsonResponse getEffectiveStatus() {
        return okayWithData(cmdbServiceV2Stub.getEffectiveStatus());
    }


}



