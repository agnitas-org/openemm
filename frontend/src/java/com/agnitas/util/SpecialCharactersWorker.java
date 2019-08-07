/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.agnitas.util.CaseInsensitiveSet;
import org.apache.commons.lang.StringUtils;

public class SpecialCharactersWorker {
    private static Map<String, String> SPECIAL_CHARACTERS_MAP1 = new HashMap<>();
    private static Set<String> SPECIAL_CHARACTER_REPLACEMENT1_EXCLUDED_CHARSETS = new CaseInsensitiveSet();
    
    private static Map<String, String> SPECIAL_CHARACTERS_MAP2 = new HashMap<>();
    private static Set<String> SPECIAL_CHARACTER_REPLACEMENT2_EXCLUDED_CHARSETS = new CaseInsensitiveSet();

    static {
        SPECIAL_CHARACTERS_MAP1.put("\u2013", "-");         // –
        SPECIAL_CHARACTERS_MAP1.put("\u0060", "'");         // `
        SPECIAL_CHARACTERS_MAP1.put("\u00B4", "'");         // ´
        SPECIAL_CHARACTERS_MAP1.put("\u0027", "'");         // '
        SPECIAL_CHARACTERS_MAP1.put("\u201E", "\"");        // „
        SPECIAL_CHARACTERS_MAP1.put("\u201C", "\"");        // “
        SPECIAL_CHARACTERS_MAP1.put("\u2093", "x");         // ₓ
        SPECIAL_CHARACTERS_MAP1.put("\uFF59", "y");         // ｙ
        SPECIAL_CHARACTERS_MAP1.put("\u2122", "(TM)");      // ™
        
        SPECIAL_CHARACTER_REPLACEMENT1_EXCLUDED_CHARSETS.add("UTF-8");

        SPECIAL_CHARACTERS_MAP2.put("\u00A9", "Copyright"); // ©
        SPECIAL_CHARACTERS_MAP2.put("\u00AE", "(R)");       // ®
        SPECIAL_CHARACTERS_MAP2.put("\u20AC", "Euro");      // €

        SPECIAL_CHARACTER_REPLACEMENT2_EXCLUDED_CHARSETS.add("ISO-8859-15");
        SPECIAL_CHARACTER_REPLACEMENT2_EXCLUDED_CHARSETS.add("UTF-8");
    }
    
    public static String processString(String stringToProcess, String encodingCharSet){
    	if (StringUtils.isNotEmpty(stringToProcess)) {
    		if (!SPECIAL_CHARACTER_REPLACEMENT1_EXCLUDED_CHARSETS.contains(encodingCharSet)) {
    			for (Entry<String, String> entry : SPECIAL_CHARACTERS_MAP1.entrySet()) {
	                stringToProcess = stringToProcess.replaceAll(entry.getKey(), entry.getValue());
	            }
	        }
	    	if (!SPECIAL_CHARACTER_REPLACEMENT2_EXCLUDED_CHARSETS.contains(encodingCharSet)) {
	    		for (Entry<String, String> entry : SPECIAL_CHARACTERS_MAP2.entrySet()) {
	                stringToProcess = stringToProcess.replaceAll(entry.getKey(), entry.getValue());
	            }
	    	}
    	}
        return stringToProcess;
    }
}
