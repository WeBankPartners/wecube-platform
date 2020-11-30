//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//import org.hibernate.annotations.Where;
//
//import javax.persistence.*;
//import java.util.Comparator;
//import java.util.LinkedHashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static com.webank.wecube.platform.core.utils.Constants.*;
//
//@Entity
//@Table(name = "plugin_config_interfaces")
//public class PluginConfigInterface {
//    public static final String DEFAULT_INTERFACE_TYPE = "EXECUTION";
//    public static final String DEFAULT_IS_ASYNC_PROCESSING_VALUE = "N";
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne
//    @JoinColumn(name = "plugin_config_id")
//    private PluginConfig pluginConfig;
//
//    @Column
//    private String action;
//    @Column
//    private String serviceName;
//    @Column
//    private String serviceDisplayName;
//    @Column
//    private String path;
//    @Column
//    private String httpMethod;
//    @Column
//    private String isAsyncProcessing;
//    @Column
//    private String type;
//    @Column
//    private String filterRule = "";
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @Where(clause = "type = 'INPUT'")
//    private Set<PluginConfigInterfaceParameter> inputParameters;
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @Where(clause = "type = 'OUTPUT'")
//    private Set<PluginConfigInterfaceParameter> outputParameters;
//
//    public String getId() {
//        return this.id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public PluginConfig getPluginConfig() {
//        return pluginConfig;
//    }
//
//    public void setPluginConfig(PluginConfig pluginConfig) {
//        this.pluginConfig = pluginConfig;
//    }
//
//    public String getAction() {
//        return action;
//    }
//
//    public void setAction(String action) {
//        this.action = action;
//    }
//
//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }
//
//    public String generateServiceName() {
//        return pluginConfig.getPluginPackage().getName() + SEPARATOR_OF_NAMES + pluginConfig.getName()
//                + (null != pluginConfig.getRegisterName()
//                        ? LEFT_BRACKET_STRING + pluginConfig.getRegisterName() + RIGHT_BRACKET_STRING : "")
//                + SEPARATOR_OF_NAMES + action;
//    }
//
//    public String getServiceDisplayName() {
//        return serviceDisplayName;
//    }
//
//    public void setServiceDisplayName(String serviceDisplayName) {
//        this.serviceDisplayName = serviceDisplayName;
//    }
//
//    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public String getHttpMethod() {
//        return httpMethod;
//    }
//
//    public void setHttpMethod(String httpMethod) {
//        this.httpMethod = httpMethod;
//    }
//    
//    public void addInputParameter(PluginConfigInterfaceParameter p){
//        if(inputParameters == null){
//            inputParameters =  new LinkedHashSet<PluginConfigInterfaceParameter>();
//        }
//        
//        inputParameters.add(p);
//    }
//    
//    public void addOutputParameter(PluginConfigInterfaceParameter p){
//        if(outputParameters == null){
//            outputParameters = new LinkedHashSet<PluginConfigInterfaceParameter>();
//        }
//        
//        outputParameters.add(p);
//    }
//
//    public Set<PluginConfigInterfaceParameter> getInlineInputParameters() {
//        return inputParameters == null ? new LinkedHashSet<>() : inputParameters;
//    }
//
//    public Set<PluginConfigInterfaceParameter> getInlineOutputParameters() {
//        return outputParameters == null ? new LinkedHashSet<>() : outputParameters;
//    }
//
//    public Set<PluginConfigInterfaceParameter> getInputParameters() {
//        // TODO: need to optimize
//        return inputParameters == null ? null
//                : inputParameters.stream().sorted(Comparator.comparing(PluginConfigInterfaceParameter::getName))
//                        .collect(Collectors.toCollection(LinkedHashSet::new));
//    }
//
//    public void setInputParameters(Set<PluginConfigInterfaceParameter> inputParameters) {
//        this.inputParameters = inputParameters;
//    }
//
//    public Set<PluginConfigInterfaceParameter> getOutputParameters() {
//        // TODO: need to optimize
//        return outputParameters == null ? null
//                : outputParameters.stream().sorted(Comparator.comparing(PluginConfigInterfaceParameter::getName))
//                        .collect(Collectors.toCollection(LinkedHashSet::new));
//    }
//
//    public void setOutputParameters(Set<PluginConfigInterfaceParameter> outputParameters) {
//        this.outputParameters = outputParameters;
//    }
//
//    public PluginConfigInterface() {
//    }
//
//    public PluginConfigInterface(String id, PluginConfig pluginConfig, String action, String serviceName,
//            String serviceDisplayName, String path, String httpMethod,
//            Set<PluginConfigInterfaceParameter> inputParameters, Set<PluginConfigInterfaceParameter> outputParameters) {
//        this.id = id;
//        this.pluginConfig = pluginConfig;
//        this.action = action;
//        this.serviceName = serviceName;
//        this.serviceDisplayName = serviceDisplayName;
//        this.path = path;
//        this.httpMethod = httpMethod;
//        this.inputParameters = inputParameters;
//        this.outputParameters = outputParameters;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[] { "pluginConfig" });
//    }
//
//    public String getIsAsyncProcessing() {
//        return isAsyncProcessing;
//    }
//
//    public void setIsAsyncProcessing(String isAsyncProcessing) {
//        this.isAsyncProcessing = isAsyncProcessing;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getFilterRule() {
//        return filterRule;
//    }
//
//    public void setFilterRule(String filterRule) {
//        this.filterRule = filterRule;
//    }
//}
