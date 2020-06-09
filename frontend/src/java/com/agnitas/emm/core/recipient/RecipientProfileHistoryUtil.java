/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.dao.impl.ComCompanyDaoImpl;

/**
 * Utility class for profile field history feature.
 */
public class RecipientProfileHistoryUtil {
	
	/** Read-only set of column names for profile fields historized by default. */
	public static final Set<String> DEFAULT_COLUMNS_FOR_HISTORY = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
				ComCompanyDaoImpl.STANDARD_FIELD_EMAIL,
				ComCompanyDaoImpl.STANDARD_FIELD_FIRSTNAME,
				ComCompanyDaoImpl.STANDARD_FIELD_LASTNAME,
				ComCompanyDaoImpl.STANDARD_FIELD_GENDER,
				ComCompanyDaoImpl.STANDARD_FIELD_MAILTYPE,
				ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK
	)));

	public static boolean isDefaultColumn(String column) {
		return StringUtils.isNotEmpty(column)
				&& DEFAULT_COLUMNS_FOR_HISTORY.contains(column.toLowerCase());
	}

}
