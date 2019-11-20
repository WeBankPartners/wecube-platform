package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataModelExpressionControllerTest extends AbstractControllerTest {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;
    private String gatewayUrl;
    private MockRestServiceServer server;

    @Before
    public void setup() {
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
        this.gatewayUrl = this.applicationProperties.getGatewayUrl();
    }

    @Test
    public void targetEntityShouldSucceed() {
        int TARGET_ENTITY_SIZE = 4;

        mockTargetEntityQueryServer();
        try {
            mvc.perform(get("/v1/dme/target-entity?package=wecmdb&entity=system_design").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(iterableWithSize(TARGET_ENTITY_SIZE))))
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("0001_0000000001", "0001_0000000002", "0001_0000000003", "0001_0000000004")))
                    .andDo(print())
                    .andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to fetch target entity: " + e.getMessage());
        }
        this.server.verify();
    }

    private void mockTargetEntityQueryServer() {
        // mockFwdNodeExpression
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
}
