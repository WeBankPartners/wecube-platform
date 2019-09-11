package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * @param inputEntityDtos list of plugin model entity dtos
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> register(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> attrDtoList = extractAttributes(inputEntityDtos);

        List<Integer> registeredEntityIdList = new ArrayList<>();
        for (PluginModelEntityDto tmpEntityDto : pluginModelEntityServiceImpl.register(inputEntityDtos)) {
            registeredEntityIdList.add(tmpEntityDto.getId());
        }
        List<PluginModelAttributeDto> flatAttributeList = setAttributeWithEntityId(attrDtoList, registeredEntityIdList);
        pluginModelAttributeServiceImpl.register(flatAttributeList);

        return pluginModelEntityServiceImpl.overview();
    }

    /**
     * Plugin model update
     *
     * @param inputEntityDtos list of plugin model entity dtos
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> update(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> attrDtoList = extractAttributes(inputEntityDtos);

        List<Integer> registeredEntityIdList = new ArrayList<>();
        for (PluginModelEntityDto tmpEntityDto : pluginModelEntityServiceImpl.update(inputEntityDtos)) {
            registeredEntityIdList.add(tmpEntityDto.getId());
        }
        List<PluginModelAttributeDto> flatAttributeList = setAttributeWithEntityId(attrDtoList, registeredEntityIdList);
        pluginModelAttributeServiceImpl.update(flatAttributeList);

        return pluginModelEntityServiceImpl.overview();
    }

    /**
     * Plugin model overview
     *
     * @return an list of entity dtos which contain both entities and attributes
     */
    @Override
    public List<PluginModelEntityDto> overview() {
        return pluginModelEntityServiceImpl.overview();
    }

    private List<PluginModelAttributeDto> setAttributeWithEntityId(List<List<PluginModelAttributeDto>> attrDtoList, List<Integer> registeredEntityIdList) {
        List<PluginModelAttributeDto> pluginModelAttributeDtos = new ArrayList<>();
        for (int i = 0; i < registeredEntityIdList.size(); i++) {
            Integer registeredId = registeredEntityIdList.get(i);
            for (PluginModelAttributeDto tmp : attrDtoList.get(i)) {
                tmp.setPluginModelEntityId(registeredId);
                pluginModelAttributeDtos.add(tmp);
            }
        }
        return pluginModelAttributeDtos;
    }

    private List<List<PluginModelAttributeDto>> extractAttributes(List<PluginModelEntityDto> inputEntityDtos) {
        List<List<PluginModelAttributeDto>> dtoList = new ArrayList<>();
        inputEntityDtos.forEach(tmpEntityDto -> dtoList.add(tmpEntityDto.getAttributeDtoList()));
        return dtoList;
    }


}
