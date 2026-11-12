/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.word.impl.postgresql;

import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.WordProcessUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts a single word or a phrase in double quotes into a quoted tsquery lexeme.
 * <p>
 * Quoting makes every input valid for to_tsquery(): tsquery operators and other special characters
 * inside the quotes are treated as word separators by the text search parser instead of breaking the query.
 * <p>
 * tsquery only supports prefix matching, so a word with wildcards ('*', '%', '?') is cut at the first
 * wildcard and matched as prefix (e.g. {@code birth*day} becomes {@code 'birth':*}).
 */
public class PostgreSQLWordProcessor implements WordProcessor {

    private static final String WILDCARDS = "*%?";
    private static final String PREFIX_MATCH = ":*";

    @Override
    public String process(String word) {
        // Group in parentheses is already processed by the query generator, but empty group "()" is invalid in tsquery
        if (WordProcessUtils.isBracketTerms(word)) {
            return StringUtils.isBlank(word.substring(1, word.length() - 1)) ? "" : word;
        }

        if (word.length() >= 2 && WordProcessUtils.isDoubleQuotesEnclosed(word)) {
            return quoteIfNotBlank(word.substring(1, word.length() - 1), "");
        }

        String withoutLeadingWildcards = StringUtils.stripStart(word, WILDCARDS);
        int wildcardIndex = StringUtils.indexOfAny(withoutLeadingWildcards, WILDCARDS);
        if (wildcardIndex >= 0) {
            return quoteIfNotBlank(withoutLeadingWildcards.substring(0, wildcardIndex), PREFIX_MATCH);
        }

        return quoteIfNotBlank(withoutLeadingWildcards, "");
    }

    private static String quoteIfNotBlank(String text, String suffix) {
        return StringUtils.isBlank(text) ? "" : quote(text) + suffix;
    }

    public static String quote(String text) {
        return "'" + text.replace("\\", "\\\\").replace("'", "''") + "'";
    }
}
