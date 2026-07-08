/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

/**
 * Some properties of the SQL code generated from EQL.
 * 
 * These properties are used to define usability of target groups in 
 * specifics contexts (like content blocks);
 */
public interface SqlCodeProperties {

	/**
	 * Returns <code>true</code>, if the SQL code uses reference tables.
	 * If this method returns <code>true</code>, {@link #isUsingNonCustomerTables()} MUST also
	 * return <code>true</code>.
	 * 
	 * @return <code>true</code> if code uses reference tables
	 * 
	 * @see #isUsingNonCustomerTables()
	 */
	boolean isUsingReferenceTables();
	
	/**
	 * Returns <code>true</code>, if the SQL code uses tables other than customer profile tables.
	 * This method MUST return <code>true</code> if {@link #isUsingReferenceTables()} also returns <code>true</code>.
	 * 
	 * @return <code>true</code> if code uses tables other than customer profile tables.
	 * 
	 * @see #isUsingReferenceTables()
	 */
	boolean isUsingNonCustomerTables();
	
	/**
	 * Returns <code>true</code> if the generated code uses sub-selects.
	 * In common, this method returns <code>true</code>, if {@link #isUsingNonCustomerTables()} and/or {@link #isUsingReferenceTables()}
	 * returns <code>true</code>. 
	 *
	 * @return <code>true</code> if generated code uses sub-selects.
	 * 
	 * @see #isUsingNonCustomerTables()
	 * @see #isUsingReferenceTables()
	 */
	boolean isUsingSubselects();

	/**
	 * Returns <code>true</code> if the generated code uses date arithmetics.
	 *
	 * @return <code>true</code> if generated code uses date arithmetics.
	 */
	boolean isUsingDateArithmetics();
}
