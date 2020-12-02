//package com.webank.wecube.platform.core.jpa.impl;
//
//import static java.lang.reflect.Modifier.isStatic;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.persistence.EntityManager;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Expression;
//import javax.persistence.criteria.Root;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import com.google.common.collect.ImmutableSet;
//import com.webank.wecube.platform.core.dto.FilterRelationship;
//import com.webank.wecube.platform.core.dto.PageInfo;
//import com.webank.wecube.platform.core.dto.QueryRequestDto;
//import com.webank.wecube.platform.core.dto.QueryResponse;
//import com.webank.wecube.platform.core.jpa.EntityRepository;
//import com.webank.wecube.platform.core.utils.JpaQueryUtils;
//
//@Repository
//public class EntityRepositoryImpl implements EntityRepository {
//
//    @Autowired
//    private EntityManager entityManager;
//
//    @Override
//    public <T> QueryResponse query(Class<T> domainClzz, QueryRequestDto queryRequest) {
//        List<Field> fieldList = getDomainAttrFields(domainClzz);
//        List<Object> countResult = doQuery(domainClzz, queryRequest, true, fieldList);
//        int totalRow = convertResultToInteger(countResult);
//
//        List<Object> resultObjs = doQuery(domainClzz, queryRequest, false, fieldList);
//        List<Object> domainObjs = new LinkedList<>();
//
//        resultObjs.forEach(x -> domainObjs.add(x));
//
//        QueryResponse queryResp = new QueryResponse<T>();
//        queryResp.setContents(domainObjs);
//
//        if (queryRequest != null && queryRequest.getPageable() != null) {
//            queryResp.setPageInfo(new PageInfo(totalRow, queryRequest.getPageable().getStartIndex(), queryRequest.getPageable().getPageSize()));
//        } else {
//            queryResp.setPageInfo(new PageInfo(totalRow, 0, totalRow));
//        }
//
//        return queryResp;
//    }
//
//    private int convertResultToInteger(List rawResults) {
//        String strVal = rawResults.get(0).toString();
//        return Integer.valueOf(strVal);
//    }
//
//    private <T> List<Object> doQuery(Class<T> domainClazz, QueryRequestDto ciRequest, boolean isSelRowCount, List<Field> fieldList) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//
//        CriteriaQuery query = cb.createQuery(domainClazz);
//        Root<T> root = query.from(domainClazz);
//
//        Map<String, Expression> selectionMap = new LinkedHashMap<>();
//        for (Field field : fieldList) {
//            selectionMap.put(field.getName(), root.get(field.getName()));
//        }
//
//        if (isSelRowCount) {
//            query.select(cb.count(root));
//        } else {
//            query.select(root);
//        }
//
//        Map<String, Class<?>> fieldTypeMap = new HashMap<>();
//        for (Field field : fieldList) {
//            fieldTypeMap.put(field.getName(), field.getType());
//        }
//
//        if (ciRequest != null) {
//            if (ciRequest.getFilters() != null && ciRequest.getFilters().size() > 0) {
//                JpaQueryUtils.applyFilter(cb, query, ciRequest.getFilters(), selectionMap, fieldTypeMap, FilterRelationship.fromCode(ciRequest.getFilterRs()), null, null);
//            }
//
//            if (isSelRowCount == false) {
//                JpaQueryUtils.applySorting(ciRequest.getSorting(), cb, query, selectionMap);
//            }
//        }
//
//        TypedQuery typedQuery = entityManager.createQuery(query);
//
//        if (isSelRowCount == false && ciRequest != null) {
//            JpaQueryUtils.applyPaging(ciRequest.isPaging(), ciRequest.getPageable(), typedQuery);
//        }
//
//        List<Object> resultObjs = typedQuery.getResultList();
//        return resultObjs;
//    }
//
//    private List<Field> getDomainAttrFields(Class domainClazz) {
//        Field[] fields = domainClazz.getDeclaredFields();
//        List<Field> fieldList = new LinkedList<>();
//        Set<String> ignoredFields = ImmutableSet.<String>of("serialVersionUID", "logger");
//        for (Field field : fields) {
//            if (isStatic(field.getModifiers())) {
//                continue;
//            }
//            if (ignoredFields.contains(field.getName())) {
//                continue;
//            }
//            if (field.getType().equals(List.class)) {
//                continue;
//            }
//            fieldList.add(field);
//        }
//        return fieldList;
//    }
//
//}
