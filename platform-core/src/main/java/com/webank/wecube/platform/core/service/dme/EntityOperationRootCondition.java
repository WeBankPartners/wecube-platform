package com.webank.wecube.platform.core.service.dme;

public class EntityOperationRootCondition {
    private String entityLinkExpr;
    private String entityIdentity;

    public EntityOperationRootCondition() {
        super();
    }

    public EntityOperationRootCondition(String entityLinkExpr, String entityIdentity) {
        super();
        this.entityLinkExpr = entityLinkExpr;
        this.entityIdentity = entityIdentity;
    }

    public String getEntityLinkExpr() {
        return entityLinkExpr;
    }

    public void setEntityLinkExpr(String entityLinkExpr) {
        this.entityLinkExpr = entityLinkExpr;
    }

    public String getEntityIdentity() {
        return entityIdentity;
    }

    public void setEntityIdentity(String entityIdentity) {
        this.entityIdentity = entityIdentity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[entityLinkExpr=");
        builder.append(entityLinkExpr);
        builder.append(", entityIdentity=");
        builder.append(entityIdentity);
        builder.append("]");
        return builder.toString();
    }
}
