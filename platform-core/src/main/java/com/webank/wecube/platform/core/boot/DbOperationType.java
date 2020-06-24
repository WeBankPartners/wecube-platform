package com.webank.wecube.platform.core.boot;

enum DbOperationType {
    Create, Alter, Insert, Update, Delete, Drop, Any;

    public static DbOperationType convert(String strOperationType) {
        DbOperationType operType = null;
        strOperationType = strOperationType.toLowerCase();
        switch (strOperationType) {
        case "create":
            operType = Create;
            break;
        case "alter":
            operType = Alter;
            break;
        case "insert":
            operType = Insert;
            break;
        case "update":
            operType = Update;
            break;
        case "delete":
            operType = Delete;
            break;
        case "drop":
            operType = Drop;
            break;
        default:
            operType = Any;
        }

        return operType;
    }
}
