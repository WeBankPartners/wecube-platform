package com.webank.wecube.platform.core.service.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.QueryResponse;
import com.webank.wecube.platform.core.dto.ResourceItemDto;
import com.webank.wecube.platform.core.dto.ResourceServerDto;
import com.webank.wecube.platform.core.dto.Sorting;
import com.webank.wecube.platform.core.jpa.EntityRepository;
import com.webank.wecube.platform.core.jpa.ResourceItemRepository;
import com.webank.wecube.platform.core.jpa.ResourceServerRepository;
import com.webank.wecube.platform.core.utils.EncryptionUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;

@Service
public class ResourceManagementService {
    public static final String PASSWORD_ENCRYPT_AES_PREFIX = "{AES}";
    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private ResourceServerRepository resourceServerRepository;

    @Autowired
    private ResourceItemRepository resourceItemRepository;

    @Autowired
    private ResourceProperties resourceProperties;

    @Autowired
    private ResourceImplementationService resourceImplementationService;

    public QueryResponse<ResourceServerDto> retrieveServers(QueryRequest queryRequest) {
        queryRequest = applyDefaultSortingAsDesc(queryRequest);
        QueryResponse<ResourceServer> queryResponse = entityRepository.query(ResourceServer.class, queryRequest);
        List<ResourceServerDto> resourceServerDto = Lists.transform(queryResponse.getContents(),
                x -> ResourceServerDto.fromDomain(x));
        return new QueryResponse<>(queryResponse.getPageInfo(), resourceServerDto);
    }

    private QueryRequest applyDefaultSortingAsDesc(QueryRequest queryRequest) {
        if (queryRequest == null) {
            queryRequest = QueryRequest.defaultQueryObject().descendingSortBy("createdDate");
        } else if (queryRequest.getSorting() == null || queryRequest.getSorting().getField() == null) {
            queryRequest.setSorting(new Sorting(false, "createdDate"));
        }
        return queryRequest;
    }

    @Transactional
    public List<ResourceServerDto> createServers(List<ResourceServerDto> resourceServers) {
        Iterable<ResourceServer> savedDomains = resourceServerRepository
                .saveAll(convertServerDtoToDomain(resourceServers));
        return convertServerDomainToDto(savedDomains);
    }

    @Transactional
    public List<ResourceServerDto> updateServers(List<ResourceServerDto> resourceServers) {
        Iterable<ResourceServer> savedDomains = resourceServerRepository
                .saveAll(convertServerDtoToDomain(resourceServers));
        return convertServerDomainToDto(savedDomains);
    }

    @Transactional
    public void deleteServers(List<ResourceServerDto> resourceServers) {
        validateIfServersAreExists(resourceServers);
        List<ResourceServer> domains = convertServerDtoToDomain(resourceServers);
        validateIfServerAllocated(domains);
        resourceServerRepository.deleteAll(convertServerDtoToDomain(resourceServers));
    }

    private void validateIfServersAreExists(List<ResourceServerDto> resourceServers) {
        resourceServers.forEach(server -> {
            if (server.getId() == null || !resourceServerRepository.existsById(server.getId())) {
                throw new WecubeCoreException("3016",
                        String.format("Can not find server with id [%s].", server.getId()), server.getId());
            }
        });
    }

    private void validateIfServerAllocated(List<ResourceServer> resourceServers) {
        resourceServers.forEach(server -> {
            if (server.getIsAllocated() != null && server.getIsAllocated() == 1) {
                throw new WecubeCoreException("3017",
                        String.format("Can not delete resource server [%s] as it has been allocated for [%s].",
                                server.getName(), server.getPurpose()),
                        server.getName(), server.getPurpose());
            }
        });
    }

    public QueryResponse<ResourceItemDto> retrieveItems(QueryRequest queryRequest) {
        queryRequest = applyDefaultSortingAsDesc(queryRequest);
        QueryResponse<ResourceItem> queryResponse = entityRepository.query(ResourceItem.class, queryRequest);
        List<ResourceItemDto> resourceItemsDto = Lists.transform(queryResponse.getContents(),
                x -> ResourceItemDto.fromDomain(x));
        return new QueryResponse<>(queryResponse.getPageInfo(), resourceItemsDto);
    }

