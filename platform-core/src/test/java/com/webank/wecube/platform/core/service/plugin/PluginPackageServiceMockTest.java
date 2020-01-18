package com.webank.wecube.platform.core.service.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PluginPackageServiceMockTest {
    @Mock
    private UserManagementService userManagementService;
    @InjectMocks
    private PluginPackageService pluginPackageService = new PluginPackageService();

//    @Test
//    public void givenRoleInPluginPackageAuthorityNotExistInUserManagementServiceWhenRegisterThenCreateRoleShouldBeCalled() {
//        RoleDto roleDto = new RoleDto("WeCube Admin", "wecube_admin");
//        when(userManagementService.retrieveRole()).thenReturn(Lists.newArrayList(roleDto));
//        PluginPackage pluginPackage = new PluginPackage("ITSM", "v1.0");
//        pluginPackage.initId();
//        PluginPackageAuthority authority = new PluginPackageAuthority(null, pluginPackage, "ITSM_OPERATOR", "JOB");
//        authority.initId();
//        pluginPackage.setPluginPackageAuthorities(Sets.newHashSet(authority));
//
//        pluginPackageService.createRolesIfNotExistInSystem(pluginPackage);
//        verify(userManagementService, times(1)).createRole(any());
//    }

//    @Test
//    public void givenRoleInPluginPackageAuthorityExistInUserManagementServiceWhenRegisterThenCreateRoleNotCalled() {
//        RoleDto roleDto = new RoleDto("WeCube Admin", "wecube_admin");
//        RoleDto roleDto2 = new RoleDto("ITSM Operator", "ITSM_OPERATOR");
//        when(userManagementService.retrieveRole()).thenReturn(Lists.newArrayList(roleDto, roleDto2));
//        PluginPackage pluginPackage = new PluginPackage("ITSM", "v1.0");
//        pluginPackage.initId();
//        PluginPackageAuthority authority = new PluginPackageAuthority(null, pluginPackage, "ITSM_OPERATOR", "JOB");
//        authority.initId();
//        pluginPackage.setPluginPackageAuthorities(Sets.newHashSet(authority));
//
//        pluginPackageService.createRolesIfNotExistInSystem(pluginPackage);
//        verify(userManagementService, never()).createRole(any());
//    }

}