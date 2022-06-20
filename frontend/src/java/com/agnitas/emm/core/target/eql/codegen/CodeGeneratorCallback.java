/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.ast.AbstractAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ConstantListEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.InRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.LikeRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NegExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantWithEscapeCharsAtomEqlNode;

/**
 * Callback interface for code generators. 
 * 
 * A code generator takes the root of a syntax tree and converts it to a specific
 * target language, like SQL.
 * 
 * The implementation of this interface should throw a {@link UnhandledSyntaxNodeException},
 * if a syntax node is encountered, that
 * <ul>
 *   <li>is unknown or</li>
 *   <li>has data, that cannot be handled (like new operators).</li>
 * </ul>
 */
public interface CodeGeneratorCallback {

	// --------------------------------------------------------------------------------------------------------------------- Callback signaling end of code generation
	/**
	 * Called, when code generation is completed.
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void finished() throws CodeGeneratorException;
	
	/**
	 * Called, when code generation is completed and no target rule was defines.
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void finishedWithEmptyTargetRule() throws CodeGeneratorException;

	/**
	 * Called, when code generation failed.
	 * 
	 * @param e Exception thrown during code generation
	 */
	void error(CodeGeneratorException e);

	// --------------------------------------------------------------------------------------------------------------------- Callback for boolean node
	/**
	 * Called, after code generation for an arbitrary boolean node.
	 * 
	 * @param node arbitrary boolean node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderAbstractBooleanEqlNode(AbstractBooleanEqlNode node) throws CodeGeneratorException;
	
	/**
	 * Called, after code generation for a boolean NOT node
	 * 
	 * @param node boolean NOT node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderNotOperatorBooleanEqlNode(NotOperatorBooleanEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for a boolean binary operator node
	 * 
	 * @param node boolean binary operator node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderBinaryOperatorBooleanEqlNode(BinaryOperatorBooleanEqlNode node) throws CodeGeneratorException;

	// --------------------------------------------------------------------------------------------------------------------- Callback for boolean to relational bridge nodes

	/**
	 * Called, after code generation for a node bridging between boolean and relational nodes
	 * 
	 * @param node node bridging between boolean and relational nodes
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderRelationalBooleanEqlNode(RelationalBooleanEqlNode node) throws CodeGeneratorException;
	
	// --------------------------------------------------------------------------------------------------------------------- Callback for relational nodes

	/**
	 * Called, after code generation for an arbitrary relational node.
	 * 
	 * @param node arbitrary relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderAbstractRelationalEqlNode(AbstractRelationalEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for an binary operator relational node.
	 * 
	 * @param node binary operator relational node
	 * @param dateFormat date format used for comparison of date values
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderBinaryOperatorRelationalEqlNode(BinaryOperatorRelationalEqlNode node, EqlDateFormat dateFormat) throws CodeGeneratorException;

	/**
	 * Called, after code generation for an IS EMPTY relational node.
	 * 
	 * @param node IS EMPTY relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderEmptyRelationalEqlNode(EmptyRelationalEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for an LIKE relational node.
	 * 
	 * @param node LIKE relational node
	 * @param stringConstantWithEscapeCharsAtomEqlNode String constant node containing the matching pattern
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderLikeRelationalEqlNode(LikeRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException;

	/**
	 * Called, after code generation for a CONTAINS relational node.
	 *
	 * @param node CONTAINS relational node
	 * @param stringConstantWithEscapeCharsAtomEqlNode String constant node containing the matching character sequence
	 *
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderContainsRelationalEqlNode(ContainsRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException;

	/**
	 * Called, after code generation for a STARTS WITH relational node.
	 *
	 * @param node STARTS WITH relational node
	 * @param stringConstantWithEscapeCharsAtomEqlNode String constant node containing the matching character sequence
	 *
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderStartsWithRelationalEqlNode(StartsWithRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException;

	/**
	 * Called, after code generation for an IN relational node.
	 * 
	 * @param node IN relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderInRelationalEqlNode(InRelationalEqlNode node) throws CodeGeneratorException;
	
	/**
	 * Called, after code generation for an OPENED MAILING relational node.
	 * 
	 * @param node OPENED MAILING relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderOpenedMailingRelationalEqlNode(OpenedMailingRelationalEqlNode node) throws CodeGeneratorException;
	
	/**
	 * Called, after code generation for an RECEIVED MAILING relational node.
	 * 
	 * @param node RECEIVED MAILING relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderReceivedMailingRelationalEqlNode(ReceivedMailingRelationalEqlNode node) throws CodeGeneratorException;
	
	/**
	 * Called, after code generation for an CLICKED IN MAILING relational node.
	 * 
	 * @param node CLICKED IN MAILING relational node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderClickedMailingRelationalEqlNode(ClickedInMailingRelationalEqlNode node) throws CodeGeneratorException;


	// --------------------------------------------------------------------------------------------------------------------- Callback for expressional nodes

	/**
	 * Called, after code generation for an arbitrary expressional node.
	 * 
	 * @param node arbitrary expressional node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderAbstractExpressionalEqlNode(AbstractExpressionalEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for binary operator expressional node.
	 * 
	 * @param node binary operator expressional node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderBinaryOperatorExpressionalEqlNode(BinaryOperatorExpressionalEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for negating (sign switching) expressional node.
	 * 
	 * @param node negating expressional node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderNegExpressionalEqlNode(NegExpressionalEqlNode node) throws CodeGeneratorException;

	/**
	 * Called, after code generation for an constant list node.
	 * 
	 * @param node expression list node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderConstantListEqlNode(ConstantListEqlNode node) throws CodeGeneratorException;
	
	// --------------------------------------------------------------------------------------------------------------------- Callback for expressional to atom bridge nodes
	/**
	 * Called after code generation for arbitrary atom nodes.
	 * 
	 * @param node atom node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void postOrderAtomExpressionalEqlNode(AtomExpressionalEqlNode node) throws CodeGeneratorException;

	// --------------------------------------------------------------------------------------------------------------------- Callback for terminal atom nodes
	/**
	 * Called after code generation for a numeric constant atom nodes.
	 * 
	 * @param node numeric constant atom node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void terminalNumericConstantAtomEqlNode(NumericConstantAtomEqlNode node) throws CodeGeneratorException;

	/**
	 * Called after code generation for a profile field name atom nodes.
	 * 
	 * @param node profile field name atom node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void terminalProfileFieldAtomEqlNode(ProfileFieldAtomEqlNode node) throws CodeGeneratorException;

	/**
	 * Called after code generation for a string constant atom nodes.
	 * The value still contains the escape sequences.
	 * 
	 * @param node string constant atom node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void terminalStringConstantWithEscapeCharsAtomEqlNode(StringConstantWithEscapeCharsAtomEqlNode node) throws CodeGeneratorException;
	
	/**
	 * Called after code generation for TODAY date atom nodes.
	 * 
	 * @param node string constant atom node
	 * 
	 * @throws CodeGeneratorException on errors during processing
	 */
	void terminalTodayAtomEqlNode(AbstractAtomEqlNode node) throws CodeGeneratorException;

}
