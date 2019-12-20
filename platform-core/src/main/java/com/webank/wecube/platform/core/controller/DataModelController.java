package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DmeDto;
import com.webank.wecube.platform.core.service.datamodel.ExpressionServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

@RestController
@RequestMapping("/v1")
public class DataModelController {

    @Autowired
    private ExpressionServiceImpl expressionServiceImpl;

    @PostMapping("/data-model/dme/all-entities")
    @ResponseBody
    public CommonResponseDto getAllEntitiesByDme(@RequestBody DmeDto request) {
        return okayWithData(expressionServiceImpl.getAllEntities(request.getDataModelExpression()));
    }

}
