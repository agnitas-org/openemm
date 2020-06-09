/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

public class Tools {
	static protected boolean isin (String needle, String user, String[] haystack) {
		if ((needle != null) && (haystack != null) && (haystack.length > 0)) {
			if ((user != null) && (user.length () > 0)) {
				for (String element : haystack) {
					if ((element != null) && needle.equals (user + "@" + element)) {
						return true;
					}
				}
				if (needle.equals (user + "@")) {
					return true;
				}
			}
			for (String element : haystack) {
				if ((element != null) && needle.equals (element)) {
					return true;
				}
			}
		}
		return false;
	}
}
