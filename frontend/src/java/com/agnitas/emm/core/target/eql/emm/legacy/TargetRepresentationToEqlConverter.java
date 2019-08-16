/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.target.eql.emm.eql.EqlUtils;
import org.agnitas.beans.ProfileField;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.impl.TargetNodeAutoImportFinished;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeMailingClicked;
import org.agnitas.target.impl.TargetNodeMailingOpened;
import org.agnitas.target.impl.TargetNodeMailingReceived;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;

import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingClickedOnSpecificLink;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingRevenue;

/**
 * Converter for legacy target groups to EQL.
 */
public class TargetRepresentationToEqlConverter {

	private static final String DATE_CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
	private static final String DATE_SYSDATE = "SYSDATE";
	private static final String DATE_NOW = "NOW()";

	/** DAO for accessing profile fields. */
	private final ComProfileFieldDao profileFieldDao;				// TODO: Replace by some resolver interface

	/**
	 * Create a new instance.
	 *
	 * @param profileFieldDao DAO accessing profile field data
		this.sqlDialect = sqlDialect;
	 */
	public TargetRepresentationToEqlConverter(final ComProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}

	/**
	 * Converts the given legacy target group to a corresponding EQL expression. Follows three-valued logic.
	 *
	 * @param representation legacy target group
	 * @param companyId company ID to use
	 *
	 * @return EQL expression representing given legacy target group
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	public String convertToEql(TargetRepresentation representation, int companyId) throws TargetRepresentationToEqlConversionException {
		return convertToEql(representation, companyId, false);
	}

	/**
	 * Converts the given legacy target group to a corresponding EQL expression.
	 *
	 * @param representation legacy target group
	 * @param companyId company ID to use
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic (generate
	 *                                an EQL having additional conditions in order to make sure that negated expression
	 *                                selects everything that direct expression doesn't)
	 *
	 * @return EQL expression representing given legacy target group
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	public String convertToEql(TargetRepresentation representation, int companyId, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		try {
			Map<String, String> resolvedNames = new HashMap<>();
			List<TargetNode> nodes = representation.getAllNodes();
			boolean firstNode = true;

			StringBuffer buffer = new StringBuffer();
			for(TargetNode node : nodes) {
				convertNode(firstNode, node, buffer, companyId, resolvedNames, disableThreeValuedLogic);
				firstNode = false;
			}

			return buffer.toString();
		} catch(Exception e) {
			throw new TargetRepresentationToEqlConversionException("Conversion of target representation to EQL failed", e);
		}
	}

	/**
	 * Converts an arbitrary legacy node.
	 *
	 * @param ignoreChainOperator if true, chain operator of target node is ignored
	 * @param node legacy node
	 * @param buffer buffer used for writing generated EQL code
	 * @param companyId company ID to use
	 * @param resolvedNames map to store resolved profile field names
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private void convertNode(boolean ignoreChainOperator, TargetNode node, StringBuffer buffer, int companyId, Map<String, String> resolvedNames, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		/*
		 * In some cases, the first target node of a target group as a chain operator set to something other than NONE.
		 * This leads to an invalid EQL expression, if not ignored.
		 */
		if(!ignoreChainOperator) {
			convertChainOperator(node, buffer);
		}

		if(node.isOpenBracketBefore()) {
			buffer.append("(");
		}

		doConvertNode(ignoreChainOperator, node, buffer, companyId, resolvedNames, disableThreeValuedLogic);

