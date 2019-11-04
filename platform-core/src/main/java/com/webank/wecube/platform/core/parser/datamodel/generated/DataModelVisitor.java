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
	 * Visit a parse tree produced by {@link DataModelParser#op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp(DataModelParser.OpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#link}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLink(DataModelParser.LinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode(DataModelParser.NodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr(DataModelParser.AttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#pkg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg(DataModelParser.PkgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#pkg_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg_name(DataModelParser.Pkg_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#entity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity(DataModelParser.EntityContext ctx);
}