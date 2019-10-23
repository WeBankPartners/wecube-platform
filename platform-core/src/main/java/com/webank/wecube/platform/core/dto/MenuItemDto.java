package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;

public class MenuItemDto {

    private Integer id;
    private String category;
    private String code;
    private String displayName;
    private String path;
    private String menuType;

    public MenuItemDto(Integer id, String category, String code, String displayName,
                                String path, String menuType) {
        this.id = id;
        this.category = category;
        this.code = code;
        this.displayName = displayName;
        this.path = path;
        this.menuType = menuType;
    }

    public MenuItemDto() {
    }

    public static MenuItemDto fromSystemMenuItem(MenuItem systemMenu) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(systemMenu.getId());
        Integer category = systemMenu.getParentId();
        if (category != null) {
            pluginPackageMenuDto.setCategory(category.toString());
        }
        pluginPackageMenuDto.setCode(systemMenu.getCode());
        pluginPackageMenuDto.setDisplayName(systemMenu.getDescription());
        pluginPackageMenuDto.setPath(null);
        pluginPackageMenuDto.setMenuType("system");
        return pluginPackageMenuDto;
    }

    public static MenuItemDto fromPackageMenuItem(PluginPackageMenu packageMenu) {
        MenuItemDto pluginPackageMenuDto = new MenuItemDto();
        pluginPackageMenuDto.setId(packageMenu.getId());
        pluginPackageMenuDto.setCategory(packageMenu.getCategory());
        pluginPackageMenuDto.setCode(packageMenu.getCode());
        pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
        pluginPackageMenuDto.setPath(packageMenu.getPath());
        pluginPackageMenuDto.setMenuType("package");
        return pluginPackageMenuDto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }
}
