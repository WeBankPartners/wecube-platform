package com.webank.wecube.platform.core.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.DataModelExpressionDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.utils.HttpClientUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Service
public class DataModelExpressionServiceImpl implements DataModelExpressionService {
//    @Autowired
//    PluginPackageDataModelServiceImpl dataModelService;
//    @Autowired
//    PluginPackageEntityRepository entityRepository;
//    @Autowired
//    PluginPackageAttributeRepository attributeRepository;

    private String gatewayUrl;
    private static final Logger logger = LoggerFactory.getLogger(DataModelExpressionServiceImpl.class);
    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}&sorting={sortName},asc";

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }


    @Override
    public List<Stack<DataModelExpressionDto>> fetchData(String gatewayUrl, List<String> dataModelExpressionList, List<String> rootIdDataList) throws WecubeCoreException {
        if (dataModelExpressionList.size() != rootIdDataList.size()) {
            String msg = "The size of input data model expression and root id data mismatch.";
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        this.setGatewayUrl(gatewayUrl);
        List<Stack<DataModelExpressionDto>> resultList = new ArrayList<>();
        int batchDataSize = dataModelExpressionList.size();

        for (int i = 0; i < batchDataSize; i++) {
            String dataModelExpression = dataModelExpressionList.get(i);
            String rootIdData = rootIdDataList.get(i);
            Stack<DataModelExpressionDto> resultDtoStack = new Stack<>();

            Queue<DataModelExpressionDto> expressionDtoQueue = new DataModelExpressionParser().parse(dataModelExpression);

            if (expressionDtoQueue.size() == 0) {
                String msg = String.format("Cannot extract information from the given expression [%s].", dataModelExpression);
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
            boolean isStart = true;
            Map<String, String> lastRequestResult = new HashMap<>();
            while (!expressionDtoQueue.isEmpty()) {
                DataModelExpressionDto expressionDto = expressionDtoQueue.poll();
                if (isStart) {
                    resolveLink(expressionDto, rootIdData);
                    isStart = false;
                } else {
                    resolveLink(expressionDto, lastRequestResult);
                }
                lastRequestResult = expressionDto.getReturnedJson().peek();
                resultDtoStack.add(expressionDto);
            }
            resultList.add(resultDtoStack);
        }

        return resultList;
    }

    private void resolveLink(DataModelExpressionDto expressionDto, String rootIdData) throws WecubeCoreException {
        // only invoke this function when the first link with rootIdData is processed
        // no need to process prev_link
        if (expressionDto.getOpTo() != null) {
            // refTo
            DataModelParser.Fwd_nodeContext fwdNode = expressionDto.getFwdNode();
            DataModelParser.EntityContext entity = expressionDto.getEntity();

            // first request
            String firstRequestPackageName = fwdNode.entity().pkg().getText();
            String firstRequestEntityName = fwdNode.entity().ety().getText();
            String firstRequestAttrName = fwdNode.attr().getText();
            MultiValueMap<String, String> firstRequestParamMap = generateRefToParamMap(requestUrl, firstRequestPackageName, firstRequestEntityName, "id", rootIdData, "");
            Map<String, String> firstRequestResponse = request(requestUrl, firstRequestParamMap);
            expressionDto.getReturnedJson().add(firstRequestResponse);

            // second request
            // fwdNode returned data is the second request's id data
            String secondRequestPackageName = entity.pkg().getText();
            String secondRequestEntityName = entity.ety().getText();
            String secondRequestIdData = firstRequestResponse.get(firstRequestAttrName);
            MultiValueMap<String, String> secondRequestParamMap = generateRefToParamMap(requestUrl, secondRequestPackageName, secondRequestEntityName, "id", secondRequestIdData, "");
            Map<String, String> secondRequestResponse = request(requestUrl, secondRequestParamMap);
            expressionDto.getReturnedJson().add(secondRequestResponse);
        }

        if (expressionDto.getOpBy() != null) {
            // refBy
            DataModelParser.EntityContext entity = expressionDto.getEntity();
            DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();

            // first request
            // TODO: verify that the rootIdData is in the given entity

            // second request
            String secondRequestPackageName = bwdNode.entity().pkg().getText();
            String secondRequestEntityName = bwdNode.entity().ety().getText();
            String secondRequestAttributeName = bwdNode.attr().getText();
            MultiValueMap<String, String> secondRequestParamMap = generateRefToParamMap(requestUrl, secondRequestPackageName, secondRequestEntityName, secondRequestAttributeName, rootIdData, "");
            Map<String, String> secondRequestResponse = request(requestUrl, secondRequestParamMap);
            expressionDto.getReturnedJson().add(secondRequestResponse);

        }
    }

    private void resolveLink(DataModelExpressionDto expressionDto, Map<String, String> lastRequestResult) throws WecubeCoreException {
        // only invoke this function when the non-first link is processed
        // no need to process fwdNode
        if (expressionDto.getOpTo() != null) {
            // refTo
            String requestId = expressionDto.getOpFetch().attr().getText();
            String requestIdData = lastRequestResult.get(requestId);
            String requestPackageName = expressionDto.getEntity().pkg().getText();
            String requestEntityName = expressionDto.getEntity().ety().getText();
            MultiValueMap<String, String> requestParamMap = generateRefToParamMap(requestUrl, requestPackageName, requestEntityName, "id", requestIdData, "");
            Map<String, String> requestResponse = request(requestUrl, requestParamMap);
            expressionDto.getReturnedJson().add(requestResponse);
        }

        if (expressionDto.getOpBy() != null) {
            // refBy
            DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
            String requestPackageName = bwdNode.entity().pkg().getText();
            String requestEntityName = bwdNode.entity().ety().getText();
            String requestAttributeName = bwdNode.attr().getText();
            String requestIdData = lastRequestResult.get("id");
            MultiValueMap<String, String> requestParamMap = generateRefToParamMap(requestUrl, requestPackageName, requestEntityName, requestAttributeName, requestIdData, "");
            Map<String, String> requestResponse = request(requestUrl, requestParamMap);
            expressionDto.getReturnedJson().add(requestResponse);
        }

        if (expressionDto.getOpBy() == null && expressionDto.getOpTo() == null && expressionDto.getOpFetch() != null) {
            // route
            String attrName = expressionDto.getOpFetch().attr().getText();
            expressionDto.setResultValue(lastRequestResult.get(attrName));
        }
    }

    private MultiValueMap<String, String> generateRefToParamMap(String gatewayUrl, String packageName, String entityName, String attributeName, String value, String sortName) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("gatewayUrl", gatewayUrl);
        paramMap.add("packageName", packageName);
        paramMap.add("entityName", entityName);
        paramMap.add("attrName", attributeName);
        paramMap.add("value", value);
        paramMap.add("sortName", sortName);
        return paramMap;
    }

    private Map<String, String> request(String requestUrl, MultiValueMap<String, String> paramMap) throws WecubeCoreException {
        ResponseEntity<String> response;
        Map<String, String> responseBodyMap = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            response = HttpClientUtils.sendGetRequestWithParamMap(requestUrl, paramMap, httpHeaders);
            if (StringUtils.isEmpty(response.getBody()) || response.getStatusCode().isError()) {
                throw new WecubeCoreException(response.toString());
            }
            responseBodyMap = JsonUtils.toObject(response.getBody(), Map.class);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return responseBodyMap;
    }

    public static void main(String[] args) {
//        DataModelExpressionServiceImpl service = new DataModelExpressionServiceImpl();
//        service.fetchData("127.0.0.1:80", "A:a.a1-B:b.b2-C:c.c2-D:d.d2", "a0", "10");
        String url = "https://support.oneskyapp.com/hc/en-us/article_attachments/202761727/example_2.json";
        try {
            ResponseEntity<String> stringResponseEntity = HttpClientUtils.sendGetRequestWithoutParam(url);
//            Map<String, Object> returnedJson = JsonUtils.toObject(Objects.requireNonNull(stringResponseEntity.getBody()), Map.class);
            JsonElement parse = new JsonParser().parse(Objects.requireNonNull(stringResponseEntity.getBody()));
//            System.out.println(returnedJson);
            JsonArray options = parse.getAsJsonObject().get("quiz").getAsJsonObject().get("sport").getAsJsonObject().get("q1").getAsJsonObject().get("options").getAsJsonArray();
            JsonElement jsonElement = parse.getAsJsonObject().get("quiz").getAsJsonObject().get("sport").getAsJsonObject().get("q1").getAsJsonObject().get("question");
            System.out.println(jsonElement.isJsonArray());
            System.out.println(jsonElement.getAsString());
            System.out.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }


}
