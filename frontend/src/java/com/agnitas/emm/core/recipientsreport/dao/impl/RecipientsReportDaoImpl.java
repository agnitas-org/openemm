/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;

public class RecipientsReportDaoImpl extends PaginatedBaseDaoImpl implements RecipientsReportDao {

	/** The logger. */
    private static final transient Logger logger = LogManager.getLogger(RecipientsReportDaoImpl.class);

    private static final ReportRowsMapper REPORT_ROWS_MAPPER = new ReportRowsMapper();

    private static final Map<String, String> SORTABLE_COLUMNS;

    private static final String DEFAULT_SORTABLE_COLUMN = "report_date";

    static {
        SORTABLE_COLUMNS = new CaseInsensitiveMap<>();
        SORTABLE_COLUMNS.put("report_date", "report_date");
        SORTABLE_COLUMNS.put("type", "type");
        SORTABLE_COLUMNS.put("filename", "filename");
        SORTABLE_COLUMNS.put("datasource_id", "datasource_id");
        SORTABLE_COLUMNS.put("username", "username");
    }

    @Override
    public void createReport(int companyId, RecipientsReport report, String fileContent) {
		if (report.getReportDate() == null) {
			report.setReportDate(new Date());
        }
		
        if (isOracleDB()) {
            int reportId = selectInt(logger, "SELECT recipients_report_tbl_seq.NEXTVAL FROM DUAL");
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, autoimport_id, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(logger, sql,
                reportId,
                report.getReportDate(),
                report.getFilename(),
                report.getDatasourceId(),
                report.getAdminId(),
                report.getType().name(),
                companyId,
                AgnUtils.normalizeTextLineBreaks(fileContent),
                report.getFileId(),
                report.getAutoImportID(),
                report.isError() ? 1 : 0);
            report.setId(reportId);
        } else {
        	String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, autoimport_id, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int newId = insertIntoAutoincrementMysqlTable(logger, "recipients_report_id", sql,
                report.getReportDate(),
                report.getFilename(),
                report.getDatasourceId(),
                report.getAdminId(),
                report.getType().name(),
                companyId,
                AgnUtils.normalizeTextLineBreaks(fileContent),
                report.getFileId(),
                report.getAutoImportID(),
                report.isError() ? 1 : 0);
            report.setId(newId);
        }
    }

    @Override
    public void createSupplementalReportData(int companyId, RecipientsReport report, File temporaryDataFile, String textContent) throws Exception {
        if (isOracleDB()) {
            int reportId = selectInt(logger, "SELECT recipients_report_tbl_seq.NEXTVAL FROM DUAL");
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, autoimport_id, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(logger, sql,
                reportId,
                report.getReportDate(),
                report.getFilename(),
                report.getDatasourceId(),
                report.getAdminId(),
                report.getType().name(),
                companyId,
                textContent,
                report.getFileId(),
                report.getAutoImportID(),
                report.isError() ? 1 : 0);
            report.setId(reportId);
        } else {
        	String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, autoimport_id, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int newId = insertIntoAutoincrementMysqlTable(logger, "recipients_report_id", sql,
                report.getReportDate(),
                report.getFilename(),
                report.getDatasourceId(),
                report.getAdminId(),
                report.getType().name(),
                companyId,
                textContent,
                report.getFileId(),
                report.getAutoImportID(),
                report.isError() ? 1 : 0);
            report.setId(newId);
        }
        try (FileInputStream inputStream = new FileInputStream(temporaryDataFile)) {
        	updateBlob(logger, "UPDATE recipients_report_tbl SET content = ? WHERE recipients_report_id = ?", inputStream, report.getId());
        }
    }

    @Override
    public String getReportTextContent(int companyId, int reportId) {
        if (companyId > 0 && reportId > 0) {
            String sql = "SELECT report FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
            Map<String, Object> result = selectSingleRow(logger, sql, companyId, reportId);
            return (String) result.get("report");
        } else {
            logger.error("RecipientsReportDaoImpl: getReportFileContent failed.");
            logger.error("companyID: " + companyId);
            logger.error("reportID: " + reportId);
            return null;
        }
    }

    @Override
    public byte[] getReportFileData(int companyId, int reportId) throws Exception {
        if (companyId > 0 && reportId > 0) {
            String sql = "SELECT content FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
            Map<String, Object> result = selectSingleRow(logger, sql, companyId, reportId);
	
            return (byte[]) result.get("content");
        } else {
            logger.error("RecipientsReportDaoImpl: getReportFileContent failed.");
            logger.error("companyID: " + companyId);
            logger.error("reportID: " + reportId);
            return null;
        }
    }
    
