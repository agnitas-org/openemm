/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser.antlr4;

import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.agnitas.emm.core.target.eql.ast.AbstractAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AnnotationBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AnnotationBooleanEqlNode.AnnotationType;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.ConstantListEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.InRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.LikeRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NegExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantListItemEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantListItemEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantWithEscapeCharsAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.TodayAtomEqlNode;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.AddExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.AndBooleanAndExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.AndBooleanOrExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.AtomFactorContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.BooleanExpressionTargetRuleContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Boolean_expressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ClickedInMailingRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Constant_listContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Constant_listelementContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Constant_listelementsContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Date_formatContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.DivTermContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.EmptyRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.EmptyTargetRuleContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.EqRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.FactorSignedFactorContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.GeqRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.GtRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.InRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.LeqRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.LikeRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.LtRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ModTermContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.MulTermContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.NegativeSignedFactorContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.NeqRelationalRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.NotBooleanAndExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.NotBooleanNotExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.NumericConstantAtomContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.Numeric_constantContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.OpenedMailingRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.OrBooleanOrExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ParenthesisBooleanNotExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ParenthesisBooleanParenthesisExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ParenthesisFactorContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.PositiveSignedFactorContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ProfileFieldAtomContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.QuotedProfilefieldNameContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ReceivedMailingRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.RelationalBooleanParenthesisExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.SignedFactorTermContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.SimpleProfilefieldNameContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.StringConstantAtomContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.StringDoubleQuoteContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.StringSingleQuoteContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.SubExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.TermExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.TodayConstantAtomContext;

/**
 * Callback for parser events.
 * 
 * Be careful when reusing an instance of this class. This might end up in strange behaviour.
 * Best practice is to use one instance per parsing.
 */
abstract class AbstractAntlr4SyntaxTreeGeneratorEqlListener implements EqlTreeGeneratorListener {

	/** Stack used to store nodes during generation of syntax tree. */
	final Stack<AbstractEqlNode> nodeStack = new Stack<>();
	
	/** Configuration of parser. */
	private final EqlParserConfiguration parserConfiguration;
	
	/**
	 * Creates a new instance with given parser configuration.
	 * 
	 * @param configuration parser configuration
	 */
	public AbstractAntlr4SyntaxTreeGeneratorEqlListener(final EqlParserConfiguration configuration) {
		this.parserConfiguration = configuration;
	}
	
