package com.webank.wecube.platform.core.parser.datamodel;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelLexer;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.datamodel.DataModelExpressionDto;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DataModelExpressionParser {
    public final static String FETCH_ALL = "ALL";

    public DataModelExpressionParser() {
    }

    public Queue<DataModelExpressionDto> parse(String expression) {
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
        Queue<DataModelExpressionDto> expressionQueue = evalByListener.getExpressionQueue();

        // check if the parser reach to the end of expression
        List<DataModelExpressionDto> expressionDtoList = new ArrayList<>(expressionQueue);
        String lastAttrName = expressionDtoList.get(expressionDtoList.size() - 1).getOpFetch().attr().getText();
        Iterable<String> split = Splitter.on('.').split(expression);
        String expressionLastAttrName = Iterables.getLast(split);
        if (!expressionLastAttrName.equals(lastAttrName)) {
            String msg = "The parser cannot reach to the end of the input expression, please verify your expression is valid or not.";
            throw new WecubeCoreException(msg);
        }
        return expressionQueue;
    }

    public static void main(String[] args) {
//        String expression = "C:c.c1-A:a.a1-B:b.b2-C:c.c2-D:d.d2-E:e.e2~F:f.f2-G:g.g1";
        String expression = "F:f~(a2)A:a.a1-B:b.b2-D:d.d2";
        DataModelExpressionParser dmeParser = new DataModelExpressionParser();
        Queue<DataModelExpressionDto> resultQueue = null;
        try {
            resultQueue = dmeParser.parse(expression);
        } catch (WecubeCoreException ex) {
            System.out.println(ex.getMessage());
        }
        assert resultQueue != null;
    }

}
