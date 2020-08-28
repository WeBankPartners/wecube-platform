package com.webank.wecube.platform.core.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.support.RestClient;
import com.webank.wecube.platform.core.support.RestClientException;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClientProperties;

@Ignore
public class UserManagementServiceTest extends DatabaseBasedTest {

    // login user
    static final String TOKEN = "Bearer";
    static final String USERNAME = "user";
    // user info
    static final String MOCK_USERNAME = "user";
    static final String MOCK_PASSWORD = "password";
    static final String AS_RETURN_PASSWORD = "$2a$10$4qmZg8NVAJZ0/4Oau6s9s.JY5rcaHj4RSi3AB5eKwSBV/Yr05vlau";
    static final int AS_REGISTERED_USER_SIZE = 1;
    static final String USER_ID = "1";
    // role info
    final static String ROLE_DISPLAY_NAME = "fake administrator";
    final static String ROLE_NAME = "fakeAdministrator";
    final static String ROLE_EMAIL = "fakeAdministrator@webank.com";
    static final int AS_REGISTERED_ROLE_SIZE = 1;
    static final String ROLE_ID = "1";
    static final String GRANT_ROLE_ID = "2";

    @Autowired
    UserManagementServiceImpl userManagementService;
    @Autowired
    @Qualifier(value = "userJwtSsoTokenRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private AuthServerRestClientProperties authServerRestClientProperties;
    private String gatewayUrl;
    private MockRestServiceServer server;

    @Before
    public void setup() {
        mockUser();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        gatewayUrl = authServerRestClientProperties.getHost() + RestClient.URI_COMPONENTS_DELIMITER + authServerRestClientProperties.getPort();
    }

    @Test
    public void whenGivenUserInfo_registerUser_houldSucceed() {
        mockCreateUserServer(this.server);
        UserDto createUserMap = mockUserDto();
        UserDto result = null;
        try {
            result = userManagementService.registerUser(createUserMap);
        } catch (RestClientException ex) {
            fail(ex.getMessage());
        }
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(MOCK_USERNAME);
        assertThat(result.getPassword()).isEqualTo(AS_RETURN_PASSWORD);
        this.server.verify();
    }

    @Test
    public void whenRetrieveAllUserInfo_shouldSucceed() {
        mockRetrieveUserServer(this.server);
        final List<UserDto> userDtoList = userManagementService.retrieveAllUserAccounts();
        assertThat(userDtoList.size()).isEqualTo(AS_REGISTERED_USER_SIZE);
        assertThat(userDtoList.get(0).getUsername()).isEqualTo(USERNAME);
        assertThat(userDtoList.get(0).getPassword()).isEqualTo(AS_RETURN_PASSWORD);
        this.server.verify();
    }

    @Test
    public void whenGivenUserId_deleteUser_shouldSucceed() {
        mockDeleteUserServer(this.server);
        try {
            userManagementService.deleteUserByUserId(USER_ID);
        } catch (RestClientException ex) {
            fail(ex.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void whenGivenRoleInfo_registerLocalRole_shouldSucceed() {
        mockRegisterRoleServer(this.server);
        RoleDto registerRoleDto = mockRegisterLocalRoleDto();
        RoleDto asReturnRoleDto = null;
        try {
            asReturnRoleDto = userManagementService.registerLocalRole(registerRoleDto);
        } catch (WecubeCoreException ex) {
            fail(ex.getMessage());
        }
        assertThat(asReturnRoleDto).isNotNull();
        assertThat(asReturnRoleDto.getName()).isEqualTo(ROLE_NAME);
        assertThat(asReturnRoleDto.getDisplayName()).isEqualTo(ROLE_DISPLAY_NAME);
        assertThat(asReturnRoleDto.getEmail()).isEqualTo(ROLE_EMAIL);
        this.server.verify();
    }

    @Test
    public void whenRetrieveRoleByRoleId_shouldSucceed() {
        mockRetrieveRoleByIdServer(this.server);
        final RoleDto roleDto = userManagementService.retrieveRoleById(ROLE_ID);
        assertThat(roleDto.getName()).isEqualTo(ROLE_NAME);
        assertThat(roleDto.getDisplayName()).isEqualTo(ROLE_DISPLAY_NAME);
        assertThat(roleDto.getEmail()).isEqualTo(ROLE_EMAIL);
        this.server.verify();
    }

    @Test
    public void whenRetrieveAllRoles_shouldSucceed() {
        mockRetrieveRoleServer(this.server);
        final List<RoleDto> roleDtos = userManagementService.retrieveAllRoles();
        assertThat(roleDtos.size()).isEqualTo(AS_REGISTERED_ROLE_SIZE);
        assertThat(roleDtos.get(0).getName()).isEqualTo(ROLE_NAME);
        assertThat(roleDtos.get(0).getDisplayName()).isEqualTo(ROLE_DISPLAY_NAME);
        assertThat(roleDtos.get(0).getEmail()).isEqualTo(ROLE_EMAIL);
        this.server.verify();
    }

    @Test
    public void whenGivenRoleId_deleteRole_shouldSucceed() {
        mockDeleteRoleServer(this.server);
        try {
            userManagementService.unregisterLocalRoleById(ROLE_ID);
        } catch (WecubeCoreException ex) {
            fail(ex.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void whenGivenUsername_getGrantedRoles_shouldSucceed() {
        mockGetRolesFromUserServer(this.server);
        final List<RoleDto> grantedRolesByUsername = userManagementService.getGrantedRolesByUsername(USERNAME);
        assertThat(grantedRolesByUsername).isNotNull();
        assertThat(grantedRolesByUsername.size()).isEqualTo(AS_REGISTERED_ROLE_SIZE);
        assertThat(grantedRolesByUsername.get(0).getName()).isEqualTo(ROLE_NAME);
        assertThat(grantedRolesByUsername.get(0).getDisplayName()).isEqualTo(ROLE_DISPLAY_NAME);
        assertThat(grantedRolesByUsername.get(0).getEmail()).isEqualTo(ROLE_EMAIL);
        this.server.verify();
    }

    @Test
    public void whenGivenRoleId_getUsersFromRole_shouldSucceed() {
        mockGetUsersFromRoleServer(this.server);
        final List<UserDto> usersByRoleId = userManagementService.getUsersByRoleId(ROLE_ID);
        assertThat(usersByRoleId).isNotNull();
        assertThat(usersByRoleId.size()).isEqualTo(AS_REGISTERED_USER_SIZE);
        assertThat(usersByRoleId.get(0).getUsername()).isEqualTo(USERNAME);
        assertThat(usersByRoleId.get(0).getPassword()).isEqualTo(AS_RETURN_PASSWORD);
        this.server.verify();
    }

    @Test
    public void whenGivenUserIdAndRoleId_grantRoleToUser_houldSucceed() {
        mockGrantRoleToUsersServer(this.server);
        List<String> userIdList = Collections.singletonList(USER_ID);
        try {
            userManagementService.grantRoleToUsers(GRANT_ROLE_ID, userIdList);
        } catch (WecubeCoreException ex) {
            fail(ex.getMessage());
        }
        this.server.verify();
    }


    @Test
    public void whenGivenUserIdAndRoleId_revokeRoleFromUser_shouldSucceed() {
        mockRevokeRoleFromUsers(this.server);
        List<String> userIdList = Collections.singletonList(USER_ID);
        try {
            userManagementService.revokeRoleFromUsers(GRANT_ROLE_ID, userIdList);
        } catch (WecubeCoreException ex) {
            fail(ex.getMessage());
        }
        this.server.verify();
    }

    private void mockUser() {

        AuthenticationContextHolder.AuthenticatedUser user = new AuthenticationContextHolder.AuthenticatedUser(USERNAME, TOKEN);
        AuthenticationContextHolder.setAuthenticatedUser(user);
    }


    private UserDto mockUserDto() {
        UserDto userDto = new UserDto();
        userDto.setUsername(MOCK_USERNAME);
        userDto.setPassword(MOCK_PASSWORD);
        return userDto;
    }

    private RoleDto mockRegisterLocalRoleDto() {

        RoleDto roleDto = new RoleDto();
        roleDto.setDisplayName(ROLE_DISPLAY_NAME);
        roleDto.setName(ROLE_NAME);
        roleDto.setEmail(ROLE_EMAIL);
        return roleDto;
    }


    private void mockCreateUserServer(MockRestServiceServer server) {
        String createUserJsonString = String.format("{\"password\":\"%s\",\"username\":\"%s\"}", MOCK_PASSWORD, MOCK_USERNAME);
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", TOKEN))
                .andExpect(content().json(createUserJsonString))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": {\n" +
                        "    \"createdBy\": null,\n" +
                        "    \"updatedBy\": null,\n" +
                        "    \"createdTime\": \"2019-12-04T06:34:06.110+0000\",\n" +
                        "    \"updatedTime\": null,\n" +
                        "    \"id\": 1,\n" +
                        "    \"username\": \"%s\",\n" +
                        "    \"password\": \"%s\",\n" +
                        "    \"active\": true,\n" +
                        "    \"blocked\": null\n" +
                        "  }\n" +
                        "}", MOCK_USERNAME, AS_RETURN_PASSWORD), MediaType.APPLICATION_JSON));

    }

    private void mockRetrieveUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:34:06.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": 1,\n" +
                        "      \"username\": \"%s\",\n" +
                        "      \"password\": \"%s\",\n" +
                        "      \"active\": true,\n" +
                        "      \"blocked\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", USERNAME, AS_RETURN_PASSWORD), MediaType.APPLICATION_JSON));

    }

