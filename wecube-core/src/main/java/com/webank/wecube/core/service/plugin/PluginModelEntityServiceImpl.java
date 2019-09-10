package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.jpa.PluginModelEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of plugin model entity service
 */
@Service
@Slf4j
@Transactional
public class PluginModelEntityServiceImpl implements PluginModelEntityService {

    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;

    /**
     * Register plugin model entity to wecube
     *
     * @param pluginModelEntityDtos plugin model entity dtos as input
     * @return registered model entities dtos
     */
    @Override
    public List<PluginModelEntityDto> register(List<PluginModelEntityDto> pluginModelEntityDtos) {
        Iterable<PluginModelEntity> savedPluginModelEntities = pluginModelEntityRepository.saveAll(convertPluginModelEntityDtoToDomain(pluginModelEntityDtos));
        return convertPluginModelEntityDomainToDto(savedPluginModelEntities);
    }

    /**
     * Update plugin model entity to wecube
     *
     * @param pluginModelEntityDtos plugin model entity dtos as input
     * @return updated model entity dtos
     */
    @Override
    public List<PluginModelEntityDto> update(List<PluginModelEntityDto> pluginModelEntityDtos) {
        Iterable<PluginModelEntity> savedPluginModelEntities = pluginModelEntityRepository.saveAll(convertPluginModelEntityDtoToDomain(pluginModelEntityDtos));
        return convertPluginModelEntityDomainToDto(savedPluginModelEntities);
    }

    /**
     * Return the plugin model entity overview
     *
     * @return list of all model entities
     */
    @Override
    public List<PluginModelEntityDto> overview() {
        return convertPluginModelEntityDomainToDto(pluginModelEntityRepository.findAll());
    }

    /**
     * Convert the plugin model entities from domains to dtos
     *
     * @param savedPluginModelEntity an Iterable pluginModelEntity
     * @return converted dtos
     */
    private List<PluginModelEntityDto> convertPluginModelEntityDomainToDto(Iterable<PluginModelEntity> savedPluginModelEntity) {
        List<PluginModelEntityDto> pluginModelEntityDtos = new ArrayList<>();
        savedPluginModelEntity.forEach(domain -> pluginModelEntityDtos.add(PluginModelEntityDto.fromDomain(domain)));
        return pluginModelEntityDtos;
    }

    /**
     * Convert the plugin model entities from dtos to domains
     *
     * @param pluginModelEntityDtos the list of pluginModelEntityDto
     * @return the list of converted domains
     */
    private List<PluginModelEntity> convertPluginModelEntityDtoToDomain(List<PluginModelEntityDto> pluginModelEntityDtos) {
        List<PluginModelEntity> pluginModelEntities = new ArrayList<>();
        pluginModelEntityDtos.forEach(dto -> {
            PluginModelEntity existedPluginModelEntity = null;
            if (dto.getId() != null) {
                Optional<PluginModelEntity> tmpPluginModelEntity = pluginModelEntityRepository.findById(dto.getId());
                if (tmpPluginModelEntity.isPresent()) {
                    existedPluginModelEntity = tmpPluginModelEntity.get();
                }
            }
            pluginModelEntities.add(PluginModelEntityDto.toDomain(dto, existedPluginModelEntity));
        });
        return pluginModelEntities;
    }
}
