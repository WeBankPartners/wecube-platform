package com.webank.wecube.platform.core.service.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.EntityDto;
import com.webank.wecube.platform.core.dto.plugin.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.plugin.PluginPackageDataModelService;

@Service
public class ExpressionServiceImpl implements ExpressionService {
    private static final Logger log = LoggerFactory.getLogger(ExpressionServiceImpl.class);

    @Autowired
    private PluginPackageDataModelService pluginPackageDataModelService;

    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntitiesMapper;

    @Autowired
    private PluginPackageAttributesMapper pluginPackageAttributesMapper;

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    public List<EntityDto> getAllEntities(String dataModelExpression) {
        if (StringUtils.isBlank(dataModelExpression)) {
            throw new WecubeCoreException("3005", "Data model expression cannot be blank.");
        }

        if (log.isInfoEnabled()) {
            log.info("Get all entities with expression:{}", dataModelExpression);
        }

        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser.parse(dataModelExpression);
        List<EntityDto> entityDtos = new ArrayList<EntityDto>();

        for (EntityQueryExprNodeInfo exprNodeInfo : exprNodeInfos) {
            EntityDto entityDto = new EntityDto();
            entityDto.setPackageName(exprNodeInfo.getPackageName());
            entityDto.setEntityName(exprNodeInfo.getEntityName());

            entityDtos.add(entityDto);
        }

        for (EntityDto entityDto : entityDtos) {
            PluginPackageDataModel dataModelEntity = pluginPackageDataModelService.tryFetchLatestAvailableDataModelEntity(entityDto.getPackageName());

            if (dataModelEntity == null) {
                continue;
            }

            List<PluginPackageEntities> pluginPackageEntities = pluginPackageEntitiesMapper
                    .selectAllByPackageNameAndEntityNameAndDataModelVersion(entityDto.getPackageName(),
                            entityDto.getEntityName(), dataModelEntity.getVersion());

            if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
                continue;
            }

            for (PluginPackageEntities entity : pluginPackageEntities) {
                List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributesMapper
                        .selectAllByEntity(entity.getId());

                if (pluginPackageAttributes == null) {
                    continue;
                }

                for (PluginPackageAttributes attr : pluginPackageAttributes) {
                    PluginPackageAttributeDto attrDto = buildPluginPackageAttributeDto(attr, entity);
                    entityDto.addAttribute(attrDto);
                }
            }
        }

        return entityDtos;
    }

    private PluginPackageAttributeDto buildPluginPackageAttributeDto(PluginPackageAttributes attr,
            PluginPackageEntities entity) {
        PluginPackageAttributeDto dto = new PluginPackageAttributeDto();
        dto.setDataType(attr.getDataType());
        dto.setDescription(attr.getDescription());
        dto.setEntityName(entity.getName());
        dto.setId(attr.getId());
        dto.setName(attr.getName());
        dto.setPackageName(entity.getPackageName());
        dto.setRefAttributeName(attr.getRefAttr());
        dto.setRefEntityName(attr.getRefEntity());
        dto.setRefPackageName(attr.getRefPackage());
        
        dto.setMultiple(attr.getMultiple());
        String mandatoryStr = null;
        Boolean mandatory = attr.getMandatory();
        if(mandatory != null) {
            mandatoryStr = mandatory?"Y":"N";
        }
        dto.setMandatory(mandatoryStr);

        return dto;
    }

}
