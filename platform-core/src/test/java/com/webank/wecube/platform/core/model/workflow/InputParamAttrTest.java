package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;

import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Test;


public class InputParamAttrTest {
    
    @Test
    public void testGetExpectedValueWithEmptyElement(){
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(InputParamAttr.DATA_TYPE_STRING);
        
        attr.addValues(new ArrayList<Object>());
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("", actualResult);
    }

    @Test
    public void testGetExpectedValueWithSingleElement() {
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(InputParamAttr.DATA_TYPE_STRING);
        
        attr.addValues(Arrays.asList(new String[]{"A"}));
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("A", actualResult);
    }
    
    
    @Test
    public void testGetExpectedValueWithMultiElements() {
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(InputParamAttr.DATA_TYPE_STRING);
        
        attr.addValues(Arrays.asList(new String[]{"A","B"}));
        attr.addValueObjects((String)null);
        attr.addValueObjects("C");
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("[A,B,,C]", actualResult);
    }

}
