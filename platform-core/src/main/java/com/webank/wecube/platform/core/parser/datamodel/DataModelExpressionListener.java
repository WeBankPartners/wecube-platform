package com.webank.wecube.platform.core.parser.datamodel;

import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelBaseListener;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.datamodel.DataModelExpressionDto;

import java.util.LinkedList;
import java.util.Queue;

public class DataModelExpressionListener extends DataModelBaseListener {

    private Queue<DataModelExpressionDto> expressionQueue = new LinkedList<>();

    public Queue<DataModelExpressionDto> getExpressionQueue() {
        return expressionQueue;
    }

    @Override
    public void exitRoute(DataModelParser.RouteContext ctx) {
        DataModelExpressionDto dataModelExpressionDto = null;
        if (ctx.link() != null) {
            // "prevLink fetch"
            dataModelExpressionDto = new DataModelExpressionDto(ctx.link(), ctx.fetch());

        }
        if (ctx.entity() != null) {
            // "entity fetch"
            dataModelExpressionDto = new DataModelExpressionDto(ctx.entity(), ctx.fetch());
        }
        assert dataModelExpressionDto != null;
        dataModelExpressionDto.setExpression(ctx.getText());
        expressionQueue.add(dataModelExpressionDto);

        super.exitRoute(ctx);
    }

    @Override
    public void exitLink(DataModelParser.LinkContext ctx) {
        DataModelExpressionDto dataModelExpressionDto;
        if (ctx.to() != null) {
            // "to" link with fwdNode/prevLink and entity
            // "fwdNode fetch opTo entity" or "prevLink fetch opTo entity"
            dataModelExpressionDto = new DataModelExpressionDto(ctx.link(), ctx.fwd_node(), ctx.fetch(), ctx.to(), ctx.entity());
        } else {
            // "by" link with entity/prevLink and bkwNode
            // "entity opBy bwdNode" or "prevLink opBy bwdNode"
            dataModelExpressionDto = new DataModelExpressionDto(ctx.link(), ctx.entity(), ctx.by(), ctx.bwd_node());
        }
        dataModelExpressionDto.setExpression(ctx.getText());
        expressionQueue.add(dataModelExpressionDto);
        super.exitLink(ctx);
    }


}
