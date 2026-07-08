/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

/**
 * Transport class combining generated SQL code and
 * properties of generated code.
 */
public class SqlCode {

	/** Generated SQL code. */
	private final String sql;
	
	/** Properties of SQL code. */
	private final SqlCodeProperties properties;
	
	/**
	 * Creates a new instance for given code and properties.
	 * 
	 * @param sql SQL code
	 * @param properties properties of SQL code
	 */
	public SqlCode(final String sql, final SqlCodeProperties properties) {
		this.sql = sql;
		this.properties = properties;
	}
	
	/**
	 * Returns the SQL code.
	 * 
	 * @return SQL code
	 */
	public String getSql() {
		return this.sql;
	}
	
	/**
	 * Returns the code properties.
	 * 
	 * @return code properties
	 */
	public SqlCodeProperties getCodeProperties() {
		return this.properties;
	}
	
}
