/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.word.impl;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;

public class OracleMultiEscapeProcessor implements WordProcessor {

    private static final String DOUBLE_QUOTES = "\"";

    @Override
    public String process(String word) {
        return word.replaceAll(DOUBLE_QUOTES, StringUtils.EMPTY);
    }

}
