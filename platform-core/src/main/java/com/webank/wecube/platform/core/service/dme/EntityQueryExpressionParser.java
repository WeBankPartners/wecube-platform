package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.utils.Constants;
/**
 * 
 * @author gavin
 *
 */
@Service("entityQueryExpressionParser")
public class EntityQueryExpressionParser {
    public static final String PKG_DELIMITER = ":";
    public static final String REG_ENTITY_ID = "@@\\w+@@";
    public static final String DME_OPERATION_DELIMITER = Constants.DME_OPERATION_DELIMETER;
    private Pattern entityIdPattern = Pattern.compile(REG_ENTITY_ID);

    public EntityQueryExpr parse(String rawExpr) {
    	
    	if(StringUtils.isBlank(rawExpr)) {
    		throw new IllegalArgumentException("Expression to parse cannot be blank.");
    	}
    	
    	String[] rawExprParts = rawExpr.trim().split(DME_OPERATION_DELIMITER);
    	
    	EntityQueryExpr entityQueryExpr = new EntityQueryExpr();
    	entityQueryExpr.setRawExpr(rawExpr);
    	
    	String expr = null;
    	if(rawExprParts.length >= 2) {
    		expr = rawExprParts[0];
    		entityQueryExpr.setExprOperation(rawExprParts[1]);
    	}else {
    		expr = rawExpr;
    	}

        String exprOpReg = String.format("[%s%s]", EntityLinkType.REF_TO.symbol(), EntityLinkType.REF_BY.symbol());

        Pattern p = Pattern.compile(exprOpReg);
        Matcher m = p.matcher(expr);
        int start = 0;

        List<EntityQueryExprNodeInfo> queryNodeInfos = new ArrayList<>();
        if (StringUtils.containsAny(expr, EntityLinkType.REF_TO.symbol(), EntityLinkType.REF_BY.symbol())) {
            String currOpTypeStr = null;
            while (m.find()) {

                EntityQueryExprNodeInfo nodeInfo = new EntityQueryExprNodeInfo();
                if (currOpTypeStr == null) {
                    nodeInfo.setEntityLinkType(null);
                    nodeInfo.setHeadEntity(true);
                } else {
                    nodeInfo.setEntityLinkType(EntityLinkType.entityLinkType(currOpTypeStr));
                    nodeInfo.setHeadEntity(false);
                }

                String nodeExpr = expr.substring(start, m.start());
                nodeInfo.setEntityQueryNodeExpr(nodeExpr);

                currOpTypeStr = m.group();
                start = m.end();

                parseEntityQueryNodeInfoDetails(nodeInfo, nodeExpr);
                queryNodeInfos.add(nodeInfo);
            }

            String lastNodeExpr = expr.substring(start);
            EntityQueryExprNodeInfo lastNodeInfo = new EntityQueryExprNodeInfo();
            lastNodeInfo.setEntityQueryNodeExpr(lastNodeExpr);

            lastNodeInfo.setEntityLinkType(EntityLinkType.entityLinkType(currOpTypeStr));
            lastNodeInfo.setHeadEntity(false);

            parseEntityQueryNodeInfoDetails(lastNodeInfo, lastNodeExpr);
            queryNodeInfos.add(lastNodeInfo);
        } else {
            EntityQueryExprNodeInfo singleNodeInfo = new EntityQueryExprNodeInfo();
            singleNodeInfo.setHeadEntity(true);
            singleNodeInfo.setEntityQueryNodeExpr(expr);
            singleNodeInfo.setEntityLinkType(null);

            parseEntityQueryNodeInfoDetails(singleNodeInfo, expr);
            queryNodeInfos.add(singleNodeInfo);
        }
        
        entityQueryExpr.setExprNodeInfos(queryNodeInfos);
        return entityQueryExpr;
    }

    protected void parseEntityQueryNodeInfoDetails(EntityQueryExprNodeInfo nodeInfo, String entityQueryNodeExpr) {
        String expr = entityQueryNodeExpr;
        
        if(nodeInfo.getEntityLinkType() == EntityLinkType.REF_BY){
            if(!expr.startsWith("(")){
                throw new IllegalArgumentException("should starts with parentheses");
            }
            
            String refByAttrName = expr.substring(1, expr.indexOf(")"));
            nodeInfo.setRefByAttrName(refByAttrName);
            
            expr = expr.substring(expr.indexOf(")")+1);
        }
        
        if(expr.indexOf(PKG_DELIMITER) > 0){
            String packageName = expr.substring(0, expr.indexOf(PKG_DELIMITER));
            nodeInfo.setPackageName(packageName);
            
            expr = expr.substring(expr.indexOf(PKG_DELIMITER) + 1);
        }
        
       
        
        int idxFirstBraceSt = expr.indexOf("{");
        int idxLastBraceEd = expr.lastIndexOf("}");
        
        String attrNameExpr = null;
        
        if(idxFirstBraceSt > 0 && (idxLastBraceEd > 0) && (idxLastBraceEd > idxFirstBraceSt)) {
        	String filterExpr = expr.substring(idxFirstBraceSt,idxLastBraceEd+1);
            nodeInfo.setEntityFilterExpr(filterExpr);
            
            expr = expr.substring(0,idxFirstBraceSt);
            nodeInfo.setEntityInfoExpr(entityQueryNodeExpr.substring(0, entityQueryNodeExpr.indexOf("{")) );
            
            parseAdditionalFilters(nodeInfo, filterExpr);
            
            attrNameExpr = entityQueryNodeExpr.substring(entityQueryNodeExpr.lastIndexOf("}")+1);
            
            if(attrNameExpr != null && attrNameExpr.indexOf(".") >= 0){
                String attrName = attrNameExpr.substring(attrNameExpr.indexOf(".") + 1);
                nodeInfo.setQueryAttrName(attrName);
            }
        }else {
        	nodeInfo.setEntityInfoExpr(entityQueryNodeExpr);
        	
        	if(expr.indexOf(".") >= 0){
                String attrName = expr.substring(expr.indexOf(".") + 1);
                nodeInfo.setQueryAttrName(attrName);
                
                expr = expr.substring(0, expr.indexOf("."));
            }
        }
        
//        if(nodeInfo.getQueryAttrName() == null && expr.indexOf(".") > 0){
//            String attrName = expr.substring(expr.lastIndexOf(".") + 1);
//            nodeInfo.setQueryAttrName(attrName);
//        }
        
        nodeInfo.setEntityName(expr);
    }
    
