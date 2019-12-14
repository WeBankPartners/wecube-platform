package com.webank.wecube.platform.core.service.workflow;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleResponseDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
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
    private ProcRoleBindingRepository procRoleBindingRepository;


    @Autowired
    public ProcessRoleServiceImpl(ProcRoleBindingRepository procRoleBindingRepository) {
        this.procRoleBindingRepository = procRoleBindingRepository;
    }

    @Override
    public ProcRoleResponseDto retrieveRoleIdByProcId(String procId) {
        ProcRoleResponseDto result = new ProcRoleResponseDto();
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

    public static ProcRoleBindingEntity.permissionEnum transferPermissionStrToEnum(String permissionStr) throws WecubeCoreException {
        ProcRoleBindingEntity.permissionEnum permissionEnum;
        try {
            permissionEnum = ProcRoleBindingEntity.permissionEnum.valueOf(ProcRoleBindingEntity.permissionEnum.class,
                    Objects.requireNonNull(permissionStr, "Permission string cannot be NULL"));
        } catch (IllegalArgumentException ex) {
            String msg = String.format("The given permission string [%s] doesn't match platform-core's match cases.", permissionStr);
            throw new WecubeCoreException(msg);
        }
        return permissionEnum;
    }


    @Override
    public List<ProcRoleDto> retrieveProcessByRoleIdList(List<Long> roleIdList, ProcRoleBindingEntity.permissionEnum permissionEnum) {
        List<ProcRoleDto> result = new ArrayList<>();
        for (Long roleId : roleIdList) {
            logger.info(String.format("Finding process to role binding infomation from roleId: [%s] and permission: [%s]", roleId, permissionEnum.toString()));
            List<ProcRoleBindingEntity> allByRoleIdAndPermission = this.procRoleBindingRepository.findAllByRoleIdAndPermission(roleId, permissionEnum);
            for (ProcRoleBindingEntity procRoleBindingEntity : allByRoleIdAndPermission) {
                result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
            }
        }
        return result;
    }

    @Override
    public List<ProcRoleDto> updateProcRoleBinding(String procId, Map<String, List<Long>> permissionRoleMap) throws WecubeCoreException {
        List<ProcRoleBindingEntity> allByProcId = this.procRoleBindingRepository.findAllByProcId(procId);
        // classify data into MGMT and USE classes
        List<ProcRoleBindingEntity> mgmtList = new ArrayList<>();
        List<ProcRoleBindingEntity> useList = new ArrayList<>();
        allByProcId.forEach(procRoleBindingEntity -> {
            switch (procRoleBindingEntity.getPermission()) {
                case MGMT:
                    mgmtList.add(procRoleBindingEntity);
                    break;
                case USE:
                    useList.add(procRoleBindingEntity);
                    break;
                default:
                    break;
            }
        });

        // classify permissionRoleMap data into MGMT and USE lists, which is consist of roleIds
        List<Long> mgmtRoleIdList = new ArrayList<>();
        List<Long> useRoleIdList = new ArrayList<>();
        permissionRoleMap.forEach((permissionStr, roleIdList) -> {
            ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permissionStr);
            switch (permissionEnum) {
                case MGMT:
                    mgmtRoleIdList.addAll(roleIdList);
                    break;
                case USE:
                    useRoleIdList.addAll(roleIdList);
                    break;
                default:
                    break;
            }
        });
        // update data into db
        List<ProcRoleBindingEntity> mgmtUpdated = updateData(mgmtList, mgmtRoleIdList);
        List<ProcRoleBindingEntity> useUpdated = updateData(useList, useRoleIdList);

        // arrange the result
        List<ProcRoleDto> result = new ArrayList<>();
        for (ProcRoleBindingEntity procRoleBindingEntity : mgmtUpdated) {
            result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
        }
        for (ProcRoleBindingEntity procRoleBindingEntity : useUpdated) {
            result.add(ProcRoleDto.fromDomain(procRoleBindingEntity));
        }
        return result;
    }

    @Override
    public void deleteProcRoleBinding(String procId, Long roleId, String permission) throws WecubeCoreException {
        ProcRoleBindingEntity.permissionEnum permissionEnum = transferPermissionStrToEnum(permission);

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

        Optional<ProcRoleBindingEntity> foundData = this.procRoleBindingRepository.findByProcIdAndRoleIdAndPermission(procId, roleId, permissionEnum);
        foundData.ifPresent(procRoleBindingEntity -> this.procRoleBindingRepository.delete(procRoleBindingEntity));
    }

    private List<ProcRoleBindingEntity> updateData(List<ProcRoleBindingEntity> dataList, List<Long> roleIdList) {
        ProcRoleBindingEntity.permissionEnum permission = dataList.get(0).getPermission();
        String procId = dataList.get(0).getProcId();

        if (permission.equals(ProcRoleBindingEntity.permissionEnum.MGMT) && roleIdList.isEmpty()) {
            String msg = "The process's management permission should have at least one role.";
            logger.info(String.format("The DELETE management roles operation was blocked, the process id is [%s].", procId));
            throw new WecubeCoreException(msg);
        }

        List<ProcRoleBindingEntity> result = new ArrayList<>();
        // current menuCodeList - new menuCodeList = needToDeleteList
        List<ProcRoleBindingEntity> needToDeleteList = dataList.stream().filter(procRoleBindingEntity -> {
            Long roleId = procRoleBindingEntity.getRoleId();
            return !roleIdList.contains(roleId);
        }).collect(Collectors.toList());
        if (!needToDeleteList.isEmpty()) {
            logger.info(String.format("Deleting ProcRoleBindings: [%s]", needToDeleteList));
            for (ProcRoleBindingEntity procRoleBindingEntity : needToDeleteList) {
                this.procRoleBindingRepository.delete(procRoleBindingEntity);
            }
        }

        // new menuCodeList - current menuCodeList = needToCreateList
        List<Long> needToCreateList;
        List<Long> storedRoleIdList = dataList.stream().map(ProcRoleBindingEntity::getRoleId).collect(Collectors.toList());
        needToCreateList = roleIdList.stream().filter(roleId -> !storedRoleIdList.contains(roleId)).collect(Collectors.toList());
        if (!needToCreateList.isEmpty()) {
            logger.info(String.format("Creating menus: [%s]", needToCreateList));
            List<ProcRoleBindingEntity> batchUpdateList = new ArrayList<>();
            needToCreateList.forEach(roleId -> batchUpdateList.add(new ProcRoleBindingEntity(procId, permission, roleId)));
            try {
                this.procRoleBindingRepository.saveAll(batchUpdateList);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw new WecubeCoreException(ex.getMessage());
            }
        }
        return result;
    }
}
