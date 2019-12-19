package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.support.datamodel.dto.DataModelExpressionDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

public class DataModelExpressionParserTest {
    private DataModelExpressionParser parser = new DataModelExpressionParser();

    @Test
    public void parseExpressionWithoutLastFetchOperationShouldAddALL() {
        String expression = "we-cmdb:system_design";
        Queue<DataModelExpressionDto> parseResult = parser.parse(expression);
        List<DataModelExpressionDto> expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:subsys_design.system_design>wecmdb:system_design";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:subsys~(subsys)wecmdb:unit";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:service_design~(service_design)wecmdb:invoke_design";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);

        expression = "wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone";
        parseResult = parser.parse(expression);
        expressionDtoList = new ArrayList<>(parseResult);
        assertThat(expressionDtoList.get(expressionDtoList.size() - 1).getExpression()).isEqualTo(expression + "." + DataModelExpressionParser.FETCH_ALL);
    }
}
