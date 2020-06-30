package com.webank.wecube.platform.auth.server.boot;

class MysqlStatementInfoParser extends StatementInfoParser {

    @Override
    public StatementInfo parseStatement(String statement, Long lineNum) {
        String trimStatement = statement.trim();
        int index = trimStatement.indexOf(" ");
        if (index <= 0) {
            index = trimStatement.indexOf("\n");
        }

        String strOperType = trimStatement.substring(0, index);
        DbOperationType operType = DbOperationType.convert(strOperType);
        if (DbOperationType.Create == operType) {
            return parseCreateStatemennt(statement, lineNum, trimStatement);
        }

        StatementInfo si = new StatementInfo(DbOperationType.Any, "", statement, lineNum);
        return si;
    }

    private StatementInfo parseCreateStatemennt(String statement, Long lineNum, String trimStatement) {
        trimStatement = trimStatement.replaceAll("\\s+|\n", " ").toLowerCase();
        int st = trimStatement.indexOf("table");
        st = st + "table".length();
        int ed = trimStatement.indexOf("(");

        String tableName = trimStatement.substring(st, ed);
        tableName = tableName.replace("`", "").trim();
        StatementInfo si = new StatementInfo(DbOperationType.Create, tableName, statement, lineNum);
        return si;
    }

}
