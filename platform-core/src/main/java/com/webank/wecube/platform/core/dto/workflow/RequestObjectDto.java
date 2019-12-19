package com.webank.wecube.platform.core.dto.workflow;

import java.util.HashMap;
import java.util.Map;

public class RequestObjectDto {
    private Map<String, String> inputs = new HashMap<>();
    private Map<String, String> outputs = new HashMap<>();

    public Map<String, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, String> inputs) {
        this.inputs = inputs;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, String> outputs) {
        this.outputs = outputs;
    }
    
    public void addInput(String paramName, String paramValAsStr){
        this.inputs.put(paramName, paramValAsStr);
    }
    
    public void addOutput(String paramName, String paramValAsStr){
        this.outputs.put(paramName, paramValAsStr);
    }

}
