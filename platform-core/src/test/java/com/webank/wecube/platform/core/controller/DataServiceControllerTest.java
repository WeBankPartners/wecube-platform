package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.controller.plugin.DataServiceController;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataServiceControllerTest extends AbstractControllerTest {
    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;
    private String gatewayUrl;
    private MockRestServiceServer server;
    @Autowired
    private DataServiceController controllerToTest;

    @Before
    public void setup() {
        this.server = MockRestServiceServer.bindTo(jwtSsoRestTemplate).build();
        this.gatewayUrl = this.applicationProperties.getGatewayUrl();
        mvc = MockMvcBuilders.standaloneSetup(controllerToTest).build();
    }

    @Test
    public void createEntityShouldSucceed() {
        mockCreateEntityServer();
        try {
            mvc
                    .perform(post("/v1/packages/wecmdb/entities/system_design/create")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(mockCreateSystemDesignJsonString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.data[*].description", containsInAnyOrder("This is a create test")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to create target entity: " + e.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void retrieveEntityShouldSucceed() {
        int TARGET_ENTITY_SIZE = 4;

        mockRetrieveEntityServer();
        try {
            mvc.perform(get("/v1/packages/wecmdb/entities/system_design/retrieve").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(TARGET_ENTITY_SIZE))))
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("0001_0000000001", "0001_0000000002", "0001_0000000003", "0001_0000000004")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to fetch target entity: " + e.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void retrieveEntityWithRequestParamShouldSucceed() {
        int TARGET_ENTITY_SIZE = 1;

        mockRetrieveEntityWithRequestParamServer();
        try {
            mvc.perform(get("/v1/packages/wecmdb/entities/system_design/retrieve?filter=id,0001_0000000001").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(TARGET_ENTITY_SIZE))))
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("0001_0000000001")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to fetch target entity: " + e.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void updateEntityShouldSucceed() {
        final String UPDATED_ENTITY_CODE = "updated_system_design_code";
        mockUpdateEntityServer();
        try {
            mvc
                    .perform(post("/v1/packages/wecmdb/entities/system_design/update")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(mockUpdateSystemDesignJsonString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.data[*].code", containsInAnyOrder(UPDATED_ENTITY_CODE)))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to update target entity: " + e.getMessage());
        }
        this.server.verify();
    }

    @Test
    public void deleteEntityShouldSucceed() {
        mockDeleteEntityServer();
        try {
            mvc
                    .perform(post("/v1/packages/wecmdb/entities/system_design/delete")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(mockDeleteSystemDesignJsonString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.message", is("Success")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to delete target entity: " + e.getMessage());
        }
        this.server.verify();
    }


    private String mockCreateSystemDesignJsonString() {
        return "[\n" +
                "    {\n" +
                "        \"guid\": \"\",\n" +
                "        \"p_guid\": null,\n" +
                "        \"state\": \"\",\n" +
                "        \"code\": \"create_test_again\",\n" +
                "        \"name\": \"create_test_again\",\n" +
                "        \"business_group\": 105,\n" +
                "        \"fixed_date\": \"\",\n" +
                "        \"description\": \"This is a create test\"\n" +
                "    }\n" +
                "]";
    }

    private String mockUpdateSystemDesignJsonString() {
        return "[\n" +
                "    {\n" +
                "        \"id\": \"0001_0000000022\",\n" +
                "        \"code\": \"updated_system_design_code\"\n" +
                "    }\n" +
                "]";
    }

    private String mockDeleteSystemDesignJsonString() {
        return "[\n" +
                "    {\n" +
                "        \"id\": \"0001_0000000022\"\n" +
                "    }\n" +
                "]";
    }

    private void mockCreateEntityServer() {
        this.server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/create", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"create_test_again\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"create_test_again\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000022\",\n" +
                        "            \"name\": \"create_test_again\",\n" +
                        "            \"description\": \"This is a create test\",\n" +
                        "            \"id\": \"0001_0000000022\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockRetrieveEntityServer() {
        this.server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"EDP\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"EDP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000001\",\n" +
                        "            \"name\": \"Deposit Micro Core System\",\n" +
                        "            \"description\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"PEBS\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"PEBS\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000002\",\n" +
                        "            \"name\": \"Personal Online Bank System\",\n" +
                        "            \"description\": \"Personal Online Bank System\",\n" +
                        "            \"id\": \"0001_0000000002\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:16\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"ECIF\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"ECIF\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000003\",\n" +
                        "            \"name\": \"CRM System\",\n" +
                        "            \"description\": \"CRM System\",\n" +
                        "            \"id\": \"0001_0000000003\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:17\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"DEMO\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"DEMO\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000004\",\n" +
                        "            \"name\": \"Demo system\",\n" +
                        "            \"description\": \"Demo system\",\n" +
                        "            \"id\": \"0001_0000000004\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 21:18:02\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockRetrieveEntityWithRequestParamServer() {
        this.server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design?filter=id,0001_0000000001", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"EDP\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"EDP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000001\",\n" +
                        "            \"name\": \"Deposit Micro Core System\",\n" +
                        "            \"description\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }


    private void mockUpdateEntityServer() {
        // mockFwdNodeExpression
        this.server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/update", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"updated_system_design_code\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"updated_system_design_code\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000022\",\n" +
                        "            \"name\": \"create_test_again\",\n" +
                        "            \"description\": \"This is a create test\",\n" +
                        "            \"id\": \"0001_0000000022\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockDeleteEntityServer() {
        // mockFwdNodeExpression
        this.server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/delete", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": null\n" +
                        "}", MediaType.APPLICATION_JSON));
    }
}
