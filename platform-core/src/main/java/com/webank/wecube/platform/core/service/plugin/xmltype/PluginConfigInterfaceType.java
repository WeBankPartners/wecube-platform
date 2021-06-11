package com.webank.wecube.platform.core.service.plugin.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interfaceType", propOrder = { "inputParameters", "outputParameters" })
public class PluginConfigInterfaceType {
    @XmlAttribute(name = "action")
    protected String action;
    @XmlAttribute(name = "path")
    protected String path;
    @XmlAttribute(name = "httpMethod")
    protected String httpMethod;
    @XmlAttribute(name = "isAsyncProcessing")
    protected String isAsyncProcessing;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "filterRule")
    protected String filterRule;

    @XmlAttribute(name = "description")
    protected String description;

    protected PluginConfigInputParametersType inputParameters;
    protected PluginConfigOutputParametersType outputParameters;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIsAsyncProcessing() {
        return isAsyncProcessing;
    }

    public void setIsAsyncProcessing(String isAsyncProcessing) {
        this.isAsyncProcessing = isAsyncProcessing;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;
    }

    public PluginConfigInputParametersType getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(PluginConfigInputParametersType inputParameters) {
        this.inputParameters = inputParameters;
    }

    public PluginConfigOutputParametersType getOutputParameters() {
        return outputParameters;
    }

    public void setOutputParameters(PluginConfigOutputParametersType outputParameters) {
        this.outputParameters = outputParameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
