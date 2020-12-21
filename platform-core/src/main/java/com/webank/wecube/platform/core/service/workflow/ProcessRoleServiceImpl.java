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
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * @author howechen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcessRoleServiceImpl implements ProcessRoleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private UserManagementServiceImpl userManagementService;
    @Autowired
    private ProcRoleBindingMapper procRoleBindingRepository;

//    @Autowired
//    public ProcessRoleServiceImpl(UserManagementServiceImpl userManagementService,
//            ProcRoleBindingMapper procRoleBindingRepository) {
//        this.userManagementService = userManagementService;
//        this.procRoleBindingRepository = procRoleBindingRepository;
//    }

    @Override
    public ProcRoleOverviewDto retrieveRoleNamesByProcess(String procDefId){
        // check if the current user has the role to manage such process
        checkPermission(procDefId, ProcRoleBindingEntity.MGMT);

        ProcRoleOverviewDto result = new ProcRoleOverviewDto();
        result.setProcessId(procDefId);
        List<ProcRoleBindingEntity> allRoleBindingsByProc = this.procRoleBindingRepository.selectAllByProcId(procDefId);
        if(allRoleBindingsByProc == null || allRoleBindingsByProc.isEmpty()) {
            return result;
        }
        
        for(ProcRoleBindingEntity roleBinding  : allRoleBindingsByProc) {
            if(ProcRoleBindingEntity.USE.equalsIgnoreCase(roleBinding.getPermission())) {
                result.getUseRoleList().add(roleBinding.getRoleName());
            }else if(ProcRoleBindingEntity.MGMT.equalsIgnoreCase(roleBinding.getRoleName())) {
                result.getMgmtRoleList().add(roleBinding.getRoleName());
            }
        }
        
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveAllProcessByRoles(List<String> roleNameList) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (String roleName : roleNameList) {
            logger.info(String.format("Finding process to role binding information from roleName: [%s].", roleName));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository
                    .selectAllByRoleName(roleName);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public List<ProcRoleDto> retrieveProcessByRolesAndPermission(List<String> roleNames,
            String permission) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (String roleName : roleNames) {
            logger.info(String.format(
                    "Finding process to role binding information from roleName: [%s] and permission: [%s]", roleName,
                    permission));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository
                    .selectAllByRoleNameAndPermission(roleName, permission);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public void createProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionEnum = procRoleRequestDto.getPermission();
        List<String> roleNameList = procRoleRequestDto.getRoleNames();

        batchSaveData(procId, roleNameList, permissionEnum);
    }

    @Override
    public void updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permissionEnum = procRoleRequestDto.getPermission();
        List<String> roleNameList = procRoleRequestDto.getRoleNames();

        // check if user's roles has permission to manage this process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);
        batchSaveData(procId, roleNameList, permissionEnum);
    }

    @Override
    public void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        String permission = procRoleRequestDto.getPermission();

        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);

        // assure corresponding data has at least one row of MGMT permission
        if (ProcRoleBindingEntity.MGMT.equals(permission)) {
            List<ProcRoleBindingEntity> procRoleBindingEntities = this.procRoleBindingRepository
                    .selectAllByProcIdAndPermission(procId, permission);
            if (procRoleBindingEntities != null) {

                if (procRoleBindingEntities.size() <= procRoleRequestDto.getRoleNames().size()) {
                    String msg = "The process's management permission should have at least one role.";
                    logger.info(String.format(
                            "The DELETE management roles operation was blocked, the process id is [%s].", procId));
                    throw new WecubeCoreException("3184", msg);
                }
            }
        }

        for (String roleName : procRoleRequestDto.getRoleNames()) {
            this.procRoleBindingRepository.deleteByProcIdAndRoleAndPermission(procId, roleName, permission);
        }
    }

    public void checkPermission(String procId, String permission){
        List<String> currentUserRoleNameList = new ArrayList<>(
                Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));
        boolean ifUserHasSuchPermission = false;
        List<ProcRoleBindingEntity> foundProcRoleBinding = this.procRoleBindingRepository
                .selectAllByProcIdAndPermission(procId, permission);

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
            String msg = String.format("The user doesn't have process: [%s]'s [%s] permission", procId, permission);
            throw new WecubeCoreException("3185", msg, procId, permission);
        }

    }

    public void batchSaveData(String procId, List<String> roleNameList, String permission) {
        for (String roleName : roleNameList) {

//            RoleDto roleDto = userManagementService.retrieveRoleById(roleId);
//            String roleName = roleDto.getName();
            // find current stored data
            ProcRoleBindingEntity byProcIdAndRoleIdAndPermission = this.procRoleBindingRepository
                    .selectByProcIdAndRoleNameAndPermission(procId, roleName, permission);

            if (byProcIdAndRoleIdAndPermission != null) {
                logger.warn(String.format(
                        "Found stored data in DB, the given data is, procId: [%s], roleName: [%s], permission: [%s]",
                        procId, roleName, permission));
                return;
            }
            // if no stored data found, then save new data in to the database
            // get roleDto from auth server
            ProcRoleBindingEntity newEntity = new ProcRoleBindingEntity();

            newEntity.setProcId(procId);
            newEntity.setRoleId(roleName);
            newEntity.setPermission(permission);
            newEntity.setRoleName(roleName);

//          ProcRoleDto.toDomain(procId, roleId, permission, roleDto.getName());
            newEntity.setId(LocalIdGenerator.generateId());
            this.procRoleBindingRepository.insert(newEntity);
        }
    }
}
