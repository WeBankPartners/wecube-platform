package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageResourceFile;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackage;
import com.webank.wecube.platform.core.lazyJpa.LazyPluginPackageRepository;
import com.webank.wecube.platform.core.lazyJpa.LazyPluginPackageResourceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;



@Service
@Transactional
public class PluginPackageResourceFileService {
    @Autowired
    private LazyPluginPackageResourceFileRepository pluginPackageResourceFileRepository;

    @Autowired
    private LazyPluginPackageRepository lazyPluginPackageRepository;

    public Set<LazyPluginPackageResourceFile> getAllPluginPackageResourceFiles() {
        Optional<Set<LazyPluginPackage>> pluginPackagesOptional = lazyPluginPackageRepository.findLatestPluginPackagesByStatusGroupByPackageName(LazyPluginPackage.Status.REGISTERED, LazyPluginPackage.Status.RUNNING, LazyPluginPackage.Status.STOPPED);
        if (pluginPackagesOptional.isPresent()) {
            Set<String> pluginPackageIds = pluginPackagesOptional.get().stream().map(p->p.getId()).collect(Collectors.toSet());

            if (null != pluginPackageIds && pluginPackageIds.size() > 0) {
                Optional<List<LazyPluginPackageResourceFile>> pluginPackageResourceFilesOptional = pluginPackageResourceFileRepository.findPluginPackageResourceFileByPluginPackageIds(pluginPackageIds.toArray(new String[pluginPackageIds.size()]));
                if (pluginPackageResourceFilesOptional.isPresent()) {
                    List<LazyPluginPackageResourceFile> pluginPackageResourceFiles = pluginPackageResourceFilesOptional.get();
                    if (null != pluginPackageResourceFiles && pluginPackageResourceFiles.size() > 0) {
                        return newLinkedHashSet(pluginPackageResourceFiles);
                    }
                }
            }
        }

        return null;
    }
}