    @Override
    public RecipientsReport.RecipientReportType getReportType(int companyId, int reportId) {
        String typeValue = selectObjectDefaultNull(logger,
                "SELECT type FROM recipients_report_tbl ir INNER JOIN admin_tbl a ON a.admin_id = ir.admin_id "
                	+ "WHERE ir.company_id = ? AND ir.recipients_report_id = ?",
                StringRowMapper.INSTANCE, companyId, reportId);
        return typeValue != null ? RecipientsReport.RecipientReportType.valueOf(typeValue) : null;
    }
    
    @Override
    public PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types) {
        sortProperty = SORTABLE_COLUMNS.getOrDefault(sortProperty, DEFAULT_SORTABLE_COLUMN);
        boolean direction = "ASC".equalsIgnoreCase(dir);
        String sql;
        List<Object> parameters = new ArrayList<>();
        
        if (types == null || types.length == 0) {
            sql = "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.type, ir.download_id, ir.autoimport_id, ir.error, a.username"
        		+ " FROM recipients_report_tbl ir INNER JOIN admin_tbl a ON a.admin_id = ir.admin_id"
        		+ " WHERE ir.company_id = ?";
        } else {
            sql = "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.type, ir.download_id, ir.autoimport_id, ir.error, a.username"
        		+ " FROM recipients_report_tbl ir INNER JOIN admin_tbl a ON a.admin_id = ir.admin_id"
        		+ " WHERE ir.company_id = ? AND type IN " + DbUtilities.joinForIN(types, RecipientsReport.RecipientReportType::name);
        }
        String dateClause = DbUtilities.getDateConstraint("REPORT_DATE", startDate, finishDate, isOracleDB());
        if (StringUtils.isNotBlank(dateClause)) {
            sql += " AND " + dateClause;
        }
        parameters.add(companyId);
        return selectPaginatedList(logger, sql, "recipients_report_tbl", sortProperty, direction, pageNumber, pageSize, REPORT_ROWS_MAPPER, parameters.toArray());
    }

    @Override
    public RecipientsReport getReport(int companyId, int reportId) {
        return selectObjectDefaultNull(logger, "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.type, ir.download_id, ir.autoimport_id, ir.error, a.username"
        	+ " FROM recipients_report_tbl ir INNER JOIN admin_tbl a ON a.admin_id = ir.admin_id WHERE ir.company_id = ? AND ir.recipients_report_id = ?",
        	REPORT_ROWS_MAPPER, companyId, reportId);
    }

    @Override
    public int deleteOldReports(int companyId, Date oldestReportDate) {
        String sql = "DELETE FROM recipients_report_tbl WHERE report_date < ? AND company_id = ?";
        return update(logger, sql, oldestReportDate, companyId);
    }
    
    @Override
    public boolean deleteReportsByCompany(int companyId) {
        update(logger, "DELETE FROM recipients_report_tbl WHERE company_id = ?", companyId);
        return selectInt(logger, "SELECT COUNT(*) FROM recipients_report_tbl WHERE company_id = ?", companyId) == 0;
    }

    public static class ReportRowsMapper implements RowMapper<RecipientsReport> {
        @Override
        public RecipientsReport mapRow(ResultSet resultSet, int i) throws SQLException {
            RecipientsReport report = new RecipientsReport();

            report.setId(resultSet.getInt("recipients_report_id"));
            report.setReportDate(resultSet.getTimestamp("report_date"));
            report.setFilename(resultSet.getString("filename"));
            report.setDatasourceId(resultSet.getObject("datasource_id") == null ? null : resultSet.getInt("datasource_id"));
            report.setAdminId(resultSet.getInt("admin_id"));
            report.setUsername(resultSet.getString("username"));
            report.setType(RecipientsReport.RecipientReportType.valueOf(resultSet.getString("type")));
            report.setFileId(resultSet.getObject("download_id") == null ? null : resultSet.getInt("download_id"));
            report.setAutoImportID(resultSet.getObject("autoimport_id") == null ? -1 : resultSet.getInt("autoimport_id"));
            report.setIsError(resultSet.getObject("error") != null && resultSet.getInt("error") > 0);

            return report;
        }
    }
}