    protected void parseAdditionalFilters(EntityQueryExprNodeInfo nodeInfo, String filtersExpr){
        Pattern filterPattern = Pattern.compile("\\{([^}\t\r\n])*?\\}");
        Matcher filterMatcher = filterPattern.matcher(filtersExpr);
        
        while (filterMatcher.find()) {
            String filerStr = filterMatcher.group();
            EntityQueryFilter filter = buildEntityQueryFilter(filerStr);
            nodeInfo.addAdditionalFilters(filter);
        }
    }
    
    public EntityQueryFilter buildEntityQueryFilter(String filterExpr) {
        if (!Pattern.matches("^\\{([^}\t\r\n])*?\\}$", filterExpr)) {
            return null;
        }

        String expr = filterExpr;
        if (expr.startsWith("{")) {
            expr = expr.substring(1);
        }

        if (expr.endsWith("}")) {
            expr = expr.substring(0, expr.length() - 1);
        }

        String splitReg = "\\s+";
        Pattern p = Pattern.compile(splitReg);
        Matcher m = p.matcher(expr);

        String attrName = null;
        int opStart = -1;
        if (m.find()) {
            attrName = expr.substring(0, m.start());
            opStart = m.end();
        }

        String op = null;
        String condExpr = null;
        if (m.find()) {
            op = expr.substring(opStart, m.start());
            condExpr = expr.substring(m.end());
        }

        EntityQueryFilter f = new EntityQueryFilter();
        f.setAttrName(attrName);
        f.setCondition(buildCondition(op, condExpr));
        f.setOp(op);
        return f;
    }
    
    private Object buildCondition(String op, String condExpr) {
        if (EntityQueryFilter.OP_EQUALS.equalsIgnoreCase(op) || EntityQueryFilter.OP_NOT_EQUALS.equalsIgnoreCase(op)
                || EntityQueryFilter.OP_LESS_THAN.equalsIgnoreCase(op) || EntityQueryFilter.OP_LIKE.equalsIgnoreCase(op)
                || EntityQueryFilter.OP_GREAT_THAN.equalsIgnoreCase(op)) {
            String conditionExpr = stripHeadAndTailChar(condExpr, "'");
            return tryCalculateConditionExpr(conditionExpr);
        }
        
        if(EntityQueryFilter.OP_IS.equalsIgnoreCase(op) || EntityQueryFilter.OP_IS_NOT.equalsIgnoreCase(op)){
            String conditionExpr = stripHeadAndTailChar(condExpr, "'");
            return conditionExpr;
        }

        if (EntityQueryFilter.OP_IN.equalsIgnoreCase(op)) {
            return buildConditionList(condExpr);
        }

        return null;
    }
    
    private String tryCalculateConditionExpr(String conditionExpr){
        if(conditionExpr == null || conditionExpr.trim().length() <= 0 ){
            return "";
        }
        Matcher m = entityIdPattern.matcher(conditionExpr);
        if(m.find()){
            String entityId = m.group();
            entityId = entityId.substring(2,entityId.length()-2);
            return entityId;
        }else{
            return conditionExpr;
        }
        
    }
    
    private List<String> buildConditionList(String listExpr) {
        String condExpr = stripHeadAndTailChar(listExpr, "[");
        condExpr = stripHeadAndTailChar(condExpr, "]");
        List<String> inConditions = new ArrayList<String>();
        String[] parts = condExpr.split(",");
        for (String part : parts) {
            String inCondition = stripHeadAndTailChar(part, "'");
            inConditions.add(tryCalculateConditionExpr(inCondition));
        }

        return inConditions;
    }

    private String stripHeadAndTailChar(String s, String specialChar) {
        String data = s;
        if (data.startsWith(specialChar)) {
            data = data.substring(1);
        }

        if (data.endsWith(specialChar)) {
            data = data.substring(0, data.length() - 1);
        }

        return data;
    }

}
