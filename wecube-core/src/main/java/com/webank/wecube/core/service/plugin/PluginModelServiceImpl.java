package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;

@Service
@Slf4j
public class PluginModelServiceImpl implements PluginModelService {
    private PluginModelEntityServiceImpl pluginModelEntityServiceImpl = new PluginModelEntityServiceImpl();
    private PluginModelAttributeServiceImpl pluginModelAttributeServiceImpl = new PluginModelAttributeServiceImpl();


    /**
     * Plugin model registration
     *
     * @param pluginModelEntityDtos    list of plugin model entity dtos
     * @param pluginModelAttributeDtos list of plugin model attribute dtos
     * @return an enum map of dtos
     */
    @Override
    public EnumMap<PluginModel, Object> register(List<PluginModelEntityDto> pluginModelEntityDtos, List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        EnumMap<PluginModel, Object> pluginModel = new EnumMap<>(PluginModel.class);
        pluginModel.put(PluginModel.entityDtos, pluginModelEntityServiceImpl.register(pluginModelEntityDtos));
        pluginModel.put(PluginModel.attributeDtos, pluginModelAttributeServiceImpl.register(pluginModelAttributeDtos));
        return pluginModel;
    }

    /**
     * Plugin model update
     *
     * @param pluginModelEntityDtos    list of plugin model entity dtos
     * @param pluginModelAttributeDtos list of plugin model attribute dtos
     * @return an enum map of dtos
     */
    @Override
    public EnumMap<PluginModel, Object> update(List<PluginModelEntityDto> pluginModelEntityDtos, List<PluginModelAttributeDto> pluginModelAttributeDtos) {
        EnumMap<PluginModel, Object> pluginModel = new EnumMap<>(PluginModel.class);
        pluginModel.put(PluginModel.entityDtos, pluginModelEntityServiceImpl.update(pluginModelEntityDtos));
        pluginModel.put(PluginModel.attributeDtos, pluginModelAttributeServiceImpl.update(pluginModelAttributeDtos));
        return pluginModel;
    }

    /**
     * Plugin model overview
     *
     * @return an enum map of dtos
     */
    @Override
    public EnumMap<PluginModel, Object> overview() {
        EnumMap<PluginModel, Object> pluginModel = new EnumMap<>(PluginModel.class);
        pluginModel.put(PluginModel.entityDtos, pluginModelEntityServiceImpl.overview());
        pluginModel.put(PluginModel.attributeDtos, pluginModelAttributeServiceImpl.overview());
        return pluginModel;
    }
}
