package com.webank.wecube.platform.core.parser.datamodel;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelLexer;
import com.webank.wecube.platform.core.parser.datamodel.generated.DataModelParser;
import com.webank.wecube.platform.core.support.parser.DataModelExpressionHelper;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedList;
import java.util.Queue;

public class DataModelExpressionParser {

    public DataModelExpressionParser() {
    }

    public Queue<DataModelExpressionHelper> parse(String expression) {
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

    public static void main(String[] args){
        String expression = "C:c.c1-A:a.a1-B:b.b2-C:c.c2-D:d.d2-E:e.e2~F:f.f2-G:g.g1";
        DataModelExpressionParser dmeParser = new DataModelExpressionParser();
        Queue<DataModelExpressionHelper> resultQueue = null;
        try {
            resultQueue = dmeParser.parse(expression);
        } catch (WecubeCoreException ex) {
            System.out.println(ex.getMessage());
        }
        assert resultQueue != null;
        while (!resultQueue.isEmpty()){
            System.out.println(resultQueue.remove().toString());
        }
    }
}
