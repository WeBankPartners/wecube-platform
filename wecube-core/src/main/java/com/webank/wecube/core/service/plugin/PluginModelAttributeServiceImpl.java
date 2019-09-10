package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.jpa.PluginModelAttributeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of plugin model attribute service
 */
@Service
@Slf4j
@Transactional
public class PluginModelAttributeServiceImpl implements PluginModelAttributeService {


    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;

    @Autowired
    private PluginProperties pluginProperties;

    /**
     * Register plugin model attribute to the wecube
     *
     * @param pluginModelAttributeDtos plugin model attribute dtos input
     * @return saved plugin model attribute dto
     */
    @Override
    public List<PluginModelAttributeDto> registerPluginModelAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        Iterable<PluginModelAttribute> savedPluginModelAttribute = pluginModelAttributeRepository.saveAll(convertPluginModelAttributeDtoToDomain(pluginModelAttributeDtos));
        return convertPluginModelAttributeDomainToDto(savedPluginModelAttribute);
    }


    /**
     * Update plugin model attribute to the wecube
     *
     * @param pluginModelAttributeDtos plugin model attribute dtos input
     * @return updated plugin model attribute
     */
    @Override
    public List<PluginModelAttributeDto> updatePluginModelAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        Iterable<PluginModelAttribute> updatedPluginModelAttribute = pluginModelAttributeRepository.saveAll(convertPluginModelAttributeDtoToDomain(pluginModelAttributeDtos));
        return convertPluginModelAttributeDomainToDto(updatedPluginModelAttribute);
    }

    /**
     * The plugin model attribute overview
     *
     * @return list of all plugin model attributes
     */
    @Override
    public List<PluginModelAttributeDto> PluginModelAttributeOverview() {
        return convertPluginModelAttributeDomainToDto(pluginModelAttributeRepository.findAll());
    }


    /**
     * Convert the plugin model attributes from domains to dtos
     *
     * @param savedPluginModelAttribute an Iterable pluginModelAttribute
     * @return converted dtos
     */
    private List<PluginModelAttributeDto> convertPluginModelAttributeDomainToDto(Iterable<PluginModelAttribute> savedPluginModelAttribute) {
        List<PluginModelAttributeDto> pluginModelAttributeDtos = new ArrayList<>();
        savedPluginModelAttribute.forEach(domain -> pluginModelAttributeDtos.add(PluginModelAttributeDto.fromDomain(domain)));
        return pluginModelAttributeDtos;
    }

    /**
     * Convert the plugin model attributes from dtos to domains
     *
     * @param pluginModelAttributeDtos the list of pluginModelAttributeDto
     * @return the list of converted domains
     */
    private List<PluginModelAttribute> convertPluginModelAttributeDtoToDomain(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        List<PluginModelAttribute> pluginModelAttributes = new ArrayList<>();
        pluginModelAttributeDtos.forEach(dto -> {
            PluginModelAttribute existedPluginModelAttribute = null;
            if (dto.getId() != null) {
                Optional<PluginModelAttribute> tmpPluginModelAttribute = pluginModelAttributeRepository.findById(dto.getId());
                if (tmpPluginModelAttribute.isPresent()) {
                    existedPluginModelAttribute = tmpPluginModelAttribute.get();
                }
            }
            pluginModelAttributes.add(PluginModelAttributeDto.toDomain(dto, existedPluginModelAttribute));
        });
        return pluginModelAttributes;
    }
}
