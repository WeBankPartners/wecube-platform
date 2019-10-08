package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.jpa.PluginModelAttributeRepository;
import com.webank.wecube.core.jpa.PluginModelEntityRepository;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class PluginModelServiceImpl implements PluginModelService {

    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;
    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    private static final Logger logger = LoggerFactory.getLogger(PluginModelServiceImpl.class);

    /**
     * Plugin model registration
     *
     * @param inputEntityDtoList list of plugin model entity dtos with embedded attribute dto list
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> register(List<PluginModelEntityDto> inputEntityDtoList)
            throws WecubeCoreException {

        // map "{package}.{entity}.{attribute}" to another "{package}.{entity}.{attribute}"
        Map<String, String> referenceNameMap = new HashMap<>();
        // map "{package}.{entity}.{attribute}" to attribute domain object
        Map<String, PluginModelAttribute> nameToAttributeMap = new HashMap<>();

        List<PluginModelEntity> cancidateEntityList = new ArrayList<>();

        buildReferenceMapping(inputEntityDtoList, referenceNameMap, nameToAttributeMap,
                cancidateEntityList);

        updateCandidateEntityList(referenceNameMap, nameToAttributeMap, cancidateEntityList);

        return convertEntityDomainToDto(pluginModelEntityRepository.saveAll(cancidateEntityList));
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
     * View one data model entity with its relationship by package name and its versions
     *
     * @param packageName the name of package
     * @param version     the version of the package
     * @return list of entity dto
     */
    @Override
    public List<PluginModelEntityDto> packageView(String packageName, String version) {
        if (version != null) {
            List<PluginModelEntity> pluginModelEntityList = new ArrayList<>();
            Optional<PluginPackage> foundPackageByNameAndVersion = pluginPackageRepository
                    .findByNameAndVersion(packageName, version);
            foundPackageByNameAndVersion.ifPresent(pluginPackage -> pluginModelEntityList
                    .addAll(pluginModelEntityRepository.findAllByPluginPackage(pluginPackage)));
            return convertEntityDomainToDto(pluginModelEntityList);
        }
        return convertEntityDomainToDto(
                pluginModelEntityRepository.findAllByPluginPackage_Name(packageName));
    }

    /**
     * Delete entity, can delete single entity under one package or all entities under one package
     *
     * @param packageName the name of package
     * @param entityNames the name of entity
     */
    @Override
    public void deleteEntity(String packageName, String... entityNames) throws WecubeCoreException {
        if (!StringUtils.isEmpty(packageName)) {
            if (entityNames.length > 0) {
                for (String entityName : entityNames) {
                    if (!StringUtils.isEmpty(entityName)) {
                        // delete specific entity under the package
                        pluginModelEntityRepository.deleteByPluginPackage_NameAndName(packageName, entityName);
                    } else {
                        String msg = "The entity name should not be null or empty";
                        logger.error(msg);
                        if (logger.isDebugEnabled()) {
                            logger
                                    .debug(String
                                            .format("packageName: %s, entityName: %s", packageName, entityName));
                        }
                        throw new WecubeCoreException(msg);
                    }
                }
            } else {
                // delete all entities under same package
                pluginModelEntityRepository.deleteByPluginPackage_Name(packageName);
            }
        } else {
            String msg = "The package name should not be null or empty";
            logger.error(msg);
            if (logger.isDebugEnabled()) {
                logger.debug(String
                        .format("packageName: %s, entityName: %s", packageName,
                                Arrays.toString(entityNames)));
            }
            throw new WecubeCoreException(msg);
        }

    }

    /**
     * Delete attribute, can delete single attribute under one package or all attributes under one
     * package
     *
     * @param entityName     the name of package
     * @param attributeNames the name of entity
     */
    @Override
    public void deleteAttribute(String entityName, String... attributeNames)
            throws WecubeCoreException {
        if (!StringUtils.isEmpty(entityName)) {
            if (attributeNames.length > 0) {
                for (String attributeName : attributeNames) {
                    if (!StringUtils.isEmpty(attributeName)) {
                        // delete specific entity under the package
                        pluginModelAttributeRepository
                                .deleteByPluginModelEntity_NameAndName(entityName, attributeName);
                    } else {
                        String msg = "The attribute name should not be null or empty";
                        logger.error(msg);
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    String
                                            .format("entityName: %s, attributeName: %s", entityName, attributeName));
                        }
                        throw new WecubeCoreException(msg);
                    }

                }
            } else {
                // delete all entities under same package
                pluginModelAttributeRepository.deleteByPluginModelEntity_Name(entityName);
            }
        } else {
            String msg = "The entity name should not be null or empty";
            logger.error(msg);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("entityName: %s, attributeName: %s", entityName,
                        Arrays.toString(attributeNames)));
            }
            throw new WecubeCoreException(msg);
        }
    }

    /**
     * Build reference mapping for package registration
     *
     * @param inputEntityDtoList  the input entity dto list
     * @param referenceNameMap    map "{package}.{entity}.{attribute}" to another "{package}.{entity}.{attribute}"
     * @param nameToAttributeMap  map "{package}.{entity}.{attribute}" to attribute domain object
     * @param cancidateEntityList the candidate entity list, will be inserted to DB later
     * @throws WecubeCoreException when missing reference name, missing datatype or datatype is not equal to "ref"
     */
    private void buildReferenceMapping(List<PluginModelEntityDto> inputEntityDtoList,
                                       Map<String, String> referenceNameMap, Map<String, PluginModelAttribute> nameToAttributeMap,
                                       List<PluginModelEntity> cancidateEntityList) throws WecubeCoreException {
        for (PluginModelEntityDto inputEntityDto : inputEntityDtoList) {
            // update the referenceNameMap with self and referenceName, the referenceName was transfered into "packageName"."entityName"."attributeName"
            for (PluginModelAttributeDto inputAttributeDto : inputEntityDto.getAttributeDtoList()) {
                if (!StringUtils.isEmpty(inputAttributeDto.getDataType())) {
                    // check the DataType
                    if ("ref".equals(inputAttributeDto.getDataType())) {
                        // if DataType equals "ref"
                        if (!StringUtils.isEmpty(inputAttributeDto.getRefPackageName()) && !StringUtils
                                .isEmpty(inputAttributeDto.getRefEntityName()) && !StringUtils
                                .isEmpty(inputAttributeDto.getRefAttributeName())) {
                            // get the self name and reference name
                            String selfName =
                                    inputEntityDto.getPackageName() + "." + inputEntityDto.getName() + "."
                                            + inputAttributeDto.getName();
                            String referenceName =
                                    inputAttributeDto.getRefPackageName() + "." + inputAttributeDto.getRefEntityName()
                                            + "." + inputAttributeDto.getRefAttributeName();
                            referenceNameMap.put(selfName, referenceName);
                        } else {
                            String msg = String.format(
                                    "The DataType should be set to \"ref\" and reference name should not be empty while registering he package [%s] with version: [%s]",
                                    inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                            logger.error(msg);
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.valueOf(inputAttributeDto));
                            }
                            throw new WecubeCoreException(msg);
                        }
                    }
                } else {
                    String msg = String.format(
                            "The DataType should not be empty or null while registering he package [%s] with version: [%s]",
                            inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                    logger.error(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(inputAttributeDto));
                    }
                    throw new WecubeCoreException(msg);
                }
            }

            // transfer from entity dto to entity domain object
            PluginModelEntity transferedEntity = PluginModelEntityDto.toDomain(inputEntityDto);
            // query the plugin package domain object by package name and version recorded in entity dto
            Optional<PluginPackage> foundPackageByNameAndVersion = pluginPackageRepository
                    .findByNameAndVersion(inputEntityDto.getPackageName(),
                            inputEntityDto.getPackageVersion());
            if (foundPackageByNameAndVersion.isPresent()) {
                transferedEntity.setPluginPackage(foundPackageByNameAndVersion.get());
            } else {
                String msg = String
                        .format("Cannot find the package [%s] with version: [%s] while registering data model",
                                inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }

            for (PluginModelAttribute transferedAttribute : transferedEntity
                    .getPluginModelAttributeList()) {
                // update the transfered attribute domain object with found transfered entity domain object
                transferedAttribute.setPluginModelEntity(transferedEntity);
                String referenceName =
                        inputEntityDto.getPackageName() + "." + inputEntityDto.getName() + "."
                                + transferedAttribute.getName();
                // update the nameToAttribute map with reference name and self
                nameToAttributeMap.put(referenceName, transferedAttribute);
            }

            // add the transfered entity domain object with attribute domain objects into the List
            cancidateEntityList.add(transferedEntity);
        }
    }

    /**
     * Update candidate entity list according to the reference mapping
     *
     * @param referenceNameMap    map "{package}.{entity}.{attribute}" to another "{package}.{entity}.{attribute}"
     * @param nameToAttributeMap  map "{package}.{entity}.{attribute}" to attribute domain object
     * @param cancidateEntityList the candidate entity list, will be inserted to DB later
     * @throws WecubeCoreException when reference name the dto passed is invalid
     */
    private void updateCandidateEntityList(Map<String, String> referenceNameMap,
                                           Map<String, PluginModelAttribute> nameToAttributeMap,
                                           List<PluginModelEntity> cancidateEntityList) throws WecubeCoreException {
        // update the attribtue domain object with pre-noted map
        for (PluginModelEntity pluginModelEntity : cancidateEntityList) {
            for (PluginModelAttribute pluginModelAttribute : pluginModelEntity
                    .getPluginModelAttributeList()) {
                String selfName =
                        pluginModelAttribute.getPluginModelEntity().getPluginPackage().getName() + "."
                                + pluginModelAttribute.getPluginModelEntity().getName() + "." + pluginModelAttribute
                                .getName();
                if (referenceNameMap.containsKey(selfName)) {
                    // only need to assign the attribute to attribute when the selfName is found in referenceNameMap
                    String referenceName = referenceNameMap.get(selfName);
                    // check nameToAttributeMap first, if not exist, then check the database, finally throw the exception
                    if (nameToAttributeMap.containsKey(referenceName)) {
                        PluginModelAttribute referenceAttribute = nameToAttributeMap
                                .get(referenceName);
                        pluginModelAttribute.setPluginModelAttribute(referenceAttribute);
                    } else {
                        // the reference cannot be found in the referenceNameMap, which means it's a cross-package reference
                        // split the crossReferenceName and assign to three parameters
                        String[] splitResult = referenceName.split("\\.");
                        if (splitResult.length == 3) {
                            String referencePackageName = splitResult[0];
                            String referenceEntityName = splitResult[1];
                            String referenceAttributeName = splitResult[2];
                            // found attribute domain object with the seperated three reference names
                            Optional<PluginModelAttribute> foundAttributeList = pluginModelAttributeRepository
                                    .findAllByPluginModelEntity_PluginPackage_NameAndPluginModelEntity_NameAndName(
                                            referencePackageName, referenceEntityName, referenceAttributeName);
                            // the foundEntity should only be one because of the unique key constraint
                            if (foundAttributeList.isPresent()) {
                                pluginModelAttribute.setPluginModelAttribute(foundAttributeList.get());
                            } else {
                                String msg = String.format(
                                        "Cannot found the specified plugin model entity with package name: [%s], entity name: [%s] and attribute name: [%s]",
                                        referencePackageName, referenceEntityName, referenceAttributeName);
                                logger.error(msg);
                                throw new WecubeCoreException(msg);
                            }
                        } else {
                            String msg = String.format("The reference name [%s] is illegal", referenceName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
                    }
                }
            }
        }
    }

    /**
     * Convert the plugin model entities from domains to dtos
     *
     * @param savedPluginModelEntity an Iterable pluginModelEntity
     * @return converted dtos
     */
    private List<PluginModelEntityDto> convertEntityDomainToDto(
            Iterable<PluginModelEntity> savedPluginModelEntity) {
        List<PluginModelEntityDto> pluginModelEntityDtos = new ArrayList<>();
        savedPluginModelEntity
                .forEach(domain -> pluginModelEntityDtos.add(PluginModelEntityDto.fromDomain(domain)));
        return pluginModelEntityDtos;
    }

}
