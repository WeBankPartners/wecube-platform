package com.webank.wecube.platform.core.utils;

public class Constants {
    public static final String PACKAGE_NAMING_PATTERN = "[a-z0-9][a-z0-9-]{1,61}[a-z0-9]";
    public static final String PACKAGE_VERSION_PATTERN = "^v([0-9]){1,2}(\\.([0-9]){1,2}){1,3}";
    public static final String S3_BUCKET_NAMING_PATTERN = "[a-z0-9][a-z0-9-]{1,61}[a-z0-9]";
    public static final String MYSQL_SCHEMA_NAMING_PATTERN = "[a-z0-9][a-z0-9_]{1,62}[a-z0-9]";
    public static final String KEY_COLUMN_DELIMITER = "__";
    public static final String[] GLOBAL_SYSTEM_VARIABLES = { "ALLOCATE_HOST", "ALLOCATE_PORT", "DB_HOST", "DB_PORT",
            "DB_SCHEMA", "DB_USER", "DB_PWD", "CORE_ADDR", "BASE_MOUNT_PATH", "HTTP_PROXY", "HTTPS_PROXY" };
    public final static String SEPARATOR_OF_NAMES = "/";
    public final static String LEFT_BRACKET_STRING = "(";
    public final static String RIGHT_BRACKET_STRING = ")";

    public static final String MAPPING_TYPE_CONTEXT = "context";
    public static final String MAPPING_TYPE_ENTITY = "entity";
    public static final String MAPPING_TYPE_SYSTEM_VARIABLE = "system_variable";
    public static final String MAPPING_TYPE_CONSTANT = "constant";
    public static final String MAPPING_TYPE_OBJECT = "object";
    public static final String MAPPING_TYPE_ASSIGN = "assign";

    public static final String STRING_BOOL_YES = "Y";
    public static final String FIELD_REQUIRED = "Y";
    public static final String FIELD_NOT_REQUIRED = "N";
    public static final String ASYNC_SERVICE_SYMBOL = "Y";
    public static final String DATA_MULTIPLE = "Y";
    public static final String DATA_NOT_MULTIPLE = "N";
    
    public static final String DATA_SENSITIVE = "Y";
    public static final String DATA_NOT_SENSITIVE = "N";

    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";
    public static final String DATA_TYPE_OBJECT = "object";

    public static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    public static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;
    public static final String CALLBACK_PARAMETER_KEY = "callbackParameter";

    public static final String RESULT_CODE_OK = "0";
    public static final String RESULT_CODE_ERROR = "1";
    
    public static final String TYPE_INPUT = "INPUT";
    public static final String TYPE_OUTPUT = "OUTPUT";
    
    public static final String MAPPING_TYPE_NOT_AVAILABLE = "N/A";
    public static final String MAPPING_TYPE_CMDB_CI_TYPE = "CMDB_CI_TYPE";
    
    public static final String UNIQUE_IDENTIFIER = "id";
    public static final String VISUAL_FIELD = "displayName";
    
    public static final String CORE_OBJECT_ID_KEY = "coreObjectId";
    public static final String CORE_OBJECT_NAME_KEY = "coreObjectName";
    
    public static final String PREFIX_OBJECT_VAR_ID = "OV";
    public static final String PREFIX_PROPERTY_VAR_ID = "PV";
    public static final String PREFIX_LIST_VAR_ID = "EV";
    
    public static final String TASK_CATEGORY_SSTN = "SSTN";
    public static final String TASK_CATEGORY_SUTN = "SUTN";
    public static final String TASK_CATEGORY_SDTN = "SDTN";
    
    public static final String DYNAMIC_BIND_YES = "Y";
    public static final String DYNAMIC_BIND_NO = "N";
    
    public static final String PRE_CHECK_YES = "Y";
    public static final String PRE_CHECK_NO = "N";
    //###User scheduled task
    public static final String SCHEDULE_MODE_MONTHLY = "Monthly";
    public static final String SCHEDULE_MODE_WEEKLY = "Weekly";
    public static final String SCHEDULE_MODE_DAILY = "Daily";
    public static final String SCHEDULE_MODE_HOURLY = "Hourly";
    
    public static final String SCHEDULE_TASK_NEW = "New";
    public static final String SCHEDULE_TASK_READY = "Ready";
    public static final String SCHEDULE_TASK_RUNNING = "Running";
    public static final String SCHEDULE_TASK_STOPPED = "Stopped";
    public static final String SCHEDULE_TASK_DELETED = "Deleted";
    
    public static final String INTERFACE_TYPE_EXECUTION = "EXECUTION";
    public static final String INTERFACE_TYPE_APPROVAL = "APPROVAL";
    public static final String INTERFACE_TYPE_DYNAMICFORM = "DYNAMICFORM";
    
    public static final String TEMPORARY_ENTITY_ID_PREFIX = "OID-";
    
    public static final String HTTP_HEADER_OPERATION = "x-operation";
    
    public static final String DME_DELIMETER = "#DME#";
    public static final String DME_OPERATION_DELIMETER = "#DMEOP#";
    
    public static final String BIND_FLAG_YES = "Y";
    public static final String BIND_FLAG_NO = "N";
    
    public static final String PASSWORD_ENCRYPT_AES_PREFIX = "{AES}";
    public static final String PASSWORD_ENCRYPT_RAW_PREFIX = "{RAW}";
    
    public static final String SSH_AUTH_MODE_PASSWORD = "PASSWD";
    public static final String SSH_AUTH_MODE_KEY = "KEY";
    
    public static final String EDITION_ENTERPRISE = "enterprise";
    public static final String EDITION_COMMUNITY = "community";
    
    public static final String DATA_MANDATORY_YES = "Y";
    public static final String DATA_MANDATORY_NO = "N";
}
