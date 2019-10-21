package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.entity.workflow.ProcessDefInfoEntity;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessDefInfoRepositoryTest {
    
    Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    ProcessDefInfoRepository repo;

    @Test
    public void testFindAll() {
        List<ProcessDefInfoEntity> entities = repo.findAll();
        
        entities.forEach(e -> {
            log.info("entity:id={},data={}",e.getId(), e.getProcDefData());
        });
    }

    @Test
    public void testGetOne() {
//        fail("Not yet implemented");
    }

}
