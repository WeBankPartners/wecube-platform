package com.webank.wecube.platform.core.jpa.workflow;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcDefInfoMapperTest {
    @Autowired
    ProcDefInfoMapper procDefInfoMapper;
    @Autowired
    ServiceNodeStatusMapper serviceNodeStatusMapper;
    
    @Test
    public void testInsert(){
        String id = "test111";
        ProcDefInfoEntity entity = new ProcDefInfoEntity();
        entity.setId(id);
        entity.setProcDefData("aaaaaa");
        entity.setCreatedTime(new Date());
        entity.setIsDeleted(false);
        
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
    }

    @Test
    public void testFindAllDeployedProcDefsByProcDefName() {
    }

    @Test
    public void testFindAllDeployedProcDefsByProcDefKey() {
    }

}
