/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.configuration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseConfigurationImpl implements DatabaseConfiguration {

	/** The logger. */
    private static final Logger logger = LogManager.getLogger(DatabaseConfigurationImpl.class);

    private static final String ORACLE = "oracle";

    private String vendor;

    public DatabaseConfigurationImpl(DataSource dataSource) {
        try(Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();
                vendor = metaData.getDatabaseProductName();
            }
        } catch (SQLException e) {
            logger.error("Unable to obtain database connection.");
        }
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    @Override
    public boolean isOracle() {
        return ORACLE.equalsIgnoreCase(vendor);
    }
}
