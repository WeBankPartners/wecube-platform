package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TrimmedPluginPackageEntityDto {
    private Integer id;
    private String packageName;
    private String packageVersion;
    private String name;
    private String displayName;


    public TrimmedPluginPackageEntityDto(Integer entityId, String packageName, String packageVersion, String name, String displayName) {
        this.id = entityId;
        this.packageName = packageName;
        this.name = name;
        this.displayName = displayName;
        this.packageVersion = packageVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        TrimmedPluginPackageEntityDto that = (TrimmedPluginPackageEntityDto) o;

        return new EqualsBuilder()
                .append(getPackageName(), that.getPackageName())
                .append(getName(), that.getName())
                .append(getDisplayName(), that.getDisplayName())
                .append(getPackageVersion(), that.getPackageVersion())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getPackageName())
                .append(getName())
                .append(getDisplayName())
                .append(getPackageName())
                .toHashCode();
    }
}

public class PluginPackageEntityDto {
    private Integer id;
    private String packageName;
    private String name;
    private String displayName;
    private String description;

    private String packageVersion;
    private Set<TrimmedPluginPackageEntityDto> referenceToEntityList = new HashSet<>();
    private Set<TrimmedPluginPackageEntityDto> referenceByEntityList = new HashSet<>();
    private List<PluginPackageAttributeDto> attributeDtoList = new ArrayList<>();


    public PluginPackageEntityDto(Integer id, String name, String displayName, String description, String state, List<PluginPackageAttributeDto> attributeDtoList) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.attributeDtoList = attributeDtoList;
    }

    public PluginPackageEntityDto() {
    }

    /**
     * @param pluginPackageEntity input entity domain object
     * @return entity dto exposed to the server
     */
    public static PluginPackageEntityDto fromDomain(PluginPackageEntity pluginPackageEntity) {
        PluginPackageEntityDto pluginPackageEntityDto = new PluginPackageEntityDto();
        pluginPackageEntityDto.setId(pluginPackageEntity.getId());
        pluginPackageEntityDto.setPackageName(pluginPackageEntity.getPluginPackage().getName());
        pluginPackageEntityDto.setName(pluginPackageEntity.getName());
        pluginPackageEntityDto.setDisplayName(pluginPackageEntity.getDisplayName());
        pluginPackageEntityDto.setDescription(pluginPackageEntity.getDescription());
        pluginPackageEntityDto.setPackageVersion(pluginPackageEntity.getPluginPackage().getVersion());
        if (pluginPackageEntity.getPluginPackageAttributeList() != null) {
            pluginPackageEntity.getPluginPackageAttributeList()
                    .forEach(pluginPackageAttribute -> pluginPackageEntityDto.attributeDtoList
                            .add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        }
        return pluginPackageEntityDto;
    }

    /**
     * @param pluginPackageEntityDto input entity dto
     * @return transformed entity domain object
     */
    public static PluginPackageEntity toDomain(PluginPackageEntityDto pluginPackageEntityDto) {

        PluginPackageEntity pluginPackageEntity = new PluginPackageEntity();
        if (pluginPackageEntityDto.getName() != null) {
            pluginPackageEntity.setName(pluginPackageEntityDto.getName());
        }

        if (pluginPackageEntityDto.getDescription() != null) {
            pluginPackageEntity.setDescription(pluginPackageEntityDto.getDescription());
        }

        if (pluginPackageEntityDto.getDisplayName() != null) {
            pluginPackageEntity.setDisplayName(pluginPackageEntityDto.getDisplayName());
        }
        if (pluginPackageEntityDto.getAttributeDtoList() != null) {
            List<PluginPackageAttribute> pluginPackageAttributeList = new ArrayList<>();
            for (PluginPackageAttributeDto pluginPackageAttributeDto : pluginPackageEntityDto.getAttributeDtoList()) {
                pluginPackageAttributeList.add(PluginPackageAttributeDto.toDomain(pluginPackageAttributeDto, null, pluginPackageEntity));
            }
            pluginPackageEntity.setPluginPackageAttributeList(pluginPackageAttributeList);
        }
        return pluginPackageEntity;
    }

    /**
     * @return transformed entity domain object
     */
    public PluginPackageEntity toDomain(PluginPackage pluginPackage) {
        PluginPackageEntity pluginPackageEntity = new PluginPackageEntity();
        pluginPackageEntity.setPluginPackage(pluginPackage);

        if (this.getName() != null) {
            pluginPackageEntity.setName(this.getName());
        }

        if (this.getDescription() != null) {
            pluginPackageEntity.setDescription(this.getDescription());
        }

        if (this.getDisplayName() != null) {
            pluginPackageEntity.setDisplayName(this.getDisplayName());
        }
        if (this.getAttributeDtoList() != null) {
            List<PluginPackageAttribute> pluginPackageAttributeList = new ArrayList<>();
            for (PluginPackageAttributeDto pluginPackageAttributeDto : this.getAttributeDtoList()) {
                pluginPackageAttributeList.add(PluginPackageAttributeDto.toDomain(pluginPackageAttributeDto, null, pluginPackageEntity));
            }
            pluginPackageEntity.setPluginPackageAttributeList(pluginPackageAttributeList);
        }
        return pluginPackageEntity;
    }

    public void updateReferenceBy(Integer entityId, String packageName, String packageVersion, String name, String displayName) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId, packageName, packageVersion, name, displayName);
        this.referenceByEntityList.add(trimmedPluginPackageEntityDto);
    }

    public void updateReferenceTo(Integer entityId, String packageName, String packageVersion, String name, String displayName) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId, packageName, packageVersion, name, displayName);
        this.referenceToEntityList.add(trimmedPluginPackageEntityDto);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public List<PluginPackageAttributeDto> getAttributeDtoList() {
        return attributeDtoList;
    }

    public void setAttributeDtoList(List<PluginPackageAttributeDto> attributeDtoList) {
        this.attributeDtoList = attributeDtoList;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<TrimmedPluginPackageEntityDto> getReferenceToEntityList() {
        return referenceToEntityList;
    }

    public void setReferenceToEntityList(Set<TrimmedPluginPackageEntityDto> referenceToEntityList) {
        this.referenceToEntityList = referenceToEntityList;
    }

    public Set<TrimmedPluginPackageEntityDto> getReferenceByEntityList() {
        return referenceByEntityList;
    }

    public void setReferenceByEntityList(Set<TrimmedPluginPackageEntityDto> referenceByEntityList) {
        this.referenceByEntityList = referenceByEntityList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
