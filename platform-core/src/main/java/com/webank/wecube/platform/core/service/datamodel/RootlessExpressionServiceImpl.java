package com.webank.wecube.platform.core.service.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.DmeFilterDto;
import com.webank.wecube.platform.core.dto.plugin.DmeLinkFilterDto;
import com.webank.wecube.platform.core.dto.plugin.FilterDto;
import com.webank.wecube.platform.core.service.dme.EntityDataDelegate;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityOperationContext;
import com.webank.wecube.platform.core.service.dme.EntityOperationType;
import com.webank.wecube.platform.core.service.dme.EntityQueryExecutor;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.EntityQueryFilter;
import com.webank.wecube.platform.core.service.dme.EntityQueryLinkNode;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;
import com.webank.wecube.platform.core.utils.Constants;

@Service
public class RootlessExpressionServiceImpl implements RootlessExpressionService {

    private static final Logger log = LoggerFactory.getLogger(RootlessExpressionServiceImpl.class);

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    @Autowired
    @Qualifier("standardEntityQueryExecutor")
    private EntityQueryExecutor entityQueryExecutor;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected RestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    private EntityDataRouteFactory entityDataRouteFactory;

    public List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto) {
        if (log.isInfoEnabled()) {
            log.info("start to fetch data with filter:{}", dmeFilterDto);
        }
        List<EntityQueryExprNodeInfo> exprNodeInfos = entityQueryExpressionParser
                .parse(dmeFilterDto.getDataModelExpression());

        enrichEntityQueryExprNodeInfos(exprNodeInfos, dmeFilterDto);

        return doFetchDataWithFilter(exprNodeInfos);

    }

    private List<Object> doFetchDataWithFilter(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
            return Collections.emptyList();
        }

        EntityOperationContext ctx = buildEntityOperationContext(exprNodeInfos);
        ctx.setEntityOperationType(EntityOperationType.QUERY);

        List<EntityDataDelegate> entityDataDelegates = entityQueryExecutor.executeQueryLeafEntity(ctx);

        EntityQueryLinkNode tailLinkNode = ctx.getTailEntityQueryLinkNode();

        return extractResultData(tailLinkNode, entityDataDelegates);
    }

    private List<Object> extractResultData(EntityQueryLinkNode tailLinkNode,
            List<EntityDataDelegate> entityDataDelegates) {
        List<Object> results = new ArrayList<Object>();
        if (StringUtils.isBlank(tailLinkNode.getQueryAttributeName())) {
            List<String> addedIds = new ArrayList<>();
            for (EntityDataDelegate delegate : entityDataDelegates) {
                Map<String, Object> record = new HashMap<String, Object>();
                record.putAll(delegate.getEntityData());

                if (!addedIds.contains(record.get(Constants.UNIQUE_IDENTIFIER))) {
                    results.add(record);
                    addedIds.add((String) record.get(Constants.UNIQUE_IDENTIFIER));
                }
            }

            return results;
        } else {
            for (EntityDataDelegate delegate : entityDataDelegates) {
                Object val = delegate.getQueryAttrValue();
                if (!results.contains(val)) {
                    results.add(val);
                }
            }

            return results;
        }
    }

    protected EntityOperationContext buildEntityOperationContext(List<EntityQueryExprNodeInfo> exprNodeInfos) {
        EntityOperationContext ctx = new EntityOperationContext();
        ctx.setEntityQueryExprNodeInfos(exprNodeInfos);
        ctx.setOriginalEntityLinkExpression("");
        ctx.setOriginalEntityData(null);
        ctx.setStandardEntityOperationRestClient(new StandardEntityOperationRestClient(userJwtSsoTokenRestTemplate));
        ctx.setHeadEntityQueryLinkNode(entityQueryExecutor.buildEntityQueryLinkNode(exprNodeInfos));
        ctx.setEntityDataRouteFactory(entityDataRouteFactory);

        return ctx;
    }

    private void enrichEntityQueryExprNodeInfos(List<EntityQueryExprNodeInfo> exprNodeInfos,
            DmeFilterDto dmeFilterDto) {
        List<DmeLinkFilterDto> filters = dmeFilterDto.getFilters();
        if (filters == null || filters.isEmpty()) {
            return;
        }

        for (DmeLinkFilterDto filterDto : filters) {
            int index = filterDto.getIndex();

            EntityQueryExprNodeInfo exprNodeInfo = null;
            if (index < exprNodeInfos.size()) {
                exprNodeInfo = exprNodeInfos.get(index);
            }

            if (exprNodeInfo == null) {
                throw new WecubeCoreException(
                        String.format("Index is not correct.Index:%s, PackageName:%s, EntityName:%s", index,
                                filterDto.getPackageName(), filterDto.getEntityName()));
            }

            List<FilterDto> attributeFilters = filterDto.getAttributeFilters();
            if (attributeFilters != null) {
                for (FilterDto attributeFilter : attributeFilters) {
                    EntityQueryFilter f = new EntityQueryFilter();
                    f.setAttrName(attributeFilter.getName());
                    f.setOp(EntityQueryFilter.OP_EQUALS);
                    f.setCondition(attributeFilter.getValue());

                    exprNodeInfo.addAdditionalFilters(f);
                }
            }

        }
    }
}
