package com.webank.wecube.platform.core.service.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.FavoritesDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.plugin.Favorites;
import com.webank.wecube.platform.core.entity.plugin.FavoritesRole;
import com.webank.wecube.platform.core.repository.plugin.FavoritesMapper;
import com.webank.wecube.platform.core.repository.plugin.FavoritesRoleMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * @author sizhe
 */
@Service
public class RoleFavoritesServiceImpl implements RoleFavoritesService {

    private static final Logger log = LoggerFactory.getLogger(RoleFavoritesServiceImpl.class);
    @Autowired
    private FavoritesRoleMapper favoritesRoleMapper;

    @Autowired
    private FavoritesMapper favoritesMapper;

//    @Autowired
//    private UserManagementServiceImpl userManagementService;

    /**
     * 
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCollectionByRole(FavoritesDto favoritesDto) {

        String collectionName = favoritesDto.getCollectionName();
        if (StringUtils.isBlank(collectionName)) {
            throw new WecubeCoreException("3256", "Collection name cannot be empty.");
        }

        List<Favorites> favoritiesEntities = favoritesMapper.selectAllByCollectionName(collectionName);
        if (favoritiesEntities != null && !favoritiesEntities.isEmpty()) {
            log.error("such process definition name already exists,collectionName={}", collectionName);
            throw new WecubeCoreException("3257", "CollectionName name should NOT duplicated.");
        }

        Favorites collectionsEntity = new Favorites();
        collectionsEntity.setFavoritesId(LocalIdGenerator.generateId());
        collectionsEntity.setCollectionName(favoritesDto.getCollectionName());
        collectionsEntity.setData(favoritesDto.getData().getBytes());
        collectionsEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUser().getUsername());
        collectionsEntity.setCreatedTime(new Date());

        favoritesMapper.insert(collectionsEntity);
        // Save ProcRoleBindingEntity
        try {
            this.saveFavoritesRoleBinding(collectionsEntity.getFavoritesId(), favoritesDto);
        } catch (Exception e) {
            log.error("error", e);
            throw new WecubeCoreException(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCollectionById(String favoritesId) {
        checkPermission(favoritesId, FavoritesRole.MGMT);
        favoritesRoleMapper.deleteAllByFavorites(favoritesId);
        favoritesMapper.deleteByPrimaryKey(favoritesId);
    }

    public List<FavoritesDto> retrieveAllCollections() {
        List<String> currentUserRoleNameList = new ArrayList<>(
                Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        List<FavoritesRole> favoritesRoles = favoritesRoleMapper.selectAllByRoles(currentUserRoleNameList);
        Set<String> favoritesIds = new HashSet<>();
        if (favoritesRoles != null) {
            for (FavoritesRole fr : favoritesRoles) {
                favoritesIds.add(fr.getFavoritesId());
            }
        }
        List<Favorites> favoritesEntities = new ArrayList<>();
        for (String favoritesId : favoritesIds) {
            Favorites favoritesEntity = favoritesMapper.selectByPrimaryKey(favoritesId);
            if (favoritesEntity != null) {
                favoritesEntities.add(favoritesEntity);
            }
        }

        List<FavoritesDto> favoritesDtos = new ArrayList<>();

        for (Favorites favoritesEntity : favoritesEntities) {

            List<FavoritesRole> favoritesRolesEntities = favoritesRoleMapper
                    .selectAllByFavorites(favoritesEntity.getFavoritesId());
            HashMap<String, List<String>> permissionToRole = new HashMap<>();
            if (favoritesRolesEntities != null) {
                List<String> mgmtRoleNames = new ArrayList<>();
                List<String> useRoleNames = new ArrayList<>();

                for (FavoritesRole fr : favoritesRolesEntities) {
                    if (FavoritesRole.MGMT.equals(fr.getPermission())) {
                        mgmtRoleNames.add(fr.getRoleName());
                    }

                    if (FavoritesRole.USE.equals(fr.getPermission())) {
                        useRoleNames.add(fr.getRoleName());
                    }
                }
                permissionToRole.put(FavoritesRole.MGMT, mgmtRoleNames);
                permissionToRole.put(FavoritesRole.USE, useRoleNames);
            }

            FavoritesDto frDto = buildFavoritesDto(favoritesEntity);
            frDto.setPermissionToRole(permissionToRole);
            favoritesDtos.add(frDto);
        }

        return favoritesDtos;
    }

    private FavoritesDto buildFavoritesDto(Favorites favoritesEntity) {
        FavoritesDto result = new FavoritesDto();
        result.setFavoritesId(favoritesEntity.getFavoritesId());
        result.setData(new String(favoritesEntity.getData()));
        result.setCollectionName(favoritesEntity.getCollectionName());
        // result.setPermissionToRole(permissionToRole);
        result.setCreatedBy(favoritesEntity.getCreatedBy());
        result.setCreatedTime(favoritesEntity.getCreatedTime());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFavoritesRoleBinding(String favoritesId, ProcRoleRequestDto favoritesRoleRequestDto) {
        
        
        if(favoritesRoleRequestDto == null ) {
            throw new WecubeCoreException("There is not role setting provided.");
        }
        Map<String,List<String>> permissionToRole = favoritesRoleRequestDto.getPermissionToRole();
        if(permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("There is not role setting provided.");
        }
        
        checkPermission(favoritesId, FavoritesRole.MGMT);
        
        for(Map.Entry<String, List<String>> entry :permissionToRole.entrySet()) {
            String permission = entry.getKey();
            List<String> roleNames = entry.getValue();
            
            if(roleNames == null || roleNames.isEmpty()) {
                continue;
            }
            
            if (FavoritesRole.MGMT.equals(permission)) {
                List<FavoritesRole> foundMgmtData = this.favoritesRoleMapper
                        .selectAllByFavoritesAndPermission(favoritesId, permission);
                if(foundMgmtData != null){
                    if(foundMgmtData.size() <= roleNames.size()){
                        String msg = "The process's management permission should have at least one role.";
                        log.info(String.format("The DELETE management roles operation was blocked, the process id is [%s].",
                                favoritesId));
                        throw new WecubeCoreException("3258", msg);
                    }
                }
                
            }

            for (String roleName : roleNames) {
                this.favoritesRoleMapper.deleteByfavoritesIdAndRoleNameAndPermission(favoritesId, roleName, permission);
            }
        }

        // check if the current user has the role to manage such process

        // assure corresponding data has at least one row of MGMT permission
        
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFavoritesRoleBinding(String favoritesId, ProcRoleRequestDto favoritesRoleRequestDto) {
        
        
        if(favoritesRoleRequestDto == null ) {
            throw new WecubeCoreException("There is not role setting provided.");
        }
        Map<String,List<String>> permissionToRole = favoritesRoleRequestDto.getPermissionToRole();
        if(permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("There is not role setting provided.");
        }
        
        checkPermission(favoritesId, FavoritesRole.MGMT);
        
        for(Map.Entry<String, List<String>> entry :permissionToRole.entrySet()) {
            String permission = entry.getKey();
            List<String> roleNames = entry.getValue();
            
            if(roleNames == null || roleNames.isEmpty()) {
                continue;
            }
            
            // check if user's roles has permission to manage this process
            batchSaveRoleFavorites(favoritesId, roleNames, permission);
        }
        
        
    }

    private void saveFavoritesRoleBinding(String collectId, FavoritesDto favoritesDto) throws WecubeCoreException {

        Map<String, List<String>> permissionToRoleMap = favoritesDto.getPermissionToRole();

        if (permissionToRoleMap == null) {
            throw new WecubeCoreException("3259", "There is no favorites to role with permission mapping found.");
        }

        String errorMsg;
        for (Map.Entry<String, List<String>> permissionToRoleListEntry : permissionToRoleMap.entrySet()) {
            String permissionStr = permissionToRoleListEntry.getKey();

            // check if key is empty or NULL
            if (StringUtils.isEmpty(permissionStr)) {
                errorMsg = "The permission key should not be empty or NULL";
                log.error(errorMsg);
                throw new WecubeCoreException("3260", errorMsg);
            }

            List<String> roleNameList = permissionToRoleListEntry.getValue();

            // check if roleIdList is NULL
            if (roleNameList == null) {
                errorMsg = String.format("The value of permission: [%s] should not be NULL", permissionStr);
                log.error(errorMsg);
                throw new WecubeCoreException("3262", errorMsg);
            }
            // when permission is MGMT and roleIdList is empty, then it is
            // invalid
            if (FavoritesRole.MGMT.equals(permissionStr) && roleNameList.isEmpty()) {
                errorMsg = "At least one role with MGMT role should be declared.";
                log.error(errorMsg);
                throw new WecubeCoreException("3263", errorMsg);
            }
            batchSaveRoleFavorites(collectId, roleNameList, permissionStr);
        }
    }

    public void batchSaveRoleFavorites(String favoritesId, List<String> roleNameList, String permissionStr) {
        for (String roleName : roleNameList) {
            //RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
            // if no stored data found, then save new data in to the database
            // get roleDto from auth server
            FavoritesRole favoritesRoleEntity = buildFavoritesRole(LocalIdGenerator.generateId(), favoritesId, null,
                    permissionStr, roleName);
            this.favoritesRoleMapper.insert(favoritesRoleEntity);
        }
    }

    private FavoritesRole buildFavoritesRole(String id, String favoritesId, String roleId, String permissionEnum,
            String roleName) {
        FavoritesRole result = new FavoritesRole();
        result.setId(id);
        result.setFavoritesId(favoritesId);
        result.setRoleId(roleId);
        result.setPermission(permissionEnum);
        result.setRoleName(roleName);
        return result;
    }

    public void checkPermission(String favoritesId, String permission) throws WecubeCoreException {
        List<String> currentUserRoleNameList = new ArrayList<>(
                Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        boolean ifUserHasSuchPermission = false;
        List<FavoritesRole> foundProcRoleBinding = this.favoritesRoleMapper
                .selectAllByFavoritesAndPermission(favoritesId, permission);

        List<String> roleNameListWithPermission = new ArrayList<>();
        if (foundProcRoleBinding != null) {
            for (FavoritesRole fr : foundProcRoleBinding) {
                roleNameListWithPermission.add(fr.getRoleName());
            }
        }
        for (String roleName : currentUserRoleNameList) {
            if (roleNameListWithPermission.contains(roleName)) {
                ifUserHasSuchPermission = true;
                break;
            }
        }

        if (!ifUserHasSuchPermission) {
            String msg = String.format("The user doesn't have favorites: [%s]'s [%s] permission", favoritesId,
                    permission);
            throw new WecubeCoreException("3265", msg, favoritesId, permission);
        }
    }

}
