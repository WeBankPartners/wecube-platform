package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.PluginCertificationDto;
import com.webank.wecube.platform.core.dto.plugin.PluginCertificationExportDto;
import com.webank.wecube.platform.core.entity.plugin.PluginCertification;
import com.webank.wecube.platform.core.repository.plugin.PluginCertificationMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginCertificationService {
    private static final Logger log = LoggerFactory.getLogger(PluginCertificationService.class);

    @Autowired
    private PluginCertificationMapper pluginCertificationMapper;

    public List<PluginCertificationDto> getAllPluginCertifications() {
        List<PluginCertificationDto> resultDtos = new ArrayList<>();
        List<PluginCertification> entities = pluginCertificationMapper.selectAllPluginCertifications();

        if (entities == null || entities.isEmpty()) {
            return resultDtos;
        }

        for (PluginCertification e : entities) {
            PluginCertificationDto dto = buildPluginCertificationDto(e);
            resultDtos.add(dto);
        }
        return resultDtos;
    }

    public void removePluginCertification(String id) {
        pluginCertificationMapper.deleteByPrimaryKey(id);
    }

    public PluginCertificationExportDto exportPluginCertification(String id) {
        PluginCertification entity = pluginCertificationMapper.selectByPrimaryKey(id);
        if (entity == null) {
            String errMsg = String.format("Try to export plugin certification with id [%s] but does not exist.", id);
            log.error(errMsg);
            throw new WecubeCoreException(errMsg);
        }

        PluginCertificationExportDto dto = new PluginCertificationExportDto();
        dto.setData(entity.getEncryptData());
        dto.setDescription(entity.getDescription());
        dto.setLpk(entity.getLpk());
        dto.setPlugin(entity.getPlugin());
        dto.setSignature(entity.getSignature());
        return dto;
    }

    public PluginCertificationDto importPluginCertification(PluginCertificationExportDto pluginCertificationExportDto) {
        if (pluginCertificationExportDto == null) {
            return null;
        }
        String plugin = pluginCertificationExportDto.getPlugin();
        if (StringUtils.isBlank(plugin)) {
            return null;
        }
        PluginCertification entity = pluginCertificationMapper.selectPluginCertificationByPlugin(plugin);
        if (entity == null) {
            entity = new PluginCertification();
            entity.setId(LocalIdGenerator.generateId());
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());

            entity.setEncryptData(pluginCertificationExportDto.getData());
            entity.setLpk(pluginCertificationExportDto.getLpk());
            entity.setSignature(pluginCertificationExportDto.getSignature());
            entity.setDescription(pluginCertificationExportDto.getDescription());
            entity.setPlugin(pluginCertificationExportDto.getPlugin());

            pluginCertificationMapper.insert(entity);
        } else {
            entity.setEncryptData(pluginCertificationExportDto.getData());
            entity.setLpk(pluginCertificationExportDto.getLpk());
            entity.setSignature(pluginCertificationExportDto.getSignature());
            entity.setDescription(pluginCertificationExportDto.getDescription());

            entity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setUpdatedTime(new Date());

            pluginCertificationMapper.updateByPrimaryKeySelective(entity);
        }

        return buildPluginCertificationDto(entity);
    }

    private PluginCertificationDto buildPluginCertificationDto(PluginCertification e) {
        PluginCertificationDto dto = new PluginCertificationDto();
        dto.setDescription(e.getDescription());
        dto.setEncryptData(e.getEncryptData());
        dto.setId(e.getId());
        dto.setLpk(e.getLpk());
        dto.setPlugin(e.getPlugin());
        dto.setSignature(e.getSignature());

        return dto;
    }
}
