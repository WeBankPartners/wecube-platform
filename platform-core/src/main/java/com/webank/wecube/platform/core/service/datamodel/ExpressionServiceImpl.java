package com.webank.wecube.platform.core.service.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.EntityDto;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelParser;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.support.datamodel.DataModelServiceStub;
import com.webank.wecube.platform.core.support.datamodel.dto.DataFlowTreeDto;
import com.webank.wecube.platform.core.support.datamodel.dto.DataModelExpressionDto;
import com.webank.wecube.platform.core.support.datamodel.dto.TreeNode;
import com.webank.wecube.platform.core.support.datamodel.dto.WriteBackTargetDto;
import com.webank.wecube.platform.core.utils.CollectionUtils;

@Service
public class ExpressionServiceImpl implements ExpressionService {
    private static final Logger log = LoggerFactory.getLogger(ExpressionServiceImpl.class);
    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    private PluginPackageDataModelRepository pluginPackageDataModelRepository;

    @Autowired
    private ApplicationProperties applicationProperties;
    
    @Autowired
    private DataModelServiceStub dataModelServiceStub;

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;



//    @Override
//    public List<Object> fetchData(DataModelExpressionToRootData dataModelExpressionToRootData) {
//
//        Stack<DataModelExpressionDto> resultDtoStack = chainRequest(new DataFlowTreeDto(), dataModelExpressionToRootData);
//
//        return resultDtoStack.pop().getResultValue();
//    }


//    @Override
//    public void writeBackData(DataModelExpressionToRootData expressionToRootData, Object writeBackData) {
//        Stack<DataModelExpressionDto> resultDtoStack = chainRequest(new DataFlowTreeDto(), expressionToRootData);
//        WriteBackTargetDto writeBackTargetDto = findWriteBackTarget(resultDtoStack);
//        Object writeBackId = dataModelServiceStub.extractValueFromResponse(writeBackTargetDto.getLastRequestResponse().get(0), DataModelServiceStub.UNIQUE_IDENTIFIER).get(0);
//        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub.generatePostUrlParamMap(
//                this.applicationProperties.getGatewayUrl(),
//                writeBackTargetDto.getWriteBackPackageName(),
//                writeBackTargetDto.getWriteBackEntityName()
//        );
//        List<Map<String, Object>> writeBackRequestBodyParamMap = dataModelServiceStub.generatePostBodyParamMap(writeBackId, writeBackTargetDto.getWriteBackAttributeName(), writeBackData);
//        dataModelServiceStub.initiatePostRequest(DataModelServiceStub.UPDATE_REQUEST_URL, postRequestUrlParamMap, writeBackRequestBodyParamMap);
//    }

//    @Override
//    public List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData) {
//        DataFlowTreeDto dataFlowTreeDto = new DataFlowTreeDto();
//        chainRequest(dataFlowTreeDto, expressionToRootData);
//        return this.flattenTreeNode(dataFlowTreeDto.getTreeNode());
//    }

