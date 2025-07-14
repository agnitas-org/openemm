/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext;

import com.agnitas.util.FulltextSearchInvalidQueryException;

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

    void validateTokens(String[] tokens) throws FulltextSearchInvalidQueryException;
}
