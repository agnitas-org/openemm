/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.legacy;

import java.util.List;
import java.util.Vector;

import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeMailingClicked;
import org.agnitas.target.impl.TargetNodeMailingOpened;
import org.agnitas.target.impl.TargetNodeMailingReceived;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.target.impl.TargetRepresentationImpl;

import com.agnitas.emm.core.target.eql.ast.AbstractAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode.InfixOperator;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.InRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.LikeRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantWithEscapeCharsAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.TodayAtomEqlNode;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.InvalidEqlDateFormatException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldNameResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingClickedOnSpecificLink;

/**
 * Converter for EQL expressions to legacy target groups.
 * 
 * <b>Please note, that not every EQL expression is convertible to legacy target groups!</b>
 */
public class EqlToTargetRepresentationConverter {
	
	/** Company ID to use. */
	protected final int companyId;
	
	/** Resolver for types of profile fields. */
	private final ProfileFieldTypeResolver typeResolver;
	
	/** Resolver for names of profile fields. */
	private final ProfileFieldNameResolver nameResolver;
	
	/** Resolver for mailing IDs to mailing types. */
	private final MailingTypeResolver mailingTypeResolver;
	
	/** Used SQL dialect. */
	private final SqlDialect sqlDialect;
	
	/**
	 * Creates a new converter.
	 * 
	 * @param companyId company ID to use
	 * @param typeResolver profile field type resolver
	 * @param mailingTypeResolver mailing type resolver
	 * @param nameResolver profile field name resolver
	 * @param sqlDialect used SQL dialect
	 */
	public EqlToTargetRepresentationConverter(final int companyId, final ProfileFieldTypeResolver typeResolver, final ProfileFieldNameResolver nameResolver, final MailingTypeResolver mailingTypeResolver, final SqlDialect sqlDialect) {
		this.companyId = companyId;
		this.typeResolver = typeResolver;
		this.nameResolver = nameResolver;
		this.mailingTypeResolver = mailingTypeResolver;
		this.sqlDialect = sqlDialect;
	}

	protected final MailingTypeResolver getMailingTypeResolver() {
		return this.mailingTypeResolver;
	}
	
