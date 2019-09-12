package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.jpa.PluginModelAttributeRepository;
import com.webank.wecube.core.jpa.PluginModelEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PluginModelServiceImpl implements PluginModelService {

    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;
    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;

    /**
     * Plugin model registration
     *
     * @param inputEntityDtos list of plugin model entity dtos
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> register(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> attrDtoList = extractAttributes(inputEntityDtos);

        List<Integer> registeredEntityIdList = new ArrayList<>();
        for (PluginModelEntityDto tmpEntityDto : registerEntity(inputEntityDtos)) {
            registeredEntityIdList.add(tmpEntityDto.getId());
        }
        List<PluginModelAttributeDto> flatAttributeList = setAttributeWithEntityId(attrDtoList, registeredEntityIdList);
        List<PluginModelAttributeDto> pluginModelAttributeDtos = registerAttribute(flatAttributeList);

        return overview();
    }

    /**
     * Plugin model update
     *
     * @param inputEntityDtos list of plugin model entity dtos
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> update(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> attrDtoList = extractAttributes(inputEntityDtos);

        List<Integer> registeredEntityIdList = new ArrayList<>();
        for (PluginModelEntityDto tmpEntityDto : updateEntity(inputEntityDtos)) {
            registeredEntityIdList.add(tmpEntityDto.getId());
        }
        List<PluginModelAttributeDto> flatAttributeList = setAttributeWithEntityId(attrDtoList, registeredEntityIdList);
        List<PluginModelAttributeDto> pluginModelAttributeDtos = updateAttribute(flatAttributeList);

        return overview();
    }

    /**
     * Plugin model overview
     *
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> overview() {
        return convertEntityDomainToDto(pluginModelEntityRepository.findAll());
    }

    /**
     * @param inputEntityDtos extract attribute out from the input entity dto list
     * @return an nested list of attribute dto list
     */
    private List<List<PluginModelAttributeDto>> extractAttributes(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> dtoList = new ArrayList<>();
        inputEntityDtos.forEach(tmpEntityDto -> dtoList.add(tmpEntityDto.getAttributeDtoList()));
        return dtoList;
    }

    /**
     * @param attrDtoList            an nested plugin model attribute dto list
     * @param registeredEntityIdList an entity id list with all registered id in one transaction
     * @return a flat list of all attribute dto list with different entity registered id
     */
    private List<PluginModelAttributeDto> setAttributeWithEntityId(List<List<PluginModelAttributeDto>> attrDtoList, List<Integer> registeredEntityIdList) {
        List<PluginModelAttributeDto> pluginModelAttributeDtos = new ArrayList<>();
        for (int i = 0; i < registeredEntityIdList.size(); i++) {
            Integer registeredId = registeredEntityIdList.get(i);
            for (PluginModelAttributeDto tmp : attrDtoList.get(i)) {
                tmp.setPluginModelEntityId(registeredId);
                pluginModelAttributeDtos.add(tmp);
            }
        }
        return pluginModelAttributeDtos;
    }


    // Entity Operation

    /**
     * Register plugin model entity to wecube
     *
     * @param pluginModelEntityDtos plugin model entity dtos as input
     * @return registered model entities dtos
     */
    private List<PluginModelEntityDto> registerEntity(List<PluginModelEntityDto> pluginModelEntityDtos) {
        Iterable<PluginModelEntity> savedPluginModelEntities = pluginModelEntityRepository.saveAll(convertEntityDtoToDomain(pluginModelEntityDtos));
        return convertEntityDomainToDto(savedPluginModelEntities);
    }

    /**
     * Update plugin model entity to wecube
     *
     * @param pluginModelEntityDtos plugin model entity dtos as input
     * @return updated model entity dtos
     */
    private List<PluginModelEntityDto> updateEntity(List<PluginModelEntityDto> pluginModelEntityDtos) {
        Iterable<PluginModelEntity> savedPluginModelEntities = pluginModelEntityRepository.saveAll(convertEntityDtoToDomain(pluginModelEntityDtos));
        return convertEntityDomainToDto(savedPluginModelEntities);
    }


    /**
     * Convert the plugin model entities from domains to dtos
     *
     * @param savedPluginModelEntity an Iterable pluginModelEntity
     * @return converted dtos
     */
    private List<PluginModelEntityDto> convertEntityDomainToDto(Iterable<PluginModelEntity> savedPluginModelEntity) {
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
    private List<PluginModelEntity> convertEntityDtoToDomain(List<PluginModelEntityDto> pluginModelEntityDtos) {
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

    // Attribute Operation

    /**
     * Register plugin model attribute to the wecube
     *
     * @param pluginModelAttributeDtos plugin model attribute dtos input
     * @return saved plugin model attribute dto
     */
    private List<PluginModelAttributeDto> registerAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        Iterable<PluginModelAttribute> savedPluginModelAttribute = pluginModelAttributeRepository.saveAll(convertAttributeDtoToDomain(pluginModelAttributeDtos));
        return convertAttributeDomainToDto(savedPluginModelAttribute);
    }


    /**
     * Update plugin model attribute to the wecube
     *
     * @param pluginModelAttributeDtos plugin model attribute dtos input
     * @return updated plugin model attribute
     */
    private List<PluginModelAttributeDto> updateAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        Iterable<PluginModelAttribute> updatedPluginModelAttribute = pluginModelAttributeRepository.saveAll(convertAttributeDtoToDomain(pluginModelAttributeDtos));
        return convertAttributeDomainToDto(updatedPluginModelAttribute);
    }

    /**
     * Convert the plugin model attributes from domains to dtos
     *
     * @param savedPluginModelAttribute an Iterable pluginModelAttribute
     * @return converted dtos
     */
    private List<PluginModelAttributeDto> convertAttributeDomainToDto(Iterable<PluginModelAttribute> savedPluginModelAttribute) {
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
    private List<PluginModelAttribute> convertAttributeDtoToDomain(List<PluginModelAttributeDto> pluginModelAttributeDtos) {
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
