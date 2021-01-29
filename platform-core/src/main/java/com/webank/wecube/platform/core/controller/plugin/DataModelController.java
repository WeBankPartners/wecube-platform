package com.webank.wecube.platform.core.controller.plugin;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.DmeDto;
import com.webank.wecube.platform.core.dto.plugin.DmeFilterDto;
import com.webank.wecube.platform.core.service.datamodel.ExpressionServiceImpl;
import com.webank.wecube.platform.core.service.datamodel.RootlessExpressionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.error;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class DataModelController {

    @Autowired
    private ExpressionServiceImpl expressionServiceImpl;
    @Autowired
    private RootlessExpressionServiceImpl rootlessExpressionService;

    @PostMapping("/data-model/dme/all-entities")
    @ResponseBody
    public CommonResponseDto getAllEntitiesByDme(@RequestBody DmeDto request) {
        return okayWithData(expressionServiceImpl.getAllEntities(request.getDataModelExpression()));
    }

    @PostMapping("/data-model/dme/integrated-query")
    @ResponseBody
    public CommonResponseDto getAllEntitiesByDme(@RequestBody DmeFilterDto dmeFilterDto) {
        List<Object> result;
        try {
            result = rootlessExpressionService.fetchDataWithFilter(dmeFilterDto);
        } catch (WecubeCoreException ex) {
            return error(ex.getMessage());
        }
        return okayWithData(result);
    }

}
