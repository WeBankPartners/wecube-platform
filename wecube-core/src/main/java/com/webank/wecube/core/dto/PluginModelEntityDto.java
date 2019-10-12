package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;

import java.util.*;

class TrimmedPluginModelEntityDto {
    private String packageName;
    private String packageVersion;
    private String name;
    private String displayName;


    public TrimmedPluginModelEntityDto(String packageName, String packageVersion, String name, String displayName) {
        this.packageName = packageName;
        this.name = name;
        this.displayName = displayName;
        this.packageVersion = packageVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrimmedPluginModelEntityDto that = (TrimmedPluginModelEntityDto) o;
        return Objects.equals(getPackageName(), that.getPackageName()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getPackageVersion(), that.getPackageVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPackageName(), getName(), getDisplayName(), getPackageVersion());
    }
}

public class PluginModelEntityDto {
    private String packageName;
    private String name;
    private String displayName;
    private String description;
    private String state = "draft";
    // plugin model attribute list
    private String packageVersion;
    private Set<TrimmedPluginModelEntityDto> referenceToEntityList = new HashSet<>();
    private Set<TrimmedPluginModelEntityDto> referenceByEntityList = new HashSet<>();
    private List<PluginModelAttributeDto> attributeDtoList = new ArrayList<>();


    public PluginModelEntityDto(String name, String displayName, String description, String state, List<PluginModelAttributeDto> attributeDtoList) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.state = state;
        this.attributeDtoList = attributeDtoList;
    }

    public PluginModelEntityDto() {
    }

    /**
     * @param pluginModelEntity input entity domain object
     * @return entity dto exposed to the server
     */
    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setPackageName(pluginModelEntity.getPluginPackage().getName());
        pluginModelEntityDto.setName(pluginModelEntity.getName());
        pluginModelEntityDto.setDisplayName(pluginModelEntity.getDisplayName());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setState(pluginModelEntity.getState());
        pluginModelEntityDto.setPackageVersion(pluginModelEntity.getPluginPackage().getVersion());
        if (pluginModelEntity.getPluginModelAttributeList() != null) {
            pluginModelEntity.getPluginModelAttributeList()
                    .forEach(pluginModelAttribute -> pluginModelEntityDto.attributeDtoList
                            .add(PluginModelAttributeDto.fromDomain(pluginModelAttribute)));
        }
        return pluginModelEntityDto;
    }

    /**
     * @param pluginModelEntityDto input entity dto
     * @return transformed entity domain object
     */
    public static PluginModelEntity toDomain(PluginModelEntityDto pluginModelEntityDto) {

        PluginModelEntity pluginModelEntity = new PluginModelEntity();
        if (pluginModelEntityDto.getName() != null) {
            pluginModelEntity.setName(pluginModelEntityDto.getName());
        }

        if (pluginModelEntityDto.getDescription() != null) {
            pluginModelEntity.setDescription(pluginModelEntityDto.getDescription());
        }
        if (pluginModelEntityDto.getState() != null) {
            pluginModelEntity.setState(pluginModelEntityDto.getState().toLowerCase());
        }
        if (pluginModelEntityDto.getDisplayName() != null) {
            pluginModelEntity.setDisplayName(pluginModelEntityDto.getDisplayName());
        }
        if (pluginModelEntityDto.getAttributeDtoList() != null) {
            List<PluginModelAttribute> pluginModelAttributeList = new ArrayList<>();
            for (PluginModelAttributeDto pluginModelAttributeDto : pluginModelEntityDto.getAttributeDtoList()) {
                pluginModelAttributeList.add(PluginModelAttributeDto.toDomain(pluginModelAttributeDto, null, pluginModelEntity));
            }
            pluginModelEntity.setPluginModelAttributeList(pluginModelAttributeList);
        }
        return pluginModelEntity;
    }

    public void updateReferenceBy(String packageName, String packageVersion, String name, String displayName) {
        TrimmedPluginModelEntityDto trimmedPluginModelEntityDto = new TrimmedPluginModelEntityDto(packageName, packageVersion, name, displayName);
        this.referenceByEntityList.add(trimmedPluginModelEntityDto);
    }

    public void updateReferenceTo(String packageName, String packageVersion, String name, String displayName) {
        TrimmedPluginModelEntityDto trimmedPluginModelEntityDto = new TrimmedPluginModelEntityDto(packageName, packageVersion, name, displayName);
        this.referenceToEntityList.add(trimmedPluginModelEntityDto);
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state.toLowerCase();
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public List<PluginModelAttributeDto> getAttributeDtoList() {
        return attributeDtoList;
    }

    public void setAttributeDtoList(List<PluginModelAttributeDto> attributeDtoList) {
        this.attributeDtoList = attributeDtoList;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<TrimmedPluginModelEntityDto> getReferenceToEntityList() {
        return referenceToEntityList;
    }

    public void setReferenceToEntityList(Set<TrimmedPluginModelEntityDto> referenceToEntityList) {
        this.referenceToEntityList = referenceToEntityList;
    }

    public Set<TrimmedPluginModelEntityDto> getReferenceByEntityList() {
        return referenceByEntityList;
    }

    public void setReferenceByEntityList(Set<TrimmedPluginModelEntityDto> referenceByEntityList) {
        this.referenceByEntityList = referenceByEntityList;
    }
}
