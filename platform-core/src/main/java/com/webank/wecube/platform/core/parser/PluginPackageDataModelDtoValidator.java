package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
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
        if (null != pluginPackageEntities && pluginPackageEntities.size() > 0) {
            pluginPackageEntities.forEach(inputEntityDto -> inputEntityDto.getAttributes().forEach(inputAttributeDto -> {
                if (StringUtils.isEmpty(inputAttributeDto.getDataType())) {
                    String msg = String.format(
                            "The DataType should not be empty or null while registering he package [%s] with version: [%s]",
                            inputEntityDto.getPackageName(), inputEntityDto.getDataModelVersion());
                    logger.error(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(inputAttributeDto));
                    }
                    throw new WecubeCoreException("3279",msg, inputEntityDto.getPackageName(), inputEntityDto.getDataModelVersion());
                }
                if ("ref" .equals(inputAttributeDto.getDataType()) && StringUtils.isEmpty(inputAttributeDto.getRefAttributeName())) {
                    String msg = "Field [ref] should be specified when [dataType] is set to [\"ref\"]";
                    logger.error(msg);
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(inputAttributeDto));
                    }
                    throw new WecubeCoreException("3280",msg);
                }
            }));
        }
    }
}
