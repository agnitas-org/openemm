/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao.impl;

import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.bean.impl.MailingRecipientStatRowImpl;
import com.agnitas.emm.core.mailing.dao.MailingRecipientsDao;
import com.agnitas.emm.core.mailing.enums.MailingRecipientType;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsOverviewFilter;
import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MailingRecipientsDaoImpl extends PaginatedBaseDaoImpl implements MailingRecipientsDao {

    private static final Logger logger = LogManager.getLogger(MailingRecipientsDaoImpl.class);

    @Override
    public PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(MailingRecipientsOverviewFilter filter, Set<String> recipientsFields, int maxCompanyRecipients,
                                                                           int mailingId, int companyId) throws Exception {
        int pageNumber = filter.getPage();
        int pageSize = filter.getNumberOfRows();
        Set<String> columns = getColumnsForSelect(recipientsFields);

        final int mailingListId = selectInt(logger, "SELECT mailinglist_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyId, mailingId);
        final List<Object> params = new ArrayList<>();

        // Keep the order of requested columns
        int totalRows = getNumberOfMailingRecipients(companyId, filter, mailingId, mailingListId, columns);
        String selectSql;
        if (isRecipientsNumberExceedsLimit(totalRows, maxCompanyRecipients)) {
            // if the maximum number of recipients to show is exceeded
            // only the first page of unsorted recipients is shown to discharge the database and its performance
            // BTW: another sql statement will be executed that was optimized
            pageNumber = 1;
            selectSql = getMailingRecipientsQueryWithoutSorting(companyId, mailingId, mailingListId, filter, columns, pageSize, params);
        } else {
            SqlPreparedStatementManager sqlStatement = prepareSqlStatement(filter, recipientsFields, mailingId, companyId);
            selectSql = sqlStatement.getPreparedSqlString();
            params.addAll(List.of(sqlStatement.getPreparedSqlParameters()));

            pageNumber = AgnUtils.getValidPageNumber(totalRows, pageNumber, pageSize);
            int offset = pageNumber * pageSize;

            if (isOracleDB()) {
                selectSql = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + selectSql + ") selection) WHERE r BETWEEN ? AND ?";
                params.addAll(List.of((offset - pageSize + 1), offset));
            } else {
                selectSql = selectSql + " LIMIT ?, ?";
                params.addAll(List.of((offset - pageSize), pageSize));
            }
        }

        List<String> selectedColumns = new ArrayList<>(columns);
        selectedColumns.addAll(List.of(
                "customer_id", "receive_time", "open_time", "openings", "click_time", "clicks", "bounce_time", "optout_time"
        ));

        List<MailingRecipientStatRow> recipients = select(logger, selectSql, new MailingRecipientStatRow_RowMapper(companyId, selectedColumns), params.toArray());
        PaginatedListImpl<MailingRecipientStatRow> list = new PaginatedListImpl<>(recipients, totalRows, pageSize, pageNumber, "", filter.ascending());

        if (filter.isUiFiltersSet()) {
            list.setNotFilteredFullListSize(getNumberOfMailingRecipients(companyId, null, mailingId, mailingListId, columns));
        }

        return list;
    }

    private Set<String> getColumnsForSelect(Set<String> recipientsFields) {
        Set<String> columns = new LinkedHashSet<>(recipientsFields);
        columns.remove("title"); // to change the order of columns in the export file
        columns.addAll(List.of("title", "firstname", "lastname", "email"));

        return columns;
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatement(MailingRecipientsOverviewFilter filter, Set<String> recipientsFields, int mailingId, int companyId) {
        final List<Object> params = new ArrayList<>();
        final int mailingListId = selectInt(logger, "SELECT mailinglist_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyId, mailingId);
        final Set<String> columns = getColumnsForSelect(recipientsFields);

        String selectSql =
                "SELECT cust.customer_id," + joinWithPrefixes(columns, "cust.") + ","
                        + " MAX(succ.timestamp) AS receive_time,"
                        + " MIN(opl.first_open) AS open_time,"
                        + " COALESCE(MAX(opl.open_count), 0) AS openings,"
                        + " MIN(rlog.timestamp) AS click_time,"
                        + " COUNT(DISTINCT rlog.timestamp) AS clicks,"
                        + " MAX(bind1.timestamp) AS bounce_time,"
                        + " MAX(bind2.timestamp) AS optout_time"
                        + " FROM customer_" + companyId + "_tbl cust"
                        + " JOIN mailtrack_" + companyId + "_tbl track ON track.customer_id = cust.customer_id AND track.mailing_id = ?"
                        + " LEFT OUTER JOIN success_" + companyId + "_tbl succ ON succ.customer_id = cust.customer_id AND succ.mailing_id = ?"
                        + " LEFT OUTER JOIN onepixellog_" + companyId + "_tbl opl ON opl.customer_id = cust.customer_id AND opl.mailing_id = ?"
                        + " LEFT OUTER JOIN rdirlog_" + companyId + "_tbl rlog ON rlog.customer_id = cust.customer_id AND rlog.mailing_id = ?"
                        + " LEFT OUTER JOIN customer_" + companyId + "_binding_tbl bind1 ON bind1.customer_id = cust.customer_id AND bind1.exit_mailing_id = ? AND bind1.user_status = ? AND bind1.user_type NOT IN (?, ?, ?)"
                        + " LEFT OUTER JOIN customer_" + companyId + "_binding_tbl bind2 ON bind2.customer_id = cust.customer_id AND bind2.exit_mailing_id = ? AND bind2.user_status IN (?, ?) AND bind2.user_type IN (?, ?)"
                        + " WHERE EXISTS"
                        + " (SELECT 1 FROM customer_" + companyId + "_binding_tbl bind WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? AND bind.user_type NOT IN (?, ?, ?))"
                        + " GROUP BY cust.customer_id, " + joinWithPrefixes(columns, "cust.");

        selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel ");

        params.addAll(List.of(
                mailingId,
                mailingId,
                mailingId,
                mailingId,
                mailingId,
                UserStatus.Bounce.getStatusCode(),
                BindingEntry.UserType.Admin.getTypeCode(),
                BindingEntry.UserType.TestUser.getTypeCode(),
                BindingEntry.UserType.TestVIP.getTypeCode(),
                mailingId,
                UserStatus.UserOut.getStatusCode(),
                UserStatus.AdminOut.getStatusCode(),
                BindingEntry.UserType.World.getTypeCode(),
                BindingEntry.UserType.WorldVIP.getTypeCode(),
                mailingListId,
                BindingEntry.UserType.Admin.getTypeCode(),
                BindingEntry.UserType.TestUser.getTypeCode(),
                BindingEntry.UserType.TestVIP.getTypeCode()
        ));

        selectSql += applyOverviewFilters(filter, params, false);
        selectSql += " " + getSortClause(filter);

        return new SqlPreparedStatementManager(selectSql, params.toArray());
    }

    private String applyOverviewFilters(MailingRecipientsOverviewFilter filter, List<Object> params, boolean excludeTypeFilter) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");

        if (!excludeTypeFilter && CollectionUtils.isNotEmpty(filter.getTypes()) && filter.getTypes().size() != MailingRecipientType.values().length) {
            String typesConditions = filter.getTypes()
                    .stream()
                    .map(t -> getSqlConditionByMailingRecipientType(t))
                    .collect(Collectors.joining(" AND "));

            whereClause.append(" AND ").append(typesConditions);
        }

        if (StringUtils.isNotBlank(filter.getFirstname())) {
            whereClause.append(getPartialSearchFilterWithAnd("firstname"));
            params.add(filter.getFirstname());
        }

        if (StringUtils.isNotBlank(filter.getLastname())) {
            whereClause.append(getPartialSearchFilterWithAnd("lastname"));
            params.add(filter.getLastname());
        }

        if (StringUtils.isNotBlank(filter.getEmail())) {
            whereClause.append(getPartialSearchFilterWithAnd("email"));
            params.add(filter.getEmail());
        }

        return whereClause.toString();
    }

    private String getSortClause(MailingRecipientsOverviewFilter filter) {
        final String sortCriterion = filter.getSortOrDefault("receive_time");
        final boolean sortAscending = filter.ascending();

        String sortClause = "ORDER BY ";
        if (isOracleDB()) {
            sortClause += sortCriterion + " " + (sortAscending ? "ASC" : "DESC") + " NULLS LAST";
        } else {
            // Force MySQL sort null values the same way that Oracle does
            sortClause += "ISNULL(" + sortCriterion + "), " + sortCriterion + " " + (sortAscending ? "ASC" : "DESC");
        }
        return sortClause + ", customer_id " + (sortAscending ? "ASC" : "DESC");
    }

    private int getNumberOfMailingRecipients(int companyId, MailingRecipientsOverviewFilter filter, int mailingId, int mailinglistId, Collection<String> columns) {
        List<Object> params = new ArrayList<>();
        params.add(mailingId);

        String subSel = "SELECT cust.customer_id, " + joinWithPrefixes(columns, "cust.") +
                " FROM customer_" + companyId + "_tbl cust " +
                "         JOIN mailtrack_" + companyId + "_tbl track ON track.customer_id = cust.customer_id AND track.mailing_id = ? ";

        if (filter != null) {
            subSel += createJoinStatementWithMailingRecipientsFiltering(filter.getTypes(), params, companyId, mailingId);
        }

        subSel += " WHERE EXISTS(SELECT 1 FROM customer_" + companyId + "_binding_tbl bind " +
                " WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? " +
                " AND bind.user_type NOT IN (?, ?, ?)) " +
                " GROUP BY cust.customer_id, " + joinWithPrefixes(columns, "cust.");

        params.add(mailinglistId);
        params.add(BindingEntry.UserType.Admin.getTypeCode());
        params.add(BindingEntry.UserType.TestUser.getTypeCode());
        params.add(BindingEntry.UserType.TestVIP.getTypeCode());

        String filterConditions = filter == null ? "" : applyOverviewFilters(filter, params, true);
        return selectInt(logger, String.format("SELECT COUNT(*) FROM (%s) sel %s", subSel, filterConditions), params.toArray());
    }

    private String createJoinStatementWithMailingRecipientsFiltering(List<MailingRecipientType> types, List<Object> params, int companyId, int mailingId) {
        if (CollectionUtils.isEmpty(types) || types.size() == MailingRecipientType.values().length) {
            return "";
        }

        StringBuilder joinStatement = new StringBuilder();

        for (MailingRecipientType type : types) {
            switch (type) {
                case OPENED:
                    joinStatement.append(" JOIN onepixellog_" + companyId + "_tbl opl ON opl.customer_id = cust.customer_id AND opl.mailing_id = ? ");
                    params.add(mailingId);
                    break;

                case CLICKED:
                    joinStatement.append(" JOIN rdirlog_" + companyId + "_tbl rlog ON rlog.customer_id = cust.customer_id AND rlog.mailing_id = ? ");
                    params.add(mailingId);
                    break;

                case BOUNCED:
                    joinStatement.append(" JOIN customer_" + companyId + "_binding_tbl bind1 ON bind1.customer_id = cust.customer_id AND bind1.exit_mailing_id = ? " +
                            "AND bind1.user_status = ? AND bind1.user_type NOT IN (?, ?, ?)");
                    params.add(mailingId);
                    params.add(UserStatus.Bounce.getStatusCode());
                    params.add(BindingEntry.UserType.Admin.getTypeCode());
                    params.add(BindingEntry.UserType.TestUser.getTypeCode());
                    params.add(BindingEntry.UserType.TestVIP.getTypeCode());
                    break;

                case UNSUBSCRIBED:
                    joinStatement.append(" JOIN customer_" + companyId + "_binding_tbl bind2 ON bind2.customer_id = cust.customer_id AND bind2.exit_mailing_id = ? " +
                            "AND bind2.user_status IN (?, ?) AND bind2.user_type IN (?, ?)");
                    params.add(mailingId);
                    params.add(UserStatus.UserOut.getStatusCode());
                    params.add(UserStatus.AdminOut.getStatusCode());
                    params.add(BindingEntry.UserType.World.getTypeCode());
                    params.add(BindingEntry.UserType.WorldVIP.getTypeCode());
                    break;

                default:
                    // filter nothing
            }
        }

        return joinStatement.toString();
    }

    private String getMailingRecipientsQueryWithoutSorting(int companyId, int mailingId, int mailingListId, MailingRecipientsOverviewFilter filter, Collection<String> columns, int pageSize, List<Object> params) {
        String sqlColumns = joinWithPrefixes(columns, "cust.");
        String s3Columns = joinWithPrefixes(columns, "s3.");

        String filteringQuery = "SELECT cust.customer_id, " + sqlColumns +
                " FROM customer_" + companyId + "_tbl cust " +
                "    JOIN mailtrack_" + companyId + "_tbl track " +
                "        ON track.customer_id = cust.customer_id AND track.mailing_id = ? " +
                "    JOIN customer_" + companyId + "_binding_tbl cb " +
                "        ON cust.customer_id = cb.customer_id AND cb.mailinglist_id = ? AND cb.user_type NOT IN (?, ?, ?) ";

        params.add(mailingId);
        params.add(mailingListId);
        params.add(BindingEntry.UserType.Admin.getTypeCode());
        params.add(BindingEntry.UserType.TestUser.getTypeCode());
        params.add(BindingEntry.UserType.TestVIP.getTypeCode());

        filteringQuery += createJoinStatementWithMailingRecipientsFiltering(filter.getTypes(), params, companyId, mailingId);
        filteringQuery += applyOverviewFilters(filter, params, true);
        filteringQuery += " GROUP BY cust.customer_id, " + sqlColumns +
                " ORDER BY cust.customer_id ";

        String limitedFilteredSelect;

        if (isOracleDB()) {
            String s1Columns = joinWithPrefixes(columns, "s1.");
            String s2Columns = joinWithPrefixes(columns, "s2.");

            String filteredRowNumSelect = String.format("SELECT s1.customer_id, %s, rownum AS r FROM (%s) s1", s1Columns, filteringQuery);
            limitedFilteredSelect = String.format("SELECT s2.customer_id, %s, s2.r FROM (%s) s2 WHERE s2.r BETWEEN 1 AND ?", s2Columns, filteredRowNumSelect);
        } else {
            limitedFilteredSelect = filteringQuery + " LIMIT 0, ?";
        }

        params.add(pageSize);

        String selectQuery = "SELECT s3.customer_id, %s," +
                "       MAX(succ.timestamp)              AS receive_time, " +
                "       MIN(opl.first_open)              AS open_time, " +
                "       COALESCE(MAX(opl.open_count), 0) AS openings, " +
                "       MIN(rlog.timestamp)              AS click_time, " +
                "       COUNT(DISTINCT rlog.timestamp)   AS clicks, " +
                "       MAX(b1.timestamp)                AS bounce_time, " +
                "       MAX(b2.timestamp)                AS optout_time " +
                "FROM (%s) s3 " +
                "     LEFT OUTER JOIN success_" + companyId + "_tbl succ " +
                "             ON succ.customer_id = s3.customer_id AND succ.mailing_id = ? " +
                "     LEFT OUTER JOIN onepixellog_" + companyId + "_tbl opl " +
                "             ON opl.customer_id = s3.customer_id AND opl.mailing_id = ? " +
                "     LEFT OUTER JOIN rdirlog_" + companyId + "_tbl rlog " +
                "             ON rlog.customer_id = s3.customer_id AND rlog.mailing_id = ? " +
                "     LEFT OUTER JOIN customer_" + companyId + "_binding_tbl b1 " +
                "             ON b1.customer_id = s3.customer_id AND b1.exit_mailing_id = ? AND " +
                "                     b1.user_status = ? AND b1.user_type NOT IN (?, ?, ?) " +
                "     LEFT OUTER JOIN customer_" + companyId + "_binding_tbl b2 " +
                "             ON b2.customer_id = s3.customer_id AND b2.exit_mailing_id = ? AND " +
                "                     b2.user_status IN (?, ?) AND b2.user_type IN (?, ?) " +
                "GROUP BY s3.customer_id, %s ";

        params.add(mailingId);
        params.add(mailingId);
        params.add(mailingId);
        params.add(mailingId);
        params.add(UserStatus.Bounce.getStatusCode());
        params.add(BindingEntry.UserType.Admin.getTypeCode());
        params.add(BindingEntry.UserType.TestUser.getTypeCode());
        params.add(BindingEntry.UserType.TestVIP.getTypeCode());

        params.add(mailingId);
        params.add(UserStatus.UserOut.getStatusCode());
        params.add(UserStatus.AdminOut.getStatusCode());
        params.add(BindingEntry.UserType.World.getTypeCode());
        params.add(BindingEntry.UserType.WorldVIP.getTypeCode());

        return String.format(selectQuery, s3Columns, limitedFilteredSelect, s3Columns);
    }

    private String getSqlConditionByMailingRecipientType(MailingRecipientType type) {
        switch (type) {
            case OPENED:
                return "open_time IS NOT NULL";

            case CLICKED:
                return "click_time IS NOT NULL";

            case BOUNCED:
                return "bounce_time IS NOT NULL";

            case UNSUBSCRIBED:
                return "optout_time IS NOT NULL";

            default:
                // filter nothing
        }

        return "1=1";
    }

    private boolean isRecipientsNumberExceedsLimit(int totalRows, int maxRecipients) {
        return maxRecipients > 0 && totalRows > maxRecipients;
    }

    private String joinWithPrefixes(Collection<String> strings, String prefix) {
        return strings.stream()
                .map(s -> prefix + s)
                .collect(Collectors.joining(", "));
    }

    private class MailingRecipientStatRow_RowMapper implements RowMapper<MailingRecipientStatRow> {
        private int companyId;
        private List<String> selectedColumns;

        public MailingRecipientStatRow_RowMapper(int companyId, List<String> selectedColumns) {
            this.companyId = companyId;
            this.selectedColumns = selectedColumns;
        }

        @Override
        public MailingRecipientStatRow mapRow(ResultSet resultSet, int row) throws SQLException {
            Recipient recipient = new RecipientImpl();
            recipient.setCompanyID(companyId);

            Map<String, Object> recipientValues = new HashMap<>();
            for (String column : selectedColumns) {
                Object value = resultSet.getObject(column);
                if (isOracleDB() && value != null && value.getClass().getName().equalsIgnoreCase("oracle.sql.TIMESTAMP")) {
                    recipientValues.put(column, resultSet.getTimestamp(column));
                } else {
                    recipientValues.put(column, value);
                }
            }
            recipient.setCustParameters(recipientValues);

            MailingRecipientStatRow mailingRecipientStatRow = new MailingRecipientStatRowImpl();
            mailingRecipientStatRow.setRecipient(recipient);
            return mailingRecipientStatRow;
        }
    }
}
