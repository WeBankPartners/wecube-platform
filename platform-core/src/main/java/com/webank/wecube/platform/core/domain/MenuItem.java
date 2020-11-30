//package com.webank.wecube.platform.core.domain;
//
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.hibernate.annotations.Generated;
//import org.hibernate.annotations.GenerationTime;
//
//import javax.persistence.*;
//import java.util.Objects;
//
//@Entity
//@Table(name = "menu_items")
//public class MenuItem {
//    public static enum Source {
//        SYSTEM, PLUGIN
//    }
//
//    public final static String ROLE_PREFIX = "MENU_";
//
//    public final static String MENU_JOBS = "JOBS";
//    public final static String MENU_DESIGNING = "DESIGNING";
//    public final static String MENU_IMPLEMENTATION = "IMPLEMENTATION";
//    public final static String MENU_MONITORING = "MONITORING";
//    public final static String MENU_ADJUSTMENT = "ADJUSTMENT";
//    public final static String MENU_INTELLIGENCE_OPS = "INTELLIGENCE_OPS";
//    public final static String MENU_COLLABORATION = "COLLABORATION";
//    public final static String MENU_ADMIN = "ADMIN";
//    public final static String MENU_JOBS_INITIATOR = "JOBS_INITIATOR";
//    public final static String MENU_JOBS_EXECUTOR = "JOBS_EXECUTOR";
//    public final static String MENU_DESIGNING_PLANNING = "DESIGNING_PLANNING";
//    public final static String MENU_DESIGNING_RESOURCE_PLANNING = "DESIGNING_RESOURCE_PLANNING";
//    public final static String MENU_DESIGNING_APPLICATION_ARCHITECTURE = "DESIGNING_APPLICATION_ARCHITECTURE";
//    public final static String MENU_DESIGNING_APPLICATION_DEPLOYMENT = "DESIGNING_APPLICATION_DEPLOYMENT";
//    public final static String MENU_DESIGNING_CI_DATA_MANAGEMENT = "DESIGNING_CI_DATA_MANAGEMENT";
//    public final static String MENU_DESIGNING_CI_DATA_ENQUIRY = "DESIGNING_CI_DATA_ENQUIRY";
//    public final static String MENU_DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT = "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT";
//    public final static String MENU_DESIGNING_CI_INTEGRATED_QUERY_EXECUTION = "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION";
//    public final static String MENU_DESIGNING_ENUM_MANAGEMENT = "DESIGNING_ENUM_MANAGEMENT";
//    public final static String MENU_DESIGNING_ENUM_ENQUIRY = "DESIGNING_ENUM_ENQUIRY";
//    public final static String MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT = "IMPLEMENTATION_ARTIFACT_MANAGEMENT";
//    public final static String MENU_IMPLEMENTATION_APPLICATION_DEPLOYMENT = "IMPLEMENTATION_APPLICATION_DEPLOYMENT";
//    public final static String MENU_IMPLEMENTATION_BATCH_JOB = "IMPLEMENTATION_BATCH_JOB";
//    public final static String MENU_IMPLEMENTATION_HIGH_RISK_INSTRUCTION_MANAGEMENT = "IMPLEMENTATION_HIGH_RISK_INSTRUCTION_MANAGEMENT";
//    public final static String MENU_IMPLEMENTATION_WORKFLOW_EXECUTION = "IMPLEMENTATION_WORKFLOW_EXECUTION";
//    public final static String MENU_MONITORING_BASIC_MONITOR_MANAGEMENT = "MONITORING_BASIC_MONITOR_MANAGEMENT";
//    public final static String MENU_MONITORING_APPLICATION_MONITOR_MANAGEMENT = "MONITORING_APPLICATION_MONITOR_MANAGEMENT";
//    public final static String MENU_MONITORING_CONTROL_PANEL_SETTING = "MONITORING_CONTROL_PANEL_SETTING";
//    public final static String MENU_MONITORING_DISCOVERY = "MONITORING_DISCOVERY";
//    public final static String MENU_MONITORING_CONSISTENCE_MANAGEMENT = "MONITORING_CONSISTENCE_MANAGEMENT";
//    public final static String MENU_ADJUSTMENT_TENDENCY = "ADJUSTMENT_TENDENCY";
//    public final static String MENU_ADJUSTMENT_ROOT_CAUSE_INVESTIGATION = "ADJUSTMENT_ROOT_CAUSE_INVESTIGATION";
//    public final static String MENU_ADJUSTMENT_EXPANSION = "ADJUSTMENT_EXPANSION";
//    public final static String MENU_ADJUSTMENT_RECOVERY = "ADJUSTMENT_RECOVERY";
//    public final static String MENU_INTELLIGENCE_OPS_MODELING = "INTELLIGENCE_OPS_MODELING";
//    public final static String MENU_INTELLIGENCE_OPS_DATA_SYNCHRONIZATION = "INTELLIGENCE_OPS_DATA_SYNCHRONIZATION";
//    public final static String MENU_COLLABORATION_PLUGIN_MANAGEMENT = "COLLABORATION_PLUGIN_MANAGEMENT";
//    public final static String MENU_COLLABORATION_WORKFLOW_ORCHESTRATION = "COLLABORATION_WORKFLOW_ORCHESTRATION";
//    public final static String MENU_COLLABORATION_SERVICE_CHANNEL = "COLLABORATION_SERVICE_CHANNEL";
//    public final static String MENU_ADMIN_CMDB_MODEL_MANAGEMENT = "ADMIN_CMDB_MODEL_MANAGEMENT";
//    public final static String MENU_ADMIN_PERMISSION_MANAGEMENT = "ADMIN_PERMISSION_MANAGEMENT";
//    public final static String MENU_ADMIN_BASE_DATA_MANAGEMENT = "ADMIN_BASE_DATA_MANAGEMENT";
//    public final static String MENU_ADMIN_OPERATION_LOG = "MENU_ADMIN_OPERATION_LOG";
//
//    @Id
//    private String id;
//
//    @Column(unique = true)
//    private String code;
//
//    @Column(name = "parent_code")
//    private String parentCode;
//
//    @Column
//    private String source;
//
//    @Column
//    private String description;
//
//    @Column
//    private String localDisplayName;
//
//    @Generated(GenerationTime.INSERT)
//    @Column(name = "menu_order", nullable = false, updatable = false, columnDefinition = "integer auto_increment")
//    private Integer menuOrder;
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//    // @JsonIgnore
//    // @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, orphanRemoval =
//    // true)
//    // private List<RoleMenu> assignedRoles = new ArrayList<>();
//
//    public MenuItem() {
//        this(null);
//    }
//
//    public MenuItem(String code, String parentCode, String description) {
//        this(null, code, parentCode, Source.SYSTEM.name(), description, null);
//    }
//
//    public MenuItem(String id, String code, String parentCode, String source, String description, Integer menuOrder) {
//        this(null, code, parentCode, Source.SYSTEM.name(), description, description, null);
//    }
//
//    public MenuItem(String id, String code, String parentCode, String source, String description,
//            String localDisplayName, Integer menuOrder) {
//        this.id = id;
//        this.code = code;
//        this.parentCode = parentCode;
//        this.source = source;
//        this.description = description;
//        this.localDisplayName = localDisplayName;
//        this.menuOrder = menuOrder;
//    }
//
//    public MenuItem(String id) {
//        this.setId(id);
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//    public String getParentCode() {
//        return parentCode;
//    }
//
//    public void setParentCode(String parentCode) {
//        this.parentCode = parentCode;
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
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getLocalDisplayName() {
//        return localDisplayName;
//    }
//
//    public void setLocalDisplayName(String localDisplayName) {
//        this.localDisplayName = localDisplayName;
//    }
//
//    public Integer getMenuOrder() {
//        return menuOrder;
//    }
//
//    public void setMenuOrder(Integer menuOrder) {
//        this.menuOrder = menuOrder;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        MenuItem menuItem = (MenuItem) o;
//        return getId().equals(menuItem.getId()) && getCode().equals(menuItem.getCode())
//                && Objects.equals(getParentCode(), menuItem.getParentCode())
//                && Objects.equals(getDescription(), menuItem.getDescription())
//                && Objects.equals(getMenuOrder(), menuItem.getMenuOrder());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getId(), getCode(), getParentCode(), getDescription(), getMenuOrder());
//    }
//}
