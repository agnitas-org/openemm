/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.configuration;

import javax.sql.DataSource;

import com.agnitas.util.DbVendor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("databaseConfiguration")
public class DatabaseConfigurationImpl implements DatabaseConfiguration {

    private final DbVendor dbVendor;

    public DatabaseConfigurationImpl(@Qualifier("dataSource") DataSource dataSource) {
        this.dbVendor = DbVendor.getDbVendor(dataSource);
    }

    @Override
    public DbVendor getDbVendor() {
        return dbVendor;
    }
}
