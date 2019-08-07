/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.download.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface DownloadDao {
	int createFile(InputStream inputStream) throws Exception;

	void writeContentToStream(int downloadId, OutputStream outputStream) throws Exception;

	void writeContentOfExportReportToStream(int companyId, int reportId, OutputStream outputStream) throws Exception;
	
    boolean deleteContent(int downloadId);

    int deleteAllContentOfOldExportReports(@VelocityCheck int companyId, Date oldestReportDate);
}
