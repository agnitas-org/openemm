/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database;

public class UnknownTableOrColumnException extends DatabaseInformationException {
	private static final long serialVersionUID = -1434727683606437381L;
	
	public UnknownTableOrColumnException(final String tableName, final String columnName) {
		super(String.format("Unknown table or column (table: %s, column: %s)", tableName, columnName));
	}
}
