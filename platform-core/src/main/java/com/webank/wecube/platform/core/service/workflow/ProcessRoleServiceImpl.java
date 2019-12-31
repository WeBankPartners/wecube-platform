package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import com.webank.wecube.platform.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author howechen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcessRoleServiceImpl implements ProcessRoleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UserManagementServiceImpl userManagementService;
    private ProcRoleBindingRepository procRoleBindingRepository;

    @Autowired
    public ProcessRoleServiceImpl(UserManagementServiceImpl userManagementService, ProcRoleBindingRepository procRoleBindingRepository) {
        this.userManagementService = userManagementService;
        this.procRoleBindingRepository = procRoleBindingRepository;
    }

    public static ProcRoleBindingEntity.permissionEnum transferPermissionStrToEnum(String permissionStr) throws WecubeCoreException {
        ProcRoleBindingEntity.permissionEnum permissionEnum;
        try {
            permissionEnum = ProcRoleBindingEntity.permissionEnum.valueOf(ProcRoleBindingEntity.permissionEnum.class,
                    Objects.requireNonNull(permissionStr, "Permission string cannot be NULL").toUpperCase());
        } catch (IllegalArgumentException ex) {
            String msg = String.format("The given permission string [%s] doesn't match platform-core's match cases.", permissionStr);
            throw new WecubeCoreException(msg);
        }
        return permissionEnum;
    }

    @Override
    public ProcRoleOverviewDto retrieveRoleIdByProcId(String procId) throws WecubeCoreException {
        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);

        ProcRoleOverviewDto result = new ProcRoleOverviewDto();
        result.setProcessId(procId);
        List<ProcRoleBindingEntity> allByProcId = this.procRoleBindingRepository.findAllByProcId(procId);
        allByProcId.forEach(procRoleBindingEntity -> {
            switch (procRoleBindingEntity.getPermission()) {
                case USE:
                    result.getUseRoleIdList().add(procRoleBindingEntity.getRoleId());
                    break;
                case MGMT:
                    result.getMgmtRoleIdList().add(procRoleBindingEntity.getRoleId());
                    break;
                default:
                    break;
            }
        });
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveAllProcessByRoleIdList(List<String> roleNameList) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (String roleName : roleNameList) {
            logger.info(String.format("Finding process to role binding information from roleName: [%s].", roleName));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository.findAllByRoleName(roleName);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveProcessByRoleIdListAndPermission(List<String> roleNameList, String permissionStr) {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
        List<ProcRoleDto> result = new ArrayList<>();
        for (String roleName : roleNameList) {
            logger.info(String.format("Finding process to role binding information from roleName: [%s] and permission: [%s]", roleName, permissionEnum.toString()));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository.findAllByRoleNameAndPermission(roleName, permissionEnum);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public void createProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionStr = procRoleRequestDto.getPermission();
        List<String> roleIdList = procRoleRequestDto.getRoleIdList();
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);

        batchSaveData(token, procId, roleIdList, permissionStr);
    }

    @Override
    public void updateProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionStr = procRoleRequestDto.getPermission();
        List<String> roleIdList = procRoleRequestDto.getRoleIdList();
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);

        // check if user's roles has permission to manage this process
        checkPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);
        batchSaveData(token, procId, roleIdList, permissionStr);
    }

    @Override
    public void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(procRoleRequestDto.getPermission());

        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);

        // assure corresponding data has at least one row of MGMT permission
        if (ProcRoleBindingEntity.permissionEnum.MGMT.equals(permissionEnum)) {
            Optional<List<ProcRoleBindingEntity>> foundMgmtData = this.procRoleBindingRepository.findAllByProcIdAndPermission(procId, permissionEnum);
            foundMgmtData.ifPresent(procRoleBindingEntities -> {
                if (procRoleBindingEntities.size() <= procRoleRequestDto.getRoleIdList().size()) {
                    String msg = "The process's management permission should have at least one role.";
                    logger.info(String.format("The DELETE management roles operation was blocked, the process id is [%s].", procId));
                    throw new WecubeCoreException(msg);
                }
            });
        }

        for (String roleId : procRoleRequestDto.getRoleIdList()) {
            this.procRoleBindingRepository.deleteByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);
        }
    }

    public void checkPermission(String procId, ProcRoleBindingEntity.permissionEnum permissionEnum) throws WecubeCoreException {
        List<String> currentUserRoleNameList = new ArrayList<>(Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        boolean ifUserHasSuchPermission = false;
        Optional<List<ProcRoleBindingEntity>> foundProcRoleBinding = Optional.empty();
        switch (permissionEnum) {
            case MGMT:
                foundProcRoleBinding = this.procRoleBindingRepository.findAllByProcIdAndPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);
                break;
            case USE:
                foundProcRoleBinding = this.procRoleBindingRepository.findAllByProcIdAndPermission(procId, ProcRoleBindingEntity.permissionEnum.USE);
                break;
            default:
                break;
        }

        List<String> roleNameListWithPermission = new ArrayList<>();
        if (foundProcRoleBinding.isPresent()) {
            roleNameListWithPermission = foundProcRoleBinding.get().stream().map(ProcRoleBindingEntity::getRoleName).collect(Collectors.toList());
        }
        for (String roleId : currentUserRoleNameList) {
            if (roleNameListWithPermission.contains(roleId)) {
                ifUserHasSuchPermission = true;
                break;
            }
        }

        if (!ifUserHasSuchPermission) {
            String msg = String.format("The user doesn't have process: [%s]'s [%s] permission", procId, permissionEnum.toString());
            throw new WecubeCoreException(msg);
        }

    }

    public void batchSaveData(String token, String procId, List<String> roleIdList, String permissionStr) {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
        for (String roleId : roleIdList) {

            RoleDto roleDto = JsonUtils.toObject(userManagementService.retrieveRoleById(token, roleId).getData(), RoleDto.class);
            String roleName = roleDto.getName();
            // find current stored data
            Optional<ProcRoleBindingEntity> byProcIdAndRoleIdAndPermission = this.procRoleBindingRepository
                    .findByProcIdAndRoleNameAndPermission(
                            procId,
                            roleName,
                            permissionEnum);

            if (byProcIdAndRoleIdAndPermission.isPresent()) {
                logger.warn(String.format("Found stored data in DB, the given data is, procId: [%s], roleId: [%s], permission: [%s]", procId, roleId, permissionEnum.toString()));
                return;
            }
            // if no stored data found, then save new data in to the database
            // get roleDto from auth server
            this.procRoleBindingRepository.save(ProcRoleDto.toDomain(procId, roleId, permissionEnum, roleDto.getName()));
        }
    }
}
