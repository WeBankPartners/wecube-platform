package com.webank.wecube.platform.core.parser.datamodel;

import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelBaseListener;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.DataModelExpressionHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DataModelExpressionListener extends DataModelBaseListener {

    private Queue<DataModelExpressionHelper> expressionQueue = new LinkedList<>();

    public Queue<DataModelExpressionHelper> getExpressionQueue() {
        return expressionQueue;
    }

    @Override
    public void exitLink(DataModelParser.LinkContext ctx) {
        List<DataModelParser.NodeContext> nodeList = ctx.node();
        DataModelExpressionHelper expressionHelper = new DataModelExpressionHelper();
        if (nodeList.size() == 2) {
            // link consist of two nodes
            expressionHelper.setFirstNode(nodeList.get(0));
            expressionHelper.setOp(ctx.op());
            expressionHelper.setSecondNode(nodeList.get(1));
        }

        if (nodeList.size() == 1) {
            // link consist of one link and one node
            expressionHelper.setExpression(ctx.getText());
            expressionHelper.setPreviousLink(ctx.link());
            expressionHelper.setOp(ctx.op());
            expressionHelper.setSecondNode(nodeList.get(0));
        }
        expressionQueue.add(expressionHelper);
        super.exitLink(ctx);
    }


}
