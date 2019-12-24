package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;

public class MenuItemDto implements Comparable<MenuItemDto> {

    private String id;
    private String category;
    private String code;
    private String source;
    private Integer menuOrder;
    private String displayName;
    private String cnDisplayName;
    private String path;

    public MenuItemDto(String id, String category, String code, String source, Integer menuOrder, String displayName, String cnDisplayName, String path) {
        this.id = id;
        this.category = category;
        this.code = code;
        this.source = source;
        this.menuOrder = menuOrder;
        this.displayName = displayName;
        this.cnDisplayName = cnDisplayName;
        this.path = path;
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
        pluginPackageMenuDto.setCnDisplayName(systemMenu.getCnDisplayName());
        pluginPackageMenuDto.setPath(null);
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
        pluginPackageMenuDto.setCnDisplayName(packageMenu.getCnDisplayName());
        pluginPackageMenuDto.setPath(packageMenu.getPath());
        return pluginPackageMenuDto;
    }

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

    public String getCnDisplayName() {
        return cnDisplayName;
    }

    public void setCnDisplayName(String cnDisplayName) {
        this.cnDisplayName = cnDisplayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public int compareTo(MenuItemDto o) {
        return this.getMenuOrder().compareTo(o.getMenuOrder());
    }
}
