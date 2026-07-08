/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.component;

import java.security.SecureRandom;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CompanySecretKeyGenerator {

    private static final int LENGTH = 32;
    private static final Set<Character> FORBIDDEN_CHARACTERS = Set.of('\'', '"', ',', ';', '\\');

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SAFE_CHARS;

    static {
        StringBuilder sb = new StringBuilder();
        for (char c = 33; c <= 126; c++) {
            if (!FORBIDDEN_CHARACTERS.contains(c)) {
                sb.append(c);
            }
        }
        SAFE_CHARS = sb.toString();
    }

    public String generateSecretKey() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(SAFE_CHARS.length());
            sb.append(SAFE_CHARS.charAt(index));
        }
        return sb.toString();
    }

}
