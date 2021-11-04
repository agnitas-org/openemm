/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchReservedLiteralsConfig;

public class MySqlFulltextSearchReservedLiteralsConfig implements FulltextSearchReservedLiteralsConfig {

    private static final String AT_SIGN = "@";
    private static final String QUOTED_AT_SIGN = "\"@\"";

    private List<Character> specialChars = new ArrayList<>();
    private List<String> specialWords = new ArrayList<>();

    public MySqlFulltextSearchReservedLiteralsConfig() {}

    public MySqlFulltextSearchReservedLiteralsConfig(List<Character> specialChars) {
        this.specialChars = specialChars;
    }

    @Override
    public List<Character> getSpecialCharacters() {
        return specialChars;
    }

    @Override
    public List<String> getSpecialWords() {
        return specialWords;
    }

    @Override
    public String sanitize(String token) {
        return token.replaceAll(QUOTED_AT_SIGN, AT_SIGN);
    }

    @Override
    public boolean isContainsDateBaseDependentControlCharacters(String searchQuery) {
        return StringUtils.contains(searchQuery, AT_SIGN);
    }
}
