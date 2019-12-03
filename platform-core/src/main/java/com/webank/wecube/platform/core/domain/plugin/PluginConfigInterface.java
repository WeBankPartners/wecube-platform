package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wecube.platform.core.utils.Constants.KEY_COLUMN_DELIMITER;

@Entity
@Table(name = "plugin_config_interfaces")
public class PluginConfigInterface {

    @Id
    private String id;

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
    @Column
    private String isAsyncProcessing;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Where(clause = "type = 'INPUT'")
    private Set<PluginConfigInterfaceParameter> inputParameters;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Where(clause = "type = 'OUTPUT'")
    private Set<PluginConfigInterfaceParameter> outputParameters;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PrePersist
    public void initId() {
        if (null == this.id || this.id.trim().equals("")) {
            this.id = String.join(KEY_COLUMN_DELIMITER,
                    null != pluginConfig ? (null != pluginConfig.getPluginPackage() ? pluginConfig.getPluginPackage().getName() : null) :null,
                    null != pluginConfig ? (null != pluginConfig.getPluginPackage() ? pluginConfig.getPluginPackage().getVersion() : null) :null,
                    null != pluginConfig ? pluginConfig.getName() : null,
                    null != pluginConfig ? pluginConfig.getEntityName() : null,
                    action.replaceAll("\\s+", "_"));
        }
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
        // TODO: need to optimize
        return inputParameters == null ? null
                : inputParameters.stream().sorted(Comparator.comparing(PluginConfigInterfaceParameter::getName))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setInputParameters(Set<PluginConfigInterfaceParameter> inputParameters) {
        this.inputParameters =inputParameters;
    }

    public Set<PluginConfigInterfaceParameter> getOutputParameters() {
        // TODO: need to optimize
        return outputParameters == null ? null
                : outputParameters.stream().sorted(Comparator.comparing(PluginConfigInterfaceParameter::getName))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setOutputParameters(Set<PluginConfigInterfaceParameter> outputParameters) {
        this.outputParameters =  outputParameters;
    }

    public PluginConfigInterface() {
    }

    public PluginConfigInterface(String id, PluginConfig pluginConfig, String action, String serviceName, String serviceDisplayName, String path, String httpMethod, Set<PluginConfigInterfaceParameter> inputParameters, Set<PluginConfigInterfaceParameter> outputParameters) {
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

    public String getIsAsyncProcessing() {
        return isAsyncProcessing;
    }

    public void setIsAsyncProcessing(String isAsyncProcessing) {
        this.isAsyncProcessing = isAsyncProcessing;
    }
}
