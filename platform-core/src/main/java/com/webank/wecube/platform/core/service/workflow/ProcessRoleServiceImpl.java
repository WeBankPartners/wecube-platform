package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
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
    public ProcRoleOverviewDto retrieveRoleIdByProcId(String token, String procId) throws WecubeCoreException {
        // check if the current user has the role to manage such process
        List<Long> roleIdListByUsername = this.userManagementService.getRoleIdListByUsername(token, AuthenticationContextHolder.getCurrentUsername());
        boolean ifUserHasMgmtPermission = checkIfUserHasMgmtPermission(procId, roleIdListByUsername);
        if (!ifUserHasMgmtPermission) {
            String msg = String.format("The user doesn't has process: [%s]'s MGMT permission", procId);
            throw new WecubeCoreException(msg);
        }

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
    public List<ProcRoleDto> retrieveAllProcessByRoleIdList(List<Long> roleIdList) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (Long roleId : roleIdList) {
            logger.info(String.format("Finding process to role binding information from roleId: [%s].", roleId));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository.findAllByRoleId(roleId);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveProcessByRoleIdListAndPermission(List<Long> roleIdList, String permissionStr) {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
        List<ProcRoleDto> result = new ArrayList<>();
        for (Long roleId : roleIdList) {
            logger.info(String.format("Finding process to role binding information from roleId: [%s] and permission: [%s]", roleId, permissionEnum.toString()));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository.findAllByRoleIdAndPermission(roleId, permissionEnum);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public ProcRoleDto updateProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionStr = procRoleRequestDto.getPermission();
        Long roleId = procRoleRequestDto.getRoleId();
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);

        List<Long> roleIdListByUsername = this.userManagementService.getRoleIdListByUsername(token, AuthenticationContextHolder.getCurrentUsername());
        boolean ifUserHasMgmtPermission = checkIfUserHasMgmtPermission(procId, roleIdListByUsername);
        if (!ifUserHasMgmtPermission) {
            String msg = String.format("The user doesn't has process: [%s]'s MGMT permission", procId);
            throw new WecubeCoreException(msg);
        }


        // find current stored data
        Optional<ProcRoleBindingEntity> byProcIdAndRoleIdAndPermission = this.procRoleBindingRepository.findByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);

        if (byProcIdAndRoleIdAndPermission.isPresent()) {
            logger.warn(String.format("Found stored data in DB, the given data is, procId: [%s], roleId: [%s], permission: [%s]", procId, roleId, permissionStr));
            return ProcRoleDto.fromDomain(byProcIdAndRoleIdAndPermission.get());
        }
        // if no stored data found, then save new data in to the database
        ProcRoleBindingEntity savedResult = this.procRoleBindingRepository.save(ProcRoleDto.toDomain(procId, roleId, permissionEnum));
        return ProcRoleDto.fromDomain(savedResult);
    }

    @Override
    public ProcRoleDto updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) {
        String permissionStr = procRoleRequestDto.getPermission();
        Long roleId = procRoleRequestDto.getRoleId();
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);

        // find current stored data
        Optional<ProcRoleBindingEntity> byProcIdAndRoleIdAndPermission = this.procRoleBindingRepository.findByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);

        if (byProcIdAndRoleIdAndPermission.isPresent()) {
            logger.warn(String.format("Found stored data in DB, the given data is, procId: [%s], roleId: [%s], permission: [%s]", procId, roleId, permissionStr));
            return ProcRoleDto.fromDomain(byProcIdAndRoleIdAndPermission.get());
        }
        // if no stored data found, then save new data in to the database
        ProcRoleBindingEntity savedResult = this.procRoleBindingRepository.save(ProcRoleDto.toDomain(procId, roleId, permissionEnum));
        return ProcRoleDto.fromDomain(savedResult);
    }

    @Override
    public void deleteProcRoleBinding(String token, String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(procRoleRequestDto.getPermission());

        // check if the current user has the role to manage such process
        List<Long> roleIdListByUsername = this.userManagementService.getRoleIdListByUsername(token, AuthenticationContextHolder.getCurrentUsername());
        boolean ifUserHasMgmtPermission = checkIfUserHasMgmtPermission(procId, roleIdListByUsername);
        if (!ifUserHasMgmtPermission) {
            String msg = String.format("The user doesn't has process: [%s]'s MGMT permission", procId);
            throw new WecubeCoreException(msg);
        }

        // assure corresponding data has at least one row of MGMT permission
        if (ProcRoleBindingEntity.permissionEnum.MGMT.equals(permissionEnum)) {
            Optional<List<ProcRoleBindingEntity>> foundMgmtData = this.procRoleBindingRepository.findByProcIdAndPermission(procId, permissionEnum);
            foundMgmtData.ifPresent(procRoleBindingEntities -> {
                if (procRoleBindingEntities.size() <= 1) {
                    String msg = "The process's management permission should have at least one role.";
                    logger.info(String.format("The DELETE management roles operation was blocked, the process id is [%s].", procId));
                    throw new WecubeCoreException(msg);
                }
            });
        }

        Optional<ProcRoleBindingEntity> foundData = this.procRoleBindingRepository.findByProcIdAndRoleIdAndPermission(procId, procRoleRequestDto.getRoleId(), permissionEnum);
        foundData.ifPresent(procRoleBindingEntity -> this.procRoleBindingRepository.delete(procRoleBindingEntity));
    }

    public boolean checkIfUserHasMgmtPermission(String procId, List<Long> userOwnedRoleIdList) {
        Optional<List<ProcRoleBindingEntity>> byProcIdAndPermission = this.procRoleBindingRepository.findByProcIdAndPermission(procId, ProcRoleBindingEntity.permissionEnum.MGMT);
        List<Long> mgmtRoleIdList = new ArrayList<>();
        if (byProcIdAndPermission.isPresent()) {
            mgmtRoleIdList = byProcIdAndPermission.get().stream().map(ProcRoleBindingEntity::getRoleId).collect(Collectors.toList());
        }
        for (Long roleId : userOwnedRoleIdList) {
            if (mgmtRoleIdList.contains(roleId)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIfUserHasUsePermission(String procId, List<Long> userOwnedRoleIdList) {
        Optional<List<ProcRoleBindingEntity>> byProcIdAndPermission = this.procRoleBindingRepository.findByProcIdAndPermission(procId, ProcRoleBindingEntity.permissionEnum.USE);
        List<Long> mgmtRoleIdList = new ArrayList<>();
        if (byProcIdAndPermission.isPresent()) {
            mgmtRoleIdList = byProcIdAndPermission.get().stream().map(ProcRoleBindingEntity::getRoleId).collect(Collectors.toList());
        }
        for (Long roleId : userOwnedRoleIdList) {
            if (mgmtRoleIdList.contains(roleId)) {
                return true;
            }
        }
        return false;
    }
}
