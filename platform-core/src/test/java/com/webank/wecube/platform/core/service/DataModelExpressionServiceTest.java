package com.webank.wecube.platform.core.service;

import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

public class DataModelExpressionServiceTest {
    //    @Autowired
//    private RestTemplate restTemplate;
    Gson gson = null;
    String wecmdbUrlGateWayUrl = "localhost:8081";

    //    @Autowired
    DataModelExpressionServiceImpl dataModelExpressionService;

    @Before
    public void setup() {
        dataModelExpressionService = new DataModelExpressionServiceImpl();
        gson = new Gson();
    }

    @Test
    public void wecmdbFwdNodeExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbBwdNodeExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockBwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbOneLinkWithOpToExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbMultipleLinksWithOpToOnlyExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbOneLinkWithOpByExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbMultipleLinksWithOpByOnlyExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    @Test
    public void wecmdbMultipleLinksWithMixedOpExpressionShouldSuccess() {
        List<Pair<String, String>> mockedInputData = mockFwdNodeExpression();
        List<List<String>> result = dataModelExpressionService.fetchData(wecmdbUrlGateWayUrl, mockedInputData);
        assert result.get(0).get(0).equals("EDP");
        assert result.get(1).get(0).equals("EDP-CORE_PRD-APP");
    }

    private List<Pair<String, String>> mockFwdNodeExpression() {
        Pair<String, String> pairOne = new ImmutablePair<>("wecmdb:system_design.code", "0001_0000000001");
        Pair<String, String> pairTwo = new ImmutablePair<>("wecmdb:unit.key_name", "0008_0000000003");
        return Arrays.asList(pairOne, pairTwo);
    }

    private List<Pair<String, String>> mockBwdNodeExpression() {
        Pair<String, String> pairOne = new ImmutablePair<>("wecmdb:system_design.code", "0001_0000000001");
        Pair<String, String> pairTwo = new ImmutablePair<>("wecmdb:unit.key_name", "0008_0000000003");
        return Arrays.asList(pairOne, pairTwo);
    }

    private MultiValueMap<String, String> mockParamMap() {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("packageName", "wecmdb");
        paramMap.add("entityName", "system_design");
        paramMap.add("attributeName", "id");
        paramMap.add("value", "0001_0000000001");
        return paramMap;
    }

}
