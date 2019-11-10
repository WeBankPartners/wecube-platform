package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.datamodel.DataModelExpressionDto;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataModelExpressionServiceImpl implements DataModelExpressionService {
    //    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}&sorting={sortName},asc";
    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}";
    private static final Logger logger = LoggerFactory.getLogger(DataModelExpressionServiceImpl.class);
    //    @Autowired
    private RestTemplate restTemplate = new RestTemplate();
    private static final String requestAllUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?sorting={sortName},asc";

    private String gatewayUrl;
    private String requestActualUrl = "";

    private String getGatewayUrl() {
        return gatewayUrl;
    }

    private void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    private String getRequestActualUrl() {
        return requestActualUrl;
    }

    private void setRequestActualUrl(String requestActualUrl) {
        this.requestActualUrl = requestActualUrl;
    }


    @Override
    public List<List<String>> fetchData(String gatewayUrl,
                                        List<Pair<String, String>> expressionToRootIdPairList
    ) throws WecubeCoreException {

        this.setGatewayUrl(gatewayUrl);
        List<Stack<DataModelExpressionDto>> resultList = new ArrayList<>();

        for (Pair<String, String> expressionToRootIdPair : expressionToRootIdPairList) {
            String dataModelExpression = expressionToRootIdPair.getLeft();
            String rootIdData = expressionToRootIdPair.getRight();
            Stack<DataModelExpressionDto> resultDtoStack = new Stack<>();

            Queue<DataModelExpressionDto> expressionDtoQueue = new DataModelExpressionParser().parse(dataModelExpression);

            if (expressionDtoQueue.size() == 0) {
                String msg = String.format("Cannot extract information from the given expression [%s].", dataModelExpression);
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
            boolean isStart = true;
            List<CommonResponseDto> lastRequestResult = new ArrayList<>();
            while (!expressionDtoQueue.isEmpty()) {
                DataModelExpressionDto expressionDto = expressionDtoQueue.poll();
                if (isStart) {
                    resolveLink(expressionDto, rootIdData);
                    isStart = false;
                } else {
                    resolveLink(expressionDto, lastRequestResult);
                }
                if (!expressionDto.getReturnedJson().empty()) {
                    lastRequestResult = expressionDto.getReturnedJson().peek();
                }
                resultDtoStack.add(expressionDto);
            }
            resultList.add(resultDtoStack);
        }

        return resultList.stream().map(expressionDtoStack -> expressionDtoStack.pop().getResultValue()).collect(Collectors.toList());
    }

    private void resolveLink(DataModelExpressionDto expressionDto, String rootIdData) throws WecubeCoreException {
        // only invoke this condition when one "entity fetch" situation occurs
        if (expressionDto.getOpTo() == null && expressionDto.getOpBy() == null && expressionDto.getOpFetch() != null) {

            DataModelParser.EntityContext entity = expressionDto.getEntity();
            DataModelParser.FetchContext opFetch = expressionDto.getOpFetch();

            // request
            String requestPackageName = entity.pkg().getText();
            String requestEntityName = entity.ety().getText();
            Map<String, String> requestParamMap = generateRefToParamMap(
                    this.gatewayUrl,
                    requestPackageName,
                    requestEntityName,
                    "id",
                    rootIdData,
                    "id");
            CommonResponseDto requestResponseDto = request(requestUrl, requestParamMap);
            expressionDto.getRequestUrlStack().add(Collections.singleton(requestActualUrl));
            expressionDto.getReturnedJson().add(Collections.singletonList(requestResponseDto));

            String secondRequstAttrName = opFetch.attr().getText();
            List<String> finalResult = commonResponseToList(requestResponseDto, secondRequstAttrName);
            expressionDto.setResultValue(finalResult);
        }

        // only invoke this function when the first link with rootIdData is processed
        // no need to process prev_link
        if (expressionDto.getOpTo() != null) {
            // refTo
            DataModelParser.Fwd_nodeContext fwdNode = expressionDto.getFwdNode();
            DataModelParser.EntityContext entity = expressionDto.getEntity();

            // first request
            String firstRequestPackageName = fwdNode.entity().pkg().getText();
            String firstRequestEntityName = fwdNode.entity().ety().getText();

            Map<String, String> firstRequestParamMap = generateRefToParamMap(
                    this.gatewayUrl,
                    firstRequestPackageName,
                    firstRequestEntityName,
                    "id",
                    rootIdData,
                    "id");
            CommonResponseDto firstRequestResponseDto = request(requestUrl, firstRequestParamMap);
            expressionDto.getRequestUrlStack().add(Collections.singleton(requestActualUrl));
            expressionDto.getReturnedJson().add(Collections.singletonList(firstRequestResponseDto));

            // second request
            // fwdNode returned data is the second request's id data
            String secondRequestPackageName = entity.pkg().getText();
            String secondRequestEntityName = entity.ety().getText();
            String secondRequestAttrName = fwdNode.attr().getText();
            List<String> secondRequestIdDataList = commonResponseToList(firstRequestResponseDto, secondRequestAttrName);
            List<CommonResponseDto> responseDtoList = new ArrayList<>();
            for (String secondRequestIdData : secondRequestIdDataList) {
                Map<String, String> secondRequestParamMap = generateRefToParamMap(
                        this.gatewayUrl,
                        secondRequestPackageName,
                        secondRequestEntityName,
                        "id",
                        secondRequestIdData,
                        "id");
                CommonResponseDto secondRequestResponse = request(requestUrl, secondRequestParamMap);
                responseDtoList.add(secondRequestResponse);
            }
            expressionDto.getRequestUrlStack().add(Collections.singleton(requestActualUrl));
            expressionDto.getReturnedJson().add(responseDtoList);
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
            Map<String, String> secondRequestParamMap = generateRefToParamMap(
                    this.gatewayUrl,
                    secondRequestPackageName,
                    secondRequestEntityName,
                    secondRequestAttributeName,
                    rootIdData,
                    "id");
            CommonResponseDto secondRequestResponse = request(requestUrl, secondRequestParamMap);  // this response may have data with one or multiple lines.
            expressionDto.getRequestUrlStack().add(Collections.singleton(requestActualUrl));
            expressionDto.getReturnedJson().add(Collections.singletonList(secondRequestResponse));

        }
    }

    private void resolveLink(DataModelExpressionDto expressionDto, List<CommonResponseDto> lastRequestResultList) throws WecubeCoreException {
        // only invoke this function when the non-first link is processed
        // no need to process fwdNode
        if (expressionDto.getOpTo() != null) {
            // refTo
            String requestId = expressionDto.getOpFetch().attr().getText();
            String requestPackageName = expressionDto.getEntity().pkg().getText();
            String requestEntityName = expressionDto.getEntity().ety().getText();

            List<CommonResponseDto> responseDtoList = new ArrayList<>();
            Set<String> requestUrlSet = new HashSet<>();
            for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {
                List<String> requestIdDataList = commonResponseToList(lastRequestResponseDto, requestId);
                for (String requestIdData : requestIdDataList) {
                    Map<String, String> requestParamMap = generateRefToParamMap(
                            this.gatewayUrl,
                            requestPackageName,
                            requestEntityName,
                            "id",
                            requestIdData,
                            "id");
                    CommonResponseDto requestResponse = request(requestUrl, requestParamMap);
                    requestUrlSet.add(requestActualUrl);
                    responseDtoList.add(requestResponse);
                }
            }
            expressionDto.getRequestUrlStack().add(requestUrlSet);
            expressionDto.getReturnedJson().add(responseDtoList);
        }

        if (expressionDto.getOpBy() != null) {
            // refBy
            DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
            String requestPackageName = bwdNode.entity().pkg().getText();
            String requestEntityName = bwdNode.entity().ety().getText();
            String requestAttributeName = bwdNode.attr().getText();

            List<CommonResponseDto> responseDtoList = new ArrayList<>();
            Set<String> requestUrlSet = new HashSet<>();
            for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {
                List<String> requestIdDataList = commonResponseToList(lastRequestResponseDto, "id");
                for (String requestIdData : requestIdDataList) {
                    Map<String, String> requestParamMap = generateRefToParamMap(
                            this.gatewayUrl,
                            requestPackageName,
                            requestEntityName,
                            requestAttributeName,
                            requestIdData,
                            "id");
                    CommonResponseDto requestResponse = request(requestUrl, requestParamMap);
                    requestUrlSet.add(requestActualUrl);
                    responseDtoList.add(requestResponse);
                }
            }
            expressionDto.getRequestUrlStack().add(requestUrlSet);
            expressionDto.getReturnedJson().add(responseDtoList);
        }

        if (expressionDto.getOpBy() == null && expressionDto.getOpTo() == null && expressionDto.getOpFetch() != null) {
            // route
            String attrName = expressionDto.getOpFetch().attr().getText();
            List<String> resultValueList = new ArrayList<>();
            for (CommonResponseDto lastRequestResult : lastRequestResultList) {
                List<String> fetchDataList = commonResponseToList(lastRequestResult, attrName);
                resultValueList.addAll(fetchDataList);
            }
            expressionDto.setResultValue(resultValueList);
        }
    }

    private Map<String, String> generateRefToParamMap(String gatewayUrl,
                                                      String packageName,
                                                      String entityName,
                                                      String attributeName,
                                                      String value,
                                                      String sortName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("attributeName", attributeName);
        paramMap.put("value", value);
//        paramMap.add("sortName", sortName);
        return paramMap;
    }

    private Map<String, String> generateRequestAllParamMap(String gatewayUrl,
                                                           String packageName,
                                                           String entityName,
                                                           String sortName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("sortName", sortName);
        return paramMap;
    }

    private CommonResponseDto request(String requestUrl, Map<String, String> paramMap) throws WecubeCoreException {
        ResponseEntity<String> response;
        CommonResponseDto responseDto = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            // combine url with param map
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
            UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(paramMap);
            String uriStr = uriComponents.toString();
            if (!this.getRequestActualUrl().equals(uriStr))
                this.setRequestActualUrl(uriStr);
            response = RestTemplateUtils.sendGetRequestWithParamMap(restTemplate, uriStr, httpHeaders);
            if (StringUtils.isEmpty(response.getBody()) || response.getStatusCode().isError()) {
                throw new WecubeCoreException(response.toString());
            }
            responseDto = JsonUtils.toObject(response.getBody(), CommonResponseDto.class);
            if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
                String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return responseDto;
    }

    private List<String> commonResponseToList(CommonResponseDto responseDto, String attributeName) {
        // transfer dto to List<LinkedTreeMap>
        List<LinkedHashMap<String, Object>> dataArray = new ArrayList<>();
        List<String> returnList = new ArrayList<>();
        String dataTypeSimpleName = responseDto.getData().getClass().getSimpleName();

        if (ArrayList.class.getSimpleName().equals(dataTypeSimpleName)) {
            dataArray = (List<LinkedHashMap<String, Object>>) responseDto.getData();
        }

        if (LinkedHashMap.class.getSimpleName().equals(dataTypeSimpleName)) {
            dataArray.add((LinkedHashMap) responseDto.getData());
        }

        if (DataModelExpressionParser.FETCH_ALL.equals(attributeName)) {
            returnList = Objects.requireNonNull(dataArray)
                    .stream()
                    .map(AbstractMap::toString)
                    .collect(Collectors.toList());
        } else {
            returnList = Objects.requireNonNull(dataArray)
                    .stream()
                    .map(linkedTreeMap -> linkedTreeMap.get(attributeName) == null ? "null" : linkedTreeMap.get(attributeName).toString())
                    .collect(Collectors.toList());
        }

        return returnList;
    }


}