    @Transactional
    public List<ResourceItemDto> createItems(List<ResourceItemDto> resourceItems) {
        List<ResourceItem> convertedDomains = convertItemDtoToDomain(resourceItems);
        resourceItemRepository.saveAll(convertedDomains);
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertedDomains);
        resourceImplementationService.createItems(enrichedItems);
        return convertItemDomainToDto(enrichedItems);
    }

    private Iterable<ResourceItem> enrichItemsFullInfo(Iterable<ResourceItem> items) {
        List<ResourceItem> enrichedItems = new ArrayList<>();
        for (ResourceItem item : items) {
            if (item.getId() != null) {
                Optional<ResourceItem> enrichedItemOpt = resourceItemRepository.findById(item.getId());
                if (enrichedItemOpt.isPresent()) {
                    ResourceItem enrichedItem = enrichedItemOpt.get();
                    enrichedItem.setResourceServer(getResourceServerById(enrichedItem.getResourceServerId()));
                    enrichedItems.add(enrichedItem);
                }
            }
        }
        return enrichedItems;
    }

    private ResourceServer getResourceServerById(String resourceServerId) {
        if (resourceServerId != null) {
            Optional<ResourceServer> enrichedServerOpt = resourceServerRepository.findById(resourceServerId);
            if (enrichedServerOpt.isPresent()) {
                return enrichedServerOpt.get();
            }
        }
        return null;
    }

    @Transactional
    public List<ResourceItemDto> updateItems(List<ResourceItemDto> resourceItems) {
        validateIfItemsAreExists(resourceItems);
        List<ResourceItem> convertedDomains = convertItemDtoToDomain(resourceItems);
        resourceItemRepository.saveAll(convertedDomains);
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertedDomains);
        resourceImplementationService.updateItems(enrichedItems);
        return convertItemDomainToDto(enrichedItems);
    }

    @Transactional
    public void deleteItems(List<ResourceItemDto> resourceItems) {
        validateIfItemsAreExists(resourceItems);
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertItemDtoToDomain(resourceItems));
        // validateIfItemAllocated(enrichedItems);
        resourceImplementationService.deleteItems(enrichedItems);
        resourceItemRepository.deleteAll(enrichedItems);
    }

    private void validateIfItemsAreExists(List<ResourceItemDto> resourceItems) {
        resourceItems.forEach(item -> {
            if (item.getId() == null && !resourceItemRepository.existsById(item.getId())) {
                throw new WecubeCoreException("3018", String.format("Can not find item with id [%s].", item.getId()),
                        item.getId());
            }
        });
    }

    private void validateIfItemAllocated(Iterable<ResourceItem> items) {
        items.forEach(item -> {
            if (item.getIsAllocated() != null && item.getIsAllocated() == 1) {
                String msg = String.format("Can not delete resource item [%s] as it has been allocated for [%s].",
                        item.getName(), item.getPurpose());
                throw new WecubeCoreException("3019", msg, item.getName(), item.getPurpose());
            }
        });
    }

    private List<ResourceServerDto> convertServerDomainToDto(Iterable<ResourceServer> savedDomains) {
        List<ResourceServerDto> dtos = new ArrayList<>();
        savedDomains.forEach(domain -> dtos.add(ResourceServerDto.fromDomain(domain)));
        return dtos;
    }

    private List<ResourceServer> convertServerDtoToDomain(List<ResourceServerDto> resourceServerDtos) {
        List<ResourceServer> domains = new ArrayList<>();
        resourceServerDtos.forEach(dto -> {
            ResourceServer existedServer = null;
            if (dto.getId() != null) {
                Optional<ResourceServer> existedServerOpt = resourceServerRepository.findById(dto.getId());
                if (existedServerOpt.isPresent()) {
                    existedServer = existedServerOpt.get();
                }
            }
            handleServerPasswordEncryption(dto);
            ResourceServer domain = ResourceServerDto.toDomain(dto, existedServer);
            domains.add(domain);
        });
        return domains;
    }

    private void handleServerPasswordEncryption(ResourceServerDto dto) {
        if (dto.getLoginPassword() != null) {
            String password = dto.getLoginPassword();
            if (!password.startsWith(PASSWORD_ENCRYPT_AES_PREFIX)) {
                password = EncryptionUtils.encryptWithAes(dto.getLoginPassword(),
                        resourceProperties.getPasswordEncryptionSeed(), dto.getName());
                password = PASSWORD_ENCRYPT_AES_PREFIX + password;
            }
            dto.setLoginPassword(password);
        }
    }

    private String generateMysqlDatabaseDefaultAccount(ResourceItemDto dto) {
        String defaultAdditionalProperties;
        String encryptedPassword = EncryptionUtils.encryptWithAes(dto.getName(),
                resourceProperties.getPasswordEncryptionSeed(), dto.getName());
        encryptedPassword = PASSWORD_ENCRYPT_AES_PREFIX + encryptedPassword;
        Map<Object, Object> map = new HashMap<>();
        map.put("username", dto.getName());
        map.put("password", encryptedPassword);
        defaultAdditionalProperties = JsonUtils.toJsonString(map);
        return defaultAdditionalProperties;
    }

    private void handleItemPasswordEncryption(ResourceItemDto dto) {
        Map<String, String> additionalProperties = dto.getAdditionalPropertiesMap();
        if (additionalProperties == null || additionalProperties.isEmpty()) {
            String defaultAdditionalProperties = null;
            if (ResourceItemType.fromCode(dto.getType()) == ResourceItemType.MYSQL_DATABASE) {
                defaultAdditionalProperties = generateMysqlDatabaseDefaultAccount(dto);
            }
            dto.setAdditionalProperties(defaultAdditionalProperties);
        } else {
            String password = additionalProperties.get("password");
            if (password != null) {
                String encryptedPassword = null;
                if (password.startsWith(PASSWORD_ENCRYPT_AES_PREFIX)) {
                    encryptedPassword = password;
                } else {
                    encryptedPassword = PASSWORD_ENCRYPT_AES_PREFIX + EncryptionUtils.encryptWithAes(password,
                            resourceProperties.getPasswordEncryptionSeed(), dto.getName());
                }

                additionalProperties.put("password", encryptedPassword);
                dto.setAdditionalProperties(JsonUtils.toJsonString(additionalProperties));
            }
        }
    }

    private List<ResourceItemDto> convertItemDomainToDto(Iterable<ResourceItem> savedDomains) {
        List<ResourceItemDto> dtos = new ArrayList<>();
        savedDomains.forEach(domain -> dtos.add(ResourceItemDto.fromDomain(domain)));
        return dtos;
    }

    private List<ResourceItem> convertItemDtoToDomain(List<ResourceItemDto> resourceItemDtos) {
        List<ResourceItem> domains = new ArrayList<>();
        resourceItemDtos.forEach(dto -> {
            ResourceItem existedItem = null;
            if (dto.getId() != null) {
                Optional<ResourceItem> existedItemOpt = resourceItemRepository.findById(dto.getId());
                if (existedItemOpt.isPresent()) {
                    existedItem = existedItemOpt.get();
                }
            }
            handleItemPasswordEncryption(dto);
            ResourceItem domain = ResourceItemDto.toDomain(dto, existedItem);
            domains.add(domain);
        });
        return domains;
    }

}
