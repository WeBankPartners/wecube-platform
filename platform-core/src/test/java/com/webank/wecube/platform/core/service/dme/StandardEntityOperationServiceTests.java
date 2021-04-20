package com.webank.wecube.platform.core.service.dme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.BaseSpringBootTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;

public class StandardEntityOperationServiceTests extends BaseSpringBootTest {

    @Autowired
    StandardEntityOperationService standardEntityOperationService;
    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;

    private String gatewayUrl = "localhost:8080";
    private MockRestServiceServer server;

    StandardEntityOperationServiceTestsMockers mockers;

    Map<Object, Object> externalCacheMap = new HashMap<>();

    @Before
    public void setup() {
        server = MockRestServiceServer.bindTo(jwtSsoRestTemplate).build();
        gatewayUrl = this.applicationProperties.getGatewayUrl();
        mockers = new StandardEntityOperationServiceTestsMockers(gatewayUrl);

        // externalCacheMap = new HashMap<>();
//        externalCacheMap = null;
    }

    @Test
    public void wecmdbMultipleRefToLinksWithOpToOnlyExpressionFetchShouldSucceed() {
        mockers.mockWecmdbMultipleRefToLinksWithOpToOnlyExpressionFetchShouldSucceed(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name",
                "0007_0000000001"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("ECIF", result.get(0));

        result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design.fixed_date", "0018_0000000002"),
                externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertNull(result.get(0));

        server.verify();
    }

    @Test
    public void givenSignleLinkNodeWithFilterExpressionWhenFetchThenShouldSucceed() {
        mockers.mockSingleLinkNodeWithFilterExpressionServer(server);

        List<Map<String, Object>> result = standardEntityOperationService.queryAttributeValuesOfLeafNode(
                new EntityOperationRootCondition("we-cmdb:system_design{attr1 eq 'ABC'}", null), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("EDP", result.get(0).get("key_name"));

        result = standardEntityOperationService.queryAttributeValuesOfLeafNode(
                new EntityOperationRootCondition("we-cmdb:unit.key_name", null), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("EDP-CORE_PRD-APP", result.get(0).get("key_name"));

        server.verify();
    }

    @Test
    public void givenPackageNameWithDashAndFwdNodeExpressionWhenFetchThenShouldSucceed() {
        mockers.mockPackageNameWithDashAndFwdNodeExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("we-cmdb:system_design.code", "0001_0000000001"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP", result.get(0));

        result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("we-cmdb:unit.key_name", "0008_0000000003"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP-CORE_PRD-APP", result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionFetchShouldSucceed() {
        mockers.mockFwdNodeExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:system_design.code", "0001_0000000001"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP", result.get(0));

        result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:unit.key_name", "0008_0000000003"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("EDP-CORE_PRD-APP", result.get(0));

        server.verify();
    }

    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void wecmdbFwdNodeExpressionFetchWithoutLastOpFetchShouldSucceed() {
        mockers.mockFwdNodeExpressionServer(server);

        final int WECMDB_SYSTEM_DESIGN_DATA_COLUMN_LENGTH = 12;
        List<Object> resultOne = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:system_design", "0001_0000000001"), externalCacheMap);
        assertNotNull(resultOne);
        LinkedHashMap<String, Object> resultOneMap = (LinkedHashMap<String, Object>) resultOne.get(0);
        assertThat(resultOneMap.size()).isEqualTo(WECMDB_SYSTEM_DESIGN_DATA_COLUMN_LENGTH);

        final int WECMDB_UNIT_DATA_COLUMN_LENGTH = 15;
        List<Object> resultTwo = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:unit", "0008_0000000003"), externalCacheMap);
        assertNotNull(resultTwo);
        LinkedHashMap<String, Object> resultTwoMap = (LinkedHashMap<String, Object>) resultTwo.get(0);
        assertThat(resultTwoMap.size()).isEqualTo(WECMDB_UNIT_DATA_COLUMN_LENGTH);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpToExpressionFetchShouldSucceed() {
        mockers.mockOneLinkWithOpToOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:subsys_design.system_design>wecmdb:system_design.code", "0002_0000000006"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("EDP", result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpByExpressionFetchShouldSucceed() {
        mockers.mockOneLinkWithOpByOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:subsys~(subsys)wecmdb:unit{attr1 eq '@@0001_1000222666@@abcDEF'}.fixed_date",
                "0007_0000000001"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("2019-07-24 16:30:35", result.get(0));
        Assert.assertEquals("", result.get(1));

        result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:service_design~(service_design)wecmdb:invoke_design.key_name",
                        "0004_0000000001"),
                externalCacheMap);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("EDP-ADMCORE-APP_SYNC_INVOC_EDP-CORE-APP-SER1", result.get(0));
        Assert.assertEquals("EDP-ADMBATCH-APP_SYNC_INVOC_EDP-CORE-APP-SER1", result.get(1));

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpToOnlyExpressionFetchShouldSucceed() {
        mockers.mockMultipleLinksWithOpToOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name",
                "0007_0000000001"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("ECIF", result.get(0));

        result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design.fixed_date", "0018_0000000002"),
                externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertNull(result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpByOnlyExpressionFetchShouldSucceed() {
        mockers.mockMultipleLinksWithOpByOnlyExpressionServer(server);

        List<Object> result = standardEntityOperationService.queryAttributeValues(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance.id",
                        "0007_0000000001"),
                externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("0015_0000000001", result.get(0));

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithMixedOpExpressionFetchShouldSucceed() {
        mockers.mockMultipleLinksWithMixedOpExpressionServer(server);
        List<Object> result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name",
                "0007_0000000001"), externalCacheMap);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("ECIF-CORE", result.get(0));
        Assert.assertEquals("ECIF-CORE", result.get(1));

        result = standardEntityOperationService.queryAttributeValues(new EntityOperationRootCondition(
                "wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone.key_name",
                "0023_0000000004"), externalCacheMap);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("PRD-GZ1-MGMT", result.get(0));
        Assert.assertEquals("PRD-GZ1-PARTNERNET", result.get(1));

        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionWriteBackShouldSucceed() {
        mockers.mockFwdNodeExpressionWriteBackServer(server);
        final Object WRITE_BACK_DATA = "Test";
        EntityOperationRootCondition expressionToRootData = new EntityOperationRootCondition(
                "wecmdb:system_design.code", "0001_0000000001");
        standardEntityOperationService.update(expressionToRootData, WRITE_BACK_DATA, externalCacheMap);
        server.verify();
    }

    @Test
    public void wecmdbFwdNodeExpressionGetPreviewTreeShouldSucceed() {
        mockers.mockFwdNodeExpressionServer(server);

        List<StandardEntityDataNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:system_design.code", "0001_0000000001"), externalCacheMap);
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(1);

        List<StandardEntityDataNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:unit.key_name", "0008_0000000003"), externalCacheMap);
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(1);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpToExpressionGetPreviewTreeShouldSucceed() {
        mockers.mockOneLinkWithOpToOnlyExpressionServer(server);

        List<StandardEntityDataNode> treeNodeList = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys_design.system_design>wecmdb:system_design.code",
                        "0002_0000000006"),
                externalCacheMap);
        assertNotNull(treeNodeList);
        assertThat(treeNodeList.size()).isEqualTo(2);

        server.verify();
    }

    @Test
    public void wecmdbOneLinkWithOpByExpressionGetPreviewTreeShouldSucceed() {
        mockers.mockOneLinkWithOpByOnlyExpressionServer(server);

        List<StandardEntityDataNode> treeNodeListOne = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit.fixed_date", "0007_0000000001"),
                externalCacheMap);
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(3);

        List<StandardEntityDataNode> treeNodeListTwo = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:service_design~(service_design)wecmdb:invoke_design.key_name",
                        "0004_0000000001"),
                externalCacheMap);
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(3);

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpToOnlyExpressionGetPreviewTreeShouldSucceed() {
        mockers.mockMultipleLinksWithOpToOnlyExpressionServer(server);

        List<StandardEntityDataNode> treeNodeListOne = standardEntityOperationService
                .generatePreviewTree(new EntityOperationRootCondition(
                        "wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name",
                        "0007_0000000001"), externalCacheMap);
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(3);

        List<StandardEntityDataNode> treeNodeListTwo = standardEntityOperationService
                .generatePreviewTree(new EntityOperationRootCondition(
                        "wecmdb:zone_link.zone1>wecmdb:zone.zone_design>wecmdb:zone_design.fixed_date",
                        "0018_0000000002"), externalCacheMap);
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(3);
        
        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithOpByOnlyExpressionGetPreviewTreeShouldSucceed() {
        mockers.mockMultipleLinksWithOpByOnlyExpressionServer(server);

        List<StandardEntityDataNode> treeNodeList = standardEntityOperationService.generatePreviewTree(
                new EntityOperationRootCondition("wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance.id",
                        "0007_0000000001"),
                externalCacheMap);
        assertNotNull(treeNodeList);
        assertThat(treeNodeList.size()).isEqualTo(4);

        server.verify();
    }

    @Test
    public void wecmdbMultipleLinksWithMixedOpExpressionGetPreviewTreeShouldSucceed() {
        // mockMultipleLinksWithMixedOpExpressionServer(server);
        mockers.mockWecmdbMultipleLinksWithMixedOpExpressionGetPreviewTreeShouldSucceed(server);
        List<StandardEntityDataNode> treeNodeListOne = standardEntityOperationService
                .generatePreviewTree(new EntityOperationRootCondition(
                        "wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name",
                        "0007_0000000001"), externalCacheMap);
        assertNotNull(treeNodeListOne);
        assertThat(treeNodeListOne.size()).isEqualTo(6); // because one treeNode
                                                         // has two parent
                                                         // nodes, each parent
                                                         // node has one node
                                                         // with same value

        List<StandardEntityDataNode> treeNodeListTwo = standardEntityOperationService
                .generatePreviewTree(new EntityOperationRootCondition(
                        "wecmdb:zone_design~(zone_design2)wecmdb:zone_link_design~(zone_link_design)wecmdb:zone_link.zone1>wecmdb:zone.key_name",
                        "0023_0000000004"), externalCacheMap);
        assertNotNull(treeNodeListTwo);
        assertThat(treeNodeListTwo.size()).isEqualTo(7);

        server.verify();
    }

}
