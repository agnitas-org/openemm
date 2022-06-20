/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

public class UnknownReferenceTableFaultyCodeException extends FaultyCodeException {
	private static final long serialVersionUID = 3729268230994498097L;
	
	private final String table;
	
	public UnknownReferenceTableFaultyCodeException(CodeLocation startLocation, String table, Throwable cause) {
		super(startLocation, "Unknown reference table: " + table, cause);
		
		this.table = table;
	}
	
	public UnknownReferenceTableFaultyCodeException(CodeLocation startLocation, Throwable cause) {
		this(startLocation, null, cause);
	}

	public String getReferenceTableName() {
		return this.table;
	}
}
