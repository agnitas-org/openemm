/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

public class UnknownReferenceTableColumnException extends ReferenceTableResolveException {
	private static final long serialVersionUID = -6952109366409546601L;
	
	private final String tableName;
	private final String columnName;
	private final int companyID;
	
	public UnknownReferenceTableColumnException(int companyID, String tableName, String columnName) {
		super("Unknown column '" + columnName + "' of reference table '" + tableName + "' for company ID " + companyID);
		
		this.tableName = tableName;
		this.columnName = columnName;
		this.companyID = companyID;
	}

	public String getTableName() {
		return tableName;
	}
	
	public String getColumnName() {
		return this.columnName;
	}

	public int getCompanyID() {
		return companyID;
	}
	
}
