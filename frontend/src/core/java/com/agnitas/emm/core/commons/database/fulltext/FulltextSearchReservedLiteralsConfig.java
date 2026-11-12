/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext;

import java.util.List;

public interface FulltextSearchReservedLiteralsConfig {

    List<Character> getSpecialCharacters();

    List<String> getSpecialWords();

    default boolean isReservedCharacter(Character character) {
        return getSpecialCharacters().contains(character);
    }

    default boolean isReservedWord(String word) {
        return getSpecialWords().contains(word);
    }

    default String escapeCharacter(char character) {
        return "\\" + character;
    }

    default String escapeWord(String word) {
        return word;
    }

    default String sanitize(String token) {
        return token;
    }

    default boolean isContainsDateBaseDependentControlCharacters(String searchQuery) {
        return false;
    }

    void validateTokens(String[] tokens);

    /**
     * Query to use when the search query cannot be transformed: unmatched quotes or parenthesis,
     * or (if {@link #isOperatorRequiredBetweenOperands()}) an operator without operand, e.g. "(a +)".
     */
    default String getFallbackQuery(String searchQuery) {
        return searchQuery;
    }

    /**
     * Whether operands written without operator in between (e.g. "word(suffix)") have to be connected by an operator.
     * Otherwise they are joined without any separator, in reverse order (e.g. "(suffix)word").
     */
    default boolean isOperatorRequiredBetweenOperands() {
        return false;
    }

    /**
     * Validates the generated query, e.g. that it is not empty for not empty search query.
     *
     * @param searchQuery search query after sanitizing and escaping (not the raw user input)
     * @param generatedQuery query generated from the search query
     */
    default void validateGeneratedQuery(String searchQuery, String generatedQuery) {
        // nothing to validate by default
    }

}
