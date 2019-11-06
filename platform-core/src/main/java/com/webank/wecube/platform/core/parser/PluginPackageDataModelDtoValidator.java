package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class PluginPackageDataModelDtoValidator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void validate(PluginPackageDataModelDto pluginPackageDataModelDto) {
        Set<PluginPackageEntityDto> pluginPackageEntities = pluginPackageDataModelDto.getPluginPackageEntities();
        if (null == pluginPackageEntities || pluginPackageEntities.size() < 0) {
            throw new WecubeCoreException("Data model entity not exist in data model.");
        }
        for (PluginPackageEntityDto inputEntityDto : pluginPackageEntities) {
            for (PluginPackageAttributeDto inputAttributeDto : inputEntityDto.getAttributes()) {
                if (StringUtils.isEmpty(inputAttributeDto.getDataType())) {
                    String msg = String.format(
                            "The DataType should not be empty or null while registering he package [%s] with version: [%s]",
                            inputEntityDto.getPackageName(), inputEntityDto.getPackageVersion());
                    logger.error(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(inputAttributeDto));
                    }
                    throw new WecubeCoreException(msg);
                }
                // check the DataType
                if ("ref".equals(inputAttributeDto.getDataType())) {
                    // if DataType equals "ref"
                    if (StringUtils.isEmpty(inputAttributeDto.getRefPackageName())
                            || StringUtils.isEmpty(inputAttributeDto.getRefEntityName())
                            || StringUtils.isEmpty(inputAttributeDto.getRefAttributeName())) {
                        // once the reference name (including packageName, entityName and attributeName should be specified
                        String msg = "All reference field should be specified when [dataType] is set to [\"ref\"]";
                        logger.error(msg);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.valueOf(inputAttributeDto));
                        }
                        throw new WecubeCoreException(msg);
                    }
                }
            }
        }
    }
}
