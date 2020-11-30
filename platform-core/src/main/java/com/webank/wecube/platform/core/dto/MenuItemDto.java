package com.webank.wecube.platform.core.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;

public class MenuItemDto implements Comparable<MenuItemDto> {

    private String id;
    private String category;
    private String code;
    private String source;
    private Integer menuOrder;
    private String displayName;
    private String localDisplayName;
    private String path;
    private Boolean active;

    public MenuItemDto(String id, String category, String code, String source, Integer menuOrder, String displayName, String localDisplayName, String path, boolean active) {
        this.id = id;
        this.category = category;
        this.code = code;
        this.source = source;
        this.menuOrder = menuOrder;
        this.displayName = displayName;
        this.localDisplayName = localDisplayName;
        this.path = path;
        this.active = active;
    }

    public MenuItemDto() {
    }

    public static MenuItemDto fromSystemMenuItem(MenuItem systemMenu) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(systemMenu.getId());
        String category = systemMenu.getParentCode();
        if (category != null) {
            pluginPackageMenuDto.setCategory(category);
        }
        pluginPackageMenuDto.setCode(systemMenu.getCode());
        pluginPackageMenuDto.setSource(systemMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(systemMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(systemMenu.getDescription());
        pluginPackageMenuDto.setLocalDisplayName(systemMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(null);
        pluginPackageMenuDto.setActive(true);
        return pluginPackageMenuDto;
    }

    public static MenuItemDto fromPackageMenuItem(PluginPackageMenu packageMenu, MenuItem menuItem) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(packageMenu.getId());
        pluginPackageMenuDto.setCategory(packageMenu.getCategory());
        pluginPackageMenuDto.setCode(packageMenu.getCode());
        pluginPackageMenuDto.setSource(packageMenu.getSource());
        pluginPackageMenuDto.setMenuOrder(menuItem.getMenuOrder() * 10000 + packageMenu.getMenuOrder());
        pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
        pluginPackageMenuDto.setLocalDisplayName(packageMenu.getLocalDisplayName());
        pluginPackageMenuDto.setPath(packageMenu.getPath());
        pluginPackageMenuDto.setActive(packageMenu.isActive());
        return pluginPackageMenuDto;
    }

//    public static MenuItemDto fromPackageMenuItem(LazyPluginPackageMenu packageMenu, MenuItem menuItem) {
//        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
//        pluginPackageMenuDto.setId(packageMenu.getId());
//        pluginPackageMenuDto.setCategory(packageMenu.getCategory());
//        pluginPackageMenuDto.setCode(packageMenu.getCode());
//        pluginPackageMenuDto.setSource(packageMenu.getSource());
//        pluginPackageMenuDto.setMenuOrder(menuItem.getMenuOrder() * 10000 + packageMenu.getMenuOrder());
//        pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
//        pluginPackageMenuDto.setLocalDisplayName(packageMenu.getLocalDisplayName());
//        pluginPackageMenuDto.setPath(packageMenu.getPath());
//        pluginPackageMenuDto.setActive(packageMenu.isActive());
//        return pluginPackageMenuDto;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocalDisplayName() {
        return localDisplayName;
    }

    public void setLocalDisplayName(String localDisplayName) {
        this.localDisplayName = localDisplayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(MenuItemDto o) {
        return this.getMenuOrder().compareTo(o.getMenuOrder());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .toString();
    }
}
