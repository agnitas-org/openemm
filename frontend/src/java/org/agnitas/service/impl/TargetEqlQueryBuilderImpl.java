/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.TargetEqlQueryBuilder;
import org.agnitas.target.ChainOperator;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.target.PseudoColumn;
import org.agnitas.util.DbColumnType;
import org.agnitas.web.forms.TargetEqlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ProfileField;

/**
 * Helper-class for building the sql-query in /recipient/list.jsp
 */
public class TargetEqlQueryBuilderImpl implements TargetEqlQueryBuilder {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(TargetEqlQueryBuilderImpl.class);

	public static final String COLUMN_TYPE_MAILING_OPENED = "OPENED_MAILING";
	public static final String COLUMN_TYPE_MAILING_CLICKED = "CLICKED_IN_MAILING";
	public static final String COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK = "CLICKED_SPECIFIC_LINK_IN_MAILING";

	// TODO This is part of a workaround a should be removed as soon as possible after recipient search uses the QueryBuilder UI
	private static final transient Pattern TODAY_WITH_OFFSET_PATTERN = Pattern.compile("^\\s*(?:TODAY|SYSDATE|CURRENT_TIMESTAMP|NOW)\\s*(\\+|\\-)\\s*(\\d+)\\s*$");

	/** Service for accessing DB column metadata. */
	protected ColumnInfoService columnInfoService;

    /**
	 * Set service for DB column meta data.
	 *
	 * @param service ColumnInfoService
	 */
	@Required
	public void setColumnInfoService(ColumnInfoService service) {
		this.columnInfoService = service;
	}
	
	public final void createEqlFromForm(final TargetEqlBuilder form, final int companyId, final int index, final StringBuffer eqlBuffer) {
		final String column = extractColumnFromForm(form, index);
		final String type = determineColumnType(column, companyId);
		
		final String eqlOfNode = convertRuleToEql(form, column, type, index, companyId);

		// In recipient search, conditions are ANDed only
		if(index > 0) {
			if(form.getChainOperator(index) == ChainOperator.OR.getOperatorCode()) {
				eqlBuffer.append(" OR ");
			} else {
				eqlBuffer.append(" AND ");
			}
		}
		
		// Add left parenthesis
		if(form.getParenthesisOpened(index) == 1) {
			eqlBuffer.append('(');
		}
		
		eqlBuffer.append(eqlOfNode);
		
		// Add right parenthesis
		if(form.getParenthesisClosed(index) == 1) {
			eqlBuffer.append(')');
		}
	}

	/**
	 * Converts rule at given index. Parenthesis will be handled by caller!
	 * 
	 * Overwrite this method in sub-classes.
	 */
	protected String convertRuleToEql(final TargetEqlBuilder form, final String columnName, final String columnType, final int nodeIndex, final int companyId) {
		switch (columnType) {
			case DbColumnType.GENERIC_TYPE_VARCHAR: // fall-through
			case "VARCHAR2": // fall-through
			case "CHAR":
				return convertStringRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), nodeIndex);
				
