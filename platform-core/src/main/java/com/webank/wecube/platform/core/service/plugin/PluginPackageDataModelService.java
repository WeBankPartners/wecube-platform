package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.BindedInterfaceEntityDto;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DataModelEntityDto;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto.PluginPackageEntityKey;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto.TrimmedPluginPackageEntityDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageAttribute;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageDataModel;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageEntity;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDataModelMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.utils.JsonUtils;

@Service
public class PluginPackageDataModelService {
    private static final Logger logger = LoggerFactory.getLogger(PluginPackageDataModelService.class);
    private static final String dataModelUrl = "http://{gatewayUrl}/{packageName}/{dataModelUrl}";

    private static final String ATTRIBUTE_KEY_SEPARATOR = "`";

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private PluginPackageDataModelMapper dataModelRepository;

    @Autowired
    private PluginPackageAttributesMapper pluginPackageAttributeRepository;

    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntityRepository;

    @Autowired
    private PluginConfigsMapper pluginConfigRepository;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginPackageDataModelMapper pluginPackageDataModelMapper;

    @Autowired
    private PluginPackageMgmtService pluginPackageMgmtService;

    /**
     * 
     * @param pluginPackageDataModelDto
     * @return
     */
    public PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto) {
        return register(pluginPackageDataModelDto, false);
    }

    // TODO
    // FIXME
    public PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto,
            boolean fromDynamicUpdate) {
        PluginPackages latestVersionPluginPackage = pluginPackageMgmtService
                .fetchLatestVersionPluginPackage(pluginPackageDataModelDto.getPackageName());

        if (latestVersionPluginPackage == null) {
            String msg = String.format("Cannot find the package [%s] while registering data model",
                    pluginPackageDataModelDto.getPackageName());
            logger.error(msg);
            throw new WecubeCoreException("3116", msg, pluginPackageDataModelDto.getPackageName());
        }

        if (fromDynamicUpdate) {
            pluginPackageDataModelDto.setDynamic(true);
            pluginPackageDataModelDto.setUpdateTime(System.currentTimeMillis());
            pluginPackageDataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
        }

        PluginPackageDataModel existingDataModelDomain = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(pluginPackageDataModelDto.getPackageName());

        int newDataModelVersion = 1;
        if (existingDataModelDomain != null) {
            newDataModelVersion = existingDataModelDomain.getVersion() + 1;
        }
        pluginPackageDataModelDto.setVersion(newDataModelVersion);
        PluginPackageDataModel pluginPackageDataModel = PluginPackageDataModelDto.toDomain(pluginPackageDataModelDto);

        if (pluginPackageDataModelDto.getPluginPackageEntities() != null
                && !pluginPackageDataModelDto.getPluginPackageEntities().isEmpty()) {
            Map<String, String> attributeReferenceNameMap = buildAttributeReferenceNameMap(pluginPackageDataModelDto);
            Map<String, PluginPackageAttributes> referenceAttributeMap = buildReferenceAttributeMap(
                    pluginPackageDataModel);
            updateAttributeReference(pluginPackageDataModel, attributeReferenceNameMap, referenceAttributeMap);
        }

        return convertDataModelDomainToDto(dataModelRepository.save(pluginPackageDataModel));
    }

    /**
     * Plugin model overview
     *
     * @return an list of data model DTOs consist of entity dtos which contain
     *         both entities and attributes
     */
    public Set<PluginPackageDataModelDto> overview() {
        Set<PluginPackageDataModelDto> pluginPackageDataModelDtos = new HashSet<>();
        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllDistinctPackages();

        if (pluginPackagesEntities == null) {
            return pluginPackageDataModelDtos;
        }

        for (PluginPackages pluginPackagesEntity : pluginPackagesEntities) {

            PluginPackageDataModel dataModelEntity = dataModelRepository
                    .selectLatestDataModelByPackageName(pluginPackagesEntity.getName());
            if (dataModelEntity != null) {
                PluginPackageDataModelDto dto = buildPluginPackageDataModelDto(dataModelEntity);
                pluginPackageDataModelDtos.add(dto);
            }
        }

        return pluginPackageDataModelDtos;
    }

    /**
     * View one data model entity with its relationship by packageName
     *
     * @param packageName
     *            the name of package
     * @return list of entity dto
     */
    public PluginPackageDataModelDto packageView(String packageName) {
        PluginPackages latestPluginPackage = pluginPackageMgmtService.fetchLatestVersionPluginPackage(packageName);

        if (latestPluginPackage == null) {
            String msg = String.format("Plugin package with name [%s] is not found", packageName);
            logger.info(msg);
            return null;
        }

        PluginPackageDataModel latestDataModelEntity = dataModelRepository
                .selectLatestDataModelByPackageName(packageName);

        if (latestDataModelEntity == null) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException("3118", errorMessage, packageName);
        }

        return convertDataModelDomainToDto(latestDataModelEntity);
    }

    /**
     * Update candidate entity list according to the reference mapping
     *
     * @param pluginPackageDataModel
     *            the candidate pluginPackageDataModel, will be inserted to DB
     *            later
     * @param referenceNameMap
     *            map "{package}`{entity}`{attribute}" to another
     *            "{package}`{entity}`{attribute}"
     * @param nameToAttributeMap
     *            map "{package}`{entity}`{attribute}" to attribute domain
     *            object
     * @throws WecubeCoreException
     *             when reference name the dto passed is invalid
     */
    private void updateAttributeReference(PluginPackageDataModel pluginPackageDataModel,
            Map<String, String> referenceNameMap, Map<String, PluginPackageAttributes> nameToAttributeMap) {
        // update the attribtue domain object with pre-noted map
        for (PluginPackageEntities candidateEntity : pluginPackageDataModel.getPluginPackageEntities()) {
            List<PluginPackageAttributes> pluginPackageAttributes = candidateEntity.getPluginPackageAttributes();
            if (pluginPackageAttributes == null) {
                continue;
            }

            for (PluginPackageAttributes pluginPackageAttribute : pluginPackageAttributes) {
                String selfName = candidateEntity.getPackageName() + ATTRIBUTE_KEY_SEPARATOR + candidateEntity.getName()
                        + ATTRIBUTE_KEY_SEPARATOR + pluginPackageAttribute.getName();
                if (referenceNameMap.containsKey(selfName)) {
                    // only need to assign the attribute to attribute when the
                    // selfName is found in referenceNameMap
                    String referenceName = referenceNameMap.get(selfName);
                    // check nameToAttributeMap first, if not exist, then check
                    // the database, finally throw the exception
                    if (nameToAttributeMap.containsKey(referenceName)) {
                        // the reference is inside the same package
                        PluginPackageAttributes referenceAttribute = nameToAttributeMap.get(referenceName);
                        pluginPackageAttribute.setPluginPackageAttribute(referenceAttribute);
                    } else {
                        // cross-package reference process
                        // the reference cannot be found in the referenceNameMap
                        // should search from the database with latest package
                        // version

                        // split the crossReferenceName
                        Iterable<String> splitResult = Splitter.on('`').trimResults().split(referenceName);
                        if (Iterables.size(splitResult) != 3) {
                            String msg = String.format("The reference name [%s] is illegal", referenceName);
                            logger.error(msg);
                            throw new WecubeCoreException("3119", msg, referenceName);
                        }
                        // fetch the packageName, packageVersion, entityName,
                        // attributeName
                        Iterator<String> splitResultIterator = splitResult.iterator();
                        String referencePackageName = splitResultIterator.next();
                        String referenceEntityName = splitResultIterator.next();
                        String referenceAttributeName = splitResultIterator.next();
                        PluginPackageDataModel latestDataModelByPackageName = dataModelRepository
                                .selectLatestDataModelByPackageName(referencePackageName);
                        if (latestDataModelByPackageName == null) {
                            String msg = String.format("Cannot found the specified data model with package name: [%s]",
                                    referencePackageName);
                            logger.error(msg);
                            throw new WecubeCoreException("3120", msg, referencePackageName);
                        }

                        PluginPackageEntities foundReferenceEntity = null;
                        List<PluginPackageEntities> foundReferenceEntities = latestDataModelByPackageName
                                .getPluginPackageEntities();
                        for (PluginPackageEntities e : foundReferenceEntities) {
                            if (referenceEntityName.equals(e.getName())) {
                                foundReferenceEntity = e;
                                break;
                            }
                        }

                        if (foundReferenceEntity == null) {
                            String msg = String.format(
                                    "Cannot found the specified plugin model entity with package name: [%s], entity name: [%s]",
                                    referencePackageName, referenceEntityName);
                            logger.error(msg);
                            throw new WecubeCoreException("3121", msg, referencePackageName, referenceEntityName);
                        }

                        PluginPackageAttributes foundReferenceAttribute = null;
                        List<PluginPackageAttributes> foundReferenceAttributes = foundReferenceEntity
                                .getPluginPackageAttributes();
                        for (PluginPackageAttributes a : foundReferenceAttributes) {
                            if (referenceAttributeName.equals(a.getName())) {
                                foundReferenceAttribute = a;
                                break;
                            }
                        }

                        if (foundReferenceAttribute != null) {
                            pluginPackageAttribute.setPluginPackageAttribute(foundReferenceAttribute);
                        } else {
                            String msg = String.format(
                                    "Cannot found the specified plugin model attribute with package name: [%s], entity name: [%s], attribute name: [%s]",
                                    referencePackageName, referenceEntityName, referenceAttributeName);
                            logger.error(msg);
                            throw new WecubeCoreException("3122", msg, referencePackageName, referenceEntityName,
                                    referenceAttributeName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Update the reference info for both reference by and reference to This
     * feature is for entity to known whom it refers to and whom it is referred
     * by
     *
     * @param inputEntityDtoList
     *            entity dto list as input
     */
    private void updateReferenceInfo(List<PluginPackageEntityDto> inputEntityDtoList) {
        for (PluginPackageEntityDto inputEntityDto : inputEntityDtoList) {
            updateReferenceInfo(inputEntityDto);
        }
    }

    private void updateReferenceInfo(PluginPackageEntityDto inputEntityDto) {
        // query for the referenceBy info
        String packageName = inputEntityDto.getPackageName();
        String entityName = inputEntityDto.getName();
        int dataModelVersion = 0;

        PluginPackageDataModel latestDataModelEntity = dataModelRepository
                .selectLatestDataModelByPackageName(packageName);
        if (latestDataModelEntity != null) {
            dataModelVersion = latestDataModelEntity.getVersion();
        }
        // TODO

        // find "reference by" info by latest data model version
        Optional<List<LazyPluginPackageAttribute>> allAttributeReferenceByList = lazyPluginPackageAttributeRepository
                .findAllChildrenAttributes(packageName, entityName, dataModelVersion);

        allAttributeReferenceByList.ifPresent(attributeList -> attributeList.forEach(attribute -> {
            // the process of found reference by info
            LazyPluginPackageEntity referenceByEntity = attribute.getPluginPackageEntity();
            if (!packageName.equals(referenceByEntity.getPackageName())
                    || !entityName.equals(referenceByEntity.getName())) {
                // only add the dto to set when the attribute doesn't belong to
                // this input entity
                inputEntityDto.updateReferenceBy(referenceByEntity.getId(), referenceByEntity.getPackageName(),
                        referenceByEntity.getDataModelVersion(), referenceByEntity.getName(),
                        referenceByEntity.getDisplayName(), PluginPackageAttributeDto.fromDomain(attribute));
            }
        }));

        // query for the referenceTo info
        List<PluginPackageAttributeDto> attributes = inputEntityDto.getAttributes();
        if (!CollectionUtils.isEmpty(attributes)) {
            attributes.forEach(attributeDto -> {
                if (Strings.isNullOrEmpty(attributeDto.getRefPackageName())) {
                    return;
                }
                Optional<String> dataModelIdOpt = lazyDataModelRepository
                        .findLatestDataModelIdByPackageName(attributeDto.getRefPackageName());
                if (dataModelIdOpt.isPresent()) {
                    Optional<LazyPluginPackageDataModel> dataModelOpt = lazyDataModelRepository
                            .findById(dataModelIdOpt.get());
                    if (dataModelOpt.isPresent()) {
                        LazyPluginPackageDataModel dataModel = dataModelOpt.get();
                        dataModel.getPluginPackageEntities().stream()
                                .filter(entity -> attributeDto.getRefEntityName().equals(entity.getName())).findAny()
                                .ifPresent(entity -> {
                                    PluginPackageEntityDto entityReferenceToDto = PluginPackageEntityDto
                                            .fromDomain(entity);
                                    inputEntityDto.updateReferenceTo(entityReferenceToDto.getId(),
                                            entityReferenceToDto.getPackageName(),
                                            entityReferenceToDto.getDataModelVersion(), entityReferenceToDto.getName(),
                                            entityReferenceToDto.getDisplayName(), attributeDto);
                                });
                    }
                }
            });
        }
    }

    private void updateReferenceInfoIncludeSelfReference(PluginPackageEntityDto inputEntityDto) {
        String packageName = inputEntityDto.getPackageName();
        String entityName = inputEntityDto.getName();
        int dataModelVersion = 0;

        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository
                .findLatestDataModelByPackageName(packageName);
        if (latestDataModelByPackageName.isPresent()) {
            dataModelVersion = latestDataModelByPackageName.get().getVersion();
        }
        Optional<List<PluginPackageAttribute>> allAttributeReferenceByList = pluginPackageAttributeRepository
                .findAllChildrenAttributes(packageName, entityName, dataModelVersion);

        allAttributeReferenceByList.ifPresent(attributeList -> attributeList.forEach(attribute -> {
            PluginPackageEntity referenceByEntity = attribute.getPluginPackageEntity();
            inputEntityDto.updateReferenceBy(referenceByEntity.getId(), referenceByEntity.getPackageName(),
                    referenceByEntity.getDataModelVersion(), referenceByEntity.getName(),
                    referenceByEntity.getDisplayName(), PluginPackageAttributeDto.fromDomain(attribute));

        }));

        List<PluginPackageAttributeDto> attributes = inputEntityDto.getAttributes();
        if (!CollectionUtils.isEmpty(attributes)) {
            attributes.forEach(attributeDto -> {
                dataModelRepository.findLatestDataModelByPackageName(attributeDto.getRefPackageName())
                        .ifPresent(dataModel -> dataModel.getPluginPackageEntities().stream()
                                .filter(entity -> attributeDto.getRefEntityName().equals(entity.getName())).findAny()
                                .ifPresent(entity -> {
                                    PluginPackageEntityDto entityReferenceToDto = PluginPackageEntityDto
                                            .fromDomain(entity);
                                    inputEntityDto.updateReferenceTo(entityReferenceToDto.getId(),
                                            entityReferenceToDto.getPackageName(),
                                            entityReferenceToDto.getDataModelVersion(), entityReferenceToDto.getName(),
                                            entityReferenceToDto.getDisplayName(), attributeDto);
                                }));
            });
        }
    }

    private List<PluginPackageEntityDto> convertEntityDomainToDto(
            Iterable<PluginPackageEntity> savedPluginPackageEntity, boolean ifUpdateReferenceInfo) {
        List<PluginPackageEntityDto> pluginPackageEntityDtos = new ArrayList<>();
        savedPluginPackageEntity
                .forEach(domain -> pluginPackageEntityDtos.add(PluginPackageEntityDto.fromDomain(domain)));
        if (ifUpdateReferenceInfo) {
            updateReferenceInfo(pluginPackageEntityDtos);
        }

        return pluginPackageEntityDtos;
    }

    public PluginPackageDataModelDto pullDynamicDataModel(String packageName) {
        PluginPackages latestPluginPackagesEntity = pluginPackageMgmtService
                .fetchLatestVersionPluginPackage(packageName);

        if (latestPluginPackagesEntity == null) {
            String errorMessage = String.format("Plugin package with name [%s] is not found", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException("3123", errorMessage, packageName);
        }

        PluginPackageDataModel dataModel = dataModelRepository.selectLatestDataModelByPackageName(packageName);

        if (dataModel == null) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException("3124", errorMessage, packageName);
        }

        if (!dataModel.getIsDynamic()) {
            String message = String.format("DataMode does not support dynamic update for package: [%s]", packageName);
            logger.error(message);
            throw new WecubeCoreException("3125", message, packageName);
        }

        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setPackageName(packageName);
        int newDataModelVersion = dataModel.getVersion() + 1;
        dataModelDto.setVersion(newDataModelVersion);
        dataModelDto.setUpdateTime(System.currentTimeMillis());
        dataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
        dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        dataModelDto.setUpdatePath(dataModel.getUpdatePath());
        dataModelDto.setDynamic(true);

        Set<PluginPackageEntityDto> dynamicPluginPackageEntities = pullDynamicDataModelFromPlugin(dataModel);

        updateEntityReferences(packageName, newDataModelVersion, dynamicPluginPackageEntities);

        dataModelDto.setPluginPackageEntities(dynamicPluginPackageEntities);

        return dataModelDto;
    }

    private void updateEntityReferences(String packageName, int newDataModelVersion,
            Set<PluginPackageEntityDto> dynamicPluginPackageEntities) {

        if (dynamicPluginPackageEntities == null || dynamicPluginPackageEntities.isEmpty()) {
            return;
        }
        Map<PluginPackageEntityDto.PluginPackageEntityKey, PluginPackageEntityDto.TrimmedPluginPackageEntityDto> referenceEntityDtoMaps = new HashMap<>();

        dynamicPluginPackageEntities.forEach(entity -> {
            entity.setPackageName(packageName);
            entity.setDataModelVersion(newDataModelVersion);
        });
        
        for(PluginPackageEntityDto dto : dynamicPluginPackageEntities){
            TrimmedPluginPackageEntityDto trimmedDto = dto.toTrimmedPluginPackageEntityDto();
            PluginPackageEntityKey key = trimmedDto.getPluginPackageEntityKey();
            referenceEntityDtoMaps.put(key, trimmedDto);
        }

//        referenceEntityDtoMaps = Collections.unmodifiableMap(
//                dynamicPluginPackageEntities.stream().map(entity -> entity.toTrimmedPluginPackageEntityDto())
//                        .collect(Collectors.toMap(x -> x.getPluginPackageEntityKey(), x -> x)));

        Map<PluginPackageEntityDto.PluginPackageEntityKey, PluginPackageEntityDto.TrimmedPluginPackageEntityDto> finalReferenceEntityDtoMaps = referenceEntityDtoMaps;

        for(PluginPackageEntityDto entityDto : dynamicPluginPackageEntities){
            List<PluginPackageAttributeDto>  attributes = entityDto.getAttributes();
            for(PluginPackageAttributeDto attribute:attributes){
                attribute.setPackageName(packageName);
                if (StringUtils.isNotBlank(attribute.getRefAttributeName())) {
                    if (StringUtils.isBlank(attribute.getRefPackageName())) {
                        attribute.setRefPackageName(packageName);
                    }
                    if (StringUtils.isBlank(attribute.getRefEntityName())) {
                        attribute.setRefEntityName(entityDto.getName());
                    }
                    entityDto.updateReferenceTo(
                            finalReferenceEntityDtoMaps.get(new PluginPackageEntityDto.PluginPackageEntityKey(
                                    attribute.getRefPackageName(), attribute.getRefEntityName())));
                }
            }
        }
        
//        dynamicPluginPackageEntities.forEach(entity -> {
//            entity.getAttributes().forEach(attribute -> {
//                attribute.setPackageName(packageName);
//                if (StringUtils.isNotBlank(attribute.getRefAttributeName())) {
//                    if (StringUtils.isBlank(attribute.getRefPackageName())) {
//                        attribute.setRefPackageName(packageName);
//                    }
//                    if (StringUtils.isBlank(attribute.getRefEntityName())) {
//                        attribute.setRefEntityName(entity.getName());
//                    }
//                    entity.updateReferenceTo(
//                            finalReferenceEntityDtoMaps.get(new PluginPackageEntityDto.PluginPackageEntityKey(
//                                    attribute.getRefPackageName(), attribute.getRefEntityName())));
//                }
//            });
//        });
    }

    @SuppressWarnings("unchecked")
    private Set<PluginPackageEntityDto> pullDynamicDataModelFromPlugin(PluginPackageDataModel dataModel) {
        Map<String, Object> parametersMap = new HashMap<>();
        String gatewayUrl = applicationProperties.getGatewayUrl();
        parametersMap.put("gatewayUrl", gatewayUrl);
        parametersMap.put("packageName", dataModel.getPackageName());
        String updatePath = dataModel.getUpdatePath();
        parametersMap.put("dataModelUrl", updatePath.startsWith("/") ? updatePath.substring(1) : updatePath);

        List<PluginPackageEntityDto> dynamicPluginPackageEntities = Collections.EMPTY_LIST;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(dataModelUrl);
            UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(parametersMap);
            HttpMethod method = HttpMethod.valueOf(dataModel.getUpdateMethod());
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(uriComponents.toString(), method, requestEntity,
                    String.class);
            if (StringUtils.isBlank(response.getBody()) || response.getStatusCode().isError()) {
                throw new WecubeCoreException(response.toString());
            }
            CommonResponseDto responseDto = JsonUtils.toObject(response.getBody(), CommonResponseDto.class);
            if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
                String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
                logger.error(msg);
                throw new WecubeCoreException("3126", msg, responseDto.getMessage());
            }
            dynamicPluginPackageEntities = JsonUtils.toList(JsonUtils.toJsonString(responseDto.getData()),
                    PluginPackageEntityDto.class);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return Sets.newLinkedHashSet(dynamicPluginPackageEntities);
    }

    /**
     * Get all refByInfo at attribute level
     *
     * @param packageName
     *            package name
     * @param entityName
     *            entity name
     * @return attribute dto list
     * @throws WecubeCoreException
     *             the wecube core exception
     */
    public List<PluginPackageAttributeDto> getRefByInfo(String packageName, String entityName) {

        PluginPackageDataModel latestDataModelEntity = dataModelRepository
                .selectLatestDataModelByPackageName(packageName);
        if (latestDataModelEntity == null) {
            String msg = String.format("Cannot find data model by package name: [%s] and entity name: [%s]",
                    packageName, entityName);
            logger.error(msg);
            throw new WecubeCoreException("3302", msg, packageName, entityName);
        }

        List<PluginPackageAttributeDto> resultList = new ArrayList<>();
        List<PluginPackageEntities> foundEntityList = pluginPackageEntityRepository
                .selectAllByPackageNameAndEntityNameAndDataModelVersion(packageName, entityName,
                        latestDataModelEntity.getVersion());

        if (foundEntityList == null || foundEntityList.isEmpty()) {
            logger.warn("empty entity list for {} {} {}", packageName, entityName, latestDataModelEntity.getVersion());
            return resultList;
        }

        PluginPackageEntities foundEntity = foundEntityList.get(0);

        List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributeRepository
                .selectAllByEntity(foundEntity.getId());

        if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
            logger.info("empty attributes for {}", foundEntity.getId());
            return resultList;
        }

        for (PluginPackageAttributes attr : pluginPackageAttributes) {
            if (!"id".equalsIgnoreCase(attr.getName())) {
                continue;
            }

            List<PluginPackageAttributes> referenceAttributes = pluginPackageAttributeRepository
                    .selectAllReferences(attr.getId());
            if (referenceAttributes == null) {
                continue;
            }

            for (PluginPackageAttributes referenceAttr : referenceAttributes) {
                PluginPackageEntities refEntity = pluginPackageEntityRepository
                        .selectByPrimaryKey(referenceAttr.getEntityId());
                String refPackageName = refEntity.getPackageName();
                String refEntityName = refEntity.getName();
                String refAttrName = referenceAttr.getName();

                PluginPackageAttributeDto refAttrDto = buildPluginPackageAttributeDto(referenceAttr);
                resultList.add(refAttrDto);
            }
        }

        return resultList;

        // // find all children attributes
        // List<PluginPackageAttributes> allChildrenAttributesOpt =
        // pluginPackageAttributeRepository
        // .findAllChildrenAttributes(packageName, entityName, version);

        // select attribute from all children where ref attribute is id
        // if (allChildrenAttributesOpt.isPresent()) {
        // List<PluginPackageAttribute> allChildrenAttribute =
        // allChildrenAttributesOpt.get();
        // for (PluginPackageAttribute pluginPackageAttribute :
        // allChildrenAttribute) {
        // String refPackageName =
        // pluginPackageAttribute.getPluginPackageAttribute().getPluginPackageEntity()
        // .getPackageName();
        // String refEntityName =
        // pluginPackageAttribute.getPluginPackageAttribute().getPluginPackageEntity()
        // .getName();
        // String refAttrName =
        // pluginPackageAttribute.getPluginPackageAttribute().getName();
        // if (packageName.equals(refPackageName) &&
        // entityName.equals(refEntityName)
        // && "id".equals(refAttrName)) {
        // PluginPackageAttributeDto returnedAttributeDto =
        // buildPluginPackageAttributeDto(
        // pluginPackageAttribute);
        // resultList.add(returnedAttributeDto);
        // }
        // }
        // }
        // return resultList;

    }

    /**
     * 
     * @param packageName
     * @param entityName
     * @return
     */
    public List<PluginPackageAttributeDto> entityView(String packageName, String entityName) {
        PluginPackageDataModel latestDataModelEntity = dataModelRepository
                .selectLatestDataModelByPackageName(packageName);
        if (latestDataModelEntity == null) {
            String msg = String.format("Cannot find data model by package name: [%s] and entity name: [%s]",
                    packageName, entityName);
            logger.error(msg);
            throw new WecubeCoreException("3302", msg, packageName, entityName);
        }

        List<PluginPackageAttributeDto> result = new ArrayList<>();
        List<PluginPackageEntities> foundEntityList = pluginPackageEntityRepository
                .selectAllByPackageNameAndEntityNameAndDataModelVersion(packageName, entityName,
                        latestDataModelEntity.getVersion());

        if (foundEntityList == null || foundEntityList.isEmpty()) {
            logger.warn("empty entity list for {} {} {}", packageName, entityName, latestDataModelEntity.getVersion());
            return result;
        }

        PluginPackageEntities foundEntity = foundEntityList.get(0);

        List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributeRepository
                .selectAllByEntity(foundEntity.getId());

        if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
            logger.info("empty attributes for {}", foundEntity.getId());
            return result;
        }

        for (PluginPackageAttributes a : pluginPackageAttributes) {
            PluginPackageAttributeDto dto = buildPluginPackageAttributeDto(a);
            result.add(dto);
        }

        return result;

        // Optional<PluginPackageEntity> foundEntity =
        // this.pluginPackageEntityRepository
        // .findByPackageNameAndNameAndDataModelVersion(packageName, entityName,
        // version);
        // foundEntity.ifPresent(entity -> {
        // entity.getPluginPackageAttributeList().forEach(
        // pluginPackageAttribute ->
        // result.add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        // });
        // return result;
    }

    public DataModelEntityDto getEntityByPackageNameAndName(String packageName, String entityName) {
        DataModelEntityDto dataModelEntityDto = new DataModelEntityDto();

        Optional<PluginPackageDataModel> dataModelOptional = dataModelRepository
                .findLatestDataModelByPackageName(packageName);
        if (!dataModelOptional.isPresent()) {
            return dataModelEntityDto;
        }
        Optional<PluginPackageEntity> entityOptional = pluginPackageEntityRepository
                .findByPackageNameAndNameAndDataModelVersion(packageName, entityName,
                        dataModelOptional.get().getVersion());
        if (!entityOptional.isPresent()) {
            return dataModelEntityDto;
        }
        dataModelEntityDto = DataModelEntityDto.fromDomain(entityOptional.get());
        updateReferenceInfoIncludeSelfReference(dataModelEntityDto);

        List<BindedInterfaceEntityDto> referenceToEntityList = new ArrayList<BindedInterfaceEntityDto>();
        List<BindedInterfaceEntityDto> referenceByEntityList = new ArrayList<BindedInterfaceEntityDto>();

        List<PluginConfig> bindedInterfacesConfigs = pluginConfigRepository
                .findAllPluginConfigGroupByTargetEntityWithFilterRule();
        if (bindedInterfacesConfigs == null || bindedInterfacesConfigs.size() == 0) {
            return dataModelEntityDto;
        }

        for (PluginConfig config : bindedInterfacesConfigs) {
            buildLeafEntity(referenceToEntityList, dataModelEntityDto.getReferenceToEntityList(), config);
            buildLeafEntity(referenceByEntityList, dataModelEntityDto.getReferenceByEntityList(), config);
        }

        dataModelEntityDto.getLeafEntityList().setReferenceToEntityList(referenceToEntityList);
        dataModelEntityDto.getLeafEntityList().setReferenceByEntityList(referenceByEntityList);

        return dataModelEntityDto;
    }

    private void buildLeafEntity(List<BindedInterfaceEntityDto> leafEntityList,
            List<TrimmedPluginPackageEntityDto> entityDtoList, PluginConfig config) {
        for (TrimmedPluginPackageEntityDto entityDto : entityDtoList) {
            if (entityDto.getPackageName().equals(config.getTargetPackage())
                    && entityDto.getName().equals(config.getTargetEntity())) {
                boolean entityExistedFlag = false;
                for (BindedInterfaceEntityDto bindedInterfaceEntityDto : leafEntityList) {
                    if (bindedInterfaceEntityDto.getPackageName().equals(config.getTargetPackage())
                            && bindedInterfaceEntityDto.getEntityName().equals(config.getTargetEntity())
                            && bindedInterfaceEntityDto.getFilterRule()
                                    .equals(config.getTargetEntityWithFilterRule())) {
                        entityExistedFlag = true;
                    }
                }
                if (!entityExistedFlag) {
                    leafEntityList.add(new BindedInterfaceEntityDto(config.getTargetPackage(), config.getTargetEntity(),
                            config.getTargetEntityWithFilterRule()));
                }
            }
        }
    }

    public PluginPackageAttributeDto buildPluginPackageAttributeDto(PluginPackageAttributes pluginPackageAttribute) {
        PluginPackageAttributeDto pluginPackageAttributeDto = new PluginPackageAttributeDto();
        // pluginPackageAttributeDto.setId(pluginPackageAttribute.getId());
        // pluginPackageAttributeDto.setName(pluginPackageAttribute.getName());
        // pluginPackageAttributeDto.setDescription(pluginPackageAttribute.getDescription());
        // pluginPackageAttributeDto.setDataType(pluginPackageAttribute.getDataType());
        //
        //
        // //TODO
        // pluginPackageAttributeDto.setPackageName(pluginPackageAttribute.getPluginPackageEntities().getPluginPackageDataModel().getPackageName());
        // pluginPackageAttributeDto.setEntityName(pluginPackageAttribute.getPluginPackageEntities().getName());
        // //TODO
        // if (pluginPackageAttribute.getPluginPackageAttribute() != null) {
        // pluginPackageAttributeDto.setRefPackageName(pluginPackageAttribute.getPluginPackageAttribute().getPluginPackageEntity().getPluginPackageDataModel().getPackageName());
        // pluginPackageAttributeDto.setRefEntityName(pluginPackageAttribute.getPluginPackageAttribute().getPluginPackageEntity().getName());
        // pluginPackageAttributeDto.setRefAttributeName(pluginPackageAttribute.getPluginPackageAttribute().getName());
        // }

        return pluginPackageAttributeDto;
    }

    private PluginPackageEntityDto buildPluginPackageEntityDto(PluginPackageEntities pluginPackageEntity) {
        PluginPackageEntityDto pluginPackageEntityDto = new PluginPackageEntityDto();
        // pluginPackageEntityDto.setId(pluginPackageEntity.getId());
        // pluginPackageEntityDto.setPackageName(pluginPackageEntity.getPluginPackageDataModel().getPackageName());
        // pluginPackageEntityDto.setName(pluginPackageEntity.getName());
        // pluginPackageEntityDto.setDisplayName(pluginPackageEntity.getDisplayName());
        // pluginPackageEntityDto.setDescription(pluginPackageEntity.getDescription());
        // pluginPackageEntityDto.setDataModelVersion(pluginPackageEntity.getPluginPackageDataModel().getVersion());
        // if (pluginPackageEntity.getPluginPackageAttributes() != null) {
        // pluginPackageEntity.getPluginPackageAttributes()
        // .forEach(pluginPackageAttribute -> pluginPackageEntityDto.attributes
        // .add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        // }
        return pluginPackageEntityDto;
    }

    private PluginPackageDataModelDto buildPluginPackageDataModelDto(
            PluginPackageDataModel savedPluginPackageDataModel) {
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        // dataModelDto.setId(savedPluginPackageDataModel.getId());
        // dataModelDto.setVersion(savedPluginPackageDataModel.getVersion());
        // dataModelDto.setPackageName(savedPluginPackageDataModel.getPackageName());
        // dataModelDto.setUpdateSource(savedPluginPackageDataModel.getUpdateSource());
        // dataModelDto.setUpdateTime(savedPluginPackageDataModel.getUpdateTime());
        // dataModelDto.setDynamic(savedPluginPackageDataModel.isDynamic());
        // if (savedPluginPackageDataModel.isDynamic()) {
        // dataModelDto.setUpdatePath(savedPluginPackageDataModel.getUpdatePath());
        // dataModelDto.setUpdateMethod(savedPluginPackageDataModel.getUpdateMethod());
        // }
        // if (null != savedPluginPackageDataModel.getPluginPackageEntities() &&
        // savedPluginPackageDataModel.getPluginPackageEntities().size() > 0) {
        // Set<PluginPackageEntityDto> pluginPackageEntities =
        // newLinkedHashSet();
        // savedPluginPackageDataModel.getPluginPackageEntities().forEach(entity->pluginPackageEntities.add(PluginPackageEntityDto.fromDomain(entity)));
        // dataModelDto.setPluginPackageEntities(pluginPackageEntities);
        // }

        return dataModelDto;
    }

    private DataModelEntityDto buildDataModelEntityDto(PluginPackageEntities pluginPackageEntity) {
        DataModelEntityDto dataModelEntityDto = new DataModelEntityDto();
        // dataModelEntityDto.setId(pluginPackageEntity.getId());
        // dataModelEntityDto.setPackageName(pluginPackageEntity.getPluginPackageDataModel().getPackageName());
        // dataModelEntityDto.setName(pluginPackageEntity.getName());
        // dataModelEntityDto.setDisplayName(pluginPackageEntity.getDisplayName());
        // dataModelEntityDto.setDescription(pluginPackageEntity.getDescription());
        // dataModelEntityDto.setDataModelVersion(pluginPackageEntity.getPluginPackageDataModel().getVersion());
        // if (pluginPackageEntity.getPluginPackageAttributeList() != null) {
        // pluginPackageEntity.getPluginPackageAttributeList().forEach(pluginPackageAttribute
        // -> dataModelEntityDto
        // .getAttributes().add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        // }
        return dataModelEntityDto;
    }

    private Map<String, PluginPackageAttributes> buildReferenceAttributeMap(
            PluginPackageDataModel transferredPluginPackageDataModel) {
        Map<String, PluginPackageAttributes> nameToAttributeMap = new HashMap<>();
        List<PluginPackageEntities> entities = transferredPluginPackageDataModel.getPluginPackageEntities();
        for (PluginPackageEntities entity : entities) {
            List<PluginPackageAttributes> pluginPackageAttributes = entity.getPluginPackageAttributes();
            for (PluginPackageAttributes attribute : pluginPackageAttributes) {
                String key = entity.getPackageName() + ATTRIBUTE_KEY_SEPARATOR + entity.getName()
                        + ATTRIBUTE_KEY_SEPARATOR + attribute.getName();
                nameToAttributeMap.put(key, attribute);
            }

        }

        return nameToAttributeMap;
    }

    private Map<String, String> buildAttributeReferenceNameMap(PluginPackageDataModelDto pluginPackageDataModelDto) {
        Map<String, String> attributeReferenceNameMap = new HashMap<>();
        Set<PluginPackageEntityDto> pluginPackageEntities = pluginPackageDataModelDto.getPluginPackageEntities();
        for (PluginPackageEntityDto entityDto : pluginPackageEntities) {
            List<PluginPackageAttributeDto> attributes = entityDto.getAttributes();
            if (attributes == null) {
                continue;
            }

            for (PluginPackageAttributeDto attribute : attributes) {
                if ("ref".equals(attribute.getDataType())) {
                    String key = entityDto.getPackageName() + ATTRIBUTE_KEY_SEPARATOR + entityDto.getName()
                            + ATTRIBUTE_KEY_SEPARATOR + attribute.getName();
                    String val = attribute.getRefPackageName() + ATTRIBUTE_KEY_SEPARATOR + attribute.getRefEntityName()
                            + ATTRIBUTE_KEY_SEPARATOR + attribute.getRefAttributeName();
                    attributeReferenceNameMap.put(key, val);
                }
            }

        }
        return attributeReferenceNameMap;
    }

    private PluginPackageDataModelDto convertDataModelDomainToDto(LazyPluginPackageDataModel dataModel) {
        Set<LazyPluginPackageEntity> entities = newLinkedHashSet();

        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setId(dataModel.getId());
        dataModelDto.setVersion(dataModel.getVersion());
        dataModelDto.setPackageName(dataModel.getPackageName());
        dataModelDto.setUpdateSource(dataModel.getUpdateSource());
        dataModelDto.setUpdateTime(dataModel.getUpdateTime());
        dataModelDto.setDynamic(dataModel.isDynamic());
        if (dataModel.isDynamic()) {
            dataModelDto.setUpdatePath(dataModel.getUpdatePath());
            dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        }
        if (null != dataModel.getPluginPackageEntities() && dataModel.getPluginPackageEntities().size() > 0) {
            Set<PluginPackageEntityDto> pluginPackageEntities = newLinkedHashSet();
            dataModel.getPluginPackageEntities()
                    .forEach(entity -> pluginPackageEntities.add(PluginPackageEntityDto.fromDomain(entity)));
            dataModelDto.setPluginPackageEntities(pluginPackageEntities);
        }
        dataModel.getPluginPackageEntities().forEach(entity -> entities.add(entity));

        dataModelDto.setPluginPackageEntities(Sets.newLinkedHashSet(convertEntityDomainToDto_lazy(entities, true)));

        return dataModelDto;
    }

    private PluginPackageDataModelDto convertDataModelDomainToDto(PluginPackageDataModel dataModel) {
        Set<PluginPackageEntity> entities = newLinkedHashSet();

        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setId(dataModel.getId());
        dataModelDto.setVersion(dataModel.getVersion());
        dataModelDto.setPackageName(dataModel.getPackageName());
        dataModelDto.setUpdateSource(dataModel.getUpdateSource());
        dataModelDto.setUpdateTime(dataModel.getUpdateTime());
        dataModelDto.setDynamic(dataModel.getIsDynamic());
        if (dataModel.getIsDynamic()) {
            dataModelDto.setUpdatePath(dataModel.getUpdatePath());
            dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        }
        if (null != dataModel.getPluginPackageEntities() && dataModel.getPluginPackageEntities().size() > 0) {
            Set<PluginPackageEntityDto> pluginPackageEntities = newLinkedHashSet();
            dataModel.getPluginPackageEntities()
                    .forEach(entity -> pluginPackageEntities.add(PluginPackageEntityDto.fromDomain(entity)));
            dataModelDto.setPluginPackageEntities(pluginPackageEntities);
        }
        dataModel.getPluginPackageEntities().forEach(entity -> entities.add(entity));

        dataModelDto.setPluginPackageEntities(Sets.newLinkedHashSet(convertEntityDomainToDto(entities, true)));

        return dataModelDto;
    }

}
