// Generated from /Users/howechen/GitHub/wecube-platform/platform-core/src/main/resources/DataModel.g4 by ANTLR 4.7.2
package com.webank.wecube.platform.core.parser.datamodel.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DataModelParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DataModelVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DataModelParser#route}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoute(DataModelParser.RouteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#link}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLink(DataModelParser.LinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#fetch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch(DataModelParser.FetchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#to}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo(DataModelParser.ToContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBy(DataModelParser.ByContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#fwd_node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFwd_node(DataModelParser.Fwd_nodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#bwd_node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBwd_node(DataModelParser.Bwd_nodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#entity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity(DataModelParser.EntityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#pkg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg(DataModelParser.PkgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#ety}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEty(DataModelParser.EtyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr(DataModelParser.AttrContext ctx);
}