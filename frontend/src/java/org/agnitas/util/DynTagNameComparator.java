/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator for sorting text-modules in content tab.
 * Compares dynTag names as usual Strings but number values in Strings
 * are compared like int values
 * 
 * Comarator sorts Strings by tag names so that names are sorted like usual
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
	public int compare(String firstName, String secondName) {
		if (firstName.equalsIgnoreCase(secondName)) {
			return 0;
		}
		List<String> firstNameTokens = AgnUtils.splitIntoNumbersAndText(firstName);
		List<String> secondNameTokens = AgnUtils.splitIntoNumbersAndText(secondName);
		int tokensNum = Math.min(firstNameTokens.size(), secondNameTokens.size());
		for (int i = 0; i < tokensNum; i++) {
			String firstToken = firstNameTokens.get(i);
			String secondToken = secondNameTokens.get(i);
			if (firstToken.equalsIgnoreCase(secondToken)) {
				continue;
			} else if (AgnUtils.isDigit(firstToken) && AgnUtils.isDigit(secondToken)) {
				int firstNumber = Integer.parseInt(firstToken);
				int secondNumber = Integer.parseInt(secondToken);
				return firstNumber < secondNumber ? -1 : 1;
			} else {
				return firstName.compareToIgnoreCase(secondName);
			}
		}
		return firstName.compareToIgnoreCase(secondName);
	}
}
