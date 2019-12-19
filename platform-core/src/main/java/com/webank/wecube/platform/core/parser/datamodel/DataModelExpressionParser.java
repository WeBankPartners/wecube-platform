package com.webank.wecube.platform.core.parser.datamodel;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelLexer;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelParser;
import com.webank.wecube.platform.core.support.datamodel.dto.DataModelExpressionDto;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DataModelExpressionParser {
    public final static String FETCH_ALL = "ALL";
    public final static String FETCH_NONE = "NONE";

    public DataModelExpressionParser() {
    }

    public Queue<DataModelExpressionDto> parse(String expression) {

        if (StringUtils.containsAny(expression, ">~")) {
            // at least one link occurs in the expression
            Iterable<String> split = Splitter.onPattern("([>~])").split(expression);
            String last = Iterables.getLast(split);
            if (!last.contains(".")) {
                expression = expression + "." + DataModelExpressionParser.FETCH_ALL;
            }
        } else {
            if (!expression.contains(".")) {
                expression = expression + "." + DataModelExpressionParser.FETCH_ALL;
            }
        }

        Queue<DataModelExpressionDto> expressionQueue = checkExpressionSyntax(expression);
        List<DataModelExpressionDto> expressionDtoList = new ArrayList<>(expressionQueue);

        // check if the parser reach to the end of expression
        String lastAttrName = expressionDtoList.get(expressionDtoList.size() - 1).getOpFetch().attr().getText();
        Iterable<String> split = Splitter.on('.').split(expression);
        String expressionLastAttrName = Iterables.getLast(split);
        if (!expressionLastAttrName.equals(lastAttrName)) {
            String msg = "The parser cannot reach to the end of the input expression, please verify your expression is valid or not.";
            throw new WecubeCoreException(msg);
        }
        return expressionQueue;
    }

    private Queue<DataModelExpressionDto> checkExpressionSyntax(String expression) {
        CharStream inputStream = CharStreams.fromString(expression);
        DataModelLexer dataModelLexer = new DataModelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(dataModelLexer);
        DataModelParser parser = new DataModelParser(tokens);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws WecubeCoreException {
                throw new WecubeCoreException(String.format("Expression syntax error: line %d:%d %s", line, charPositionInLine, msg));
            }
        });
        ParseTree tree = parser.route();
        ParseTreeWalker walker = new ParseTreeWalker();
        DataModelExpressionListener evalByListener = new DataModelExpressionListener();
        walker.walk(evalByListener, tree);
        return evalByListener.getExpressionQueue();
    }

}
