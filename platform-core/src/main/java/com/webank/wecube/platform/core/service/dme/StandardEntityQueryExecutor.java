package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * @author gavinli
 *
 */
@Service("standardEntityQueryExecutor")
public class StandardEntityQueryExecutor implements EntityQueryExecutor {
	private static final Logger log = LoggerFactory.getLogger(StandardEntityQueryExecutor.class);

	@Override
	public EntityTreeNodesOverview generateEntityLinkOverview(EntityOperationContext ctx) {
		doExecuteQuery(ctx);

		List<TreeNode> hierarchicalEntityNodes = generateHierarchicalEntityTreeNodes(ctx);
		List<TreeNode> leafNodeEntityNodes = generateLeafNodeEntityNodes(ctx);

		return buildEntityTreeNodesOverview(hierarchicalEntityNodes, leafNodeEntityNodes);
	}

	@Override
	public List<TreeNode> generatePreviewTree(EntityOperationContext ctx) {
		doExecuteQuery(ctx);
		return generateHierarchicalEntityTreeNodes(ctx);
	}

	private void pupolateTreeNodeWithLinkNode(List<TreeNode> result, EntityQueryLinkNode linkNode) {
		for (EntityDataDelegate delegate : linkNode.getEntityDataDelegates()) {

			TreeNode currTreeNode = findTreeNode(result, delegate.getPackageName(), delegate.getEntityName(),
					delegate.getId());
			if (currTreeNode == null) {
				currTreeNode = new TreeNode();
				currTreeNode.setRootId(delegate.getId());
				currTreeNode.setDisplayName(delegate.getDisplayName());
				currTreeNode.setEntityName(delegate.getEntityName());
				currTreeNode.setPackageName(delegate.getPackageName());

				result.add(currTreeNode);
			}

			EntityDataDelegate prevDelegate = delegate.getPreviousEntity();
			if (prevDelegate != null) {
				TreeNode prevTreeNode = findTreeNode(result, prevDelegate.getPackageName(),
						prevDelegate.getEntityName(), prevDelegate.getId());
				if (prevTreeNode == null) {
					prevTreeNode = new TreeNode();
					prevTreeNode.setRootId(prevDelegate.getId());
					prevTreeNode.setDisplayName(prevDelegate.getDisplayName());
					prevTreeNode.setEntityName(prevDelegate.getEntityName());
					prevTreeNode.setPackageName(prevDelegate.getPackageName());

					result.add(prevTreeNode);
				}

				currTreeNode.setParent(prevTreeNode);
				prevTreeNode.addChildren(currTreeNode);
			}

			for (EntityDataDelegate succeedingDelegate : delegate.getSucceedingEntities()) {
				TreeNode succeedingTreeNode = findTreeNode(result, succeedingDelegate.getPackageName(),
						succeedingDelegate.getEntityName(), succeedingDelegate.getId());
				if (succeedingTreeNode == null) {
					succeedingTreeNode = new TreeNode();
					succeedingTreeNode.setRootId(succeedingDelegate.getId());
					succeedingTreeNode.setDisplayName(succeedingDelegate.getDisplayName());
					succeedingTreeNode.setEntityName(succeedingDelegate.getEntityName());
					succeedingTreeNode.setPackageName(succeedingDelegate.getPackageName());

					result.add(succeedingTreeNode);
				}

				succeedingTreeNode.setParent(currTreeNode);
				currTreeNode.addChildren(succeedingTreeNode);
			}
		}
	}

	private TreeNode findTreeNode(List<TreeNode> nodes, String packageName, String entityName, String id) {
		for (TreeNode n : nodes) {
			if (n.getPackageName().equals(packageName) && n.getEntityName().equals(entityName)
					&& n.getRootId().equals(id)) {
				return n;
			}
		}

		return null;
	}

	public void executeUpdate(EntityOperationContext ctx, Object valueToUpdate) {
		List<EntityDataDelegate> entitiesToUpdate = executeQueryLeafEntity(ctx);
		List<EntityDataRecord> entityDataRecordsToUpdate = buildEntityDataRecords(entitiesToUpdate, valueToUpdate);

		EntityQueryLinkNode leafLinkNode = ctx.getTailEntityQueryLinkNode();
		EntityQueryExprNodeInfo nodeInfo = leafLinkNode.getExprNodeInfo();
		EntityRouteDescription entityDef = ctx.getEntityDataRouteFactory()
				.deduceEntityDescription(nodeInfo.getEntityName(), nodeInfo.getPackageName());

		StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
		restClient.update(entityDef, entityDataRecordsToUpdate);
	}

