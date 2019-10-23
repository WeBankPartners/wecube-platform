package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;

public class PluginPackageMenuDto {

  private Integer id;
  private String category;
  private String code;
  private String displayName;
  private String path;
  private String menuState;

  public PluginPackageMenuDto(Integer id, String category, String code, String displayName,
      String path, String menuState) {
    this.id = id;
    this.category = category;
    this.code = code;
    this.displayName = displayName;
    this.path = path;
    this.menuState = menuState;
  }

  public PluginPackageMenuDto() {
  }

  public static PluginPackageMenuDto fromCoreMenuItem(MenuItem systemMenu) {
    PluginPackageMenuDto pluginPackageMenuDto = new PluginPackageMenuDto();
    pluginPackageMenuDto.setId(systemMenu.getId());
    Integer category = systemMenu.getParentId();
    if (category != null) {
      pluginPackageMenuDto.setCategory(category.toString());
    }
    pluginPackageMenuDto.setCode(systemMenu.getCode());
    pluginPackageMenuDto.setDisplayName(systemMenu.getDescription());
    pluginPackageMenuDto.setPath(null);
    pluginPackageMenuDto.setMenuState("system");
    return pluginPackageMenuDto;
  }

  public static PluginPackageMenuDto fromPackageMenuItem(PluginPackageMenu packageMenu) {
    PluginPackageMenuDto pluginPackageMenuDto = new PluginPackageMenuDto();
    pluginPackageMenuDto.setId(packageMenu.getId());
    pluginPackageMenuDto.setCategory(packageMenu.getCategory());
    pluginPackageMenuDto.setCode(packageMenu.getCode());
    pluginPackageMenuDto.setDisplayName(packageMenu.getDisplayName());
    pluginPackageMenuDto.setPath(packageMenu.getPath());
    pluginPackageMenuDto.setMenuState("package");
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

  public String getMenuState() {
    return menuState;
  }

  public void setMenuState(String menuState) {
    this.menuState = menuState;
  }
}
