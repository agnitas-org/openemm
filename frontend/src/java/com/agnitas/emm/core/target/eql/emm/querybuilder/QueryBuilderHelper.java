/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import org.apache.log4j.Logger;

import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.codegen.DataType;

public class QueryBuilderHelper {
	
	private static final transient Logger logger = Logger.getLogger(QueryBuilderHelper.class);
	
	public static final String booleanEqlOperator(final QueryBuilderCondition condition) throws QueryBuilderToEqlConversionException {
		switch(condition) {
		case AND:				return "AND";
		case OR:				return "OR";
		
		default:
			final String msg = String.format("Unhandled QueryBuilder condition '%s'", condition);
			
			logger.error(msg);
			
			throw new QueryBuilderToEqlConversionException(msg);
		}
	}
	
	public static final String relationalEqlOperator(final QueryBuilderOperator operator) throws QueryBuilderToEqlConversionException {
		switch(operator) {
		case EQ:				return "=";
		case GEQ:				return ">=";
		case GT:
		case AFTER:
			return ">";
		case LEQ:				return "<=";
		case LT:
		case BEFORE:
			return "<";
		case NEQ:				return "<>";
		
		default:
			final String msg = String.format("Unhandled QueryBuilder operator '%s'", operator);
			
			logger.error(msg);
			
			throw new QueryBuilderToEqlConversionException(msg);
		}
	}
	
	public static final QueryBuilderCondition conditionOfGroup(final QueryBuilderGroupNode node) throws QueryBuilderToEqlConversionException {
		final QueryBuilderCondition condition = QueryBuilderCondition.findByQueryBuilderName(node.getCondition());
		
		if(condition == null) {
			final String msg = String.format("Unknown group condition '%s'", node.getCondition());
			
			if(logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new QueryBuilderToEqlConversionException(msg);
		}
		
		return condition;
	}
	
	public static final QueryBuilderOperator operatorOfRule(final QueryBuilderRuleNode node, final DataType dataType) throws QueryBuilderToEqlConversionException {
		final QueryBuilderOperator operator = QueryBuilderOperator.findByQueryBuilderName(node.getOperator());
		
		if(operator == null) {
			final String msg = String.format("Cannot convert QueryBuilder operator '%s' to EQL", node.getOperator());
			
			throw new QueryBuilderToEqlConversionException(msg);
		}

		return operator;
	}

	public static final String relationalEqlOperatorToQueryBuilderString(final BinaryOperatorRelationalEqlNode.InfixOperator eqlOperator) throws EqlToQueryBuilderConversionException {
		return relationalEqlOperatorToQueryBuilder(eqlOperator).queryBuilderName();
	}

	public static final String relationalEqlOperatorToQueryBuilderString(final BinaryOperatorRelationalEqlNode.InfixOperator eqlOperator, DataType dataType) throws EqlToQueryBuilderConversionException {
		return relationalEqlOperatorToQueryBuilder(eqlOperator, dataType).queryBuilderName();
	}


	public static final QueryBuilderOperator relationalEqlOperatorToQueryBuilder(final BinaryOperatorRelationalEqlNode.InfixOperator eqlOperator) throws EqlToQueryBuilderConversionException {
		final QueryBuilderOperator operator = QueryBuilderOperator.findByEqlOperator(eqlOperator);

		if(operator == null) {
			final String msg = String.format("Binary relational operator %s not supported", eqlOperator);
			
			if(logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new EqlToQueryBuilderConversionException(msg);
		} else {
			return operator;
		}
	}
	
	public static final QueryBuilderOperator relationalEqlOperatorToQueryBuilder(final BinaryOperatorRelationalEqlNode.InfixOperator eqlOperator, DataType dataType) throws EqlToQueryBuilderConversionException {
		if (dataType == null) {
			return relationalEqlOperatorToQueryBuilder(eqlOperator);
		}
		
		final QueryBuilderOperator operator = QueryBuilderOperator.findByEqlOperator(eqlOperator, dataType);

		if(operator == null) {
			final String msg = String.format("Binary relational operator %s not supported by type %s", eqlOperator, dataType.name());
			
			if(logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new EqlToQueryBuilderConversionException(msg);
		} else {
			return operator;
		}
	}

	public static final String booleanEqlOperatorToQueryBuilderString(final BinaryOperatorBooleanEqlNode.InfixOperator operator) throws EqlToQueryBuilderConversionException {
		final QueryBuilderCondition condition = QueryBuilderCondition.findByEqlOperator(operator);

		if(condition == null) {
			final String msg = String.format("Binary boolean operator %s not supported", operator);
			
			if(logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new EqlToQueryBuilderConversionException(msg);
		} else {
			return condition.queryBuilderName();
		}
	}

}
