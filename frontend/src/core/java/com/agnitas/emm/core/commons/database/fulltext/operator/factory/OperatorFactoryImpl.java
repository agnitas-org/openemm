/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.operator.factory;

import java.util.Map;

import com.agnitas.emm.core.commons.database.configuration.DatabaseConfiguration;
import com.agnitas.emm.core.commons.database.fulltext.operator.Operator;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.mariadb.MariaDBConjunction;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.mariadb.MariaDBDisjunction;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.postgresql.PostgreSQLConjunction;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.postgresql.PostgreSQLDisjunction;
import org.springframework.stereotype.Component;

@Component("operatorFactory")
public class OperatorFactoryImpl implements OperatorFactory {

    private final DatabaseConfiguration databaseConfiguration;

    public OperatorFactoryImpl(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    @Override
    public Map<String, Operator> createOperators() {
        return switch (databaseConfiguration.getDbVendor()) {
            case MARIADB -> Map.of("+", new MariaDBConjunction(), " ", new MariaDBDisjunction());
            case POSTGRES -> Map.of("+", new PostgreSQLConjunction(), " ", new PostgreSQLDisjunction());
            case ORACLE -> createOracleOperators();
        };
    }

    protected Map<String, Operator> createOracleOperators() {
        throw new UnsupportedOperationException("Oracle is not supported");
    }
}
