package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.webank.wecube.platform.core.commons.WecubeCoreException;

/**
 * 
 * @author gavin
 *
 */
public class EntityQueryLinkNode {
    private int index;
    private boolean isHead;
    private EntityQueryLinkNode previousNode;
    private EntityQueryLinkNode succeedingNode;

    private EntityQueryExprNodeInfo exprNodeInfo;

    private EntityAttributeDefInfo attrDefInfo;

    private List<EntityDataDelegate> entityDataDelegates = new ArrayList<>();

    public EntityQueryLinkNode(EntityQueryLinkNode previousNode, EntityQueryLinkNode succeedingNode,
            EntityQueryExprNodeInfo exprNodeInfo) {
        this(previousNode, succeedingNode, exprNodeInfo, false);
    }

    public EntityQueryLinkNode(EntityQueryLinkNode previousNode, EntityQueryLinkNode succeedingNode,
            EntityQueryExprNodeInfo exprNodeInfo, boolean isHead) {
        super();
        this.previousNode = previousNode;
        this.succeedingNode = succeedingNode;
        this.exprNodeInfo = exprNodeInfo;
        this.isHead = isHead;
    }

    public EntityQueryLinkNode() {
    }

    public void executeQuery(StandardEntityQueryExecutor executor, EntityOperationContext ctx) {
        ctx.setCurrentEntityQueryLinkNode(this);
        executor.performQuery(ctx, this);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Object> extractFinalAttributeValues() {
        List<Object> attrValues = new ArrayList<>();
        if (!hasQueryAttribute()) {
            return attrValues;
        }

        for (EntityDataDelegate delegate : this.getEntityDataDelegates()) {
            if (delegate.getQueryAttrValue() != null) {
                if (delegate.getQueryAttrValue() instanceof Collection) {
                    validateMultipleData();
                    Collection<Object> c = (Collection) delegate.getQueryAttrValue();
                    attrValues.addAll(c);
                } else {
                    attrValues.add(delegate.getQueryAttrValue());
                }
            }
        }

        return attrValues;
    }

    private void validateMultipleData() {
        if (attrDefInfo == null) {
            return;
        }

        if (attrDefInfo.isMultiple()) {
            return;
        } else {
            String errMsg = String.format(
                    "Attribute[%s:%s:%s] declaration is not multiple kind data but got a collection kind data from expression calculation.",
                    attrDefInfo.getPackageName(), attrDefInfo.getEntityName(), attrDefInfo.getAttrName());
            throw new WecubeCoreException(errMsg);
        }
    }

    public boolean isHeadLinkNode() {
        return isHead | (previousNode == null);
    }

    public boolean isTailLinkNode() {
        return succeedingNode == null;
    }

    public boolean hasQueryAttribute() {
        return this.getExprNodeInfo().hasQueryAttribute();
    }

    public String getQueryAttributeName() {
        return this.getExprNodeInfo().getQueryAttrName();
    }

    public EntityQueryLinkNode addEntityDataDelegates(EntityDataDelegate... dataDelegates) {
        for (EntityDataDelegate d : dataDelegates) {
            if (d != null) {
                if (StringUtils.isBlank(d.getId())) {
                    throw new RuntimeException("Entity data should contain ID.");
                }
                // if (contains(d)) {
                // continue;
                // }

                this.entityDataDelegates.add(d);
            }
        }

        return this;
    }

    public EntityQueryLinkNode addAllEntityDataDelegates(List<EntityDataDelegate> dataDelegates) {
        for (EntityDataDelegate d : dataDelegates) {
            this.addEntityDataDelegates(d);
        }

        return this;
    }

    public boolean isHead() {
        return isHead;
    }

    public EntityQueryLinkNode getPreviousNode() {
        return previousNode;
    }

    public EntityQueryLinkNode getSucceedingNode() {
        return succeedingNode;
    }

    public EntityQueryExprNodeInfo getExprNodeInfo() {
        return exprNodeInfo;
    }

    public List<EntityDataDelegate> getEntityDataDelegates() {
        return Collections.unmodifiableList(entityDataDelegates);
    }

    protected boolean contains(EntityDataDelegate d) {
        for (EntityDataDelegate exist : this.entityDataDelegates) {
            if (exist == null) {
                continue;
            }
            if (d.getId().equals(exist.getId())) {
                return true;
            }
        }

        return false;
    }

    public void setHead(boolean isHead) {
        this.isHead = isHead;
    }

    public void setPreviousNode(EntityQueryLinkNode previousNode) {
        if (previousNode != null) {
            previousNode.setSucceedingNode(this);
        }
        this.previousNode = previousNode;
    }

    public void setSucceedingNode(EntityQueryLinkNode succeedingNode) {
        this.succeedingNode = succeedingNode;
    }

    public void setExprNodeInfo(EntityQueryExprNodeInfo exprNodeInfo) {
        this.exprNodeInfo = exprNodeInfo;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public EntityAttributeDefInfo getAttrDefInfo() {
        return attrDefInfo;
    }

    public void setAttrDefInfo(EntityAttributeDefInfo attrDefInfo) {
        this.attrDefInfo = attrDefInfo;
    }

}
