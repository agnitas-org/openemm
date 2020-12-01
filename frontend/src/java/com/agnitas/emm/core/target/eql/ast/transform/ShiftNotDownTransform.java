/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AnnotationBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalInfixOperator;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.RevenueByMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags;

/**
 * This transform on the syntax tree shifts <i>NOT</i> operators down as far as possible applying
 * the rules of the De Morgan's Law (see article on <a href="https://en.wikipedia.org/wiki/De_Morgan%27s_laws">wikipedia</a>)
 * recursively to the nodes of the syntax tree.
 *
 * For syntax nodes sub-classed from {@link AbstractRelationalEqlNode} annotated with {@link SpecialNotOperatorHandling}, additional modifications will be applied like:
 * <ul>
 *   <li>Adding a special condition to exclude all recipients with tracking veto set</li>
 *   <li>...</li>
 * </ul>
 * 
 * This transform should only be applied to syntax trees that are used for code generation.
 */
public final class ShiftNotDownTransform {
	
	/**
	 * Annotation for special handling of tracking veto in combination with NOT operators.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SpecialNotOperatorHandling { 
		public boolean respectTrackingVeto() default true;
		public boolean mustHaveReceivedMailing() default false;
	}

	public static final BooleanExpressionTargetRuleEqlNode shiftNotDown(final BooleanExpressionTargetRuleEqlNode node, final CodeGenerationFlags flags) {
		return shiftNotDown(node, false, flags);
	}
	
	private static final BooleanExpressionTargetRuleEqlNode shiftNotDown(final BooleanExpressionTargetRuleEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		if(node.getChild().isPresent()) {
			final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild().get(), inNot, flags);
			
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
	
	private static final AbstractBooleanEqlNode shiftNotDown(final AbstractBooleanEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		if(node instanceof AnnotationBooleanEqlNode) {
			return shiftNotDown((AnnotationBooleanEqlNode) node, inNot, flags);
		} else if(node instanceof BinaryOperatorBooleanEqlNode) {
			return shiftNotDown((BinaryOperatorBooleanEqlNode) node, inNot, flags);
		} else if(node instanceof NotOperatorBooleanEqlNode) {
			return shiftNotDown((NotOperatorBooleanEqlNode) node, inNot, flags);	
		} else if(node instanceof RelationalBooleanEqlNode) {
			return shiftNotDown((RelationalBooleanEqlNode) node, inNot, flags);
		} else {
			throw new RuntimeException("Unhandled node type " + node.getClass().getCanonicalName());
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final AnnotationBooleanEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild(), inNot, flags);
		
		if(newChild == node.getChild()) {
			return node;
		} else { 
			return new AnnotationBooleanEqlNode(newChild, node.getAnnotations());
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final BinaryOperatorBooleanEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		final AbstractBooleanEqlNode newLeft = shiftNotDown(node.getLeft(), inNot, flags);
		final AbstractBooleanEqlNode newRight = shiftNotDown(node.getRight(), inNot, flags);
		
		if(newLeft == node.getLeft() && newRight == node.getRight()) {
			return node;
		} else {
			switch(node.getOperator()) {
			case AND: {
				if(inNot) {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.InfixOperator.OR, newRight);
				} else {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.InfixOperator.AND, newRight);
				}
			}
			
			case OR: {		
				if(inNot) {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.InfixOperator.AND, newRight);
				} else {
					return new BinaryOperatorBooleanEqlNode(newLeft, BinaryOperatorBooleanEqlNode.InfixOperator.OR, newRight);
				}
			}
	
			default:
				throw new RuntimeException("Unhandled operator " + node.getOperator());
			}
		}
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final NotOperatorBooleanEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		final AbstractBooleanEqlNode newChild = shiftNotDown(node.getChild(), !inNot, flags);
		
		return newChild;
	}
	
	private static final AbstractBooleanEqlNode shiftNotDown(final RelationalBooleanEqlNode node, final boolean inNot, final CodeGenerationFlags flags) {
		if(inNot) {
			final AbstractRelationalEqlNode childNode = node.getChild();
			final SpecialNotOperatorHandling annotation = childNode.getClass().getAnnotation(SpecialNotOperatorHandling.class);

			if(annotation != null) {
				// Adding the tracking veto condition must be done before adding the "must have received mailing" condition, otherwise this condition will be negated, too.
				AbstractBooleanEqlNode transformedNode = handleRespectTrackingVeto(childNode, annotation.respectTrackingVeto() && !flags.isIgnoreTrackingVeto());
				transformedNode = handleMustHaveReceivedMailing(transformedNode, childNode, annotation.mustHaveReceivedMailing());
				
				return transformedNode;
			} else {
				return new NotOperatorBooleanEqlNode(new RelationalBooleanEqlNode(childNode));
			}
		} else {
			// No NOT operator shifted down, so we do not need something to change here
			return node;
		}
	}
	
	private static final AbstractBooleanEqlNode handleRespectTrackingVeto(final AbstractRelationalEqlNode node, final boolean respectTrackinVeto) {
		final NotOperatorBooleanEqlNode newChildNode = new NotOperatorBooleanEqlNode(new RelationalBooleanEqlNode(node));

		if(respectTrackinVeto) {
			final RelationalBooleanEqlNode trackingVetoExclusionNode1 = new RelationalBooleanEqlNode(
					new BinaryOperatorRelationalEqlNode(
							new AtomExpressionalEqlNode(new ProfileFieldAtomEqlNode("$tracking_veto", node.getStartLocation())),			// "$" marks this as internally generated identifier
							RelationalInfixOperator.EQ, 
							new AtomExpressionalEqlNode(new NumericConstantAtomEqlNode("0", node.getStartLocation())),	
							null));				// Date format is not used in this comparison
			
			final RelationalBooleanEqlNode trackingVetoExclusionNode2 = new RelationalBooleanEqlNode(
					new EmptyRelationalEqlNode(
							new AtomExpressionalEqlNode(new ProfileFieldAtomEqlNode("$tracking_veto", node.getStartLocation())), false));
			
			final BinaryOperatorBooleanEqlNode trackingVetoExclusionNode = new BinaryOperatorBooleanEqlNode(
					trackingVetoExclusionNode1, 
					BinaryOperatorBooleanEqlNode.InfixOperator.OR, 
					trackingVetoExclusionNode2);
			
			return new BinaryOperatorBooleanEqlNode(
					newChildNode,
					BinaryOperatorBooleanEqlNode.InfixOperator.AND,
					trackingVetoExclusionNode
					);
		} else {
			return newChildNode;
		}
	}

	private static final AbstractBooleanEqlNode handleMustHaveReceivedMailing(final AbstractBooleanEqlNode node, final AbstractRelationalEqlNode oldNode, final boolean mustHaveReceivedMailing) {
		if(!mustHaveReceivedMailing) {
			return node;
		}
		
		final Integer mailingId = mailingIdFromNode(oldNode); 
		
		if(mailingId == null) {
			return node;
		}
		
		final AbstractRelationalEqlNode conditionNode = new ReceivedMailingRelationalEqlNode(mailingId, oldNode.getStartLocation());
		
		return new BinaryOperatorBooleanEqlNode(
				node,
				BinaryOperatorBooleanEqlNode.InfixOperator.AND,
				new RelationalBooleanEqlNode(conditionNode)
				);
	}
	
	private static final Integer mailingIdFromNode(final AbstractRelationalEqlNode node) {
		if(node instanceof OpenedMailingRelationalEqlNode) {
			return ((OpenedMailingRelationalEqlNode) node).getMailingId();
		} else if(node instanceof ReceivedMailingRelationalEqlNode) {
			return ((ReceivedMailingRelationalEqlNode) node).getMailingId();
		} else if(node instanceof ClickedInMailingRelationalEqlNode) {
			return ((ClickedInMailingRelationalEqlNode) node).getMailingId();
		} else if(node instanceof RevenueByMailingRelationalEqlNode) {
			return ((RevenueByMailingRelationalEqlNode) node).getMailingId();
		} else {
			return null;
		}
	}
}
