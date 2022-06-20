/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

import org.agnitas.util.DateUtilities;

public interface Constants {
	String DATE_PATTERN_FULL = DateUtilities.YYYY_MM_DD_HH_MM;

	int MILLISECONDS_PER_MINUTE = 60 * 1000;
	int MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
	int MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;
}