	public List<Object> executeQueryLeafAttributes(EntityOperationContext ctx) {
		doExecuteQuery(ctx);
		return extractAttrValues(ctx);
	}

	public List<EntityDataDelegate> executeQueryLeafEntity(EntityOperationContext ctx) {
		doExecuteQuery(ctx);
		return extractLeafEntityData(ctx);
	}

	public void performQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode) {
		if (log.isInfoEnabled()) {
			log.info("performing query for {} {}", linkNode.getIndex(),
					linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
		}

		EntityQueryExprNodeInfo nodeInfo = linkNode.getExprNodeInfo();
		EntityRouteDescription entityDef = ctx.getEntityDataRouteFactory()
				.deduceEntityDescription(nodeInfo.getEntityName(), nodeInfo.getPackageName());

		doPerformQuery(ctx, linkNode, entityDef);
	}

	protected List<EntityDataRecord> buildEntityDataRecords(List<EntityDataDelegate> entitiesToUpdate,
			Object valueToUpdate) {
		List<EntityDataRecord> dataRecords = new ArrayList<>();
		for (EntityDataDelegate delegate : entitiesToUpdate) {
			if (delegate.getQueryAttrName() == null || delegate.getQueryAttrName().trim().length() < 1) {
				log.warn("Unknown field to update for {} {}, probably the expression is not valid. ",
						delegate.getPackageName(), delegate.getEntityName());
				throw new IllegalStateException(String.format("Unknown field to update for %s %s",
						delegate.getPackageName(), delegate.getEntityName()));
			}
			if (log.isInfoEnabled()) {
				log.info("UPDATE entity:id={} name={} attrName={} oldValue={} newValue={}", delegate.getId(),
						delegate.getDisplayName(), delegate.getQueryAttrName(), delegate.getQueryAttrValue(),
						valueToUpdate);
			}
			EntityDataRecord record = new EntityDataRecord();
			record.setId(delegate.getId());
			EntityDataAttr attr = new EntityDataAttr();
			attr.setAttrName(delegate.getQueryAttrName());
			attr.setAttrValue(valueToUpdate);
			record.addAttrs(attr);

			dataRecords.add(record);
		}

		return dataRecords;
	}

	private void doPerformQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef) {

		if (linkNode.isHeadLinkNode()) {
			doPerformHeadEntityLinkNodeQuery(ctx, linkNode, entityDef);
			return;
		}

		if (linkNode.getExprNodeInfo().getEntityLinkType() == EntityLinkType.REF_TO) {
			doPerformRefToEntityLinkNodeQuery(ctx, linkNode, entityDef);
			return;
		}

		if (linkNode.getExprNodeInfo().getEntityLinkType() == EntityLinkType.REF_BY) {
			doPerformRefByEntityLinkNodeQuery(ctx, linkNode, entityDef);
			return;
		}

		log.error("Such entity link type {} is not supported currently",
				linkNode.getExprNodeInfo().getEntityLinkType());
		throw new UnsupportedOperationException("Such entity link type is not supported.");

	}

	private void doPerformRefByEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef) {
		if (log.isDebugEnabled()) {
			log.debug("perform query for RefBy entity link  node with {}",
					linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
		}

		EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();

		EntityQueryLinkNode previousLinkNode = linkNode.getPreviousNode();
		List<EntityDataDelegate> prevEntityDataDelegates = previousLinkNode.getEntityDataDelegates();

		if (prevEntityDataDelegates != null) {
			for (EntityDataDelegate prevEntityDataDelegate : prevEntityDataDelegates) {
				EntityQuerySpecification querySpec = buildRefByEntityQuerySpecification(ctx, linkNode, entityDef,
						exprNodeInfo, previousLinkNode, prevEntityDataDelegate);

				performRestOperation(ctx, linkNode, entityDef, prevEntityDataDelegate, querySpec);
			}
		}
	}

	protected void doExecuteQuery(EntityOperationContext ctx) {
		EntityQueryLinkNode linkNode = ctx.getHeadEntityQueryLinkNode();
		while (linkNode != null) {
			linkNode.executeQuery(this, ctx);
			linkNode = linkNode.getSucceedingNode();
		}
	}

	private EntityQuerySpecification buildRefByEntityQuerySpecification(EntityOperationContext ctx,
			EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
			EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate) {
		EntityQuerySpecification querySpec = new EntityQuerySpecification();
		EntityQueryCriteria criteria = new EntityQueryCriteria();
		criteria.setAttrName(exprNodeInfo.getRefByAttrName());
		criteria.setCondition(String.valueOf(prevEntityDataDelegate.getId()));

		if (exprNodeInfo.getAdditionalFilters() != null) {
			for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
				EntityQueryFilter queryFilter = new EntityQueryFilter();
				queryFilter.setAttrName(f.getAttrName());
				queryFilter.setOp(f.getOp());
				queryFilter.setCondition(f.getCondition());
				querySpec.addAdditionalFilters(queryFilter);
			}
		}

		querySpec.setCriteria(criteria);

		return querySpec;
	}

	private void doPerformRefToEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef) {
		if (log.isDebugEnabled()) {
			log.debug("perform query for RefTo entity link  node with {}",
					linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
		}

		EntityQueryLinkNode previousLinkNode = linkNode.getPreviousNode();
		List<EntityDataDelegate> prevEntityDataDelegates = previousLinkNode.getEntityDataDelegates();
		if (prevEntityDataDelegates != null) {
			for (EntityDataDelegate prevEntityDataDelegate : prevEntityDataDelegates) {
				if (prevEntityDataDelegate == null) {
					continue;
				}

				performRefToEntityDataDelegate(ctx, linkNode, entityDef, prevEntityDataDelegate, previousLinkNode);
			}
		}
	}

	private void performRefToEntityDataDelegate(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef, EntityDataDelegate prevEntityDataDelegate,
			EntityQueryLinkNode previousLinkNode) {
		List<EntityQuerySpecification> querySpecs = buildRefToEntityQuerySpecifications(ctx, linkNode, entityDef,
				linkNode.getExprNodeInfo(), previousLinkNode, prevEntityDataDelegate);

		if (log.isInfoEnabled() && (querySpecs.size() > 1)) {
			log.info("performing multi-ref-to querying for {} {}", linkNode.getExprNodeInfo().getPackageName(),
					linkNode.getExprNodeInfo().getEntityName());
		}

		for (EntityQuerySpecification querySpec : querySpecs) {
			performRestOperation(ctx, linkNode, entityDef, prevEntityDataDelegate, querySpec);
		}

	}

	private List<EntityQuerySpecification> buildRefToEntityQuerySpecifications(EntityOperationContext ctx,
			EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
			EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate) {
		List<EntityQuerySpecification> specs = new ArrayList<EntityQuerySpecification>();
		if (prevEntityDataDelegate.getQueryAttrValue() == null) {
			return specs;
		}

		String queryAttrValueStr = String.valueOf(prevEntityDataDelegate.getQueryAttrValue());
		if (queryAttrValueStr.trim().length() <= 0) {
			return specs;
		}
		
		queryAttrValueStr = stripHeadAndTailChar(queryAttrValueStr, "[");
		queryAttrValueStr = stripHeadAndTailChar(queryAttrValueStr, "]");

		String[] queryAttrValueParts = queryAttrValueStr.split(",");

		for (String queryAttrValuePart : queryAttrValueParts) {
			EntityQuerySpecification spec = buildRefToEntityQuerySpecification(ctx, linkNode, entityDef, exprNodeInfo,
					previousLinkNode, prevEntityDataDelegate, queryAttrValuePart);
			specs.add(spec);
		}

		return specs;
	}

	private EntityQuerySpecification buildRefToEntityQuerySpecification(EntityOperationContext ctx,
			EntityQueryLinkNode linkNode, EntityRouteDescription entityDef, EntityQueryExprNodeInfo exprNodeInfo,
			EntityQueryLinkNode previousLinkNode, EntityDataDelegate prevEntityDataDelegate, String queryAttrValueStr) {
		EntityQuerySpecification querySpec = new EntityQuerySpecification();
		EntityQueryCriteria criteria = new EntityQueryCriteria();
		criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
		criteria.setCondition(queryAttrValueStr.trim());

		if (exprNodeInfo.getAdditionalFilters() != null) {
			for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
				EntityQueryFilter queryFilter = new EntityQueryFilter();
				queryFilter.setAttrName(f.getAttrName());
				queryFilter.setOp(f.getOp());
				queryFilter.setCondition(f.getCondition());
				querySpec.addAdditionalFilters(queryFilter);
			}
		}

		querySpec.setCriteria(criteria);

		return querySpec;
	}

	private void doPerformHeadEntityLinkNodeQuery(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef) {
		if (log.isDebugEnabled()) {
			log.debug("perform query for head entity link  node with {}",
					linkNode.getExprNodeInfo().getEntityQueryNodeExpr());
		}
		EntityQueryExprNodeInfo exprNodeInfo = linkNode.getExprNodeInfo();
		EntityQuerySpecification querySpec = new EntityQuerySpecification();

		EntityQueryCriteria criteria = null;
		if (ctx.getOriginalEntityData() != null && ctx.getOriginalEntityData().trim().length() > 0) {
			criteria = new EntityQueryCriteria();
			criteria.setAttrName(EntityDataDelegate.UNIQUE_IDENTIFIER);
			criteria.setCondition(ctx.getOriginalEntityData());
		}

		if (exprNodeInfo.getAdditionalFilters() != null) {
			for (EntityQueryFilter f : exprNodeInfo.getAdditionalFilters()) {
				EntityQueryFilter queryFilter = new EntityQueryFilter();
				queryFilter.setAttrName(f.getAttrName());
				queryFilter.setOp(f.getOp());
				queryFilter.setCondition(f.getCondition());
				querySpec.addAdditionalFilters(queryFilter);
			}
		}

		if (criteria != null) {
			querySpec.setCriteria(criteria);
		}

		performRestOperation(ctx, linkNode, entityDef, null, querySpec);

	}

	private void performRestOperation(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityRouteDescription entityDef, EntityDataDelegate prevEntityDataDelegate,
			EntityQuerySpecification querySpec) {
		StandardEntityOperationRestClient restClient = ctx.getStandardEntityOperationRestClient();
		StandardEntityOperationResponseDto responseDto = restClient.query(entityDef, querySpec);

		if (StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(responseDto.getStatus())) {
			performEntityDataExtraction(ctx, linkNode, prevEntityDataDelegate, responseDto.getData());
		} else {
			log.error("Error status met {} with message {}", responseDto.getStatus(), responseDto.getMessage());
			throw new IllegalStateException(String.format("Errors met while fetching data from %s due to status %s.",
					entityDef.getPackageName(), responseDto.getStatus()));
		}
	}

	protected void performEntityDataExtraction(EntityOperationContext ctx, EntityQueryLinkNode linkNode,
			EntityDataDelegate prevEntityDataDelegate, Object responseData) {
		List<Map<String, Object>> recordMapList = extractEntityDataFromResponse(responseData);
		for (Map<String, Object> recordMap : recordMapList) {
			linkNode.addEntityDataDelegates(buildEntityDataDelegate(prevEntityDataDelegate, recordMap, linkNode));
		}

	}

	private EntityDataDelegate buildEntityDataDelegate(EntityDataDelegate prevEntityDataDelegate,
			Map<String, Object> recordMap, EntityQueryLinkNode linkNode) {
		EntityDataDelegate entity = new EntityDataDelegate();
		entity.setEntityData(recordMap);
		entity.setDisplayName((String) recordMap.get(EntityDataDelegate.VISUAL_FIELD));
		entity.setId((String) recordMap.get(EntityDataDelegate.UNIQUE_IDENTIFIER));
		entity.setPackageName(linkNode.getExprNodeInfo().getPackageName());
		entity.setEntityName(linkNode.getExprNodeInfo().getEntityName());

		if (prevEntityDataDelegate != null) {
			entity.setPreviousEntity(prevEntityDataDelegate);
		}

		if (linkNode.hasQueryAttribute()) {
			entity.setQueryAttrName(linkNode.getQueryAttributeName());
			entity.setQueryAttrValue(recordMap.get(linkNode.getQueryAttributeName()));
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> extractEntityDataFromResponse(Object responseData) {
		List<Map<String, Object>> recordMapList = new ArrayList<Map<String, Object>>();
		if (responseData == null) {
			log.info("response data is empty");
			return recordMapList;
		}

		if (responseData instanceof List) {
			List<?> dataList = ((List<Map<String, Object>>) responseData);
			for (Object m : dataList) {
				if (m == null) {
					continue;
				}
				if (m instanceof Map) {
					Map<String, Object> dataMap = (Map<String, Object>) m;
					recordMapList.add(dataMap);
				}
			}
		} else if (responseData instanceof Map) {
			Map<String, Object> dataMap = ((Map<String, Object>) responseData);
			recordMapList.add(dataMap);
		}

		return recordMapList;
	}

	protected List<EntityDataDelegate> extractLeafEntityData(EntityOperationContext ctx) {
		EntityQueryLinkNode tailEntityQueryLinkNode = ctx.getTailEntityQueryLinkNode();
		List<EntityDataDelegate> retDataDelegates = new ArrayList<>();
		for (EntityDataDelegate e : tailEntityQueryLinkNode.getEntityDataDelegates()) {
			EntityDataDelegate ret = new EntityDataDelegate();
			ret.setDisplayName(e.getDisplayName());
			ret.setEntityName(e.getEntityName());
			ret.setId(e.getId());
			ret.setPackageName(e.getPackageName());
			ret.setQueryAttrName(e.getQueryAttrName());
			ret.setQueryAttrValue(e.getQueryAttrValue());
			ret.setEntityData(e.getEntityData());

			retDataDelegates.add(ret);

		}
		return retDataDelegates;
	}

	protected List<Object> extractAttrValues(EntityOperationContext ctx) {
		EntityQueryLinkNode tailEntityQueryLinkNode = ctx.getTailEntityQueryLinkNode();
		return Collections.unmodifiableList(tailEntityQueryLinkNode.extractFinalAttributeValues());
	}

	protected List<TreeNode> generateHierarchicalEntityTreeNodes(EntityOperationContext ctx) {
		List<TreeNode> result = new ArrayList<>();

		EntityQueryLinkNode headEntityQueryLinkNode = ctx.getHeadEntityQueryLinkNode();
		EntityQueryLinkNode linkNode = headEntityQueryLinkNode;

		while (linkNode != null) {
			pupolateTreeNodeWithLinkNode(result, linkNode);
			linkNode = linkNode.getSucceedingNode();
		}

		return result;
	}

	protected EntityTreeNodesOverview buildEntityTreeNodesOverview(List<TreeNode> hierarchicalEntityNodes,
			List<TreeNode> leafNodeEntityNodes) {
		EntityTreeNodesOverview result = new EntityTreeNodesOverview();
		if (hierarchicalEntityNodes != null) {
			for (TreeNode tn : hierarchicalEntityNodes) {
				result.addHierarchicalEntityNodes(tn);
			}
		}

		if (leafNodeEntityNodes != null) {
			for (TreeNode tn : leafNodeEntityNodes) {
				result.addLeafNodeEntityNodes(tn);
			}
		}

		return result;
	}

	protected List<TreeNode> generateLeafNodeEntityNodes(EntityOperationContext ctx) {
		EntityQueryLinkNode leafLinkNode = ctx.getTailEntityQueryLinkNode();
		List<EntityDataDelegate> entityDataDelegates = leafLinkNode.getEntityDataDelegates();
		List<TreeNode> result = new ArrayList<>();

		for (EntityDataDelegate delegate : entityDataDelegates) {
			if (delegate == null) {
				continue;
			}

			TreeNode tn = new TreeNode();
			tn.setPackageName(leafLinkNode.getExprNodeInfo().getPackageName());
			tn.setEntityName(leafLinkNode.getExprNodeInfo().getEntityName());
			tn.setDisplayName(delegate.getDisplayName());
			tn.setRootId(delegate.getId());

			result.add(tn);
		}
		return result;
	}
	
	private String stripHeadAndTailChar(String s, String specialChar) {
        String data = s;
        if (data.startsWith(specialChar)) {
            data = data.substring(1);
        }

        if (data.endsWith(specialChar)) {
            data = data.substring(0, data.length() - 1);
        }

        return data;
    }

}
