package com.webank.wecube.platform.core.lazyJpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageMenu;
import org.springframework.data.repository.CrudRepository;

import java.util.*;


public interface LazyPluginPackageMenuRepository extends CrudRepository<LazyPluginPackageMenu, String> {
    Optional<List<LazyPluginPackageMenu>> findAllByPluginPackage_StatusIn(Collection<PluginPackage.Status> statuses);

    Optional<List<LazyPluginPackageMenu>> findAllByCodeAndPluginPackage_StatusIn(String menuCode, Collection<PluginPackage.Status> statuses);

    Optional<List<LazyPluginPackageMenu>> findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(boolean active, String pluginPackageId, Collection<PluginPackage.Status> statuses);

    default Optional<List<LazyPluginPackageMenu>> findAllMenusByStatusAndPluginPackageId(boolean active, String pluginPackageId) {
        return findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(active, pluginPackageId, PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<LazyPluginPackageMenu>> findAllActiveMenuByCode(String menuCode) {
        return findAllByCodeAndPluginPackage_StatusIn(menuCode, PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<LazyPluginPackageMenu>> findAllPluginPackageMenusForAllActivePackages() {
        return findAllByPluginPackage_StatusIn(PluginPackage.ACTIVE_STATUS);
    }

    default Optional<List<LazyPluginPackageMenu>> findAndMergePluginMenus() {
        Optional<List<LazyPluginPackageMenu>> allForAllActivePackages = findAllPluginPackageMenusForAllActivePackages();
        if (allForAllActivePackages.isPresent()) {
            Map<String, TreeSet<LazyPluginPackageMenu>> menuSetByMenuOrderMap = new HashMap<>();
            List<LazyPluginPackageMenu> pluginPackageMenus = allForAllActivePackages.get();
            pluginPackageMenus.forEach(menu -> {
                        if (!menuSetByMenuOrderMap.containsKey(menu.getCode())) {
                            TreeSet<LazyPluginPackageMenu> menus = new TreeSet<>(new PluginPackageMenuComparator());
                            menus.add(menu);
                            menuSetByMenuOrderMap.put(menu.getCode(), menus);
                        } else {
                            menuSetByMenuOrderMap.get(menu.getCode()).add(menu);
                        }
                    }
            );

            List<LazyPluginPackageMenu> result = new ArrayList<>();
            for (TreeSet<LazyPluginPackageMenu> menuCodeTreeSet : menuSetByMenuOrderMap.values()) {
                Optional<LazyPluginPackageMenu> activeMenuOptional = menuCodeTreeSet.stream().filter(LazyPluginPackageMenu::isActive).max(LazyPluginPackageMenu.COMPARE_BY_MENU_ORDER);
                if (activeMenuOptional.isPresent()) {
                    result.add(activeMenuOptional.get());
                } else {
                    Optional<LazyPluginPackageMenu> maxMenuOrderInactiveMenuOptional = menuCodeTreeSet.stream().max(LazyPluginPackageMenu.COMPARE_BY_MENU_ORDER);
                    maxMenuOrderInactiveMenuOptional.ifPresent(menu -> result.add(maxMenuOrderInactiveMenuOptional.get()));
                }
            }
            return Optional.of(result);
        }

        return Optional.empty();
    }

    /**
     * Find menu by menuCode and return one with largest menu order
     * Which is the package's latest version
     *
     * @param menuCode menu code of menu
     * @return optional of found LazyPluginPackageMenu
     */
    default Optional<LazyPluginPackageMenu> findAndMergePluginMenus(String menuCode) {
        Optional<List<LazyPluginPackageMenu>> menuByCodeFromDifferentVersionPackages = findAllActiveMenuByCode(menuCode);
        if (menuByCodeFromDifferentVersionPackages.isPresent()) {
            List<LazyPluginPackageMenu> pluginPackageMenus = menuByCodeFromDifferentVersionPackages.get();
            Optional<LazyPluginPackageMenu> activeMenuOptional = pluginPackageMenus.stream().filter(LazyPluginPackageMenu::isActive).max(LazyPluginPackageMenu.COMPARE_BY_MENU_ORDER);
            if (activeMenuOptional.isPresent()) {
                return activeMenuOptional;
            } else {
                return pluginPackageMenus.stream().max(LazyPluginPackageMenu.COMPARE_BY_MENU_ORDER);
            }
        }
        return Optional.empty();
    }

    class PluginPackageMenuComparator implements Comparator<LazyPluginPackageMenu> {
        @Override
        public int compare(LazyPluginPackageMenu menu1, LazyPluginPackageMenu menu2) {
            return menu1.getMenuOrder().compareTo(menu2.getMenuOrder());
        }
    }
}
