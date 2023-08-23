/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import java.util.List;

import com.agnitas.emm.core.target.eql.ast.AbstractAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractConstantListItemEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AnnotationBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ConstantListEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.InRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.LikeRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NegExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantListItemEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantListItemEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantWithEscapeCharsAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.TodayAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.transform.ShiftNotDownTransform;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;

/**
 * Code generator that does post-order traversal on syntax tree.
 * 
 * @see EqlParserConfiguration
 */
public class CodeGenerator {

	/**
	 * Generate code for given syntax tree root.
	 *  
	 * @param root root node of syntax tree
	 * @param callback callback to generate target code for visited node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	public void generateCode(final BooleanExpressionTargetRuleEqlNode root, final CodeGeneratorCallback callback) throws CodeGeneratorException {
		generateCode(root, callback, CodeGenerationFlags.DEFAULT_FLAGS);
	}
	
	/**
	 * Generate code for given syntax tree root.
	 *  
	 * @param root root node of syntax tree
	 * @param callback callback to generate target code for visited node
	 * @param flags flags to alter behavior of code generation
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	public void generateCode(final BooleanExpressionTargetRuleEqlNode root, final CodeGeneratorCallback callback, final CodeGenerationFlags flags) throws CodeGeneratorException {
		doGenerateCode(
				root, 
				callback, 
				flags != null ? flags : CodeGenerationFlags.DEFAULT_FLAGS);
	}
	
	protected final void doGenerateCode(final BooleanExpressionTargetRuleEqlNode root, final CodeGeneratorCallback callback, final CodeGenerationFlags flags) throws CodeGeneratorException {
		try {
			final BooleanExpressionTargetRuleEqlNode transformedRoot = ShiftNotDownTransform.shiftNotDown(root, flags);
			
			if(root.getChild().isPresent()) {
				handleAbstractBooleanEqlNode(transformedRoot.getChild().get(), callback);
				callback.finished();
			} else {
				callback.finishedWithEmptyTargetRule();
			}
		} catch(CodeGeneratorException e) {
			callback.error(e);
			throw e;
		}
	}
	
	/**
	 * Processes a syntax node of Type {@link AbstractBooleanEqlNode} and dispatches processing to
	 * method handle specific sub-type of node.
	 * 
	 * @param node boolean node to handle
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 * @throws UnhandledSyntaxNodeException on implementation error when node type is not handled
	 */
	protected void handleAbstractBooleanEqlNode(AbstractBooleanEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		if(node instanceof BinaryOperatorBooleanEqlNode) {
			handleBinaryOperatorBooleanEqlNode((BinaryOperatorBooleanEqlNode) node, callback);
		} else if(node instanceof NotOperatorBooleanEqlNode) {
			handleNotOperatorBooleanEqlNode((NotOperatorBooleanEqlNode) node, callback);
		} else if(node instanceof RelationalBooleanEqlNode) {
			handleRelationalBooleanEqlNode((RelationalBooleanEqlNode) node, callback);
		} else if(node instanceof AnnotationBooleanEqlNode) {
			handleAbstractBooleanEqlNode(((AnnotationBooleanEqlNode) node).getChild(), callback);
		} else {
			throw new UnhandledSyntaxNodeException(node);
		}
		
		callback.postOrderAbstractBooleanEqlNode(node);
	}

