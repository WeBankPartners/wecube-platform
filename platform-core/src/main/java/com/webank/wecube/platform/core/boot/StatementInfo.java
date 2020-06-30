package com.webank.wecube.platform.core.boot;

public class StatementInfo {
    private DbOperationType operType;
    private String tableName;
    private String statement;
    private Long lineNum;

    public StatementInfo() {
        super();
    }

    public StatementInfo(DbOperationType operType, String tableName, String statement, Long lineNum) {
        super();
        this.operType = operType;
        this.tableName = tableName;
        this.statement = statement;
        this.lineNum = lineNum;
    }

    public DbOperationType getOperType() {
        return operType;
    }

    public void setOperType(DbOperationType operType) {
        this.operType = operType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Long getLineNum() {
        return lineNum;
    }

    public void setLineNum(Long lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StatementInfo [operType=");
        builder.append(operType);
        builder.append(", tableName=");
        builder.append(tableName);
        builder.append(", statement=");
        builder.append(statement);
        builder.append(", lineNum=");
        builder.append(lineNum);
        builder.append("]");
        return builder.toString();
    }

}
