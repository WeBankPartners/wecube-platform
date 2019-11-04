package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.DataModelExpressionDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.DataModelExpressionHelper;
import com.webank.wecube.platform.core.utils.HttpClientUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.constant.DataModelExpressionOpType;
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
    @Autowired
    PluginPackageDataModelServiceImpl dataModelService;
    @Autowired
    PluginPackageEntityRepository entityRepository;
    @Autowired
    PluginPackageAttributeRepository attributeRepository;

    private String gatewayUrl;
    private Queue<DataModelExpressionHelper> expressionHelperQueue;
    private static final Logger logger = LoggerFactory.getLogger(DataModelExpressionServiceImpl.class);
    private static final String requestUrl = "http://{gatewayUrl}/{packageName}/entities/{entityName}/{attributeName}?value={value}";

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }


    public Queue<DataModelExpressionHelper> getExpressionHelperQueue() {
        return expressionHelperQueue;
    }

    public void setExpressionHelperQueue(Queue<DataModelExpressionHelper> expressionHelperQueue) {
        this.expressionHelperQueue = expressionHelperQueue;
    }


    @Override
    public Stack<DataModelExpressionDto> fetchData(String gatewayUrl, String dataModelExpression, String rootIdName, String rootIdData) throws WecubeCoreException {
        Stack<DataModelExpressionDto> resultList = new Stack<>();
        expressionHelperQueue = parseDataModelExpression(dataModelExpression);

        if (expressionHelperQueue.size() == 0) {
            String msg = String.format("Cannot extract information from the given expression [%s].", dataModelExpression);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        boolean isStart = true;
        String lastLinkAttr = rootIdName;
        while (!expressionHelperQueue.isEmpty()) {
            DataModelExpressionDto resolvedResult;
            DataModelExpressionHelper link = Objects.requireNonNull(expressionHelperQueue.poll());
            if (isStart) {
                // resolve root link with guid as input
                resolvedResult = resolveLink(link, lastLinkAttr, Collections.singletonList(rootIdData), true);
                resultList.add(resolvedResult);
                isStart = false;
            } else {
                // resolve non-root link with last link's output as input
                Stack<Map<String, String>> latestReturnedJsonList = resultList.peek().getReturnedJson();
                Map<String, String> latestReturnedJson = latestReturnedJsonList.peek();
                resolvedResult = resolveLink(link, lastLinkAttr, Collections.singletonList(latestReturnedJson.get(lastLinkAttr)), false);
            }
            resultList.add(resolvedResult);

            lastLinkAttr = link.getSecondNode() == null ? link.getFirstNode().attr().getText() : link.getSecondNode().attr().getText();

        }
        return resultList;
    }

    private DataModelExpressionDto resolveLink(DataModelExpressionHelper link, String lastLinkAttr, List<String> inputData, boolean isStart) throws WecubeCoreException {

        Stack<Map<String, String>> requestResultList = new Stack<>();

        if (isStart){
            // first node
            String firstNodePackageName = link.getFirstNode().pkg().getText();
            String firstNodeEntityName = link.getFirstNode().entity().getText();


            for (String data : inputData) {
                MultiValueMap<String, String> paramMap = generateParameterMap(gatewayUrl, firstNodePackageName, firstNodeEntityName, lastLinkAttr, data);
                Map<String, String> requestResult = request(paramMap);
                requestResultList.add(requestResult);
            }
        }

        // second node

        DataModelParser.NodeContext secondNode = link.getSecondNode();
        if (secondNode == null) {
            // if second node is null, which means the link is consisted of one node
            return new DataModelExpressionDto(link.getFirstNode().getText(), requestResultList);
        }

        // get op from link
        String op = link.getOp().getText();
        // first node attribute name is the next node's input attribute
        String firstNodeAttributeName = link.getFirstNode().attr().getText();

        // TODO: second node request and return result, need to solve referenceTo and referenceBy op
        DataModelExpressionOpType opType = DataModelExpressionOpType.fromCode(op);
        if (opType == DataModelExpressionOpType.ReferenceBy) {
            System.out.println("By");
            // TODO: add referenceBy validation
        }

        if (opType == DataModelExpressionOpType.ReferenceTo) {
            System.out.println("To");
            // TODO: add referenceTo validation
        }
        String secondNodePackageName = link.getSecondNode().pkg().getText();
        String secondNodeEntityName = link.getSecondNode().entity().getText();
        Map<String, String> lastRequestResult = requestResultList.peek();
        for (String data : Collections.singletonList(lastRequestResult.get(firstNodeAttributeName))) {
            MultiValueMap<String, String> paramMap = generateParameterMap(gatewayUrl, secondNodePackageName, secondNodeEntityName, "id", data);
            Map<String, String> requestResult = request(paramMap);
            requestResultList.add(requestResult);
        }
        return new DataModelExpressionDto(link.getExpression(), requestResultList);
    }

    private MultiValueMap<String, String> generateParameterMap(String gatewayUrl, String packageName, String entityName, String attributeName, String value) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("gatewayUrl", gatewayUrl);
        paramMap.add("packageName", packageName);
        paramMap.add("entityName", entityName);
        paramMap.add("attrName", attributeName);
        paramMap.add("value", value);
        return paramMap;
    }

    public static void main(String[] args) {
//        DataModelExpressionServiceImpl service = new DataModelExpressionServiceImpl();
//        service.setGatewayUrl("127.0.0.1");
//        String url = "https://support.oneskyapp.com/hc/en-us/article_attachments/202761727/example_2.json";
//        try {
//            ResponseEntity<String> stringResponseEntity = HttpClientUtils.sendGetRequestWithoutParam(url);
//            Map<String, String> returnedJson = JsonUtils.toObject(Objects.requireNonNull(stringResponseEntity.getBody()), Map.class);
//            System.out.println(returnedJson);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }


    }

    private Queue<DataModelExpressionHelper> parseDataModelExpression(String dataModelExpression) throws WecubeCoreException {
        Queue<DataModelExpressionHelper> expressionHelperQueue;
        expressionHelperQueue = new DataModelExpressionParser().parse(dataModelExpression);
        return expressionHelperQueue;

    }

    private Map<String, String> request(MultiValueMap<String, String> paramMap) throws WecubeCoreException {
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


}
