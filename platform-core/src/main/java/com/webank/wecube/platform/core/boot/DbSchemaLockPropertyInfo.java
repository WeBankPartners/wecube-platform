package com.webank.wecube.platform.core.boot;

class DbSchemaLockPropertyInfo extends AppPropertyInfo {
    public static final String PROPERTY_NAME = "db.schema.lock";
    public static final String VAL_UNLOCK = "0";
    public static final String VAL_LOCK = "1";

    public DbSchemaLockPropertyInfo() {
        super();
    }

    public DbSchemaLockPropertyInfo(String val, Integer rev) {
        super(PROPERTY_NAME, val, rev);
    }
    
    public boolean isUnlocked(){
        return VAL_UNLOCK.equals(this.getVal());
    }

}
