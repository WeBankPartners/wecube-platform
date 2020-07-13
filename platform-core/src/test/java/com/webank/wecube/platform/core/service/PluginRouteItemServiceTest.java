package com.webank.wecube.platform.core.service;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.PluginRouteItemDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginRouteItemServiceTest {
	
	@Autowired
	private PluginRouteItemService pluginRouteItemService;

	@Ignore
	@Test
	public void testGetAllPluginRouteItems() {
		List<PluginRouteItemDto> results = pluginRouteItemService.getAllPluginRouteItems();
		System.out.println(results.size());
	}

}
