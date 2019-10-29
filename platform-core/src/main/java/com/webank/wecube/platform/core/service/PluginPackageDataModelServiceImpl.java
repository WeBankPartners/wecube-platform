package com.webank.wecube.platform.core.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class PluginPackageDataModelServiceImpl implements PluginPackageDataModelService {

    @Autowired
    PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    PluginPackageAttributeRepository pluginPackageAttributeRepository;
    @Autowired
    PluginPackageRepository pluginPackageRepository;

    private static final Logger logger = LoggerFactory.getLogger(PluginPackageDataModelServiceImpl.class);

    /**
     * Plugin model registration
     *
     * @param inputEntityDtoList list of plugin model entity dtos with embedded attribute dto list
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginPackageEntityDto> register(List<PluginPackageEntityDto> inputEntityDtoList)
            throws WecubeCoreException {

        // map "{package}`{version}`{entity}`{attribute}" to another "{package}`{version}`{entity}`{attribute}"
        Map<String, String> referenceNameMap = new HashMap<>();
        // map "{package}`{version}`{entity}`{attribute}" to attribute domain object
        Map<String, PluginPackageAttribute> nameToAttributeMap = new HashMap<>();

        List<PluginPackageEntity> candidateEntityList = new ArrayList<>();

        buildReferenceMapping(inputEntityDtoList, referenceNameMap, nameToAttributeMap, candidateEntityList);

        updateCandidateEntityList(referenceNameMap, nameToAttributeMap, candidateEntityList);

        return convertEntityDomainToDto(pluginPackageEntityRepository.saveAll(candidateEntityList));
    }


    /**
     * Plugin model overview
     *
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginPackageEntityDto> overview() {
        return convertEntityDomainToDto(pluginPackageEntityRepository.findAll());
    }

    /**
     * View one data model entity with its relationship by package name and its versions
     *
     * @param packageName the name of package
     * @param version     the version of the package
     * @return list of entity dto
     */
    @Override
    public List<PluginPackageEntityDto> packageView(String packageName, String version) throws WecubeCoreException {
        // check if the package name is empty or null first
        if (StringUtils.isEmpty(packageName)) {
            String msg = String.format("The package name: [%s] should not be null or empty.", packageName);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        // then check the version
        if (StringUtils.isEmpty(version)) {
            // if version is empty or null, then can return all the data model according to the give package name, ignore the version
            Optional<List<PluginPackageEntity>> allByPluginPackage_name = pluginPackageEntityRepository.findAllByPluginPackage_Name(packageName);
            if (!allByPluginPackage_name.isPresent()) {
                String msg = String.format("Cannot find datamodel according to packageName: [%s].", packageName);
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
            List<PluginPackageEntity> pluginPackageEntityList = allByPluginPackage_name.get();
            return convertEntityDomainToDto(pluginPackageEntityList);
        }

        // give package name and version are all not empty or not null
        Optional<List<PluginPackageEntity>> allByPluginPackage_nameAndPluginPackage_version = pluginPackageEntityRepository.findAllByPluginPackage_NameAndPluginPackage_Version(packageName, version);
        if (!allByPluginPackage_nameAndPluginPackage_version.isPresent()) {
            String msg = String.format("Cannot find datamodel according to packageName: [%s] and version: [%s].", packageName, version);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        List<PluginPackageEntity> pluginPackageEntityList = allByPluginPackage_nameAndPluginPackage_version.get();
        return convertEntityDomainToDto(pluginPackageEntityList);
    }

    /**
     * View one data model entity with its relationship by packageId
     *
     * @param packageId the name of package
     * @return list of entity dto
     */
    @Override
    public List<PluginPackageEntityDto> packageView(int packageId) throws WecubeCoreException {
        Optional<List<PluginPackageEntity>> allByPluginPackage_id = pluginPackageEntityRepository.findAllByPluginPackage_Id(packageId);
        if (!allByPluginPackage_id.isPresent()) {
            String msg = String.format("The data model of package ID: %d cannot be found.", packageId);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        List<PluginPackageEntity> pluginPackageEntityList = allByPluginPackage_id.get();
        return convertEntityDomainToDto(pluginPackageEntityList);
    }

    /**
     * Delete one or multiple entity by package name and its version
     *
     * @param packageName the name of package
     * @param version     version of package
     */
    @Override
    public void deleteModel(String packageName, String version) throws WecubeCoreException {
        if (StringUtils.isEmpty(packageName) || StringUtils.isEmpty(version)) {
            String msg = "The package name or version should not be null or empty";
            logger.error(msg);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("packageName: %s, version: %s", packageName, version));
            }
            throw new WecubeCoreException(msg);
        }

        // if the attribute the user want to delete is still referenced by other attributes, the delete operation will terminate
        Optional<List<PluginPackageAttribute>> allByPackageNameAndVersion = pluginPackageAttributeRepository.findAllReferenceByAttribute(packageName, version);
        allByPackageNameAndVersion.ifPresent(pluginPackageAttributeList -> {
            for (PluginPackageAttribute pluginPackageAttribute : pluginPackageAttributeList) {
                String entityName = pluginPackageAttribute.getPluginPackageEntity().getName();
                String attributeName = pluginPackageAttribute.getName();
                long allReferenceCount = pluginPackageAttributeRepository
                        .countAllReferenceByAttribute(
                                packageName, version, entityName, attributeName);
                if (allReferenceCount > 0) {
                    String msg = String.format("The attribute: [%s] from Package: [%s] with Version: [%s] and Entity: [%s]  is still referenced by others, delete operation will terminate.", attributeName, packageName, version, entityName);
                    logger.error(msg);
                    throw new WecubeCoreException(msg);
                }
            }
        });

        pluginPackageEntityRepository.deleteByPluginPackage_NameAndPluginPackage_Version(packageName, version);

    }

    /**
     * Build reference mapping for package registration
     *
     * @param inputEntityDtoList  the input entity dto list
     * @param referenceNameMap    map "{package}`{version}`{entity}`{attribute}" to another "{package}`{version}`{entity}`{attribute}"
     * @param nameToAttributeMap  map "{package}`{version}`{entity}`{attribute}" to attribute domain object
     * @param candidateEntityList the candidate entity list, will be inserted to DB later
     * @throws WecubeCoreException when missing reference name, missing datatype or datatype is not equal to "ref"
     */
    private void buildReferenceMapping(List<PluginPackageEntityDto> inputEntityDtoList,
                                       Map<String, String> referenceNameMap, Map<String, PluginPackageAttribute> nameToAttributeMap,
                                       List<PluginPackageEntity> candidateEntityList) throws WecubeCoreException {
        for (PluginPackageEntityDto inputEntityDto : inputEntityDtoList) {
            // update the referenceNameMap with self and referenceName, the referenceName was transfered into "packageName"."entityName"."attributeName"
            for (PluginPackageAttributeDto inputAttributeDto : inputEntityDto.getAttributeDtoList()) {
                if (StringUtils.isEmpty(inputAttributeDto.getDataType())) {
                    String msg = String.format(
                            "The DataType should not be empty or null while registering he package [%s] with version: [%s]",
                            inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                    logger.error(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(inputAttributeDto));
                    }
                    throw new WecubeCoreException(msg);
                }
                // check the DataType
                if ("ref".equals(inputAttributeDto.getDataType())) {
                    // if DataType equals "ref"
                    if (StringUtils.isEmpty(inputAttributeDto.getRefPackageName())
                            || StringUtils.isEmpty(inputAttributeDto.getRefPackageVersion())
                            || StringUtils.isEmpty(inputAttributeDto.getRefEntityName())
                            || StringUtils.isEmpty(inputAttributeDto.getRefAttributeName())) {
                        // once the reference name (including packageName, packageVersion, entityName and attributeName should be specified
                        String msg = "All reference field should be specified when [dataType] is set to [\"ref\"]";
                        logger.error(msg);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.valueOf(inputAttributeDto));
                        }
                        throw new WecubeCoreException(msg);
                    }
                    // get the self name and reference name
                    String selfName =
                            inputEntityDto.getPackageName() + "`"
                                    + inputEntityDto.getPackageVersion() + "`"
                                    + inputEntityDto.getName() + "`"
                                    + inputAttributeDto.getName();
                    String referenceName =
                            inputAttributeDto.getRefPackageName() + "`"
                                    + inputAttributeDto.getRefPackageVersion() + "`"
                                    + inputAttributeDto.getRefEntityName() + "`"
                                    + inputAttributeDto.getRefAttributeName();
                    referenceNameMap.put(selfName, referenceName);
                }
            }

            // transfer from entity dto to entity domain object
            PluginPackageEntity transferedEntity = PluginPackageEntityDto.toDomain(inputEntityDto);
            // query the plugin package domain object by package name and version recorded in entity dto
            Optional<PluginPackage> foundPackageByNameAndVersion = pluginPackageRepository.findByNameAndVersion(
                    inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());

            if (!foundPackageByNameAndVersion.isPresent()) {
                String msg = String.format("Cannot find the package [%s] with version: [%s] while registering data model",
                        inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }

            transferedEntity.setPluginPackage(foundPackageByNameAndVersion.get());

            for (PluginPackageAttribute transferedAttribute : transferedEntity
                    .getPluginPackageAttributeList()) {
                // update the transfered attribute domain object with found transfered entity domain object
                transferedAttribute.setPluginPackageEntity(transferedEntity);
                String referenceName =
                        inputEntityDto.getPackageName() + "`"
                                + inputEntityDto.getPackageVersion() + "`"
                                + inputEntityDto.getName() + "`"
                                + transferedAttribute.getName();
                // update the nameToAttribute map with reference name and self
                nameToAttributeMap.put(referenceName, transferedAttribute);
            }

            // add the transfered entity domain object with attribute domain objects into the List
            candidateEntityList.add(transferedEntity);
        }
    }

    /**
     * Update candidate entity list according to the reference mapping
     *
     * @param referenceNameMap    map "{package}`{version}`{entity}`{attribute}" to another "{package}`{version}`{entity}`{attribute}"
     * @param nameToAttributeMap  map "{package}`{version}`{entity}`{attribute}" to attribute domain object
     * @param candidateEntityList the candidate entity list, will be inserted to DB later
     * @throws WecubeCoreException when reference name the dto passed is invalid
     */
    private void updateCandidateEntityList(Map<String, String> referenceNameMap,
                                           Map<String, PluginPackageAttribute> nameToAttributeMap,
                                           List<PluginPackageEntity> candidateEntityList) throws WecubeCoreException {
        // update the attribtue domain object with pre-noted map
        for (PluginPackageEntity pluginPackageEntity : candidateEntityList) {
            for (PluginPackageAttribute pluginPackageAttribute : pluginPackageEntity
                    .getPluginPackageAttributeList()) {
                String selfName = pluginPackageAttribute.getPluginPackageEntity().getPluginPackage().getName() + "`"
                        + pluginPackageAttribute.getPluginPackageEntity().getPluginPackage().getVersion() + "`"
                        + pluginPackageAttribute.getPluginPackageEntity().getName() + "`"
                        + pluginPackageAttribute.getName();
                if (referenceNameMap.containsKey(selfName)) {
                    // only need to assign the attribute to attribute when the selfName is found in referenceNameMap
                    String referenceName = referenceNameMap.get(selfName);
                    // check nameToAttributeMap first, if not exist, then check the database, finally throw the exception
                    if (nameToAttributeMap.containsKey(referenceName)) {
                        // the reference is inside the same package
                        PluginPackageAttribute referenceAttribute = nameToAttributeMap
                                .get(referenceName);
                        pluginPackageAttribute.setPluginPackageAttribute(referenceAttribute);
                    } else {
                        // cross-package reference process
                        // the reference cannot be found in the referenceNameMap

                        // split the crossReferenceName
                        Iterable<String> splitResult = Splitter.on('`').trimResults().split(referenceName);
                        if (Iterables.size(splitResult) != 4) {
                            String msg = String.format("The reference name [%s] is illegal", referenceName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
                        // fetch the packageName, packageVersion, entityName, attributeName
                        Iterator<String> splitResultIterator = splitResult.iterator();
                        String referencePackageName = splitResultIterator.next();
                        String referencePackageVersion = splitResultIterator.next();
                        String referenceEntityName = splitResultIterator.next();
                        String referenceAttributeName = splitResultIterator.next();

                        // found attribute domain object with the seperated three reference names
                        Optional<PluginPackageAttribute> foundAttributeList = pluginPackageAttributeRepository
                                .findSingleAttribute(
                                        referencePackageName, referencePackageVersion, referenceEntityName, referenceAttributeName);
                        // the foundEntity should only be one because of the unique key constraint
                        if (!foundAttributeList.isPresent()) {
                            String msg = String.format(
                                    "Cannot found the specified plugin model entity with package name: [%s], entity name: [%s], attribute name: [%s] and package version: [%s]",
                                    referencePackageName, referenceEntityName, referenceAttributeName, referencePackageVersion);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
                        pluginPackageAttribute.setPluginPackageAttribute(foundAttributeList.get());
                    }
                }
            }
        }
    }

    /**
     * Update the reference info for both reference by and reference to
     * This feature is for entity to known whom it refers to and whom it is referred by
     *
     * @param inputEntityDtoList entity dto list as input
     */
    private void updateReferenceInfo(List<PluginPackageEntityDto> inputEntityDtoList) {
        for (PluginPackageEntityDto inputEntityDto : inputEntityDtoList) {
            // query for the referenceBy info
            String packageName = inputEntityDto.getPackageName();
            String packageVersion = inputEntityDto.getPackageVersion();
            String entityName = inputEntityDto.getName();
            Optional<List<PluginPackageAttribute>> allAttributeReferenceByList = pluginPackageAttributeRepository
                    .findAllReferenceByAttribute(packageName, packageVersion, entityName);
            allAttributeReferenceByList.ifPresent(attributeList -> attributeList.forEach(attribute -> {
                // the process of found reference by info
                PluginPackageEntity referenceByEntity = attribute.getPluginPackageEntity();
                Integer referenceByPackageId = referenceByEntity.getId();
                String referenceByPackageName = referenceByEntity.getPluginPackage().getName();
                String referenceByPackageVersion = referenceByEntity.getPluginPackage().getVersion();
                String referenceByEntityName = referenceByEntity.getName();
                String displayName = referenceByEntity.getDisplayName();
                if (!packageName.equals(referenceByPackageName) ||
                        !packageVersion.equals(referenceByPackageVersion) ||
                        !entityName.equals(referenceByEntityName)) {
                    // only add the dto to set when the attribute doesn't belong to this input entity
                    inputEntityDto.updateReferenceBy(
                            referenceByPackageId,
                            referenceByPackageName,
                            referenceByPackageVersion,
                            referenceByEntityName,
                            displayName
                    );
                }
            }));

            // query for the referenceTo info
            List<PluginPackageAttributeDto> attributeDtoList = inputEntityDto.getAttributeDtoList();
            if (!CollectionUtils.isEmpty(attributeDtoList)) {
                for (PluginPackageAttributeDto attributeDto : attributeDtoList) {
                    Optional<PluginPackageEntity> entityReferenceTo = pluginPackageEntityRepository.findByPluginPackage_NameAndPluginPackage_VersionAndName(
                            attributeDto.getRefPackageName(),
                            attributeDto.getRefPackageVersion(),
                            attributeDto.getRefEntityName()
                    );
                    entityReferenceTo.ifPresent(entity -> {
                        PluginPackageEntityDto entityReferenceToDto = PluginPackageEntityDto.fromDomain(entity);
                        inputEntityDto.updateReferenceTo(
                                entityReferenceToDto.getId(),
                                entityReferenceToDto.getPackageName(),
                                entityReferenceToDto.getPackageVersion(),
                                entityReferenceToDto.getName(),
                                entityReferenceToDto.getDisplayName()
                        );
                    });
                }
            }
        }
    }

    /**
     * Convert the plugin model entities from domains to dtos
     *
     * @param savedPluginPackageEntity an Iterable pluginPackageEntity
     * @return converted dtos
     */
    private List<PluginPackageEntityDto> convertEntityDomainToDto(Iterable<PluginPackageEntity> savedPluginPackageEntity) {
        List<PluginPackageEntityDto> pluginPackageEntityDtos = new ArrayList<>();
        savedPluginPackageEntity
                .forEach(domain -> pluginPackageEntityDtos.add(PluginPackageEntityDto.fromDomain(domain)));
        updateReferenceInfo(pluginPackageEntityDtos);

        return pluginPackageEntityDtos;
    }

}
