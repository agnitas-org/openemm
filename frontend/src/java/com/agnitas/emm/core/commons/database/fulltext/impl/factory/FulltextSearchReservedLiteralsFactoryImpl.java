/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.impl.factory;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.database.configuration.DatabaseConfiguration;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchReservedLiteralsConfig;
import com.agnitas.emm.core.commons.database.fulltext.impl.MySqlFulltextSearchReservedLiteralsConfig;
import com.agnitas.emm.core.commons.database.fulltext.impl.OracleFulltextSearchReservedLiteralsConfig;

public class FulltextSearchReservedLiteralsFactoryImpl implements FulltextSearchReservedLiteralsFactory {

    private DatabaseConfiguration databaseConfiguration;

    @Override
    public FulltextSearchReservedLiteralsConfig createdReservedLiteralsConfig() {
        return databaseConfiguration.isOracle() ? new OracleFulltextSearchReservedLiteralsConfig() : new MySqlFulltextSearchReservedLiteralsConfig();
    }

    @Required
    public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }
}
