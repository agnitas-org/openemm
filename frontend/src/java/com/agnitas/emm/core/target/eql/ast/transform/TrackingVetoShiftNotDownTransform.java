/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast.transform;

import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AnnotationBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;

/**
 * This transform on the syntax tree shifts NOT operators down as far as possible applying
 * the rules of the De Morgan's Law (see article on <a href="https://en.wikipedia.org/wiki/De_Morgan%27s_laws">wikipedia</a>)
 * recursively to the nodes of the syntax tree.
 * 
 * In special cases (OPENED/RECEIVED/CLICKED IN/REVENVUE BY MAILING operator within NOT), a special condition to exclude all recipients
 * with tracking veto set will be added.
 * 
 * This transform should only be applied to syntax trees that are used for code generation.
 * 
 * To enable special code generation for NOT operator, the node sub-classed from {@link AbstractRelationalEqlNode} must implement the 
 * {@link SpecialTrackingVetoNotHandling} marker interface. 
 */
public final class TrackingVetoShiftNotDownTransform {
	
	/**
	 * Marker interface for special handling of tracking veto in combination with NOT operators.
	 */
	public interface SpecialTrackingVetoNotHandling { /* No methods. */ }

	public static final BooleanExpressionTargetRuleEqlNode shiftNotDown(final BooleanExpressionTargetRuleEqlNode node) {
		return shiftNotDown(node, false);
	}
	
	private static final BooleanExpressionTargetRuleEqlNode shiftNotDown(final BooleanExpressionTargetRuleEqlNode node, final boolean inNot) {
		if(node.getChild().isPresent()) {
			final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild().get(), inNot);
			
			// If same instance of child node, then subtree has not been changed. So we can return the node itself.
			if(newChild == node.getChild().get()) {
				return node;
			} else {
				// Different child node instances, so we need to create a node with new child node.
				return new BooleanExpressionTargetRuleEqlNode(newChild);
			}
		} else {
			return node;
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final AbstractBooleanEqlNode node, final boolean inNot) {
		if(node instanceof AnnotationBooleanEqlNode) {
			return shiftNotDown((AnnotationBooleanEqlNode) node, inNot);
		} else if(node instanceof BinaryOperatorBooleanEqlNode) {
			return shiftNotDown((BinaryOperatorBooleanEqlNode) node, inNot);
		} else if(node instanceof NotOperatorBooleanEqlNode) {
			return shiftNotDown((NotOperatorBooleanEqlNode) node, inNot);	
		} else if(node instanceof RelationalBooleanEqlNode) {
			return shiftNotDown((RelationalBooleanEqlNode) node, inNot);
		} else {
			throw new RuntimeException("Unhandled node type " + node.getClass().getCanonicalName());
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final AnnotationBooleanEqlNode node, final boolean inNot) {
		final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild(), inNot);
		
		if(newChild == node.getChild()) {
			return node;
		} else { 
			return new AnnotationBooleanEqlNode(newChild, node.getAnnotations());
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final BinaryOperatorBooleanEqlNode node, final boolean inNot) {
		final AbstractBooleanEqlNode newLeft = shiftNotDown(node.getLeft(), inNot);
		final AbstractBooleanEqlNode newRight = shiftNotDown(node.getRight(), inNot);
		
		if(newLeft == node.getLeft() && newRight == node.getRight()) {
			return node;
		} else {
			switch(node.getOperator()) {
			case AND: {
				if(inNot) {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.Operator.OR, newRight);
				} else {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.Operator.AND, newRight);
				}
			}
			
			case OR: {		
				if(inNot) {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.Operator.AND, newRight);
				} else {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.Operator.OR, newRight);
				}
			}
	
			default:
				throw new RuntimeException("Unhandled operator " + node.getOperator());
			}
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final NotOperatorBooleanEqlNode node, final boolean inNot) {
		final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild(), !inNot);
		
		return newChild;
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final RelationalBooleanEqlNode node, final boolean inNot) {
		if(inNot) {
			final AbstractRelationalEqlNode childNode = node.getChild();

			if(childNode instanceof SpecialTrackingVetoNotHandling) {
				final NotOperatorBooleanEqlNode newChildNode = new NotOperatorBooleanEqlNode(new RelationalBooleanEqlNode(childNode));
				
				final RelationalBooleanEqlNode trackingVetoExclusionNode1 = new RelationalBooleanEqlNode(
						new BinaryOperatorRelationalEqlNode(
								new AtomExpressionalEqlNode(new ProfileFieldAtomEqlNode("$tracking_veto", node.getStartLocation())),			// "$" marks this as internally generated identifier
								BinaryOperatorRelationalEqlNode.Operator.EQ, 
								new AtomExpressionalEqlNode(new NumericConstantAtomEqlNode("0", node.getStartLocation())),	
								null));				// Date format is not used in this comparison
				
				final RelationalBooleanEqlNode trackingVetoExclusionNode2 = new RelationalBooleanEqlNode(
						new EmptyRelationalEqlNode(
								new AtomExpressionalEqlNode(new ProfileFieldAtomEqlNode("$tracking_veto", node.getStartLocation())), false));
				
				final BinaryOperatorBooleanEqlNode trackingVetoExclusionNode = new BinaryOperatorBooleanEqlNode(
						trackingVetoExclusionNode1, 
						BinaryOperatorBooleanEqlNode.Operator.OR, 
						trackingVetoExclusionNode2);
				
				return new BinaryOperatorBooleanEqlNode(
						newChildNode,
						BinaryOperatorBooleanEqlNode.Operator.AND,
						trackingVetoExclusionNode
						);
			} else {
				return new NotOperatorBooleanEqlNode(new RelationalBooleanEqlNode(childNode));
			}
		} else {
			// No NOT operator shifted down, so we do not need something to change here
			return node;
		}
	}
	
}
