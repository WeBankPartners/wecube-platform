//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
//import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.*;
//
//
//public interface PluginPackageMenuRepository extends CrudRepository<PluginPackageMenu, String> {
//    Optional<List<PluginPackageMenu>> findAllByPluginPackage_StatusIn(Collection<PluginPackage.Status> statuses);
//
//    Optional<List<PluginPackageMenu>> findAllByCodeAndPluginPackage_StatusIn(String menuCode, Collection<PluginPackage.Status> statuses);
//
//    Optional<List<PluginPackageMenu>> findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(boolean active, String pluginPackageId, Collection<PluginPackage.Status> statuses);
//
//    default Optional<List<PluginPackageMenu>> findAllMenusByStatusAndPluginPackageId(boolean active, String pluginPackageId) {
//        return findAllByActiveAndPluginPackage_IdAndPluginPackage_StatusIn(active, pluginPackageId, PluginPackage.ACTIVE_STATUS);
//    }
//
//    default Optional<List<PluginPackageMenu>> findAllActiveMenuByCode(String menuCode) {
//        return findAllByCodeAndPluginPackage_StatusIn(menuCode, PluginPackage.ACTIVE_STATUS);
//    }
//
//    default Optional<List<PluginPackageMenu>> findAllPluginPackageMenusForAllActivePackages() {
//        return findAllByPluginPackage_StatusIn(PluginPackage.ACTIVE_STATUS);
//    }
//
////    default Optional<List<PluginPackageMenu>> findAndMergePluginMenus() {
////        Optional<List<PluginPackageMenu>> allForAllActivePackages = findAllPluginPackageMenusForAllActivePackages();
////        if (allForAllActivePackages.isPresent()) {
////            Map<String, TreeSet<PluginPackageMenu>> menuSetByMenuOrderMap = new HashMap<>();
////            List<PluginPackageMenu> pluginPackageMenus = allForAllActivePackages.get();
////            pluginPackageMenus.forEach(menu -> {
////                        if (!menuSetByMenuOrderMap.containsKey(menu.getCode())) {
////                            TreeSet<PluginPackageMenu> menus = new TreeSet<>(new PluginPackageMenuComparator());
////                            menus.add(menu);
////                            menuSetByMenuOrderMap.put(menu.getCode(), menus);
////                        } else {
////                            menuSetByMenuOrderMap.get(menu.getCode()).add(menu);
////                        }
////                    }
////            );
////
////            List<PluginPackageMenu> result = new ArrayList<>();
////            for (TreeSet<PluginPackageMenu> menuCodeTreeSet : menuSetByMenuOrderMap.values()) {
////                Optional<PluginPackageMenu> activeMenuOptional = menuCodeTreeSet.stream().filter(PluginPackageMenu::isActive).max(PluginPackageMenu.COMPARE_BY_MENU_ORDER);
////                if (activeMenuOptional.isPresent()) {
////                    result.add(activeMenuOptional.get());
////                } else {
////                    Optional<PluginPackageMenu> maxMenuOrderInactiveMenuOptional = menuCodeTreeSet.stream().max(PluginPackageMenu.COMPARE_BY_MENU_ORDER);
////                    maxMenuOrderInactiveMenuOptional.ifPresent(menu -> result.add(maxMenuOrderInactiveMenuOptional.get()));
////                }
////            }
////            return Optional.of(result);
////        }
////
////        return Optional.empty();
////    }
//
//    /**
//     * Find menu by menuCode and return one with largest menu order
//     * Which is the package's latest version
//     *
//     * @param menuCode menu code of menu
//     * @return optional of found PluginPackageMenu
//     */
//    default Optional<PluginPackageMenu> findAndMergePluginMenus(String menuCode) {
//        Optional<List<PluginPackageMenu>> menuByCodeFromDifferentVersionPackages = findAllActiveMenuByCode(menuCode);
//        if (menuByCodeFromDifferentVersionPackages.isPresent()) {
//            List<PluginPackageMenu> pluginPackageMenus = menuByCodeFromDifferentVersionPackages.get();
//            Optional<PluginPackageMenu> activeMenuOptional = pluginPackageMenus.stream().filter(PluginPackageMenu::isActive).max(PluginPackageMenu.COMPARE_BY_MENU_ORDER);
//            if (activeMenuOptional.isPresent()) {
//                return activeMenuOptional;
//            } else {
//                return pluginPackageMenus.stream().max(PluginPackageMenu.COMPARE_BY_MENU_ORDER);
//            }
//        }
//        return Optional.empty();
//    }
//
//    class PluginPackageMenuComparator implements Comparator<PluginPackageMenu> {
//        @Override
//        public int compare(PluginPackageMenu menu1, PluginPackageMenu menu2) {
//            return menu1.getMenuOrder().compareTo(menu2.getMenuOrder());
//        }
//    }
//}
