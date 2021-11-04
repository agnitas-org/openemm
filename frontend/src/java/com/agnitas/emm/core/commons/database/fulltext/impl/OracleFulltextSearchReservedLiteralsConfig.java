/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchReservedLiteralsConfig;

public class OracleFulltextSearchReservedLiteralsConfig implements FulltextSearchReservedLiteralsConfig {

    private static final List<Character> SPECIAL_CHARS = new ArrayList<>(Arrays.asList(';', ',', '&', '=', '{', '}', '[', ']', '-', '~', '|', '$', '!', '>', '_'));

    private static final List<String> SPECIAL_WORDS = new ArrayList<>(Arrays.asList(
            "ABOUT",
            "ACCUM",
            "AND",
            "BT",
            "BTG",
            "BTI",
            "BTP",
            "EQUIV",
            "FUZZY",
            "HASPATH",
            "INPATH",
            "MDATA",
            "MINUS",
            "NEAR",
            "NOT",
            "NT",
            "NTG",
            "NTI",
            "NTP",
            "OR",
            "PT",
            "RT",
            "SQE",
            "SYN",
            "TR",
            "TRSYN",
            "TT",
            "WITHIN"
    ));

    @Override
    public List<Character> getSpecialCharacters() {
        return ListUtils.emptyIfNull(SPECIAL_CHARS);
    }

    @Override
    public List<String> getSpecialWords() {
        return ListUtils.emptyIfNull(SPECIAL_WORDS);
    }

    @Override
    public boolean isReservedWord(String word) {
        return getSpecialWords().contains(StringUtils.upperCase(word));
    }

    @Override
    public String escapeWord(String word) {
        return "{" + word + "}";
    }
}
