/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.sql;

import com.agnitas.emm.core.target.eql.codegen.CodeFragment;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat.DateFragment;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;

/**
 * PostgreSQL-specific implementation of {@link SqlDialect}.
 */
public class PostgreSQLDialect implements SqlDialect {

	/** PostgreSQL format symbol for "day, numeric, 2 digits". */
	private static final String POSTGRES_DAY_SYMBOL = "DD";

	/** PostgreSQL format symbol for "month, numeric, 2 digits". */
	private static final String POSTGRES_MONTH_SYMBOL = "MM";

	/** PostgreSQL format symbol for "year, numeric, 4 digits". */
	private static final String POSTGRES_YEAR_SYMBOL = "YYYY";

	@Override
	public String stringConcat(String left, String right) {
		return "(" + left + ") || (" + right + ")";
	}
	
	@Override
	public String isEmpty(CodeFragment operand) {
		StringBuilder builder = new StringBuilder();

		builder.append(operand.getCode());
		builder.append(" IS NULL");
		if(operand.evaluatesToType(DataType.TEXT)) {
			builder.append(" OR ");
			builder.append(operand.getCode());
			builder.append("=''");
		}

		return builder.toString();
	}

	@Override
	public String today() {
		return "current_timestamp";
	}

	@Override
	public String dateToString(String code, EqlDateFormat dateFormat) {
		return "to_char(" + code + ", '" + dateFormat(dateFormat) + "')";
	}

	@Override
	public String dateFormat(EqlDateFormat dateFormat) {
		StringBuilder builder = new StringBuilder();

		for (DateFragment fragment : dateFormat) {
			switch (fragment) {
				case DAY:
					builder.append(POSTGRES_DAY_SYMBOL);
					break;

				case MONTH:
					builder.append(POSTGRES_MONTH_SYMBOL);
					break;

				case YEAR:
					builder.append(POSTGRES_YEAR_SYMBOL);
					break;

				case PERIOD, HYPHEN, UNDERSCORE:
					builder.append(fragment.pattern());
					break;

				default:
					throw new IllegalArgumentException("Unhandled date fragment " + fragment);
			}
		}

		return builder.toString();
	}

	@Override
	public String dateAddDays(String left, String right) {
		return "(" + left + ") + (" + right + ") * INTERVAL '1 day'";
	}

	@Override
	public String dateSubDays(String left, String right) {
		return "(" + left + ") - (" + right + ") * INTERVAL '1 day'";
	}

}
