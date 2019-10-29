package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import com.webank.wecube.platform.core.dto.PluginPackageResourceFilesDto;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageResourceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newLinkedHashSet;

@Service
@Transactional
public class PluginPackageResourceFileService {
    @Autowired
    private PluginPackageResourceFileRepository pluginPackageResourceFileRepository;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    public PluginPackageResourceFilesDto getAllResourceFilesByPluginPackageId(int pluginPackageId) {
        if (!pluginPackageRepository.existsById(pluginPackageId)) {
            throw new WecubeCoreException(String.format("Plugin Package not found for id [%s]", pluginPackageId));
        }
        PluginPackageResourceFilesDto pluginPackageResourceFilesDto = new PluginPackageResourceFilesDto();
        PluginPackage pluginPackage = pluginPackageRepository.findById(pluginPackageId).get();
        pluginPackageResourceFilesDto.setPluginPackageId(pluginPackageId);
        pluginPackageResourceFilesDto.setPluginPackageName(pluginPackage.getName());
        pluginPackageResourceFilesDto.setPluginPackageVersion(pluginPackage.getVersion());

        Optional<List<PluginPackageResourceFile>> pluginPackageResourceFileOptional = pluginPackageResourceFileRepository.findAllByPluginPackageId(pluginPackageId);
        if (pluginPackageResourceFileOptional.isPresent()) {
            pluginPackageResourceFilesDto.setPluginPackageResourceFiles(newLinkedHashSet(pluginPackageResourceFileOptional.get()));
        }

        return pluginPackageResourceFilesDto;
    }
}
