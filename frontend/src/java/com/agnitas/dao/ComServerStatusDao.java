/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * DAO handler for status infos
 */
public interface ComServerStatusDao {
	boolean checkDatabaseConnection();

	boolean checkDatabaseVersion(int majorVersion, int minorVersion, int microVersion, int hotfixVersion);

	String getJobWorkerStatus(String string);

	String getDbUrl() throws Exception;

	String getDbVendor();

	Map<String, String> geDbInformation();

	int getLogEntryCount();

	List<String> getErrorJobsStatuses();

	List<String> getDKIMKeys();

	List<String> killRunningImports();

	File getFullTbl(String dbStatement, String tableName) throws Exception;

	String getDbVersion() throws Exception;

	List<String> getErroneousImports();

	List<String> getErroneousExports();
}
