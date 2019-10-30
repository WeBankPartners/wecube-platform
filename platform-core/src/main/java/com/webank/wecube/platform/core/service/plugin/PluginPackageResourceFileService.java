package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageResourceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;

@Service
@Transactional
public class PluginPackageResourceFileService {
    @Autowired
    private PluginPackageResourceFileRepository pluginPackageResourceFileRepository;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    public Set<PluginPackageResourceFile> getAllPluginPackageResourceFiles() {
        Optional<Set<PluginPackage>> pluginPackagesOptional = pluginPackageRepository.findLatestPluginPackagesByStatusGroupByPackageName(REGISTERED, RUNNING, STOPPED);
        if (pluginPackagesOptional.isPresent()) {
            Set<Integer> pluginPackageIds = pluginPackagesOptional.get().stream().map(p->p.getId()).collect(Collectors.toSet());

            if (null != pluginPackageIds && pluginPackageIds.size() > 0) {
                Optional<List<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = pluginPackageResourceFileRepository.findPluginPackageResourceFileByPluginPackageIds(pluginPackageIds.toArray(new Integer[pluginPackageIds.size()]));
                if (pluginPackageResourceFilesOptional.isPresent()) {
                    List<PluginPackageResourceFile> pluginPackageResourceFiles = pluginPackageResourceFilesOptional.get();
                    if (null != pluginPackageResourceFiles && pluginPackageResourceFiles.size() > 0) {
                        return newLinkedHashSet(pluginPackageResourceFiles);
                    }
                }
            }
        }

        return null;
    }
}
