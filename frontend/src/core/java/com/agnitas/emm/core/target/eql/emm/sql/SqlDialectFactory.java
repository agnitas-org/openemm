/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.target.eql.emm.sql;

import javax.sql.DataSource;

import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;
import com.agnitas.util.DbVendor;

/**
 * Factory creating instance of {@link SqlDialect} depending on underlaying database engine.
 */
public class SqlDialectFactory {

	/** Datasource to check used database engine. */
	private final DataSource dataSource;

    public SqlDialectFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
	 * Creates SQLDialect depending on used database engine.
	 *
	 * @return SQLDialect for used database engine
	 */
	public SqlDialect databaseDependentInstance() {
		return switch (DbVendor.getDbVendor(dataSource)) {
            case MARIADB -> new MariaDbDialect();
			case ORACLE -> createOracleDialect();
			case POSTGRES -> new PostgreSQLDialect();
		};
	}

	protected SqlDialect createOracleDialect() {
		throw new UnsupportedOperationException("Oracle is not supported");
	}

}
