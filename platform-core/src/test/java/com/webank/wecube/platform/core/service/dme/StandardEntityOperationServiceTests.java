package com.webank.wecube.platform.core.service.dme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
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

import com.webank.wecube.platform.core.BaseSpringBootTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;



public class StandardEntityOperationServiceTests extends BaseSpringBootTest{

    @Autowired
    StandardEntityOperationService standardEntityOperationService;
    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;
    private String gatewayUrl = "localhost:8080";
    private MockRestServiceServer server;
    

    @Before
    public void setup() {
        server = MockRestServiceServer.bindTo(jwtSsoRestTemplate).build();
        gatewayUrl = this.applicationProperties.getGatewayUrl();
    }

    @Test
    public void givenPackageNameWithDashAndFwdNodeExpressionWhenFetchThenShouldSucceed() {
        mockPackageNameWithDashAndFwdNodeExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("we-cmdb:system_design.code", "0001_0000000001"));
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP", result.get(0));

        result = standardEntityOperationService.query(
                new EntityOperationRootCondition("we-cmdb:unit.key_name", "0008_0000000003"));
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP-CORE_PRD-APP", result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionFetchShouldSucceed() {
        mockFwdNodeExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:system_design.code", "0001_0000000001"));
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP", result.get(0));

        result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:unit.key_name", "0008_0000000003"));
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP-CORE_PRD-APP", result.get(0));

