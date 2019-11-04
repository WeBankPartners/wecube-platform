// Generated from /Users/howechen/GitHub/wecube-platform/platform-core/src/main/resources/DataModel.g4 by ANTLR 4.7.2
package com.webank.wecube.platform.core.parser.datamodel.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DataModelParser}.
 */
public interface DataModelListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DataModelParser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(DataModelParser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(DataModelParser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(DataModelParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(DataModelParser.OpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#link}.
	 * @param ctx the parse tree
	 */
	void enterLink(DataModelParser.LinkContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#link}.
	 * @param ctx the parse tree
	 */
	void exitLink(DataModelParser.LinkContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#node}.
	 * @param ctx the parse tree
	 */
	void enterNode(DataModelParser.NodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#node}.
	 * @param ctx the parse tree
	 */
	void exitNode(DataModelParser.NodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#attr}.
	 * @param ctx the parse tree
	 */
	void enterAttr(DataModelParser.AttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#attr}.
	 * @param ctx the parse tree
	 */
	void exitAttr(DataModelParser.AttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#pkg}.
	 * @param ctx the parse tree
	 */
	void enterPkg(DataModelParser.PkgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#pkg}.
	 * @param ctx the parse tree
	 */
	void exitPkg(DataModelParser.PkgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#pkg_name}.
	 * @param ctx the parse tree
	 */
	void enterPkg_name(DataModelParser.Pkg_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#pkg_name}.
	 * @param ctx the parse tree
	 */
	void exitPkg_name(DataModelParser.Pkg_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#entity}.
	 * @param ctx the parse tree
	 */
	void enterEntity(DataModelParser.EntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#entity}.
	 * @param ctx the parse tree
	 */
	void exitEntity(DataModelParser.EntityContext ctx);
}