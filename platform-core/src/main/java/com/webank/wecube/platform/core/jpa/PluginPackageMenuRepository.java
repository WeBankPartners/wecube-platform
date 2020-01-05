package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.stream.Collectors;


public interface PluginPackageMenuRepository extends CrudRepository<PluginPackageMenu, String> {
    Optional<List<PluginPackageMenu>> findAllByPluginPackage_StatusIn(Collection<PluginPackage.Status> statuses);

    Optional<List<PluginPackageMenu>> findAllByCodeAndPluginPackage_StatusIn(String menuCode, Collection<PluginPackage.Status> statuses);

    Optional<List<PluginPackageMenu>> findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(boolean active, String pluginPackageId, Collection<PluginPackage.Status> statuses);

    default Optional<List<PluginPackageMenu>> findAllMenusByStatusAndPluginPackageId(boolean active, String pluginPackageId) {
        return findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(active, pluginPackageId, PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<PluginPackageMenu>> findAllActiveMenuByCode(String menuCode) {
        return findAllByCodeAndPluginPackage_StatusIn(menuCode, PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<PluginPackageMenu>> findAllPluginPackageMenusForAllActivePackages() {
        return findAllByPluginPackage_StatusIn(PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<PluginPackageMenu>> findAndMergePluginMenus() {
        Optional<List<PluginPackageMenu>> allForAllActivePackages = findAllPluginPackageMenusForAllActivePackages();
        if (allForAllActivePackages.isPresent()) {
            Map<String, TreeSet<PluginPackageMenu>> menuSetByMenuOrderMap = new HashMap<>();
            List<PluginPackageMenu> pluginPackageMenus = allForAllActivePackages.get();
            pluginPackageMenus.forEach(menu -> {
                        if (!menuSetByMenuOrderMap.containsKey(menu.getCode())) {
                            TreeSet<PluginPackageMenu> menus = new TreeSet<>(new PluginPackageMenuComparator());
                            menus.add(menu);
                            menuSetByMenuOrderMap.put(menu.getCode(), menus);
                        } else {
                            menuSetByMenuOrderMap.get(menu.getCode()).add(menu);
                        }
                    }
            );

            return Optional.of(menuSetByMenuOrderMap.values().stream().map(it -> it.last()).collect(Collectors.toList()));
        }

        return Optional.empty();
    }

    /**
     * Find menu by menuCode and return one with largest menu order
     * Which is the package's latest version
     *
     * @param menuCode menu code of menu
     * @return optional of found PluginPackageMenu
     */
    default Optional<PluginPackageMenu> findAndMergePluginMenus(String menuCode) {
        Optional<List<PluginPackageMenu>> menuByCodeFromDifferentVersionPackages = findAllActiveMenuByCode(menuCode);
        if (menuByCodeFromDifferentVersionPackages.isPresent()) {
            List<PluginPackageMenu> pluginPackageMenus = menuByCodeFromDifferentVersionPackages.get();
            return pluginPackageMenus.stream().max(PluginPackageMenu.COMPARE_BY_MENU_ORDER);
        }
        return Optional.empty();
    }

    class PluginPackageMenuComparator implements Comparator<PluginPackageMenu> {
        @Override
        public int compare(PluginPackageMenu menu1, PluginPackageMenu menu2) {
            return menu1.getMenuOrder().compareTo(menu2.getMenuOrder());
        }
    }
}
