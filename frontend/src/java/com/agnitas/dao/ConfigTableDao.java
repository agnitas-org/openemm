/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.agnitas.util.Version;

/**
 * This class is intended to simplify access to the config_tbl.
 */
public interface ConfigTableDao {
	DataSource getDataSource();
	
	Map<String, Map<Integer, String>> getAllEntriesForThisHost();

	@DaoUpdateReturnValueCheck
	void storeEntry(String classString, String name, String hostName, String value, String description);

    void deleteEntry(String classString, String name);

	int getJobqueueHostStatus(String hostName);

	List<Map<String, Object>> getReleaseData(String hostNamePattern, String applicationTypePattern);

	void checkAndSetReleaseVersion();

	Date getCurrentDbTime();

	int getStartupCountOfLtsVersion(Version versionToCheck);

	int getStartupCount();
}
