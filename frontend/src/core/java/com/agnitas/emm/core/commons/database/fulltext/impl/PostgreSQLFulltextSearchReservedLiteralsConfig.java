/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchReservedLiteralsConfig;
import com.agnitas.emm.core.commons.database.fulltext.word.impl.postgresql.PostgreSQLWordProcessor;
import com.agnitas.messages.Message;
import com.agnitas.util.FulltextSearchInvalidQueryException;
import org.apache.commons.lang3.StringUtils;

/**
 * No characters or words have to be escaped here: {@link PostgreSQLWordProcessor} puts every word into quotes,
 * which makes all special characters of tsquery harmless.
 */
public class PostgreSQLFulltextSearchReservedLiteralsConfig implements FulltextSearchReservedLiteralsConfig {

    private static final String DOUBLE_QUOTES = "\"";
    private static final String CONJUNCTION = "+";
    private static final String AT_SIGN = "@";
    private static final String QUOTED_AT_SIGN = "\"@\"";

    @Override
    public List<Character> getSpecialCharacters() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSpecialWords() {
        return Collections.emptyList();
    }

    @Override
    public String sanitize(String token) {
        return token.replace(QUOTED_AT_SIGN, AT_SIGN);
    }

    @Override
    public void validateTokens(String[] tokens) {
        boolean withinQuotes = false;
        for (int i = 0; i < tokens.length; i++) {
            if (!DOUBLE_QUOTES.equals(tokens[i])) {
                continue;
            }

            // Only an opening double quote directly followed by the closing one is an empty phrase (not e.g. "a""b")
            if (!withinQuotes && i + 1 < tokens.length && DOUBLE_QUOTES.equals(tokens[i + 1])) {
                throw new FulltextSearchInvalidQueryException(
                        "Invalid search phrase! Has paired double quotes without content inside!",
                        Message.of("error.search.empty")
                );
            }

            withinQuotes = !withinQuotes;
        }

        if (tokens.length > 0 && CONJUNCTION.equals(tokens[tokens.length - 1])) {
            throw new FulltextSearchInvalidQueryException(
                    "Invalid search phrase! Ends with invalid symbol!",
                    Message.of("error.search.operator.forbidden", "'" + CONJUNCTION + "'")
            );
        }
    }

    /**
     * Adjacent operands without operator (e.g. "customer(s)") would be invalid tsquery syntax.
     */
    @Override
    public boolean isOperatorRequiredBetweenOperands() {
        return true;
    }

    /**
     * Search query consisting only of wildcards or empty groups (e.g. "*", "()") results in an empty query,
     * which would silently disable the search filter.
     */
    @Override
    public void validateGeneratedQuery(String searchQuery, String generatedQuery) {
        if (StringUtils.isNotBlank(searchQuery) && StringUtils.isBlank(generatedQuery)) {
            throw new FulltextSearchInvalidQueryException(
                    "Search phrase contains no characters to search for!",
                    Message.of("error.search.character.missing")
            );
        }
    }

    /**
     * Raw search query would be invalid tsquery syntax, so search for any of the words instead.
     */
    @Override
    public String getFallbackQuery(String searchQuery) {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(searchQuery)))
                .map(word -> PostgreSQLWordProcessor.quote(word) + ":*")
                .collect(Collectors.joining(" | "));
    }
}
