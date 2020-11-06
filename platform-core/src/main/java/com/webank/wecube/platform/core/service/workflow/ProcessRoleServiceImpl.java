package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * @author howechen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcessRoleServiceImpl implements ProcessRoleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UserManagementServiceImpl userManagementService;
    private ProcRoleBindingMapper procRoleBindingRepository;

    @Autowired
    public ProcessRoleServiceImpl(UserManagementServiceImpl userManagementService,
            ProcRoleBindingMapper procRoleBindingRepository) {
        this.userManagementService = userManagementService;
        this.procRoleBindingRepository = procRoleBindingRepository;
    }

    @Override
    public ProcRoleOverviewDto retrieveRoleIdByProcId(String procId) throws WecubeCoreException {
        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);

        ProcRoleOverviewDto result = new ProcRoleOverviewDto();
        result.setProcessId(procId);
        List<ProcRoleBindingEntity> allByProcId = this.procRoleBindingRepository.findAllByProcId(procId);
        allByProcId.forEach(procRoleBindingEntity -> {
            switch (procRoleBindingEntity.getPermission()) {
            case ProcRoleBindingEntity.USE:
                result.getUseRoleIdList().add(procRoleBindingEntity.getRoleId());
                break;
            case ProcRoleBindingEntity.MGMT:
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
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository
                    .findAllByRoleName(roleName);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveProcessByRoleIdListAndPermission(List<String> roleNameList,
            String permissionEnum) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (String roleName : roleNameList) {
            logger.info(String.format(
                    "Finding process to role binding information from roleName: [%s] and permission: [%s]", roleName,
                    permissionEnum));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository
                    .findAllByRoleNameAndPermission(roleName, permissionEnum);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public void createProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionEnum = procRoleRequestDto.getPermission();
        List<String> roleIdList = procRoleRequestDto.getRoleIdList();

        batchSaveData(procId, roleIdList, permissionEnum);
    }

    @Override
    public void updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionEnum = procRoleRequestDto.getPermission();
        List<String> roleIdList = procRoleRequestDto.getRoleIdList();

        // check if user's roles has permission to manage this process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);
        batchSaveData(procId, roleIdList, permissionEnum);
    }

    @Override
    public void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionEnum = procRoleRequestDto.getPermission();

        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);

        // assure corresponding data has at least one row of MGMT permission
        if (ProcRoleBindingEntity.MGMT.equals(permissionEnum)) {
            List<ProcRoleBindingEntity> procRoleBindingEntities = this.procRoleBindingRepository
                    .findAllByProcIdAndPermission(procId, permissionEnum);
            if (procRoleBindingEntities != null) {

                if (procRoleBindingEntities.size() <= procRoleRequestDto.getRoleIdList().size()) {
                    String msg = "The process's management permission should have at least one role.";
                    logger.info(String.format(
                            "The DELETE management roles operation was blocked, the process id is [%s].", procId));
                    throw new WecubeCoreException("3184", msg);
                }
            }
        }

        for (String roleId : procRoleRequestDto.getRoleIdList()) {
            this.procRoleBindingRepository.deleteByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);
        }
    }

    public void checkPermission(String procId, String permissionEnum) throws WecubeCoreException {
        List<String> currentUserRoleNameList = new ArrayList<>(
                Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        boolean ifUserHasSuchPermission = false;
        List<ProcRoleBindingEntity> foundProcRoleBinding = null;
        switch (permissionEnum) {
        case ProcRoleBindingEntity.MGMT:
            foundProcRoleBinding = this.procRoleBindingRepository.findAllByProcIdAndPermission(procId,
                    ProcRoleBindingEntity.MGMT);
            break;
        case ProcRoleBindingEntity.USE:
            foundProcRoleBinding = this.procRoleBindingRepository.findAllByProcIdAndPermission(procId,
                    ProcRoleBindingEntity.USE);
            break;
        default:
            break;
        }

        List<String> roleNameListWithPermission = new ArrayList<>();
        if (foundProcRoleBinding != null) {
            roleNameListWithPermission = foundProcRoleBinding.stream().map(ProcRoleBindingEntity::getRoleName)
                    .collect(Collectors.toList());
        }
        for (String roleId : currentUserRoleNameList) {
            if (roleNameListWithPermission.contains(roleId)) {
                ifUserHasSuchPermission = true;
                break;
            }
        }

        if (!ifUserHasSuchPermission) {
            String msg = String.format("The user doesn't have process: [%s]'s [%s] permission", procId,
                    permissionEnum.toString());
            throw new WecubeCoreException("3185", msg, procId, permissionEnum.toString());
        }

    }

    public void batchSaveData(String procId, List<String> roleIdList, String permissionEnum) {
        for (String roleId : roleIdList) {

            RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
            String roleName = roleDto.getName();
            // find current stored data
            ProcRoleBindingEntity byProcIdAndRoleIdAndPermission = this.procRoleBindingRepository
                    .findByProcIdAndRoleNameAndPermission(procId, roleName, permissionEnum);

            if (byProcIdAndRoleIdAndPermission != null) {
                logger.warn(String.format(
                        "Found stored data in DB, the given data is, procId: [%s], roleId: [%s], permission: [%s]",
                        procId, roleId, permissionEnum.toString()));
                return;
            }
            // if no stored data found, then save new data in to the database
            // get roleDto from auth server
            ProcRoleBindingEntity newEntity = ProcRoleDto.toDomain(procId, roleId, permissionEnum, roleDto.getName());
            newEntity.setId(LocalIdGenerator.generateId());
            this.procRoleBindingRepository.insert(newEntity);
        }
    }
}
