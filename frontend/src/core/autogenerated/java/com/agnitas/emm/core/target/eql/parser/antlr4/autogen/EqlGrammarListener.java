/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

// Generated from EqlGrammar.g4 by ANTLR 4.4

package com.agnitas.emm.core.target.eql.parser.antlr4.autogen;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EqlGrammarParser}.
 */
public interface EqlGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code PositiveSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void enterPositiveSignedFactor(@NotNull EqlGrammarParser.PositiveSignedFactorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PositiveSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void exitPositiveSignedFactor(@NotNull EqlGrammarParser.PositiveSignedFactorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TermExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTermExpression(@NotNull EqlGrammarParser.TermExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TermExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTermExpression(@NotNull EqlGrammarParser.TermExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DivTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void enterDivTerm(@NotNull EqlGrammarParser.DivTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DivTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void exitDivTerm(@NotNull EqlGrammarParser.DivTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterEmptyRelationalExpression(@NotNull EqlGrammarParser.EmptyRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitEmptyRelationalExpression(@NotNull EqlGrammarParser.EmptyRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code GeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterGeqRelationalRelationalExpression(@NotNull EqlGrammarParser.GeqRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code GeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitGeqRelationalRelationalExpression(@NotNull EqlGrammarParser.GeqRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#relational_infix_op}.
	 * @param ctx the parse tree
	 */
	void enterRelational_infix_op(@NotNull EqlGrammarParser.Relational_infix_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#relational_infix_op}.
	 * @param ctx the parse tree
	 */
	void exitRelational_infix_op(@NotNull EqlGrammarParser.Relational_infix_opContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LtRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterLtRelationalRelationalExpression(@NotNull EqlGrammarParser.LtRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LtRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitLtRelationalRelationalExpression(@NotNull EqlGrammarParser.LtRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#constant_listelement}.
	 * @param ctx the parse tree
	 */
	void enterConstant_listelement(@NotNull EqlGrammarParser.Constant_listelementContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#constant_listelement}.
	 * @param ctx the parse tree
	 */
	void exitConstant_listelement(@NotNull EqlGrammarParser.Constant_listelementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenthesisFactor}
	 * labeled alternative in {@link EqlGrammarParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisFactor(@NotNull EqlGrammarParser.ParenthesisFactorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenthesisFactor}
	 * labeled alternative in {@link EqlGrammarParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisFactor(@NotNull EqlGrammarParser.ParenthesisFactorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ReceivedMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterReceivedMailingRelationalExpression(@NotNull EqlGrammarParser.ReceivedMailingRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReceivedMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitReceivedMailingRelationalExpression(@NotNull EqlGrammarParser.ReceivedMailingRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumericConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterNumericConstantAtom(@NotNull EqlGrammarParser.NumericConstantAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumericConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitNumericConstantAtom(@NotNull EqlGrammarParser.NumericConstantAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotBooleanNotExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_not_expression}.
	 * @param ctx the parse tree
	 */
	void enterNotBooleanNotExpression(@NotNull EqlGrammarParser.NotBooleanNotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotBooleanNotExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_not_expression}.
	 * @param ctx the parse tree
	 */
	void exitNotBooleanNotExpression(@NotNull EqlGrammarParser.NotBooleanNotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrBooleanOrExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_or_expression}.
	 * @param ctx the parse tree
	 */
	void enterOrBooleanOrExpression(@NotNull EqlGrammarParser.OrBooleanOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrBooleanOrExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_or_expression}.
	 * @param ctx the parse tree
	 */
	void exitOrBooleanOrExpression(@NotNull EqlGrammarParser.OrBooleanOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenthesisBooleanNotExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_not_expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisBooleanNotExpression(@NotNull EqlGrammarParser.ParenthesisBooleanNotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenthesisBooleanNotExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_not_expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisBooleanNotExpression(@NotNull EqlGrammarParser.ParenthesisBooleanNotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#constant_listelements}.
	 * @param ctx the parse tree
	 */
	void enterConstant_listelements(@NotNull EqlGrammarParser.Constant_listelementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#constant_listelements}.
	 * @param ctx the parse tree
	 */
	void exitConstant_listelements(@NotNull EqlGrammarParser.Constant_listelementsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ContainsRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterContainsRelationalExpression(@NotNull EqlGrammarParser.ContainsRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ContainsRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitContainsRelationalExpression(@NotNull EqlGrammarParser.ContainsRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code QuotedProfilefieldName}
	 * labeled alternative in {@link EqlGrammarParser#profilefield}.
	 * @param ctx the parse tree
	 */
	void enterQuotedProfilefieldName(@NotNull EqlGrammarParser.QuotedProfilefieldNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code QuotedProfilefieldName}
	 * labeled alternative in {@link EqlGrammarParser#profilefield}.
	 * @param ctx the parse tree
	 */
	void exitQuotedProfilefieldName(@NotNull EqlGrammarParser.QuotedProfilefieldNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FactorSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void enterFactorSignedFactor(@NotNull EqlGrammarParser.FactorSignedFactorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FactorSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void exitFactorSignedFactor(@NotNull EqlGrammarParser.FactorSignedFactorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringSingleQuote}
	 * labeled alternative in {@link EqlGrammarParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void enterStringSingleQuote(@NotNull EqlGrammarParser.StringSingleQuoteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringSingleQuote}
	 * labeled alternative in {@link EqlGrammarParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void exitStringSingleQuote(@NotNull EqlGrammarParser.StringSingleQuoteContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_expression(@NotNull EqlGrammarParser.Boolean_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_expression(@NotNull EqlGrammarParser.Boolean_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ProfileFieldAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterProfileFieldAtom(@NotNull EqlGrammarParser.ProfileFieldAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ProfileFieldAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitProfileFieldAtom(@NotNull EqlGrammarParser.ProfileFieldAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void enterMulTerm(@NotNull EqlGrammarParser.MulTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void exitMulTerm(@NotNull EqlGrammarParser.MulTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NegativeSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void enterNegativeSignedFactor(@NotNull EqlGrammarParser.NegativeSignedFactorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegativeSignedFactor}
	 * labeled alternative in {@link EqlGrammarParser#signed_factor}.
	 * @param ctx the parse tree
	 */
	void exitNegativeSignedFactor(@NotNull EqlGrammarParser.NegativeSignedFactorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAddExpression(@NotNull EqlGrammarParser.AddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAddExpression(@NotNull EqlGrammarParser.AddExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SignedFactorTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void enterSignedFactorTerm(@NotNull EqlGrammarParser.SignedFactorTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SignedFactorTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void exitSignedFactorTerm(@NotNull EqlGrammarParser.SignedFactorTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ModTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void enterModTerm(@NotNull EqlGrammarParser.ModTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ModTerm}
	 * labeled alternative in {@link EqlGrammarParser#term}.
	 * @param ctx the parse tree
	 */
	void exitModTerm(@NotNull EqlGrammarParser.ModTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterEqRelationalRelationalExpression(@NotNull EqlGrammarParser.EqRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitEqRelationalRelationalExpression(@NotNull EqlGrammarParser.EqRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterStringConstantAtom(@NotNull EqlGrammarParser.StringConstantAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitStringConstantAtom(@NotNull EqlGrammarParser.StringConstantAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OpenedMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterOpenedMailingRelationalExpression(@NotNull EqlGrammarParser.OpenedMailingRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OpenedMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitOpenedMailingRelationalExpression(@NotNull EqlGrammarParser.OpenedMailingRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BooleanExpressionTargetRule}
	 * labeled alternative in {@link EqlGrammarParser#target_expression}.
	 * @param ctx the parse tree
	 */
	void enterBooleanExpressionTargetRule(@NotNull EqlGrammarParser.BooleanExpressionTargetRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BooleanExpressionTargetRule}
	 * labeled alternative in {@link EqlGrammarParser#target_expression}.
	 * @param ctx the parse tree
	 */
	void exitBooleanExpressionTargetRule(@NotNull EqlGrammarParser.BooleanExpressionTargetRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenthesisBooleanParenthesisExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_parenthesis_expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisBooleanParenthesisExpression(@NotNull EqlGrammarParser.ParenthesisBooleanParenthesisExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenthesisBooleanParenthesisExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_parenthesis_expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisBooleanParenthesisExpression(@NotNull EqlGrammarParser.ParenthesisBooleanParenthesisExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndBooleanOrExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_or_expression}.
	 * @param ctx the parse tree
	 */
	void enterAndBooleanOrExpression(@NotNull EqlGrammarParser.AndBooleanOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndBooleanOrExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_or_expression}.
	 * @param ctx the parse tree
	 */
	void exitAndBooleanOrExpression(@NotNull EqlGrammarParser.AndBooleanOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TodayConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterTodayConstantAtom(@NotNull EqlGrammarParser.TodayConstantAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TodayConstantAtom}
	 * labeled alternative in {@link EqlGrammarParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitTodayConstantAtom(@NotNull EqlGrammarParser.TodayConstantAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#constant_list}.
	 * @param ctx the parse tree
	 */
	void enterConstant_list(@NotNull EqlGrammarParser.Constant_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#constant_list}.
	 * @param ctx the parse tree
	 */
	void exitConstant_list(@NotNull EqlGrammarParser.Constant_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#timestamp_expression}.
	 * @param ctx the parse tree
	 */
	void enterTimestamp_expression(@NotNull EqlGrammarParser.Timestamp_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#timestamp_expression}.
	 * @param ctx the parse tree
	 */
	void exitTimestamp_expression(@NotNull EqlGrammarParser.Timestamp_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndBooleanAndExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_and_expression}.
	 * @param ctx the parse tree
	 */
	void enterAndBooleanAndExpression(@NotNull EqlGrammarParser.AndBooleanAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndBooleanAndExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_and_expression}.
	 * @param ctx the parse tree
	 */
	void exitAndBooleanAndExpression(@NotNull EqlGrammarParser.AndBooleanAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelationalBooleanParenthesisExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_parenthesis_expression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalBooleanParenthesisExpression(@NotNull EqlGrammarParser.RelationalBooleanParenthesisExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelationalBooleanParenthesisExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_parenthesis_expression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalBooleanParenthesisExpression(@NotNull EqlGrammarParser.RelationalBooleanParenthesisExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterNeqRelationalRelationalExpression(@NotNull EqlGrammarParser.NeqRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitNeqRelationalRelationalExpression(@NotNull EqlGrammarParser.NeqRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterLeqRelationalRelationalExpression(@NotNull EqlGrammarParser.LeqRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LeqRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitLeqRelationalRelationalExpression(@NotNull EqlGrammarParser.LeqRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LikeRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterLikeRelationalExpression(@NotNull EqlGrammarParser.LikeRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LikeRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitLikeRelationalExpression(@NotNull EqlGrammarParser.LikeRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SubExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubExpression(@NotNull EqlGrammarParser.SubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SubExpression}
	 * labeled alternative in {@link EqlGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubExpression(@NotNull EqlGrammarParser.SubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#numeric_constant}.
	 * @param ctx the parse tree
	 */
	void enterNumeric_constant(@NotNull EqlGrammarParser.Numeric_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#numeric_constant}.
	 * @param ctx the parse tree
	 */
	void exitNumeric_constant(@NotNull EqlGrammarParser.Numeric_constantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AtomFactor}
	 * labeled alternative in {@link EqlGrammarParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterAtomFactor(@NotNull EqlGrammarParser.AtomFactorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AtomFactor}
	 * labeled alternative in {@link EqlGrammarParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitAtomFactor(@NotNull EqlGrammarParser.AtomFactorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringDoubleQuote}
	 * labeled alternative in {@link EqlGrammarParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void enterStringDoubleQuote(@NotNull EqlGrammarParser.StringDoubleQuoteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringDoubleQuote}
	 * labeled alternative in {@link EqlGrammarParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void exitStringDoubleQuote(@NotNull EqlGrammarParser.StringDoubleQuoteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SimpleProfilefieldName}
	 * labeled alternative in {@link EqlGrammarParser#profilefield}.
	 * @param ctx the parse tree
	 */
	void enterSimpleProfilefieldName(@NotNull EqlGrammarParser.SimpleProfilefieldNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SimpleProfilefieldName}
	 * labeled alternative in {@link EqlGrammarParser#profilefield}.
	 * @param ctx the parse tree
	 */
	void exitSimpleProfilefieldName(@NotNull EqlGrammarParser.SimpleProfilefieldNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyTargetRule}
	 * labeled alternative in {@link EqlGrammarParser#target_expression}.
	 * @param ctx the parse tree
	 */
	void enterEmptyTargetRule(@NotNull EqlGrammarParser.EmptyTargetRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyTargetRule}
	 * labeled alternative in {@link EqlGrammarParser#target_expression}.
	 * @param ctx the parse tree
	 */
	void exitEmptyTargetRule(@NotNull EqlGrammarParser.EmptyTargetRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StartsWithRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterStartsWithRelationalExpression(@NotNull EqlGrammarParser.StartsWithRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StartsWithRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitStartsWithRelationalExpression(@NotNull EqlGrammarParser.StartsWithRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ClickedInMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterClickedInMailingRelationalExpression(@NotNull EqlGrammarParser.ClickedInMailingRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ClickedInMailingRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitClickedInMailingRelationalExpression(@NotNull EqlGrammarParser.ClickedInMailingRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EqlGrammarParser#date_format}.
	 * @param ctx the parse tree
	 */
	void enterDate_format(@NotNull EqlGrammarParser.Date_formatContext ctx);
	/**
	 * Exit a parse tree produced by {@link EqlGrammarParser#date_format}.
	 * @param ctx the parse tree
	 */
	void exitDate_format(@NotNull EqlGrammarParser.Date_formatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code GtRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterGtRelationalRelationalExpression(@NotNull EqlGrammarParser.GtRelationalRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code GtRelationalRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitGtRelationalRelationalExpression(@NotNull EqlGrammarParser.GtRelationalRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void enterInRelationalExpression(@NotNull EqlGrammarParser.InRelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InRelationalExpression}
	 * labeled alternative in {@link EqlGrammarParser#relational_expression}.
	 * @param ctx the parse tree
	 */
	void exitInRelationalExpression(@NotNull EqlGrammarParser.InRelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotBooleanAndExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_and_expression}.
	 * @param ctx the parse tree
	 */
	void enterNotBooleanAndExpression(@NotNull EqlGrammarParser.NotBooleanAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotBooleanAndExpression}
	 * labeled alternative in {@link EqlGrammarParser#boolean_and_expression}.
	 * @param ctx the parse tree
	 */
	void exitNotBooleanAndExpression(@NotNull EqlGrammarParser.NotBooleanAndExpressionContext ctx);
}
