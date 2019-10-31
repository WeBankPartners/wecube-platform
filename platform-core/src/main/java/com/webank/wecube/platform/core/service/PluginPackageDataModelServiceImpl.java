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

        // map "{package}`{entity}`{attribute}" to another "{package}`{entity}`{attribute}"
        Map<String, String> referenceNameMap = new HashMap<>();
        // map "{package}`{entity}`{attribute}" to attribute domain object
        Map<String, PluginPackageAttribute> nameToAttributeMap = new HashMap<>();

        List<PluginPackageEntity> candidateEntityList = new ArrayList<>();

        buildReferenceMapping(inputEntityDtoList, referenceNameMap, nameToAttributeMap, candidateEntityList);

        updateCandidateEntityList(referenceNameMap, nameToAttributeMap, candidateEntityList);

        return convertEntityDomainToDto(pluginPackageEntityRepository.saveAll(candidateEntityList), false);
    }


    /**
     * Plugin model overview
     *
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginPackageEntityDto> overview() {
        List<PluginPackageEntity> allEntityList = new ArrayList<>();
        Optional<List<String>> allPackageNameOpt = pluginPackageRepository.findAllDistinctPackage();
        allPackageNameOpt.ifPresent(allPackageNameList -> {
            for (String packageName : allPackageNameList) {
                Optional<List<PluginPackageEntity>> allLatestEntityByPluginPackage_nameOpt = pluginPackageEntityRepository.findAllLatestEntityByPluginPackage_name(packageName);
                allLatestEntityByPluginPackage_nameOpt.ifPresent(allEntityList::addAll);
            }
        });
        return convertEntityDomainToDto(allEntityList, true);
    }

    /**
     * View one data model entity with its relationship by packageId
     *
     * @param packageId the name of package
     * @return list of entity dto
     */
    @Override
    public List<PluginPackageEntityDto> packageView(int packageId) throws WecubeCoreException {
        Optional<List<PluginPackageEntity>> allByPluginPackage_id = pluginPackageEntityRepository.findAllLatestByPluginPackage_Id(packageId);
        if (!allByPluginPackage_id.isPresent()) {
            String msg = String.format("The data model of package ID: %d cannot be found.", packageId);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        List<PluginPackageEntity> pluginPackageEntityList = allByPluginPackage_id.get();
        return convertEntityDomainToDto(pluginPackageEntityList, true);
    }

    /**
     * Build reference mapping for package registration
     *
     * @param inputEntityDtoList  the input entity dto list
     * @param referenceNameMap    map "{package}`{entity}`{attribute}" to another "{package}`{entity}`{attribute}"
     * @param nameToAttributeMap  map "{package}`{entity}`{attribute}" to attribute domain object
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
                            || StringUtils.isEmpty(inputAttributeDto.getRefEntityName())
                            || StringUtils.isEmpty(inputAttributeDto.getRefAttributeName())) {
                        // once the reference name (including packageName, entityName and attributeName should be specified
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
                                    + inputEntityDto.getName() + "`"
                                    + inputAttributeDto.getName();
                    String referenceName =
                            inputAttributeDto.getRefPackageName() + "`"
                                    + inputAttributeDto.getRefEntityName() + "`"
                                    + inputAttributeDto.getRefAttributeName();
                    referenceNameMap.put(selfName, referenceName);
                }
            }


            // transfer from entity dto to entity domain object
            PluginPackageEntity transferedEntity = PluginPackageEntityDto.toDomain(inputEntityDto);

            // update transferred entity with latest data model version
            // if latest version of entity is not found, then do nothing.
            // by default, the data model version is 1
            Optional<PluginPackageEntity> latestVersionOfEntity = pluginPackageEntityRepository.findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(inputEntityDto.getPackageName(), inputEntityDto.getName());
            latestVersionOfEntity.ifPresent(entity -> transferedEntity.setDataModelVersion(entity.getDataModelVersion() + 1));
            // query the plugin package domain object by package name and version recorded in entity dto
            Optional<PluginPackage> foundPackageByNameAndVersion = pluginPackageRepository.findTop1ByNameOrderByVersionDesc(
                    inputEntityDto.getPackageName());

            if (!foundPackageByNameAndVersion.isPresent()) {
                String msg = String.format("Cannot find the package [%s] while registering data model",
                        inputEntityDto.getPackageName());
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
     * @param referenceNameMap    map "{package}`{entity}`{attribute}" to another "{package}`{entity}`{attribute}"
     * @param nameToAttributeMap  map "{package}`{entity}`{attribute}" to attribute domain object
     * @param candidateEntityList the candidate entity list, will be inserted to DB later
     * @throws WecubeCoreException when reference name the dto passed is invalid
     */
    private void updateCandidateEntityList(Map<String, String> referenceNameMap,
                                           Map<String, PluginPackageAttribute> nameToAttributeMap,
                                           List<PluginPackageEntity> candidateEntityList) throws WecubeCoreException {
        // update the attribtue domain object with pre-noted map
        for (PluginPackageEntity candidateEntity : candidateEntityList) {

            for (PluginPackageAttribute pluginPackageAttribute : candidateEntity
                    .getPluginPackageAttributeList()) {
                String selfName = pluginPackageAttribute.getPluginPackageEntity().getPluginPackage().getName() + "`"
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
                        // should search from the database with latest package version

                        // split the crossReferenceName
                        Iterable<String> splitResult = Splitter.on('`').trimResults().split(referenceName);
                        if (Iterables.size(splitResult) != 3) {
                            String msg = String.format("The reference name [%s] is illegal", referenceName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
                        // fetch the packageName, packageVersion, entityName, attributeName
                        Iterator<String> splitResultIterator = splitResult.iterator();
                        String referencePackageName = splitResultIterator.next();
                        String referenceEntityName = splitResultIterator.next();
                        String referenceAttributeName = splitResultIterator.next();
                        Optional<PluginPackageEntity> foundReferenceEntityOpt = pluginPackageEntityRepository.findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(referencePackageName, referenceEntityName);
                        if (!foundReferenceEntityOpt.isPresent()) {
                            String msg = String.format(
                                    "Cannot found the specified plugin model entity with package name: [%s], entity name: [%s]",
                                    referencePackageName, referenceEntityName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }

                        boolean ifFoundAttrInDb = false;
                        for (PluginPackageAttribute attribute : foundReferenceEntityOpt.get().getPluginPackageAttributeList()) {
                            if (referenceAttributeName.equals(attribute.getName())) {
                                pluginPackageAttribute.setPluginPackageAttribute(attribute);
                                ifFoundAttrInDb = true;
                                break;
                            }
                        }

                        if (!ifFoundAttrInDb) {
                            String msg = String.format(
                                    "Cannot found the specified plugin model attribute with package name: [%s], entity name: [%s], attribute name: [%s]",
                                    referencePackageName, referenceEntityName, referenceAttributeName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
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
            String entityName = inputEntityDto.getName();
            long dataModelVersion = 0;

            Optional<PluginPackageEntity> latestEntityOpt = pluginPackageEntityRepository.findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(packageName, entityName);
            if (latestEntityOpt.isPresent()) {
                dataModelVersion = latestEntityOpt.get().getDataModelVersion();
            }
            // find "reference by" info by latest data model version
            Optional<List<PluginPackageAttribute>> allAttributeReferenceByList = pluginPackageAttributeRepository
                    .findAllReferenceByAttribute(packageName, entityName, dataModelVersion);

            allAttributeReferenceByList.ifPresent(attributeList -> attributeList.forEach(attribute -> {
                // the process of found reference by info
                PluginPackageEntity referenceByEntity = attribute.getPluginPackageEntity();
                Integer referenceByPackageId = referenceByEntity.getId();
                String referenceByPackageName = referenceByEntity.getPluginPackage().getName();
                String referenceByPackageVersion = referenceByEntity.getPluginPackage().getVersion();
                String referenceByEntityName = referenceByEntity.getName();
                String displayName = referenceByEntity.getDisplayName();
                if (!packageName.equals(referenceByPackageName) ||
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
                    Optional<PluginPackageEntity> entityReferenceTo = pluginPackageEntityRepository.findTop1ByPluginPackage_NameAndNameOrderByDataModelVersionDesc(
                            attributeDto.getRefPackageName(),
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
    private List<PluginPackageEntityDto> convertEntityDomainToDto(Iterable<PluginPackageEntity> savedPluginPackageEntity, boolean ifUpdateReferenceInfo) {
        List<PluginPackageEntityDto> pluginPackageEntityDtos = new ArrayList<>();
        savedPluginPackageEntity
                .forEach(domain -> pluginPackageEntityDtos.add(PluginPackageEntityDto.fromDomain(domain)));
        if (ifUpdateReferenceInfo) updateReferenceInfo(pluginPackageEntityDtos);

        return pluginPackageEntityDtos;
    }

}
