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
	 * Enter a parse tree produced by {@link DataModelParser#fetch}.
	 * @param ctx the parse tree
	 */
	void enterFetch(DataModelParser.FetchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#fetch}.
	 * @param ctx the parse tree
	 */
	void exitFetch(DataModelParser.FetchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#to}.
	 * @param ctx the parse tree
	 */
	void enterTo(DataModelParser.ToContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#to}.
	 * @param ctx the parse tree
	 */
	void exitTo(DataModelParser.ToContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#by}.
	 * @param ctx the parse tree
	 */
	void enterBy(DataModelParser.ByContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#by}.
	 * @param ctx the parse tree
	 */
	void exitBy(DataModelParser.ByContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#fwd_node}.
	 * @param ctx the parse tree
	 */
	void enterFwd_node(DataModelParser.Fwd_nodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#fwd_node}.
	 * @param ctx the parse tree
	 */
	void exitFwd_node(DataModelParser.Fwd_nodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#bwd_node}.
	 * @param ctx the parse tree
	 */
	void enterBwd_node(DataModelParser.Bwd_nodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#bwd_node}.
	 * @param ctx the parse tree
	 */
	void exitBwd_node(DataModelParser.Bwd_nodeContext ctx);
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
	 * Enter a parse tree produced by {@link DataModelParser#ety}.
	 * @param ctx the parse tree
	 */
	void enterEty(DataModelParser.EtyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#ety}.
	 * @param ctx the parse tree
	 */
	void exitEty(DataModelParser.EtyContext ctx);
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
}