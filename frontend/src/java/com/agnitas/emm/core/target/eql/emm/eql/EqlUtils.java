/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.eql;

import com.agnitas.emm.core.target.eql.codegen.InvalidEqlDateFormatException;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;

public class EqlUtils {
	
	public static TargetOperator getValidOperator(TargetOperator[] validOperator, int operatorCode) {
		return operatorCode > 0 && operatorCode <= validOperator.length ?
				validOperator[operatorCode - 1] : null;
	}

    public static String getIsEmptyOperatorValue(String value) {
		return StringUtils.equalsIgnoreCase("NOT_NULL", value) ? "IS NOT EMPTY" : "IS EMPTY";
    }

    public static String toEQLDateFormat(String dateFormat) {
			/*
			 * Note on date formats:
			 *
			 * We do not use Mysql-specific format parameters (like %Y) here.
			 * We always get something like "yyyymmdd" from UI.
			 *
			 * Transformation to Mysql-specific format parameters is done
			 * in TargetNodeDate by calling AgnUtils.sqlDateString()!
			 */
		try {
			StringBuilder sb = new StringBuilder();
			for (EqlDateFormat.DateFragment fragment : EqlDateFormat.parse(StringUtils.upperCase(dateFormat))) {
				sb.append(fragment.pattern());
			}
			return sb.toString();
		} catch (InvalidEqlDateFormatException e) {
			throw new IllegalArgumentException("Invalid date format pattern: " + dateFormat, e);
		}
	}
	
	public static String makeEquation(String field, TargetOperator operator1, Object value1, boolean disableThreeValuedLogic) {
		return makeEquation(field, operator1, value1, null, null, disableThreeValuedLogic);
	}
	
	public static String makeEquation(String field, TargetOperator operator1, Object value1, TargetOperator operator2, String value2, boolean disableThreeValuedLogic) {
		TargetOperator comparisonOperator = operator2 == null ? operator1 : operator2;
		StringBuilder operatorExpression = new StringBuilder();
		
		if (disableThreeValuedLogic) {
			disableThreeValuedLogic = TargetNode.isThreeValuedLogicOperator(comparisonOperator);
			
			if (TargetNode.isInequalityOperator(comparisonOperator)) {
				operatorExpression.append('(');
			} else {
				comparisonOperator = TargetNode.getOppositeOperator(comparisonOperator);
				operatorExpression.append("NOT (");
			}
		}
		
		if (operator2 == null) {
			operatorExpression.append(field)
					.append(" ")
					.append(comparisonOperator.getOperatorSymbol())
					.append(" ")
					.append(value1);
		} else {
			operatorExpression.append(field)
					.append(" ")
					.append(operator1 == TargetNode.OPERATOR_MOD ? "%" : operator1.getOperatorSymbol())
					.append(" ")
					.append(value1)
					.append(" ")
					.append(comparisonOperator.getOperatorSymbol())
					.append(" ")
					.append(value2);
		}
		
		if (disableThreeValuedLogic) {
			operatorExpression.append(" OR ")
					.append(field)
					.append(" IS EMPTY")
					.append(')');
		}
		
		return operatorExpression.toString();
	}
	
	public static String convertChainOperator(int chainOperator) {
		switch(chainOperator) {
			case TargetNode.CHAIN_OPERATOR_AND:
				return "AND";
    
			case TargetNode.CHAIN_OPERATOR_OR:
				return "OR";
            
			default:
				return "";
        }
	}
}
