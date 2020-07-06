package com.webank.wecube.platform.core.service.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.DmeFilterDto;
import com.webank.wecube.platform.core.dto.DmeLinkFilterDto;
import com.webank.wecube.platform.core.dto.Filter;
import com.webank.wecube.platform.core.service.dme.EntityDataDelegate;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityOperationContext;
import com.webank.wecube.platform.core.service.dme.EntityOperationType;
import com.webank.wecube.platform.core.service.dme.EntityQueryExecutor;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.EntityQueryFilter;
import com.webank.wecube.platform.core.service.dme.EntityQueryLinkNode;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;

/**
 * @author howechen
 */
@Service
public class RootlessExpressionServiceImpl implements RootlessExpressionService {

    private static final Logger log = LoggerFactory.getLogger(RootlessExpressionServiceImpl.class);

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    @Autowired
    @Qualifier("standardEntityQueryExecutor")
    private EntityQueryExecutor entityQueryExecutor;

    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;

    @Autowired
    private EntityDataRouteFactory entityDataRouteFactory;

    public List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto) {
        if(log.isInfoEnabled()){
            log.info("start to fetch data with filter:{}", dmeFilterDto);
        }
        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser
                .parse(dmeFilterDto.getDataModelExpression());

        enrichEntityQueryExprNodeInfos(exprNodeInfos, dmeFilterDto);

        return doFetchDataWithFilter(exprNodeInfos);

    }

    private List<Object> doFetchDataWithFilter(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
            return Collections.emptyList();
        }

        EntityOperationContext ctx = buildEntityOperationContext(exprNodeInfos);
        ctx.setEntityOperationType(EntityOperationType.QUERY);

        List<EntityDataDelegate> entityDataDelegates = entityQueryExecutor.executeQueryLeafEntity(ctx);

        EntityQueryLinkNode tailLinkNode = ctx.getTailEntityQueryLinkNode();

        return extractResultData(tailLinkNode, entityDataDelegates);
    }

    private List<Object> extractResultData(EntityQueryLinkNode tailLinkNode,
            List<EntityDataDelegate> entityDataDelegates) {
        List<Object> results = new ArrayList<Object>();
        if (StringUtils.isBlank(tailLinkNode.getQueryAttributeName())) {
            for (EntityDataDelegate delegate : entityDataDelegates) {
                Map<String, Object> record = new HashMap<String, Object>();
                record.putAll(delegate.getEntityData());
                results.add(record);
            }

            return results;
        } else {
            for (EntityDataDelegate delegate : entityDataDelegates) {
                Object val = delegate.getQueryAttrValue();
                results.add(val);
            }

            return results;
        }
    }

    protected EntityOperationContext buildEntityOperationContext(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        EntityOperationContext ctx = new EntityOperationContext();
        ctx.setEntityQueryExprNodeInfos(exprNodeInfos);
        ctx.setOriginalEntityLinkExpression("");
        ctx.setOriginalEntityData(null);
        ctx.setStandardEntityOperationRestClient(new StandardEntityOperationRestClient(jwtSsoRestTemplate));
        ctx.setHeadEntityQueryLinkNode(entityQueryExecutor.buildEntityQueryLinkNode(exprNodeInfos));
        ctx.setEntityDataRouteFactory(entityDataRouteFactory);

        return ctx;
    }

    private void enrichEntityQueryExprNodeInfos(List<EntityQueryExprNodeInfo> exprNodeInfos,
            DmeFilterDto dmeFilterDto) {
        List<DmeLinkFilterDto> filters = dmeFilterDto.getFilters();
        if (filters == null || filters.isEmpty()) {
            return;
        }

        for (DmeLinkFilterDto filterDto : filters) {
            int index = filterDto.getIndex();

            EntityQueryExprNodeInfo exprNodeInfo = null;
            if (index < exprNodeInfos.size()) {
                exprNodeInfo = exprNodeInfos.get(index);
            }

            if (exprNodeInfo == null) {
                throw new WecubeCoreException(
                        String.format("Index is not correct.Index:%s, PackageName:%s, EntityName:%s", index,
                                filterDto.getPackageName(), filterDto.getEntityName()));
            }
            
            List<Filter> attributeFilters = filterDto.getAttributeFilters();
            if(attributeFilters != null){
                for(Filter attributeFilter : attributeFilters){
                    EntityQueryFilter f = new EntityQueryFilter();
                    f.setAttrName(attributeFilter.getName());
                    f.setOp(EntityQueryFilter.OP_EQUALS);
                    f.setCondition(attributeFilter.getValue());
                    
                    exprNodeInfo.addAdditionalFilters(f);
                }
            }

        }
    }

    /**
     * @param dmeFilterDto
     *            consist with data model expression and list of filters of each
     *            link
     * @return request result
     */
    // @Override
    // public List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto) {
    // Stack<DataModelExpressionDto> dataModelExpressionDtos =
    // chainRequest(dmeFilterDto);
    // return dataModelExpressionDtos.pop().getResultValue();
    // }

    /**
     * Chain request operation from dataModelExpression and root Id data pair
     *
     * @param dmeFilterDto
     *            consist with data model expression and list of filters of each
     *            link
     * @return request dto stack comprises returned value and intermediate
     *         responses, peek is the latest request
     */
    // private Stack<DataModelExpressionDto> chainRequest(DmeFilterDto
    // dmeFilterDto) throws WecubeCoreException {
    // String dataModelExpression = dmeFilterDto.getDataModelExpression();
    // List<DmeLinkFilterDto> linkFilterDtoList = dmeFilterDto.getFilters();
    // logger.info(String.format("Setting up chain request process, the DME is
    // [%s].", dataModelExpression));
    // Stack<DataModelExpressionDto> resultDtoStack = new Stack<>();
    //
    // Queue<DataModelExpressionDto> expressionDtoQueue = new
    // DataModelExpressionParser().parse(dataModelExpression);
    // int expressionDtoQueueSize = expressionDtoQueue.size();
    // checkLinkFilter(dataModelExpression, linkFilterDtoList,
    // expressionDtoQueueSize);
    // Queue<DmeLinkFilterDto> linkFilterDtoQueue =
    // extendLinkFilter(linkFilterDtoList, expressionDtoQueueSize);
    //
    // boolean isStart = true;
    // DataModelExpressionDto lastExpressionDto = null;
    // while (!expressionDtoQueue.isEmpty()) {
    // DataModelExpressionDto expressionDto = expressionDtoQueue.poll();
    // if (isStart) {
    // if (expressionDtoQueueSize == 1) {
    // resolveLink(expressionDto, linkFilterDtoQueue.poll());
    // } else {
    // resolveLink(expressionDto, linkFilterDtoQueue.poll(),
    // linkFilterDtoQueue.poll());
    // }
    // isStart = false;
    // } else {
    // resolveLink(expressionDto, Objects.requireNonNull(lastExpressionDto),
    // linkFilterDtoQueue.poll());
    // }
    // if (!expressionDtoQueue.isEmpty()) {
    // lastExpressionDto = expressionDto;
    // }
    // resultDtoStack.add(expressionDto);
    // }
    // return resultDtoStack;
    // }

    /**
     * Resolve first link which comprises only fwdNode, bwdNode and one
     * {package}:{entity}.{attribute} situation.
     *
     * @param expressionDto
     *            first link expression dto
     */
    // private void resolveLink(DataModelExpressionDto expressionDto,
    // DmeLinkFilterDto... linkFilterDtoArray) {
    // log.info(String.format("Resolving first link [%s].",
    // expressionDto.getExpression()));
    //
    // DataModelParser.EntityContext entity = expressionDto.getEntity();
    // UrlToResponseDto urlToResponseDto;
    // String requestPackageName;
    // String requestEntityName;
    // Map<String, Object> requestParamMap;
    // DmeLinkFilterDto firstFilter;
    // DmeLinkFilterDto secondFilter;
    // List<CommonResponseDto> responseDtoList;
    // Set<String> requestUrlSet;
    //
    // switch (expressionDto.getDataModelExpressionOpType()) {
    // case ENTITY_FETCH:
    // firstFilter = linkFilterDtoArray[0];
    //
    // requestPackageName = entity.pkg().getText();
    // requestEntityName = entity.ety().getText();
    //
    // // rootless request
    // urlToResponseDto = getAllDataFromFirstEntity(expressionDto,
    // requestPackageName, requestEntityName);
    // filterJsonResponse(urlToResponseDto, firstFilter, requestPackageName,
    // requestEntityName);
    //
    // String fetchAttributeName = expressionDto.getOpFetch().attr().getText();
    // List<Object> finalResult =
    // dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(),
    // fetchAttributeName);
    //
    // expressionDto.setResultValue(finalResult);
    // break;
    // case REF_TO:
    // firstFilter = linkFilterDtoArray[0];
    // secondFilter = linkFilterDtoArray[1];
    //
    // String firstRequestPackageName =
    // expressionDto.getFwdNode().entity().pkg().getText();
    // String firstRequestEntityName =
    // expressionDto.getFwdNode().entity().ety().getText();
    //
    // // rootless first request
    // urlToResponseDto = getAllDataFromFirstEntity(expressionDto,
    // firstRequestPackageName,
    // firstRequestEntityName);
    // filterJsonResponse(urlToResponseDto, firstFilter,
    // firstRequestPackageName, firstRequestEntityName);
    //
    // // second request
    // // fwdNode returned data is the second request's id data
    // String secondRequestPackageName = entity.pkg().getText();
    // String secondRequestEntityName = entity.ety().getText();
    // String secondRequestAttrName =
    // expressionDto.getFwdNode().attr().getText();
    // List<Object> secondRequestIdDataList = dataModelServiceStub
    // .extractValueFromResponse(urlToResponseDto.getResponseDto(),
    // secondRequestAttrName);
    // responseDtoList = new ArrayList<>();
    // requestUrlSet = new LinkedHashSet<>();
    // for (Object secondRequestIdData : secondRequestIdDataList) {
    // Map<String, Object> secondRequestParamMap =
    // dataModelServiceStub.generateGetUrlParamMap(
    // this.applicationProperties.getGatewayUrl(), secondRequestPackageName,
    // secondRequestEntityName,
    // UNIQUE_IDENTIFIER, secondRequestIdData);
    // UrlToResponseDto secondRequestUrlToResponseDto = dataModelServiceStub
    // .initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL,
    // secondRequestParamMap);
    // filterJsonResponse(secondRequestUrlToResponseDto, secondFilter,
    // secondRequestPackageName,
    // secondRequestEntityName);
    // requestUrlSet.add(secondRequestUrlToResponseDto.getRequestUrl());
    // responseDtoList.add(secondRequestUrlToResponseDto.getResponseDto());
    // }
    // expressionDto.getRequestUrlStack().add(requestUrlSet);
    // expressionDto.getJsonResponseStack().add(responseDtoList);
    //
    // break;
    // case REF_BY:
    // firstFilter = linkFilterDtoArray[0];
    // secondFilter = linkFilterDtoArray[1];
    //
    // requestPackageName = entity.pkg().getText();
    // requestEntityName = entity.ety().getText();
    //
    // // rootless first request
    // urlToResponseDto = getAllDataFromFirstEntity(expressionDto,
    // requestPackageName, requestEntityName);
    // filterJsonResponse(urlToResponseDto, firstFilter, requestPackageName,
    // requestEntityName);
    //
    // List<Object> firstRequestIdDataList = dataModelServiceStub
    // .extractValueFromResponse(urlToResponseDto.getResponseDto(),
    // UNIQUE_IDENTIFIER);
    //
    // // refBy second request
    // DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
    // requestPackageName = bwdNode.entity().pkg().getText();
    // requestEntityName = bwdNode.entity().ety().getText();
    // String requestAttributeName = bwdNode.attr().getText();
    //
    // responseDtoList = new ArrayList<>();
    // requestUrlSet = new LinkedHashSet<>();
    // for (Object rootIdData : firstRequestIdDataList) {
    // requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
    // this.applicationProperties.getGatewayUrl(), requestPackageName,
    // requestEntityName,
    // requestAttributeName, rootIdData);
    //
    // // the response may have data with one or multiple lines.
    // urlToResponseDto =
    // dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL,
    // requestParamMap);
    // filterJsonResponse(urlToResponseDto, secondFilter, requestPackageName,
    // requestEntityName);
    // requestUrlSet.add(urlToResponseDto.getRequestUrl());
    // responseDtoList.add(urlToResponseDto.getResponseDto());
    // }
    // expressionDto.getRequestUrlStack().add(requestUrlSet);
    // expressionDto.getJsonResponseStack().add(responseDtoList);
    // break;
    // default:
    // break;
    // }
    // }

    /**
     * Resolve links which comprise previous links and final fetch action
     *
     * @param expressionDto
     *            subsequent link expression dto
     * @param lastExpressionDto
     *            the expression dto from last link
     */
    // private void resolveLink(DataModelExpressionDto expressionDto,
    // DataModelExpressionDto lastExpressionDto,
    // DmeLinkFilterDto linkFilterDto) {
    // log.info(String.format(
    // "Entering resolving subsequent link process, the last expression is [%s],
    // now resolving new subsequent link [%s].",
    // lastExpressionDto.getExpression(), expressionDto.getExpression()));
    //
    // List<CommonResponseDto> lastRequestResultList =
    // lastExpressionDto.getJsonResponseStack().peek();
    //
    // String requestPackageName;
    // String requestEntityName;
    // List<CommonResponseDto> responseDtoList = new ArrayList<>();
    // Set<String> requestUrlSet = new HashSet<>();
    // switch (expressionDto.getDataModelExpressionOpType()) {
    // case REF_TO:
    // // new request info
    // String requestId = expressionDto.getOpFetch().attr().getText();
    // requestPackageName = expressionDto.getEntity().pkg().getText();
    // requestEntityName = expressionDto.getEntity().ety().getText();
    // for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {
    // List<Object> requestIdDataList =
    // dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto,
    // requestId);
    // for (Object requestIdData : requestIdDataList) {
    //
    // Map<String, Object> requestParamMap =
    // dataModelServiceStub.generateGetUrlParamMap(
    // this.applicationProperties.getGatewayUrl(), requestPackageName,
    // requestEntityName,
    // UNIQUE_IDENTIFIER, requestIdData);
    // UrlToResponseDto urlToResponseDto = dataModelServiceStub
    // .initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL,
    // requestParamMap);
    // filterJsonResponse(urlToResponseDto, linkFilterDto, requestPackageName,
    // requestEntityName);
    // requestUrlSet.add(urlToResponseDto.getRequestUrl());
    // responseDtoList.add(urlToResponseDto.getResponseDto());
    //
    // }
    // }
    // expressionDto.getRequestUrlStack().add(requestUrlSet);
    // expressionDto.getJsonResponseStack().add(responseDtoList);
    // break;
    // case REF_BY:
    // // new request info
    // DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
    // requestPackageName = bwdNode.entity().pkg().getText();
    // requestEntityName = bwdNode.entity().ety().getText();
    // String requestAttributeName = bwdNode.attr().getText();
    //
    // for (CommonResponseDto lastRequestResponseDto : lastRequestResultList) {
    //
    // List<Object> requestIdDataList =
    // dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto,
    // UNIQUE_IDENTIFIER);
    // for (Object requestIdData : requestIdDataList) {
    // Objects.requireNonNull(requestIdData, "Cannot find 'id' from last request
    // response. "
    // + "Please ensure that the interface returned the data with one key named:
    // 'id' as the development guideline requires.");
    //
    // Map<String, Object> requestParamMap =
    // dataModelServiceStub.generateGetUrlParamMap(
    // this.applicationProperties.getGatewayUrl(), requestPackageName,
    // requestEntityName,
    // requestAttributeName, requestIdData);
    // UrlToResponseDto urlToResponseDto = dataModelServiceStub
    // .initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL,
    // requestParamMap);
    // filterJsonResponse(urlToResponseDto, linkFilterDto, requestPackageName,
    // requestEntityName);
    // requestUrlSet.add(urlToResponseDto.getRequestUrl());
    // responseDtoList.add(urlToResponseDto.getResponseDto());
    //
    // }
    // }
    // expressionDto.getRequestUrlStack().add(requestUrlSet);
    // expressionDto.getJsonResponseStack().add(responseDtoList);
    // break;
    // case LINK_FETCH:
    // // final route, which is prev link and fetch
    // String attrName = expressionDto.getOpFetch().attr().getText();
    // List<Object> resultValueList = new ArrayList<>();
    // for (CommonResponseDto lastRequestResult : lastRequestResultList) {
    // List<Object> fetchDataList =
    // dataModelServiceStub.extractValueFromResponse(lastRequestResult,
    // attrName);
    // resultValueList.addAll(fetchDataList);
    // }
    // expressionDto.setResultValue(resultValueList);
    // break;
    // default:
    // break;
    // }
    //
    // }

    // private UrlToResponseDto getAllDataFromFirstEntity(DataModelExpressionDto
    // expressionDto, String requestPackageName,
    // String requestEntityName) {
    // Map<String, Object> requestParamMap;
    // UrlToResponseDto urlToResponseDto;// request
    // requestParamMap =
    // dataModelServiceStub.generateGetAllParamMap(this.applicationProperties.getGatewayUrl(),
    // requestPackageName, requestEntityName);
    //
    // urlToResponseDto =
    // dataModelServiceStub.initiateGetRequest(DataModelServiceStub.RETRIEVE_REQUEST_URL,
    // requestParamMap);
    //
    // expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
    // expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));
    //
    // return urlToResponseDto;
    // }

    // private void filterJsonResponse(UrlToResponseDto urlToResponseDto,
    // DmeLinkFilterDto linkFilter,
    // String dmePackageName, String dmeEntityName) throws WecubeCoreException {
    // if (null == linkFilter) {
    // return;
    // }
    //
    // if (null != linkFilter.getPackageName() && null !=
    // linkFilter.getEntityName()) {
    // if (!dmePackageName.equals(linkFilter.getPackageName())
    // || !dmeEntityName.equals(linkFilter.getEntityName())) {
    // String msg = String.format(
    // "The given filter's package name: [%s] and entity name: [%s] don't match
    // to the name in DME: [%s]:[%s].",
    // linkFilter.getPackageName(), linkFilter.getEntityName(), dmePackageName,
    // dmeEntityName);
    // log.error(msg);
    // throw new WecubeCoreException(msg);
    // }
    // }
    //
    // List<Map<String, Object>> data = this.dataModelServiceStub
    // .filterData(urlToResponseDto.getResponseDto().getData(),
    // linkFilter.getAttributeFilters());
    // urlToResponseDto.getResponseDto().setData(data);
    // }

    // private void checkLinkFilter(String dataModelExpression,
    // List<DmeLinkFilterDto> linkFilterDtoList,
    // int expressionDtoQueueSize) throws WecubeCoreException {
    // if (linkFilterDtoList.size() > expressionDtoQueueSize) {
    // String msg = String.format("The filters' length exceeds the length of the
    // entities parsed from DME: [%s]",
    // dataModelExpression);
    // if (log.isDebugEnabled()) {
    // log.error(msg);
    // }
    // throw new WecubeCoreException(msg);
    // }
    //
    // linkFilterDtoList.forEach(dmeLinkFilterDto -> {
    // if (dmeLinkFilterDto.getIndex() >= expressionDtoQueueSize) {
    // String msg = String.format(
    // "The filters' index exceeds the length of the entities parsed from DME:
    // [%s]",
    // dataModelExpression);
    // if (log.isDebugEnabled()) {
    // log.error(msg);
    // }
    // throw new WecubeCoreException(msg);
    // }
    // });
    // }
    //
    // private Queue<DmeLinkFilterDto> extendLinkFilter(List<DmeLinkFilterDto>
    // linkFilterDtoList, Integer entityIndexSize)
    // throws WecubeCoreException {
    // List<DmeLinkFilterDto> result = Arrays.asList(new
    // DmeLinkFilterDto[entityIndexSize]);
    //
    // linkFilterDtoList.forEach(linkFilterDto -> {
    // int index = linkFilterDto.getIndex();
    // if (result.get(index) != null) {
    // String msg = String.format(
    // "The current index: [%d] already has an filter, which cannot be
    // overwritten by another filter.",
    // index);
    // if (log.isDebugEnabled()) {
    // log.error(msg, linkFilterDto.toString());
    // }
    // throw new WecubeCoreException(msg);
    // }
    // result.set(linkFilterDto.getIndex(), linkFilterDto);
    // });
    // return new LinkedList<>(result);
    // }
}
