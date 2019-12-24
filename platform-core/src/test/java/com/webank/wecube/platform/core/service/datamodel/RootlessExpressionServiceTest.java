package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.BaseSpringBootTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.DmeFilterDto;
import com.webank.wecube.platform.core.dto.DmeLinkFilterDto;
import com.webank.wecube.platform.core.dto.Filter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RootlessExpressionServiceTest extends BaseSpringBootTest {


    @Autowired
    RootlessExpressionService rootlessExpressionService;
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
    public void givenPackageNameWithDashAndFwdNodeExpressionWhenFetchThenShouldSucceed() {
        mockPackageNameWithDashAndFwdNodeExpressionServer(server);

        List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto("wecmdb:system_design.code", Collections.singletonList(new DmeLinkFilterDto())));
        assert resultOne.get(0).equals("DEMO1");
        assert resultOne.get(1).equals("DEMO2");
        assert resultOne.get(2).equals("WECUBE");

        List<Object> resultTwo = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto("wecmdb:unit.key_name", Collections.singletonList(new DmeLinkFilterDto())));
        assert resultTwo.get(0).equals("DEMO1_PRD_ADM_APP");
        assert resultTwo.get(1).equals("WECUBE_PRD_CORE_APP");

        server.verify();
    }

    @Test
    public void givenPackageNameWithDashAndFwdNodeExpressionWithFilterWhenFetchThenShouldSucceed() {
        mockPackageNameWithDashAndFwdNodeExpressionServer(server);


        List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto(
                "wecmdb:system_design.code",
                Collections.singletonList(new DmeLinkFilterDto(
                        0,
                        "wecmdb",
                        "system_design",
                        Collections.singletonList(new Filter(
                                "key_name",
                                "eq",
                                "DEMO1"))))));
        assertThat(resultOne.size()).isEqualTo(1);
        assertThat(resultOne.get(0)).isEqualTo("DEMO1");

        List<Object> resultTwo = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto(
                "wecmdb:unit.key_name", Collections.singletonList(new DmeLinkFilterDto(
                0,
                "wecmdb",
                "unit",
                Collections.singletonList(new Filter(
                        "code",
                        "eq",
                        "APP"))))));
        assertThat(resultTwo.size()).isEqualTo(2);
        assertThat(resultTwo.get(0)).isEqualTo("DEMO1_PRD_ADM_APP");
        assertThat(resultTwo.get(1)).isEqualTo("WECUBE_PRD_CORE_APP");

        server.verify();
    }