    /**
     * Chain request operation from dataModelExpression and root Id data pair
     *
     * @param dataFlowTreeDto a support class comprises
     * @return request dto stack comprises returned value and intermediate responses, peek is the latest request
     */
    private Stack<DataModelExpressionDto> chainRequest(DataFlowTreeDto dataFlowTreeDto, DataModelExpressionToRootData dataModelExpressionToRootData) {
        String dataModelExpression = dataModelExpressionToRootData.getDataModelExpression();
        String rootIdData = dataModelExpressionToRootData.getRootData();
        log.info(String.format("Setting up chain request process, the DME is [%s] and the root id data is [%s].", dataModelExpression, rootIdData));
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
        log.info(String.format("Resolving first link [%s] with root id data [%s]", expressionDto.getExpression(), rootIdData));

        DataModelParser.EntityContext entity = expressionDto.getEntity();
        UrlToResponseDto urlToResponseDto;
        String requestPackageName;
        String requestEntityName;
        Map<String, Object> requestParamMap;
        List<Object> extractedVisualField;
        switch (expressionDto.getDataModelExpressionOpType()) {
            case ENTITY_FETCH:
                requestPackageName = entity.pkg().getText();
                requestEntityName = entity.ety().getText();


                // request
                requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        requestPackageName,
                        requestEntityName,
                        DataModelServiceStub.UNIQUE_IDENTIFIER,
                        rootIdData);

                urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

                // tree node
                // when given root id data, the found data size should always be one
                extractedVisualField = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                dataFlowTreeDto.setTreeNode(new TreeNode(requestPackageName, requestEntityName, rootIdData, extractedVisualField.get(0), null, null));

                String fetchAttributeName = expressionDto.getOpFetch().attr().getText();
                List<Object> finalResult = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), fetchAttributeName);

                expressionDto.setResultValue(finalResult);
                break;
            case REF_TO:
                String firstRequestPackageName = expressionDto.getFwdNode().entity().pkg().getText();
                String firstRequestEntityName = expressionDto.getFwdNode().entity().ety().getText();

                // first request
                Map<String, Object> firstRequestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        firstRequestPackageName,
                        firstRequestEntityName,
                        DataModelServiceStub.UNIQUE_IDENTIFIER,
                        rootIdData);
                urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, firstRequestParamMap);
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

                // first tree node
                // when given root id data, the found data size should always be one
                extractedVisualField = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                dataFlowTreeDto.setTreeNode(new TreeNode(firstRequestPackageName, firstRequestEntityName, rootIdData, extractedVisualField.get(0), null, new ArrayList<>()));

                // second request
                // fwdNode returned data is the second request's id data
                String secondRequestPackageName = entity.pkg().getText();
                String secondRequestEntityName = entity.ety().getText();
                String secondRequestAttrName = expressionDto.getFwdNode().attr().getText();
                List<Object> secondRequestIdDataList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), secondRequestAttrName);
                List<CommonResponseDto> responseDtoList = new ArrayList<>();
                Set<String> requestUrlSet = new LinkedHashSet<>();
                for (Object secondRequestIdData : secondRequestIdDataList) {
                    Map<String, Object> secondRequestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                            this.applicationProperties.getGatewayUrl(),
                            secondRequestPackageName,
                            secondRequestEntityName,
                            DataModelServiceStub.UNIQUE_IDENTIFIER,
                            secondRequestIdData);
                    UrlToResponseDto secondRequestUrlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, secondRequestParamMap);
                    requestUrlSet.add(secondRequestUrlToResponseDto.getRequestUrl());
                    responseDtoList.add(secondRequestUrlToResponseDto.getResponseDto());

                    // set child tree node and update parent tree node
                    // when the operation is ref to, the found data size should always be one
                    extractedVisualField = dataModelServiceStub.extractValueFromResponse(secondRequestUrlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                    TreeNode childNode = new TreeNode(secondRequestPackageName, secondRequestEntityName, secondRequestIdData, extractedVisualField.get(0), dataFlowTreeDto.getTreeNode(), new ArrayList<>());
                    dataFlowTreeDto.getTreeNode().getChildren().add(childNode);
                    dataFlowTreeDto.getAnchorTreeNodeList().add(childNode);
                }
                expressionDto.getRequestUrlStack().add(requestUrlSet);
                expressionDto.getJsonResponseStack().add(responseDtoList);
                break;
            case REF_BY:

                // first TreeNode, which is the entity, need to initiate the request to get the visual field
                // request to get visual field
                requestPackageName = entity.pkg().getText();
                requestEntityName = entity.ety().getText();

                requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        requestPackageName,
                        requestEntityName,
                        DataModelServiceStub.UNIQUE_IDENTIFIER,
                        rootIdData);

                urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                expressionDto.getRequestUrlStack().add(Collections.singleton(urlToResponseDto.getRequestUrl()));
                expressionDto.getJsonResponseStack().add(Collections.singletonList(urlToResponseDto.getResponseDto()));

                // first tree node
                // when given root id data, the found data size should always be one
                extractedVisualField = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                dataFlowTreeDto.setTreeNode(new TreeNode(requestPackageName, requestEntityName, rootIdData, extractedVisualField.get(0), null, new ArrayList<>()));


                // refBy request
                DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
                requestPackageName = bwdNode.entity().pkg().getText();
                requestEntityName = bwdNode.entity().ety().getText();
                String requestAttributeName = bwdNode.attr().getText();
                requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                        this.applicationProperties.getGatewayUrl(),
                        requestPackageName,
                        requestEntityName,
                        requestAttributeName,
                        rootIdData);

                // the response may have data with one or multiple lines.
                urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);

                // second TreeNode, might be multiple
                List<Object> refByDataIdList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.UNIQUE_IDENTIFIER);
                List<Object> refByDataVisualFieldList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                Map<Object, Object> idToVisualFieldMap = CollectionUtils.zipToMap(refByDataIdList, refByDataVisualFieldList);
                String finalRequestPackageName = requestPackageName;
                String finalRequestEntityName = requestEntityName;
                idToVisualFieldMap.forEach((id, visualField) -> {
                    TreeNode childNode = new TreeNode(finalRequestPackageName, finalRequestEntityName, id, visualField, dataFlowTreeDto.getTreeNode(), new ArrayList<>());
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
        log.info(String.format("Entering resolving subsequent link process, the last expression is [%s], now resolving new subsequent link [%s].", lastExpressionDto.getExpression(), expressionDto.getExpression()));

        List<CommonResponseDto> lastRequestResultList = lastExpressionDto.getJsonResponseStack().peek();
        List<TreeNode> newAnchorTreeNodeList = new ArrayList<>();

        // last request info for building preview tree usage
        String lastRequestPackageName = "";
        String lastRequestEntityName = "";
        switch (lastExpressionDto.getDataModelExpressionOpType()) {
            case REF_TO:
                lastRequestPackageName = Objects.requireNonNull(lastExpressionDto.getEntity().pkg()).getText();
                lastRequestEntityName = Objects.requireNonNull(lastExpressionDto.getEntity().ety()).getText();
                break;
            case REF_BY:
                lastRequestPackageName = Objects.requireNonNull(lastExpressionDto.getBwdNode().entity().pkg()).getText();
                lastRequestEntityName = Objects.requireNonNull(lastExpressionDto.getBwdNode().entity().ety()).getText();
                break;
            default:
                break;
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
                    List<Object> requestIdDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto, requestId);
                    for (Object requestIdData : requestIdDataList) {
                        // find parent tree node, from attribute to id might found multiple ID which means multiple tree nodes
                        List<Object> parentIdList = dataModelServiceStub.getResponseIdFromAttribute(lastRequestResponseDto, requestId, requestIdData);
                        List<TreeNode> parentTreeNodeList = new ArrayList<>();
                        String finalLastRequestPackageName = lastRequestPackageName;
                        String finalLastRequestEntityName = lastRequestEntityName;
                        Objects.requireNonNull(parentIdList).forEach(parentId -> {
                            TreeNode parentNode = findParentNode(dataFlowTreeDto.getAnchorTreeNodeList(), finalLastRequestPackageName, finalLastRequestEntityName, parentId);
                            Objects.requireNonNull(parentNode, "Cannot find parent node from given last request info");
                            parentTreeNodeList.add(parentNode);
                        });

                        Map<String, Object> requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                DataModelServiceStub.UNIQUE_IDENTIFIER,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

                        // set child tree node and update parent tree node
                        List<Object> responseIdList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.UNIQUE_IDENTIFIER);
                        List<Object> responseVisualFieldList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                        Map<Object, Object> idToVisualFieldMap = CollectionUtils.zipToMap(responseIdList, responseVisualFieldList);
                        idToVisualFieldMap.forEach((id, visualField) -> {
                            // the list's size is one due to it's referenceTo operation
                            parentTreeNodeList.forEach(parentNode -> {
                                // bind childNode which is generated by one id to multiple parents
                                TreeNode childNode = new TreeNode(requestPackageName, requestEntityName, id, visualField, parentNode, new ArrayList<>());
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

                    List<Object> requestIdDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResponseDto, DataModelServiceStub.UNIQUE_IDENTIFIER);
                    for (Object requestIdData : requestIdDataList) {
                        Objects.requireNonNull(requestIdData,
                                "Cannot find 'id' from last request response. " +
                                        "Please ensure that the interface returned the data with one key named: 'id' as the development guideline requires.");
                        // find parent tree node
                        TreeNode parentNode = findParentNode(dataFlowTreeDto.getAnchorTreeNodeList(), lastRequestPackageName, lastRequestEntityName, requestIdData);
                        Objects.requireNonNull(parentNode, "Cannot find parent node from given last request info");

                        Map<String, Object> requestParamMap = dataModelServiceStub.generateGetUrlParamMap(
                                this.applicationProperties.getGatewayUrl(),
                                requestPackageName,
                                requestEntityName,
                                requestAttributeName,
                                requestIdData);
                        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.CHAIN_REQUEST_URL, requestParamMap);
                        requestUrlSet.add(urlToResponseDto.getRequestUrl());
                        responseDtoList.add(urlToResponseDto.getResponseDto());

                        // set child tree node and update parent tree node
                        List<Object> responseIdList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.UNIQUE_IDENTIFIER);
                        List<Object> responseVisualFieldList = dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelServiceStub.VISUAL_FIELD);
                        Map<Object, Object> idToVisualFieldMap = CollectionUtils.zipToMap(responseIdList, responseVisualFieldList);
                        idToVisualFieldMap.forEach((id, visualField) -> {
                            TreeNode childNode = new TreeNode(requestPackageName, requestEntityName, id, visualField, parentNode, new ArrayList<>());
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
                    List<Object> fetchDataList = dataModelServiceStub.extractValueFromResponse(lastRequestResult, attrName);
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
            if (node.equals(new TreeNode(lastRequestPackageName, lastRequestEntityName, rootIdData))) {
                return node;
            }
        }
        return null;
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

    /**
     * Find write back target
     *
     * @param resultDtoStack the resolved expression dto result stack
     * @return the write back target dto which contains info to execute write back function
     */
    private WriteBackTargetDto findWriteBackTarget(Stack<DataModelExpressionDto> resultDtoStack) {
        DataModelExpressionDto finalFetchDto = Objects.requireNonNull(resultDtoStack.pop());
        List<CommonResponseDto> lastRequestResponse;
        String writeBackPackageName = "";
        String writeBackEntityName = "";
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
            switch (lastLinkDto.getDataModelExpressionOpType()) {
                case REF_TO:
                    writeBackPackageName = Objects.requireNonNull(lastLinkDto.getEntity().pkg(), "Cannot find package.").getText();
                    writeBackEntityName = Objects.requireNonNull(lastLinkDto.getEntity().ety(), "Cannot find attribute.").getText();
                    break;
                case REF_BY:
                    writeBackPackageName = Objects.requireNonNull(lastLinkDto.getBwdNode().entity().pkg(), "Cannot find package.").getText();
                    writeBackEntityName = Objects.requireNonNull(lastLinkDto.getBwdNode().entity().ety(), "Cannot find entity.").getText();
                    break;
                default:
                    break;
            }
        }
        String writeBackAttr = Objects.requireNonNull(finalFetchDto.getOpFetch()).attr().getText();
        return new WriteBackTargetDto(lastRequestResponse, writeBackPackageName, writeBackEntityName, writeBackAttr);
    }
    
    public List<EntityDto> getAllEntities(String dataModelExpression){
        if(StringUtils.isBlank(dataModelExpression)){
            throw new WecubeCoreException("Data model expression cannot be blank.");
        }
        
        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(dataModelExpression);
        List<EntityDto> entityDtos = new ArrayList<EntityDto>();
        
        for(EntityQueryExprNodeInfo exprNodeInfo : exprNodeInfos){
            EntityDto entityDto = new EntityDto();
            entityDto.setPackageName(exprNodeInfo.getPackageName());
            entityDto.setEntityName(exprNodeInfo.getEntityName());
            
            entityDtos.add(entityDto);
        }
        
        entityDtos.forEach(entity -> {
            Optional<PluginPackageDataModel> dataModelOptional = pluginPackageDataModelRepository
                    .findLatestDataModelByPackageName(entity.getPackageName());
            if (dataModelOptional.isPresent()) {
                
                Optional<PluginPackageEntity> entityOptional = pluginPackageEntityRepository
                        .findByPackageNameAndNameAndDataModelVersion(entity.getPackageName(), entity.getEntityName(),
                                dataModelOptional.get().getVersion());
                if (entityOptional.isPresent()) {
                    entity.setAttributes(entityOptional.get().getPluginPackageAttributeList());
                }
            }
        });
        
        return entityDtos;
    }

//    @Override
//    public List<EntityDto> getAllEntities(String dataModelExpression) {
//        if (dataModelExpression.isEmpty() || !dataModelExpression.contains(":")) {
//            throw new WecubeCoreException(String.format("Illegal data model expression[%s]", dataModelExpression));
//        }
//
//        Iterable<String> split = Splitter.onPattern("([>~])").split(dataModelExpression);
//        Iterable<String> firstSection = Splitter.onPattern("([:.])").splitToList((split.iterator().next()));
//        List<String> firstSectionStrings = new ArrayList<String>();
//        Iterator<String> firstSectionStringIterator = firstSection.iterator();
//        while (firstSectionStringIterator.hasNext()) {
//            firstSectionStrings.add(firstSectionStringIterator.next());
//        }
//        if (firstSectionStrings.size() < 2) {
//            throw new WecubeCoreException(String.format("Illegal data model expression[%s]", dataModelExpression));
//        }
//
//        Queue<DataModelExpressionDto> expressionDtoQueue = new DataModelExpressionParser()
//                .parseAll(dataModelExpression);
//
//        List<EntityDto> entityDtos = new ArrayList<EntityDto>();
//        DataModelExpressionDto dataModelExpressionDtoFirst = expressionDtoQueue.poll();
//        entityDtos = resolveFirstLinkReturnEntities(dataModelExpressionDtoFirst);
//
//        for (int i = expressionDtoQueue.size(); i > 1; i--) {
//            DataModelExpressionDto dataModelExpressionDto = expressionDtoQueue.poll();
//            EntityDto entityDto = resolveLinkReturnEntity(dataModelExpressionDto);
//            if (null != entityDto) {
//                entityDtos.add(entityDto);
//            }
//        }
//
//        entityDtos.forEach(entity -> {
//            Optional<PluginPackageDataModel> dataModelOptional = pluginPackageDataModelRepository
//                    .findLatestDataModelByPackageName(entity.getPackageName());
//            if (dataModelOptional.isPresent()) {
//                Optional<PluginPackageEntity> entityOptional = pluginPackageEntityRepository
//                        .findByPackageNameAndNameAndDataModelVersion(entity.getPackageName(), entity.getEntityName(),
//                                dataModelOptional.get().getVersion());
//                if (entityOptional.isPresent()) {
//                    entity.setAttributes(entityOptional.get().getPluginPackageAttributeList());
//                }
//            }
//        });
//
//        return entityDtos;
//    }

    private EntityDto buildEntityForEntityFetch(DataModelExpressionDto expressionDto) {
        DataModelParser.EntityContext entity = expressionDto.getEntity();
        return new EntityDto(entity.pkg().getText(), entity.ety().getText());
    }

    private EntityDto resolveLinkReturnEntity(DataModelExpressionDto expressionDto) {
        switch (expressionDto.getDataModelExpressionOpType()) {
            case REF_TO:
                return new EntityDto(expressionDto.getEntity().pkg().getText(), expressionDto.getEntity().ety().getText());
            case REF_BY:
                DataModelParser.Bwd_nodeContext bwdNode = expressionDto.getBwdNode();
                return new EntityDto(bwdNode.entity().pkg().getText(), bwdNode.entity().ety().getText());
            default:
                break;
        }
        return null;
    }

    private List<EntityDto> resolveFirstLinkReturnEntities(DataModelExpressionDto expressionDto) {
        switch (expressionDto.getDataModelExpressionOpType()) {
            case ENTITY_FETCH:
                EntityDto entityDto = buildEntityForEntityFetch(expressionDto);
                return Lists.newArrayList(entityDto);
            case REF_TO:
                EntityDto firstEntityDtoInRefToCase = buildForwardNodeEntityForRefTo(expressionDto);
                EntityDto secondEntityDtoInRefToCase = buildEntityForRefTo(expressionDto);
                return Lists.newArrayList(firstEntityDtoInRefToCase, secondEntityDtoInRefToCase);
            case REF_BY:
                EntityDto firstEntityDtoInRefByCase = buildEntityForRefBy(expressionDto);
                EntityDto secondEntityDtoInRefByCase = buildBackwardNodeEntityForRefBy(expressionDto);
                return Lists.newArrayList(firstEntityDtoInRefByCase, secondEntityDtoInRefByCase);
            default:
                break;
        }
        return null;
    }

    private EntityDto buildForwardNodeEntityForRefTo(DataModelExpressionDto expressionDto) {
        return new EntityDto(expressionDto.getFwdNode().entity().pkg().getText(),
                expressionDto.getFwdNode().entity().ety().getText());
    }

    private EntityDto buildEntityForRefTo(DataModelExpressionDto expressionDto) {
        return new EntityDto(expressionDto.getEntity().pkg().getText(), expressionDto.getEntity().ety().getText());
    }

    private EntityDto buildBackwardNodeEntityForRefBy(DataModelExpressionDto expressionDto) {
        return new EntityDto(expressionDto.getBwdNode().entity().pkg().getText(),
                expressionDto.getBwdNode().entity().ety().getText());
    }

    private EntityDto buildEntityForRefBy(DataModelExpressionDto expressionDto) {
        return new EntityDto(expressionDto.getEntity().pkg().getText(), expressionDto.getEntity().ety().getText());
    }
}