	/**
	 * Handles the node, that embeds relational expressions in boolean expressions.
	 * 
	 * @param node node representing a relational expression
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleRelationalBooleanEqlNode(RelationalBooleanEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractRelationalEqlNode(node.getChild(), callback);
		
		callback.postOrderRelationalBooleanEqlNode(node);
	}

	/**
	 * Handles the NOT operator in a boolean expression.
	 * 
	 * @param node node representing the NOT operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleNotOperatorBooleanEqlNode(NotOperatorBooleanEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractBooleanEqlNode(node.getChild(), callback);
		
		callback.postOrderNotOperatorBooleanEqlNode(node);
	}

	/**
	 * Handles a binary operator in a boolean expression.
	 * 
	 * @param node node representing the binary operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleBinaryOperatorBooleanEqlNode(BinaryOperatorBooleanEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractBooleanEqlNode(node.getLeft(), callback);
		handleAbstractBooleanEqlNode(node.getRight(), callback);
		
		callback.postOrderBinaryOperatorBooleanEqlNode(node);
	}
	
	/**
	 * Handles an arbitrary relational node.
	 * 
	 * @param node relational node
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleAbstractRelationalEqlNode(AbstractRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		doHandleAbstractRelationalEqlNode(node, callback);
		
		callback.postOrderAbstractRelationalEqlNode(node);
	}

	protected void doHandleAbstractRelationalEqlNode(AbstractRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		if(node instanceof EmptyRelationalEqlNode) {
			handleEmptyRelationalEqlNode((EmptyRelationalEqlNode) node, callback);
		} else if(node instanceof InRelationalEqlNode) {
			handleInRelationalEqlNode((InRelationalEqlNode) node, callback);
		} else if(node instanceof LikeRelationalEqlNode) {
			handleLikeRelationalEqlNode((LikeRelationalEqlNode) node, callback);
		} else if(node instanceof ContainsRelationalEqlNode) {
			handleContainsRelationalEqlNode((ContainsRelationalEqlNode) node, callback);
		} else if(node instanceof StartsWithRelationalEqlNode) {
			handleStartsWithRelationalEqlNode((StartsWithRelationalEqlNode) node, callback);
		} else if(node instanceof BinaryOperatorRelationalEqlNode) {
			handleBinaryOperatorRelationalEqlNode((BinaryOperatorRelationalEqlNode) node, callback);
		} else if(node instanceof OpenedMailingRelationalEqlNode) {
			handleOpenedMailingRelationalEqlNode((OpenedMailingRelationalEqlNode) node, callback);
		} else if(node instanceof ReceivedMailingRelationalEqlNode) {
			handleReceivedMailingRelationalEqlNode((ReceivedMailingRelationalEqlNode) node, callback);
		} else if(node instanceof ClickedInMailingRelationalEqlNode) {
			handleClickedInMailingRelationalEqlNode((ClickedInMailingRelationalEqlNode) node, callback);
		} else {
			throw new UnhandledSyntaxNodeException(node);
		}
	}
	
	/**
	 * Handles a binary operator in a relational expression.
	 * 
	 * @param node node representing a binary operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleBinaryOperatorRelationalEqlNode(BinaryOperatorRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);
		handleAbstractExpressionalEqlNode(node.getRight(), callback);
		
		try {
			EqlDateFormat dateFormat = EqlDateFormat.parse(node.getDateFormat());
			
			callback.postOrderBinaryOperatorRelationalEqlNode(node, dateFormat);
		} catch(InvalidEqlDateFormatException e) {
			throw new DateFormatFaultyCodeException(node, e);
		}
	}
	
	/**
	 * Handles the operator </i>OPENED MAILING</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleOpenedMailingRelationalEqlNode(final OpenedMailingRelationalEqlNode node, final CodeGeneratorCallback callback) throws CodeGeneratorException {
		callback.postOrderOpenedMailingRelationalEqlNode(node);
	}
	
	/**
	 * Handles the operator </i>RECEIVED MAILING</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleReceivedMailingRelationalEqlNode(ReceivedMailingRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		callback.postOrderReceivedMailingRelationalEqlNode(node);
	}
	
	/**
	 * Handles the operator </i>CLICKED (LINK) IN MAILING</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleClickedInMailingRelationalEqlNode(final ClickedInMailingRelationalEqlNode node, final CodeGeneratorCallback callback) throws CodeGeneratorException {
		callback.postOrderClickedMailingRelationalEqlNode(node);
	}

	/**
	 * Handles the operator </i>LIKE</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleLikeRelationalEqlNode(LikeRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);
		
		/*
		 * Calling the post-order method is slightly different from the other calls,
		 * because we have to do some translations on input data (matching pattern),
		 * which depends on target language and is therefore part of the code generation
		 * of the LIKE operator.
		 */
		callback.postOrderLikeRelationalEqlNode(node, node.getRight());
	}

	/**
	 * Handles the operator </i>CONTAINS</i> in a relational expression.
	 *
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 *
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleContainsRelationalEqlNode(ContainsRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);

		callback.postOrderContainsRelationalEqlNode(node, node.getRight());
	}

	/**
	 * Handles the operator </i>STARTS WITH</i> in a relational expression.
	 *
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 *
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleStartsWithRelationalEqlNode(StartsWithRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);

		callback.postOrderStartsWithRelationalEqlNode(node, node.getRight());
	}

	/**
	 * Handles the operator </i>IN</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleInRelationalEqlNode(InRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);
		handleConstantListEqlNode(node.getRight(), callback);
		
		callback.postOrderInRelationalEqlNode(node);
	}

	/**
	 * Handles the operator </i>IS (NOT) EMPTY</i> in a relational expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleEmptyRelationalEqlNode(EmptyRelationalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getChild(), callback);

		callback.postOrderEmptyRelationalEqlNode(node);
	}
	
	/**
	 * Handles an arbitrary expression node.
	 * 
	 * @param node node representing an expression
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleAbstractExpressionalEqlNode(AbstractExpressionalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		if(node instanceof AtomExpressionalEqlNode) {
			handleAtomExpressionalEqlNode((AtomExpressionalEqlNode) node, callback);
		} else if(node instanceof NegExpressionalEqlNode) {
			handleNegExpressionalEqlNode((NegExpressionalEqlNode) node, callback);
		} else if(node instanceof BinaryOperatorExpressionalEqlNode) {
			handleBinaryOperatorExpressionalEqlNode((BinaryOperatorExpressionalEqlNode) node, callback);
		} else {
			throw new UnhandledSyntaxNodeException(node);
		}
		
		callback.postOrderAbstractExpressionalEqlNode(node);
	}

	/**
	 * Handles a binary operator in an arithmetic expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleBinaryOperatorExpressionalEqlNode(BinaryOperatorExpressionalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getLeft(), callback);
		handleAbstractExpressionalEqlNode(node.getRight(), callback);
	
		callback.postOrderBinaryOperatorExpressionalEqlNode(node);
	}

	/**
	 * Handles the negation operator in an arithmetic expression.
	 * 
	 * @param node node representing the operator
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleNegExpressionalEqlNode(NegExpressionalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractExpressionalEqlNode(node.getChild(), callback);

		callback.postOrderNegExpressionalEqlNode(node);
	}

	/**
	 * Handles an atomic element in an arithmetic expression.
	 * 
	 * @param node node representing the atomic element
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleAtomExpressionalEqlNode(AtomExpressionalEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		handleAbstractAtomEqlNode(node.getChild(), callback);

		callback.postOrderAtomExpressionalEqlNode(node);
	}

	/**
	 * Handles a list of constants of an IN operator
	 * 
	 * @param node node representing the list
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleConstantListEqlNode(ConstantListEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		List<AbstractConstantListItemEqlNode> elements = node.elements();
		
		for(AbstractConstantListItemEqlNode element : elements) {
			handleAbstractConstantListItemEqlNode(element, callback);
		}
		
		callback.postOrderConstantListEqlNode(node);
	}
	
	/**
	 * Handles list of constant values.
	 * 
	 * @param node node representing list of constant values
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleAbstractConstantListItemEqlNode(AbstractConstantListItemEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		if(node instanceof NumericConstantListItemEqlNode) {
			handleAbstractAtomEqlNode(((NumericConstantListItemEqlNode)node).getNumericConstantNode(), callback);
		} else if(node instanceof StringConstantListItemEqlNode) {
			handleAbstractAtomEqlNode(((StringConstantListItemEqlNode)node).getStringConstantNode(), callback);
		} else {
			throw new UnhandledSyntaxNodeException(node);
		}
	}

	/**
	 * Handles an arbitrary atomic element
	 * 
	 * @param node node atomic element
	 * @param callback called after successfully handling the node
	 * 
	 * @throws CodeGeneratorException on errors during code generation
	 */
	protected void handleAbstractAtomEqlNode(AbstractAtomEqlNode node, CodeGeneratorCallback callback) throws CodeGeneratorException {
		if(node instanceof NumericConstantAtomEqlNode) {
			callback.terminalNumericConstantAtomEqlNode((NumericConstantAtomEqlNode) node);
		} else if (node instanceof ProfileFieldAtomEqlNode) {
			callback.terminalProfileFieldAtomEqlNode((ProfileFieldAtomEqlNode) node);
		} else if (node instanceof StringConstantWithEscapeCharsAtomEqlNode) {
			callback.terminalStringConstantWithoutEscapeCharsAtomEqlNode((StringConstantWithEscapeCharsAtomEqlNode) node);
		} else if (node instanceof TodayAtomEqlNode) {
			callback.terminalTodayAtomEqlNode(node);
		} else {
			throw new UnhandledSyntaxNodeException(node);
		}
	}
	
}
