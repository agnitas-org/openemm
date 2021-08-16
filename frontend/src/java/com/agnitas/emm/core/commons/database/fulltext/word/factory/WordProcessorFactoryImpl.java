/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.word.factory;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.database.configuration.DatabaseConfiguration;
import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mysql.MysqlWildcardWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.oracle.OracleMultiEscapeProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.oracle.OracleWildcardWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.SingleWildcardWordProcessor;

public class WordProcessorFactoryImpl implements WordProcessorFactory {

    private DatabaseConfiguration databaseConfiguration;

    @Override
    public Set<WordProcessor> createWordProcessors() {
        Set<WordProcessor> wordProcessors = new HashSet<>();
        wordProcessors.add(new SingleWildcardWordProcessor());
        if (databaseConfiguration.isOracle()) {
            wordProcessors.add(new OracleWildcardWordProcessor());
            wordProcessors.add(new OracleMultiEscapeProcessor());
        } else {
            wordProcessors.add(new MysqlWildcardWordProcessor());
        }
        return wordProcessors;
    }

    @Required
    public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }
}
