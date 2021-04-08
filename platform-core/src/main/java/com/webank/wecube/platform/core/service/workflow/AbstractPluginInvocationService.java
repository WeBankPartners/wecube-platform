package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMgmtService;

public abstract class AbstractPluginInvocationService extends AbstractWorkflowService {

    protected static final String CALLBACK_PARAMETER_KEY = "callbackParameter";
    protected static final String INPUT_PARAMETER_KEY_OPERATOR = "operator";

    protected static final String RESULT_CODE_OK = "0";
    protected static final String RESULT_CODE_ERR = "1";
    
    protected static final String PLUGIN_RESULT_CODE_FAIL = "1";
    protected static final String PLUGIN_RESULT_CODE_PARTIALLY_FAIL = "1";
    protected static final String PLUGIN_RESULT_CODE_PARTIALLY_KEY = "errorCode";

    protected static final String DATA_TYPE_STRING = "string";
    protected static final String DATA_TYPE_NUMBER = "number";

    protected static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    protected static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;

    protected static final int MAX_PARAM_VAL_SIZE = 3000;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected PluginInvocationResultService pluginInvocationResultService;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestRepository;

    @Autowired
    protected TaskNodeInstInfoMapper taskNodeInstInfoRepository;

    @Autowired
    protected TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamRepository;

    @Autowired
    protected PluginConfigMgmtService pluginConfigMgmtService;

    @Autowired
    protected StandardEntityOperationService entityOperationService;
    
    @Autowired
    protected ApplicationProperties applicationProperties;
    
    @Autowired
    protected SimpleEncryptionService simpleEncryptionService;
    
    @Autowired
    protected EntityQueryExpressionParser entityQueryExpressionParser;
    
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected TaskNodeInstInfoEntity findExactTaskNodeInstInfoEntityWithNodeId(
            List<TaskNodeInstInfoEntity> nodeInstEntities, String nodeId) {
        for (TaskNodeInstInfoEntity nodeInst : nodeInstEntities) {
            if (nodeId.equalsIgnoreCase(nodeInst.getNodeId())) {
                return nodeInst;
            }
        }

        return null;
    }

    protected TaskNodeDefInfoEntity findExactTaskNodeDefInfoEntityWithNodeId(
            List<TaskNodeDefInfoEntity> nodeDefEntities, String nodeId) {
        for (TaskNodeDefInfoEntity nodeDef : nodeDefEntities) {
            if (nodeId.equalsIgnoreCase(nodeDef.getNodeId())) {
                return nodeDef;
            }
        }

        return null;
    }

    protected String trimWithMaxLength(String s) {
        if (s == null) {
            return "";
        }

        if (s.length() < 200) {
            return s;
        }

        return s.substring(0, 200);
    }

    protected String asString(Object val, String sType) {
        if (val == null) {
            return null;
        }
        if (val instanceof String && DATA_TYPE_STRING.equals(sType)) {
            return (String) val;
        }

        if (val instanceof Integer && DATA_TYPE_NUMBER.equals(sType)) {
            return String.valueOf(val);
        }

        return val.toString();

    }

    protected String trimExceedParamValue(String val, int size) {
        if (val.length() > size) {
            return val.substring(0, size);
        }

        return val;
    }

    protected Object fromString(String val, String sType) {
        if (DATA_TYPE_STRING.equals(sType)) {
            return val;
        }

        if (StringUtils.isBlank(val)) {
            return null;
        }

        if (DATA_TYPE_NUMBER.equals(sType)) {
            return Integer.parseInt(val);
        }

        return val;
    }

    protected String assembleValueList(List<Object> retDataValues) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        sb.append("[");

        for (Object dv : retDataValues) {
            if (!isFirst) {
                sb.append(",");
            } else {
                isFirst = false;
            }

            sb.append(dv == null ? "" : dv);
        }

        sb.append("]");

        return sb.toString();
    }

    protected List<Map<String, Object>> validateAndCastResultData(List<Object> resultData) {
        List<Map<String, Object>> outputParameterMaps = new ArrayList<Map<String, Object>>();

        for (Object obj : resultData) {
            if (obj == null) {
                continue;
            }

            if (!(obj instanceof Map)) {
                log.error("unexpected data type:returned object is not a instance of map, obj={}", obj);
                throw new WecubeCoreException("3163","Unexpected data type");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> retRecord = (Map<String, Object>) obj;

            outputParameterMaps.add(retRecord);
        }

        return outputParameterMaps;

    }

    protected PluginConfigInterfaceParameters findPreConfiguredPluginConfigInterfaceParameter(
            List<PluginConfigInterfaceParameters> outputParameters, String paramName) {
        for (PluginConfigInterfaceParameters p : outputParameters) {
            if (p.getName().equals(paramName)) {
                return p;
            }
        }

        return null;
    }
    
    protected String tryEncodeParamDataValue(String rawDataValue){
        if(StringUtils.isBlank(rawDataValue)){
            return rawDataValue;
        }
        
        String cipherDataValue = simpleEncryptionService.encodeToAesBase64(rawDataValue);
        return cipherDataValue;
    }
    
    protected String tryDecodeParamDataValue(String cipherDataValue){
        if(StringUtils.isBlank(cipherDataValue)){
            return cipherDataValue;
        }
        
        String rawDataValue = null;
        try{
            rawDataValue = simpleEncryptionService.decodeFromAesBase64(cipherDataValue);
        }catch(Exception e){
            log.info("errors while decode cipher data value:{},error:{}", cipherDataValue, e.getMessage());
            rawDataValue = cipherDataValue;
        }
        
        return rawDataValue;
    }

}
