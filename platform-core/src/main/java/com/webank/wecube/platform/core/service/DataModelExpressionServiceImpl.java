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
    public List<DataModelExpressionDto> fetchData(String gatewayUrl, String dataModelExpression, String rootIdName, String rootIdData) throws WecubeCoreException {
        List<DataModelExpressionDto> resultList = new ArrayList<>();
        expressionHelperQueue = parseDataModelExpression(dataModelExpression);

        if (expressionHelperQueue.size() == 0) {
            String msg = String.format("Cannot extract information from the given expression [%s].", dataModelExpression);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        boolean isStart = true;
        String lastLinkAttr = rootIdName;
        while (!expressionHelperQueue.isEmpty()) {
            DataModelExpressionDto resolvedResult = new DataModelExpressionDto();
            DataModelExpressionHelper link = Objects.requireNonNull(expressionHelperQueue.poll());
            if (isStart) {
                // resolve root link with guid as input
                resolvedResult = resolveLink(link, lastLinkAttr, Collections.singletonList(rootIdData));
                resultList.add(resolvedResult);
                isStart = false;
            } else {
                // resolve non-root link with last link's output as input
                List<Map<String, String>> latestReturnedJsonList = resultList.get(resultList.size() - 1).getReturnedJson();
                Map<String, String> latestReturnedJson = latestReturnedJsonList.get(latestReturnedJsonList.size() - 1);
                resolvedResult = resolveLink(link, lastLinkAttr, Collections.singletonList(latestReturnedJson.get(lastLinkAttr)));
            }
            resultList.add(resolvedResult);

            lastLinkAttr = link.getSecondNode() == null ? link.getFirstNode().attr().getText() : link.getSecondNode().attr().getText();

        }
        return resultList;
    }

    private DataModelExpressionDto resolveLink(DataModelExpressionHelper link, String lastLinkAttr, List<String> inputData) {
        // TODO: add data model service validation
        // first node
        String packageName = link.getFirstNode().pkg().getText();
        String entityName = link.getFirstNode().entity().getText();
        String op = link.getOp().getText();
        List<Map<String, String>> requestResultList = new ArrayList<>();
        for (String data : inputData) {
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("gatewayUrl", gatewayUrl);
            paramMap.add("packageName", packageName);
            paramMap.add("entityName", entityName);
            paramMap.add("attrName", lastLinkAttr);
            paramMap.add("value", data);
            Map<String, String> requestResult = request(paramMap);
            requestResultList.add(requestResult);
        }

        // second node
        DataModelParser.NodeContext secondNode = link.getSecondNode();
        if (secondNode == null) {
            // if second node is null, which means the link is consisted of one node
            return new DataModelExpressionDto(link.getFirstNode().getText(), requestResultList);
        }
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

        return new DataModelExpressionDto();
    }


    public static void main(String[] args) {
        DataModelExpressionServiceImpl service = new DataModelExpressionServiceImpl();
        service.setGatewayUrl("127.0.0.1");
    }

    private Queue<DataModelExpressionHelper> parseDataModelExpression(String dataModelExpression) throws WecubeCoreException {
        Queue<DataModelExpressionHelper> expressionHelperQueue = new LinkedList<>();
        expressionHelperQueue = new DataModelExpressionParser().parse(dataModelExpression);
        return expressionHelperQueue;

    }

    private Map<String, String> request(MultiValueMap<String, String> paramMap) throws WecubeCoreException {
        ResponseEntity<String> response;
//        url = "https://support.oneskyapp.com/hc/en-us/article_attachments/202761727/example_2.json";
        Map<String, String> responseBodyMap = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            response = HttpClientUtils.sendGetRequestWithParamMap(requestUrl, paramMap, httpHeaders);
            if (StringUtils.isEmpty(response.getBody()) || response.getStatusCode().isError()) {
                String msg = String.format("Cannot fetch info from request url with param: [%s]", paramMap.toString());
                throw new WecubeCoreException(msg);
            }
            responseBodyMap = JsonUtils.toObject(response.getBody(), Map.class);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return responseBodyMap;
    }


}
