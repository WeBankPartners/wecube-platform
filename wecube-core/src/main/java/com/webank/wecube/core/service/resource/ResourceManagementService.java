package com.webank.wecube.core.service.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;
import com.webank.wecube.core.domain.ResourceServer;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.QueryResponse;
import com.webank.wecube.core.dto.ResourceItemDto;
import com.webank.wecube.core.dto.ResourceServerDto;
import com.webank.wecube.core.jpa.EntityRepository;
import com.webank.wecube.core.jpa.ResourceItemRepository;
import com.webank.wecube.core.jpa.ResourceServerRepository;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.utils.EncryptionUtils;
import com.webank.wecube.core.utils.JsonUtils;

@Service
public class ResourceManagementService {

    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private ResourceServerRepository resourceServerRepository;

    @Autowired
    private ResourceItemRepository resourceItemRepository;

    @Autowired
    private ResourceImplementationService resourceImplementationService;

    @Autowired
    private CmdbResourceService cmdbResourceService;

    public QueryResponse<ResourceServerDto> retrieveServers(QueryRequest queryRequest) {
        QueryResponse<ResourceServer> queryResponse = entityRepository.query(ResourceServer.class, queryRequest);
        List<ResourceServerDto> resourceServerDto = Lists.transform(queryResponse.getContents(), x -> ResourceServerDto.fromDomain(x));
        return new QueryResponse<>(queryResponse.getPageInfo(), resourceServerDto);
    }

    @Transactional
    public List<ResourceServerDto> createServers(List<ResourceServerDto> resourceServers) {
        Iterable<ResourceServer> savedDomains = resourceServerRepository.saveAll(convertServerDtoToDomain(resourceServers));
        return convertServerDomainToDto(savedDomains);
    }

    @Transactional
    public List<ResourceServerDto> updateServers(List<ResourceServerDto> resourceServers) {
        Iterable<ResourceServer> savedDomains = resourceServerRepository.saveAll(convertServerDtoToDomain(resourceServers));
        return convertServerDomainToDto(savedDomains);
    }

    @Transactional
    public void deleteServers(List<ResourceServerDto> resourceServers) {
        validateIfServerAllocated(resourceServers);
        resourceServerRepository.deleteAll(convertServerDtoToDomain(resourceServers));
    }

    private void validateIfServerAllocated(List<ResourceServerDto> resourceServers) {
        resourceServers.forEach(server -> {
            if (server.getIsAllocated()) {
                throw new WecubeCoreException(String.format("Can not delete resource server [%s] as it has been allocated for [%s].", server.getName(), server.getPurpose()));
            }
        });
    }

    public QueryResponse<ResourceItemDto> retrieveItems(QueryRequest queryRequest) {
        QueryResponse<ResourceItem> queryResponse = entityRepository.query(ResourceItem.class, queryRequest);
        List<ResourceItemDto> resourceItemsDto = Lists.transform(queryResponse.getContents(), x -> ResourceItemDto.fromDomain(x));
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

    private ResourceServer getResourceServerById(Integer resourceServerId) {
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
        List<ResourceItem> convertedDomains = convertItemDtoToDomain(resourceItems);
        resourceItemRepository.saveAll(convertedDomains);
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertedDomains);
        resourceImplementationService.updateItems(enrichedItems);
        return convertItemDomainToDto(enrichedItems);
    }

    @Transactional
    public void deleteItems(List<ResourceItemDto> resourceItems) {
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertItemDtoToDomain(resourceItems));
        resourceImplementationService.deleteItems(enrichedItems);
        resourceItemRepository.deleteAll(enrichedItems);
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
            ResourceServer domain = ResourceServerDto.toDomain(dto, existedServer);
            handlePasswordEncryption(domain);
            domains.add(domain);
        });
        return domains;
    }

    private void handlePasswordEncryption(ResourceServer domain) {
        if (domain.getLoginPassword() != null) {
            try {
                domain.setLoginPassword(EncryptionUtils.encryptWithAes(domain.getLoginPassword(), cmdbResourceService.getSeedFromSystemEnum(), domain.getName()));
            } catch (Exception e) {
                throw new WecubeCoreException(String.format("Failed to encrypt password, meet error [%s]", e.getMessage()), e);
            }
        }
    }

    private void handlePasswordEncryption(ResourceItem domain) {
        Map<String, String> additionalProperties = domain.getAdditionalPropertiesMap();
        if (additionalProperties.get("password") != null) {
            try {
                String encryptedPassword = EncryptionUtils.encryptWithAes(additionalProperties.get("password"), cmdbResourceService.getSeedFromSystemEnum(), domain.getName());
                additionalProperties.put("password", encryptedPassword);
                domain.setAdditionalProperties(JsonUtils.toJsonString(additionalProperties));
            } catch (Exception e) {
                throw new WecubeCoreException(String.format("Failed to encrypt password, meet error [%s]", e.getMessage()), e);
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
            ResourceItem domain = ResourceItemDto.toDomain(dto, existedItem);
            handlePasswordEncryption(domain);
            domains.add(domain);
        });
        return domains;
    }

}
