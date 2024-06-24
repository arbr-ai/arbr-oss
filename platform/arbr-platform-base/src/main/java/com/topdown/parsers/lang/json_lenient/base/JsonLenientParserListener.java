// Generated from /home/bill/arbr/arbr-oss/platform/arbr-platform-base/src/main/antlr/json_lenient/JsonLenientParser.g4 by ANTLR 4.13.1
package com.topdown.parsers.lang.json_lenient.base;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JsonLenientParser}.
 */
public interface JsonLenientParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JsonLenientParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(JsonLenientParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLenientParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(JsonLenientParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLenientParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(JsonLenientParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLenientParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(JsonLenientParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLenientParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(JsonLenientParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLenientParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(JsonLenientParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLenientParser#arr}.
	 * @param ctx the parse tree
	 */
	void enterArr(JsonLenientParser.ArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLenientParser#arr}.
	 * @param ctx the parse tree
	 */
	void exitArr(JsonLenientParser.ArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLenientParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JsonLenientParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLenientParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JsonLenientParser.ValueContext ctx);
}