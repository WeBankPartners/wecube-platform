package com.webank.wecube.platform.core.service;

import com.google.gson.internal.LinkedTreeMap;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DataModelExpressionDto;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.utils.HttpClientUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataModelExpressionServiceImpl implements DataModelExpressionService {
//    @Autowired
//    PluginPackageDataModelServiceImpl dataModelService;
//    @Autowired
//    PluginPackageEntityRepository entityRepository;
//    @Autowired
//    PluginPackageAttributeRepository attributeRepository;


    private static final Logger logger = LoggerFactory.getLogger(DataModelExpressionServiceImpl.class);
    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}&sorting={sortName},asc";
    private static final String requestAllUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?sorting={sortName},asc";

    private String gatewayUrl;
    private String requestActualUrl;

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getRequestActualUrl() {
        return requestActualUrl;
    }

    public void setRequestActualUrl(String requestActualUrl) {
        this.requestActualUrl = requestActualUrl;
    }

    @Override
    public List<Stack<DataModelExpressionDto>> fetchData(String gatewayUrl,
                                                         List<String> dataModelExpressionList,
                                                         List<String> rootIdDataList) throws WecubeCoreException {
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
            List<CommonResponseDto> lastRequestResult = new ArrayList<>();
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
        // only invoke this condition when one "entity fetch" situation occurs
        if (expressionDto.getOpTo() == null && expressionDto.getOpBy() == null && expressionDto.getOpFetch() != null) {

            DataModelParser.EntityContext entity = expressionDto.getEntity();
            DataModelParser.FetchContext opFetch = expressionDto.getOpFetch();

            // request
            String requestPackageName = entity.pkg().getText();
            String requestEntityName = entity.ety().getText();
            MultiValueMap<String, String> requestParamMap = generateRefToParamMap(
                    requestUrl,
                    requestPackageName,
                    requestEntityName,
                    "id",
                    rootIdData,
                    "");
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

            MultiValueMap<String, String> firstRequestParamMap = generateRefToParamMap(
                    requestUrl,
                    firstRequestPackageName,
                    firstRequestEntityName,
                    "id",
                    rootIdData,
                    "");
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
                MultiValueMap<String, String> secondRequestParamMap = generateRefToParamMap(
                        requestUrl,
                        secondRequestPackageName,
                        secondRequestEntityName,
                        "id",
                        secondRequestIdData,
                        "");
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
            MultiValueMap<String, String> secondRequestParamMap = generateRefToParamMap(
                    requestUrl,
                    secondRequestPackageName,
                    secondRequestEntityName,
                    secondRequestAttributeName,
                    rootIdData,
                    "");
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
                    MultiValueMap<String, String> requestParamMap = generateRefToParamMap(
                            requestUrl,
                            requestPackageName,
                            requestEntityName,
                            "id",
                            requestIdData,
                            "");
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
                    MultiValueMap<String, String> requestParamMap = generateRefToParamMap(
                            requestUrl,
                            requestPackageName,
                            requestEntityName,
                            requestAttributeName,
                            requestIdData,
                            "");
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

    private MultiValueMap<String, String> generateRefToParamMap(String gatewayUrl,
                                                                String packageName,
                                                                String entityName,
                                                                String attributeName,
                                                                String value,
                                                                String sortName) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("gatewayUrl", gatewayUrl);
        paramMap.add("packageName", packageName);
        paramMap.add("entityName", entityName);
        paramMap.add("attrName", attributeName);
        paramMap.add("value", value);
        paramMap.add("sortName", sortName);
        return paramMap;
    }

    private MultiValueMap<String, String> generateRequestAllParamMap(String gatewayUrl,
                                                                     String packageName,
                                                                     String entityName,
                                                                     String sortName) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("gatewayUrl", gatewayUrl);
        paramMap.add("packageName", packageName);
        paramMap.add("entityName", entityName);
        paramMap.add("sortName", sortName);
        return paramMap;
    }

    private CommonResponseDto request(String requestUrl, MultiValueMap<String, String> paramMap) throws WecubeCoreException {
        ResponseEntity<String> response;
        CommonResponseDto responseDto = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            // combine url with param map
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(requestUrl).queryParams(paramMap);
            String requestUri = uriBuilder.toUriString();
            if (!this.getRequestActualUrl().equals(requestUri)) this.setRequestActualUrl(requestUri);
            response = HttpClientUtils.sendGetRequestWithParamMap(requestUri, httpHeaders);
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
        List<LinkedTreeMap> dataArray = null;
        List<String> returnList = new ArrayList<>();
        String dataTypeSimpleName = responseDto.getData().getClass().getSimpleName();

        if (ArrayList.class.getSimpleName().equals(dataTypeSimpleName)) {
            dataArray = new ArrayList<>((List<LinkedTreeMap>) responseDto.getData());
        }

        if (LinkedTreeMap.class.getSimpleName().equals(dataTypeSimpleName)) {
            dataArray = new ArrayList<>();
            dataArray.add((LinkedTreeMap) responseDto.getData());
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
