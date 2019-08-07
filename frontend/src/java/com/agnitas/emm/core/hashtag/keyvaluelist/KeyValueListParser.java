/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.keyvaluelist;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KeyValueListParser {

	private static final Pattern PATTERN = Pattern.compile("^\\s*([^ =]+)\\s*=\\s*(?:([^'\"][^ ]*)|'([^']*)'|\"([^\"]*)\")\\s*(?:,(.*))?$");
	
	public static final Map<String, String> parseKeyValueList(final String str) throws KeyValueListParserException {
		final Map<String, String> map = new HashMap<>();
		
		String remaining = str;
		while(!"".equals(remaining) && remaining != null) {
			final Matcher m = PATTERN.matcher(remaining);
			
			if(m.matches()) {
				final String key = m.group(1);
				remaining = m.group(5);
				
				if(m.group(2) != null) {
					map.put(key, m.group(2));
				} else if(m.group(3) != null) {
					map.put(key, m.group(3));
				} else if(m.group(4) != null) {
					map.put(key, m.group(4));
				} else {
					throw new KeyValueListParserException("Error parsing key in: " + str);
				}
				
			} else {
				throw new KeyValueListParserException("Malformed key value list: " + str);
			}
		}
		
		return map;
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println(parseKeyValueList("date='dd.mm.yyyy'"));
	}

}
