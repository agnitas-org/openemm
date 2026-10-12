/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.util;

import java.util.function.Predicate;
import javax.sql.DataSource;

public enum DbVendor {

	ORACLE("Oracle", DbUtilities::checkDbVendorIsOracle),
	MARIADB("MariaDB", DbUtilities::checkDbVendorIsMariaDB),
	POSTGRES("PostgreSQL", DbUtilities::checkDbVendorIsPostgreSQL);

	private final String name;
	private final Predicate<DataSource> matcher;

	DbVendor(String name, Predicate<DataSource> matcher) {
		this.name = name;
		this.matcher = matcher;
	}

	public String getName() {
		return name;
	}

	public static DbVendor getDbVendor(DataSource dataSource) {
		for (DbVendor vendor : values()) {
			if (vendor.matcher.test(dataSource)) {
				return vendor;
			}
		}

		throw new IllegalStateException("Cannot detect db vendor: no matching vendor found");
	}

}
