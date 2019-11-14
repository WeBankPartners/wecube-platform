package com.webank.wecube.platform.core.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.support.PluginPackageDataModelHelper;
import com.webank.wecube.platform.core.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

import static com.google.common.collect.Sets.newLinkedHashSet;

@Service
@Transactional
public class PluginPackageDataModelServiceImpl implements PluginPackageDataModelService {
    private static final String dataModelUrl = "http://{gatewayUrl}/{packageName}/{dataModelUrl}";

    public static final String ATTRIBUTE_KEY_SEPARATOR = "`";
    @Autowired
    private PluginPackageDataModelRepository dataModelRepository;
    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    private PluginPackageAttributeRepository pluginPackageAttributeRepository;
    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(PluginPackageDataModelServiceImpl.class);

    @Override
    public PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto) {
        return register(pluginPackageDataModelDto, false);
    }

    @Override
    public PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto, boolean fromDynamicUpdate) {
        Optional<PluginPackage> latestPackageByName = pluginPackageRepository.findLatestVersionByName(pluginPackageDataModelDto.getPackageName());
        if (!latestPackageByName.isPresent()) {
            String msg = String.format("Cannot find the package [%s] while registering data model", pluginPackageDataModelDto.getPackageName());
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        if (fromDynamicUpdate) {
            pluginPackageDataModelDto.setDynamic(true);
            pluginPackageDataModelDto.setUpdateTime(System.currentTimeMillis());
            pluginPackageDataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
        }
        Optional<PluginPackageDataModel> pluginPackageDataModelOptional = dataModelRepository.findLatestDataModelByPackageName(pluginPackageDataModelDto.getPackageName());
        int newDataModelVersion = 1;
        if (pluginPackageDataModelOptional.isPresent()){
            PluginPackageDataModel existingDataModelDomain = pluginPackageDataModelOptional.get();
            if (PluginPackageDataModelHelper.isDataModelSameAsAnother(pluginPackageDataModelDto, existingDataModelDomain)) {
                throw new WecubeCoreException("Refreshed data model is same as existing latest one.");
            }
            newDataModelVersion = existingDataModelDomain.getVersion() + 1;
        }
        pluginPackageDataModelDto.setVersion(newDataModelVersion);
        PluginPackageDataModel transferredPluginPackageDataModel = PluginPackageDataModelDto.toDomain(pluginPackageDataModelDto);

        if (null != pluginPackageDataModelDto.getPluginPackageEntities() && pluginPackageDataModelDto.getPluginPackageEntities().size() > 0) {
            Map<String, String> attributeReferenceNameMap = buildAttributeReferenceNameMap(pluginPackageDataModelDto);
            Map<String, PluginPackageAttribute> referenceAttributeMap = buildReferenceAttributeMap(transferredPluginPackageDataModel);
            updateAttributeReference(transferredPluginPackageDataModel, attributeReferenceNameMap, referenceAttributeMap);
        }

        PluginPackageDataModel savedPluginPackageDataModel = dataModelRepository.save(transferredPluginPackageDataModel);
        if (fromDynamicUpdate) {
            PluginPackage pluginPackage = pluginPackageRepository.findLatestVersionByName(pluginPackageDataModelDto.getPackageName()).get();
            pluginPackage.setStatus(PluginPackage.Status.UNREGISTERED);
            pluginPackageRepository.save(pluginPackage);
        }

        return PluginPackageDataModelDto.fromDomain(savedPluginPackageDataModel);
    }

    private Map<String, PluginPackageAttribute> buildReferenceAttributeMap(PluginPackageDataModel transferredPluginPackageDataModel) {
        Map<String, PluginPackageAttribute> nameToAttributeMap = new HashMap<>();
        transferredPluginPackageDataModel.getPluginPackageEntities()
                .forEach(entity->
                        entity.getPluginPackageAttributeList()
                                .forEach(attribute->
                                        nameToAttributeMap.put(entity.getPackageName()+ ATTRIBUTE_KEY_SEPARATOR + entity.getName() + ATTRIBUTE_KEY_SEPARATOR + attribute.getName(), attribute)));

        return nameToAttributeMap;
    }

    private Map<String, String> buildAttributeReferenceNameMap(PluginPackageDataModelDto pluginPackageDataModelDto) {
        Map<String, String> attributeReferenceNameMap = new HashMap<>();
        pluginPackageDataModelDto.getPluginPackageEntities().forEach(entityDto->
            entityDto.getAttributes()
                    .stream()
                    .filter(attribute->"ref".equals(attribute.getDataType()))
                    .forEach(attribute->
                            attributeReferenceNameMap.put(entityDto.getPackageName()+ ATTRIBUTE_KEY_SEPARATOR +entityDto.getName()+ ATTRIBUTE_KEY_SEPARATOR +attribute.getName(),
                                    attribute.getRefPackageName() + ATTRIBUTE_KEY_SEPARATOR + attribute.getRefEntityName() + ATTRIBUTE_KEY_SEPARATOR + attribute.getRefAttributeName())));
        return attributeReferenceNameMap;
    }

    /**
     * Plugin model overview
     *
     * @return an list of data model DTOs consist of entity dtos which contain both entities and attributes
     */
    @Override
    public Set<PluginPackageDataModelDto> overview() {
        Set<PluginPackageDataModelDto> pluginPackageDataModelDtos = newLinkedHashSet();
        Optional<List<String>> allPackageNamesOptional = pluginPackageRepository.findAllDistinctPackage();
        allPackageNamesOptional.ifPresent(allPackageNames -> {
            for (String packageName : allPackageNames) {
                Optional<PluginPackageDataModel> pluginPackageDataModelOptional = dataModelRepository.findLatestDataModelByPackageName(packageName);
                if (pluginPackageDataModelOptional.isPresent()) {
                    pluginPackageDataModelDtos.add(convertDataModelDomainToDto(pluginPackageDataModelOptional.get()));
                }
            }
        });

        return pluginPackageDataModelDtos;
    }

    private PluginPackageDataModelDto convertDataModelDomainToDto(PluginPackageDataModel dataModel) {
        Set<PluginPackageEntity> entities = newLinkedHashSet();

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
            dataModel.getPluginPackageEntities().forEach(entity->pluginPackageEntities.add(PluginPackageEntityDto.fromDomain(entity)));
            dataModelDto.setPluginPackageEntities(pluginPackageEntities);
        }
        dataModel.getPluginPackageEntities().forEach(entity -> entities.add(entity));

        dataModelDto.setPluginPackageEntities(Sets.newLinkedHashSet(convertEntityDomainToDto(entities, true)));

        return dataModelDto;
    }

    /**
     * View one data model entity with its relationship by packageName
     *
     * @param packageName the name of package
     * @return list of entity dto
     */
    @Override
    public PluginPackageDataModelDto packageView(String packageName) throws WecubeCoreException {
        Optional<PluginPackage> latestPluginPackageByName = pluginPackageRepository.findLatestVersionByName(packageName);
        if (!latestPluginPackageByName.isPresent()) {
            String msg = String.format("Plugin package with name [%s] is not found", packageName);
            logger.info(msg);
            return null;
        }
        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(packageName);
        if (!latestDataModelByPackageName.isPresent()) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }

        return convertDataModelDomainToDto(latestDataModelByPackageName.get());
    }

    /**
     * Update candidate entity list according to the reference mapping
     *
     * @param pluginPackageDataModel the candidate pluginPackageDataModel, will be inserted to DB later
     * @param referenceNameMap    map "{package}`{entity}`{attribute}" to another "{package}`{entity}`{attribute}"
     * @param nameToAttributeMap  map "{package}`{entity}`{attribute}" to attribute domain object
     * @throws WecubeCoreException when reference name the dto passed is invalid
     */
    private void updateAttributeReference(PluginPackageDataModel pluginPackageDataModel, Map<String, String> referenceNameMap,
                                          Map<String, PluginPackageAttribute> nameToAttributeMap) throws WecubeCoreException {
        // update the attribtue domain object with pre-noted map
        for (PluginPackageEntity candidateEntity : pluginPackageDataModel.getPluginPackageEntities()) {

            for (PluginPackageAttribute pluginPackageAttribute : candidateEntity
                    .getPluginPackageAttributeList()) {
                String selfName = pluginPackageAttribute.getPluginPackageEntity().getPackageName() + ATTRIBUTE_KEY_SEPARATOR
                        + pluginPackageAttribute.getPluginPackageEntity().getName() + ATTRIBUTE_KEY_SEPARATOR
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
                        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(referencePackageName);
                        if (!latestDataModelByPackageName.isPresent()) {
                            String msg = String.format("Cannot found the specified data model with package name: [%s]", referencePackageName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }
                        PluginPackageDataModel dataModel = latestDataModelByPackageName.get();
                        Optional<PluginPackageEntity> foundReferenceEntityOptional = dataModel.getPluginPackageEntities().stream().filter(entity -> referenceEntityName.equals(entity.getName())).findAny();

                        if (!foundReferenceEntityOptional.isPresent()) {
                            String msg = String.format("Cannot found the specified plugin model entity with package name: [%s], entity name: [%s]", referencePackageName, referenceEntityName);
                            logger.error(msg);
                            throw new WecubeCoreException(msg);
                        }

                        Optional<PluginPackageAttribute> pluginPackageAttributeOptional = foundReferenceEntityOptional.get().getPluginPackageAttributeList().stream().filter(attribute -> referenceAttributeName.equals(attribute.getName())).findAny();
                        if (pluginPackageAttributeOptional.isPresent()) {
                            pluginPackageAttribute.setPluginPackageAttribute(pluginPackageAttributeOptional.get());
                        } else {
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
            int dataModelVersion = 0;

            Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(packageName);
            if (latestDataModelByPackageName.isPresent()) {
                dataModelVersion = latestDataModelByPackageName.get().getVersion();
            }
            // find "reference by" info by latest data model version
            Optional<List<PluginPackageAttribute>> allAttributeReferenceByList = pluginPackageAttributeRepository.findAllChildrenAttributes(packageName, entityName, dataModelVersion);

            allAttributeReferenceByList.ifPresent(attributeList -> attributeList.forEach(attribute -> {
                // the process of found reference by info
                PluginPackageEntity referenceByEntity = attribute.getPluginPackageEntity();
                if (!packageName.equals(referenceByEntity.getPackageName()) ||
                        !entityName.equals(referenceByEntity.getName())) {
                    // only add the dto to set when the attribute doesn't belong to this input entity
                    inputEntityDto.updateReferenceBy(
                            referenceByEntity.getId(),
                            referenceByEntity.getPackageName(),
                            referenceByEntity.getDataModelVersion(),
                            referenceByEntity.getName(),
                            referenceByEntity.getDisplayName());
                }
            }));

            // query for the referenceTo info
            List<PluginPackageAttributeDto> attributes = inputEntityDto.getAttributes();
            if (!CollectionUtils.isEmpty(attributes)) {
                attributes.forEach(attributeDto-> {
                            dataModelRepository.findLatestDataModelByPackageName(attributeDto.getRefPackageName()).ifPresent(dataModel ->
                                    dataModel.getPluginPackageEntities().stream().filter(entity -> attributeDto.getRefEntityName().equals(entity.getName())).findAny().ifPresent(entity -> {
                                        PluginPackageEntityDto entityReferenceToDto = PluginPackageEntityDto.fromDomain(entity);
                                        inputEntityDto.updateReferenceTo(
                                                entityReferenceToDto.getId(),
                                                entityReferenceToDto.getPackageName(),
                                                entityReferenceToDto.getDataModelVersion(),
                                                entityReferenceToDto.getName(),
                                                entityReferenceToDto.getDisplayName()
                                        );
                                    }));
                        }
                );
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

    @Override
    public PluginPackageDataModelDto pullDynamicDataModel(String packageName) {
        Optional<PluginPackage> latestPluginPackageByName = pluginPackageRepository.findLatestVersionByName(packageName);
        if (!latestPluginPackageByName.isPresent()) {
            String errorMessage = String.format("Plugin package with name [%s] is not found", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }

        Optional<PluginPackageDataModel> latestDataModelByPackageName = dataModelRepository.findLatestDataModelByPackageName(packageName);
        if (!latestDataModelByPackageName.isPresent()) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            logger.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }

        PluginPackageDataModel dataModel = latestDataModelByPackageName.get();
        if (!dataModel.isDynamic()) {
            String message = String.format("DataMode does not support dynamic update for package: [%s]", packageName);
            logger.error(message);
            throw new WecubeCoreException(message);
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

        if (null != dynamicPluginPackageEntities && dynamicPluginPackageEntities.size() > 0) {
            dynamicPluginPackageEntities.forEach(entity-> {
                entity.setPackageName(packageName);
                entity.setDataModelVersion(newDataModelVersion);
                entity.getAttributes().forEach(attribute-> {
                    attribute.setPackageName(packageName);
                    if (StringUtils.isNotBlank(attribute.getRefAttributeName())) {
                        if (StringUtils.isBlank(attribute.getRefPackageName())) {
                            attribute.setRefPackageName(packageName);
                        }
                        if (StringUtils.isBlank(attribute.getRefEntityName())) {
                            attribute.setRefEntityName(entity.getName());
                        }
                    }
                        }
                );
            });
        }
        dataModelDto.setPluginPackageEntities(dynamicPluginPackageEntities);


        return dataModelDto;
    }

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

            ResponseEntity<String> response = restTemplate.exchange(uriComponents.toString(), method, requestEntity, String.class);
            if (StringUtils.isBlank(response.getBody()) || response.getStatusCode().isError()) {
                throw new WecubeCoreException(response.toString());
            }
            JsonResponse responseDto = JsonUtils.toObject(response.getBody(), JsonResponse.class);
            if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
                String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
                logger.error(msg);
                throw new WecubeCoreException(msg);
            }
            dynamicPluginPackageEntities = JsonUtils.toList(JsonUtils.toJsonString(responseDto.getData()), PluginPackageEntityDto.class);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return Sets.newLinkedHashSet(dynamicPluginPackageEntities);
    }

}
