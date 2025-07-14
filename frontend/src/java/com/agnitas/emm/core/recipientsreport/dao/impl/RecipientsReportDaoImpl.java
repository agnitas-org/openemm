/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class RecipientsReportDaoImpl extends PaginatedBaseDaoImpl implements RecipientsReportDao {

    private static final ReportRowsMapper REPORT_ROWS_MAPPER = new ReportRowsMapper();

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private static final Map<String, String> SORTABLE_COLUMNS = new CaseInsensitiveMap<>(Map.of(
            "report_date", "report_date",
            "type", "type",
            "filename", "filename",
            "datasource_id", "datasource_id",
            "username", "username"
    ));

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

        // TODO: EMMGUI-714: remove when removing old design if not backward compatibilities problems will be found
        if (report.getEntityType() == RecipientsReport.EntityType.IMPORT) {
            params.add(RecipientsReport.RecipientReportType.IMPORT_REPORT.name());
        } else if (report.getEntityType() == RecipientsReport.EntityType.EXPORT) {
            params.add(RecipientsReport.RecipientReportType.EXPORT_REPORT.name());
        } else {
            throw new IllegalStateException("Can't create new report with type " + report.getEntityType().getId());
        }

        if (isOracleDB()) {
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(sql, params.toArray());
        } else {
            String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            reportId = insertIntoAutoincrementMysqlTable("recipients_report_id", sql, params.toArray());
        }

        report.setId(reportId);
    }

    @Override
    public void createNewSupplementalReport(int companyId, RecipientsReport report, File temporaryDataFile, String textContent) throws Exception {
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
        params.add(RecipientsReport.RecipientReportType.IMPORT_REPORT.name());
        params.add(companyId);
        params.add(textContent);
        params.add(report.getFileId());
        params.add(report.isError() ? 1 : 0);
        params.add(report.getEntityType().getId());
        params.add(report.getEntityExecution().getId());
        params.add(report.getEntityData().getId());
        params.add(report.getEntityId());

        if (isOracleDB()) {
            String sql = "INSERT INTO recipients_report_tbl (recipients_report_id, report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            update(sql, params.toArray());
        } else {
            String sql = "INSERT INTO recipients_report_tbl (report_date, filename, datasource_id, admin_id, type, company_id, report, download_id, error, entity_type, entity_execution, entity_data, entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            reportId = insertIntoAutoincrementMysqlTable("recipients_report_id", sql, params.toArray());
        }

        report.setId(reportId);

        try (FileInputStream inputStream = new FileInputStream(temporaryDataFile)) {
            updateBlob("UPDATE recipients_report_tbl SET content = ? WHERE recipients_report_id = ?", inputStream, report.getId());
        }
    }

    @Override
    public String getReportTextContent(int companyId, int reportId) {
        if (companyId > 0 && reportId > 0) {
            String sql = "SELECT report FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
            Map<String, Object> result = selectSingleRow(sql, companyId, reportId);
            return (String) result.get("report");
        } else {
            logger.error("RecipientsReportDaoImpl: getReportFileContent failed. companyID: {}, reportID: {}", companyId, reportId);
            return null;
        }
    }

    @Override
    public byte[] getReportFileData(int companyId, int reportId) {
        if (companyId > 0 && reportId > 0) {
            String sql = "SELECT content FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?";
            Map<String, Object> result = selectSingleRow(sql, companyId, reportId);
	
            return (byte[]) result.get("content");
        } else {
            logger.error("RecipientsReportDaoImpl: getReportFileContent failed. companyID: {}, reportID: {}", companyId, reportId);
            return null;
        }
    }
    
    @Override
    public RecipientsReport.EntityType getReportType(int companyId, int reportId) {
        RecipientsReport.EntityType entityType = IntEnum.fromId(RecipientsReport.EntityType.class, selectInt(
                "SELECT entity_type FROM recipients_report_tbl WHERE company_id = ? AND recipients_report_id = ?",
                companyId, reportId));
        if (entityType != null && entityType != RecipientsReport.EntityType.UNKNOWN) {
            return entityType;
        }
        String typeValue = selectObjectDefaultNull(
                "SELECT type FROM recipients_report_tbl ir WHERE ir.company_id = ? AND ir.recipients_report_id = ?",
                StringRowMapper.INSTANCE, companyId, reportId);
        
        // TODO: remove in future after removing of 'type' column. for backward compatibility only
        if (RecipientsReport.RecipientReportType.EXPORT_REPORT.name().equalsIgnoreCase(typeValue)) {
            return RecipientsReport.EntityType.EXPORT;
        }
        if (RecipientsReport.RecipientReportType.IMPORT_REPORT.name().equalsIgnoreCase(typeValue)) {
            return RecipientsReport.EntityType.IMPORT;
        }
        return RecipientsReport.EntityType.UNKNOWN;
    }

    @Override
    public PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types) {
        sortProperty = SORTABLE_COLUMNS.getOrDefault(sortProperty, "report_date");
        boolean direction = "ASC".equalsIgnoreCase(dir);
        List<Object> parameters = new ArrayList<>();
        
        String sql = "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.type, ir.download_id, ir.error, NVL(a.username, 'AutoImport / AutoExport') AS username"
    		+ " FROM recipients_report_tbl ir LEFT JOIN admin_tbl a ON a.admin_id = ir.admin_id"
    		+ " WHERE ir.company_id = ?";        

        if (types != null && types.length > 0) {
        	sql += " AND type IN " + DbUtilities.joinForIN(types, RecipientsReport.RecipientReportType::name);
        }
        
        String dateClause = DbUtilities.getDateConstraint("REPORT_DATE", startDate, finishDate, isOracleDB());
        if (StringUtils.isNotBlank(dateClause)) {
            sql += " AND " + dateClause;
        }
        parameters.add(companyId);
        return selectPaginatedList(sql, "recipients_report_tbl", sortProperty, direction, pageNumber, pageSize, REPORT_ROWS_MAPPER, parameters.toArray());
    }
    
    @Override
    public PaginatedListImpl<RecipientsReport> getReports(RecipientsReportForm filter, int companyId) {
        String sql = "SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.entity_data," +
                " ir.admin_id, ir.entity_type, ir.download_id, ir.error," +
                " NVL(a.username, 'AutoImport / AutoExport') AS username" +
                " FROM recipients_report_tbl ir LEFT JOIN admin_tbl a ON a.admin_id = ir.admin_id" +
                " WHERE ir.company_id = ?";
        List<Object> params = new ArrayList<>(List.of(companyId));
        sql += applyOverviewFilters(filter, params);

        PaginatedListImpl<RecipientsReport> list = selectPaginatedList(sql, "recipients_report_tbl",
                filter.getSortOrDefault("report_date"), filter.ascending(),
                filter.getPage(), filter.getNumberOfRows(), REPORT_ROWS_MAPPER, params.toArray());

        if (filter.isUiFiltersSet()) {
            int unfilteredTotalCount = selectIntWithDefaultValue(
                    "SELECT COUNT(*) FROM recipients_report_tbl WHERE company_id = ?",
                    0,
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

        String dateClause = DbUtilities.getDateConstraint("REPORT_DATE", filter.getReportDate().getFrom(), filter.getReportDate().getTo(), isOracleDB());
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
        return selectObjectDefaultNull("SELECT ir.recipients_report_id, ir.report_date, ir.filename, ir.datasource_id, ir.admin_id, ir.type, ir.entity_type, ir.download_id, ir.error, NVL(a.username, 'AutoImport / AutoExport') AS username"
        	+ " FROM recipients_report_tbl ir LEFT JOIN admin_tbl a ON a.admin_id = ir.admin_id WHERE ir.company_id = ? AND ir.recipients_report_id = ?",
        	REPORT_ROWS_MAPPER, companyId, reportId);
    }

    @Override
    public int deleteOldReports(int companyId, Date oldestReportDate) {
        String sql = "DELETE FROM recipients_report_tbl WHERE report_date < ? AND company_id = ?";
        return update(sql, oldestReportDate, companyId);
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
            if (DbUtilities.resultsetHasColumn(resultSet, "type")) {
                report.setType(RecipientsReport.RecipientReportType.valueOf(resultSet.getString("type")));
            }

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

