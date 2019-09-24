package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelEntityDto {
    private Integer id;
    private Integer packageId;
    private String description;
    private String name;
    private String state = "draft";
    // plugin model attribute list
    private String packageName;
    private String packageVersion;
    private List<PluginModelAttributeDto> attributeDtoList;

    /**
     * @param pluginModelEntity input entity domain object
     * @return entity dto exposed to the server
     */
    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setName(pluginModelEntity.getName());
        pluginModelEntityDto.setPackageName(pluginModelEntity.getPackageName());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setState(pluginModelEntity.getState());
        if (pluginModelEntity.getPluginModelAttributeList() != null) {
            pluginModelEntity.getPluginModelAttributeList()
                    .forEach(pluginModelAttribute -> pluginModelEntityDto.attributeDtoList
                            .add(PluginModelAttributeDto.fromDomain(pluginModelAttribute)));
        }
//        pluginModelEntityDto.setPackageName(pluginModelEntity.getPackageId());

        return pluginModelEntityDto;
    }

    /**
     * @param pluginModelEntityDto input entity dto
     * @return transformed entity domain object
     */
    public static PluginModelEntity toDomain(PluginModelEntityDto pluginModelEntityDto) {
        PluginModelEntity pluginModelEntity = new PluginModelEntity();

        if (pluginModelEntityDto.getId() != null) {
            pluginModelEntity.setId(pluginModelEntityDto.getId());
        }
        if (pluginModelEntityDto.getPackageId() != null) {
            pluginModelEntity.setPackageId(pluginModelEntityDto.getPackageId());
        }
        if (pluginModelEntityDto.getName() != null) {
            pluginModelEntity.setName(pluginModelEntityDto.getName());
        }
        if (pluginModelEntityDto.getPackageName() != null) {
            pluginModelEntity.setPackageName(pluginModelEntityDto.getPackageName());
        }
        if (pluginModelEntityDto.getPackageVersion() != null) {
            pluginModelEntity.setPackageVersion(pluginModelEntityDto.getPackageVersion());
        }
        if (pluginModelEntityDto.getDescription() != null) {
            pluginModelEntity.setDescription(pluginModelEntityDto.getDescription());
        }
        if (pluginModelEntityDto.getState() != null) {
            pluginModelEntity.setState(pluginModelEntityDto.getState().toUpperCase());
        }
        if (pluginModelEntityDto.getAttributeDtoList() != null) {
            List<PluginModelAttribute> pluginModelAttributeList = new ArrayList<>();
            for (PluginModelAttributeDto pluginModelAttributeDto : pluginModelEntityDto.getAttributeDtoList()) {
                pluginModelAttributeList.add(PluginModelAttributeDto.toDomain(pluginModelAttributeDto, null));
            }
            pluginModelEntity.setPluginModelAttributeList(pluginModelAttributeList);
        }
        return pluginModelEntity;
    }
}
