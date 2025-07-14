/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

import com.agnitas.emm.core.target.eql.ast.RelationalInfixOperator;
import com.agnitas.emm.core.target.eql.codegen.UnhandledOperatorException;

public final class OperatorUtil {

	
	public static final String eqlOperatorToSqlOperator(final RelationalInfixOperator operator) throws UnhandledOperatorException {
		switch (operator) {
		case EQ: 	return "=";
		case LT:	return "<";
		case GT:	return ">";
		case NEQ:	return "<>";
		case LEQ:	return "<=";
		case GEQ:	return ">=";
		default:
			throw new UnhandledOperatorException(operator);
		}

	}

}
