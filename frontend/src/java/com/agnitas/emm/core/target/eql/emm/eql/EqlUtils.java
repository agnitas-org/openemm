/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.eql;

import java.util.Optional;

import org.agnitas.target.ChainOperator;
import org.agnitas.target.ConditionalOperator;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.InvalidEqlDateFormatException;

public class EqlUtils {

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

	public static String makeEquation(String field, ConditionalOperator operator1, Object value1, boolean disableThreeValuedLogic) {
		return makeEquation(field, operator1, value1, null, null, disableThreeValuedLogic);
	}

	public static String makeEquation(String field, ConditionalOperator operator1, Object value1, ConditionalOperator operator2, String value2, boolean disableThreeValuedLogic) {
		ConditionalOperator comparisonOperator = operator2 == null ? operator1 : operator2;
		final StringBuilder operatorExpression = new StringBuilder();

		if (disableThreeValuedLogic) {
			disableThreeValuedLogic = comparisonOperator.isThreeValuedLogicOperator();

			if (comparisonOperator.isInequalityOperator()) {
				operatorExpression.append('(');
			} else {
				comparisonOperator = comparisonOperator.getOppositeOperator();
				operatorExpression.append("NOT (");
			}
		}

		if (operator2 == null) {
			operatorExpression.append(field)
					.append(" ")
					.append(comparisonOperator.getEqlSymbol())
					.append(" ")
					.append(value1);
		} else {
			operatorExpression.append(field)
					.append(" ")
					.append(operator1 == ConditionalOperator.MOD ? "%" : operator1.getEqlSymbol())
					.append(" ")
					.append(value1)
					.append(" ")
					.append(comparisonOperator.getEqlSymbol())
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
		final Optional<ChainOperator> opOpt = ChainOperator.fromCode(chainOperator);
		
		if(opOpt.isPresent()) {
			switch(opOpt.get()) {
			case AND:	return "AND";
			case OR:	return "OR";
			default:	return "";
			}
		} else {
			return "";
		}
	}
}
