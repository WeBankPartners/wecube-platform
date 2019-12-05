package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class UserManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    UserManagementServiceImpl userManagementService;
    String createUserJsonString = "{\"password\":\"howehowe\",\"userName\":\"howe\"}";
    String createRoleJsonString = "{\"displayName\":\"fake administrator\",\"name\":\"fakeAdministrator\"}";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;
    private String gatewayUrl;
    private MockRestServiceServer server;

    @Before
    public void setup() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
        gatewayUrl = this.applicationProperties.getGatewayUrl();
    }

    @Test
    public void createUserShouldSucceed() {
        mockCreateUserServer(this.server);
        Map<String, Object> createUserMap = mockCreateUserMap();
        userManagementService.createUser(createUserMap);
        this.server.verify();
    }

    @Test
    public void retrieveUserShouldSucceed() {
        mockRetrieveUserServer(this.server);
        userManagementService.retrieveUser();
        this.server.verify();
    }

    @Test
    public void deleteUserShouldSucceed() {
        mockDeleteUserServer(this.server);
        Long userId = 1L;
        userManagementService.deleteUser(userId);
        this.server.verify();
    }

    @Test
    public void createRoleShouldSucceed() {
        mockCreateRoleServer(this.server);
        Map<String, Object> createUserMap = mockCreateRoleMap();
        userManagementService.createRole(createUserMap);
        this.server.verify();
    }


    @Test
    public void retrieveRoleShouldSucceed() {
        mockRetrieveRoleServer(this.server);
        userManagementService.retrieveRole();
        this.server.verify();
    }

    @Test
    public void deleteRoleShouldSucceed() {
        mockDeleteRoleServer(this.server);
        Long userId = 1L;
        userManagementService.deleteRole(userId);
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
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/create", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/retrieve", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
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
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/users/1/delete", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }


    private void mockCreateRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/create", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(createRoleJsonString))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": {\n" +
                        "    \"createdBy\": null,\n" +
                        "    \"updatedBy\": null,\n" +
                        "    \"createdTime\": \"2019-12-04T06:37:39.805+0000\",\n" +
                        "    \"updatedTime\": null,\n" +
                        "    \"id\": 1,\n" +
                        "    \"name\": \"fakeAdministrator\",\n" +
                        "    \"displayName\": \"fake administrator\"\n" +
                        "  }\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockRetrieveRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/retrieve", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"createdBy\": null,\n" +
                        "      \"updatedBy\": null,\n" +
                        "      \"createdTime\": \"2019-12-04T06:37:40.000+0000\",\n" +
                        "      \"updatedTime\": null,\n" +
                        "      \"id\": 1,\n" +
                        "      \"name\": \"fakeAdministrator\",\n" +
                        "      \"displayName\": \"fake administrator\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", MediaType.APPLICATION_JSON));

    }

    private void mockDeleteRoleServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/auth/v1/roles/1/delete", this.gatewayUrl)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\n" +
                        "  \"status\": \"OK\",\n" +
                        "  \"message\": \"success\",\n" +
                        "  \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));

    }
}
