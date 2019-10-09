package com.webank.wecube.core.controller;

import com.webank.wecube.core.service.PluginModelServiceTest;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static com.webank.wecube.core.domain.MenuItem.MENU_COLLABORATION_PLUGIN_MANAGEMENT;
import static com.webank.wecube.core.domain.MenuItem.ROLE_PREFIX;

@WithMockUser(username = "test", authorities = {ROLE_PREFIX + MENU_COLLABORATION_PLUGIN_MANAGEMENT})
public class PluginModelControllerTest extends AbstractControllerTest {
    @Test
    public void getOverview() {

    }
}
