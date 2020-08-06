package com.webank.wecube.platform.core.service.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.EntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageDataModelRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;

@Service
public class ExpressionServiceImpl implements ExpressionService {
    private static final Logger log = LoggerFactory.getLogger(ExpressionServiceImpl.class);
    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    private PluginPackageDataModelRepository pluginPackageDataModelRepository;

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    public List<EntityDto> getAllEntities(String dataModelExpression) {
        if (StringUtils.isBlank(dataModelExpression)) {
            throw new WecubeCoreException("3005","Data model expression cannot be blank.");
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

        entityDtos.forEach(entity -> {
            Optional<PluginPackageDataModel> dataModelOptional = pluginPackageDataModelRepository
                    .findLatestDataModelByPackageName(entity.getPackageName());
            if (dataModelOptional.isPresent()) {

                Optional<PluginPackageEntity> entityOptional = pluginPackageEntityRepository
                        .findByPackageNameAndNameAndDataModelVersion(entity.getPackageName(), entity.getEntityName(),
                                dataModelOptional.get().getVersion());
                if (entityOptional.isPresent()) {
                    entity.setAttributes(entityOptional.get().getPluginPackageAttributeList());
                }
            }
        });

        return entityDtos;
    }

}