        server.verify();
    }

    @Ignore
    @Test
    public void wecmdbFwdNodeExpressionFetchWithoutLastOpFetchShouldSucceed() {
        mockFwdNodeExpressionServer(server);

        final int WECMDB_SYSTEM_DESIGN_DATA_COLUMN_LENGTH = 12;
        List<Object> resultOne = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:system_design", "0001_0000000001"));
        assertNotNull(resultOne);
        LinkedHashMap<String, Object> resultOneMap = (LinkedHashMap<String, Object>) resultOne.get(0);
        assertThat(resultOneMap.size()).isEqualTo(WECMDB_SYSTEM_DESIGN_DATA_COLUMN_LENGTH);

        final int WECMDB_UNIT_DATA_COLUMN_LENGTH = 15;
        List<Object> resultTwo = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:unit", "0008_0000000003"));
        assertNotNull(resultTwo);
        LinkedHashMap<String, Object> resultTwoMap = (LinkedHashMap<String, Object>) resultTwo.get(0);
        assertThat(resultTwoMap.size()).isEqualTo(WECMDB_UNIT_DATA_COLUMN_LENGTH);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpToExpressionFetchShouldSucceed() {
        mockOneLinkWithOpToOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:subsys_design.system_design>wecmdb:system_design.code", "0002_0000000006"));
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("EDP", result.get(0));
        

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpByExpressionFetchShouldSucceed() {
        mockOneLinkWithOpByOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit.fixed_date{attr1 eq 'abcDEF'}", "0007_0000000001"));
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("2019-07-24 16:30:35", result.get(0));
        Assert.assertEquals("", result.get(1));

        result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:service_design~(service_design)wecmdb:invoke_design.key_name", "0004_0000000001"));
        
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("EDP-ADMCORE-APP_SYNC_INVOC_EDP-CORE-APP-SER1", result.get(0));
        Assert.assertEquals("EDP-ADMBATCH-APP_SYNC_INVOC_EDP-CORE-APP-SER1", result.get(1));

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpToOnlyExpressionFetchShouldSucceed() {
        mockMultipleLinksWithOpToOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name", "0007_0000000001"));
        Assert.assertNotNull(result);
        Assert.assertEquals("ECIF", result.get(0));

        result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design.fixed_date", "0018_0000000002"));
        Assert.assertNotNull(result);
        Assert.assertNull(result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpByOnlyExpressionFetchShouldSucceed() {
        mockMultipleLinksWithOpByOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance.id", "0007_0000000001"));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("0015_0000000001", result.get(0));
        

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithMixedOpExpressionFetchShouldSucceed() {
        mockMultipleLinksWithMixedOpExpressionServer(server);
        List<Object> result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name", "0007_0000000001"));
        
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("ECIF-CORE", result.get(0));
        Assert.assertEquals("ECIF-CORE", result.get(1));


        result = standardEntityOperationService.query(
                new EntityOperationRootCondition("wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone.key_name", "0023_0000000004"));
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("PRD-GZ1-MGMT", result.get(0));
        Assert.assertEquals("PRD-GZ1-PARTNERNET", result.get(1));

        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionWriteBackShouldSucceed() {
        mockFwdNodeExpressionWriteBackServer(server);
        final Object WRITE_BACK_DATA = "Test";
        EntityOperationRootCondition expressionToRootData = new EntityOperationRootCondition("wecmdb:system_design.code", "0001_0000000001");
        standardEntityOperationService.update(
                expressionToRootData, WRITE_BACK_DATA);
        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionGetPreviewTreeShouldSucceed() {
        mockFwdNodeExpressionServer(server);

        List<TreeNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:system_design.code", "0001_0000000001"));
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(1);

        List<TreeNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:unit.key_name", "0008_0000000003"));
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(1);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpToExpressionGetPreviewTreeShouldSucceed() {
        mockOneLinkWithOpToOnlyExpressionServer(server);

        List<TreeNode> treeNodeList = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys_design.system_design>wecmdb:system_design.code", "0002_0000000006"));
        assertNotNull(treeNodeList);
        assertThat(treeNodeList.size()).isEqualTo(2);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpByExpressionGetPreviewTreeShouldSucceed() {
        mockOneLinkWithOpByOnlyExpressionServer(server);

        List<TreeNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit.fixed_date", "0007_0000000001"));
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(3);


        List<TreeNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:service_design~(service_design)wecmdb:invoke_design.key_name", "0004_0000000001"));
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(3);

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpToOnlyExpressionGetPreviewTreeShouldSucceed() {
        mockMultipleLinksWithOpToOnlyExpressionServer(server);

        List<TreeNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name", "0007_0000000001"));
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(3);

        List<TreeNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design.fixed_date", "0018_0000000002"));
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(3);

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpByOnlyExpressionGetPreviewTreeShouldSucceed() {
        mockMultipleLinksWithOpByOnlyExpressionServer(server);

        List<TreeNode> treeNodeList = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance.id", "0007_0000000001"));
        assertNotNull(treeNodeList);
        assertThat(treeNodeList.size()).isEqualTo(4);

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithMixedOpExpressionGetPreviewTreeShouldSucceed() {
        mockMultipleLinksWithMixedOpExpressionServer(server);
        List<TreeNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name", "0007_0000000001"));
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(7); // because one treeNode has two parent nodes, each parent node has one node with same value

        List<TreeNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone.key_name", "0023_0000000004"));
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(7);

        server.verify();
    }

    private void mockPackageNameWithDashAndFwdNodeExpressionServer(MockRestServiceServer server) {
        // mockFwdNodeExpression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/we-cmdb/entities/system_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/we-cmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"package\": \"\",\n" +
                        "            \"r_guid\": \"0008_0000000003\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"displayName\": \"\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"EDP-CORE_PRD-APP\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000003\",\n" +
                        "            \"id\": \"0008_0000000003\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:37\",\n" +
                        "            \"unit_design\": \"0003_0000000002\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockFwdNodeExpressionServer(MockRestServiceServer server) {
        // mockFwdNodeExpression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"package\": \"\",\n" +
                        "            \"r_guid\": \"0008_0000000003\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"displayName\": \"\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"EDP-CORE_PRD-APP\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000003\",\n" +
                        "            \"id\": \"0008_0000000003\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:37\",\n" +
                        "            \"unit_design\": \"0003_0000000002\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockOneLinkWithOpToOnlyExpressionServer(MockRestServiceServer server) {
        // mockOneLinkWithOpToOnlyExpression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"ADMBATCH\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0002_0000000006\",\n" +
                        "            \"description\": \"ADM Batch Subsystem\",\n" +
                        "            \"displayName\": \"ADM Batch Subsystem\",\n" +
                        "            \"dcn_design_type\": 132,\n" +
                        "            \"key_name\": \"EDP-ADMBATCH\",\n" +
                        "            \"name\": \"ADM Batch Subsystem\",\n" +
                        "            \"id\": \"0002_0000000006\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:28:25\",\n" +
                        "            \"system_design\": \"0001_0000000001\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockOneLinkWithOpByOnlyExpressionServer(MockRestServiceServer server) {
        // mockOneLinkWithOpByOnlyExpression

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"\",\n" +
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
                        "            \"displayName\": \"aa\",\n" +
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

        // second expression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/service_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));


        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/invoke_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": \"\",\n" +
                        "            \"key_name\": \"EDP-ADMCORE-APP_SYNC_INVOC_EDP-CORE-APP-SER1\",\n" +
                        "            \"code\": \"sync_invoke\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0005_0000000008\",\n" +
                        "            \"service_design\": \"0004_0000000001\",\n" +
                        "            \"description\": \"Access CORE\",\n" +
                        "            \"displayName\": \"Access CORE\",\n" +
                        "            \"id\": \"0005_0000000008\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"type\": 152,\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000019\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": \"\",\n" +
                        "            \"key_name\": \"EDP-ADMBATCH-APP_SYNC_INVOC_EDP-CORE-APP-SER1\",\n" +
                        "            \"code\": \"sync_invoke\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0005_0000000009\",\n" +
                        "            \"service_design\": \"0004_0000000001\",\n" +
                        "            \"description\": \"Access CORE\",\n" +
                        "            \"displayName\": \"Access CORE\",\n" +
                        "            \"id\": \"0005_0000000009\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"type\": 152,\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000018\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));


    }

    private void mockMultipleLinksWithOpToOnlyExpressionServer(MockRestServiceServer server) {
        // first expression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"CRM Core Subsystem\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"key_name\": \"ECIF\",\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"ECIF\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0001_0000000003\",\n" +
                        "            \"name\": \"CRM System\",\n" +
                        "            \"description\": \"CRM System\",\n" +
                        "            \"displayName\": \"CRM System\",\n" +
                        "            \"id\": \"0001_0000000003\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        // second expression
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"MGMT-SF\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0018_0000000002\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"displayName\": \"\",\n" +
                        "            \"zone2\": \"0017_0000000001\",\n" +
                        "            \"zone1\": \"0017_0000000003\",\n" +
                        "            \"key_name\": \"PRD-GZ1-MGMT_link_PRD-GZ1-SF\",\n" +
                        "            \"asset_code\": \"\",\n" +
                        "            \"name\": \"MGMT-SF\",\n" +
                        "            \"id\": \"0018_0000000002\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"zone_link_design\": \"0024_0000000001\",\n" +
                        "            \"fixed_date\": null\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"MGMT\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"zone_layer\": 124,\n" +
                        "            \"key_name\": \"PRD-MGMT\",\n" +
                        "            \"code\": \"MGMT\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0023_0000000003\",\n" +
                        "            \"description\": \"MGMT\",\n" +
                        "            \"displayName\": \"MGMT\",\n" +
                        "            \"id\": \"0023_0000000003\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"type\": 121,\n" +
                        "            \"fixed_date\": null,\n" +
                        "            \"idc_design\": \"0022_0000000001\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockMultipleLinksWithOpByOnlyExpressionServer(MockRestServiceServer server) {

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"\",\n" +
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
                        "            \"displayName\": \"aa\",\n" +
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

        server.expect(ExpectedCount.once(), requestTo(String.format("http://%s/wecmdb/entities/running_instance/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"charge_type\": 115,\n" +
                        "            \"code\": \"APP_01\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0015_0000000001\",\n" +
                        "            \"instance_mem\": 2,\n" +
                        "            \"description\": \"APP_01\",\n" +
                        "            \"displayName\": \"APP_01\",\n" +
                        "            \"type\": 174,\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-APP_APP_01\",\n" +
                        "            \"instance_num\": 3,\n" +
                        "            \"unit\": \"0008_0000000001\",\n" +
                        "            \"asset_code\": \"\",\n" +
                        "            \"port\": \"\",\n" +
                        "            \"instance_disk\": 100,\n" +
                        "            \"host\": \"0012_0000000003\",\n" +
                        "            \"id\": \"0015_0000000001\",\n" +
                        "            \"state\": 40,\n" +
                        "            \"fixed_date\": \"\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.once(), requestTo(String.format("http://%s/wecmdb/entities/running_instance/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": []\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

    private void mockMultipleLinksWithMixedOpExpressionServer(MockRestServiceServer server) {
        // first request

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"\",\n" +
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
                        "            \"displayName\": \"aa\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"Application module\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"DB Module\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"CRM Core Subsystem\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"displayName\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0023_0000000004\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"MGMT-ECN\",\n" +
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
                        "            \"displayName\": \"PARTNERNET-ECN\",\n" +
                        "            \"id\": \"0024_0000000006\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": null,\n" +
                        "            \"zone_design1\": \"0023_0000000007\",\n" +
                        "            \"zone_design2\": \"0023_0000000004\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone_link/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"MGMT\",\n" +
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

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/zone/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"PARTNER\",\n" +
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
        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
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
                        "            \"displayName\": \"Deposit Micro Core System\",\n" +
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
                        "            \"displayName\": \"Deposit Micro Core System\",\n" +
                        "            \"id\": \"0001_0000000001\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));
    }

}
