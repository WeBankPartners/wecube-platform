package com.webank.wecube.platform.core.commons;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class XPathEvaluator {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static XPathFactory xPathFactory = XPathFactory.newInstance();

    private Document document;
    private XPath xPath;

    public static XPathEvaluator newInstance(InputSource inputSource) throws ParserConfigurationException, IOException, SAXException {
        return new XPathEvaluator(inputSource);
    }

    private XPathEvaluator(InputSource inputSource) throws ParserConfigurationException, IOException, SAXException {
        document = documentBuilderFactory.newDocumentBuilder().parse(inputSource);
        xPath = xPathFactory.newXPath();
    }

    private Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        return xPath.evaluate(expression, item, returnType);
    }

    public String getString(String expression, Object node) throws XPathExpressionException {
        return (String) evaluate(expression, node, XPathConstants.STRING);
    }

    public String getString(String expression) throws XPathExpressionException {
        return (String) evaluate(expression, document, XPathConstants.STRING);
    }

    public Boolean getBoolean(String expression) throws XPathExpressionException {
        return (Boolean) xPath.evaluate(expression, document, XPathConstants.BOOLEAN);
    }

    public Boolean getBoolean(String expression, Object node) throws XPathExpressionException {
        return (Boolean) xPath.evaluate(expression, node, XPathConstants.BOOLEAN);
    }

    public Number getNumber(String expression, Object node) throws XPathExpressionException {
        return (Number) evaluate(expression, node, XPathConstants.NUMBER);
    }

    public Number getNumber(String expression) throws XPathExpressionException {
        return (Number) evaluate(expression, document, XPathConstants.NUMBER);
    }

    public Node getNode(String expression) throws XPathExpressionException {
        return (Node) evaluate(expression, document, XPathConstants.NODE);
    }

    public Node getNode(String expression, Object node) throws XPathExpressionException {
        return (Node) evaluate(expression, node, XPathConstants.NODE);
    }

    public NodeList getNodeList(String expression) throws XPathExpressionException {
        return (NodeList) evaluate(expression, document, XPathConstants.NODESET);
    }

    public NodeList getNodeList(String expression, Object node) throws XPathExpressionException {
        return (NodeList) evaluate(expression, node, XPathConstants.NODESET);
    }

}