//    @Test
//    public void wecmdbMultipleLinksWithMixedOpExpressionFetchShouldSucceed() {
//        mockMultipleLinksWithMixedOpExpressionServer(server);
//        List<Object> resultOne = dataModelExpressionService.fetchData(
//                new DataModelExpressionToRootData("wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name", "0007_0000000001"));
//
//        assert resultOne.size() == 2;
//        assert resultOne.get(0).equals("ECIF-CORE");
//        assert resultOne.get(1).equals("ECIF-CORE");
//
//        List<Object> resultTwo = dataModelExpressionService.fetchData(
//                new DataModelExpressionToRootData("wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone.key_name", "0023_0000000004"));
//        assert resultTwo.size() == 2;
//        assert resultTwo.get(0).equals("PRD-GZ1-MGMT");
//        assert resultTwo.get(1).equals("PRD-GZ1-PARTNERNET");
//
//        server.verify();
//    }

    private void mockPackageNameWithDashAndFwdNodeExpressionServer(MockRestServiceServer server) {
        // mockFwdNodeExpression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"key_name\": \"DEMO1\",\n" +
                        "            \"p_guid\": null,\n" +
                        "            \"business_group\": \"business_group_A\",\n" +
                        "            \"code\": \"DEMO1\",\n" +
                        "            \"r_guid\": \"0001_0000000001\",\n" +
                        "            \"name\": \"演示系统1\",\n" +
                        "            \"description\": \"1\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": \"new\",\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"key_name\": \"DEMO2\",\n" +
                        "            \"p_guid\": null,\n" +
                        "            \"business_group\": \"business_group_B\",\n" +
                        "            \"code\": \"DEMO2\",\n" +
                        "            \"r_guid\": \"0001_0000000002\",\n" +
                        "            \"name\": \"演示系统2\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"id\": \"0001_0000000002\",\n" +
                        "            \"state\": \"new\",\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"key_name\": \"WECUBE\",\n" +
                        "            \"p_guid\": null,\n" +
                        "            \"business_group\": \"business_group_mt\",\n" +
                        "            \"code\": \"WECUBE\",\n" +
                        "            \"r_guid\": \"0001_0000000003\",\n" +
                        "            \"name\": \"WeCube\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"id\": \"0001_0000000003\",\n" +
                        "            \"state\": \"new\",\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"deploy_package\": [],\n" +
                        "            \"manager\": \"\",\n" +
                        "            \"r_guid\": \"0009_0000000008\",\n" +
                        "            \"security_group_asset_code\": \"\",\n" +
                        "            \"monitor_port\": \"20008\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"resource_set\": [\n" +
                        "                \"0022_0000000001\",\n" +
                        "                \"0022_0000000027\"\n" +
                        "            ],\n" +
                        "            \"unit_type\": [\n" +
                        "                \"tomcat8\"\n" +
                        "            ],\n" +
                        "            \"key_name\": \"DEMO1_PRD_ADM_APP\",\n" +
                        "            \"subsys\": \"0008_0000000007\",\n" +
                        "            \"p_guid\": null,\n" +
                        "            \"port\": \"22099\",\n" +
                        "            \"id\": \"0009_0000000008\",\n" +
                        "            \"state\": \"created\",\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000016\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"deploy_package\": [],\n" +
                        "            \"manager\": \"\",\n" +
                        "            \"r_guid\": \"0009_0000000009\",\n" +
                        "            \"security_group_asset_code\": \"\",\n" +
                        "            \"monitor_port\": \"20008\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"resource_set\": [\n" +
                        "                \"0022_0000000019\"\n" +
                        "            ],\n" +
                        "            \"unit_type\": [\n" +
                        "                \"tomcat8\"\n" +
                        "            ],\n" +
                        "            \"key_name\": \"WECUBE_PRD_CORE_APP\",\n" +
                        "            \"subsys\": \"0008_0000000009\",\n" +
                        "            \"p_guid\": null,\n" +
                        "            \"port\": \"8080\",\n" +
                        "            \"id\": \"0009_0000000009\",\n" +
                        "            \"state\": \"created\",\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000020\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockMultipleLinksWithMixedOpExpressionServer(MockRestServiceServer server) {
        // first request
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit?filter=subsys,0007_0000000001", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"package\": \"\",\n" +
                        "            \"r_guid\": \"0008_0000000001\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:35\",\n" +
                        "            \"unit_design\": \"0003_0000000006\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": \"\",\n" +
                        "            \"code\": \"DB\",\n" +
                        "            \"orchestration\": 231,\n" +
                        "            \"package\": \"0011_0000000010\",\n" +
                        "            \"r_guid\": \"0008_0000000007\",\n" +
                        "            \"description\": \"aa\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000007\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000007\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit_design?filter=id,0003_0000000006", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0003_0000000006\",\n" +
                        "            \"description\": \"Application module\",\n" +
                        "            \"resource_set_design\": null,\n" +
                        "            \"across_idc\": 147,\n" +
                        "            \"type\": 106,\n" +
                        "            \"key_name\": \"ECIF-CORE-APP\",\n" +
                        "            \"resource_set_design_type\": 136,\n" +
                        "            \"name\": \"Application module\",\n" +
                        "            \"id\": \"0003_0000000006\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:29:05\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit_design?filter=id,0003_0000000007", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"code\": \"DB\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0003_0000000007\",\n" +
                        "            \"description\": \"DB Module\",\n" +
                        "            \"resource_set_design\": null,\n" +
                        "            \"across_idc\": 147,\n" +
                        "            \"type\": 107,\n" +
                        "            \"key_name\": \"ECIF-CORE-DB\",\n" +
                        "            \"resource_set_design_type\": 137,\n" +
                        "            \"name\": \"DB Module\",\n" +
                        "            \"id\": \"0003_0000000007\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:00\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys_design?filter=id,0002_0000000010", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0002_0000000010\",\n" +
                        "            \"description\": \"CRM Core Subsystem\",\n" +
                        "            \"dcn_design_type\": 135,\n" +
                        "            \"key_name\": \"ECIF-CORE\",\n" +
                        "            \"name\": \"CRM Core Subsystem\",\n" +
                        "            \"id\": \"0002_0000000010\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:28:27\",\n" +
                        "            \"system_design\": \"0001_0000000003\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        // second request
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link_design?filter=zone_design2,0023_0000000004", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"PRD-MGMT_link_PRD-ECN\",\n" +
                        "            \"code\": \"MGMT-ECN\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0024_0000000005\",\n" +
                        "            \"description\": \"MGMT-ECN\",\n" +
                        "            \"id\": \"0024_0000000005\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": null,\n" +
                        "            \"zone_design1\": \"0023_0000000003\",\n" +
                        "            \"zone_design2\": \"0023_0000000004\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"PRD-PARTNERNET_link_PRD-ECN\",\n" +
                        "            \"code\": \"PARTNERNET-ECN\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0024_0000000006\",\n" +
                        "            \"description\": \"PARTNERNET-ECN\",\n" +
                        "            \"id\": \"0024_0000000006\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": null,\n" +
                        "            \"zone_design1\": \"0023_0000000007\",\n" +
                        "            \"zone_design2\": \"0023_0000000004\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link?filter=zone_link_design,0024_0000000005", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"MGMT-ECN\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0018_0000000003\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"zone2\": \"0017_0000000002\",\n" +
                        "            \"zone1\": \"0017_0000000003\",\n" +
                        "            \"key_name\": \"PRD-GZ1-MGMT_link_PRD-GZ1-ECN\",\n" +
                        "            \"asset_code\": \"\",\n" +
                        "            \"name\": \"MGMT-ECN\",\n" +
                        "            \"id\": \"0018_0000000003\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"zone_link_design\": \"0024_0000000005\",\n" +
                        "            \"fixed_date\": null\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link?filter=zone_link_design,0024_0000000006", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"PARTNERNET-ECN\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0018_0000000007\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"zone2\": \"0017_0000000002\",\n" +
                        "            \"zone1\": \"0017_0000000005\",\n" +
                        "            \"key_name\": \"PRD-GZ1-PARTNERNET_link_PRD-GZ1-ECN\",\n" +
                        "            \"asset_code\": \"\",\n" +
                        "            \"name\": \"PARTNERNET-ECN\",\n" +
                        "            \"id\": \"0018_0000000007\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"zone_link_design\": \"0024_0000000006\",\n" +
                        "            \"fixed_date\": null\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone?filter=id,0017_0000000003", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"zone_layer\": 124,\n" +
                        "            \"code\": \"MGMT\",\n" +
                        "            \"orchestration\": 224,\n" +
                        "            \"network_segment\": \"0021_0000000004\",\n" +
                        "            \"r_guid\": \"0017_0000000003\",\n" +
                        "            \"vpc\": \"\",\n" +
                        "            \"description\": \"MGMT\",\n" +
                        "            \"idc\": \"0016_0000000001\",\n" +
                        "            \"type\": 121,\n" +
                        "            \"key_name\": \"PRD-GZ1-MGMT\",\n" +
                        "            \"asset_code\": \"\",\n" +
                        "            \"name\": \"MGMT\",\n" +
                        "            \"id\": \"0017_0000000003\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": null,\n" +
                        "            \"zone_design\": \"0023_0000000003\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone?filter=id,0017_0000000005", this.gatewayUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": \"rxf8rvAJ2Bi\",\n" +
                        "            \"zone_layer\": 123,\n" +
                        "            \"code\": \"PARTNERNET\",\n" +
                        "            \"orchestration\": 224,\n" +
                        "            \"network_segment\": \"0021_0000000008\",\n" +
                        "            \"r_guid\": \"0017_0000000005\",\n" +
                        "            \"vpc\": \"\",\n" +
                        "            \"description\": \"PARTNER\",\n" +
                        "            \"idc\": \"0016_0000000001\",\n" +
                        "            \"type\": 117,\n" +
                        "            \"key_name\": \"PRD-GZ1-PARTNERNET\",\n" +
                        "            \"asset_code\": \"vpc-hewlni6b\",\n" +
                        "            \"name\": \"PARTNER\",\n" +
                        "            \"id\": \"0017_0000000005\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"2019-07-25 21:46:43\",\n" +
                        "            \"zone_design\": \"0023_0000000007\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));


    }

    private void mockFwdNodeExpressionWriteBackServer(MockRestServiceServer server) {
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design?filter=id,0001_0000000001", this.gatewayUrl)))
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


        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/update", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"EDP\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"Test\",\n" +
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

}
