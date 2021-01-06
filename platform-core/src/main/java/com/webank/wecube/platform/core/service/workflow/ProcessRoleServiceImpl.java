package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            }else if(ProcRoleBindingEntity.MGMT.equalsIgnoreCase(roleBinding.getPermission())) {
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
        if(procRoleRequestDto == null ) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        Map<String,List<String>> permissionToRole = procRoleRequestDto.getPermissionToRole();
        if(permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        
        for(Map.Entry<String, List<String>> entry :permissionToRole.entrySet()) {
            String permission = entry.getKey();
            List<String> roleNames = entry.getValue();
            
            if(roleNames == null || roleNames.isEmpty()) {
                continue;
            }

            batchSaveData(procId, roleNames, permission);
        }
        
        
    }

    @Override
    public void updateProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        
        if(procRoleRequestDto == null ) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        Map<String,List<String>> permissionToRole = procRoleRequestDto.getPermissionToRole();
        if(permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        
        // check if user's roles has permission to manage this process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);
        
        for(Map.Entry<String, List<String>> entry :permissionToRole.entrySet()) {
            String permission = entry.getKey();
            List<String> inputRoleNames = entry.getValue();
            
            if(inputRoleNames == null) {
                inputRoleNames = new ArrayList<>();
            }
            
            List<String> existRoleNames = getExistRoleBindingsOfProcessAndPermission(procId, permission);
            
            List<String> roleNamesToAdd = new ArrayList<>();
            for(String roleName : inputRoleNames) {
                if(existRoleNames.contains(roleName)) {
                    continue;
                }
                
                roleNamesToAdd.add(roleName);
            }
            
            List<String> roleNamesToRemove = new ArrayList<>();
            
            for(String roleName : existRoleNames) {
                if(inputRoleNames.contains(roleName)) {
                    continue;
                }
                
                roleNamesToRemove.add(roleName);
            }

            for (String roleName : roleNamesToRemove) {
                this.procRoleBindingRepository.deleteByProcIdAndRoleAndPermission(procId, roleName, permission);
            }
            batchSaveData(procId, roleNamesToAdd, permission);
        }
    }

    @Override
    public void deleteProcRoleBinding(String procId, ProcRoleRequestDto procRoleRequestDto) throws WecubeCoreException {
        
        if(procRoleRequestDto == null ) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        Map<String,List<String>> permissionToRole = procRoleRequestDto.getPermissionToRole();
        if(permissionToRole == null || permissionToRole.isEmpty()) {
            throw new WecubeCoreException("There is not process role setting provided.");
        }
        
        // check if the current user has the role to manage such process
        checkPermission(procId, ProcRoleBindingEntity.MGMT);
        
        

        for(Map.Entry<String, List<String>> entry :permissionToRole.entrySet()) {
            String permission = entry.getKey();
            List<String> roleNames = entry.getValue();
            
            if(roleNames == null || roleNames.isEmpty()) {
                continue;
            }
            
            if (ProcRoleBindingEntity.MGMT.equals(permission)) {
                List<ProcRoleBindingEntity> procRoleBindingEntities = this.procRoleBindingRepository
                        .selectAllByProcIdAndPermission(procId, permission);
                if (procRoleBindingEntities != null) {

                    if (procRoleBindingEntities.size() <= roleNames.size()) {
                        String msg = "The process's management permission should have at least one role.";
                        logger.info(String.format(
                                "The DELETE management roles operation was blocked, the process id is [%s].", procId));
                        throw new WecubeCoreException("3184", msg);
                    }
                }
            }

            for (String roleName : roleNames) {
                this.procRoleBindingRepository.deleteByProcIdAndRoleAndPermission(procId, roleName, permission);
            }
        }

        // assure corresponding data has at least one row of MGMT permission
        
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
    
    private List<String> getExistRoleBindingsOfProcessAndPermission(String procDefId, String permission){
        List<String> existRoleNames = new ArrayList<>();
        
        List<ProcRoleBindingEntity> roleBindings = procRoleBindingRepository.selectAllByProcIdAndPermission(procDefId, permission);
        if(roleBindings == null || roleBindings.isEmpty()) {
            return existRoleNames;
        }
        
        for(ProcRoleBindingEntity b : roleBindings) {
            if(!existRoleNames.contains(b.getRoleName())) {
                existRoleNames.add(b.getRoleName());
            }
        }
        
        return existRoleNames;
        
    }
}
