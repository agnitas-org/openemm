/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

// Generated from EqlGrammar.g4 by ANTLR 4.4

    package com.agnitas.emm.core.target.eql.parser.antlr4.autogen;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EqlGrammarParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		IS=1, IN=2, LIKE=3, NOT=4, EMPTY=5, AND=6, OR=7, CONTAINS=8, STARTS=9, 
		HAS=10, BINDING=11, OPENED=12, RECEIVED=13, CLICKED=14, LINK=15, MAILING=16, 
		MAILINGLIST=17, OF=18, TYPE=19, WITH=20, STATUS=21, BY=22, REVENUE=23, 
		TODAY=24, DATEFORMAT=25, REFERENCES=26, HAVING=27, ON=28, DEVICE=29, OS=30, 
		CLASS=31, CLIENT=32, FINISHED=33, AUTOIMPORT=34, ACTIVE=35, OPTOUT=36, 
		BLACKLISTED=37, WAIT_FOR_DOI_CONFIRM=38, ADMIN=39, TEST=40, REGULAR=41, 
		FOR=42, MEDIATYPE=43, EMAIL=44, SMS=45, PERIOD=46, OPEN=47, CLOSE=48, 
		COMMA=49, ADD=50, SUB=51, MUL=52, DIV=53, MOD=54, EQ=55, LT=56, GT=57, 
		LEQ=58, GEQ=59, NEQ=60, IDENTIFIER=61, QUOTED_IDENTIFIER=62, NUMERIC_VALUE=63, 
		NUMERIC_ID=64, SPACES=65, STRING_SINGLEQUOTE=66, STRING_DOUBLEQUOTE=67, 
		ESCAPED_CHAR=68;
	public static final String[] tokenNames = {
		"<INVALID>", "IS", "IN", "LIKE", "NOT", "EMPTY", "AND", "OR", "CONTAINS", 
		"STARTS", "HAS", "BINDING", "OPENED", "RECEIVED", "CLICKED", "LINK", "MAILING", 
		"MAILINGLIST", "OF", "TYPE", "WITH", "STATUS", "BY", "REVENUE", "TODAY", 
		"DATEFORMAT", "REFERENCES", "HAVING", "ON", "DEVICE", "OS", "CLASS", "CLIENT", 
		"FINISHED", "AUTOIMPORT", "ACTIVE", "OPTOUT", "BLACKLISTED", "WAIT_FOR_DOI_CONFIRM", 
		"ADMIN", "TEST", "REGULAR", "FOR", "MEDIATYPE", "EMAIL", "SMS", "'.'", 
		"'('", "')'", "','", "'+'", "'-'", "'*'", "'/'", "'%'", "'='", "'<'", 
		"'>'", "'<='", "'>='", "NEQ", "IDENTIFIER", "QUOTED_IDENTIFIER", "NUMERIC_VALUE", 
		"NUMERIC_ID", "SPACES", "STRING_SINGLEQUOTE", "STRING_DOUBLEQUOTE", "ESCAPED_CHAR"
	};
	public static final int
		RULE_target_expression = 0, RULE_boolean_expression = 1, RULE_boolean_or_expression = 2, 
		RULE_boolean_and_expression = 3, RULE_boolean_not_expression = 4, RULE_boolean_parenthesis_expression = 5, 
		RULE_relational_expression = 6, RULE_expression = 7, RULE_term = 8, RULE_signed_factor = 9, 
		RULE_factor = 10, RULE_profilefield = 11, RULE_atom = 12, RULE_numeric_constant = 13, 
		RULE_string_constant = 14, RULE_date_format = 15, RULE_constant_list = 16, 
		RULE_constant_listelements = 17, RULE_constant_listelement = 18;
	public static final String[] ruleNames = {
		"target_expression", "boolean_expression", "boolean_or_expression", "boolean_and_expression", 
		"boolean_not_expression", "boolean_parenthesis_expression", "relational_expression", 
		"expression", "term", "signed_factor", "factor", "profilefield", "atom", 
		"numeric_constant", "string_constant", "date_format", "constant_list", 
		"constant_listelements", "constant_listelement"
	};

	@Override
	public String getGrammarFileName() { return "EqlGrammar.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	    public static boolean insideReferencesHavingExpression = false;
	    
	    public static boolean isReferenceTableAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isReceivedMailingAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isOpenedMailingAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isClickedInMailingAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isRevenueByMailingAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isReferencesHavingAllowed() {
	        return !insideReferencesHavingExpression;
	    }

	    public static boolean isFinishedAutoImportAllowed() {
	        return !insideReferencesHavingExpression;
	    }
	    
	    public static boolean isHasBindingAllowed() {
	    	 return !insideReferencesHavingExpression;
	    }
	    

	public EqlGrammarParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Target_expressionContext extends ParserRuleContext {
		public Target_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_target_expression; }
	 
		public Target_expressionContext() { }
		public void copyFrom(Target_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class EmptyTargetRuleContext extends Target_expressionContext {
		public TerminalNode EOF() { return getToken(EqlGrammarParser.EOF, 0); }
		public EmptyTargetRuleContext(Target_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterEmptyTargetRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitEmptyTargetRule(this);
		}
	}
	public static class BooleanExpressionTargetRuleContext extends Target_expressionContext {
		public TerminalNode EOF() { return getToken(EqlGrammarParser.EOF, 0); }
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public BooleanExpressionTargetRuleContext(Target_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterBooleanExpressionTargetRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitBooleanExpressionTargetRule(this);
		}
	}

	public final Target_expressionContext target_expression() throws RecognitionException {
		Target_expressionContext _localctx = new Target_expressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_target_expression);
		try {
			setState(42);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				_localctx = new BooleanExpressionTargetRuleContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(38); boolean_expression();
				setState(39); match(EOF);
				}
				break;
			case 2:
				_localctx = new EmptyTargetRuleContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(41); match(EOF);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_expressionContext extends ParserRuleContext {
		public Boolean_or_expressionContext boolean_or_expression() {
			return getRuleContext(Boolean_or_expressionContext.class,0);
		}
		public Boolean_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterBoolean_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitBoolean_expression(this);
		}
	}

	public final Boolean_expressionContext boolean_expression() throws RecognitionException {
		Boolean_expressionContext _localctx = new Boolean_expressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_boolean_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44); boolean_or_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_or_expressionContext extends ParserRuleContext {
		public Boolean_or_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_or_expression; }
	 
		public Boolean_or_expressionContext() { }
		public void copyFrom(Boolean_or_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class OrBooleanOrExpressionContext extends Boolean_or_expressionContext {
		public Boolean_and_expressionContext boolean_and_expression() {
			return getRuleContext(Boolean_and_expressionContext.class,0);
		}
		public Boolean_or_expressionContext boolean_or_expression() {
			return getRuleContext(Boolean_or_expressionContext.class,0);
		}
		public TerminalNode OR() { return getToken(EqlGrammarParser.OR, 0); }
		public OrBooleanOrExpressionContext(Boolean_or_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterOrBooleanOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitOrBooleanOrExpression(this);
		}
	}
	public static class AndBooleanOrExpressionContext extends Boolean_or_expressionContext {
		public Boolean_and_expressionContext boolean_and_expression() {
			return getRuleContext(Boolean_and_expressionContext.class,0);
		}
		public AndBooleanOrExpressionContext(Boolean_or_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterAndBooleanOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitAndBooleanOrExpression(this);
		}
	}

	public final Boolean_or_expressionContext boolean_or_expression() throws RecognitionException {
		Boolean_or_expressionContext _localctx = new Boolean_or_expressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_boolean_or_expression);
		try {
			setState(51);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				_localctx = new AndBooleanOrExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(46); boolean_and_expression();
				}
				break;
			case 2:
				_localctx = new OrBooleanOrExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(47); boolean_and_expression();
				setState(48); match(OR);
				setState(49); boolean_or_expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_and_expressionContext extends ParserRuleContext {
		public Boolean_and_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_and_expression; }
	 
		public Boolean_and_expressionContext() { }
		public void copyFrom(Boolean_and_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AndBooleanAndExpressionContext extends Boolean_and_expressionContext {
		public Boolean_and_expressionContext boolean_and_expression() {
			return getRuleContext(Boolean_and_expressionContext.class,0);
		}
		public TerminalNode AND() { return getToken(EqlGrammarParser.AND, 0); }
		public Boolean_not_expressionContext boolean_not_expression() {
			return getRuleContext(Boolean_not_expressionContext.class,0);
		}
		public AndBooleanAndExpressionContext(Boolean_and_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterAndBooleanAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitAndBooleanAndExpression(this);
		}
	}
	public static class NotBooleanAndExpressionContext extends Boolean_and_expressionContext {
		public Boolean_not_expressionContext boolean_not_expression() {
			return getRuleContext(Boolean_not_expressionContext.class,0);
		}
		public NotBooleanAndExpressionContext(Boolean_and_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNotBooleanAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNotBooleanAndExpression(this);
		}
	}

	public final Boolean_and_expressionContext boolean_and_expression() throws RecognitionException {
		Boolean_and_expressionContext _localctx = new Boolean_and_expressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_boolean_and_expression);
		try {
			setState(58);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new NotBooleanAndExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(53); boolean_not_expression();
				}
				break;
			case 2:
				_localctx = new AndBooleanAndExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(54); boolean_not_expression();
				setState(55); match(AND);
				setState(56); boolean_and_expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_not_expressionContext extends ParserRuleContext {
		public Boolean_not_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_not_expression; }
	 
		public Boolean_not_expressionContext() { }
		public void copyFrom(Boolean_not_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ParenthesisBooleanNotExpressionContext extends Boolean_not_expressionContext {
		public Boolean_parenthesis_expressionContext boolean_parenthesis_expression() {
			return getRuleContext(Boolean_parenthesis_expressionContext.class,0);
		}
		public ParenthesisBooleanNotExpressionContext(Boolean_not_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterParenthesisBooleanNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitParenthesisBooleanNotExpression(this);
		}
	}
	public static class NotBooleanNotExpressionContext extends Boolean_not_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public Boolean_parenthesis_expressionContext boolean_parenthesis_expression() {
			return getRuleContext(Boolean_parenthesis_expressionContext.class,0);
		}
		public NotBooleanNotExpressionContext(Boolean_not_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNotBooleanNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNotBooleanNotExpression(this);
		}
	}

	public final Boolean_not_expressionContext boolean_not_expression() throws RecognitionException {
		Boolean_not_expressionContext _localctx = new Boolean_not_expressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_boolean_not_expression);
		try {
			setState(63);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new ParenthesisBooleanNotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(60); boolean_parenthesis_expression();
				}
				break;
			case 2:
				_localctx = new NotBooleanNotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(61); match(NOT);
				setState(62); boolean_parenthesis_expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_parenthesis_expressionContext extends ParserRuleContext {
		public Boolean_parenthesis_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_parenthesis_expression; }
	 
		public Boolean_parenthesis_expressionContext() { }
		public void copyFrom(Boolean_parenthesis_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RelationalBooleanParenthesisExpressionContext extends Boolean_parenthesis_expressionContext {
		public Relational_expressionContext relational_expression() {
			return getRuleContext(Relational_expressionContext.class,0);
		}
		public RelationalBooleanParenthesisExpressionContext(Boolean_parenthesis_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterRelationalBooleanParenthesisExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitRelationalBooleanParenthesisExpression(this);
		}
	}
	public static class ParenthesisBooleanParenthesisExpressionContext extends Boolean_parenthesis_expressionContext {
		public TerminalNode OPEN() { return getToken(EqlGrammarParser.OPEN, 0); }
		public TerminalNode CLOSE() { return getToken(EqlGrammarParser.CLOSE, 0); }
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public ParenthesisBooleanParenthesisExpressionContext(Boolean_parenthesis_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterParenthesisBooleanParenthesisExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitParenthesisBooleanParenthesisExpression(this);
		}
	}

	public final Boolean_parenthesis_expressionContext boolean_parenthesis_expression() throws RecognitionException {
		Boolean_parenthesis_expressionContext _localctx = new Boolean_parenthesis_expressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_boolean_parenthesis_expression);
		try {
			setState(70);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new RelationalBooleanParenthesisExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(65); relational_expression();
				}
				break;
			case 2:
				_localctx = new ParenthesisBooleanParenthesisExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(66); match(OPEN);
				setState(67); boolean_expression();
				setState(68); match(CLOSE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Relational_expressionContext extends ParserRuleContext {
		public Relational_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relational_expression; }
	 
		public Relational_expressionContext() { }
		public void copyFrom(Relational_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class EqRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EQ() { return getToken(EqlGrammarParser.EQ, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public EqRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterEqRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitEqRelationalRelationalExpression(this);
		}
	}
	public static class ContainsRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public TerminalNode CONTAINS() { return getToken(EqlGrammarParser.CONTAINS, 0); }
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ContainsRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterContainsRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitContainsRelationalExpression(this);
		}
	}
	public static class EmptyRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public TerminalNode IS() { return getToken(EqlGrammarParser.IS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EMPTY() { return getToken(EqlGrammarParser.EMPTY, 0); }
		public EmptyRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterEmptyRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitEmptyRelationalExpression(this);
		}
	}
	public static class GeqRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public TerminalNode GEQ() { return getToken(EqlGrammarParser.GEQ, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public GeqRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterGeqRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitGeqRelationalRelationalExpression(this);
		}
	}
	public static class OpenedMailingRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NUMERIC_ID() { return getToken(EqlGrammarParser.NUMERIC_ID, 0); }
		public TerminalNode MAILING() { return getToken(EqlGrammarParser.MAILING, 0); }
		public TerminalNode OPENED() { return getToken(EqlGrammarParser.OPENED, 0); }
		public OpenedMailingRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterOpenedMailingRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitOpenedMailingRelationalExpression(this);
		}
	}
	public static class LtRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public TerminalNode LT() { return getToken(EqlGrammarParser.LT, 0); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public LtRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterLtRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitLtRelationalRelationalExpression(this);
		}
	}
	public static class StartsWithRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public TerminalNode WITH() { return getToken(EqlGrammarParser.WITH, 0); }
		public TerminalNode STARTS() { return getToken(EqlGrammarParser.STARTS, 0); }
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StartsWithRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterStartsWithRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitStartsWithRelationalExpression(this);
		}
	}
	public static class NeqRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public TerminalNode NEQ() { return getToken(EqlGrammarParser.NEQ, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public NeqRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNeqRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNeqRelationalRelationalExpression(this);
		}
	}
	public static class ClickedInMailingRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode CLICKED() { return getToken(EqlGrammarParser.CLICKED, 0); }
		public List<TerminalNode> NUMERIC_ID() { return getTokens(EqlGrammarParser.NUMERIC_ID); }
		public TerminalNode MAILING() { return getToken(EqlGrammarParser.MAILING, 0); }
		public TerminalNode IN() { return getToken(EqlGrammarParser.IN, 0); }
		public TerminalNode NUMERIC_ID(int i) {
			return getToken(EqlGrammarParser.NUMERIC_ID, i);
		}
		public TerminalNode LINK() { return getToken(EqlGrammarParser.LINK, 0); }
		public ClickedInMailingRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterClickedInMailingRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitClickedInMailingRelationalExpression(this);
		}
	}
	public static class LeqRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public TerminalNode LEQ() { return getToken(EqlGrammarParser.LEQ, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public LeqRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterLeqRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitLeqRelationalRelationalExpression(this);
		}
	}
	public static class ReceivedMailingRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NUMERIC_ID() { return getToken(EqlGrammarParser.NUMERIC_ID, 0); }
		public TerminalNode MAILING() { return getToken(EqlGrammarParser.MAILING, 0); }
		public TerminalNode RECEIVED() { return getToken(EqlGrammarParser.RECEIVED, 0); }
		public ReceivedMailingRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterReceivedMailingRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitReceivedMailingRelationalExpression(this);
		}
	}
	public static class LikeRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public TerminalNode LIKE() { return getToken(EqlGrammarParser.LIKE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LikeRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterLikeRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitLikeRelationalExpression(this);
		}
	}
	public static class GtRelationalRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode DATEFORMAT() { return getToken(EqlGrammarParser.DATEFORMAT, 0); }
		public Date_formatContext date_format() {
			return getRuleContext(Date_formatContext.class,0);
		}
		public TerminalNode GT() { return getToken(EqlGrammarParser.GT, 0); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public GtRelationalRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterGtRelationalRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitGtRelationalRelationalExpression(this);
		}
	}
	public static class InRelationalExpressionContext extends Relational_expressionContext {
		public TerminalNode NOT() { return getToken(EqlGrammarParser.NOT, 0); }
		public Constant_listContext constant_list() {
			return getRuleContext(Constant_listContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(EqlGrammarParser.IN, 0); }
		public InRelationalExpressionContext(Relational_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterInRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitInRelationalExpression(this);
		}
	}

	public final Relational_expressionContext relational_expression() throws RecognitionException {
		Relational_expressionContext _localctx = new Relational_expressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_relational_expression);
		int _la;
		try {
			setState(167);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				_localctx = new InRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(72); expression(0);
				setState(74);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(73); match(NOT);
					}
				}

				setState(76); match(IN);
				setState(77); constant_list();
				}
				break;
			case 2:
				_localctx = new EmptyRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(79); expression(0);
				setState(80); match(IS);
				setState(82);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(81); match(NOT);
					}
				}

				setState(84); match(EMPTY);
				}
				break;
			case 3:
				_localctx = new LikeRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(86); expression(0);
				setState(88);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(87); match(NOT);
					}
				}

				setState(90); match(LIKE);
				setState(91); string_constant();
				}
				break;
			case 4:
				_localctx = new ContainsRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(93); expression(0);
				setState(95);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(94); match(NOT);
					}
				}

				setState(97); match(CONTAINS);
				setState(98); string_constant();
				}
				break;
			case 5:
				_localctx = new StartsWithRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(100); expression(0);
				setState(102);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(101); match(NOT);
					}
				}

				setState(104); match(STARTS);
				setState(105); match(WITH);
				setState(106); string_constant();
				}
				break;
			case 6:
				_localctx = new EqRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(108); expression(0);
				setState(109); match(EQ);
				setState(110); expression(0);
				setState(113);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(111); match(DATEFORMAT);
					setState(112); date_format();
					}
				}

				}
				break;
			case 7:
				_localctx = new NeqRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(115); expression(0);
				setState(116); match(NEQ);
				setState(117); expression(0);
				setState(120);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(118); match(DATEFORMAT);
					setState(119); date_format();
					}
				}

				}
				break;
			case 8:
				_localctx = new LtRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(122); expression(0);
				setState(123); match(LT);
				setState(124); expression(0);
				setState(127);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(125); match(DATEFORMAT);
					setState(126); date_format();
					}
				}

				}
				break;
			case 9:
				_localctx = new GtRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(129); expression(0);
				setState(130); match(GT);
				setState(131); expression(0);
				setState(134);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(132); match(DATEFORMAT);
					setState(133); date_format();
					}
				}

				}
				break;
			case 10:
				_localctx = new LeqRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(136); expression(0);
				setState(137); match(LEQ);
				setState(138); expression(0);
				setState(141);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(139); match(DATEFORMAT);
					setState(140); date_format();
					}
				}

				}
				break;
			case 11:
				_localctx = new GeqRelationalRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(143); expression(0);
				setState(144); match(GEQ);
				setState(145); expression(0);
				setState(148);
				_la = _input.LA(1);
				if (_la==DATEFORMAT) {
					{
					setState(146); match(DATEFORMAT);
					setState(147); date_format();
					}
				}

				}
				break;
			case 12:
				_localctx = new OpenedMailingRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(150);
				if (!( isOpenedMailingAllowed() )) throw new FailedPredicateException(this, " isOpenedMailingAllowed() ");
				setState(151); match(OPENED);
				setState(152); match(MAILING);
				setState(153); match(NUMERIC_ID);
				}
				break;
			case 13:
				_localctx = new ReceivedMailingRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(154);
				if (!( isReceivedMailingAllowed() )) throw new FailedPredicateException(this, " isReceivedMailingAllowed() ");
				setState(155); match(RECEIVED);
				setState(156); match(MAILING);
				setState(157); match(NUMERIC_ID);
				}
				break;
			case 14:
				_localctx = new ClickedInMailingRelationalExpressionContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(158);
				if (!( isClickedInMailingAllowed() )) throw new FailedPredicateException(this, " isClickedInMailingAllowed() ");
				setState(159); match(CLICKED);
				setState(162);
				_la = _input.LA(1);
				if (_la==LINK) {
					{
					setState(160); match(LINK);
					setState(161); match(NUMERIC_ID);
					}
				}

				setState(164); match(IN);
				setState(165); match(MAILING);
				setState(166); match(NUMERIC_ID);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TermExpressionContext extends ExpressionContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterTermExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitTermExpression(this);
		}
	}
	public static class AddExpressionContext extends ExpressionContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode ADD() { return getToken(EqlGrammarParser.ADD, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AddExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterAddExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitAddExpression(this);
		}
	}
	public static class SubExpressionContext extends ExpressionContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode SUB() { return getToken(EqlGrammarParser.SUB, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SubExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterSubExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitSubExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 14;
		enterRecursionRule(_localctx, 14, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new TermExpressionContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(170); term(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(180);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(178);
					switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
					case 1:
						{
						_localctx = new AddExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(172);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(173); match(ADD);
						setState(174); term(0);
						}
						break;
					case 2:
						{
						_localctx = new SubExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(175);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(176); match(SUB);
						setState(177); term(0);
						}
						break;
					}
					} 
				}
				setState(182);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MulTermContext extends TermContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Signed_factorContext signed_factor() {
			return getRuleContext(Signed_factorContext.class,0);
		}
		public TerminalNode MUL() { return getToken(EqlGrammarParser.MUL, 0); }
		public MulTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterMulTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitMulTerm(this);
		}
	}
	public static class DivTermContext extends TermContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Signed_factorContext signed_factor() {
			return getRuleContext(Signed_factorContext.class,0);
		}
		public TerminalNode DIV() { return getToken(EqlGrammarParser.DIV, 0); }
		public DivTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterDivTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitDivTerm(this);
		}
	}
	public static class SignedFactorTermContext extends TermContext {
		public Signed_factorContext signed_factor() {
			return getRuleContext(Signed_factorContext.class,0);
		}
		public SignedFactorTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterSignedFactorTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitSignedFactorTerm(this);
		}
	}
	public static class ModTermContext extends TermContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Signed_factorContext signed_factor() {
			return getRuleContext(Signed_factorContext.class,0);
		}
		public TerminalNode MOD() { return getToken(EqlGrammarParser.MOD, 0); }
		public ModTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterModTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitModTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		return term(0);
	}

	private TermContext term(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermContext _localctx = new TermContext(_ctx, _parentState);
		TermContext _prevctx = _localctx;
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_term, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new SignedFactorTermContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(184); signed_factor();
			}
			_ctx.stop = _input.LT(-1);
			setState(197);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(195);
					switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
					case 1:
						{
						_localctx = new MulTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(186);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(187); match(MUL);
						setState(188); signed_factor();
						}
						break;
					case 2:
						{
						_localctx = new DivTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(189);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(190); match(DIV);
						setState(191); signed_factor();
						}
						break;
					case 3:
						{
						_localctx = new ModTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(192);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(193); match(MOD);
						setState(194); signed_factor();
						}
						break;
					}
					} 
				}
				setState(199);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Signed_factorContext extends ParserRuleContext {
		public Signed_factorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signed_factor; }
	 
		public Signed_factorContext() { }
		public void copyFrom(Signed_factorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PositiveSignedFactorContext extends Signed_factorContext {
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public TerminalNode ADD() { return getToken(EqlGrammarParser.ADD, 0); }
		public PositiveSignedFactorContext(Signed_factorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterPositiveSignedFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitPositiveSignedFactor(this);
		}
	}
	public static class NegativeSignedFactorContext extends Signed_factorContext {
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public TerminalNode SUB() { return getToken(EqlGrammarParser.SUB, 0); }
		public NegativeSignedFactorContext(Signed_factorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNegativeSignedFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNegativeSignedFactor(this);
		}
	}
	public static class FactorSignedFactorContext extends Signed_factorContext {
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public FactorSignedFactorContext(Signed_factorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterFactorSignedFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitFactorSignedFactor(this);
		}
	}

	public final Signed_factorContext signed_factor() throws RecognitionException {
		Signed_factorContext _localctx = new Signed_factorContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_signed_factor);
		try {
			setState(205);
			switch (_input.LA(1)) {
			case TODAY:
			case OPEN:
			case IDENTIFIER:
			case QUOTED_IDENTIFIER:
			case NUMERIC_VALUE:
			case NUMERIC_ID:
			case STRING_SINGLEQUOTE:
			case STRING_DOUBLEQUOTE:
				_localctx = new FactorSignedFactorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(200); factor();
				}
				break;
			case ADD:
				_localctx = new PositiveSignedFactorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(201); match(ADD);
				setState(202); factor();
				}
				break;
			case SUB:
				_localctx = new NegativeSignedFactorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(203); match(SUB);
				setState(204); factor();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FactorContext extends ParserRuleContext {
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
	 
		public FactorContext() { }
		public void copyFrom(FactorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ParenthesisFactorContext extends FactorContext {
		public TerminalNode OPEN() { return getToken(EqlGrammarParser.OPEN, 0); }
		public TerminalNode CLOSE() { return getToken(EqlGrammarParser.CLOSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ParenthesisFactorContext(FactorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterParenthesisFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitParenthesisFactor(this);
		}
	}
	public static class AtomFactorContext extends FactorContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomFactorContext(FactorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterAtomFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitAtomFactor(this);
		}
	}

	public final FactorContext factor() throws RecognitionException {
		FactorContext _localctx = new FactorContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_factor);
		try {
			setState(212);
			switch (_input.LA(1)) {
			case TODAY:
			case IDENTIFIER:
			case QUOTED_IDENTIFIER:
			case NUMERIC_VALUE:
			case NUMERIC_ID:
			case STRING_SINGLEQUOTE:
			case STRING_DOUBLEQUOTE:
				_localctx = new AtomFactorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(207); atom();
				}
				break;
			case OPEN:
				_localctx = new ParenthesisFactorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(208); match(OPEN);
				setState(209); expression(0);
				setState(210); match(CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ProfilefieldContext extends ParserRuleContext {
		public ProfilefieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_profilefield; }
	 
		public ProfilefieldContext() { }
		public void copyFrom(ProfilefieldContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SimpleProfilefieldNameContext extends ProfilefieldContext {
		public TerminalNode IDENTIFIER() { return getToken(EqlGrammarParser.IDENTIFIER, 0); }
		public SimpleProfilefieldNameContext(ProfilefieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterSimpleProfilefieldName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitSimpleProfilefieldName(this);
		}
	}
	public static class QuotedProfilefieldNameContext extends ProfilefieldContext {
		public TerminalNode QUOTED_IDENTIFIER() { return getToken(EqlGrammarParser.QUOTED_IDENTIFIER, 0); }
		public QuotedProfilefieldNameContext(ProfilefieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterQuotedProfilefieldName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitQuotedProfilefieldName(this);
		}
	}

	public final ProfilefieldContext profilefield() throws RecognitionException {
		ProfilefieldContext _localctx = new ProfilefieldContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_profilefield);
		try {
			setState(216);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new SimpleProfilefieldNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(214); match(IDENTIFIER);
				}
				break;
			case QUOTED_IDENTIFIER:
				_localctx = new QuotedProfilefieldNameContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(215); match(QUOTED_IDENTIFIER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtomContext extends ParserRuleContext {
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
	 
		public AtomContext() { }
		public void copyFrom(AtomContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StringConstantAtomContext extends AtomContext {
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public StringConstantAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterStringConstantAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitStringConstantAtom(this);
		}
	}
	public static class NumericConstantAtomContext extends AtomContext {
		public Numeric_constantContext numeric_constant() {
			return getRuleContext(Numeric_constantContext.class,0);
		}
		public NumericConstantAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNumericConstantAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNumericConstantAtom(this);
		}
	}
	public static class ProfileFieldAtomContext extends AtomContext {
		public ProfilefieldContext profilefield() {
			return getRuleContext(ProfilefieldContext.class,0);
		}
		public ProfileFieldAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterProfileFieldAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitProfileFieldAtom(this);
		}
	}
	public static class TodayConstantAtomContext extends AtomContext {
		public TerminalNode TODAY() { return getToken(EqlGrammarParser.TODAY, 0); }
		public TodayConstantAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterTodayConstantAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitTodayConstantAtom(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_atom);
		try {
			setState(222);
			switch (_input.LA(1)) {
			case NUMERIC_VALUE:
			case NUMERIC_ID:
				_localctx = new NumericConstantAtomContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(218); numeric_constant();
				}
				break;
			case IDENTIFIER:
			case QUOTED_IDENTIFIER:
				_localctx = new ProfileFieldAtomContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(219); profilefield();
				}
				break;
			case STRING_SINGLEQUOTE:
			case STRING_DOUBLEQUOTE:
				_localctx = new StringConstantAtomContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(220); string_constant();
				}
				break;
			case TODAY:
				_localctx = new TodayConstantAtomContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(221); match(TODAY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Numeric_constantContext extends ParserRuleContext {
		public TerminalNode NUMERIC_ID() { return getToken(EqlGrammarParser.NUMERIC_ID, 0); }
		public TerminalNode NUMERIC_VALUE() { return getToken(EqlGrammarParser.NUMERIC_VALUE, 0); }
		public Numeric_constantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeric_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterNumeric_constant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitNumeric_constant(this);
		}
	}

	public final Numeric_constantContext numeric_constant() throws RecognitionException {
		Numeric_constantContext _localctx = new Numeric_constantContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_numeric_constant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			_la = _input.LA(1);
			if ( !(_la==NUMERIC_VALUE || _la==NUMERIC_ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_constantContext extends ParserRuleContext {
		public String_constantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_constant; }
	 
		public String_constantContext() { }
		public void copyFrom(String_constantContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StringSingleQuoteContext extends String_constantContext {
		public TerminalNode STRING_SINGLEQUOTE() { return getToken(EqlGrammarParser.STRING_SINGLEQUOTE, 0); }
		public StringSingleQuoteContext(String_constantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterStringSingleQuote(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitStringSingleQuote(this);
		}
	}
	public static class StringDoubleQuoteContext extends String_constantContext {
		public TerminalNode STRING_DOUBLEQUOTE() { return getToken(EqlGrammarParser.STRING_DOUBLEQUOTE, 0); }
		public StringDoubleQuoteContext(String_constantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterStringDoubleQuote(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitStringDoubleQuote(this);
		}
	}

	public final String_constantContext string_constant() throws RecognitionException {
		String_constantContext _localctx = new String_constantContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_string_constant);
		try {
			setState(228);
			switch (_input.LA(1)) {
			case STRING_SINGLEQUOTE:
				_localctx = new StringSingleQuoteContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(226); match(STRING_SINGLEQUOTE);
				}
				break;
			case STRING_DOUBLEQUOTE:
				_localctx = new StringDoubleQuoteContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(227); match(STRING_DOUBLEQUOTE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Date_formatContext extends ParserRuleContext {
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public Date_formatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_date_format; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterDate_format(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitDate_format(this);
		}
	}

	public final Date_formatContext date_format() throws RecognitionException {
		Date_formatContext _localctx = new Date_formatContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_date_format);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230); string_constant();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Constant_listContext extends ParserRuleContext {
		public TerminalNode OPEN() { return getToken(EqlGrammarParser.OPEN, 0); }
		public Constant_listelementsContext constant_listelements() {
			return getRuleContext(Constant_listelementsContext.class,0);
		}
		public TerminalNode CLOSE() { return getToken(EqlGrammarParser.CLOSE, 0); }
		public Constant_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterConstant_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitConstant_list(this);
		}
	}

	public final Constant_listContext constant_list() throws RecognitionException {
		Constant_listContext _localctx = new Constant_listContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_constant_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232); match(OPEN);
			setState(233); constant_listelements();
			setState(234); match(CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Constant_listelementsContext extends ParserRuleContext {
		public List<Constant_listelementContext> constant_listelement() {
			return getRuleContexts(Constant_listelementContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(EqlGrammarParser.COMMA); }
		public Constant_listelementContext constant_listelement(int i) {
			return getRuleContext(Constant_listelementContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(EqlGrammarParser.COMMA, i);
		}
		public Constant_listelementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_listelements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterConstant_listelements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitConstant_listelements(this);
		}
	}

	public final Constant_listelementsContext constant_listelements() throws RecognitionException {
		Constant_listelementsContext _localctx = new Constant_listelementsContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_constant_listelements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236); constant_listelement();
			setState(241);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(237); match(COMMA);
				setState(238); constant_listelement();
				}
				}
				setState(243);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Constant_listelementContext extends ParserRuleContext {
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public Numeric_constantContext numeric_constant() {
			return getRuleContext(Numeric_constantContext.class,0);
		}
		public Constant_listelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_listelement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).enterConstant_listelement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EqlGrammarListener ) ((EqlGrammarListener)listener).exitConstant_listelement(this);
		}
	}

	public final Constant_listelementContext constant_listelement() throws RecognitionException {
		Constant_listelementContext _localctx = new Constant_listelementContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_constant_listelement);
		try {
			setState(246);
			switch (_input.LA(1)) {
			case STRING_SINGLEQUOTE:
			case STRING_DOUBLEQUOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(244); string_constant();
				}
				break;
			case NUMERIC_VALUE:
			case NUMERIC_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(245); numeric_constant();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 6: return relational_expression_sempred((Relational_expressionContext)_localctx, predIndex);
		case 7: return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 8: return term_sempred((TermContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3: return precpred(_ctx, 2);
		case 4: return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean term_sempred(TermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5: return precpred(_ctx, 3);
		case 6: return precpred(_ctx, 2);
		case 7: return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean relational_expression_sempred(Relational_expressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return  isOpenedMailingAllowed() ;
		case 1: return  isReceivedMailingAllowed() ;
		case 2: return  isClickedInMailingAllowed() ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3F\u00fb\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\3\2\3\2\3\2\3\2\5\2-\n\2\3\3\3\3\3\4\3\4\3\4\3\4"+
		"\3\4\5\4\66\n\4\3\5\3\5\3\5\3\5\3\5\5\5=\n\5\3\6\3\6\3\6\5\6B\n\6\3\7"+
		"\3\7\3\7\3\7\3\7\5\7I\n\7\3\b\3\b\5\bM\n\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b"+
		"U\n\b\3\b\3\b\3\b\3\b\5\b[\n\b\3\b\3\b\3\b\3\b\3\b\5\bb\n\b\3\b\3\b\3"+
		"\b\3\b\3\b\5\bi\n\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\bt\n\b\3\b\3"+
		"\b\3\b\3\b\3\b\5\b{\n\b\3\b\3\b\3\b\3\b\3\b\5\b\u0082\n\b\3\b\3\b\3\b"+
		"\3\b\3\b\5\b\u0089\n\b\3\b\3\b\3\b\3\b\3\b\5\b\u0090\n\b\3\b\3\b\3\b\3"+
		"\b\3\b\5\b\u0097\n\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5"+
		"\b\u00a5\n\b\3\b\3\b\3\b\5\b\u00aa\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\7\t\u00b5\n\t\f\t\16\t\u00b8\13\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\7\n\u00c6\n\n\f\n\16\n\u00c9\13\n\3\13\3\13\3\13\3\13"+
		"\3\13\5\13\u00d0\n\13\3\f\3\f\3\f\3\f\3\f\5\f\u00d7\n\f\3\r\3\r\5\r\u00db"+
		"\n\r\3\16\3\16\3\16\3\16\5\16\u00e1\n\16\3\17\3\17\3\20\3\20\5\20\u00e7"+
		"\n\20\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23\7\23\u00f2\n\23\f\23"+
		"\16\23\u00f5\13\23\3\24\3\24\5\24\u00f9\n\24\3\24\2\4\20\22\25\2\4\6\b"+
		"\n\f\16\20\22\24\26\30\32\34\36 \"$&\2\3\3\2AB\u0114\2,\3\2\2\2\4.\3\2"+
		"\2\2\6\65\3\2\2\2\b<\3\2\2\2\nA\3\2\2\2\fH\3\2\2\2\16\u00a9\3\2\2\2\20"+
		"\u00ab\3\2\2\2\22\u00b9\3\2\2\2\24\u00cf\3\2\2\2\26\u00d6\3\2\2\2\30\u00da"+
		"\3\2\2\2\32\u00e0\3\2\2\2\34\u00e2\3\2\2\2\36\u00e6\3\2\2\2 \u00e8\3\2"+
		"\2\2\"\u00ea\3\2\2\2$\u00ee\3\2\2\2&\u00f8\3\2\2\2()\5\4\3\2)*\7\2\2\3"+
		"*-\3\2\2\2+-\7\2\2\3,(\3\2\2\2,+\3\2\2\2-\3\3\2\2\2./\5\6\4\2/\5\3\2\2"+
		"\2\60\66\5\b\5\2\61\62\5\b\5\2\62\63\7\t\2\2\63\64\5\6\4\2\64\66\3\2\2"+
		"\2\65\60\3\2\2\2\65\61\3\2\2\2\66\7\3\2\2\2\67=\5\n\6\289\5\n\6\29:\7"+
		"\b\2\2:;\5\b\5\2;=\3\2\2\2<\67\3\2\2\2<8\3\2\2\2=\t\3\2\2\2>B\5\f\7\2"+
		"?@\7\6\2\2@B\5\f\7\2A>\3\2\2\2A?\3\2\2\2B\13\3\2\2\2CI\5\16\b\2DE\7\61"+
		"\2\2EF\5\4\3\2FG\7\62\2\2GI\3\2\2\2HC\3\2\2\2HD\3\2\2\2I\r\3\2\2\2JL\5"+
		"\20\t\2KM\7\6\2\2LK\3\2\2\2LM\3\2\2\2MN\3\2\2\2NO\7\4\2\2OP\5\"\22\2P"+
		"\u00aa\3\2\2\2QR\5\20\t\2RT\7\3\2\2SU\7\6\2\2TS\3\2\2\2TU\3\2\2\2UV\3"+
		"\2\2\2VW\7\7\2\2W\u00aa\3\2\2\2XZ\5\20\t\2Y[\7\6\2\2ZY\3\2\2\2Z[\3\2\2"+
		"\2[\\\3\2\2\2\\]\7\5\2\2]^\5\36\20\2^\u00aa\3\2\2\2_a\5\20\t\2`b\7\6\2"+
		"\2a`\3\2\2\2ab\3\2\2\2bc\3\2\2\2cd\7\n\2\2de\5\36\20\2e\u00aa\3\2\2\2"+
		"fh\5\20\t\2gi\7\6\2\2hg\3\2\2\2hi\3\2\2\2ij\3\2\2\2jk\7\13\2\2kl\7\26"+
		"\2\2lm\5\36\20\2m\u00aa\3\2\2\2no\5\20\t\2op\79\2\2ps\5\20\t\2qr\7\33"+
		"\2\2rt\5 \21\2sq\3\2\2\2st\3\2\2\2t\u00aa\3\2\2\2uv\5\20\t\2vw\7>\2\2"+
		"wz\5\20\t\2xy\7\33\2\2y{\5 \21\2zx\3\2\2\2z{\3\2\2\2{\u00aa\3\2\2\2|}"+
		"\5\20\t\2}~\7:\2\2~\u0081\5\20\t\2\177\u0080\7\33\2\2\u0080\u0082\5 \21"+
		"\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u00aa\3\2\2\2\u0083\u0084"+
		"\5\20\t\2\u0084\u0085\7;\2\2\u0085\u0088\5\20\t\2\u0086\u0087\7\33\2\2"+
		"\u0087\u0089\5 \21\2\u0088\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u00aa"+
		"\3\2\2\2\u008a\u008b\5\20\t\2\u008b\u008c\7<\2\2\u008c\u008f\5\20\t\2"+
		"\u008d\u008e\7\33\2\2\u008e\u0090\5 \21\2\u008f\u008d\3\2\2\2\u008f\u0090"+
		"\3\2\2\2\u0090\u00aa\3\2\2\2\u0091\u0092\5\20\t\2\u0092\u0093\7=\2\2\u0093"+
		"\u0096\5\20\t\2\u0094\u0095\7\33\2\2\u0095\u0097\5 \21\2\u0096\u0094\3"+
		"\2\2\2\u0096\u0097\3\2\2\2\u0097\u00aa\3\2\2\2\u0098\u0099\6\b\2\2\u0099"+
		"\u009a\7\16\2\2\u009a\u009b\7\22\2\2\u009b\u00aa\7B\2\2\u009c\u009d\6"+
		"\b\3\2\u009d\u009e\7\17\2\2\u009e\u009f\7\22\2\2\u009f\u00aa\7B\2\2\u00a0"+
		"\u00a1\6\b\4\2\u00a1\u00a4\7\20\2\2\u00a2\u00a3\7\21\2\2\u00a3\u00a5\7"+
		"B\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6"+
		"\u00a7\7\4\2\2\u00a7\u00a8\7\22\2\2\u00a8\u00aa\7B\2\2\u00a9J\3\2\2\2"+
		"\u00a9Q\3\2\2\2\u00a9X\3\2\2\2\u00a9_\3\2\2\2\u00a9f\3\2\2\2\u00a9n\3"+
		"\2\2\2\u00a9u\3\2\2\2\u00a9|\3\2\2\2\u00a9\u0083\3\2\2\2\u00a9\u008a\3"+
		"\2\2\2\u00a9\u0091\3\2\2\2\u00a9\u0098\3\2\2\2\u00a9\u009c\3\2\2\2\u00a9"+
		"\u00a0\3\2\2\2\u00aa\17\3\2\2\2\u00ab\u00ac\b\t\1\2\u00ac\u00ad\5\22\n"+
		"\2\u00ad\u00b6\3\2\2\2\u00ae\u00af\f\4\2\2\u00af\u00b0\7\64\2\2\u00b0"+
		"\u00b5\5\22\n\2\u00b1\u00b2\f\3\2\2\u00b2\u00b3\7\65\2\2\u00b3\u00b5\5"+
		"\22\n\2\u00b4\u00ae\3\2\2\2\u00b4\u00b1\3\2\2\2\u00b5\u00b8\3\2\2\2\u00b6"+
		"\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\21\3\2\2\2\u00b8\u00b6\3\2\2"+
		"\2\u00b9\u00ba\b\n\1\2\u00ba\u00bb\5\24\13\2\u00bb\u00c7\3\2\2\2\u00bc"+
		"\u00bd\f\5\2\2\u00bd\u00be\7\66\2\2\u00be\u00c6\5\24\13\2\u00bf\u00c0"+
		"\f\4\2\2\u00c0\u00c1\7\67\2\2\u00c1\u00c6\5\24\13\2\u00c2\u00c3\f\3\2"+
		"\2\u00c3\u00c4\78\2\2\u00c4\u00c6\5\24\13\2\u00c5\u00bc\3\2\2\2\u00c5"+
		"\u00bf\3\2\2\2\u00c5\u00c2\3\2\2\2\u00c6\u00c9\3\2\2\2\u00c7\u00c5\3\2"+
		"\2\2\u00c7\u00c8\3\2\2\2\u00c8\23\3\2\2\2\u00c9\u00c7\3\2\2\2\u00ca\u00d0"+
		"\5\26\f\2\u00cb\u00cc\7\64\2\2\u00cc\u00d0\5\26\f\2\u00cd\u00ce\7\65\2"+
		"\2\u00ce\u00d0\5\26\f\2\u00cf\u00ca\3\2\2\2\u00cf\u00cb\3\2\2\2\u00cf"+
		"\u00cd\3\2\2\2\u00d0\25\3\2\2\2\u00d1\u00d7\5\32\16\2\u00d2\u00d3\7\61"+
		"\2\2\u00d3\u00d4\5\20\t\2\u00d4\u00d5\7\62\2\2\u00d5\u00d7\3\2\2\2\u00d6"+
		"\u00d1\3\2\2\2\u00d6\u00d2\3\2\2\2\u00d7\27\3\2\2\2\u00d8\u00db\7?\2\2"+
		"\u00d9\u00db\7@\2\2\u00da\u00d8\3\2\2\2\u00da\u00d9\3\2\2\2\u00db\31\3"+
		"\2\2\2\u00dc\u00e1\5\34\17\2\u00dd\u00e1\5\30\r\2\u00de\u00e1\5\36\20"+
		"\2\u00df\u00e1\7\32\2\2\u00e0\u00dc\3\2\2\2\u00e0\u00dd\3\2\2\2\u00e0"+
		"\u00de\3\2\2\2\u00e0\u00df\3\2\2\2\u00e1\33\3\2\2\2\u00e2\u00e3\t\2\2"+
		"\2\u00e3\35\3\2\2\2\u00e4\u00e7\7D\2\2\u00e5\u00e7\7E\2\2\u00e6\u00e4"+
		"\3\2\2\2\u00e6\u00e5\3\2\2\2\u00e7\37\3\2\2\2\u00e8\u00e9\5\36\20\2\u00e9"+
		"!\3\2\2\2\u00ea\u00eb\7\61\2\2\u00eb\u00ec\5$\23\2\u00ec\u00ed\7\62\2"+
		"\2\u00ed#\3\2\2\2\u00ee\u00f3\5&\24\2\u00ef\u00f0\7\63\2\2\u00f0\u00f2"+
		"\5&\24\2\u00f1\u00ef\3\2\2\2\u00f2\u00f5\3\2\2\2\u00f3\u00f1\3\2\2\2\u00f3"+
		"\u00f4\3\2\2\2\u00f4%\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f6\u00f9\5\36\20"+
		"\2\u00f7\u00f9\5\34\17\2\u00f8\u00f6\3\2\2\2\u00f8\u00f7\3\2\2\2\u00f9"+
		"\'\3\2\2\2\37,\65<AHLTZahsz\u0081\u0088\u008f\u0096\u00a4\u00a9\u00b4"+
		"\u00b6\u00c5\u00c7\u00cf\u00d6\u00da\u00e0\u00e6\u00f3\u00f8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