	/**
	 * Returns the root node of the parsed EQL expression.
	 * 	
	 * @return root node of syntax tree
	 * 
	 * @throws EqlParserException if parsing failed
	 */
	@Override
	public BooleanExpressionTargetRuleEqlNode getSyntaxTreeRootNode() throws EqlParserException {
		assert(nodeStack.size() <= 1);	// Just the root node or none (failure)
		
		if(nodeStack.size() == 1) {
			return (BooleanExpressionTargetRuleEqlNode) nodeStack.peek();
		} else {
			throw new EqlParserException("Parsing failed");
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------- Top level node 
	@Override
	public final void exitEmptyTargetRule(final EmptyTargetRuleContext ctx) {
		assert nodeStack.size() == 0;
		
		nodeStack.push(new BooleanExpressionTargetRuleEqlNode(Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}
	
	@Override 
	public final void exitBooleanExpressionTargetRule(final BooleanExpressionTargetRuleContext ctx) { 
		assert nodeStack.size() == 1;
		
		final AbstractBooleanEqlNode child = (AbstractBooleanEqlNode) nodeStack.pop();
				
		nodeStack.push(new BooleanExpressionTargetRuleEqlNode(child));
	}

	
	// ------------------------------------------------------------------------------------------------------------------------------- Boolean nodes

	@Override
	public void exitAndBooleanAndExpression(AndBooleanAndExpressionContext ctx) {
		assert(nodeStack.size() >= 2);
		
		AbstractBooleanEqlNode right = (AbstractBooleanEqlNode) nodeStack.pop();
		AbstractBooleanEqlNode left = (AbstractBooleanEqlNode) nodeStack.pop();

		BinaryOperatorBooleanEqlNode node = new BinaryOperatorBooleanEqlNode(left, BinaryOperatorBooleanEqlNode.InfixOperator.AND, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitOrBooleanOrExpression(OrBooleanOrExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractBooleanEqlNode right = (AbstractBooleanEqlNode) nodeStack.pop();
		AbstractBooleanEqlNode left = (AbstractBooleanEqlNode) nodeStack.pop();

		BinaryOperatorBooleanEqlNode node = new BinaryOperatorBooleanEqlNode(left, BinaryOperatorBooleanEqlNode.InfixOperator.OR, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitNotBooleanNotExpression(NotBooleanNotExpressionContext ctx) {
		assert(nodeStack.size() >= 1);

		AbstractBooleanEqlNode child = (AbstractBooleanEqlNode) nodeStack.pop();
		
		NotOperatorBooleanEqlNode node = new NotOperatorBooleanEqlNode(child);
		
		nodeStack.push(node);
	}

	@Override
	public void exitRelationalBooleanParenthesisExpression(RelationalBooleanParenthesisExpressionContext ctx) {
		assert(nodeStack.size() >= 1);
		
		AbstractRelationalEqlNode child = (AbstractRelationalEqlNode) nodeStack.pop();

		// Just for bridging between boolean-level and relational-level
		RelationalBooleanEqlNode node = new RelationalBooleanEqlNode(child);
		
		nodeStack.push(node);
	}
	
	@Override 
	public void exitParenthesisBooleanParenthesisExpression(final ParenthesisBooleanParenthesisExpressionContext ctx) {
		assert(nodeStack.size() >= 1);
		
		if(this.parserConfiguration.getCreateBooleanAnnotationNodes()) {
			final AbstractBooleanEqlNode child = (AbstractBooleanEqlNode) nodeStack.pop();
			
			final AnnotationBooleanEqlNode node = new AnnotationBooleanEqlNode(child, AnnotationType.PARENTHESIS);
			
			nodeStack.push(node);
		}
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------- Relational nodes

	@Override
	public void exitEqRelationalRelationalExpression(EqRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;
		
		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.EQ, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitGeqRelationalRelationalExpression(GeqRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.GEQ, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitLtRelationalRelationalExpression(LtRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;
		
		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();

		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.LT, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitNeqRelationalRelationalExpression(NeqRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;
	
		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();

		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.NEQ, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitLeqRelationalRelationalExpression(LeqRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();

		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.LEQ, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitGtRelationalRelationalExpression(GtRelationalRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		String dateFormat = ctx.DATEFORMAT() != null ? StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop()).getValue()) : null;
		
		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();

		BinaryOperatorRelationalEqlNode node = new BinaryOperatorRelationalEqlNode(left, BinaryOperatorRelationalEqlNode.InfixOperator.GT, right, dateFormat);
		
		nodeStack.push(node);
	}

	@Override
	public void exitInRelationalExpression(InRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);
		
		ConstantListEqlNode listNode = (ConstantListEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode expressionNode = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		// Check, if "NOT" token is present
		boolean notFlag = ctx.NOT() != null;
		
		InRelationalEqlNode node = new InRelationalEqlNode(expressionNode, listNode, notFlag);
		
		nodeStack.push(node);
	}

	@Override
	public void exitLikeRelationalExpression(LikeRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);
		
		StringConstantWithEscapeCharsAtomEqlNode patternNode = (StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode expressionNode = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		// Check, if "NOT" token is present
		boolean notFlag = ctx.NOT() != null;
		
		LikeRelationalEqlNode node = new LikeRelationalEqlNode(expressionNode, patternNode, notFlag);
		
		nodeStack.push(node);
	}

	@Override
	public void exitContainsRelationalExpression(EqlGrammarParser.ContainsRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		StringConstantWithEscapeCharsAtomEqlNode patternNode = (StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode expressionNode = (AbstractExpressionalEqlNode) nodeStack.pop();

		// Check, if "NOT" token is present
		boolean notFlag = ctx.NOT() != null;

		ContainsRelationalEqlNode node = new ContainsRelationalEqlNode(expressionNode, patternNode, notFlag);

		nodeStack.push(node);
	}

	@Override
	public void exitStartsWithRelationalExpression(EqlGrammarParser.StartsWithRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		StringConstantWithEscapeCharsAtomEqlNode patternNode = (StringConstantWithEscapeCharsAtomEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode expressionNode = (AbstractExpressionalEqlNode) nodeStack.pop();

		// Check, if "NOT" token is present
		boolean notFlag = ctx.NOT() != null;

		StartsWithRelationalEqlNode node = new StartsWithRelationalEqlNode(expressionNode, patternNode, notFlag);

		nodeStack.push(node);
	}

	@Override
	public void exitEmptyRelationalExpression(EmptyRelationalExpressionContext ctx) {
		assert(nodeStack.size() >= 1);
		
		AbstractExpressionalEqlNode expressionNode = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		// Check, if "NOT" token is present
		boolean notFlag = ctx.NOT() != null;
		
		EmptyRelationalEqlNode node = new EmptyRelationalEqlNode(expressionNode, notFlag);
		
		nodeStack.push(node);
	}

	@Override
	public void exitReceivedMailingRelationalExpression(ReceivedMailingRelationalExpressionContext ctx) {
		// Due to grammar, this is guaranteed to succeed.
		int mailingId = Integer.parseInt(ctx.NUMERIC_ID().getText());
		
		nodeStack.push(new ReceivedMailingRelationalEqlNode(mailingId, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------- Expressional  nodes

	@Override
	public void exitAddExpression(AddExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorExpressionalEqlNode node = new BinaryOperatorExpressionalEqlNode(left, BinaryOperatorExpressionalEqlNode.InfixOperator.ADD, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitSubExpression(SubExpressionContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorExpressionalEqlNode node = new BinaryOperatorExpressionalEqlNode(left, BinaryOperatorExpressionalEqlNode.InfixOperator.SUB, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitMulTerm(MulTermContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorExpressionalEqlNode node = new BinaryOperatorExpressionalEqlNode(left, BinaryOperatorExpressionalEqlNode.InfixOperator.MUL, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitDivTerm(DivTermContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorExpressionalEqlNode node = new BinaryOperatorExpressionalEqlNode(left, BinaryOperatorExpressionalEqlNode.InfixOperator.DIV, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitModTerm(ModTermContext ctx) {
		assert(nodeStack.size() >= 2);

		AbstractExpressionalEqlNode right = (AbstractExpressionalEqlNode) nodeStack.pop();
		AbstractExpressionalEqlNode left = (AbstractExpressionalEqlNode) nodeStack.pop();
		
		BinaryOperatorExpressionalEqlNode node = new BinaryOperatorExpressionalEqlNode(left, BinaryOperatorExpressionalEqlNode.InfixOperator.MOD, right);
		
		nodeStack.push(node);
	}

	@Override
	public void exitNegativeSignedFactor(NegativeSignedFactorContext ctx) {
		assert(nodeStack.size() >= 1);
		
		AbstractExpressionalEqlNode child = (AbstractExpressionalEqlNode) nodeStack.pop();
		NegExpressionalEqlNode node = new NegExpressionalEqlNode(child);
		
		nodeStack.push(node);
	}

	@Override
	public void exitAtomFactor(AtomFactorContext ctx) {
		assert(nodeStack.size() >= 1);
		
		AbstractAtomEqlNode child = (AbstractAtomEqlNode) nodeStack.pop();
		
		AtomExpressionalEqlNode node = new AtomExpressionalEqlNode(child);
		
		nodeStack.push(node);
	}
	
	
	// ------------------------------------------------------------------------------------------------------------------------------- Atomar  nodes

	@Override
	public void exitQuotedProfilefieldName(QuotedProfilefieldNameContext ctx) {
		String tokenContent = ctx.QUOTED_IDENTIFIER().getText();

		// Cut off String delimiter
		String tokenValue = tokenContent.substring(1, tokenContent.length() - 1);

		ProfileFieldAtomEqlNode node = new ProfileFieldAtomEqlNode(tokenValue, Antlr4BasedCodeLocation.fromParserRuleContext(ctx));

		nodeStack.push(node);
	}


	@Override
	public void exitSimpleProfilefieldName(SimpleProfilefieldNameContext ctx) {
		ProfileFieldAtomEqlNode node = new ProfileFieldAtomEqlNode(ctx.IDENTIFIER().getText(), Antlr4BasedCodeLocation.fromParserRuleContext(ctx));

		nodeStack.push(node);
	}

	@Override
	public void exitStringSingleQuote(StringSingleQuoteContext ctx) {
		String tokenContent = ctx.STRING_SINGLEQUOTE().getText();
		
		// Cut off String delimiter
		String tokenValue = tokenContent.substring(1, tokenContent.length() - 1);

		nodeStack.push(new StringConstantWithEscapeCharsAtomEqlNode(tokenValue, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}

	@Override
	public void exitStringDoubleQuote(StringDoubleQuoteContext ctx) {
		String tokenContent = ctx.STRING_DOUBLEQUOTE().getText();
		
		// Cut off String delimiter
		String tokenValue = tokenContent.substring(1, tokenContent.length() - 1);
		
		nodeStack.push(new StringConstantWithEscapeCharsAtomEqlNode(tokenValue, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}

	@Override
	public void exitNumeric_constant(Numeric_constantContext ctx) {
		if(ctx.NUMERIC_ID() != null) {
			String tokenValue = ctx.NUMERIC_ID().getText();
			
			nodeStack.push(new NumericConstantAtomEqlNode(tokenValue, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
		} else {
			String tokenValue = ctx.NUMERIC_VALUE().getText();
			
			nodeStack.push(new NumericConstantAtomEqlNode(tokenValue, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
		}
	}

	@Override
	public void exitTodayConstantAtom(TodayConstantAtomContext ctx) {
		nodeStack.push(new TodayAtomEqlNode(Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}

	// ------------------------------------------------------------------------------------------------------------------------------- List  nodes

	@Override
	public void enterConstant_list(Constant_listContext ctx) {
		 // "enter"-event is used here. We need the surrounding node before creating list elements, because list elements are added to list node.
		nodeStack.push(new ConstantListEqlNode());		
	}

	@Override
	public void exitConstant_listelement(Constant_listelementContext ctx) {
		assert(nodeStack.size() >= 2);
		
		AbstractAtomEqlNode constantNode = (AbstractAtomEqlNode) nodeStack.pop();
		ConstantListEqlNode listNode = (ConstantListEqlNode) nodeStack.peek(); // Get list node, but leave it on stack!
		
		if(constantNode instanceof NumericConstantAtomEqlNode) {
			listNode.addListElement(new NumericConstantListItemEqlNode((NumericConstantAtomEqlNode) constantNode));
		} else if(constantNode instanceof StringConstantWithEscapeCharsAtomEqlNode) {
			listNode.addListElement(new StringConstantListItemEqlNode((StringConstantWithEscapeCharsAtomEqlNode) constantNode));
		} else {
			throw new RuntimeException("Handled constant type for lists: " + constantNode.getClass().getCanonicalName());
		}
	}


	
	// ------------------------------------------------------------------------------------------------------------------------------- Unused because just forwarding AST node

	@Override public void exitBoolean_expression(Boolean_expressionContext ctx) { /* No changes on AST. */ }
	@Override public void exitTermExpression(TermExpressionContext ctx) { /* No changes on AST. */ }
	@Override public void exitAndBooleanOrExpression(AndBooleanOrExpressionContext ctx) { /* No changes on AST. */ }
	@Override public void exitParenthesisFactor(ParenthesisFactorContext ctx) { /* No changes on AST. */ }
	@Override public void exitParenthesisBooleanNotExpression(ParenthesisBooleanNotExpressionContext ctx) { /* No changes on AST. */ }
	@Override public void exitFactorSignedFactor(FactorSignedFactorContext ctx) { /* No changes on AST. */ }
	@Override public void exitSignedFactorTerm(SignedFactorTermContext ctx) { /* No changes on AST. */ }
	@Override public void exitNotBooleanAndExpression(NotBooleanAndExpressionContext ctx) { /* No changes on AST. */ }
	@Override public void exitStringConstantAtom(StringConstantAtomContext ctx) { /* No changes on AST. */ }
	@Override public void exitConstant_listelements(Constant_listelementsContext ctx) { /* No changes on AST. */ }
	@Override public void exitPositiveSignedFactor(PositiveSignedFactorContext ctx) { /* No changes on AST. */ }
	@Override public void exitNumericConstantAtom(NumericConstantAtomContext ctx) { /* No changes on AST. */ }
	@Override public void exitProfileFieldAtom(ProfileFieldAtomContext ctx) { /* No changes on AST. */ }

	
	
	// ------------------------------------------------------------------------------------------------------------------------------- Unused callbacks

	@Override public void enterEmptyTargetRule(EmptyTargetRuleContext ctx) { /* unused */ }
	@Override public void enterStartsWithRelationalExpression(EqlGrammarParser.StartsWithRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterEveryRule(ParserRuleContext ctx) { /* unused */ }
	@Override public void exitEveryRule(ParserRuleContext ctx) { /* unused */ }
	@Override public void visitTerminal(TerminalNode ctx) { /* unused */ }
	@Override public void enterQuotedProfilefieldName(QuotedProfilefieldNameContext ctx) { /* unused */ }
	@Override public void enterSimpleProfilefieldName(SimpleProfilefieldNameContext ctx) { /* unused */ }
	@Override public void enterNumeric_constant(Numeric_constantContext ctx) { /* unused */ }
	@Override public void enterBoolean_expression(Boolean_expressionContext ctx) { /* unused */ }
	@Override public void enterBooleanExpressionTargetRule(BooleanExpressionTargetRuleContext ctx) { /* unused */ }
	@Override public void enterPositiveSignedFactor(PositiveSignedFactorContext ctx) { /* unused */ }
	@Override public void enterTermExpression(TermExpressionContext ctx) { /* unused */ }
	@Override public void enterDivTerm(DivTermContext ctx) { /* unused */ }
	@Override public void enterParenthesisBooleanParenthesisExpression(ParenthesisBooleanParenthesisExpressionContext ctx) { /* unused */ }
	@Override public void enterAndBooleanOrExpression(AndBooleanOrExpressionContext ctx) { /* unused */ }
	@Override public void enterAndBooleanAndExpression(AndBooleanAndExpressionContext ctx) { /* unused */ }
	@Override public void enterParenthesisFactor(ParenthesisFactorContext ctx) { /* unused */ }
	@Override public void enterSubExpression(SubExpressionContext ctx) { /* unused */ }
	@Override public void enterNotBooleanNotExpression(NotBooleanNotExpressionContext ctx) { /* unused */ }
	@Override public void enterOrBooleanOrExpression(OrBooleanOrExpressionContext ctx) { /* unused */ }
	@Override public void enterParenthesisBooleanNotExpression(ParenthesisBooleanNotExpressionContext ctx) { /* unused */ }
	@Override public void enterFactorSignedFactor(FactorSignedFactorContext ctx) { /* unused */ }
	@Override public void enterProfileFieldAtom(ProfileFieldAtomContext ctx) { /* unused */ }
	@Override public void enterMulTerm(MulTermContext ctx) { /* unused */ }
	@Override public void enterNegativeSignedFactor(NegativeSignedFactorContext ctx) { /* unused */ }
	@Override public void enterAddExpression(AddExpressionContext ctx) { /* unused */ }
	@Override public void enterNumericConstantAtom(NumericConstantAtomContext ctx) { /* unused */ }
	@Override public void enterSignedFactorTerm(SignedFactorTermContext ctx) { /* unused */ }
	@Override public void enterModTerm(ModTermContext ctx) { /* unused */ }
	@Override public void enterStringConstantAtom(StringConstantAtomContext ctx) { /* unused */ }
	@Override public void enterNotBooleanAndExpression(NotBooleanAndExpressionContext ctx) { /* unused */ }
	@Override public void enterStringSingleQuote(StringSingleQuoteContext ctx) { /* unused */ }
	@Override public void enterStringDoubleQuote(StringDoubleQuoteContext ctx) { /* unused */ }
	@Override public void enterEqRelationalRelationalExpression(EqRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterEmptyRelationalExpression(EmptyRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterGeqRelationalRelationalExpression(GeqRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterLtRelationalRelationalExpression(LtRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterRelationalBooleanParenthesisExpression(RelationalBooleanParenthesisExpressionContext ctx) { /* unused */ }
	@Override public void enterNeqRelationalRelationalExpression(NeqRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterLeqRelationalRelationalExpression(LeqRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterLikeRelationalExpression(LikeRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterGtRelationalRelationalExpression(GtRelationalRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterInRelationalExpression(InRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterConstant_listelement(Constant_listelementContext ctx) { /* unused */ }
	@Override public void enterConstant_listelements(Constant_listelementsContext ctx) { /* unused */ }
	@Override public void enterContainsRelationalExpression(EqlGrammarParser.ContainsRelationalExpressionContext ctx) { /* unused */ }
	@Override public void exitConstant_list(Constant_listContext ctx) { /* unused */ }
	@Override public void enterAtomFactor(AtomFactorContext ctx) { /* unused */ }
	@Override public void enterOpenedMailingRelationalExpression(OpenedMailingRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterReceivedMailingRelationalExpression(ReceivedMailingRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterClickedInMailingRelationalExpression(ClickedInMailingRelationalExpressionContext ctx) { /* unused */ }
	@Override public void enterTodayConstantAtom(TodayConstantAtomContext ctx) { /* unused */ }
	@Override public void enterDate_format(Date_formatContext ctx) { /* unused */ }
	@Override public void exitDate_format(Date_formatContext ctx) { /* unused */ }
	@Override public void visitErrorNode(ErrorNode ctx) { /* unused */ }

}
