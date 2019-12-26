package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DmeFilterDto;
import com.webank.wecube.platform.core.dto.DmeLinkFilterDto;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelParser;
import com.webank.wecube.platform.core.support.datamodel.DataModelServiceStub;
import com.webank.wecube.platform.core.support.datamodel.dto.DataModelExpressionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.webank.wecube.platform.core.support.datamodel.DataModelServiceStub.UNIQUE_IDENTIFIER;

/**
 * @author howechen
 */
@Service
public class RootlessExpressionServiceImpl implements RootlessExpressionService {

    private static final Logger logger = LoggerFactory.getLogger(RootlessExpressionServiceImpl.class);
    private ApplicationProperties applicationProperties;
    private DataModelServiceStub dataModelServiceStub;

    @Autowired
    public RootlessExpressionServiceImpl(ApplicationProperties applicationProperties, DataModelServiceStub dataModelServiceStub) {
        this.applicationProperties = applicationProperties;
        this.dataModelServiceStub = dataModelServiceStub;
    }

    /**
     * @param dmeFilterDto consist with data model expression and list of filters of each link
     * @return request result
     */
    @Override
    public List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto) {
        Stack<DataModelExpressionDto> dataModelExpressionDtos = chainRequest(dmeFilterDto);
        return dataModelExpressionDtos.pop().getResultValue();
    }

    /**
     * Chain request operation from dataModelExpression and root Id data pair
     *
     * @param dmeFilterDto consist with data model expression and list of filters of each link
     * @return request dto stack comprises returned value and intermediate responses, peek is the latest request
     */
    private Stack<DataModelExpressionDto> chainRequest(DmeFilterDto dmeFilterDto) throws WecubeCoreException {
        String dataModelExpression = dmeFilterDto.getDataModelExpression();
        List<DmeLinkFilterDto> linkFilterDtoList = dmeFilterDto.getFilters();
        logger.info(String.format("Setting up chain request process, the DME is [%s].", dataModelExpression));
        Stack<DataModelExpressionDto> resultDtoStack = new Stack<>();

        Queue<DataModelExpressionDto> expressionDtoQueue = new DataModelExpressionParser().parse(dataModelExpression);
        int expressionDtoQueueSize = expressionDtoQueue.size();
        checkLinkFilter(dataModelExpression, linkFilterDtoList, expressionDtoQueueSize);
        Queue<DmeLinkFilterDto> linkFilterDtoQueue = extendLinkFilter(linkFilterDtoList, expressionDtoQueueSize);

        boolean isStart = true;
        DataModelExpressionDto lastExpressionDto = null;
        while (!expressionDtoQueue.isEmpty()) {
            DataModelExpressionDto expressionDto = expressionDtoQueue.poll();
            if (isStart) {
                if (expressionDtoQueueSize == 1) {
                    resolveLink(expressionDto, linkFilterDtoQueue.poll());
                } else {
                    resolveLink(expressionDto, linkFilterDtoQueue.poll(), linkFilterDtoQueue.poll());
                }
                isStart = false;
            } else {
                resolveLink(expressionDto, Objects.requireNonNull(lastExpressionDto), linkFilterDtoQueue.poll());
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
     */
    private void resolveLink(DataModelExpressionDto expressionDto, DmeLinkFilterDto... linkFilterDtoArray) {
        logger.info(String.format("Resolving first link [%s].", expressionDto.getExpression()));


        DataModelParser.EntityContext entity = expressionDto.getEntity();
        UrlToResponseDto urlToResponseDto;
        String requestPackageName;
        String requestEntityName;
        Map<String, Object> requestParamMap;
        DmeLinkFilterDto firstFilter;
        DmeLinkFilterDto secondFilter;
        List<CommonResponseDto> responseDtoList;
        Set<String> requestUrlSet;

        switch (expressionDto.getDataModelExpressionOpType()) {
            case ENTITY_FETCH:
                firstFilter = linkFilterDtoArray[0];

                requestPackageName = entity.pkg().getText();
                requestEntityName = entity.ety().getText();

                // rootless request
                urlToResponseDto = getAllDataFromFirstEntity(expressionDto, requestPackageName, requestEntityName);
                filterJsonResponse(urlToResponseDto, firstFilter, requestPackageName, requestEntityName);

                String fetchAttributeName = expressionDto.getOpFetch().attr().getText();
                List<Object> finalResult = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), fetchAttributeName);

                expressionDto.setResultValue(finalResult);
                break;
            case REF_TO:
                firstFilter = linkFilterDtoArray[0];
                secondFilter = linkFilterDtoArray[1];

                String firstRequestPackageName = expressionDto.getFwdNode().entity().pkg().getText();
                String firstRequestEntityName = expressionDto.getFwdNode().entity().ety().getText();


                //  rootless first request
                urlToResponseDto = getAllDataFromFirstEntity(expressionDto, firstRequestPackageName, firstRequestEntityName);
                filterJsonResponse(urlToResponseDto, firstFilter, firstRequestPackageName, firstRequestEntityName);

                // second request
                // fwdNode returned data is the second request's id data
                String secondRequestPackageName = entity.pkg().getText();
                String secondRequestEntityName = entity.ety().getText();
                String secondRequestAttrName = expressionDto.getFwdNode().attr().getText();
                List<Object> secondRequestIdDataList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), secondRequestAttrName);
                responseDtoList = new ArrayList<>();
                requestUrlSet = new LinkedHashSet<>();
                for (Object secondRequestIdData : secondRequestIdDataList) {
                    Map<String, Object> secondRequestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                            this.applicationProperties.getGatewayUrl(),
                            secondRequestPackageName,
                            secondRequestEntityName,
                            UNIQUE_IDENTIFIER,
                            secondRequestIdData);
                    UrlToResponseDto secondRequestUrlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, secondRequestParamMap);
                    filterJsonResponse(secondRequestUrlToResponseDto, secondFilter, secondRequestPackageName, secondRequestEntityName);
                    requestUrlSet.add(secondRequestUrlToResponseDto.getRequestUrl());
                    responseDtoList.add(secondRequestUrlToResponseDto.getResponseDto());
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);

                break;
            case REF_BY:
                firstFilter = linkFilterDtoArray[0];
                secondFilter = linkFilterDtoArray[1];

                requestPackageName = entity.pkg().getText();
                requestEntityName = entity.ety().getText();

                //  rootless first request
                urlToResponseDto = getAllDataFromFirstEntity(expressionDto, requestPackageName, requestEntityName);
                filterJsonResponse(urlToResponseDto, firstFilter, requestPackageName, requestEntityName);

                List<Object> firstRequestIdDataList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), UNIQUE_IDENTIFIER);

                // refBy second request
                DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
                requestPackageName = bwdNode.entity().pkg().getText();
                requestEntityName = bwdNode.entity().ety().getText();
                String requestAttributeName = bwdNode.attr().getText();

                responseDtoList = new ArrayList<>();
                requestUrlSet = new LinkedHashSet<>();
                for (Object rootIdData : firstRequestIdDataList) {
                    requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                            this.applicationProperties.getGatewayUrl(),
                            requestPackageName,
                            requestEntityName,
                            requestAttributeName,
                            rootIdData);

                    // the response may have data with one or multiple lines.
                    urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                    filterJsonResponse(urlToResponseDto, secondFilter, requestPackageName, requestEntityName);
                    requestUrlSet.add(urlToResponseDto.getRequestUrl());
                    responseDtoList.add(urlToResponseDto.getResponseDto());
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);
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
    private void resolveLink(DataModelExpressionDto expressionDto, DataModelExpressionDto lastExpressionDto, DmeLinkFilterDto linkFilterDto) {
        logger.info(String.format("Entering resolving subsequent link process, the last expression is [%s], now resolving new subsequent link [%s].", lastExpressionDto.getExpression(), expressionDto.getExpression()));

        List<CommonResponseDto> lastRequestResultList = lastExpressionDto.getJsonResponseStack().peek();


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
                    List<Object> requestIdDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto, requestId);
                    for (Object requestIdData : requestIdDataList) {

                        Map<String, Object> requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                UNIQUE_IDENTIFIER,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                        filterJsonResponse(urlToResponseDto, linkFilterDto, requestPackageName, requestEntityName);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

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

                    List<Object> requestIdDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto, UNIQUE_IDENTIFIER);
                    for (Object requestIdData : requestIdDataList) {
                        Objects.requireNonNull(requestIdData,
                                "Cannot find 'id' from last request response. " +
                                        "Please ensure that the interface returned the data with one key named: 'id' as the development guideline requires.");

                        Map<String, Object> requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                requestAttributeName,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                        filterJsonResponse(urlToResponseDto, linkFilterDto, requestPackageName, requestEntityName);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

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
                    List<Object> fetchDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResult, attrName);
                    resultValueList.addAll(fetchDataList);
                }
                expressionDto.setResultValue(resultValueList);
                break;
            default:
                break;
        }

    }

    private UrlToResponseDto getAllDataFromFirstEntity(DataModelExpressionDto expressionDto, String requestPackageName, String requestEntityName) {
        Map<String, Object> requestParamMap;
        UrlToResponseDto urlToResponseDto;// request
        requestParamMap = dataModelServiceStub.generateGetAllParamMap(
                this.applicationProperties.getGatewayUrl(),
                requestPackageName,
                requestEntityName);

        urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.RETRIEVE_REQUEST_URL, requestParamMap);

        expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
        expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

        return urlToResponseDto;
    }

    private void filterJsonResponse(UrlToResponseDto urlToResponseDto,
                                    DmeLinkFilterDto linkFilter,
                                    String dmePackageName,
                                    String dmeEntityName) throws WecubeCoreException {
        if (null == linkFilter) {
            return;
        }

        if (null != linkFilter.getPackageName() && null != linkFilter.getEntityName()) {
            if (!dmePackageName.equals(linkFilter.getPackageName()) || !dmeEntityName.equals(linkFilter.getEntityName())) {
                String msg = String.format("The given filter's package name: [%s] and entity name: [%s] don't match to the name in DME: [%s]:[%s].",
                        linkFilter.getPackageName(),
                        linkFilter.getEntityName(),
                        dmePackageName,
                        dmeEntityName);
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
        }


        List<Map<String, Object>> data = this.dataModelServiceStub.filterData(urlToResponseDto.getResponseDto().getData(), linkFilter.getAttributeFilters());
        urlToResponseDto.getResponseDto().setData(data);
    }

    private void checkLinkFilter(String dataModelExpression, List<DmeLinkFilterDto> linkFilterDtoList, int expressionDtoQueueSize) throws WecubeCoreException {
        if (linkFilterDtoList.size() > expressionDtoQueueSize) {
            String msg = String.format("The filters' length exceeds the length of the entities parsed from DME: [%s]", dataModelExpression);
            if (logger.isDebugEnabled()) {
                logger.error(msg);
            }
            throw new WecubeCoreException(msg);
        }

        linkFilterDtoList.forEach(dmeLinkFilterDto -> {
            if (dmeLinkFilterDto.getIndex() >= expressionDtoQueueSize) {
                String msg = String.format("The filters' index exceeds the length of the entities parsed from DME: [%s]", dataModelExpression);
                if (logger.isDebugEnabled()) {
                    logger.error(msg);
                }
                throw new WecubeCoreException(msg);
            }
        });
    }

    private Queue<DmeLinkFilterDto> extendLinkFilter(List<DmeLinkFilterDto> linkFilterDtoList, Integer entityIndexSize) throws WecubeCoreException {
        List<DmeLinkFilterDto> result = Arrays.asList(new DmeLinkFilterDto[entityIndexSize]);

        linkFilterDtoList.forEach(linkFilterDto -> {
            int index = linkFilterDto.getIndex();
            if (result.get(index) != null) {
                String msg = String.format("The current index: [%d] already has an filter, which cannot be overwritten by another filter.", index);
                if (logger.isDebugEnabled()) {
                    logger.error(msg, linkFilterDto.toString());
                }
                throw new WecubeCoreException(msg);
            }
            result.set(linkFilterDto.getIndex(), linkFilterDto);
        });
        return new LinkedList<>(result);
    }
}
