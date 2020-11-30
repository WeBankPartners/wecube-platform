//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//import org.hibernate.annotations.Generated;
//import org.hibernate.annotations.GenerationTime;
//
//import javax.persistence.*;
//import java.util.Comparator;
//
//@Entity
//@Table(name = "plugin_package_menus")
//public class PluginPackageMenu implements Comparable<PluginPackageMenu> {
//
//    @Id
//    private String id;
//
//    public static final Comparator<PluginPackageMenu> COMPARE_BY_MENU_ORDER = Comparator.comparing(PluginPackageMenu::getMenuOrder);
//
//    @Column
//    private String code;
//
//    @Column
//    private String category;
//
//    @Column
//    private String source;
//
//    @Column
//    private String displayName;
//
//    @Column
//    private String localDisplayName;
//
//    @Generated(GenerationTime.INSERT)
//    @Column(name = "menu_order", nullable = false, updatable = false, columnDefinition = "integer auto_increment")
//    private Integer menuOrder;
//
//    @Column
//    private String path;
//
//    @Column
//    private boolean active = false;
//
//    public String getId() {
//        return id;
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
//    public PluginPackage getPluginPackage() {
//        return pluginPackage;
//    }
//
//    public void setPluginPackage(PluginPackage pluginPackage) {
//        this.pluginPackage = pluginPackage;
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
//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
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
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
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
//    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//
//    public PluginPackageMenu() {
//        super();
//    }
//
//    public PluginPackageMenu(PluginPackage pluginPackage, String code, String category, String displayName, String path) {
//        this(null, pluginPackage, code, category, pluginPackage.getId(), displayName, null, path);
//    }
//
//    @JsonBackReference
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
//    @JoinColumn(name = "plugin_package_id")
//    private PluginPackage pluginPackage;
//
//    public PluginPackageMenu(String id, PluginPackage pluginPackage, String code, String category, String source, String displayName, String localDisplayName, Integer menuOrder, String path) {
//        this(id, pluginPackage, code, category, source, displayName, localDisplayName, menuOrder, path, false);
//    }
//
//    public PluginPackageMenu(String id, PluginPackage pluginPackage, String code, String category, String source, String displayName, String localDisplayName, Integer menuOrder, String path, boolean status) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.code = code;
//        this.category = category;
//        this.source = source;
//        this.displayName = displayName;
//        this.localDisplayName = localDisplayName;
//        this.menuOrder = menuOrder;
//        this.path = path;
//        this.active = status;
//    }
//
//    public PluginPackageMenu(String id, PluginPackage pluginPackage, String code, String category, String source, String displayName, Integer menuOrder, String path) {
//        this(null, pluginPackage, code, category, source, displayName, displayName, menuOrder, path);
//    }
//
//    @Override
//    public int compareTo(PluginPackageMenu compareObject) {
//        return this.getId().compareTo(compareObject.getId());
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}