/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.sql;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat.DateFragment;
import com.agnitas.emm.core.target.eql.codegen.sql.CodeFragment;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;

/**
 * Oracle-specific implementation of {@link SqlDialect}.
 */
public class MysqlDialect implements SqlDialect {
	
	/** MySQL's format symbol for "day, numeric, 2 digits". */
	private static final String MYSQL_DAY_SYMBOL = "%d";
	
	/** MySQL's format symbol for "month, numeric, 2 digits". */
	private static final String MYSQL_MONTH_SYMBOL = "%m";
	
	/** MySQL's format symbol for "year, numeric, 4 digits". */
	private static final String MYSQL_YEAR_SYMBOL = "%Y";

	@Override
	public String stringConcat(String left, String right) {
		return "concat(" + left + ", " + right + ")";
	}
	
	@Override
	public String isEmpty(CodeFragment operand) {
		final StringBuffer buffer = new StringBuffer();
		
		buffer.append(operand.getCode());
		buffer.append(" IS NULL");
		if(operand.evaluatesToType(DataType.TEXT) || operand.evaluatesToType(DataType.DATE)) {
			buffer.append(" OR ");
			buffer.append(operand.getCode());
			buffer.append("=''");
		}
		
		return buffer.toString();
	}

	@Override
	public String today() {
		return "current_timestamp";
	}

	@Override
	public String dateToString(String code, EqlDateFormat dateFormat) {
		String format = dateFormat(dateFormat);
		
		return "date_format(" + code + ", '" + format + "')";
	}

	@Override
	public String dateFormat(EqlDateFormat dateFormat) {
		StringBuilder builder = new StringBuilder();

		for(DateFragment fragment : dateFormat) {
			switch(fragment) {
			case DAY:
					builder.append(MYSQL_DAY_SYMBOL);
				break;
				
			case MONTH:
					builder.append(MYSQL_MONTH_SYMBOL);
				break;
				
			case YEAR:
					builder.append(MYSQL_YEAR_SYMBOL);
					break;

				case PERIOD:
				case HYPHEN:
				case UNDERSCORE:
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
		return "(" + left + ") + INTERVAL (" + right + ") * 86400 SECOND";
	}

	@Override
	public String dateSubDays(String left, String right) {
		return "(" + left + ") - INTERVAL (" + right + ") * 86400 SECOND";
	}

}
