package com.webank.wecube.platform.core.service;

import static com.webank.wecube.platform.core.service.permission.CiTypePermissionUtil.evaluatePartialActionPermissions;
import static com.webank.wecube.platform.core.service.permission.CiTypePermissionUtil.mergeActionPermissions;
import static com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypePermissions.DISABLED;
import static com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypePermissions.ENABLED;
import static com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static com.webank.wecube.platform.core.utils.BooleanUtils.isTrue;
import static com.webank.wecube.platform.core.utils.CollectionUtils.asMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.Role;
import com.webank.wecube.platform.core.domain.RoleMenu;
import com.webank.wecube.platform.core.domain.RoleUser;
import com.webank.wecube.platform.core.domain.User;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.InputType;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeCtrlAttrConditionDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeCtrlAttrDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class UserManagerService {

    private static final String CONSTANT_CREATION_PERMISSION = "creationPermission";
    private static final String CONSTANT_REMOVAL_PERMISSION = "removalPermission";
    private static final String CONSTANT_MODIFICATION_PERMISSION = "modificationPermission";
    private static final String CONSTANT_ENQUIRY_PERMISSION = "enquiryPermission";
    private static final String CONSTANT_GRANT_PERMISSION = "grantPermission";
    private static final String CONSTANT_EXECUTION_PERMISSION = "executionPermission";
    private static final String CONSTANT_ROLE_CI_TYPE_CTRL_ATTR_ID = "roleCiTypeCtrlAttrId";
    private static final String CONSTANT_CALLBACK_ID = "callbackId";

    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    CmdbServiceV2Stub cmdbServiceStub;

    public List<User> createUser(User user) {
        if (user == null)
            throw new WecubeCoreException("User parameter should not be null.");
        if (user.getUsername() == null)
            throw new WecubeCoreException("Username should not be null.");
        if (user.getFullName() == null)
            user.setFullName(user.getUsername());
        if (user.getDescription() == null)
            user.setDescription(user.getFullName());
        if (checkUserExists(user.getUsername())) {
            throw new WecubeCoreException(String.format("Username[%s] already exists.", user.getUsername()));
        }

        return cmdbServiceStub.createUsers(user);
    }

    public boolean checkUserExists(String username) {
        return cmdbServiceStub.getUserByUsername(username) != null;
    }

    public void deleteRole(int roleId) {
        List<User> users = cmdbServiceStub.getUsersByRoleId(roleId);
        if (isNotEmpty(users))
            throw new WecubeCoreException(String.format("Failed to delete role[%d] because it is used for User: %s",
                    roleId, users.stream().map(User::getUsername).collect(Collectors.joining(","))));
        List<String> menuItems = getMenuItemsByRoleId(roleId);
        if (isNotEmpty(menuItems))
            throw new WecubeCoreException(
                    String.format("Failed to delete role[%d] because it is used for Menu: %s", roleId, menuItems));
        List<RoleCiTypeDto> roleCiTypes = getRoleCiTypesByRoleId(roleId);
        if (isNotEmpty(roleCiTypes))
            throw new WecubeCoreException(String.format("Failed to delete role[%d] because it is used for CiType: %s",
                    roleId,
                    roleCiTypes.stream().map(p -> String.valueOf(p.getCiTypeId())).collect(Collectors.joining(","))));
        cmdbServiceStub.deleteRoles(roleId);
    }

    public List<MenuItem> getAllMenuItems() {
        return Lists.newArrayList(menuItemRepository.findAll());
    }

    public List<MenuItemDto> getAllSysMenus() {
        List<MenuItemDto> returnMenuDto = new ArrayList<>();
        Iterable<MenuItem> systemMenus = menuItemRepository.findAll();
        for (MenuItem systemMenu : systemMenus) {
            MenuItemDto systemMenuDto = MenuItemDto.fromCoreMenuItem(systemMenu);
            returnMenuDto.add(systemMenuDto);
        }
        return returnMenuDto;
    }

    public List<String> getMenuItemsByRoleId(int roleId) {
        return menuItemRepository.findMenuItemsByRoles(roleId).stream().map(MenuItem::getCode)
                .collect(Collectors.toList());
    }

    public List<String> getMenuItemCodesByUsername(String username) {
        return getMenuItemsByUsername(username, false).stream().map(MenuItem::getCode).collect(toList());
    }

    public List<MenuItem> getMenuItemsByUsername(String username, boolean withParentMenu) {
        List<Role> roles = cmdbServiceStub.getRolesByUsername(username);
        log.info("Roles {} found for user {}", roles, username);
        if (isNotEmpty(roles)) {
            Integer[] roleIds = roles.stream().map(Role::getRoleId).toArray(Integer[]::new);
            List<MenuItem> menuItems = menuItemRepository.findMenuItemsByRoles(roleIds);
            if (isNotEmpty(menuItems) && withParentMenu) {
                Set<Integer> fetchedParentIds = Sets.newHashSet();
                Set<Integer> toFetchedParentIds = Sets.newHashSet();
                for (MenuItem menuItem : menuItems) {
                    Integer parentId = menuItem.getParentId();
                    if (parentId == null || parentId == 0) {
                        fetchedParentIds.add(menuItem.getId());
                    } else {
                        toFetchedParentIds.add(parentId);
                    }
                }
                toFetchedParentIds.removeAll(fetchedParentIds);
                Iterable<MenuItem> parentMenus = menuItemRepository.findAllById(toFetchedParentIds);
                parentMenus.forEach(menuItems::add);
            }
            return menuItems;
        }
        return Collections.emptyList();
    }

    public List<RoleCiTypeDto> getRoleCiTypesByRoleId(int roleId) {
        List<RoleCiTypeDto> roleCiTypes = cmdbServiceStub.getRoleCiTypeByRoleId(roleId);
        if (roleCiTypes == null)
            roleCiTypes = Lists.newArrayList();

        List<CiTypeDto> allCiTypes = cmdbServiceStub.getAllCiTypes(false, null);
        if (isNotEmpty(allCiTypes) && roleCiTypes.size() < allCiTypes.size()) {
            Set<Integer> ciTypeIds = roleCiTypes.stream().map(RoleCiTypeDto::getCiTypeId).collect(Collectors.toSet());
            RoleCiTypeDto[] toAddRoleCiTypes = allCiTypes.stream()
                    .filter(ciType -> !ciTypeIds.contains(ciType.getCiTypeId()))
                    .map(ciType -> new RoleCiTypeDto(roleId, ciType.getCiTypeId(), ciType.getName()))
                    .toArray(RoleCiTypeDto[]::new);
            List<RoleCiTypeDto> addedRoleCiTypes = cmdbServiceStub.createRoleCiTypes(toAddRoleCiTypes);
            roleCiTypes.addAll(addedRoleCiTypes);
        }
        roleCiTypes.sort(new CiTypeIdComparator());
        evaluatePartialActionPermissions(roleCiTypes);
        return roleCiTypes;
    }

    public List<RoleCiTypeDto> getRoleCiTypesByUsername(String username) {
        List<RoleCiTypeDto> roleCiTypes = cmdbServiceStub.getRoleCiTypeByUsername(username);
        if (isNotEmpty(roleCiTypes)) {
            roleCiTypes = mergePermissionsByCiTypeId(roleCiTypes);
        }
        evaluatePartialActionPermissions(roleCiTypes);
        return roleCiTypes;
    }

    private List<RoleCiTypeDto> mergePermissionsByCiTypeId(List<RoleCiTypeDto> roleCiTypes) {
        Map<Integer, RoleCiTypeDto> mergedRoleCiTypes = new LinkedHashMap<>();
        roleCiTypes.forEach(roleCiType -> {
            Integer ciTypeId = roleCiType.getCiTypeId();
            RoleCiTypeDto mergedRoleCiType = mergedRoleCiTypes.get(ciTypeId);
            if (mergedRoleCiType == null) {
                mergedRoleCiType = new RoleCiTypeDto();
                mergedRoleCiType.setRoleCiTypeId(-1);
                mergedRoleCiType.setCiTypeId(ciTypeId);
                mergedRoleCiType.setCiTypeName(roleCiType.getCiTypeName());
                mergedRoleCiTypes.put(ciTypeId, mergedRoleCiType);
            }
            mergeActionPermissions(mergedRoleCiType, roleCiType);
        });
        return Lists.newArrayList(mergedRoleCiTypes.values());
    }

    public Map<String, List> getRoleCiTypeCtrlAttributesByRoleCiTypeId(int roleCiTypeId) {
        RoleCiTypeDto roleCiType = cmdbServiceStub.getRoleCiTypeById(roleCiTypeId);
        if (roleCiType == null)
            throw new WecubeCoreException("CiType permission not found for roleCiTypeId:" + roleCiTypeId);
        List<CiTypeAttrDto> accessControlAttributes = cmdbServiceStub
                .getCiTypeAccessControlAttributesByCiTypeId(roleCiType.getCiTypeId());

        List<RoleCiTypeCtrlAttrDto> roleCiTypeCtrlAttrs = cmdbServiceStub
                .getRoleCiTypeCtrlAttributesByRoleCiTypeId(roleCiTypeId);
        List<Map<String, Object>> roleCiTypeCtrlAttrsModels;
        if (isNotEmpty(roleCiTypeCtrlAttrs)) {
            roleCiTypeCtrlAttrsModels = roleCiTypeCtrlAttrs.stream()
                    .map(roleCiTypeCtrlAttr -> convertRoleCiTypeCtrlAttrDtoToMap(roleCiTypeCtrlAttr,
                            accessControlAttributes))
                    .collect(toList());
        } else {
            roleCiTypeCtrlAttrsModels = Lists.newArrayList();
        }

        Map<String, List> result = new HashMap<>();
        result.put("header", accessControlAttributes);
        result.put("body", roleCiTypeCtrlAttrsModels);
        return result;
    }

    private Map<String, Object> convertRoleCiTypeCtrlAttrDtoToMap(RoleCiTypeCtrlAttrDto roleCiTypeCtrlAttr,
            List<CiTypeAttrDto> accessControlAttributes) {
        Map<String, Object> model = new LinkedHashMap<>();
        if (isNotEmpty(accessControlAttributes)) {
            Map<Integer, RoleCiTypeCtrlAttrConditionDto> conditionMap = asMap(roleCiTypeCtrlAttr.getConditions(),
                    RoleCiTypeCtrlAttrConditionDto::getCiTypeAttrId);
            accessControlAttributes.forEach(attr -> {
                RoleCiTypeCtrlAttrConditionDto condition = conditionMap.get(attr.getCiTypeAttrId());
                if (condition == null) {
                    condition = new RoleCiTypeCtrlAttrConditionDto();
                    condition.setRoleCiTypeCtrlAttrId(roleCiTypeCtrlAttr.getRoleCiTypeCtrlAttrId());
                    condition.setCiTypeAttrId(attr.getCiTypeAttrId());
                    condition.setCiTypeAttrName(attr.getName());
                    condition = cmdbServiceStub.createRoleCiTypeCtrlAttrCondition(condition);
                }
                enrichConditionValueObject(condition, attr);
                model.put(attr.getPropertyName(), condition);
            });
        }
        model.put(CONSTANT_CREATION_PERMISSION, roleCiTypeCtrlAttr.getCreationPermission());
        model.put(CONSTANT_REMOVAL_PERMISSION, roleCiTypeCtrlAttr.getRemovalPermission());
        model.put(CONSTANT_MODIFICATION_PERMISSION, roleCiTypeCtrlAttr.getModificationPermission());
        model.put(CONSTANT_ENQUIRY_PERMISSION, roleCiTypeCtrlAttr.getEnquiryPermission());
        model.put(CONSTANT_GRANT_PERMISSION, roleCiTypeCtrlAttr.getGrantPermission());
        model.put(CONSTANT_EXECUTION_PERMISSION, roleCiTypeCtrlAttr.getExecutionPermission());

        model.put("roleCiTypeId", roleCiTypeCtrlAttr.getRoleCiTypeId());
        model.put(CONSTANT_ROLE_CI_TYPE_CTRL_ATTR_ID, roleCiTypeCtrlAttr.getRoleCiTypeCtrlAttrId());
        return model;
    }

    private void enrichConditionValueObject(RoleCiTypeCtrlAttrConditionDto condition, CiTypeAttrDto attribute) {
        String conditionValue = condition.getConditionValue();
        if (!StringUtils.isEmpty(conditionValue)) {
            String inputType = attribute.getInputType();
            if (InputType.DROPLIST.getCode().equals(inputType)
                    || InputType.MULT_SEL_DROPLIST.getCode().equals(inputType)) {
                List<Integer> codeIds;
                if (conditionValue.contains(",")) {
                    codeIds = Stream.of(conditionValue.split(",")).map(String::trim).map(Integer::valueOf)
                            .collect(Collectors.toList());
                } else {
                    codeIds = Lists.newArrayList(Integer.parseInt(conditionValue));
                }
                condition.setConditionValueObject(Lists.newArrayList(cmdbServiceStub.getEnumCodesByIds(codeIds)));
            } else if (InputType.REFERENCE.getCode().equals(inputType)
                    || InputType.MULT_REF.getCode().equals(inputType)) {
                Integer targetCiTypeId = attribute.getReferenceId();
                List<String> guidList;
                if (conditionValue.contains(",")) {
                    guidList = Stream.of(conditionValue.split(",")).map(String::trim).collect(toList());
                } else {
                    guidList = Lists.newArrayList(conditionValue);
                }
                condition.setConditionValueObject(cmdbServiceStub.getCiDataByGuid(targetCiTypeId, guidList));
            } else {
                condition.setConditionValueObject(conditionValue);
            }
        }
    }

    private RoleCiTypeCtrlAttrDto convertMapToRoleCiTypeCtrlAttrDto(int roleCiTypeId, Map<String, Object> model,
            List<CiTypeAttrDto> accessControlAttributes) {
        RoleCiTypeCtrlAttrDto roleCiTypeCtrlAttr = new RoleCiTypeCtrlAttrDto();
        roleCiTypeCtrlAttr.setRoleCiTypeId(roleCiTypeId);
        if (model.containsKey(CONSTANT_ROLE_CI_TYPE_CTRL_ATTR_ID))
            roleCiTypeCtrlAttr.setRoleCiTypeCtrlAttrId((Integer) model.get(CONSTANT_ROLE_CI_TYPE_CTRL_ATTR_ID));

        if (model.containsKey(CONSTANT_CREATION_PERMISSION))
            roleCiTypeCtrlAttr.setCreationPermission(checkPermission((String) model.get(CONSTANT_CREATION_PERMISSION)));
        if (model.containsKey(CONSTANT_REMOVAL_PERMISSION))
            roleCiTypeCtrlAttr.setRemovalPermission(checkPermission((String) model.get(CONSTANT_REMOVAL_PERMISSION)));
        if (model.containsKey(CONSTANT_MODIFICATION_PERMISSION))
            roleCiTypeCtrlAttr
                    .setModificationPermission(checkPermission((String) model.get(CONSTANT_MODIFICATION_PERMISSION)));
        if (model.containsKey(CONSTANT_ENQUIRY_PERMISSION))
            roleCiTypeCtrlAttr.setEnquiryPermission(checkPermission((String) model.get(CONSTANT_ENQUIRY_PERMISSION)));
        if (model.containsKey(CONSTANT_GRANT_PERMISSION))
            roleCiTypeCtrlAttr.setGrantPermission(checkPermission((String) model.get(CONSTANT_GRANT_PERMISSION)));
        if (model.containsKey(CONSTANT_EXECUTION_PERMISSION))
            roleCiTypeCtrlAttr
                    .setExecutionPermission(checkPermission((String) model.get(CONSTANT_EXECUTION_PERMISSION)));

        if (model.containsKey(CONSTANT_CALLBACK_ID))
            roleCiTypeCtrlAttr.setCallbackId(String.valueOf(model.get(CONSTANT_CALLBACK_ID)));

        if (isNotEmpty(accessControlAttributes)) {
            accessControlAttributes.forEach(attr -> {
                Map conditionModel = (Map) model.get(attr.getPropertyName());
                if (conditionModel != null) {
                    roleCiTypeCtrlAttr.getConditions()
                            .add(convertMapToRoleCiTypeCtrlAttrConditionDto(conditionModel, attr));
                }
            });
        }
        return roleCiTypeCtrlAttr;
    }

    private String checkPermission(String permission) {
        return isTrue(permission) ? ENABLED : DISABLED;
    }

    private RoleCiTypeCtrlAttrConditionDto convertMapToRoleCiTypeCtrlAttrConditionDto(Map model,
            CiTypeAttrDto ciTypeAttr) {
        RoleCiTypeCtrlAttrConditionDto condition = new RoleCiTypeCtrlAttrConditionDto();
        condition.setCiTypeAttrId(ciTypeAttr.getCiTypeAttrId());
        condition.setCiTypeAttrName(ciTypeAttr.getName());
        if (model.containsKey("conditionId"))
            condition.setConditionId((Integer) model.get("conditionId"));
        if (model.containsKey("conditionValue"))
            condition.setConditionValue((String) model.get("conditionValue"));
        if (model.containsKey(CONSTANT_CALLBACK_ID))
            condition.setCallbackId(String.valueOf(model.get(CONSTANT_CALLBACK_ID)));
        return condition;
    }

    public List<Map<String, Object>> createRoleCiTypeCtrlAttributes(int roleCiTypeId,
            List<Map<String, Object>> roleCiTypeCtrlAttributes) {
        RoleCiTypeDto roleCiType = cmdbServiceStub.getRoleCiTypeById(roleCiTypeId);
        List<CiTypeAttrDto> accessControlAttributes = cmdbServiceStub
                .getCiTypeAccessControlAttributesByCiTypeId(roleCiType.getCiTypeId());

        List<Map<String, Object>> addedCtrlAttrDtos = Lists.newArrayList();
        if (isNotEmpty(roleCiTypeCtrlAttributes)) {
            roleCiTypeCtrlAttributes.forEach(roleCiTypeCtrlAttribute -> {
                RoleCiTypeCtrlAttrDto ctrlAttrDto = convertMapToRoleCiTypeCtrlAttrDto(roleCiTypeId,
                        roleCiTypeCtrlAttribute, accessControlAttributes);
                RoleCiTypeCtrlAttrDto addedCtrlAttr = cmdbServiceStub.createRoleCiTypeCtrlAttribute(ctrlAttrDto);
                if (isNotEmpty(ctrlAttrDto.getConditions())) {
                    ctrlAttrDto.getConditions().forEach(
                            condition -> condition.setRoleCiTypeCtrlAttrId(addedCtrlAttr.getRoleCiTypeCtrlAttrId()));
                    List<RoleCiTypeCtrlAttrConditionDto> addedConditions = cmdbServiceStub
                            .createRoleCiTypeCtrlAttrConditions(ctrlAttrDto.getConditions()
                                    .toArray(new RoleCiTypeCtrlAttrConditionDto[ctrlAttrDto.getConditions().size()]));
                    addedCtrlAttr.setConditions(addedConditions);
                }
                addedCtrlAttrDtos.add(convertRoleCiTypeCtrlAttrDtoToMap(addedCtrlAttr, accessControlAttributes));
            });
        }
        return addedCtrlAttrDtos;
    }

    public List<Map<String, Object>> updateRoleCiTypeCtrlAttributes(int roleCiTypeId,
            List<Map<String, Object>> roleCiTypeCtrlAttributes) {
        RoleCiTypeDto roleCiType = cmdbServiceStub.getRoleCiTypeById(roleCiTypeId);
        List<CiTypeAttrDto> accessControlAttributes = cmdbServiceStub
                .getCiTypeAccessControlAttributesByCiTypeId(roleCiType.getCiTypeId());

        List<Map<String, Object>> updateCtrlAttrDtos = Lists.newArrayList();
        if (isNotEmpty(roleCiTypeCtrlAttributes)) {
            roleCiTypeCtrlAttributes.forEach(roleCiTypeCtrlAttribute -> {
                RoleCiTypeCtrlAttrDto ctrlAttrDto = convertMapToRoleCiTypeCtrlAttrDto(roleCiTypeId,
                        roleCiTypeCtrlAttribute, accessControlAttributes);
                RoleCiTypeCtrlAttrDto updatedCtrlAttr = cmdbServiceStub.updateRoleCiTypeCtrlAttribute(ctrlAttrDto);
                if (isNotEmpty(ctrlAttrDto.getConditions())) {
                    ctrlAttrDto.getConditions().forEach(
                            condition -> condition.setRoleCiTypeCtrlAttrId(updatedCtrlAttr.getRoleCiTypeCtrlAttrId()));
                    List<RoleCiTypeCtrlAttrConditionDto> updatedConditions = cmdbServiceStub
                            .updateRoleCiTypeCtrlAttrConditions(ctrlAttrDto.getConditions()
                                    .toArray(new RoleCiTypeCtrlAttrConditionDto[ctrlAttrDto.getConditions().size()]));
                    updatedCtrlAttr.setConditions(updatedConditions);
                }
                updateCtrlAttrDtos.add(convertRoleCiTypeCtrlAttrDtoToMap(updatedCtrlAttr, accessControlAttributes));
            });
        }
        return updateCtrlAttrDtos;
    }

    public void grantRoleForUsers(int roleId, List<String> userIds) {
        if (isNotEmpty(userIds)) {
            cmdbServiceStub
                    .createRoleUsers(userIds.stream().map(u -> new RoleUser(roleId, u)).toArray(RoleUser[]::new));
        } else {
            log.info("Do nothing due to userIds is empty.");
        }
    }

    public void revokeRoleForUsers(int roleId, List<String> userIds) {
        if (isNotEmpty(userIds)) {
            List<RoleUser> roleUsers = cmdbServiceStub.getRoleUsers(
                    defaultQueryObject().addEqualsFilter("roleId", roleId).addInFilter("userId", userIds));
            if (isEmpty(roleUsers)) {
                log.warn("Nothing to delete because no permission found for role {} and userIds {}", roleId, userIds);
            } else {
                cmdbServiceStub
                        .deleteRoleUsers(roleUsers.stream().map(RoleUser::getRoleUserId).toArray(Integer[]::new));
            }
        } else {
            log.info("Nothing to delete because userIds is empty.");
        }
    }

    public void assignMenuPermissionForRoles(int roleId, List<String> menuCodes) {
        if (isNotEmpty(menuCodes)) {
            for (String menuCode : menuCodes) {
                assignMenuPermissionForRole(roleId, menuCode);
            }
        }
    }

    private void assignMenuPermissionForRole(int roleId, String menuCode) {
        MenuItem menuItem = menuItemRepository.findByCode(menuCode);
        if (menuItem == null)
            throw new WecubeCoreException("Unknown menu code " + menuCode);
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setMenuItem(menuItem);
        roleMenu.setRoleId(roleId);
        menuItem.getAssignedRoles().add(roleMenu);
        menuItemRepository.save(menuItem);
    }

    public void removeMenuPermissionForRoles(int roleId, List<String> menuCodes) {
        if (isNotEmpty(menuCodes)) {
            for (String menuCode : menuCodes) {
                removeMenuPermissionForRole(roleId, menuCode);
            }
        }
    }

    private void removeMenuPermissionForRole(int roleId, String menuCode) {
        MenuItem menuItem = menuItemRepository.findByCode(menuCode);
        if (menuItem == null)
            throw new WecubeCoreException("Unknown menu code " + menuCode);
        if (roleId == 1 && "ADMIN_PERMISSION_MANAGEMENT".equals(menuCode))
            throw new WecubeCoreException("Cannot be deleted as this is Admin permission menu.");
        if (menuItem.getAssignedRoles() != null) {
            menuItem.getAssignedRoles().removeIf(roleMenu -> roleMenu.getRoleId().equals(roleId));
        }
        menuItemRepository.save(menuItem);
    }

    public void assignCiTypePermissionForRole(int roleId, int ciTypeId, String actionCode) {
        RoleCiTypeDto roleCiType = cmdbServiceStub.getRoleCiTypeByRoleIdAndCiTypeId(roleId, ciTypeId);
        if (roleCiType == null) {
            throw new WecubeCoreException(
                    String.format("Permission for role[%d] ciType[%d] not found.", roleId, ciTypeId));
        } else {
            roleCiType.enableActionPermission(actionCode);
            cmdbServiceStub.updateRoleCiTypes(roleCiType);
        }
    }

    public void removeCiTypePermissionForRole(int roleId, int ciTypeId, String actionCode) {
        RoleCiTypeDto roleCiType = cmdbServiceStub.getRoleCiTypeByRoleIdAndCiTypeId(roleId, ciTypeId);
        if (roleCiType == null) {
            throw new WecubeCoreException(
                    String.format("Permission for role[%d] ciType[%d] not found.", roleId, ciTypeId));
        } else {
            roleCiType.disableActionPermission(actionCode);
            cmdbServiceStub.updateRoleCiTypes(roleCiType);
        }
    }

    public static class CiTypeIdComparator implements Comparator<RoleCiTypeDto> {
        @Override
        public int compare(RoleCiTypeDto o1, RoleCiTypeDto o2) {
            return o1.getCiTypeId().compareTo(o2.getCiTypeId());
        }
    }

}