			case DbColumnType.GENERIC_TYPE_INTEGER: // fall-through
			case "DOUBLE": // fall-through
			case DbColumnType.GENERIC_TYPE_FLOAT: // fall-through
			case "NUMBER":
				return convertNumericRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), nodeIndex);
				
			case DbColumnType.GENERIC_TYPE_DATE: // fall-through
			case DbColumnType.GENERIC_TYPE_DATETIME:
				return convertDateRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), nodeIndex);
				
			default:
				// Leave switch-case
		}
		
		if (COLUMN_TYPE_MAILING_OPENED.equalsIgnoreCase(columnType)) {
			return convertOpenedMailingNodeToEql(form, nodeIndex);	
		} else if (COLUMN_TYPE_MAILING_CLICKED.equalsIgnoreCase(columnType) ||
			COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK.equalsIgnoreCase(columnType)) {
			return convertClickedInMailingNodeToEql(form, nodeIndex);
		}
		
		throw new RuntimeException(String.format("Encountered unhandled column type '%s'", columnType));
	}
	
	private String columnNameToProfileFieldName(final String columnName, final int companyId) {
		try {
			final ProfileField field = columnInfoService.getColumnInfo(companyId, columnName);
			
			return field != null ? field.getShortname() : columnName;
		} catch(final Exception e) {
			logger.error(String.format("Cannot determine shortname for profile field column '%s'", columnName), e);
			
			return columnName;
		}
	}
	
	private String convertStringRuleToEql(final TargetEqlBuilder form, final String profileFieldName, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final String primaryValue = form.getPrimaryValue(nodeIndex);
	
		if(primaryOperator == ConditionalOperator.IS) {
			return "null".equalsIgnoreCase(primaryValue)
					? String.format("`%s` IS EMPTY", profileFieldName)
							: String.format("`%s` IS NOT EMPTY", profileFieldName);
		} else {
			return String.format("`%s` %s \"%s\"", profileFieldName, primaryOperator.getEqlSymbol(), primaryValue);
		}
	}
	
	private String convertNumericRuleToEql(final TargetEqlBuilder form, final String profileFieldName, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final ConditionalOperator secondaryOperator = ConditionalOperator.fromOperatorCode(form.getSecondaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);

		String primaryValue = form.getPrimaryValue(nodeIndex);

		if (primaryOperator == ConditionalOperator.MOD) {
			return String.format(
					"`%s` MOD %s %s %s",
					profileFieldName,
					form.getPrimaryValue(nodeIndex),
					secondaryOperator.getEqlSymbol(),
					form.getSecondaryValue(nodeIndex));
		} else if (primaryOperator == ConditionalOperator.IS) {
			return "null".equalsIgnoreCase(primaryValue)
					? String.format("`%s` IS EMPTY", profileFieldName)
							: String.format("`%s` IS NOT EMPTY", profileFieldName);
		} else {
			return String.format(
					"`%s` %s %s",
					profileFieldName,
					primaryOperator.getEqlSymbol(),
					form.getPrimaryValue(nodeIndex));
		}
	}
	
	private String convertDateRuleToEql(final TargetEqlBuilder form, final String profileFieldName, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final String primaryValue = form.getPrimaryValue(nodeIndex);
		final String dateFormat = form.getDateFormat(nodeIndex) != null
				? form.getDateFormat(nodeIndex).toUpperCase()
				: "YYYYMMDD";
				
		if (primaryOperator == ConditionalOperator.IS) {
			return "null".equalsIgnoreCase(primaryValue)
					? String.format("`%s` IS EMPTY", profileFieldName)
							: String.format("`%s` IS NOT EMPTY", profileFieldName);
		} else {
			// TODO This is part of a workaround a should be removed as soon as possible after recipient search uses the QueryBuilder UI
			final Matcher todayWithOffsetMatcher = TODAY_WITH_OFFSET_PATTERN.matcher(primaryValue.toUpperCase());
			
			if(todayWithOffsetMatcher.matches()) {
				final String operator = todayWithOffsetMatcher.group(1);
				final String offset = todayWithOffsetMatcher.group(2);
				
				return String.format(
						"%s %s TODAY%s%s DATEFORMAT \"%s\"",
						isNow(profileFieldName) ? "TODAY" : String.format("`%s`", profileFieldName),
						primaryOperator.getEqlSymbol(),
						operator,
						offset,
						dateFormat.toUpperCase());
				
			} else {
				return String.format(
						"%s %s %s DATEFORMAT \"%s\"",
						isNow(profileFieldName) ? "TODAY" : String.format("`%s`", profileFieldName),
						primaryOperator.getEqlSymbol(),
						isNow(primaryValue) ? "TODAY" : String.format("\"%s\"", primaryValue),
						dateFormat.toUpperCase());
			}
		}
	}

	private static boolean isNow(final String name) {
		return "current_timestamp".equalsIgnoreCase(name)
				|| "sysdate".equalsIgnoreCase(name)
				|| "now".equals(name)
				|| "today".equals(name);
	}
	
	private final String extractColumnFromForm(final TargetEqlBuilder form, final int index) {
  		final String column = form.getColumnAndType(index);
  	  	
  		final int indexOfHash = column.indexOf('#');
  		
  		if (indexOfHash != -1) {
  			return column.substring(0, indexOfHash);
  		} else {
  			return column;
  		}
	}

	@Override
	public String determineColumnType(final String columnName, final int companyId) {
		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(columnName)) {
			return DbColumnType.GENERIC_TYPE_DATE;
		} else if (PseudoColumn.OPENED_MAILING.isThisPseudoColumn(columnName)) {
			return COLUMN_TYPE_MAILING_OPENED;
		} else if (PseudoColumn.CLICKED_IN_MAILING.isThisPseudoColumn(columnName)) {
			return COLUMN_TYPE_MAILING_CLICKED;
		} else if (PseudoColumn.CLICKED_SPECIFIC_LINK_IN_MAILING.isThisPseudoColumn(columnName)) {
			return COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK;

		} else {
			try {
				return columnInfoService.getColumnInfo(companyId, columnName).getDataType();
			} catch (Exception e) {
				logger.error(String.format("Cannot find fieldtype for companyId %d and column '%s'", companyId, columnName), e);
				
				return "unknownType";
			}
		}
	}
	

	private String convertClickedInMailingNodeToEql(final TargetEqlBuilder form, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.YES);
		final String primaryValue = form.getPrimaryValue(nodeIndex);
		String secondaryValue = form.getSecondaryValue(nodeIndex);

		if (StringUtils.isEmpty(secondaryValue) || StringUtils.equals(secondaryValue, "-1")) {
			return primaryOperator == ConditionalOperator.YES
					? String.format("CLICKED IN MAILING %s", primaryValue)
					: String.format("NOT (CLICKED IN MAILING %s)", primaryValue);
		}

		return "CLICKED LINK " + secondaryValue + " IN MAILING " + primaryValue;
	}

	private String convertOpenedMailingNodeToEql(final TargetEqlBuilder form, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.YES);
		final String primaryValue = form.getPrimaryValue(nodeIndex);

		return primaryOperator == ConditionalOperator.YES
				? String.format("OPENED MAILING %s", primaryValue)
				: String.format("NOT (OPENED MAILING %s)", primaryValue);
	}

}
