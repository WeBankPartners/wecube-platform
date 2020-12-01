//package com.webank.wecube.platform.core.domain;
//
//import javax.persistence.*;
//
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//@Entity
//@Table(name = "system_variables")
//public class SystemVariable {
//    public static final String ACTIVE = "active";
//    public static final String INACTIVE = "inactive";
//
//    public static final String SCOPE_GLOBAL = "global";
//    public static final String SOURCE_SYSTEM = "system";
//
//    @Id
//    private String id;
//
//    @Column
//    private String scope;
//
//    @Column
//    private String packageName;
//
//	@Column
//	private String name;
//	@Column
//	private String value;
//	@Column
//	private String defaultValue;
//
//	@Column
//    private String source;
//
//	@Column
//	private String status;
//
//	public String getId() {
//		return id;
//	}
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
//    public String getPackageName() {
//        return packageName;
//    }
//
//    public void setPackageName(String packageName) {
//        this.packageName = packageName;
//    }
//
//    public String getScope() {
//        return scope;
//    }
//
//    public void setScope(String scope) {
//        this.scope = scope;
//    }
//
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public String getDefaultValue() {
//        return defaultValue;
//    }
//
//    public void setDefaultValue(String defaultValue) {
//        this.defaultValue = defaultValue;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getSource() {
//        return source;
//    }
//
//    public void setSource(String source) {
//        this.source = source;
//    }
//
//    public SystemVariable(String id, String name, String value, String defaultValue, String scope,
//                          String source, String packageName, String status) {
//        this.id = id;
//        this.name = name;
//        this.value = value;
//        this.defaultValue = defaultValue;
//        this.scope = scope;
//        this.source = source;
//        this.packageName = packageName;
//        this.status = status;
//    }
//
//    public SystemVariable() {
//        super();
//    }
//
//    public SystemVariable activate() {
//	    this.status = SystemVariable.ACTIVE;
//	    return this;
//    }
//
//    public SystemVariable deactivate() {
//	    this.status = SystemVariable.INACTIVE;
//	    return this;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toString(this);
//    }
//
//}
