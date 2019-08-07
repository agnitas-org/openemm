/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.download.dao.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.download.dao.DownloadDao;
import com.agnitas.emm.core.recipientsreport.dao.impl.RecipientsReportDaoImpl;

public class DownloadDaoImpl extends BaseDaoImpl implements DownloadDao {

    private static final transient Logger logger = Logger.getLogger(RecipientsReportDaoImpl.class);

    @Override
	public int createFile(InputStream inputStream) throws Exception {
    	int newDownloadID;
		if (isOracleDB()) {
			newDownloadID = selectInt(logger, "SELECT download_tbl_seq.NEXTVAL FROM DUAL");
			if (update(logger, "INSERT INTO download_tbl (download_id, content) VALUES (?, EMPTY_BLOB())", newDownloadID) != 1) {
				throw new RuntimeException("Illegal insert result");
			}
		} else {
			newDownloadID = insertIntoAutoincrementMysqlTable(logger, "download_id", "INSERT INTO download_tbl (content) VALUES ('')");
		}
		updateBlob(logger, "UPDATE download_tbl SET content = ? WHERE download_id = ?", inputStream, newDownloadID);
		return newDownloadID;
	}

    @Override
    public void writeContentToStream(int downloadId, OutputStream outputStream) throws Exception {
        if (downloadId > 0) {
            writeBlobInStream(logger, "SELECT content FROM download_tbl where download_id = ?", outputStream, downloadId);
        } else {
        	throw new Exception("writeContentToStream failed for downloadId: " + downloadId);
        }
    }

    @Override
    public void writeContentOfExportReportToStream(int companyId, int reportId, OutputStream outputStream) throws Exception {
        if (reportId > 0) {
        	writeBlobInStream(logger, "SELECT d.content FROM download_tbl d INNER JOIN recipients_report_tbl r ON r.download_id = d.download_id WHERE r.recipients_report_id = ? AND r.company_id = ?", outputStream, reportId, companyId);
        } else {
        	throw new Exception("writeContentToStream failed for reportId: " + companyId + "/" + reportId);
        }
    }

    @Override
    public boolean deleteContent(int downloadId){
        String sql = "DELETE FROM download_tbl WHERE download_id = ?";
        return update(logger, sql, downloadId) > 0;
    }

    @Override
    public int deleteAllContentOfOldExportReports(@VelocityCheck int companyId, Date oldestReportDate){
        String sql = "DELETE FROM download_tbl WHERE download_id in (SELECT download_id FROM recipients_report_tbl WHERE report_date < ? and company_id = ?)";
        return update(logger, sql, oldestReportDate, companyId);
    }
}