	/**
	 * Converts EQL to legacy target group.
	 * 
	 * @param node node of syntax tree
	 * 
	 * @return legacy target group
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors converting EQL to legacy target group
	 */
	public TargetRepresentation convertToTargetRepresentation(final BooleanExpressionTargetRuleEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node.getChild().isPresent()) {
			final List<TargetNode> targetNodes = convertBooleanNode(node.getChild().get(), false, false, false);
			
			final TargetRepresentation representation = new TargetRepresentationImpl();
			for(final TargetNode targetNode : targetNodes) {
				representation.addNode(targetNode);
			}
			
			return representation;
		} else {
			return new TargetRepresentationImpl();
		}
	}
	
	/**
	 * Converts an arbitrary boolean expression.
	 * 
	 * @param node node representing boolean expression
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated list of legacy target nodes
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected List<TargetNode> convertBooleanNode(AbstractBooleanEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(node instanceof NotOperatorBooleanEqlNode) {
			return convertBooleanNode((NotOperatorBooleanEqlNode) node, not, open, close);
		} else if(node instanceof BinaryOperatorBooleanEqlNode) {
			return convertBooleanNode((BinaryOperatorBooleanEqlNode) node, not, open, close);
		} else if(node instanceof RelationalBooleanEqlNode) {
			TargetNode targetNode = convertBooleanNode((RelationalBooleanEqlNode) node, not, open, close);

			List<TargetNode> list = new Vector<>();
			list.add(targetNode);

			return list;
		} else {
			throw new EqlToTargetRepresentationConversionException("No conversion rule for node type " + node.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Converts a boolean NOT operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated list of legacy target nodes
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected List<TargetNode> convertBooleanNode(NotOperatorBooleanEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		return convertBooleanNode(node.getChild(), !not, open, close);
	}
	
	/**
	 * Converts a boolean binary operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated list of legacy target nodes
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected List<TargetNode> convertBooleanNode(BinaryOperatorBooleanEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(not) {
			throw new UnconvertibleEqlException();
		} else {
			if((open && precedence(node.getLeft()) < precedence(node.getOperator())) || (close && precedence(node.getRight()) < precedence(node.getOperator()))) {
				throw new UnconvertibleParenthesisException();
			} else {
				List<TargetNode> left = convertBooleanNode(node.getLeft(), not, open || precedence(node.getLeft()) < precedence(node.getOperator()), precedence(node.getLeft()) < precedence(node.getOperator()));
				List<TargetNode> right = convertBooleanNode(node.getRight(), not, precedence(node.getRight()) < precedence(node.getOperator()), close || precedence(node.getRight()) < precedence(node.getOperator()));

				switch(node.getOperator()) {
				case AND:
					right.get(0).setChainOperator(TargetNode.CHAIN_OPERATOR_AND);
					break;
					
				case OR:
					right.get(0).setChainOperator(TargetNode.CHAIN_OPERATOR_OR);
					break;

				default:
					throw new EqlToTargetRepresentationConversionException("Unable to handle boolean operator " + node.getOperator());
				}

				left.addAll(right);
				
				return left;
			}
		}
	}
	
	/**
	 * Converts a node embedding a relational expression into a boolean expression.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertBooleanNode(RelationalBooleanEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		TargetNode targetNode = convertRelationalNode(node.getChild(), not, open, close);
		targetNode.setOpenBracketBefore(open);
		targetNode.setCloseBracketAfter(close);
		
		return targetNode;
	}
	
	/**
	 * Converts an arbitrary relational node.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(AbstractRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(node instanceof BinaryOperatorRelationalEqlNode) {
			return convertRelationalNode((BinaryOperatorRelationalEqlNode) node, not, open, close);
		} else if(node instanceof LikeRelationalEqlNode) {
			return convertRelationalNode((LikeRelationalEqlNode) node, not, open, close);
		} else if(node instanceof ContainsRelationalEqlNode) {
			return convertRelationalNode((ContainsRelationalEqlNode) node, not, open, close);
		} else if(node instanceof StartsWithRelationalEqlNode) {
			return convertRelationalNode((StartsWithRelationalEqlNode) node, not, open, close);
		} else if(node instanceof InRelationalEqlNode) {
			return convertRelationalNode((InRelationalEqlNode) node, not, open, close);
		} else if(node instanceof EmptyRelationalEqlNode) {
			return convertRelationalNode((EmptyRelationalEqlNode) node, not, open, close);
		} else if(node instanceof ClickedInMailingRelationalEqlNode) {
			return convertRelationalNode((ClickedInMailingRelationalEqlNode) node, not, open, close);
		} else if(node instanceof OpenedMailingRelationalEqlNode) {
			return convertRelationalNode((OpenedMailingRelationalEqlNode) node, not, open, close);
		} else if(node instanceof ReceivedMailingRelationalEqlNode) {
			return convertRelationalNode((ReceivedMailingRelationalEqlNode) node, not, open, close);
		} else {
			throw new EqlToTargetRepresentationConversionException("Unable to convert node " + node.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Converts a binary relational node.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(BinaryOperatorRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(not) {
			throw new UnconvertibleEqlException();
		}

		try {
			if(isToday(node.getLeft())) {
				String today = todayValue(node.getLeft());
				String value = stringConstantValue(node.getRight());
				String format = node.getDateFormat();
				
				EqlDateFormat dateFormat = EqlDateFormat.parse(format);
				TargetNodeDate targetNode = new TargetNodeDate(sqlDialect.dateFormat(dateFormat).toLowerCase());	// Conversion to lower case is necessary for UI
				
				targetNode.setOpenBracketBefore(open);
				targetNode.setCloseBracketAfter(close);
				targetNode.setPrimaryField(today);
				targetNode.setPrimaryValue(value);
				
				switch(node.getOperator()) {
				case EQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_EQ.getOperatorCode());
					break;
					
				case LT:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_LT.getOperatorCode());
					break;
					
				case GT:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_GT.getOperatorCode());
					break;
					
				case NEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_NEQ.getOperatorCode());
					break;
					
				case LEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_LT_EQ.getOperatorCode());
					break;
					
				case GEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_GT_EQ.getOperatorCode());
					break;
					
				default:
					throw new EqlToTargetRepresentationConversionException("Unable to convert operator " + node.getOperator());
				}
				
				return targetNode;
			} else if(isModOperation(node.getLeft())) {
				BinaryOperatorExpressionalEqlNode modNode = (BinaryOperatorExpressionalEqlNode) node.getLeft();
				
				AbstractExpressionalEqlNode modLeft = modNode.getLeft();
				AbstractExpressionalEqlNode modRight = modNode.getRight();

				String columnName = profileFieldColumnName(modLeft);

				TargetNodeNumeric targetNode = new TargetNodeNumeric();
				
				targetNode.setOpenBracketBefore(open);
				targetNode.setCloseBracketAfter(close);
				targetNode.setPrimaryField(columnName.toUpperCase());
	
				targetNode.setPrimaryOperator(TargetNode.OPERATOR_MOD.getOperatorCode());
				setPrimaryValue(targetNode, modRight);
				
				switch(node.getOperator()) {
				case EQ:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_EQ.getOperatorCode());
					break;
					
				case LT:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_LT.getOperatorCode());
					break;
					
				case GT:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_GT.getOperatorCode());
					break;
					
				case NEQ:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_NEQ.getOperatorCode());
					break;
					
				case LEQ:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_LT_EQ.getOperatorCode());
					break;
					
				case GEQ:
					targetNode.setSecondaryOperator(TargetNode.OPERATOR_GT_EQ.getOperatorCode());
					break;
					
				default:
					throw new EqlToTargetRepresentationConversionException("Unable to convert operator " + node.getOperator());
				}

				targetNode.setSecondaryValue(Integer.parseInt(numericConstantValue(node.getRight())));
				
				return targetNode;
				
			} else {
				String fieldName = profileFieldName(node.getLeft());
				String columnName = profileFieldColumnName(node.getLeft());
				TargetNode targetNode = createTargetNodeByFieldType(fieldName);
				
				targetNode.setOpenBracketBefore(open);
				targetNode.setCloseBracketAfter(close);
				targetNode.setPrimaryField(columnName.toUpperCase());
	
				setPrimaryValue(targetNode, node.getRight());
				
				if(targetNode instanceof TargetNodeDate) {
					EqlDateFormat dateFormat = EqlDateFormat.parse(node.getDateFormat());
					
					if(dateFormat == null) {
						dateFormat = EqlDateFormat.parse("YYYYMMDD");
					}
					
					((TargetNodeDate) targetNode).setDateFormat(sqlDialect.dateFormat(dateFormat).toLowerCase());	// Conversion to lower case is necessary for UI
				}
				
				switch(node.getOperator()) {
				case EQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_EQ.getOperatorCode());
					break;
					
				case LT:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_LT.getOperatorCode());
					break;
					
				case GT:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_GT.getOperatorCode());
					break;
					
				case NEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_NEQ.getOperatorCode());
					break;
					
				case LEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_LT_EQ.getOperatorCode());
					break;
					
				case GEQ:
					targetNode.setPrimaryOperator(TargetNode.OPERATOR_GT_EQ.getOperatorCode());
					break;
					
				default:
					throw new EqlToTargetRepresentationConversionException("Unable to convert operator " + node.getOperator());
				}
				
				return targetNode;
			}
		} catch(InvalidEqlDateFormatException e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL expression", e);
		} catch(ProfileFieldResolveException e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL expression", e);
		}
	}
	
	/**
	 * Converts a node representing the "LIKE" operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(LikeRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		// Define some char the will never be used in EQL. This char is used as escape char in the SQL like pattern and will only be used for detection on non-convertible patterns.
		final char nonUsedChar = '\3';
		
		if(not) {
			throw new UnconvertibleEqlException();
		}

		try {
			String fieldName = profileFieldColumnName(node.getLeft());
	
			String pattern = StringUtil.convertEqlToSqlLikePattern(node.getRight().getValue(), nonUsedChar);
			if(pattern.indexOf(nonUsedChar) != -1) {
				throw new EqlToTargetRepresentationConversionException("Unable to convert EQL structure to legacy representation - matching pattern contains unconvertible symbols");
			}
			
			TargetNode targetNode = new TargetNodeString();
			targetNode.setPrimaryField(fieldName.toUpperCase());
			targetNode.setPrimaryOperator(node.getNotFlag() ? TargetNode.OPERATOR_NLIKE.getOperatorCode() : TargetNode.OPERATOR_LIKE.getOperatorCode());
			targetNode.setPrimaryValue(StringUtil.replaceEscapedChars(pattern));

			return targetNode;
		} catch(Exception e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL expression", e);
		}
	}

	/**
	 * Converts a node representing the "CONTAINS" operator.
	 *
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 *
	 * @return generated legacy target node
	 *
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(ContainsRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if (not) {
			throw new UnconvertibleEqlException();
		}

		try {
			String fieldName = profileFieldColumnName(node.getLeft());

			TargetNode targetNode = new TargetNodeString();
			targetNode.setPrimaryField(fieldName.toUpperCase());
			targetNode.setPrimaryOperator(node.getNotFlag() ? TargetNode.OPERATOR_NOT_CONTAINS.getOperatorCode() : TargetNode.OPERATOR_CONTAINS.getOperatorCode());
			targetNode.setPrimaryValue(StringUtil.replaceEscapedChars(node.getRight().getValue()));

			return targetNode;
		} catch (Exception e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL expression", e);
		}
	}

	/**
	 * Converts a node representing the "STARTS WITH" operator.
	 *
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 *
	 * @return generated legacy target node
	 *
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(StartsWithRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if (not) {
			throw new UnconvertibleEqlException();
		}

		try {
			String fieldName = profileFieldColumnName(node.getLeft());

			TargetNode targetNode = new TargetNodeString();
			targetNode.setPrimaryField(fieldName.toUpperCase());
			targetNode.setPrimaryOperator(node.getNotFlag() ? TargetNode.OPERATOR_NOT_STARTS_WITH.getOperatorCode() : TargetNode.OPERATOR_STARTS_WITH.getOperatorCode());
			targetNode.setPrimaryValue(StringUtil.replaceEscapedChars(node.getRight().getValue()));

			return targetNode;
		} catch (Exception e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL expression", e);
		}
	}

	/**
	 * Converts a node representing the "IN" operator. This method always throws and {@link UnconvertibleEqlException} because the IN operator is
	 * not available in legacy target groups.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(InRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		// IN is not supported by legacy target representation
		throw new UnconvertibleEqlException();
	}
	
	/**
	 * Converts a node representing the "EMPTY" operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(EmptyRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(not) {
			throw new UnconvertibleEqlException();
		}

		try {
			String fieldName = profileFieldName(node.getChild());
			String columnName = profileFieldColumnName(node.getChild());
			
			TargetNode targetNode = createTargetNodeByFieldType(fieldName);
			
			targetNode.setPrimaryField(columnName.toUpperCase());
			targetNode.setPrimaryOperator(TargetNode.OPERATOR_IS.getOperatorCode());
			targetNode.setOpenBracketBefore(open);
			targetNode.setCloseBracketAfter(close);
			
			if(node.getNotFlag()) {
				targetNode.setPrimaryValue("not null");
			} else {
				targetNode.setPrimaryValue("null");
			}
			
			return targetNode;
		} catch(ProfileFieldResolveException e) {
			throw new EqlToTargetRepresentationConversionException("Unable to resolve profile field", e);
		} 
	}

	/**
	 * Converts a node representing the "CLICKED (LINK) IN MAILING" operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(ClickedInMailingRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(node.hasDeviceQuery())  {
			throw new EqlToTargetRepresentationConversionException("Device query not supported by legacy editor");
		}
		
		int primaryOperator = not ? TargetNode.OPERATOR_NO.getOperatorCode() : TargetNode.OPERATOR_YES.getOperatorCode();
		
		int mailingID = node.getMailingId();
		int linkID = node.getLinkId() != null ? node.getLinkId() : 0;
		
		
		if(node.getLinkId() == null) {
			return new TargetNodeMailingClicked(companyId, TargetNode.CHAIN_OPERATOR_NONE, open ? 1 : 0, primaryOperator, Integer.toString(mailingID), close ? 1 : 0);
		} else {
			return new TargetNodeMailingClickedOnSpecificLink(companyId, TargetNode.CHAIN_OPERATOR_NONE, open ? 1 : 0, primaryOperator, mailingID, linkID, close ? 1 : 0);
		}
	}
	
	/**
	 * Converts a node representing the "OPENED MAILING" operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 */
	protected TargetNode convertRelationalNode(OpenedMailingRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		if(node.hasDeviceQuery())  {
			throw new EqlToTargetRepresentationConversionException("Device query not supported by legacy editor");
		}
		
		int primaryOperator = not ? TargetNode.OPERATOR_NO.getOperatorCode() : TargetNode.OPERATOR_YES.getOperatorCode();
		
		int mailingID = node.getMailingId();

		return new TargetNodeMailingOpened(companyId, TargetNode.CHAIN_OPERATOR_NONE, open ? 1 : 0, primaryOperator, Integer.toString(mailingID), close ? 1 : 0);
	}
	
	/**
	 * Converts a node representing the "RECEIVED MAILING" operator.
	 * 
	 * @param node node representing operator
	 * @param not flag, if a superordinate NOT operator was found
	 * @param open flag, if node directly requires an opening parenthesis
	 * @param close flag, if node directly requires an closing parenthesis
	 * 
	 * @return generated legacy target node
	 * @throws EqlToTargetRepresentationConversionException  on errors during conversion
	 */
	protected TargetNode convertRelationalNode(ReceivedMailingRelationalEqlNode node, boolean not, boolean open, boolean close) throws EqlToTargetRepresentationConversionException {
		int primaryOperator = not ? TargetNode.OPERATOR_NO.getOperatorCode() : TargetNode.OPERATOR_YES.getOperatorCode();
		
		int mailingID = node.getMailingId();
		
		try {
			return new TargetNodeMailingReceived(companyId, TargetNode.CHAIN_OPERATOR_NONE, open ? 1 : 0, primaryOperator, Integer.toString(mailingID), close ? 1 : 0);
		} catch(Exception e) {
			throw new EqlToTargetRepresentationConversionException("Error converting RECEIVED MAILING operator", e);
		}
	}
	
	/**
	 * Returns the profile field name stored in the node. 
	 * 
	 * @param node node representing operator
	 * 
	 * @return profile field name
	 * 
	 * @throws EqlToTargetRepresentationConversionException if node does not represent a profile field name
	 */
	protected String profileFieldName(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof AtomExpressionalEqlNode) {
			AtomExpressionalEqlNode atomExpressionNode = (AtomExpressionalEqlNode) node;
			AbstractAtomEqlNode atomNode = atomExpressionNode.getChild();

			if(atomNode instanceof ProfileFieldAtomEqlNode) {
				ProfileFieldAtomEqlNode profileFieldNode = (ProfileFieldAtomEqlNode) atomNode;
				
				return profileFieldNode.getName();
			} else {
				throw new ProfileFieldNameExpectedException();
			}
		} else {
			throw new ProfileFieldNameExpectedException();
		}
	}
	
	/**
	 * Checks, if given expression (represented by the {@link AbstractExpressionalEqlNode}) only consists of a TODAY.
	 * 
	 * @param node node to check
	 * 
	 * @return true, if expression only consists of a TODAY
	 */
	protected boolean isToday(AbstractExpressionalEqlNode node) {
		if(node instanceof AtomExpressionalEqlNode) {
			AtomExpressionalEqlNode atomExpressionNode = (AtomExpressionalEqlNode) node;
			AbstractAtomEqlNode atomNode = atomExpressionNode.getChild();

			return atomNode instanceof TodayAtomEqlNode;
		} else {
			return false;
		}
	}

	/**
	 * Checks, if given expression (represented by the {@link AbstractExpressionalEqlNode} is a modulo operation.
	 * 
	 * @param node node to check
	 * 
	 * @return if expression represents a modulo operation
	 */
	protected boolean isModOperation(AbstractExpressionalEqlNode node) {
		if(node instanceof BinaryOperatorExpressionalEqlNode) {
			BinaryOperatorExpressionalEqlNode expNode = (BinaryOperatorExpressionalEqlNode) node;
			
			return expNode.getOperator() == BinaryOperatorExpressionalEqlNode.InfixOperator.MOD;
		} else {
			return false;
		}
	}
	/**
	 * Returns the profile fields column name stored in the node. 
	 * 
	 * @param node node representing operator
	 * 
	 * @return column name
	 * 
	 * @throws EqlToTargetRepresentationConversionException if node does not represent a profile field name
	 * @throws ProfileFieldResolveException on errors resolving profile field
	 */
	protected String profileFieldColumnName(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException, ProfileFieldResolveException {
		String name = profileFieldName(node);
		
		return this.nameResolver.resolveProfileFieldName(name);
	}

	/**
	 * Creates a legacy target node depending on the type of the given profile field.
	 * 
	 * @param profileFieldName name of profile field.
	 * 
	 * @return legacy target node depending of type of profile field
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion
	 * @throws ProfileFieldResolveException on errors resolving profile field
	 */
	protected TargetNode createTargetNodeByFieldType(String profileFieldName) throws EqlToTargetRepresentationConversionException, ProfileFieldResolveException {
		DataType type = typeResolver.resolveProfileFieldType(profileFieldName);
		
		switch(type) {
		case NUMERIC:
			return new TargetNodeNumeric();
			
		case TEXT:
			return new TargetNodeString();
			
		case DATE:
			return new TargetNodeDate(null);
			
		default:
			throw new EqlToTargetRepresentationConversionException("Unable to handle field type " + type);
		}
	}

	/**
	 * Set the primary value of given target node according to given syntax tree node.
	 * This method does several checks, that will throw an exception if it fails:
	 * <ul>
	 *   <li>Checks, that syntax tree node represents a constant value</li>
	 *   <li>Checks, that type of constant value stored in node is compatible to type of targetn ode</li>
	 * </ul>
	 * 
	 * @param targetNode target node to set primary value
	 * @param node source of primary value for target node
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors during conversion.
	 */
	protected void setPrimaryValue(TargetNode targetNode, AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		
		if(targetNode instanceof TargetNodeNumeric) {
			targetNode.setPrimaryValue(numericConstantValue(node));
		} else if(targetNode instanceof TargetNodeString) {
			targetNode.setPrimaryValue(stringConstantValue(node));
		} else if(targetNode instanceof TargetNodeDate) {
			try {
				targetNode.setPrimaryValue(stringConstantValue(node));		// TODO: Check!!!!
			} catch(StringConstantExpectedException e) {
				targetNode.setPrimaryValue(todayArithmetic(node));
			}
		} else {
			throw new EqlToTargetRepresentationConversionException("Unsupported node type " + targetNode.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Returns the value of the numeric constant represented by the {@link AbstractExpressionalEqlNode}. 
	 * If the node is not a simple numeric constant, a {@link NumericConstantExpectedException} is thrown.
	 * 
	 * @param node node to get numeric constant value from
	 * 
	 * @return value of numeric constant
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors reading value of numeric constant
	 */
	protected static String numericConstantValue(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof AtomExpressionalEqlNode) {
			AbstractAtomEqlNode atomNode = ((AtomExpressionalEqlNode) node).getChild();

			return numericConstantValue(atomNode);
		} else {
			throw new NumericConstantExpectedException();
		}
	}
	
	/**
	 * Returns the value of the numeric constant represented by the {@link AbstractAtomEqlNode}. 
	 * If the node is not a simple numeric constant, a {@link NumericConstantExpectedException} is thrown.
	 * 
	 * @param node node to get numeric constant value from
	 * 
	 * @return value of numeric constant
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors reading value of numeric constant
	 */
	protected static String numericConstantValue(AbstractAtomEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof NumericConstantAtomEqlNode) {
			return ((NumericConstantAtomEqlNode) node).getValue();
		} else {
			throw new NumericConstantExpectedException();
		}
	}
	
	/**
	 * Returns the value of the string constant represented by the {@link AbstractExpressionalEqlNode}. 
	 * If the node is not a simple string constant, a {@link StringConstantExpectedException} is thrown.
	 * 
	 * @param node node to get string constant value from
	 * 
	 * @return value of string constant
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors reading value of string constant
	 */
	protected static String stringConstantValue(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof AtomExpressionalEqlNode) {
			AbstractAtomEqlNode atomNode = ((AtomExpressionalEqlNode) node).getChild();

			return stringConstantValue(atomNode);
		} else {
			throw new StringConstantExpectedException();
		}
	}
	
	/**
	 * Returns the value of the string constant represented by the {@link AbstractAtomEqlNode}. 
	 * If the node is not a simple string constant, a {@link StringConstantExpectedException} is thrown.
	 * 
	 * @param node node to get string constant value from
	 * 
	 * @return value of string constant
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors reading value of string constant
	 */
	protected static String stringConstantValue(AbstractAtomEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof StringConstantWithEscapeCharsAtomEqlNode) {
			return StringUtil.replaceEscapedChars(((StringConstantWithEscapeCharsAtomEqlNode) node).getValue());
		} else {
			throw new StringConstantExpectedException();
		}
	}
	
	/**
	 * Returns a date arithmetics (as a string) for given {@link AbstractExpressionalEqlNode}. If the node does not represent a 
	 * date arithmetics or expression is too complex a {@link StringConstantExpectedException} is thrown.
	 * 
	 * @param node node to convert
	 * @return String representation of date arithmetics
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors converting date arithmetics
	 */
	protected String todayArithmetic(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof BinaryOperatorExpressionalEqlNode) {
			return todayArithmetic((BinaryOperatorExpressionalEqlNode) node);
		} else if(node instanceof AtomExpressionalEqlNode) {
			return todayValue(node);
		} else {
			throw new StringConstantExpectedException(); 	// TODO: Find some better exception here?
		}
	}
	
	/**
	 * Returns a date arithmetics (as a string) for given {@link BinaryOperatorExpressionalEqlNode}. If the node does not represent a 
	 * date arithmetics or expression is too complex an exception is thrown.
	 * 
	 * @param node node to convert
	 * @return String representation of date arithmetics
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors converting date arithmetics
	 */
	protected String todayArithmetic(BinaryOperatorExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node.getOperator() != BinaryOperatorExpressionalEqlNode.InfixOperator.ADD && node.getOperator() != BinaryOperatorExpressionalEqlNode.InfixOperator.SUB) {
			throw new EqlToTargetRepresentationConversionException("Operator " + node.getOperator() + " not allowed for date types");
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(todayValue(node.getLeft()));
		
		if(node.getOperator() == InfixOperator.ADD) {
			buffer.append("+");
		} else {
			buffer.append("-");
		}
		
		buffer.append(numericConstantValue(node.getRight()));
		
		return buffer.toString();
	}
	
	/**
	 * Returns the TODAY keyword for the given node. If the expression (represented by the {@link AbstractExpressionalEqlNode} is not a simple TODAY, an exception is thrown.
	 * 
	 * @param node node to convert
	 * @return TODAY keyword as string
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors converting node
	 */
	protected String todayValue(AbstractExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		if(node instanceof AtomExpressionalEqlNode) {
			return todayValue((AtomExpressionalEqlNode) node);
		} else {
			throw new EqlToTargetRepresentationConversionException("TODAY expected");
		}
	}
	
	/**
	 * Returns the TODAY keyword for the given node. If the expression (represented by the {@link AtomExpressionalEqlNode} is not a simple TODAY, an exception is thrown.
	 * 
	 * @param node node to convert
	 * @return TODAY keyword as string
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors converting node
	 */
	protected String todayValue(AtomExpressionalEqlNode node) throws EqlToTargetRepresentationConversionException {
		AbstractAtomEqlNode atomNode = node.getChild();
		
		if(atomNode instanceof TodayAtomEqlNode) {
			return sqlDialect.today();
		} else {
			throw new StringConstantExpectedException(); 	// TODO: Find some better exception here?
		}
	}
	
	/**
	 * Returns the operator precedence of the given binary boolean operator.
	 * 
	 * @param op boolean operator
	 * 
	 * @return operator precedence.
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors determining operator precedence
	 */
	protected static int precedence(BinaryOperatorBooleanEqlNode.InfixOperator op) throws EqlToTargetRepresentationConversionException {
		
		/*
		 * IMPORT: When changing operator precedece, check that it is sound with precedence in method precedence(AbstractBooleanEqlNode)!!!
		 */
		
		switch(op) {
		case OR:
			return 0;
				
		case AND:
			return 1;
				
		default:
			throw new EqlToTargetRepresentationConversionException("Unable to determine precedence for boolean operator " + op);
		}
	}
	
	/**
	 * Returns the precedence of the given boolean node.
	 * 
	 * @param node boolean node
	 * 
	 * @return precedence.
	 * 
	 * @throws EqlToTargetRepresentationConversionException on errors determining precedence
	 */
	protected static int precedence(AbstractBooleanEqlNode node) throws EqlToTargetRepresentationConversionException {

		/*
		 * IMPORT: When changing operator precedece, check that it is sound with precedence in method precedence(BinaryOperatorBooleanEqlNode.Operator)!!!
		 */

		
		if(node instanceof BinaryOperatorBooleanEqlNode)
			return precedence(((BinaryOperatorBooleanEqlNode) node).getOperator());
		if(node instanceof NotOperatorBooleanEqlNode) {
			return 2;
		} else if(node instanceof RelationalBooleanEqlNode) {
			return 3;
		} else {
			throw new EqlToTargetRepresentationConversionException("Unable to determine precedence for node " + node.getClass().getCanonicalName());
		}
	}
}
