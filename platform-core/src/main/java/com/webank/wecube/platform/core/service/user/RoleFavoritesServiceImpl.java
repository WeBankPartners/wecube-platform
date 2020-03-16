package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.FavoritesDto;
import com.webank.wecube.platform.core.dto.FavoritesRoleDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.workflow.FavoritesEntity;
import com.webank.wecube.platform.core.entity.workflow.FavoritesRoleEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.jpa.FavoritesInfoRepository;
import com.webank.wecube.platform.core.jpa.user.RoleFavoritesRepository;
import com.webank.wecube.platform.core.service.workflow.ProcessRoleServiceImpl;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sizhe
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleFavoritesServiceImpl implements RoleFavoritesService {

    private static final Logger log = LoggerFactory.getLogger(RoleFavoritesServiceImpl.class);
    @Autowired
    private RoleFavoritesRepository roleFavoritesRepository;

    @Autowired
    private FavoritesInfoRepository favoritesInfoRepository;
    @Autowired
    private UserManagementServiceImpl userManagementService;


    @Override
    public void createCollectionByRole(FavoritesDto favoritesDto) {

        String collectionName = favoritesDto.getCollectionName();
        if (StringUtils.isBlank(collectionName)) {
            throw new WecubeCoreException("Collection name cannot be empty.");
        }

        List<FavoritesEntity> existingCollections = favoritesInfoRepository.findAllCollectionByCollectionName(collectionName);
        if (existingCollections != null && !existingCollections.isEmpty()) {
            log.error("such process definition name already exists,collectionName={}", collectionName);
            throw new WecubeCoreException("CollectionName name should NOT duplicated.");
        }

        FavoritesEntity collectionsEntity = new FavoritesEntity();
        collectionsEntity.setFavoritesId(LocalIdGenerator.generateId());
        collectionsEntity.setCollectionName(favoritesDto.getCollectionName());
        collectionsEntity.setData(favoritesDto.getData().getBytes());
        collectionsEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUser().getUsername());
        collectionsEntity.setCreatedTime(new Date());

        FavoritesEntity savedProcDefInfoEntity = favoritesInfoRepository.save(collectionsEntity);
        // Save ProcRoleBindingEntity
        this.saveFavoritesRoleBinding(savedProcDefInfoEntity.getFavoritesId(), favoritesDto);
    }

    @Override
    public void deleteCollectionById(String favoritesId) {
        checkPermission(favoritesId, FavoritesRoleEntity.permissionEnum.MGMT);
        favoritesInfoRepository.deleteById(favoritesId);
        roleFavoritesRepository.deleteByFavoritesId(favoritesId);
    }

    @Override
    public void updateCollectionByRole(FavoritesDto favoritesDto) {
        // check if user's roles has permission to manage this process
        checkPermission(favoritesDto.getCollectionName(), FavoritesRoleEntity.permissionEnum.MGMT);
        saveFavoritesRoleBinding(favoritesDto.getFavoritesId(), favoritesDto);
    }

    @Override
    public List<FavoritesDto> retrieveAllCollections() {
        List<String> currentUserRoleNameList = new ArrayList<>(Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));

        Optional<List<FavoritesRoleEntity>> favoritesRoles = roleFavoritesRepository.findByRoleIdIn(currentUserRoleNameList);
        List<String> favoritesIds = new ArrayList<>();
        List<String> mgmtRoleIds;
        List<String> useRoleids;
        HashMap<String, List<String>> permissionToRole = new HashMap<>();
        if (favoritesRoles.isPresent()) {
            favoritesIds = favoritesRoles.get().stream().map(FavoritesRoleEntity::getFavoritesId).collect(Collectors.toList());
            mgmtRoleIds = favoritesRoles.get().stream().filter(favoritesRoleEntity -> favoritesRoleEntity.getPermission().equals(FavoritesRoleEntity.permissionEnum.MGMT)).map(FavoritesRoleEntity::getFavoritesId).collect(Collectors.toList());
            useRoleids = favoritesRoles.get().stream().filter(favoritesRoleEntity -> favoritesRoleEntity.getPermission().equals(FavoritesRoleEntity.permissionEnum.USE)).map(FavoritesRoleEntity::getFavoritesId).collect(Collectors.toList());
            permissionToRole.put(FavoritesRoleEntity.permissionEnum.MGMT.toString(),mgmtRoleIds);
            permissionToRole.put(FavoritesRoleEntity.permissionEnum.USE.toString(),useRoleids);
        }

        List<FavoritesEntity> favoritesEntitys = favoritesInfoRepository.findAllById(favoritesIds);
        return favoritesEntitys.stream().map(favoritesEntity -> FavoritesEntity.fromDomain(permissionToRole,favoritesEntity)).collect(Collectors.toList());
    }


    private void saveFavoritesRoleBinding(String collectId, FavoritesDto favoritesDto)
            throws WecubeCoreException {

        Map<String, List<String>> permissionToRoleMap = favoritesDto.getPermissionToRole();

        if (null == permissionToRoleMap) {
            throw new WecubeCoreException("There is no process to role with permission mapping found.");
        }

        String errorMsg;
        for (Map.Entry<String, List<String>> permissionToRoleListEntry : permissionToRoleMap.entrySet()) {
            String permissionStr = permissionToRoleListEntry.getKey();

            // check if key is empty or NULL
            if (StringUtils.isEmpty(permissionStr)) {
                errorMsg = "The permission key should not be empty or NULL";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }

            // check key is valid permission enum
            if (!EnumUtils.isValidEnum(ProcRoleBindingEntity.permissionEnum.class, permissionStr)) {
                errorMsg = "The request's key is not valid as a permission.";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }

            List<String> roleIdList = permissionToRoleListEntry.getValue();

            // check if roleIdList is NULL
            if (null == roleIdList) {
                errorMsg = String.format("The value of permission: [%s] should not be NULL", permissionStr);
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }
            // when permission is MGMT and roleIdList is empty, then it is
            // invalid
            if (ProcRoleBindingEntity.permissionEnum.MGMT.toString().equals(permissionStr) && roleIdList.isEmpty()) {
                errorMsg = "At least one role with MGMT role should be declared.";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }
            batchSaveRoleFavorites(collectId, roleIdList, permissionStr);
        }
    }

    public void batchSaveRoleFavorites(String favoritesId, List<String> roleIdList, String permissionStr) {
        FavoritesRoleEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
        for (String roleId : roleIdList) {
            RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
            // if no stored data found, then save new data in to the database
            // get roleDto from auth server
            this.roleFavoritesRepository.save(FavoritesRoleDto.toDomain(favoritesId, roleId, permissionEnum, roleDto.getName()));
        }
    }
    public static FavoritesRoleEntity.permissionEnum transferPermissionStrToEnum(String permissionStr) throws WecubeCoreException {
        FavoritesRoleEntity.permissionEnum permissionEnum;
        try {
            permissionEnum = FavoritesRoleEntity.permissionEnum.valueOf(FavoritesRoleEntity.permissionEnum.class,
                    Objects.requireNonNull(permissionStr, "Permission string cannot be NULL").toUpperCase());
        } catch (IllegalArgumentException ex) {
            String msg = String.format("The given permission string [%s] doesn't match platform-core's match cases.", permissionStr);
            throw new WecubeCoreException(msg);
        }
        return permissionEnum;
    }

    public void checkPermission(String favoritesId, FavoritesRoleEntity.permissionEnum permissionEnum) throws WecubeCoreException {
        List<String> currentUserRoleNameList = new ArrayList<>(Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        boolean ifUserHasSuchPermission = false;
        Optional<List<FavoritesRoleEntity>> foundProcRoleBinding = Optional.empty();
        switch (permissionEnum) {
            case MGMT:
                foundProcRoleBinding = this.roleFavoritesRepository.findAllByFavoritesIdAndPermission(favoritesId, FavoritesRoleEntity.permissionEnum.MGMT);
                break;
            case USE:
                foundProcRoleBinding = this.roleFavoritesRepository.findAllByFavoritesIdAndPermission(favoritesId, FavoritesRoleEntity.permissionEnum.USE);
                break;
            default:
                break;
        }

        List<String> roleNameListWithPermission = new ArrayList<>();
        if (foundProcRoleBinding.isPresent()) {
            roleNameListWithPermission = foundProcRoleBinding.get().stream().map(FavoritesRoleEntity::getRoleName).collect(Collectors.toList());
        }
        for (String roleId : currentUserRoleNameList) {
            if (roleNameListWithPermission.contains(roleId)) {
                ifUserHasSuchPermission = true;
                break;
            }
        }

        if (!ifUserHasSuchPermission) {
            String msg = String.format("The user doesn't have process: [%s]'s [%s] permission", favoritesId, permissionEnum.toString());
            throw new WecubeCoreException(msg);
        }
    }

}
