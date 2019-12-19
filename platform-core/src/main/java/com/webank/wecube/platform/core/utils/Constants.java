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
}
