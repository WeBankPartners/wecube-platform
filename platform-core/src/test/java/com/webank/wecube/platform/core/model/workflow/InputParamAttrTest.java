package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;

import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

import com.webank.wecube.platform.core.utils.Constants;


public class InputParamAttrTest {
    
    @Test
    public void testGetExpectedValueWithEmptyElement(){
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(Constants.DATA_TYPE_STRING);
        
        attr.addValues(new ArrayList<Object>());
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("", actualResult);
    }

    @Test
    public void testGetExpectedValueWithSingleElement() {
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(Constants.DATA_TYPE_STRING);
        
        attr.addValues(Arrays.asList(new String[]{"A"}));
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("A", actualResult);
    }
    
    
    @Test
    public void testGetExpectedValueWithMultiElements() {
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(Constants.DATA_TYPE_STRING);
        
        attr.addValues(Arrays.asList(new String[]{"A","B"}));
        attr.addValueObjects((String)null);
        attr.addValueObjects("C");
        
        Object actualResult = attr.getExpectedValue();
        
        Assert.assertEquals("[A,B,,C]", actualResult);
    }
    
    @Test
    public void testGetValuesAsStringWithEmptyElement(){
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(Constants.DATA_TYPE_STRING);
        
        attr.addValues(Arrays.asList(new String[]{"A","B"}));
        attr.addValueObjects((String)null);
        attr.addValueObjects("C");
        
        String actual = attr.getValuesAsString();
        Assert.assertEquals("A,B,,C,", actual);
    }
    
    @Test
    public void testGetValuesAsStringWithEmptyNumberElement(){
        InputParamAttr attr = new InputParamAttr();
        attr.setMapType("entity");
        attr.setName("testAttr");
        attr.setType(Constants.DATA_TYPE_NUMBER);
        
        attr.addValues(Arrays.asList(new int[]{1,2}));
        attr.addValueObjects((Integer)null);
        attr.addValueObjects(9);
        
        String actual = attr.getValuesAsString();
        Assert.assertEquals("1,2,0,9,", actual);
    }

}
