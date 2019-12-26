package com.webank.wecube.platform.core.utils;

public class Constants {
    public static final String PACKAGE_NAMING_PATTERN = "[a-z0-9][a-z0-9-]{1,61}[a-z0-9]";
    public static final String S3_BUCKET_NAMING_PATTERN = "[a-z0-9][a-z0-9-]{1,61}[a-z0-9]";
    public static final String MYSQL_SCHEMA_NAMING_PATTERN = "[a-z0-9][a-z0-9_]{1,62}[a-z0-9]";
    public static final String KEY_COLUMN_DELIMITER = "__";
    public static final String[] GLOBAL_SYSTEM_VARIABLES = { "ALLOCATE_HOST", "ALLOCATE_PORT", "DB_HOST", "DB_PORT",
            "DB_SCHEMA", "DB_USER", "DB_PWD", "CORE_ADDR", "CMDB_URL", "BASE_MOUNT_PATH" };
    public final static String SEPARATOR_OF_NAMES = "/";
    public final static String LEFT_BRACKET_STRING = "(";
    public final static String RIGHT_BRACKET_STRING = ")";

    public static final String MAPPING_TYPE_CONTEXT = "context";
    public static final String MAPPING_TYPE_ENTITY = "entity";
    public static final String MAPPING_TYPE_SYSTEM_VARIABLE = "system_variable";
    public static final String MAPPING_TYPE_CONSTANT = "constant";

    public static final String FIELD_REQUIRED = "Y";
    public static final String ASYNC_SERVICE_SYMBOL = "Y";

    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";

    public static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    public static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;
    public static final String CALLBACK_PARAMETER_KEY = "callbackParameter";

}
