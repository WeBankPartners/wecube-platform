package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.jpa.PluginModelAttributeRepository;
import com.webank.wecube.core.jpa.PluginModelEntityRepository;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@Transactional
public class PluginModelServiceImpl implements PluginModelService {

    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;
    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    /**
     * Plugin model registration
     *
     * @param inputEntityDtoList list of plugin model entity dtos with embedded attribute dto list
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> register(List<PluginModelEntityDto> inputEntityDtoList) {
        // map "{entity}.{attribute}" the reference, another "{entity}.{attribute}"
        Map<String, String> referenceNameMap = new HashMap<>();
        // map "{entity}.{attribute}" to attribute domain object
        Map<String, PluginModelAttribute> nameToAttributeMap = new HashMap<>();

        List<PluginModelEntity> pluginModelEntityList = new ArrayList<>();

        // initialize the entity domain object and its nested attribute domain object
        // also this for-loop build up the maps
        for (PluginModelEntityDto entityDto : inputEntityDtoList) {

            // build referenceName map
            for (PluginModelAttributeDto attrDto : entityDto.getAttributeDtoList()) {
                if (attrDto.getPackageName() != null && attrDto.getEntityName() != null && attrDto.getName() != null) {
                    if (pluginPackageRepository.findLatestVersionByName(entityDto.getPackageName(), entityDto.getPackageVersion()).isPresent()) {
                        String packageName = pluginPackageRepository.findLatestVersionByName(entityDto.getPackageName(), entityDto.getPackageVersion()).get().getName();
                        referenceNameMap.put(
                                packageName + "." + entityDto.getName() + "." + attrDto.getName(),
                                attrDto.getPackageName() + "." + attrDto.getEntityName() + "." + attrDto.getName());
                    } else {
                        throw new WecubeCoreException(String.format("Cannot find entity by package name: [%s] with version: [%s]", entityDto.getPackageName(), entityDto.getPackageVersion()));
                    }
                }
            }

            // dto -> domain
            PluginModelEntity tmp = PluginModelEntityDto.toDomain(entityDto);

            // build nameToAttributeMap
            for (PluginModelAttribute attr : tmp.getPluginModelAttributeList()) {
                nameToAttributeMap.put(
                        tmp.getPackageName() + tmp.getName() + "." + attr.getName(),
                        attr);
                attr.setPluginModelEntity(tmp);
            }

            pluginModelEntityList.add(tmp);
        }

        // find the reference in nameToAttributeMap from referenceNameMap
        for (PluginModelEntity pluginModelEntity : pluginModelEntityList) {
            for (PluginModelAttribute pluginModelAttribute : pluginModelEntity.getPluginModelAttributeList()) {
                // this would set the attribute reference to the domain object pre-recorded in nameToAttributeMap
                pluginModelAttribute.setPluginModelAttribute(
                        nameToAttributeMap.get(
                                referenceNameMap.get(
                                        pluginModelEntity.getPackageName() + "." + pluginModelEntity.getName() + "." + pluginModelAttribute.getName()
                                )
                        )
                );
            }
        }
        return convertEntityDomainToDto(pluginModelEntityRepository.saveAll(pluginModelEntityList));


    }

    /**
     * @param inputEntityDtoList input dto list that need to be updated
     * @return updated entity dto list
     */
    @Override
    public List<PluginModelEntityDto> update(List<PluginModelEntityDto> inputEntityDtoList) {
        List<PluginModelEntity> pluginModelEntityList = new ArrayList<>();
        for (PluginModelEntityDto tmpEntityDto :
                inputEntityDtoList) {
            pluginModelEntityRepository.findAllByPackageNameAndName(
                    tmpEntityDto.getPackageName(),
                    tmpEntityDto.getName()).ifPresent(foundEntity -> {
                tmpEntityDto.setId(foundEntity.getId());
            });

            for (PluginModelAttributeDto tmpAttributeDto :
                    tmpEntityDto.getAttributeDtoList()) {
                pluginModelAttributeRepository.findAllByPackageNameAndEntityNameAndName(
                        tmpAttributeDto.getPackageName(),
                        tmpAttributeDto.getEntityName(),
                        tmpAttributeDto.getName()).ifPresent(foundAttribute -> {
                    tmpAttributeDto.setId(foundAttribute.getId());
                });
            }
            pluginModelEntityList.add(PluginModelEntityDto.toDomain(tmpEntityDto));
        }
        return convertEntityDomainToDto(pluginModelEntityRepository.saveAll(pluginModelEntityList));
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
     * @param packageName the name of package
     * @param version     set of versions
     * @return list of entity dto
     */
    @Override
    public List<PluginModelEntityDto> overview(String packageName, String... version) {
        List<PluginModelEntity> pluginModelEntityList = new ArrayList<>();
        if (pluginPackageRepository.findLatestVersionByName(packageName, version).isPresent()) {
            Integer packageId = pluginPackageRepository.findLatestVersionByName(packageName, version).get().getId();
            pluginModelEntityList.addAll(pluginModelEntityRepository.findAllByPackageId(packageId));
        } else {
            throw new WecubeCoreException(String.format("Cannot find entity by package name: [%s] with version: [%s]", packageName, Arrays.toString(version)));
        }
        return convertEntityDomainToDto(pluginModelEntityList);
    }

    /**
     * @param packageName the name of package
     * @param entityNames the name of entity
     */
    @Override
    public void deleteEntity(String packageName, String... entityNames) {
        if (pluginPackageRepository.findLatestVersionByName(packageName).isPresent()) {
            Integer packageId = pluginPackageRepository.findLatestVersionByName(packageName).get().getId();
            if (entityNames != null) {
                for (String entityName : entityNames) {
                    // delete specific entity under the package
                    pluginModelEntityRepository.delete(packageId, entityName);
                }
            } else {
                // delete all entities under same package
                pluginModelEntityRepository.delete(packageId);
            }
        } else {
            throw new WecubeCoreException(String.format("Cannot find entity by package name: [%s]", packageName));
        }
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


}
