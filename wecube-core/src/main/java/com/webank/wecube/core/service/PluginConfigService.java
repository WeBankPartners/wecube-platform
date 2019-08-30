package com.webank.wecube.core.service;


import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.*;
import com.webank.wecube.core.jpa.PluginConfigInterfaceParameterRepository;
import com.webank.wecube.core.jpa.PluginConfigRepository;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import com.webank.wecube.core.service.plugin.PluginConfigRegisteringProcessor;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Service
@Slf4j
@Transactional
public class PluginConfigService {

    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginConfigRepository pluginConfigRepository;
    @Autowired
    CmdbServiceV2Stub cmdbServiceV2Stub;
    @Autowired
    private PluginConfigInterfaceParameterRepository pluginConfigInterfaceParameterRepository;

    public Iterable<PluginPackage> getPluginPackages() {
        return pluginPackageRepository.findAll();
    }

    public List<PluginConfigInterface> getPluginConfigInterfaces(int pluginConfigId) {
        return pluginConfigRepository.findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfigId);
    }

    public List<PluginConfigInterface> getLatestOnlinePluginInterfaces(Integer ciTypeId) {
        return pluginConfigRepository.findLatestOnlinePluginInterfaces(ciTypeId);
    }


    public List<PluginConfigFilteringRule> getPluginConfigFilteringRules(int pluginConfigId) {
        return pluginConfigRepository.findAllPluginConfigFilteringRulesByConfigId(pluginConfigId);
    }

    public void releasePluginConfig(int pluginConfigId) {
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(pluginConfigId);
        if (!pluginConfigOptional.isPresent())
            throw new WecubeCoreException("Plugin config id not found, id = " + pluginConfigId);
        PluginConfig pluginConfig = pluginConfigOptional.get();
        if (pluginConfig.getStatus().equals(NOT_CONFIGURED))
            throw new WecubeCoreException("Plugin with status[NotConfigured] can't be released.");
        pluginConfig.setStatus(ONLINE);
        pluginConfigRepository.save(pluginConfig);
    }

    public void decommissionPluginConfig(int pluginConfigId) {
        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(pluginConfigId);
        if (!pluginConfigOptional.isPresent())
            throw new WecubeCoreException("Can not found the decommission plugin package ID, id = " + pluginConfigId);
        PluginConfig pluginConfig = pluginConfigOptional.get();
        pluginConfig.setStatus(DECOMMISSIONED);
        pluginConfigRepository.save(pluginConfig);
    }

    public void savePluginConfig(int pluginConfigId, int cmdbCiTypeId, String cmdbCiTypeName, PluginRegisteringModel registeringModel) {
        if (pluginConfigId == 0) throw new WecubeCoreException("pluginConfigId required.");
        if (cmdbCiTypeId == 0) throw new WecubeCoreException("cmdbCiTypeId required.");
        if (StringUtils.isEmpty(cmdbCiTypeName)) throw new WecubeCoreException("cmdbCiTypeName should NOT be empty.");
        if (registeringModel == null) throw new WecubeCoreException("pluginRegisteringModel should NOT be null.");
        if (isEmpty(registeringModel.getInterfaceConfigs()))
            throw new WecubeCoreException("Interface config should NOT be empty.");

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(pluginConfigId);
        if (!pluginConfigOptional.isPresent())
            throw new WecubeCoreException("Plugin config id not found when, id = " + pluginConfigId);
        PluginConfig pluginConfig = pluginConfigOptional.get();
        pluginConfig.setCmdbCiTypeId(cmdbCiTypeId);

        new PluginConfigRegisteringProcessor(cmdbServiceV2Stub, pluginConfig, pluginConfigInterfaceParameterRepository)
                .process(cmdbCiTypeId, cmdbCiTypeName, registeringModel);

        pluginConfigRepository.save(pluginConfig);
    }

    public PluginConfigInterface getPluginConfigInterfaceByServiceName(String serviceName) {
        Optional<PluginConfigInterface> pluginConfigInterface = pluginConfigRepository
                .findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(serviceName);
        if (!pluginConfigInterface.isPresent()) {
            throw new WecubeCoreException(
                    String.format("Plugin interface not found for serviceName [%s].", serviceName));
        }
        return pluginConfigInterface.get();
    }


}
