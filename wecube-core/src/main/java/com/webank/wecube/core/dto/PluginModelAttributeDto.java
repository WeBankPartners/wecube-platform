package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelAttributeDto {

    private Integer id;
    private Integer entityId;
    private String description;
    private String name;
    private String packageName;
    private String entityName;
    private String dataType;
    private String state = "draft";
    // for service to bind the unpacked reference info
    /**
     * @param pluginModelAttribute input attribute domain object
     * @return attribute dto exposed to the server
     */
    public static PluginModelAttributeDto fromDomain(PluginModelAttribute pluginModelAttribute) {
        PluginModelAttributeDto pluginModelAttributeDto = new PluginModelAttributeDto();
        pluginModelAttributeDto.setName(pluginModelAttribute.getName());
        pluginModelAttributeDto.setEntityName(pluginModelAttribute.getEntityName());
        pluginModelAttributeDto.setPackageName(pluginModelAttribute.getPackageName());
        pluginModelAttributeDto.setDescription(pluginModelAttribute.getDescription());
        pluginModelAttributeDto.setDataType(pluginModelAttribute.getDataType());
        pluginModelAttributeDto.setState(pluginModelAttribute.getState());
        return pluginModelAttributeDto;
    }

    /**
     * @param pluginModelAttributeDto     input attribute dto
     * @param existedPluginModelAttribute existed attribute domain object
     * @return transformed attribute domain object
     */
    public static PluginModelAttribute toDomain(PluginModelAttributeDto pluginModelAttributeDto,
                                                PluginModelAttribute existedPluginModelAttribute) {
        PluginModelAttribute pluginModelAttribute = existedPluginModelAttribute;
        if (pluginModelAttribute == null) {
            pluginModelAttribute = new PluginModelAttribute();
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute.setId(pluginModelAttributeDto.getId());
        }

        if (pluginModelAttributeDto.getEntityId() != null) {
            pluginModelAttribute
                    .setEntityId(pluginModelAttributeDto.getEntityId());
        }

        if (pluginModelAttributeDto.getName() != null) {
            pluginModelAttribute.setName(pluginModelAttributeDto.getName());
        }

        if (pluginModelAttributeDto.getEntityName() != null) {
            pluginModelAttribute.setEntityName(pluginModelAttributeDto.getEntityName());
        }

        if (pluginModelAttributeDto.getPackageName() != null) {
            pluginModelAttribute.setPackageName(pluginModelAttributeDto.getPackageName());
        }

        if (pluginModelAttributeDto.getDescription() != null) {
            pluginModelAttribute.setDescription(pluginModelAttributeDto.getDescription());
        }

        if (pluginModelAttributeDto.getDataType() != null) {
            pluginModelAttribute.setDataType(pluginModelAttributeDto.getDataType());
        }


        if (pluginModelAttributeDto.getState() != null) {
            pluginModelAttribute.setState(pluginModelAttributeDto.getState().toUpperCase());
        }

        return pluginModelAttribute;
    }
}
