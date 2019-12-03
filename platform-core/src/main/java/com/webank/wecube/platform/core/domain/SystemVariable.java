package com.webank.wecube.platform.core.domain;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.webank.wecube.platform.core.utils.Constants.KEY_COLUMN_DELIMITER;

@Entity
@Table(name = "system_variables")
public class SystemVariable {
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";

	public static final String SCOPE_TYPE_GLOBAL = "global";
	public static final String SCOPE_TYPE_PLUGIN_PACKAGE = "plugin-package";

	@Id
	private String id;

	@Column(name = "plugin_package_id")
    private String pluginPackageId;

	@JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "plugin_package_id", insertable = false, updatable = false)
    private PluginPackage pluginPackage;

	@Column
	private String name;
	@Column
	private String value;
	@Column
	private String defaultValue;
	@Column
	private String scopeType;
	@Column
	private String scopeValue;

	@Column
	private Integer seqNo;
	@Column
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@PrePersist
	public void initId() {
		if (null == this.id || this.id.trim().equals("")) {
			this.id = String.join(KEY_COLUMN_DELIMITER,
					null != pluginPackage ? pluginPackage.getName() : null,
					null != pluginPackage ? pluginPackage.getVersion() : null,
					name,
					SCOPE_TYPE_GLOBAL.equals(scopeType) ? SCOPE_TYPE_GLOBAL : scopeValue
			);
			this.id = this.id.replaceAll("\\s+", "_");
		}
	}

	public PluginPackage getPluginPackage() {
		return pluginPackage;
	}

	public void setPluginPackage(PluginPackage pluginPackage) {
		if (null != pluginPackage) {
			this.pluginPackage = pluginPackage;
			this.pluginPackageId = pluginPackage.getId();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getScopeType() {
		return scopeType;
	}

	public void setScopeType(String scopeType) {
		this.scopeType = scopeType;
	}

	public String getScopeValue() {
		return scopeValue;
	}

	public void setScopeValue(String scopeValue) {
		this.scopeValue = scopeValue;
	}

	public Integer getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static String getActive() {
		return ACTIVE;
	}

	public static String getInactive() {
		return INACTIVE;
	}

	public static String getScopeTypeGlobal() {
		return SCOPE_TYPE_GLOBAL;
	}

	public static String getScopeTypePluginPackage() {
		return SCOPE_TYPE_PLUGIN_PACKAGE;
	}

	public SystemVariable(String id, String name, String value, String defaultValue, String scopeType, String scopeValue, PluginPackage pluginPackage, Integer seqNo, String status) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.defaultValue = defaultValue;
		this.scopeType = scopeType;
		this.scopeValue = scopeValue;
		setPluginPackage(pluginPackage);
		this.seqNo = seqNo;
		this.status = status;
	}

	public SystemVariable() {
		super();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, new String[] {"pluginPackage"});
	}

    public String getPluginPackageId() {
        return null != pluginPackageId ? pluginPackageId : (null != pluginPackage ? pluginPackage.getId() : null);
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }
}
