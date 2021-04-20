package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityDataDelegate {
    public static final String UNIQUE_IDENTIFIER = "id";
    public static final String VISUAL_FIELD = "displayName";
    private String id;
    private String displayName;
    private String queryAttrName;
    private Object queryAttrValue;
    private Map<String, Object> entityData;

    private String packageName;
    private String entityName;
    private EntityDataDelegate previousEntity;
    private List<EntityDataDelegate> succeedingEntities = new ArrayList<EntityDataDelegate>();

    public Object getAttributeValue(String attrName) {
        if (entityData == null) {
            return null;
        }
        return this.entityData.get(attrName);
    }

    public String getId() {
        return id;
    }
    
    public String getFullId(){
        if(previousEntity == null){
            return id;
        }
        
        return String.format("%s::%s", previousEntity.getFullId(), id);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Object> getEntityData() {
        return entityData;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEntityData(Map<String, Object> entityData) {
        this.entityData = entityData;
    }

    public String getQueryAttrName() {
        return queryAttrName;
    }

    public void setQueryAttrName(String queryAttrName) {
        this.queryAttrName = queryAttrName;
    }

    public Object getQueryAttrValue() {
        return queryAttrValue;
    }

    public void setQueryAttrValue(Object queryAttrValue) {
        this.queryAttrValue = queryAttrValue;
    }

    public boolean hasQueryAttribute() {
        return (this.queryAttrName != null);
    }

    public EntityDataDelegate getPreviousEntity() {
        return previousEntity;
    }

    public void setPreviousEntity(EntityDataDelegate previousEntity) {
        this.previousEntity = previousEntity;
        if (previousEntity != null) {
            previousEntity.addSucceedingEntities(this);
        }
    }

    public List<EntityDataDelegate> getSucceedingEntities() {
        return succeedingEntities;
    }

    public void setSucceedingEntities(List<EntityDataDelegate> succeedingEntities) {
        this.succeedingEntities = succeedingEntities;
    }

    public void addSucceedingEntities(EntityDataDelegate... succeedingEntities) {
        for (EntityDataDelegate entity : succeedingEntities) {
            if (entity == null) {
                continue;
            }

            if (contains(entity)) {
                continue;
            }

            this.succeedingEntities.add(entity);

            if (entity.getPreviousEntity() == null) {
                entity.setPreviousEntity(this);
            }
        }
    }

    public void removeSucceedingEntities(EntityDataDelegate... succeedingEntitiesToRemove) {
        for (EntityDataDelegate removeEntity : succeedingEntitiesToRemove) {
            if (removeEntity == null) {
                continue;
            }

            EntityDataDelegate entityToRemove = findFromSucceedings(removeEntity);
            if (entityToRemove != null) {
                this.succeedingEntities.remove(entityToRemove);
            }
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    private boolean contains(EntityDataDelegate entity) {
        for (EntityDataDelegate elmt : this.succeedingEntities) {
            if (elmt.equals(entity)) {
                return true;
            }
        }

        return false;
    }

    private EntityDataDelegate findFromSucceedings(EntityDataDelegate entity) {
        for (EntityDataDelegate succeed : this.getSucceedingEntities()) {
            if (succeed.equals(entity)) {
                return succeed;
            }
        }

        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityDataDelegate other = (EntityDataDelegate) obj;
        if (entityName == null) {
            if (other.entityName != null) {
                return false;
            }
        } else if (!entityName.equals(other.entityName)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!packageName.equals(other.packageName)) {
            return false;
        }
        return true;
    }

}
