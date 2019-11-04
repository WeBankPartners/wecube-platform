package com.webank.wecube.platform.core.support.parser;

import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DataModelExpressionHelper {
    private String expression;
    private DataModelParser.LinkContext previousLink;
    private DataModelParser.NodeContext firstNode;
    private DataModelParser.OpContext op;
    private DataModelParser.NodeContext secondNode;

    public DataModelExpressionHelper() {
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public DataModelParser.LinkContext getPreviousLink() {
        return previousLink;
    }

    public void setPreviousLink(DataModelParser.LinkContext previousLink) {
        this.previousLink = previousLink;
    }

    public DataModelParser.NodeContext getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(DataModelParser.NodeContext firstNode) {
        this.firstNode = firstNode;
    }


    public DataModelParser.OpContext getOp() {
        return op;
    }

    public void setOp(DataModelParser.OpContext op) {
        this.op = op;
    }

    public DataModelParser.NodeContext getSecondNode() {
        return secondNode;
    }

    public void setSecondNode(DataModelParser.NodeContext secondNode) {
        this.secondNode = secondNode;
    }

    @Override
    public String toString() {
        return (String.format(
                "==========\n\t" +
                        "expression: %s\n\t" +
                        "previousLink: %s\n\t" +
                        "firstNode: %s\n\t" +
                        "op: %s\n\t" +
                        "secondNode: %s",
                null == this.getExpression() ? "null" : this.getExpression(),
                null == this.getPreviousLink() ? "null" : this.getPreviousLink().getText(),
                null == this.getFirstNode() ? "null" : this.getFirstNode().getText(),
                null == this.getOp() ? "null" : this.getOp().getText(),
                null == this.getSecondNode() ? "null" : this.getSecondNode().getText()));
    }


}
