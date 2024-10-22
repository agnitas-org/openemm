/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

class DefaultSqlCodeProperties implements SqlCodeProperties {

	private boolean referenceTables;
	private boolean nonCustomerTables;
	private boolean subselects;
	private boolean dateArithmetics;
	
	@Override
	public boolean isUsingReferenceTables() {
		return this.referenceTables;
	}

	@Override
	public boolean isUsingNonCustomerTables() {
		return this.nonCustomerTables || isUsingReferenceTables();
	}

	@Override
	public boolean isUsingSubselects() {
		return this.subselects;
	}

	@Override
	public boolean isUsingDateArithmetics() { return this.dateArithmetics; }

	/**
	 * Called when code generation encountered a reference table.
	 */
	public void encounteredReferenceTable() {
		this.referenceTables = true;
	}
	
	/**
	 * Called when code generation encountered a table, which is not a customer-profile-table.
	 */
	public void encounteredNonCustomerTable() {
		this.nonCustomerTables = true;
	}
	
	/**
	 * Called when code generation uses a sub-select.
	 */
	public void encounteredSubselect() {
		this.subselects = true;
	}

	public void encounteredDateArithmetics() { this.dateArithmetics = true; }
}