		if(node.isCloseBracketAfter()) {
			buffer.append(")");
		}
	}
	
	protected void doConvertNode(boolean ignoreChainOperator, TargetNode node, StringBuffer buffer, int companyId, Map<String, String> resolvedNames, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		if(node instanceof TargetNodeNumeric) {
			convertNode((TargetNodeNumeric) node, buffer, companyId, resolvedNames, disableThreeValuedLogic);
		} else if(node instanceof TargetNodeString) {
			convertNode((TargetNodeString) node, buffer, companyId, resolvedNames, disableThreeValuedLogic);
		} else if(node instanceof TargetNodeDate) {
			convertNode((TargetNodeDate) node, buffer, companyId, resolvedNames, disableThreeValuedLogic);
		} else if(node instanceof TargetNodeAutoImportFinished) {
			convertNode((TargetNodeAutoImportFinished) node, buffer);
		} else if(node instanceof TargetNodeMailingClicked) {
			convertNode((TargetNodeMailingClicked) node, buffer);
		} else if(node instanceof TargetNodeMailingClickedOnSpecificLink) {
			convertNode((TargetNodeMailingClickedOnSpecificLink) node, buffer);
		} else if(node instanceof TargetNodeMailingOpened) {
			convertNode((TargetNodeMailingOpened) node, buffer);
		} else if(node instanceof TargetNodeMailingReceived) {
			convertNode((TargetNodeMailingReceived) node, buffer);
		} else if(node instanceof TargetNodeMailingRevenue) {
			convertNode((TargetNodeMailingRevenue) node, buffer);
		} else {
			throw new TargetRepresentationToEqlConversionException("Cannot handle target node type " + node.getClass().getCanonicalName());
		}
		
	}

	/**
	 * Converts the legacy chain operator (AND, OR) to corresponding EQL operator.
	 *
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertChainOperator(TargetNode node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		switch(node.getChainOperator()) {
		case TargetNode.CHAIN_OPERATOR_NONE:
			// Nothing to do here
			break;

		case TargetNode.CHAIN_OPERATOR_AND:
			buffer.append(" AND ");
			break;

		case TargetNode.CHAIN_OPERATOR_OR:
			buffer.append(" OR ");
			break;

		default:
			throw new TargetRepresentationToEqlConversionException("Cannot handle chaining operator type " + node.getChainOperator());
		}
	}

	/**
	 * Converts a legacy numeric node.
	 *
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * @param companyId company ID to use
	 * @param resolvedNames map to store resolved profile field names
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private void convertNode(TargetNodeNumeric node, StringBuffer buffer, int companyId, Map<String, String> resolvedNames, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		TargetOperator primaryOperator = TargetNodeNumeric.getValidOperators()[node.getPrimaryOperator() - 1];

		if (primaryOperator == null) {
			throw new TargetRepresentationToEqlConversionException("No or invalid primary operator defined");
		}

		String field = convertProfileField(node.getPrimaryField(), companyId, resolvedNames);

		if (primaryOperator == TargetNode.OPERATOR_IS) {
			buffer.append(field);

			if (node.getPrimaryValue().toLowerCase().startsWith("null")) {
				buffer.append(" IS EMPTY");
			} else {
				buffer.append(" IS NOT EMPTY");
			}
		} else if (primaryOperator == TargetNode.OPERATOR_MOD) {
			TargetOperator secondaryOperator = TargetNode.findSecondaryOperatorForMod(node.getSecondaryOperator());

			if (secondaryOperator == null) {
				throw new TargetRepresentationToEqlConversionException("No or invalid secondary operator defined");
			}

			convertNodeEquation(buffer, field, primaryOperator, node.getPrimaryValue(), secondaryOperator, node.getSecondaryValue(), disableThreeValuedLogic);
		} else {
			convertNodeEquation(buffer, field, primaryOperator, node.getPrimaryValue(), disableThreeValuedLogic);
		}
	}

	/**
	 * Converts a legacy String node.
	 *
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * @param companyId company ID to use
	 * @param resolvedNames map to store resolved profile field names
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private void convertNode(TargetNodeString node, StringBuffer buffer, int companyId, Map<String, String> resolvedNames, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		TargetOperator primaryOperator = TargetNodeString.getValidOperators()[node.getPrimaryOperator() - 1];

		if (primaryOperator == null) {
			throw new TargetRepresentationToEqlConversionException("No or invalid primary operator defined");
		}

		String field = convertProfileField(node.getPrimaryField(), companyId, resolvedNames);
		if (primaryOperator == TargetNode.OPERATOR_IS) {
			buffer.append(field);

			if (node.getPrimaryValue().toLowerCase().startsWith("null")) {
				buffer.append(" IS EMPTY");
			} else {
				buffer.append(" IS NOT EMPTY");
			}
		} else if (primaryOperator == TargetNode.OPERATOR_MOD) {
			throw new TargetRepresentationToEqlConversionException("No or invalid primary operator defined");
		} else {
			Object value;
			if (primaryOperator == TargetNode.OPERATOR_LIKE || primaryOperator == TargetNode.OPERATOR_NLIKE) {
				value = StringUtil.makeEqlMatchingPattern(node.getPrimaryValue());
			} else {
				value = StringUtil.makeEqlStringConstant(node.getPrimaryValue());
			}
			convertNodeEquation(buffer, field, primaryOperator, value, disableThreeValuedLogic);
		}
	}

	/**
	 * Converts a legacy Date node.
	 *
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * @param companyId company ID to use
	 * @param resolvedNames map to store resolved profile field names
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private void convertNode(TargetNodeDate node, StringBuffer buffer, int companyId, Map<String, String> resolvedNames, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		TargetOperator primaryOperator = TargetNodeDate.getValidOperators()[node.getPrimaryOperator() - 1];

		if (primaryOperator == null) {
			throw new TargetRepresentationToEqlConversionException("No or invalid primary operator defined");
		}
		
		if (primaryOperator == TargetNode.OPERATOR_IS) {
			buffer.append(convertProfileField(node.getPrimaryField(), companyId, resolvedNames));
			
			if (node.getPrimaryValue().toLowerCase().startsWith("null")) {
				buffer.append(" IS EMPTY");
			} else {
				buffer.append(" IS NOT EMPTY");
			}
		} else {
			String field = node.getPrimaryField();

			if (DATE_CURRENT_TIMESTAMP.equalsIgnoreCase(field) || DATE_SYSDATE.equalsIgnoreCase(field) || DATE_NOW.equalsIgnoreCase(field)) {
				buffer.append("TODAY ");
				buffer.append(primaryOperator.getOperatorSymbol());
				buffer.append(" ");
				buffer.append(StringUtil.makeEqlStringConstant(node.getPrimaryValue()));
				buffer.append(" DATEFORMAT ");
				buffer.append(StringUtil.makeEqlStringConstant(EqlUtils.toEQLDateFormat(node.getDateFormat())));
			} else {
				field = convertProfileField(node.getPrimaryField(), companyId, resolvedNames);

				String value = node.getPrimaryValue();

				if (value.toUpperCase().startsWith(DATE_CURRENT_TIMESTAMP)) {
					value = "TODAY" + value.substring(DATE_CURRENT_TIMESTAMP.length());
				} else if (value.toUpperCase().startsWith(DATE_SYSDATE)) {
					value = "TODAY" + value.substring(DATE_SYSDATE.length());
				} else if (value.toUpperCase().startsWith(DATE_NOW)) {
					value = "TODAY" + value.substring(DATE_NOW.length());
				} else {
					value = StringUtil.makeEqlStringConstant(value);
				}

				value += " DATEFORMAT '" + EqlUtils.toEQLDateFormat(node.getDateFormat()) + "'";

				convertNodeEquation(buffer, field, primaryOperator, value, disableThreeValuedLogic);
			}
		}
		
	}

	/**
	 * Converts a legacy node representing "FINISHED AUTOIMPORT".
	 *
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeAutoImportFinished node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if (node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("FINISHED AUTOIMPORT ");
			buffer.append(node.getPrimaryValue());
		} else if (node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (");
			buffer.append("FINISHED AUTOIMPORT ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}

	/**
	 * Converts a legacy node representing "CLICKED IN MAILING".
	 * 
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeMailingClicked node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if(node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("CLICKED IN MAILING ");
			buffer.append(node.getPrimaryValue());
		} else if(node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (CLICKED IN MAILING ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}
	
	/**
	 * Converts a legacy node representing "CLICKED LINK IN MAILING".
	 * 
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeMailingClickedOnSpecificLink node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if(node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("CLICKED LINK ");
			buffer.append(node.getSecondaryValue());
			buffer.append(" IN MAILING ");
			buffer.append(node.getPrimaryValue());
		} else if(node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (CLICKED LINK ");
			buffer.append(node.getSecondaryValue());
			buffer.append(" IN MAILING ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}

	/**
	 * Converts a legacy node representing "OPENED MAILING".
	 * 
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeMailingOpened node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if(node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("OPENED MAILING ");
			buffer.append(node.getPrimaryValue());
		} else if(node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (");
			buffer.append("OPENED MAILING ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}

	/**
	 * Converts a legacy node representing "RECEIVED MAILING" for non-interval mailings.
	 * 
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeMailingReceived node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if(node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("RECEIVED MAILING ");
			buffer.append(node.getPrimaryValue());
		} else if(node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (");
			buffer.append("RECEIVED MAILING ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}

	/**
	 * Converts a legacy node representing "REVENUE BY MAILING".
	 * 
	 * @param node legacy node
	 * @param buffer buffer for writing EQL code
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private static void convertNode(TargetNodeMailingRevenue node, StringBuffer buffer) throws TargetRepresentationToEqlConversionException {
		if(node.getPrimaryOperator() == TargetNode.OPERATOR_YES.getOperatorCode()) {
			buffer.append("REVENUE BY MAILING ");
			buffer.append(node.getPrimaryValue());
		} else if(node.getPrimaryOperator() == TargetNode.OPERATOR_NO.getOperatorCode()) {
			buffer.append("NOT (");
			buffer.append("REVENUE BY MAILING ");
			buffer.append(node.getPrimaryValue());
			buffer.append(")");
		} else {
			throw new TargetRepresentationToEqlConversionException("Invalid primary operator " + node.getPrimaryOperator());
		}
	}
	
	/**
	 * Converts a legacy profile field name to an EQL profile field name.
	 * A map containing previously resolved profile field names is used to to speed up conversion.
	 * DB names of profile fields are keys, profile field shortnames are the values.
	 * 
	 * @param name legacy profile field name
	 * @param companyId company ID to use
	 * @param resolvedNames map to store resolved profile field names
	 * 
	 * @return EQL-styled profile field name
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private String convertProfileField(String name, int companyId, Map<String, String> resolvedNames) throws TargetRepresentationToEqlConversionException {
		String shortname = resolvedNames.get(name);

		if(shortname == null) {
			shortname = convertProfileFieldFromDb(name, companyId);
			
			resolvedNames.put(name, shortname);
		}
		
		return shortname;
	}
	
	/**
	 * Converts a legacy profile field name to an EQL profile field name.
	 * This method is called by {@link #convertProfileField(String, int, Map)} in case of a &quot;cache miss&quot;.
	 * 
	 * @param name legacy profile field name
	 * @param companyId company ID to use
	 * 
	 * @return EQL-styled profile field name
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	private String convertProfileFieldFromDb(String name, int companyId) throws TargetRepresentationToEqlConversionException {
		try {
			ProfileField profileField = this.profileFieldDao.getProfileField(companyId, name);
			if (profileField != null) {
				return "`" + profileField.getShortname() + "`";
			} else {
				return "`" + name + "`";
			}
		} catch(Exception e) {
			throw new TargetRepresentationToEqlConversionException("Unable to convert profile field name '" + name + "'", e);
		}
	}

	private static void convertNodeEquation(StringBuffer buffer, String field, TargetOperator operator1, Object value1, boolean disableThreeValuedLogic) {
		convertNodeEquation(buffer, field, operator1, value1, null, null, disableThreeValuedLogic);
	}

	private static void convertNodeEquation(StringBuffer buffer, String field, TargetOperator operator1, Object value1, TargetOperator operator2, Object value2, boolean disableThreeValuedLogic) {
		TargetOperator comparisonOperator = operator2 == null ? operator1 : operator2;

		if (disableThreeValuedLogic) {
			disableThreeValuedLogic = TargetNode.isThreeValuedLogicOperator(comparisonOperator);
		}

		if (disableThreeValuedLogic) {
			if (TargetNode.isInequalityOperator(comparisonOperator)) {
				buffer.append('(');
			} else {
				comparisonOperator = TargetNode.getOppositeOperator(comparisonOperator);
				buffer.append("NOT (");
			}
		}

		if (operator2 == null) {
			buffer.append(field);
			buffer.append(" ");
			buffer.append(comparisonOperator.getOperatorSymbol());
			buffer.append(" ");
			buffer.append(value1);
		} else {
			buffer.append(field);
			buffer.append(" ");
			buffer.append(operator1 == TargetNode.OPERATOR_MOD ? "%" : operator1.getOperatorSymbol());
			buffer.append(" ");
			buffer.append(value1);
			buffer.append(" ");
			buffer.append(comparisonOperator.getOperatorSymbol());
			buffer.append(" ");
			buffer.append(value2);
		}

		if (disableThreeValuedLogic) {
			buffer.append(" OR ");
			buffer.append(field);
			buffer.append(" IS EMPTY");
			buffer.append(')');
		}
	}

}
