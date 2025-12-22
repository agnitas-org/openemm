/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.IntEnum;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.BlobRowMapper;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class RecipientsReportDaoImpl extends PaginatedBaseDaoImpl implements RecipientsReportDao {

    private static final ReportRowsMapper REPORT_ROWS_MAPPER = new ReportRowsMapper();

    @Override
    public List<DashboardRecipientReport> getReportsForDashboard(int companyId) {
        String subQuery = addRowLimit("SELECT recipients_report_id, entity_type, entity_execution, error, entity_id, entity_data, report_date, company_id" +
                " FROM recipients_report_tbl WHERE company_id = ? AND entity_id IS NOT NULL ORDER BY report_date DESC", 10);

        return select(" SELECT r.*," +
                "   CASE WHEN r.entity_type = 1 AND r.entity_execution = 1 THEN i.shortname" +
                "        WHEN r.entity_type = 2 AND r.entity_execution = 1 THEN e.shortname" +
                getExtendedNameCasesForDashboardReports() +
                "   END AS report_name" +
                " FROM (" + subQuery + ") r " +
                "   LEFT JOIN export_predef_tbl e ON e.export_predef_id = r.entity_id AND e.company_id = r.company_id" +
                "   LEFT JOIN import_profile_tbl i ON i.id = r.entity_id AND i.company_id = r.company_id" +
                getExtendedJoinsForDashboardReports(), new DashboardReportRowMapper(), companyId);
    }

    protected String getExtendedJoinsForDashboardReports() {
       return ""; // overridden in extended class
    }

    protected String getExtendedNameCasesForDashboardReports() {
        return ""; // overridden in extended class
    }

    @Override
    public void createNewReport(int companyId, RecipientsReport report, String fileContent) {
        if (report.getReportDate() == null) {
            report.setReportDate(new Date());
        }

        int reportId = 0;
        List<Object> params = new ArrayList<>();
        if (isOracleDB()) {
            reportId = selectInt("SELECT recipients_report_tbl_seq.NEXTVAL FROM DUAL");
            params.add(reportId);
        }

        params.add(report.getReportDate());
        params.add(report.getFilename());
        params.add(report.getDatasourceId());
        params.add(report.getAdminId());
        params.add(companyId);
        params.add(AgnUtils.normalizeTextLineBreaks(fileContent));
        params.add(report.getFileId());
        params.add(report.isError() ? 1 : 0);
        params.add(report.getEntityType().getId());
        params.add(report.getEntityExecution().getId());
        params.add(report.getEntityData().getId());
        params.add(report.getEntityId());

        if (isOracleDB()) {
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(sql, params.toArray());
        } else {
            String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            reportId = insert("recipients_report_id", sql, params.toArray());
        }

        report.setId(reportId);
    }

    @Override
    public void createNewSupplementalReport(int companyId, RecipientsReport report, byte[] content, String textContent) {
        int reportId = 0;
        List<Object> params = new ArrayList<>();
        if (isOracleDB()) {
            reportId = selectInt("SELECT recipients_report_tbl_seq.NEXTVAL FROM DUAL");
            params.add(reportId);
        }

        params.add(report.getReportDate());
        params.add(report.getFilename());
        params.add(report.getDatasourceId());
        params.add(report.getAdminId());
        params.add(companyId);
        params.add(textContent);
        params.add(report.getFileId());
        params.add(report.isError() ? 1 : 0);
        params.add(report.getEntityType().getId());
        params.add(report.getEntityExecution().getId());
        params.add(report.getEntityData().getId());
        params.add(report.getEntityId());

        if (isOracleDB()) {
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(sql, params.toArray());
        } else {
            String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            reportId = insert("recipients_report_id", sql, params.toArray());
        }

        report.setId(reportId);
        updateBlob("UPDATE recipients_report_tbl SET content = ? WHERE recipients_report_id = ?", content, report.getId());
    }

    @Override
    public String getReportTextContent(int companyId, int reportId) {
        String sql = "SELECT report FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
        return selectStringDefaultNull(sql, companyId, reportId);
    }

    @Override
    public byte[] getReportFileData(int companyId, int reportId) {
        String sql = "SELECT content FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
        return selectObjectDefaultNull(sql, BlobRowMapper.INSTANCE, companyId, reportId);
    }
    
    @Override
    public RecipientsReport.EntityType getReportType(int companyId, int reportId) {
        return IntEnum.fromId(RecipientsReport.EntityType.class, selectInt(
                "SELECT entity_type FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?",
                companyId, reportId));
    }

    @Override
    public PaginatedList<RecipientsReport> getReports(RecipientsReportForm filter, int companyId) {
        String sql = "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.entity_data," +
                " ir.admin_id, ir.entity_type, ir.download_id, ir.error," +
                " COALESCE(a.username, 'AutoImport / AutoExport') AS username" +
                " FROM recipients_report_tbl ir LEFT JOIN admin_tbl a ON a.admin_id = ir.admin_id" +
                " WHERE ir.company_id = ?";
        List<Object> params = new ArrayList<>(List.of(companyId));
        sql += applyOverviewFilters(filter, params);

        PaginatedList<RecipientsReport> list = selectPaginatedList(sql, "recipients_report_tbl",
                filter.getSortOrDefault("report_date"), filter.ascending(),
                filter.getPage(), filter.getNumberOfRows(), REPORT_ROWS_MAPPER, params.toArray());

        if (filter.isUiFiltersSet()) {
            int unfilteredTotalCount = selectInt(
                    "SELECT COUNT(*) FROM recipients_report_tbl WHERE company_id = ?",
                    companyId
            );

            list.setNotFilteredFullListSize(unfilteredTotalCount);
        }

        return list;
    }

    private String applyOverviewFilters(RecipientsReportForm filter, List<Object> params) {
        String filterSql = "";
        if (filter.getTypes() != null && filter.getTypes().length > 0) {
            filterSql += " AND entity_type IN (" + AgnUtils.csvQMark(filter.getTypes().length) + ")";
            params.addAll(Arrays.stream(filter.getTypes()).map(RecipientsReport.EntityType::getId).toList());
        }

        String dateClause = DbUtilities.getDateConstraint("REPORT_DATE", filter.getReportDate().getFrom(), filter.getReportDate().getTo(), isOracleDB() || isPostgreSQL());
        if (StringUtils.isNotBlank(dateClause)) {
            filterSql += " AND " + dateClause;
        }
        if (filter.getDatasourceId() != null) {
            filterSql += getPartialSearchFilterWithAnd("ir.datasource_id", filter.getDatasourceId(), params);
        }
        if (StringUtils.isNotBlank(filter.getFileName())) {
            filterSql += getPartialSearchFilterWithAnd("ir.filename", filter.getFileName(), params);
        }
        if (filter.getAdminId() > 0) {
            filterSql += " AND ir.admin_id = ?";
            params.add(filter.getAdminId());
        }
        return filterSql;
    }

    @Override
    public RecipientsReport getReport(int companyId, int reportId) {
        return selectObjectDefaultNull("SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.entity_type, ir.download_id, ir.error, COALESCE(a.username, 'AutoImport / AutoExport') AS username"
        	+ " FROM recipients_report_tbl ir LEFT JOIN admin_tbl a ON a.admin_id = ir.admin_id WHERE ir.company_id = ? AND ir.recipients_report_id = ?",
        	REPORT_ROWS_MAPPER, companyId, reportId);
    }

    @Override
    public boolean deleteReportsByCompany(int companyId) {
        update("DELETE FROM recipients_report_tbl WHERE company_id = ?", companyId);
        return selectInt("SELECT COUNT(*) FROM recipients_report_tbl WHERE company_id = ?", companyId) == 0;
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

            if (DbUtilities.resultsetHasColumn(resultSet, "entity_type")) {
                report.setEntityType(IntEnum.fromId(RecipientsReport.EntityType.class, resultSet.getInt("entity_type")));
            }

            report.setFileId(resultSet.getObject("download_id") == null ? null : resultSet.getInt("download_id"));
            report.setIsError(resultSet.getObject("error") != null && resultSet.getInt("error") > 0);

            if (DbUtilities.resultsetHasColumn(resultSet, "entity_data")) {
                report.setEntityData(IntEnum.fromId(RecipientsReport.EntityData.class, resultSet.getInt("entity_data")));
            }

            return report;
        }
    }

    public static class DashboardReportRowMapper implements RowMapper<DashboardRecipientReport> {
        @Override
        public DashboardRecipientReport mapRow(ResultSet resultSet, int i) throws SQLException {
            DashboardRecipientReport report = new DashboardRecipientReport();

            report.setId(resultSet.getInt("recipients_report_id"));
            report.setType(DashboardRecipientReport.Type.detect(resultSet.getInt("entity_type"), resultSet.getInt("entity_execution")));
            report.setName(resultSet.getString("report_name"));
            report.setSuccessful(!BooleanUtils.toBoolean(resultSet.getInt("error")));
            report.setLastExecutionDate(resultSet.getTimestamp("report_date"));

            return report;
        }
    }
}

