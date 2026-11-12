/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.word.factory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.commons.database.configuration.DatabaseConfiguration;
import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBAtSignWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBSingleWildcardWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBWildcardWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.postgresql.PostgreSQLWordProcessor;
import org.springframework.stereotype.Component;

@Component("wordProcessorFactory")
public class WordProcessorFactoryImpl implements WordProcessorFactory {

    private final DatabaseConfiguration databaseConfiguration;

    public WordProcessorFactoryImpl(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    @Override
    public Set<WordProcessor> createWordProcessors() {
        return switch (databaseConfiguration.getDbVendor()) {
            case MARIADB -> new LinkedHashSet<>(List.of(
                    new MariaDBSingleWildcardWordProcessor(),
                    new MariaDBWildcardWordProcessor(),
                    new MariaDBAtSignWordProcessor()
            ));
            case POSTGRES -> Set.of(new PostgreSQLWordProcessor());
            case ORACLE -> createOracleWordProcessors();
        };
    }

    protected Set<WordProcessor> createOracleWordProcessors() {
        throw new UnsupportedOperationException("Oracle is not supported");
    }
}
