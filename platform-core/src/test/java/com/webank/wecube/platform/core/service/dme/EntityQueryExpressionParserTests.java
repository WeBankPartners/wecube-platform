package com.webank.wecube.platform.core.service.dme;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class EntityQueryExpressionParserTests {
	EntityQueryExpressionParser parser = new EntityQueryExpressionParser();

	@Test
	public void testParseRefToWithConditionShouldSucceed() {
		String inputExpr = "wecmdb:subsys{att1 eq '@@0008_123456789@@eee'}{attr2 like 'AAA'}.subsys_design"
				+ ">wecmdb:subsys_design.system_design" + ">wecmdb:system_design{attr2 like 'AAA'}.key_name";

		List<EntityQueryExprNodeInfo> queryNodeInfos = parser.parse(inputExpr);
		Assert.assertNotNull(queryNodeInfos);
		Assert.assertEquals(3, queryNodeInfos.size());
		EntityQueryExprNodeInfo nodeInfo = queryNodeInfos.get(0);
		Assert.assertEquals("wecmdb:subsys", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("subsys", nodeInfo.getEntityName());
		Assert.assertEquals("subsys_design", nodeInfo.getQueryAttrName());
		Assert.assertEquals("{att1 eq '@@0008_123456789@@eee'}{attr2 like 'AAA'}", nodeInfo.getEntityFilterExpr());

		Assert.assertEquals(2, nodeInfo.getAdditionalFilters().size());

		EntityQueryFilter filter = nodeInfo.getAdditionalFilters().get(0);
		Assert.assertEquals("att1", filter.getAttrName());
		Assert.assertEquals(EntityQueryFilter.OP_EQUALS, filter.getOp());
		Assert.assertEquals("0008_123456789", filter.getCondition());

		filter = nodeInfo.getAdditionalFilters().get(1);
		Assert.assertEquals("attr2", filter.getAttrName());
		Assert.assertEquals(EntityQueryFilter.OP_LIKE, filter.getOp());
		Assert.assertEquals("AAA", filter.getCondition());

		nodeInfo = queryNodeInfos.get(2);
		Assert.assertEquals("wecmdb:system_design", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("system_design", nodeInfo.getEntityName());
		Assert.assertEquals("key_name", nodeInfo.getQueryAttrName());
		Assert.assertEquals("{attr2 like 'AAA'}", nodeInfo.getEntityFilterExpr());

		Assert.assertEquals(1, nodeInfo.getAdditionalFilters().size());

		filter = nodeInfo.getAdditionalFilters().get(0);
		Assert.assertEquals("attr2", filter.getAttrName());
		Assert.assertEquals(EntityQueryFilter.OP_LIKE, filter.getOp());
		Assert.assertEquals("AAA", filter.getCondition());

	}

	@Test
	public void testParseSingleExprNodeWithoutFilterExprShouldSucceed() {
		String inputExpr = "we-cmdb:system_design";

		List<EntityQueryExprNodeInfo> queryNodeInfos = parser.parse(inputExpr);
		Assert.assertNotNull(queryNodeInfos);
		Assert.assertEquals(1, queryNodeInfos.size());
		Assert.assertEquals("we-cmdb:system_design", queryNodeInfos.get(0).getEntityInfoExpr());
		Assert.assertEquals("we-cmdb", queryNodeInfos.get(0).getPackageName());
		Assert.assertEquals("system_design", queryNodeInfos.get(0).getEntityName());
	}

	@Test
	public void testParseMultiExprNodeWithoutFilterExprShouldSucceed() {
		String inputExpr = "wecmdb:zone_design" + "~(zone_design2)wecmdb:zone_link_design"
				+ "~(zone_link_design)wecmdb:zone_link.zone1" + ">wecmdb:zone";

		List<EntityQueryExprNodeInfo> queryNodeInfos = parser.parse(inputExpr);
		Assert.assertNotNull(queryNodeInfos);
		Assert.assertEquals(4, queryNodeInfos.size());
		// 0
		Assert.assertEquals("wecmdb:zone_design", queryNodeInfos.get(0).getEntityInfoExpr());
		Assert.assertEquals("wecmdb", queryNodeInfos.get(0).getPackageName());
		Assert.assertEquals("zone_design", queryNodeInfos.get(0).getEntityName());
		Assert.assertTrue(queryNodeInfos.get(0).isHeadEntity());
		Assert.assertNull(queryNodeInfos.get(0).getEntityLinkType());

		// 1
		Assert.assertEquals("(zone_design2)wecmdb:zone_link_design", queryNodeInfos.get(1).getEntityInfoExpr());
		Assert.assertEquals("wecmdb", queryNodeInfos.get(1).getPackageName());
		Assert.assertEquals("zone_link_design", queryNodeInfos.get(1).getEntityName());
		Assert.assertFalse(queryNodeInfos.get(1).isHeadEntity());
		Assert.assertEquals(EntityLinkType.REF_BY, queryNodeInfos.get(1).getEntityLinkType());
		Assert.assertEquals("zone_design2", queryNodeInfos.get(1).getRefByAttrName());
	}

	@Test
	public void testParseSingleExprNodeWithFilterExprShouldSucceed() {
		String inputExpr = "we-cmdb:system_design{att1 eq '@@0008_123456789@@eee'}";

		List<EntityQueryExprNodeInfo> queryNodeInfos = parser.parse(inputExpr);
		Assert.assertNotNull(queryNodeInfos);
		Assert.assertEquals(1, queryNodeInfos.size());

		EntityQueryExprNodeInfo nodeInfo = queryNodeInfos.get(0);
		Assert.assertEquals("we-cmdb:system_design", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("we-cmdb", nodeInfo.getPackageName());
		Assert.assertEquals("system_design", nodeInfo.getEntityName());

		Assert.assertEquals("{att1 eq '@@0008_123456789@@eee'}", nodeInfo.getEntityFilterExpr());
		Assert.assertTrue(nodeInfo.hasAdditionalFilters());
		Assert.assertEquals(1, nodeInfo.getAdditionalFilters().size());
	}

	@Test
	public void testParseMultiExprNodeWithFilterExprShouldSucceed() {
		String inputExpr = "wecmdb:zone_design{att1 eq 'eee @@'}{att2 in ['@@0009_123456@@a','b']}"
				+ "~(zone_design2)wecmdb:zone_link_design{att1 like 'A'}" + "~(zone_link_design)wecmdb:zone_link.zone1"
				+ ">wecmdb:zone";

		List<EntityQueryExprNodeInfo> queryNodeInfos = parser.parse(inputExpr);
		Assert.assertNotNull(queryNodeInfos);
		Assert.assertEquals(4, queryNodeInfos.size());
		// 0
		EntityQueryExprNodeInfo nodeInfo = queryNodeInfos.get(0);
		Assert.assertEquals("wecmdb:zone_design", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("zone_design", nodeInfo.getEntityName());
		Assert.assertTrue(nodeInfo.isHeadEntity());
		Assert.assertNull(nodeInfo.getEntityLinkType());
		Assert.assertEquals("{att1 eq 'eee @@'}{att2 in ['@@0009_123456@@a','b']}", nodeInfo.getEntityFilterExpr());
		Assert.assertTrue(nodeInfo.hasAdditionalFilters());
		Assert.assertEquals(2, nodeInfo.getAdditionalFilters().size());

		EntityQueryFilter filter = nodeInfo.getAdditionalFilters().get(0);
		Assert.assertEquals("att1", filter.getAttrName());
		Assert.assertEquals(EntityQueryFilter.OP_EQUALS, filter.getOp());
		Assert.assertEquals("eee @@", filter.getCondition());

		filter = nodeInfo.getAdditionalFilters().get(1);
		Assert.assertEquals("att2", filter.getAttrName());
		Assert.assertEquals(EntityQueryFilter.OP_IN, filter.getOp());
		Assert.assertTrue(filter.getCondition() instanceof List);
		Assert.assertEquals(2, ((List<?>) filter.getCondition()).size());

		// 1
		nodeInfo = queryNodeInfos.get(1);
		Assert.assertEquals("(zone_design2)wecmdb:zone_link_design", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("zone_link_design", nodeInfo.getEntityName());
		Assert.assertFalse(nodeInfo.isHeadEntity());
		Assert.assertEquals(EntityLinkType.REF_BY, nodeInfo.getEntityLinkType());
		Assert.assertEquals("zone_design2", nodeInfo.getRefByAttrName());
		Assert.assertEquals("{att1 like 'A'}", nodeInfo.getEntityFilterExpr());
		Assert.assertTrue(nodeInfo.hasAdditionalFilters());
		Assert.assertEquals(1, nodeInfo.getAdditionalFilters().size());

		// 2
		nodeInfo = queryNodeInfos.get(2);
		Assert.assertEquals("(zone_link_design)wecmdb:zone_link.zone1", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("zone_link", nodeInfo.getEntityName());
		Assert.assertFalse(nodeInfo.isHeadEntity());
		Assert.assertEquals(EntityLinkType.REF_BY, nodeInfo.getEntityLinkType());
		Assert.assertEquals("zone_link_design", nodeInfo.getRefByAttrName());
		Assert.assertEquals(null, nodeInfo.getEntityFilterExpr());
		Assert.assertTrue(nodeInfo.hasAdditionalFilters() == false);
		Assert.assertEquals(0, nodeInfo.getAdditionalFilters().size());
		Assert.assertEquals("zone1", nodeInfo.getQueryAttrName());

		// 3
		nodeInfo = queryNodeInfos.get(3);
		Assert.assertEquals("wecmdb:zone", nodeInfo.getEntityInfoExpr());
		Assert.assertEquals("wecmdb", nodeInfo.getPackageName());
		Assert.assertEquals("zone", nodeInfo.getEntityName());
		Assert.assertFalse(nodeInfo.isHeadEntity());
		Assert.assertEquals(EntityLinkType.REF_TO, nodeInfo.getEntityLinkType());
		Assert.assertEquals(null, nodeInfo.getRefByAttrName());
		Assert.assertEquals(null, nodeInfo.getEntityFilterExpr());
		Assert.assertTrue(nodeInfo.hasAdditionalFilters() == false);
		Assert.assertEquals(0, nodeInfo.getAdditionalFilters().size());
		Assert.assertEquals(null, nodeInfo.getQueryAttrName());
	}
}
