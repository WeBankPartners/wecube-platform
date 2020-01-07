package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class UserManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    UserManagementServiceImpl userManagementService;
    @Autowired
    @Qualifier(value = "userJwtSsoTokenRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;
    private String gatewayUrl;
    private MockRestServiceServer server;
    String token = "Bearer";

    @Before
    public void setup() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
        gatewayUrl = this.applicationProperties.getGatewayUrl();
    }

    @Test
    public void createUserShouldSucceed() {
        mockCreateUserServer(this.server);
        Map<String, Object> createUserMap = mockCreateUserMap();
        CommonResponseDto responseDto = userManagementService.createUser(this.token, createUserMap);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void retrieveUserShouldSucceed() {
        mockRetrieveUserServer(this.server);
        CommonResponseDto responseDto = userManagementService.retrieveUser(this.token);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void deleteUserShouldSucceed() {
        mockDeleteUserServer(this.server);
        Long userId = 1L;
        CommonResponseDto responseDto = userManagementService.deleteUser(this.token, userId);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void createRoleShouldSucceed() {
        mockCreateRoleServer(this.server);
        Map<String, Object> createUserMap = mockCreateRoleMap();
        CommonResponseDto responseDto = userManagementService.createRole(this.token, createUserMap);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void createRoleInternalShouldSucceed() {
        mockCreateRoleServer(this.server);
        RoleDto createRoleDto = new RoleDto("fake administrator", "fakeAdministrator");
        userManagementService.createRole(createRoleDto);
        this.server.verify();
    }


    @Test
    public void retrieveRoleShouldSucceed() {
        mockRetrieveRoleServer(this.server);
        CommonResponseDto responseDto = userManagementService.retrieveRole(this.token);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void retrieveRoleInternalShouldSucceed() {
        Integer RETRIEVE_ROLE_SIZE = 1;
        mockRetrieveRoleServer(this.server);
        List<RoleDto> retrieveRoleResultList = userManagementService.retrieveRole();
        assertThat(retrieveRoleResultList.size()).isEqualTo(RETRIEVE_ROLE_SIZE);
        assertThat(retrieveRoleResultList.get(0).getId()).isEqualTo("1");
        this.server.verify();
    }

    @Test
    public void deleteRoleShouldSucceed() {
        mockDeleteRoleServer(this.server);
        String roleId = "1";
        CommonResponseDto responseDto = userManagementService.deleteRole(this.token, roleId);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void deleteRoleInternalShouldSucceed() {
        mockDeleteRoleServer(this.server);
        String roleId = "1";
        String token = "Bearer";
        try {
            userManagementService.deleteRole(token, roleId);
        } catch (WecubeCoreException e) {
            fail(e.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void getRolesFromUserShouldSucceed() {
        mockGetRolesFromUserServer(this.server);
        String userName = "howe";
        CommonResponseDto responseDto = userManagementService.getRolesByUserName(this.token, userName);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void getUsersFromRoleShouldSucceed() {
        mockGetUsersFromRoleServer(this.server);
        String roleId = "1";
        CommonResponseDto responseDto = userManagementService.getUsersByRoleId(this.token, roleId);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }

    @Test
    public void grantRoleForUserShouldSucceed() {
        mockGrantRoleToUsersServer(this.server);
        String roleId = "2";
        List<Object> userIdList = Collections.singletonList(2);
        CommonResponseDto responseDto = userManagementService.grantRoleToUsers(this.token, roleId, userIdList);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }


    @Test
    public void revokeRoleFromUserShouldSucceed() {
        mockRevokeRoleFromUsers(this.server);
        String roleId = "2";
        List<Object> userIdList = Collections.singletonList(2);
        CommonResponseDto responseDto = userManagementService.revokeRoleFromUsers(this.token, roleId, userIdList);
        assertThat(responseDto.getStatus()).isEqualTo(CommonResponseDto.STATUS_OK);
        this.server.verify();
    }


    private Map<String, Object> mockCreateUserMap() {
        Map<String, Object> createUserMap = new LinkedHashMap<>();
        createUserMap.put("password", "howehowe");
        createUserMap.put("userName", "howe");
        return createUserMap;
    }

    private Map<String, Object> mockCreateRoleMap() {
        Map<String, Object> createRoleMap = new LinkedHashMap<>();
        createRoleMap.put("displayName", "fake administrator");
        createRoleMap.put("name", "fakeAdministrator");
        return createRoleMap;
    }


    private void mockCreateUserServer(MockRestServiceServer server) {
        String createUserJsonString = "{\"password\":\"howehowe\",\"userName\":\"howe\"}";
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", this.token))
                .andExpect(content().json(createUserJsonString))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": {\n" +
                        "    \"createdBy\": null,\n" +
                        "    \"updatedBy\": null,\n" +
                        "    \"createdTime\": \"2019-12-04T06:34:06.110+0000\",\n" +
                        "    \"updatedTime\": null,\n" +
                        "    \"id\": 1,\n" +
                        "    \"username\": \"howe\",\n" +
                        "    \"password\": \"$2a$10$4qmZg8NVAJZ0/4Oau6s9s.JY5rcaHj4RSi3AB5eKwSBV/Yr05vlau\",\n" +
                        "    \"active\": true,\n" +
                        "    \"blocked\": null\n" +
                        "  }\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockRetrieveUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:34:06.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": 1,\n" +
                        "      \"username\": \"howe\",\n" +
                        "      \"password\": \"$2a$10$4qmZg8NVAJZ0/4Oau6s9s.JY5rcaHj4RSi3AB5eKwSBV/Yr05vlau\",\n" +
                        "      \"active\": true,\n" +
                        "      \"blocked\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockDeleteUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/1", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }


    private void mockCreateRoleServer(MockRestServiceServer server) {
        String createRoleJsonString = "{\"displayName\":\"fake administrator\",\"name\":\"fakeAdministrator\"}";
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", this.token))
                .andExpect(content().json(createRoleJsonString))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": {\n" +
                        "    \"createdBy\": null,\n" +
                        "    \"updatedBy\": null,\n" +
                        "    \"createdTime\": \"2019-12-04T06:37:39.805+0000\",\n" +
                        "    \"updatedTime\": null,\n" +
                        "    \"id\": \"1\",\n" +
                        "    \"name\": \"fakeAdministrator\",\n" +
                        "    \"displayName\": \"fake administrator\"\n" +
                        "  }\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockRetrieveRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:37:40.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"name\": \"fakeAdministrator\",\n" +
                        "      \"displayName\": \"fake administrator\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockDeleteRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/1", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockGetRolesFromUserServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/howe/roles", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:56:39.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": \"1\",\n" +
                        "      \"name\": \"fakeAdministrator\",\n" +
                        "      \"displayName\": \"fake administrator\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockGetUsersFromRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/1/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", this.token))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:56:18.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": 1,\n" +
                        "      \"username\": \"howe\",\n" +
                        "      \"password\": \"$2a$10$4tn9dqctE6VcEMTVkyR/We4P6e4qZWz.Qt5qCbnE1tudPlACVQOsi\",\n" +
                        "      \"active\": true,\n" +
                        "      \"blocked\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockGrantRoleToUsersServer(MockRestServiceServer server) {
        String grantRoleToUserJsonString = "[2]";

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/2/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", this.token))
                .andExpect(content().string(grantRoleToUserJsonString))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockRevokeRoleFromUsers(MockRestServiceServer server) {
        String revokeRoleFromUserJsonString = "[2]";
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/2/users", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", this.token))
                .andExpect(content().string(revokeRoleFromUserJsonString))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

}
