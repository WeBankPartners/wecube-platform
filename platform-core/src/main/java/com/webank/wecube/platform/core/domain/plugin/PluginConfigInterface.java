package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "plugin_config_interfaces")
public class PluginConfigInterface {

    @Id
    @GeneratedValue
    private Integer id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "plugin_config_id")
    private PluginConfig pluginConfig;

    @Column
    private String action;
    @Column
    private String serviceName;
    @Column
    private String serviceDisplayName;
    @Column
    private String path;
    @Column
    private String httpMethod;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Where(clause = "type = 'INPUT'")
    private Set<PluginConfigInterfaceParameter> inputParameters = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Where(clause = "type = 'OUTPUT'")
    private Set<PluginConfigInterfaceParameter> outputParameters = new LinkedHashSet<>();

    public void addInputParameter(PluginConfigInterfaceParameter parameter) {
        this.inputParameters.add(parameter);
    }

    public void addOutputParameter(PluginConfigInterfaceParameter parameter) {
        this.outputParameters.add(parameter);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDisplayName() {
        return serviceDisplayName;
    }

    public void setServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName;
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

    public Set<PluginConfigInterfaceParameter> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(Set<PluginConfigInterfaceParameter> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public Set<PluginConfigInterfaceParameter> getOutputParameters() {
        return outputParameters;
    }

    public void setOutputParameters(Set<PluginConfigInterfaceParameter> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public PluginConfigInterface() {
    }

    public PluginConfigInterface(Integer id, PluginConfig pluginConfig, String action, String serviceName, String serviceDisplayName, String path, String httpMethod, Set<PluginConfigInterfaceParameter> inputParameters, Set<PluginConfigInterfaceParameter> outputParameters) {
        this.id = id;
        this.pluginConfig = pluginConfig;
        this.action = action;
        this.serviceName = serviceName;
        this.serviceDisplayName = serviceDisplayName;
        this.path = path;
        this.httpMethod = httpMethod;
        this.inputParameters = inputParameters;
        this.outputParameters = outputParameters;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginConfig"});
    }
}
