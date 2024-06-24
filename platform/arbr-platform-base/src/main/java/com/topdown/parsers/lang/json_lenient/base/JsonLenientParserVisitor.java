// Generated from /home/bill/arbr/arbr-oss/platform/arbr-platform-base/src/main/antlr/json_lenient/JsonLenientParser.g4 by ANTLR 4.13.1
package com.topdown.parsers.lang.json_lenient.base;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JsonLenientParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JsonLenientParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JsonLenientParser#json}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson(JsonLenientParser.JsonContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLenientParser#obj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObj(JsonLenientParser.ObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLenientParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(JsonLenientParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLenientParser#arr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArr(JsonLenientParser.ArrContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLenientParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(JsonLenientParser.ValueContext ctx);
}