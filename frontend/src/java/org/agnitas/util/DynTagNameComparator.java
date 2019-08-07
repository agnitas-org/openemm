/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Comparator for sorting text-modules in content tab.
 * Compares dynTag names as usual Strings but number values in Strings
 * are compared like int values
 * 
 * Comparator sorts Strings by tag names so that names are sorted like usual
 * Strings but number values inside these Strings are compared like numbers
 *
 * Example:
 * "3.2 module"
 * "1 module"
 * "10 module"
 * "3 module"
 * "4 module"
 *
 * will be sorted in a following way:
 * "1 module"
 * "3 module"
 * "3.2 module"
 * "4 module"
 * "10 module"
 */
public class DynTagNameComparator implements Comparator<String> {
    /**
     * Compares two names of dynTags as usual Strings but
     * number values in names are compared like int values
     *
     * @param firstName first name
     * @param secondName second name
     * @return -1 if name1 is lesser; 0 if names are equal; 1 if name1 is greater.
     */
	@Override
	public int compare(String name1, String name2) {
		if (StringUtils.equalsIgnoreCase(name1, name2)) {
			return 0;
		}

		List<String> tokens1 = AgnUtils.splitIntoNumbersAndText(name1);
		List<String> tokens2 = AgnUtils.splitIntoNumbersAndText(name2);
		int tokensNum = Math.min(tokens1.size(), tokens2.size());

		for (int i = 0; i < tokensNum; i++) {
			String token1 = tokens1.get(i);
			String token2 = tokens2.get(i);

			int result = name1.compareToIgnoreCase(name2);
			if (result != 0) {
				if (AgnUtils.isDigit(token1) && AgnUtils.isDigit(token2)) {
					return Integer.parseInt(token1) < Integer.parseInt(token2) ? -1 : 1;
				} else {
					return result;
				}
			}
		}

		return AgnUtils.compareIgnoreCase(name1, name2);
	}
}
