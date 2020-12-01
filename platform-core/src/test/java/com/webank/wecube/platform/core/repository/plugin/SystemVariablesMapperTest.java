package com.webank.wecube.platform.core.repository.plugin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.entity.plugin.SystemVariablesExample;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemVariablesMapperTest {
    
    @Autowired
    SystemVariablesMapper mapper;

    @Test
    public void testCountByExample() {
        SystemVariablesExample example = new SystemVariablesExample();
        int count = mapper.countByExample(example);
        System.out.println(count);
    }

    @Test
    public void testSelectByExample() {
    }

}
