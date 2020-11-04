package com.webank.wecube.platform.core.jpa.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowMybatisMapperTest {
    @Autowired
    ProcDefInfoMapper procDefInfoMapper;
    @Autowired
    ServiceNodeStatusMapper serviceNodeStatusMapper;
    
    @Autowired
    ProcInstInfoMapper procInstInfoMapper;
    
    @Test
    public void testFindByProcDefIdIn() {
        List<String> procDefIds = new ArrayList<>();
        procDefIds.add("s9oR3S4l2Bq");
        procDefIds.add("s9oRktFl2Bz");
        List<ProcInstInfoEntity> entities = procInstInfoMapper.findByProcDefIdIn(procDefIds);
        
        System.out.println(entities.size());
    }
    
    @Test
    public void testInsert(){
        String id = LocalIdGenerator.generateId();
        ProcDefInfoEntity entity = new ProcDefInfoEntity();
        entity.setId(id);
        entity.setProcDefData("proc def data");
        entity.setCreatedTime(new Date());
        entity.setIsDeleted(false);
        
        entity.setActive(true);
        entity.setProcDefVer(8);
        entity.setRev(1);
        entity.setProcDefKey("proc def key");
        entity.setProcDefName("proc-def-name-test");
        entity.setStatus(ProcDefInfoEntity.DEPLOYED_STATUS);
        
        procDefInfoMapper.insert(entity);
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        String id = "rWMKoelC2BR";
        ProcDefInfoEntity entity = procDefInfoMapper.selectByPrimaryKey(id);
        
        System.out.println(entity.getProcDefName());
        System.out.println(entity.getIsDeleted());
        System.out.println(entity.getActive());
        
        ServiceNodeStatusEntity e = serviceNodeStatusMapper.selectByPrimaryKey("rWMRjYZC2FW");
        System.out.println(e.getNodeName());
    }

    @Test
    public void testFindAllDeployedOrDraftProcDefs() {
        List<ProcDefInfoEntity> entities = procDefInfoMapper.findAllDeployedOrDraftProcDefs();
        System.out.println(entities.size());
    }

    @Test
    public void testFindAllDeployedProcDefsByProcDefName() {
        String procDefName = "默认告警处理编排_V0.1";
        List<ProcDefInfoEntity> entities = procDefInfoMapper.findAllDeployedProcDefsByProcDefName(procDefName);
        
        System.out.println(entities.size());
    }

    @Test
    public void testFindAllDeployedProcDefsByProcDefKey() {
        String procDefKey = "wecube1584968688912";
        String status = ProcDefInfoEntity.DEPLOYED_STATUS;
        List<ProcDefInfoEntity> entities = procDefInfoMapper.findAllDeployedProcDefsByProcDefKey(procDefKey, status);
        System.out.println(entities.size());
    }

}
