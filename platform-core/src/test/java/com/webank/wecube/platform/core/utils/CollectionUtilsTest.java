package com.webank.wecube.platform.core.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class CollectionUtilsTest {

    @Test
    public void testListMinus() {
        List<String> minuend = Arrays.asList(new String[]{"1", "2", "3", "6", "8"});
        List<String> extraction = Arrays.asList(new String[]{"6", "8", "9"});
        
        List<String> results = CollectionUtils.listMinus(minuend, extraction);
        
        Assert.assertEquals(3, results.size());
                
    }

}
