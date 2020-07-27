package com.webank.wecube.platform.core.boot;

import java.util.Iterator;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

/**
 * 
 * @author gavin
 *
 */
class MysqlStatementInfoParser extends StatementInfoParser {

    public StatementInfo parseStatement(String singleSql, Long lineNum){
        MySqlStatementParser parser = new MySqlStatementParser(singleSql);
        SQLStatement sqlStatement = parser.parseStatement();
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        sqlStatement.accept(visitor);
        
        DbOperationType operType = null;
        if(SQLCreateTableStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Create;
        }else if(SQLInsertStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Insert;
        }else if(SQLDeleteStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Delete;
        }else if(SQLUpdateStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Update;
        }else if(SQLAlterTableStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Alter;
        }else if(SQLDropTableStatement.class.isAssignableFrom(sqlStatement.getClass())){
            operType = DbOperationType.Drop;
        }
        else{
            operType = DbOperationType.Any;
        }
        
        StatementInfo si = new StatementInfo(operType, getTableName(visitor), singleSql, lineNum);
        return si;
    }
    
    protected String getTableName(MySqlSchemaStatVisitor visitor){
        String tableName = null;
        Iterator<TableStat.Name> nameIt = visitor.getTables().keySet().iterator();
        if(nameIt.hasNext()){
            TableStat.Name name = visitor.getTables().keySet().iterator().next();
            tableName = name.getName();
        }
        
        return tableName;
    }

}
