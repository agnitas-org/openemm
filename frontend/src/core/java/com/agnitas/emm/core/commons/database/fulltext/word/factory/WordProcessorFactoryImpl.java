/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.word.factory;

import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBAtSignWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBSingleWildcardWordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.mariadb.MariaDBWildcardWordProcessor;

public class WordProcessorFactoryImpl implements WordProcessorFactory {

    @Override
    public Set<WordProcessor> createWordProcessors() {
        Set<WordProcessor> wordProcessors = new HashSet<>();
        wordProcessors.add(new MariaDBSingleWildcardWordProcessor());
        wordProcessors.add(new MariaDBWildcardWordProcessor());
        wordProcessors.add(new MariaDBAtSignWordProcessor());
        return wordProcessors;
    }
}
