package com.webank.wecube.platform.core.service.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.plugin.ResourceItemDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceServerDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceServerProductSerialDto;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.repository.plugin.ResourceItemMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceServerMapper;
import com.webank.wecube.platform.core.service.cmder.ssh2.CommandService;
import com.webank.wecube.platform.core.service.cmder.ssh2.RemoteCommandExecutorConfig;
import com.webank.wecube.platform.core.service.plugin.PluginPageableDataService;
import com.webank.wecube.platform.core.utils.Constants;
import com.webank.wecube.platform.core.utils.EncryptionUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class ResourceManagementService {
    private static final Logger log = LoggerFactory.getLogger(ResourceManagementService.class);

    @Autowired
    private ResourceServerMapper resourceServerRepository;

    @Autowired
    private ResourceItemMapper resourceItemRepository;

    @Autowired
    private ResourceProperties resourceProperties;

    @Autowired
    private ResourceImplementationService resourceImplementationService;

    @Autowired
    private PluginPageableDataService pluginPageableDataService;

    @Autowired
    private CommandService commandService;

    /**
     * 
     */
    public ResourceServerProductSerialDto retrieveResourceServerProductSerial(String resourceServerId) {
        if (StringUtils.isBlank(resourceServerId)) {
            return null;
        }

        ResourceServer existResourceServer = resourceServerRepository.selectByPrimaryKey(resourceServerId);
        if (existResourceServer == null) {
            throw new WecubeCoreException("Such resource server does not exist with id:" + resourceServerId);
        }

        String host = existResourceServer.getHost();
        String password = decryptPassword(existResourceServer);
        String user = existResourceServer.getLoginUsername();
        int port = Integer.parseInt(existResourceServer.getPort());
        String sshKey = decryptSshKey(existResourceServer);

        String productSerial = "";
        String cmd = "cat /sys/class/dmi/id/product_serial";

        try {
            //TODO
            RemoteCommandExecutorConfig sshConfig = new RemoteCommandExecutorConfig();
            sshConfig.setAuthMode(existResourceServer.getLoginMode());
            sshConfig.setPort(port);
            sshConfig.setPsword(password);
            sshConfig.setRemoteHost(host);
            sshConfig.setUser(user);
            sshConfig.setSshKey(sshKey);
            productSerial = commandService.runAtRemote(sshConfig, cmd);
        } catch (Exception e) {
            log.error("errors while running remote command:{}", cmd);
        }

        ResourceServerProductSerialDto dto = new ResourceServerProductSerialDto();
        dto.setProductSerial(productSerial);
        dto.setHost(existResourceServer.getHost());
        dto.setId(existResourceServer.getId());
        dto.setLoginUsername(existResourceServer.getLoginUsername());
        dto.setPort(existResourceServer.getPort());
        dto.setPurpose(existResourceServer.getPurpose());
        dto.setStatus(existResourceServer.getStatus());
        dto.setName(existResourceServer.getName());
        return dto;
    }

    /**
     * 
     * @param queryRequest
     * @return
     */
    public QueryResponse<ResourceServerDto> retrieveServers(QueryRequestDto queryRequest) {

        com.github.pagehelper.PageInfo<ResourceServer> pageInfo = pluginPageableDataService
                .retrieveResourceServers(queryRequest);

        List<ResourceServerDto> resultDataList = new ArrayList<>();
        for (ResourceServer e : pageInfo.getList()) {
            ResourceServerDto dto = buildResourceServerDto(e);
            resultDataList.add(dto);
        }

        com.webank.wecube.platform.core.dto.plugin.PageInfo respPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        respPageInfo.setPageSize(queryRequest.getPageable().getPageSize());
        respPageInfo.setStartIndex(queryRequest.getPageable().getStartIndex());
        respPageInfo.setTotalRows((int) pageInfo.getTotal());
        return new QueryResponse<>(respPageInfo, resultDataList);

    }

    /**
     * 
     * @param queryRequest
     * @return
     */
    public QueryResponse<ResourceItemDto> retrieveItems(QueryRequestDto queryRequest) {

        com.github.pagehelper.PageInfo<ResourceItem> pageInfo = pluginPageableDataService
                .retrieveResourceItems(queryRequest);

        List<ResourceItemDto> resultDataList = new ArrayList<>();
        for (ResourceItem e : pageInfo.getList()) {
            if (StringUtils.isNoneBlank(e.getResourceServerId())) {
                ResourceServer resourceServer = resourceServerRepository.selectByPrimaryKey(e.getResourceServerId());
                e.setResourceServer(resourceServer);
            }
            ResourceItemDto dto = buildResourceItemDto(e);
            resultDataList.add(dto);
        }

        com.webank.wecube.platform.core.dto.plugin.PageInfo respPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        respPageInfo.setPageSize(queryRequest.getPageable().getPageSize());
        respPageInfo.setStartIndex(queryRequest.getPageable().getStartIndex());
        respPageInfo.setTotalRows((int) pageInfo.getTotal());
        return new QueryResponse<>(respPageInfo, resultDataList);
    }

    /**
     * 
     * @param resourceServers
     * @return
     */
    @Transactional
    public List<ResourceServerDto> createServers(List<ResourceServerDto> resourceServers) {

        List<ResourceServer> savedDomains = convertServerDtoToDomain(resourceServers);
        for (ResourceServer s : savedDomains) {
            if (StringUtils.isBlank(s.getId())) {
                s.setId(LocalIdGenerator.generateId());
                resourceServerRepository.insert(s);
            } else {
                resourceServerRepository.updateByPrimaryKeySelective(s);
            }
        }
        return convertServerDomainToDto(savedDomains);
    }

    /**
     * 
     * @param resourceServers
     * @return
     */
    @Transactional
    public List<ResourceServerDto> updateServers(List<ResourceServerDto> resourceServers) {

        List<ResourceServer> savedDomains = convertServerDtoToDomain(resourceServers);
        for (ResourceServer savedDomain : savedDomains) {
            if (StringUtils.isNoneBlank(savedDomain.getId())) {
                resourceServerRepository.updateByPrimaryKeySelective(savedDomain);
            } else {
                savedDomain.setId(LocalIdGenerator.generateId());
                resourceServerRepository.insert(savedDomain);
            }
        }

        return convertServerDomainToDto(savedDomains);
    }

    /**
     * 
     * @param resourceServers
     */
    @Transactional
    public void deleteServers(List<ResourceServerDto> resourceServers) {
        validateIfServersAreExists(resourceServers);
        List<ResourceServer> domains = convertServerDtoToDomain(resourceServers);
        validateIfServerAllocated(domains);

        for (ResourceServerDto dto : resourceServers) {
            if (StringUtils.isBlank(dto.getId())) {
                continue;
            } else {
                resourceServerRepository.deleteByPrimaryKey(dto.getId());
            }
        }

    }

    /**
     * 
     * @param resourceItems
     * @return
     */
    @Transactional
    public List<ResourceItemDto> createItems(List<ResourceItemDto> resourceItems) {
        List<ResourceItem> convertedDomains = convertItemDtoToDomain(resourceItems);
        for (ResourceItem item : convertedDomains) {
            if (StringUtils.isBlank(item.getId())) {
                item.setId(LocalIdGenerator.generateId());
                resourceItemRepository.insert(item);
            } else {
                resourceItemRepository.updateByPrimaryKeySelective(item);
            }
        }
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertedDomains);
        resourceImplementationService.createItems(enrichedItems);
        return convertItemDomainToDto(enrichedItems);
    }

    /**
     * 
     * @param resourceItems
     * @return
     */
    @Transactional
    public List<ResourceItemDto> updateItems(List<ResourceItemDto> resourceItems) {
        validateIfItemsAreExists(resourceItems);
        List<ResourceItem> convertedDomains = convertItemDtoToDomain(resourceItems);
        for (ResourceItem item : convertedDomains) {
            if (StringUtils.isBlank(item.getId())) {
                item.setId(LocalIdGenerator.generateId());
                resourceItemRepository.insert(item);
            } else {
                resourceItemRepository.updateByPrimaryKeySelective(item);
            }
        }
        Iterable<ResourceItem> enrichedItems = enrichItemsFullInfo(convertedDomains);
        resourceImplementationService.updateItems(enrichedItems);
        return convertItemDomainToDto(enrichedItems);
    }

    /**
     * 
     * @param resourceItems
     */
    @Transactional
    public void deleteItems(List<ResourceItemDto> resourceItems) {
        validateIfItemsAreExists(resourceItems);
        List<ResourceItem> enrichedItems = enrichItemsFullInfo(convertItemDtoToDomain(resourceItems));
        // validateIfItemAllocated(enrichedItems);
        resourceImplementationService.deleteItems(enrichedItems);

        if (enrichedItems != null) {
            for (ResourceItem item : enrichedItems) {
                resourceItemRepository.deleteByPrimaryKey(item.getId());
            }
        }
    }

    private ResourceItemDto buildResourceItemDto(ResourceItem e) {
        ResourceItemDto dto = ResourceItemDto.fromDomain(e);
        return dto;
    }

    private void validateIfItemsAreExists(List<ResourceItemDto> resourceItems) {
        for (ResourceItemDto item : resourceItems) {
            if (StringUtils.isBlank(item.getId())) {
                String errMsg = String.format("Can not find item with id [%s].", item.getId());
                throw new WecubeCoreException("3018", errMsg, item.getId());
            }

            ResourceItem resourceItemEntity = resourceItemRepository.selectByPrimaryKey(item.getId());
            if (resourceItemEntity == null) {
                String errMsg = String.format("Can not find item with id [%s].", item.getId());
                throw new WecubeCoreException("3018", errMsg, item.getId());
            }
        }
    }

    private void validateIfServersAreExists(List<ResourceServerDto> resourceServers) {
        for (ResourceServerDto dto : resourceServers) {
            if (dto.getId() == null) {
                throw new WecubeCoreException("3016", String.format("Can not find server with id [%s].", dto.getId()),
                        dto.getId());
            }

            ResourceServer entity = resourceServerRepository.selectByPrimaryKey(dto.getId());
            if (entity == null) {
                throw new WecubeCoreException("3016", String.format("Can not find server with id [%s].", dto.getId()),
                        dto.getId());
            }
        }
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
    
    private String decryptSshKey(ResourceServer s) {
        String sshKey = s.getSshKey();
        if (StringUtils.isBlank(sshKey)) {
            return sshKey;
        }
        
        String plainSshKey = EncryptionUtils.decryptAesPrefixedStringForcely(sshKey, resourceProperties.getPasswordEncryptionSeed(),
                s.getName());

        return plainSshKey;
    }

    private String decryptPassword(ResourceServer s) {
        String password = s.getLoginPassword();
        if (StringUtils.isBlank(password)) {
            return null;
        }
        
        String plainPassword = EncryptionUtils.decryptAesPrefixedStringForcely(password, resourceProperties.getPasswordEncryptionSeed(),
                s.getName());

        return plainPassword;
    }

    private ResourceServerDto buildResourceServerDto(ResourceServer e) {
        ResourceServerDto dto = ResourceServerDto.fromDomain(e);
        return dto;
    }

    private List<ResourceServerDto> convertServerDomainToDto(List<ResourceServer> savedDomains) {
        List<ResourceServerDto> dtos = new ArrayList<>();
        savedDomains.forEach(domain -> dtos.add(ResourceServerDto.fromDomain(domain)));
        return dtos;
    }

    private List<ResourceServer> convertServerDtoToDomain(List<ResourceServerDto> resourceServerDtos) {
        List<ResourceServer> domains = new ArrayList<>();
        for (ResourceServerDto dto : resourceServerDtos) {
            ResourceServer existedServer = null;
            if (dto.getId() != null) {
                ResourceServer existedServerOpt = resourceServerRepository.selectByPrimaryKey(dto.getId());
                if (existedServerOpt != null) {
                    existedServer = existedServerOpt;
                }
            }
            handleServerPasswordEncryption(dto);
            handleServerSshKeyEncryption(dto);
            ResourceServer domain = ResourceServerDto.toDomain(dto, existedServer);

            domains.add(domain);
        }
        return domains;
    }

    private void handleServerPasswordEncryption(ResourceServerDto dto) {
        if (dto.getLoginPassword() != null) {
            String password = dto.getLoginPassword();
            if (!password.startsWith(Constants.PASSWORD_ENCRYPT_AES_PREFIX)) {
                password = EncryptionUtils.encryptWithAes(dto.getLoginPassword(),
                        resourceProperties.getPasswordEncryptionSeed(), dto.getName());
                password = Constants.PASSWORD_ENCRYPT_AES_PREFIX + password;
            }
            dto.setLoginPassword(password);
        }
    }

    private void handleServerSshKeyEncryption(ResourceServerDto dto) {
        if (StringUtils.isNoneBlank(dto.getSshKey())) {
            String sshKey = dto.getSshKey();
            if (!sshKey.startsWith(Constants.PASSWORD_ENCRYPT_AES_PREFIX)) {
                sshKey = EncryptionUtils.encryptWithAes(dto.getLoginPassword(),
                        resourceProperties.getPasswordEncryptionSeed(), dto.getName());
                sshKey = Constants.PASSWORD_ENCRYPT_AES_PREFIX + sshKey;
            }
            dto.setSshKey(sshKey);
        }
    }

    private String generateMysqlDatabaseDefaultAccount(ResourceItemDto dto) {
        String defaultAdditionalProperties;
        String encryptedPassword = EncryptionUtils.encryptWithAes(dto.getName(),
                resourceProperties.getPasswordEncryptionSeed(), dto.getName());
        encryptedPassword = Constants.PASSWORD_ENCRYPT_AES_PREFIX + encryptedPassword;
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
                if (password.startsWith(Constants.PASSWORD_ENCRYPT_AES_PREFIX)) {
                    encryptedPassword = password;
                } else {
                    encryptedPassword = Constants.PASSWORD_ENCRYPT_AES_PREFIX + EncryptionUtils.encryptWithAes(password,
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
                ResourceItem existedItemOpt = resourceItemRepository.selectByPrimaryKey(dto.getId());
                if (existedItemOpt != null) {
                    existedItem = existedItemOpt;
                }
            }
            handleItemPasswordEncryption(dto);
            ResourceItem domain = ResourceItemDto.toDomain(dto, existedItem);
            domains.add(domain);
        });
        return domains;
    }

    private List<ResourceItem> enrichItemsFullInfo(List<ResourceItem> items) {
        List<ResourceItem> enrichedItems = new ArrayList<>();
        for (ResourceItem item : items) {
            if (item.getId() != null) {
                ResourceItem enrichedItemOpt = resourceItemRepository.selectByPrimaryKey(item.getId());
                if (enrichedItemOpt != null) {
                    ResourceItem enrichedItem = enrichedItemOpt;
                    enrichedItem.setResourceServer(getResourceServerById(enrichedItem.getResourceServerId()));
                    enrichedItems.add(enrichedItem);
                }
            }
        }
        return enrichedItems;
    }

    private ResourceServer getResourceServerById(String resourceServerId) {
        if (StringUtils.isBlank(resourceServerId)) {
            return null;
        }
        ResourceServer enrichedServer = resourceServerRepository.selectByPrimaryKey(resourceServerId);

        return enrichedServer;
    }

}