    private void mockDeleteUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/%s", this.gatewayUrl, USER_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }


    private void mockRegisterRoleServer(MockRestServiceServer server) {
        String createRoleJsonString = String.format("{\"displayName\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}", ROLE_DISPLAY_NAME, ROLE_NAME, ROLE_EMAIL);
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", TOKEN))
                .andExpect(content().json(createRoleJsonString))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": {\n" +
                        "    \"createdBy\": null,\n" +
                        "    \"updatedBy\": null,\n" +
                        "    \"createdTime\": \"2019-12-04T06:37:39.805+0000\",\n" +
                        "    \"updatedTime\": null,\n" +
                        "    \"id\": \"1\",\n" +
                        "    \"name\": \"%s\",\n" +
                        "    \"displayName\": \"%s\",\n" +
                        "    \"email\": \"%s\"\n" +
                        "  }\n" +
                        "}", ROLE_NAME, ROLE_DISPLAY_NAME, ROLE_EMAIL), MediaType.APPLICATION_JSON));
    }

    private void mockRetrieveRoleByIdServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/%s", this.gatewayUrl, ROLE_ID)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\":\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:37:40.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"name\": \"%s\",\n" +
                        "      \"displayName\": \"%s\",\n" +
                        "      \"email\": \"%s\"\n" +
                        "    }\n" +
                        "}", ROLE_NAME, ROLE_DISPLAY_NAME, ROLE_EMAIL), MediaType.APPLICATION_JSON));

    }

    private void mockRetrieveRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:37:40.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"name\": \"%s\",\n" +
                        "      \"displayName\": \"%s\",\n" +
                        "      \"email\": \"%s\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", ROLE_NAME, ROLE_DISPLAY_NAME, ROLE_EMAIL), MediaType.APPLICATION_JSON));

    }

    private void mockDeleteRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/1", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockGetRolesFromUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/%s/roles", this.gatewayUrl, USERNAME)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:56:39.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"name\": \"%s\",\n" +
                        "      \"displayName\": \"%s\",\n" +
                        "      \"email\": \"%s\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", ROLE_NAME, ROLE_DISPLAY_NAME, ROLE_EMAIL), MediaType.APPLICATION_JSON));

    }

    private void mockGetUsersFromRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/%s/users", this.gatewayUrl, ROLE_ID)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess(String.format("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:56:18.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": 1,\n" +
                        "      \"username\": \"%s\",\n" +
                        "      \"password\": \"%s\",\n" +
                        "      \"active\": true,\n" +
                        "      \"blocked\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", USERNAME, AS_RETURN_PASSWORD), MediaType.APPLICATION_JSON));

    }

    private void mockGrantRoleToUsersServer(MockRestServiceServer server) {

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/%s/users", this.gatewayUrl, GRANT_ROLE_ID)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockRevokeRoleFromUsers(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/%s/users/revoke", this.gatewayUrl, GRANT_ROLE_ID)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

}
