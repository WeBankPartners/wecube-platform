package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelParser;
import com.webank.wecube.platform.core.support.datamodel.DataFlowTreeDto;
import com.webank.wecube.platform.core.support.datamodel.DataModelExpressionDto;
import com.webank.wecube.platform.core.support.datamodel.TreeNode;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RestTemplate restTemplate;
    private ApplicationProperties applicationProperties;

    private static final Logger logger = LoggerFactory.getLogger(DataModelExpressionServiceImpl.class);
    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}";
    private static final String createRequestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}/create";
    private static final String updateRequestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}/update";
    private static final String deleteRequestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}/delete";
    private static final String requestAllUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}";
    final String UNIQUE_IDENTIFIER = "id";


    @Autowired
    public DataModelExpressionServiceImpl(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }


    @Override
    public List<Object> fetchData(DataModelExpressionToRootData dataModelExpressionToRootData) {

        Stack<DataModelExpressionDto> resultDtoStack = chainRequest(new DataFlowTreeDto(), dataModelExpressionToRootData);

        return resultDtoStack.pop().getResultValue();
    }


    @Override
    public void writeBackData(DataModelExpressionToRootData expressionToRootData, Object writeBackData) {
        Stack<DataModelExpressionDto> resultDtoStack = chainRequest(new DataFlowTreeDto(), expressionToRootData);
        List<CommonResponseDto> lastRequestResponse;
        DataModelExpressionDto finalFetchDto = Objects.requireNonNull(resultDtoStack.pop());
        String writeBackPackageName = null;
        String writeBackEntityName = null;
        if (resultDtoStack.empty()) {
            // no remain of stack, means the stack size is 1 when the function is invoked
            // {package}:{entity}.{attr} condition
            // the size of the stack is one
            lastRequestResponse = Objects.requireNonNull(finalFetchDto.getJsonResponseStack(), "No returned json found by the request.").pop();
            writeBackPackageName = Objects.requireNonNull(finalFetchDto.getEntity().pkg(), "Cannot find package.").getText();
            writeBackEntityName = Objects.requireNonNull(finalFetchDto.getEntity().ety(), "Cannot find entity.").getText();
        } else {
            DataModelExpressionDto lastLinkDto = resultDtoStack.pop();
            Stack<List<CommonResponseDto>> requestResponseList = lastLinkDto.getJsonResponseStack();
            lastRequestResponse = requestResponseList.pop();
            if (null != lastLinkDto.getOpFetch()) {
                // refBy
                writeBackPackageName = Objects.requireNonNull(lastLinkDto.getBwdNode().entity().pkg(), "Cannot find package.").getText();
                writeBackEntityName = Objects.requireNonNull(lastLinkDto.getBwdNode().entity().ety(), "Cannot find entity.").getText();

            }

            if (null != lastLinkDto.getOpTo()) {
                // refTo
                writeBackPackageName = Objects.requireNonNull(lastLinkDto.getEntity().pkg(), "Cannot find package.").getText();
                writeBackEntityName = Objects.requireNonNull(lastLinkDto.getEntity().ety(), "Cannot find attribute.").getText();
            }
        }
        String writeBackAttr = Objects.requireNonNull(finalFetchDto.getOpFetch()).attr().getText();
        Object writeBackId = extractValueFromResponse(lastRequestResponse.get(0), this.UNIQUE_IDENTIFIER).get(0);
        Map<String, Object> postRequestUrlParamMap = generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), writeBackPackageName, writeBackEntityName);
        List<Map<String, Object>> writeBackRequestBodyParamMap = generatePostBodyParamMap(writeBackId, writeBackAttr, writeBackData);
        UrlToResponseDto urlToResponseDto = InitiatePostRequest(updateRequestUrl, postRequestUrlParamMap, writeBackRequestBodyParamMap);
        resultDtoStack.add(new DataModelExpressionDto(urlToResponseDto.getRequestUrl(), urlToResponseDto.getResponseDto()));
    }

    @Override
    public List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData) {
        DataFlowTreeDto dataFlowTreeDto = new DataFlowTreeDto();
        chainRequest(dataFlowTreeDto, expressionToRootData);
        return this.flattenTreeNode(dataFlowTreeDto.getTreeNode());
    }

    @Override
    public List<Map<String, Object>> createEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = InitiatePostRequest(createRequestUrl, postRequestUrlParamMap, request);
        return responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public List<Object> retrieveEntity(String packageName, String entityName) {
        Map<String, Object> getAllUrlParamMap = generateGetAllParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = InitiateGetRequest(requestAllUrl, getAllUrlParamMap);
        return extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelExpressionParser.FETCH_ALL);
    }

    @Override
    public List<Map<String, Object>> updateEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = InitiatePostRequest(updateRequestUrl, postRequestUrlParamMap, request);
        return responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public void deleteEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        InitiatePostRequest(deleteRequestUrl, postRequestUrlParamMap, request);
    }

    /**
     * Chain request operation from dataModelExpression and root Id data pair
     *
     * @param dataFlowTreeDto a support class comprises
     * @return request dto stack comprises returned value and intermediate responses, peek is the latest request
     */
    private Stack<DataModelExpressionDto> chainRequest(DataFlowTreeDto dataFlowTreeDto, DataModelExpressionToRootData dataModelExpressionToRootData) {
        String dataModelExpression = dataModelExpressionToRootData.getDataModelExpression();
        String rootIdData = dataModelExpressionToRootData.getRootData();
        logger.info(String.format("Setting up chain request process, the DME is [%s] and the root id data is [%s].", dataModelExpression, rootIdData));
        Stack<DataModelExpressionDto> resultDtoStack = new Stack<>();

        Queue<DataModelExpressionDto> expressionDtoQueue = new DataModelExpressionParser().parse(dataModelExpression);

        boolean isStart = true;
        DataModelExpressionDto lastExpressionDto = null;
        while (!expressionDtoQueue.isEmpty()) {
            DataModelExpressionDto expressionDto = expressionDtoQueue.poll();
            if (isStart) {
                resolveLink(dataFlowTreeDto, expressionDto, rootIdData);
                isStart = false;
            } else {
                resolveLink(dataFlowTreeDto, expressionDto, Objects.requireNonNull(lastExpressionDto));
            }
            if (!expressionDtoQueue.isEmpty()) {
                lastExpressionDto = expressionDto;
            }
            resultDtoStack.add(expressionDto);
        }
        return resultDtoStack;
    }


    /**
     * Resolve first link which comprises only fwdNode, bwdNode and one {package}:{entity}.{attribute} situation.
     *
     * @param expressionDto first link expression dto
     * @param rootIdData    root data id data
     */
    private void resolveLink(DataFlowTreeDto dataFlowTreeDto, DataModelExpressionDto expressionDto, String rootIdData) {
        logger.info(String.format("Resolving first link [%s] with root id data [%s]", expressionDto.getExpression(), rootIdData));

        DataModelParser.EntityContext entity = expressionDto.getEntity();
        UrlToResponseDto urlToResponseDto;
        String requestPackageName;
        String requestEntityName;
        Map<String, Object> requestParamMap;
        switch (expressionDto.getDataModelExpressionOpType()) {
            case ENTITY_FETCH:
                requestPackageName = entity.pkg().getText();
                requestEntityName = entity.ety().getText();

                // tree node
                dataFlowTreeDto.setTreeNode(new TreeNode(requestPackageName, requestEntityName, rootIdData, null, null));

                // request
                requestParamMap = generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        requestPackageName,
                        requestEntityName,
                        this.UNIQUE_IDENTIFIER,
                        rootIdData);

                urlToResponseDto = InitiateGetRequest(requestUrl, requestParamMap);
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

                String fetchAttributeName = expressionDto.getOpFetch().attr().getText();
                List<Object> finalResult = extractValueFromResponse(urlToResponseDto.getResponseDto(), fetchAttributeName);

                expressionDto.setResultValue(finalResult);
                break;
            case REF_TO:
                String firstRequestPackageName = expressionDto.getFwdNode().entity().pkg().getText();
                String firstRequestEntityName = expressionDto.getFwdNode().entity().ety().getText();

                // first tree node
                dataFlowTreeDto.setTreeNode(new TreeNode(firstRequestPackageName, firstRequestEntityName, rootIdData, null, new ArrayList<>()));

                // first request
                Map<String, Object> firstRequestParamMap = generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        firstRequestPackageName,
                        firstRequestEntityName,
                        this.UNIQUE_IDENTIFIER,
                        rootIdData);
                urlToResponseDto = InitiateGetRequest(requestUrl, firstRequestParamMap);
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

                // second request
                // fwdNode returned data is the second request's id data
                String secondRequestPackageName = entity.pkg().getText();
                String secondRequestEntityName = entity.ety().getText();
                String secondRequestAttrName = expressionDto.getFwdNode().attr().getText();
                List<Object> secondRequestIdDataList = extractValueFromResponse(urlToResponseDto.getResponseDto(), secondRequestAttrName);
                List<CommonResponseDto> responseDtoList = new ArrayList<>();
                Set<String> requestUrlSet = new LinkedHashSet<>();
                for (Object secondRequestIdData : secondRequestIdDataList) {
                    Map<String, Object> secondRequestParamMap = generateGetUrlParamMap(
                            this.applicationProperties.getGatewayUrl(),
                            secondRequestPackageName,
                            secondRequestEntityName,
                            this.UNIQUE_IDENTIFIER,
                            secondRequestIdData);
                    UrlToResponseDto secondRequestUrlToResponseDto = InitiateGetRequest(requestUrl, secondRequestParamMap);
                    requestUrlSet.add(secondRequestUrlToResponseDto.getRequestUrl());
                    responseDtoList.add(secondRequestUrlToResponseDto.getResponseDto());

                    // set child tree node and update parent tree node
                    TreeNode childNode = new TreeNode(secondRequestPackageName, secondRequestEntityName, secondRequestIdData, dataFlowTreeDto.getTreeNode(), new ArrayList<>());
                    dataFlowTreeDto.getTreeNode().getChildren().add(childNode);
                    dataFlowTreeDto.getAnchorTreeNodeList().add(childNode);
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);
                break;
            case REF_BY:
                DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
                requestPackageName = bwdNode.entity().pkg().getText();
                requestEntityName = bwdNode.entity().ety().getText();
                String requestAttributeName = bwdNode.attr().getText();

                // first TreeNode, which is the entity
                dataFlowTreeDto.setTreeNode(new TreeNode(entity.pkg().getText(), entity.ety().getText(), rootIdData, null, new ArrayList<>()));

                // refBy request
                requestParamMap = generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        requestPackageName,
                        requestEntityName,
                        requestAttributeName,
                        rootIdData);

                // the response may have data with one or multiple lines.
                urlToResponseDto = InitiateGetRequest(requestUrl, requestParamMap);

                // second TreeNode, might be multiple
                List<Object> refByDataIdList = extractValueFromResponse(urlToResponseDto.getResponseDto(), this.UNIQUE_IDENTIFIER);
                refByDataIdList.forEach(id -> {
                    TreeNode childNode = new TreeNode(requestPackageName, requestEntityName, id, dataFlowTreeDto.getTreeNode(), new ArrayList<>());
                    dataFlowTreeDto.getTreeNode().getChildren().add(childNode);
                    dataFlowTreeDto.getAnchorTreeNodeList().add(childNode);
                });
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));
                break;
            default:
                break;
        }
    }

    /**
     * Resolve links which comprise previous links and final fetch action
     *
     * @param expressionDto     subsequent link expression dto
     * @param lastExpressionDto the expression dto from last link
     */
    private void resolveLink(DataFlowTreeDto dataFlowTreeDto, DataModelExpressionDto expressionDto, DataModelExpressionDto lastExpressionDto) {
        logger.info(String.format("Entering resolving subsequent link process, the last expression is [%s], now resolving new subsequent link [%s].", lastExpressionDto.getExpression(), expressionDto.getExpression()));

        List<CommonResponseDto> lastRequestResultList = lastExpressionDto.getJsonResponseStack().peek();
        List<TreeNode> newAnchorTreeNodeList = new ArrayList<>();

        // last request info
        String lastRequestPackageName;
        String lastRequestEntityName;
        if (lastExpressionDto.getOpTo() != null) {
            // the last expression is refTo
            lastRequestPackageName = Objects.requireNonNull(lastExpressionDto.getEntity().pkg()).getText();
            lastRequestEntityName = Objects.requireNonNull(lastExpressionDto.getEntity().ety()).getText();
        } else {
            // the last expression is refBy
            lastRequestPackageName = Objects.requireNonNull(lastExpressionDto.getBwdNode().entity().pkg()).getText();
            lastRequestEntityName = Objects.requireNonNull(lastExpressionDto.getBwdNode().entity().ety()).getText();
        }


        String requestPackageName;
        String requestEntityName;
        List<CommonResponseDto> responseDtoList = new ArrayList<>();
        Set<String> requestUrlSet = new HashSet<>();
        switch (expressionDto.getDataModelExpressionOpType()) {
            case REF_TO:
                // new request info
                String requestId = expressionDto.getOpFetch().attr().getText();
                requestPackageName = expressionDto.getEntity().pkg().getText();
                requestEntityName = expressionDto.getEntity().ety().getText();
                for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {


                    // request for data and update the parent tree node
                    List<Object> requestIdDataList = extractValueFromResponse(lastRequestResponseDto, requestId);
                    for (Object requestIdData : requestIdDataList) {
                        // find parent tree node, from attribute to id might found multiple ID which means multiple tree nodes
                        List<Object> parentIdList = getResponseIdFromAttribute(lastRequestResponseDto, requestId, requestIdData);
                        List<TreeNode> parentTreeNodeList = new ArrayList<>();
                        Objects.requireNonNull(parentIdList).forEach(id -> {
                            TreeNode parentNode = findParentNode(dataFlowTreeDto.getAnchorTreeNodeList(), lastRequestPackageName, lastRequestEntityName, id);
                            Objects.requireNonNull(parentNode, "Cannot find parent node from given last request info");
                            parentTreeNodeList.add(parentNode);
                        });

                        Map<String, Object> requestParamMap = generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                this.UNIQUE_IDENTIFIER,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = InitiateGetRequest(requestUrl, requestParamMap);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

                        // set child tree node and update parent tree node
                        List<Object> responseIdList = extractValueFromResponse(urlToResponseDto.getResponseDto(), this.UNIQUE_IDENTIFIER);
                        responseIdList.forEach(id -> {
                            // the list's size is one due to it's referenceTo operation
                            parentTreeNodeList.forEach(parentNode -> {
                                // bind childNode which is generated by one id to multiple parents
                                TreeNode childNode = new TreeNode(requestPackageName, requestEntityName, id, parentNode, new ArrayList<>());
                                parentNode.getChildren().add(childNode);
                                newAnchorTreeNodeList.add(childNode);
                            });
                        });
                    }
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);
                break;
            case REF_BY:
                // new request info
                DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
                requestPackageName = bwdNode.entity().pkg().getText();
                requestEntityName = bwdNode.entity().ety().getText();
                String requestAttributeName = bwdNode.attr().getText();

                for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {

                    List<Object> requestIdDataList = extractValueFromResponse(lastRequestResponseDto, this.UNIQUE_IDENTIFIER);
                    for (Object requestIdData : requestIdDataList) {
                        Objects.requireNonNull(requestIdData,
                                "Cannot find 'id' from last request response. " +
                                        "Please ensure that the interface returned the data with one key named: 'id' as the development guideline requires.");
                        // find parent tree node
                        TreeNode parentNode = findParentNode(dataFlowTreeDto.getAnchorTreeNodeList(), lastRequestPackageName, lastRequestEntityName, requestIdData);
                        Objects.requireNonNull(parentNode, "Cannot find parent node from given last request info");

                        Map<String, Object> requestParamMap = generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                requestAttributeName,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = InitiateGetRequest(requestUrl, requestParamMap);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

                        // set child tree node and update parent tree node
                        List<Object> responseIdList = extractValueFromResponse(urlToResponseDto.getResponseDto(), this.UNIQUE_IDENTIFIER);
                        responseIdList.forEach(id -> {
                            TreeNode childNode = new TreeNode(requestPackageName, requestEntityName, id, parentNode, new ArrayList<>());
                            parentNode.getChildren().add(childNode);
                            newAnchorTreeNodeList.add(childNode);
                        });
                    }
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);
                break;
            case LINK_FETCH:
                // final route, which is prev link and fetch
                String attrName = expressionDto.getOpFetch().attr().getText();
                List<Object> resultValueList = new ArrayList<>();
                for (CommonResponseDto lastRequestResult : lastRequestResultList) {
                    List<Object> fetchDataList = extractValueFromResponse(lastRequestResult, attrName);
                    resultValueList.addAll(fetchDataList);
                }
                expressionDto.setResultValue(resultValueList);
                break;
            default:
                break;
        }

        // update anchor tree node list
        dataFlowTreeDto.setAnchorTreeNodeList(newAnchorTreeNodeList);

    }

    /**
     * Get response's id data from given attribute key and value
     *
     * @param lastRequestResponseDto last request's response dto
     * @param requestAttributeName   key of filter
     * @param requestAttributeValue  value of filter
     * @return found responseId list
     * @throws WecubeCoreException throws exception when there is an error converting response to LinkedHashMap
     */
    @SuppressWarnings("unchecked")
    private List<Object> getResponseIdFromAttribute(CommonResponseDto lastRequestResponseDto, String requestAttributeName, Object requestAttributeValue) throws WecubeCoreException {
        List<Object> result = new ArrayList<>();
        List<Object> requestResponseDataList = extractValueFromResponse(lastRequestResponseDto, DataModelExpressionParser.FETCH_ALL);
        requestResponseDataList.forEach(o -> {

            if (!(o instanceof LinkedHashMap<?, ?>)) {
                String msg = "Cannot transfer lastRequestResponse list to LinkedHashMap.";
                logger.error(msg, lastRequestResponseDto, requestAttributeName, requestAttributeValue);
                throw new WecubeCoreException(msg);
            }
            LinkedHashMap<String, Object> requestResponseDataMap = (LinkedHashMap<String, Object>) o;
            if (requestAttributeValue.equals(requestResponseDataMap.get(requestAttributeName))) {
                result.add(requestResponseDataMap.get(this.UNIQUE_IDENTIFIER));
            }
        });

        return result;
    }

    /**
     * Find parent tree node from last link's operation
     *
     * @param anchorTreeNodeList     intermediate tree node list as anchor, which is the latest tree's most bottom leaves
     * @param lastRequestPackageName last request package name
     * @param lastRequestEntityName  last request entity name
     * @param rootIdData             tree node's root id data
     * @return found tree node or null
     */
    private TreeNode findParentNode(List<TreeNode> anchorTreeNodeList, String lastRequestPackageName, String lastRequestEntityName, Object rootIdData) {
        for (TreeNode node : anchorTreeNodeList) {
            if (node.equals(new TreeNode(lastRequestPackageName, lastRequestEntityName, rootIdData))) return node;
        }
        return null;
    }

    /**
     * Generation of fetch data url param map
     *
     * @param gatewayUrl    gate way url
     * @param packageName   package name
     * @param entityName    entity name
     * @param attributeName attribute name
     * @param value         value
     * @return response map
     */
    private Map<String, Object> generateGetUrlParamMap(Object gatewayUrl,
                                                       Object packageName,
                                                       Object entityName,
                                                       Object attributeName,
                                                       Object value) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("attributeName", attributeName);
        paramMap.put("value", value);
        return paramMap;
    }

    /**
     * Generation of fetch all entity data url param map
     *
     * @param gatewayUrl  gate way url
     * @param packageName package name
     * @param entityName  entity name
     * @return response map
     */
    private Map<String, Object> generateGetAllParamMap(Object gatewayUrl,
                                                       Object packageName,
                                                       Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;
    }

    /**
     * Generation of data write back url param map
     *
     * @param gatewayUrl  gateway url
     * @param packageName package name
     * @param entityName  entity name
     * @return generated param map for url binding
     */
    private Map<String, Object> generatePostUrlParamMap(Object gatewayUrl,
                                                        Object packageName,
                                                        Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;

    }

    /**
     * Generation of data write back body param map
     *
     * @param entityId       gateway url
     * @param attributeName  package name
     * @param attributeValue entity name
     * @return generated param map for url binding
     */
    private List<Map<String, Object>> generatePostBodyParamMap(Object entityId,
                                                               String attributeName,
                                                               Object attributeValue) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(this.UNIQUE_IDENTIFIER, entityId);
        paramMap.put(attributeName, attributeValue);
        return Collections.singletonList(paramMap);

    }

    /**
     * Issue a request from request url with place holders and param map
     *
     * @param requestUrl request url with place holders
     * @param paramMap   generated param map
     * @return common response dto
     */
    private UrlToResponseDto InitiateGetRequest(String requestUrl, Map<String, Object> paramMap) {
        UrlToResponseDto responseDto;
        HttpHeaders httpHeaders = new HttpHeaders();
        // combine url with param map
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(paramMap);
        String uriStr = uriComponents.toString();
        responseDto = sendGetRequest(uriStr, httpHeaders);

        return responseDto;
    }

    /**
     * Issue a request from request url with place holders and param map
     *
     * @param requestUrl request url with place holders
     * @param paramMap   generated param map
     * @Param chainRequestDto chain request dto scope
     */
    private UrlToResponseDto InitiatePostRequest(String requestUrl, Map<String, Object> paramMap, List<Map<String, Object>> requestBodyParamMap) {
        UrlToResponseDto urlToResponseDto;
        HttpHeaders httpHeaders = new HttpHeaders();
        // combine url with param map
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(paramMap);
        String uriStr = uriComponents.toString();
        urlToResponseDto = sendPostRequest(uriStr, httpHeaders, requestBodyParamMap);
        return urlToResponseDto;
    }

    /**
     * Send request then transfer the response to common response dto
     *
     * @param uriStr      bind and expanded uri string
     * @param httpHeaders http header
     * @return common response dto
     */
    private UrlToResponseDto sendGetRequest(String uriStr, HttpHeaders httpHeaders) {
        logger.info(String.format("Sending GET request to target url: [%s]", uriStr));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendGetRequestWithParamMap(restTemplate, uriStr, httpHeaders);
        responseDto = checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Send request then transfer the response to common response dto
     *
     * @param uriStr      bind and expanded uri string
     * @param httpHeaders http header
     * @return common response dto
     */
    private UrlToResponseDto sendPostRequest(String uriStr, HttpHeaders httpHeaders, List<Map<String, Object>> postRequestBodyParamMap) {
        logger.info(String.format("Sending POST request to target url: [%s] with request body: [%s]", uriStr, postRequestBodyParamMap));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendPostRequestWithParamMap(restTemplate, uriStr, httpHeaders, postRequestBodyParamMap);
        responseDto = checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Check response from a http request
     *
     * @param response response from http request
     * @return transferred commonResponseDto from response
     * @throws WecubeCoreException while JsonUtils transferring response to CommonResponseDto class
     */
    private CommonResponseDto checkResponse(ResponseEntity<String> response) throws WecubeCoreException {
        CommonResponseDto responseDto;
        if (StringUtils.isEmpty(response.getBody()) || response.getStatusCode().isError()) {
            if (response.getStatusCode().is4xxClientError()) {
                throw new WecubeCoreException(String.format("The target server returned error code: [%s]. The target server doesn't implement the request controller.", response.getStatusCode().toString()));
            }

            if (response.getStatusCode().is5xxServerError()) {
                throw new WecubeCoreException(String.format("The target server returned error code: [%s], which is an target server's internal error.", response.getStatusCode().toString()));
            }
        }
        try {
            responseDto = JsonUtils.toObject(response.getBody(), CommonResponseDto.class);
        } catch (IOException e) {
            String msg = "Cannot transfer response from target server to CommonResponseDto class, the target server doesn't standardize the response style.";
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
            String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        return responseDto;
    }


    /**
     * Handle response and resolve it to list of objects
     *
     * @param responseDto   common response dto
     * @param attributeName the attribute name the expression want to fetch
     * @return list of value fetched from expression
     */
    private List<Object> extractValueFromResponse(CommonResponseDto responseDto, String attributeName) {
        // transfer dto to List<LinkedTreeMap>
        List<Object> returnList = new ArrayList<>();
        List<Map<String, Object>> dataArray = responseToMapList(responseDto);

        logger.info(String.format("Extract value from given http request's response [%s] by attribute name: [%s]", dataArray, attributeName));

        switch (attributeName) {
            case DataModelExpressionParser.FETCH_ALL: {
                returnList = Objects.requireNonNull(dataArray)
                        .stream()
                        .sorted(Comparator.comparing(o -> String.valueOf(o.get(this.UNIQUE_IDENTIFIER))))
                        .collect(Collectors.toList());
                break;
            }
            case DataModelExpressionParser.FETCH_NONE: {
                break;
            }
            default: {
                returnList = Objects.requireNonNull(dataArray)
                        .stream()
                        .sorted(Comparator.comparing(o -> String.valueOf(o.get(this.UNIQUE_IDENTIFIER))))
                        .map(linkedTreeMap -> linkedTreeMap.get(attributeName))
                        .collect(Collectors.toList());
                break;
            }
        }

        logger.info(String.format("The extraction from request's response by given attribute name [%s] is [%s]", attributeName, returnList));

        return returnList;
    }

    /**
     * Handle response and resolve it to list of objects
     *
     * @param responseDto common response dto
     * @return list of value fetched from expression
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> responseToMapList(CommonResponseDto responseDto) {
        List<Map<String, Object>> dataArray = new ArrayList<>();
        Object data = responseDto.getData();

        if (data instanceof ArrayList<?>) {
            dataArray = (List<Map<String, Object>>) data;
        } else if (data instanceof LinkedHashMap<?, ?>) {
            dataArray.add((Map<String, Object>) data);
        }

        return dataArray;
    }

    /**
     * Flatten a given tree
     *
     * @param treeNode root tree node of a tree
     * @return flattened tree node list
     */
    private List<TreeNode> flattenTreeNode(TreeNode treeNode) {
        List<TreeNode> result = new ArrayList<>();
        if (null != treeNode.getChildren() && !treeNode.getChildren().isEmpty()) {
            Objects.requireNonNull(treeNode.getChildren()).forEach(childNode -> result.addAll(flattenTreeNode(childNode)));
        }
        result.add(treeNode);
        return result;
    }
}
