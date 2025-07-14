/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dao.impl;

import com.agnitas.emm.core.report.bean.CompositeBindingEntryHistory;
import com.agnitas.emm.core.report.bean.PlainBindingEntryHistory;
import com.agnitas.emm.core.report.bean.impl.CompositeBindingEntryHistoryImpl;
import com.agnitas.emm.core.report.bean.impl.PlainBindingEntryHistoryImpl;
import com.agnitas.emm.core.report.dao.BindingEntryHistoryDao;
import com.agnitas.beans.Mailinglist;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.MailinglistRowMapper;
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BindingEntryHistoryDaoImpl extends PaginatedBaseDaoImpl implements BindingEntryHistoryDao {

    @Override
    public List<PlainBindingEntryHistory> getHistory(int companyId, int recipientId, int mailinglistId, int mediaType) {
        String tableName = getRecipientBindingHistoryTableName(companyId);

        if (DbUtilities.checkIfTableExists(getDataSource(), tableName)) {
            StringBuilder query = new StringBuilder("SELECT * FROM ").append(tableName)
                    .append(" WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?")
                    .append(" ORDER BY timestamp_change ASC");

            return select(query.toString(), new PlainRowMapper(), recipientId, mailinglistId, mediaType);
        }

        return Collections.emptyList();
    }

    @Override
    public List<CompositeBindingEntryHistory> getHistoryOfNonexistentBindings(int companyId, int recipientId) {
        String recipientBindingHistoryTable = getRecipientBindingHistoryTableName(companyId);
        String recipientBindingTable = getRecipientBindingTableName(companyId);
        String recipientTable = getRecipientTableName(companyId);

        DataSource dataSource = getDataSource();
        boolean isBindingHistoryTableExisted = DbUtilities.checkIfTableExists(dataSource, recipientBindingHistoryTable);
        if (isBindingHistoryTableExisted) {

            StringBuilder bindingsQuery = new StringBuilder("SELECT 1 FROM ").append(recipientBindingTable).append(" bind");
            bindingsQuery.append(" WHERE bind.customer_id = hst.customer_id");
            bindingsQuery.append(" AND bind.mailinglist_id = hst.mailinglist_id");
            bindingsQuery.append(" AND bind.mediatype = hst.mediatype");

            StringBuilder nonexistentBindingsQuery = new StringBuilder("SELECT hst.customer_id, hst.mailinglist_id,");
            nonexistentBindingsQuery.append(" hst.mediatype, MAX(hst.timestamp_change) AS max_timestamp_change");
            nonexistentBindingsQuery.append(" FROM ").append(recipientBindingHistoryTable).append(" hst");
            nonexistentBindingsQuery.append(" WHERE NOT EXISTS (").append(bindingsQuery).append(")");
            nonexistentBindingsQuery.append(" AND hst.customer_id = ?");
            nonexistentBindingsQuery.append(" GROUP BY hst.customer_id, hst.mailinglist_id, hst.mediatype");

            StringBuilder historyOfNonexistentBindings = new StringBuilder("SELECT hst_out.*,");
            historyOfNonexistentBindings.append(" ml.mailinglist_id AS ml_mailinglist_id, ml.company_id AS ml_company_id,");
            historyOfNonexistentBindings.append(" ml.shortname AS ml_shortname, ml.description AS ml_description,");
            historyOfNonexistentBindings.append(" ml.change_date AS ml_change_date, ml.creation_date AS ml_creation_date,");
            historyOfNonexistentBindings.append(" ml.deleted AS ml_deleted");
            historyOfNonexistentBindings.append(" FROM ").append(recipientBindingHistoryTable).append(" hst_out");
            historyOfNonexistentBindings.append(" INNER JOIN (").append(nonexistentBindingsQuery).append(") hst_in");
            historyOfNonexistentBindings.append(" ON hst_out.customer_id = hst_in.customer_id");
            historyOfNonexistentBindings.append(" AND hst_out.mailinglist_id = hst_in.mailinglist_id");
            historyOfNonexistentBindings.append(" AND hst_out.mediatype = hst_in.mediatype");
            historyOfNonexistentBindings.append(" AND hst_out.timestamp_change = hst_in.max_timestamp_change");
            historyOfNonexistentBindings.append(" INNER JOIN ").append(recipientTable).append(" rec");
            historyOfNonexistentBindings.append(" ON hst_out.customer_id = rec.customer_id");
            historyOfNonexistentBindings.append(" LEFT JOIN mailinglist_tbl ml");
            historyOfNonexistentBindings.append(" ON hst_out.mailinglist_id = ml.mailinglist_id");
            historyOfNonexistentBindings.append(" ORDER BY timestamp_change ASC");

            CompositeRowMapperWithMailinglist mapper = new CompositeRowMapperWithMailinglist("ml_");
            return select(historyOfNonexistentBindings.toString(), mapper, recipientId);
        }

        return Collections.emptyList();
    }

    public static class PlainRowMapper implements RowMapper<PlainBindingEntryHistory> {

        @Override
        public PlainBindingEntryHistory mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            PlainBindingEntryHistory bindingEntryHistory = new PlainBindingEntryHistoryImpl();

            bindingEntryHistory.setCustomerId(resultSet.getInt("customer_id"));
            bindingEntryHistory.setMailingListId(resultSet.getInt("mailinglist_id"));
            bindingEntryHistory.setUserType(resultSet.getString("user_type"));
            bindingEntryHistory.setUserStatus(resultSet.getInt("user_status"));
            bindingEntryHistory.setUserRemark(resultSet.getString("user_remark"));
            bindingEntryHistory.setTimestamp(resultSet.getTimestamp("timestamp"));
            bindingEntryHistory.setCreationDate(resultSet.getTimestamp("creation_date"));
            bindingEntryHistory.setExitMailingId(resultSet.getInt("exit_mailing_id"));
            bindingEntryHistory.setMediaType(resultSet.getInt("mediatype"));
            bindingEntryHistory.setChangeType(resultSet.getInt("change_type"));
            bindingEntryHistory.setTimestampChange(resultSet.getTimestamp("timestamp_change"));
            bindingEntryHistory.setClientInfo(resultSet.getString("client_info"));
            bindingEntryHistory.setEmail(resultSet.getString("email"));

            return bindingEntryHistory;
        }
    }

    public static class CompositeRowMapperWithMailinglist implements RowMapper<CompositeBindingEntryHistory> {

        private final String columnPrefix;
        private final MailinglistRowMapper mailinglistRowMapper;

        /**
         * Constructor uses in case of your ResultSet contains default names of columns with some prefix.
         * Maybe useful when you calls one RowMapper inside the second one.
         *
         * @param mailinglistColumnPrefix prefix for mailing list column names.
         */
        public CompositeRowMapperWithMailinglist(String mailinglistColumnPrefix) {
            columnPrefix = StringUtils.EMPTY;
            mailinglistRowMapper = new MailinglistRowMapper(mailinglistColumnPrefix);
        }

        @Override
        public CompositeBindingEntryHistory mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            CompositeBindingEntryHistory compositeBindingHistory = new CompositeBindingEntryHistoryImpl();

            // mailinglist mapping
            Mailinglist mailinglist = mailinglistRowMapper.mapRow(resultSet, rowNum);
            compositeBindingHistory.setMailingList(mailinglist.getId() > 0 ? mailinglist : null);

            // recipient mapping. This RowMapper adds just Mailinglist entity.
            compositeBindingHistory.setRecipient(null);

            compositeBindingHistory.setCustomerId(resultSet.getInt(columnPrefix + "customer_id"));
            compositeBindingHistory.setMailingListId(resultSet.getInt(columnPrefix + "mailinglist_id"));
            compositeBindingHistory.setUserType(resultSet.getString(columnPrefix + "user_type"));
            compositeBindingHistory.setUserStatus(resultSet.getInt(columnPrefix + "user_status"));
            compositeBindingHistory.setUserRemark(resultSet.getString(columnPrefix + "user_remark"));
            compositeBindingHistory.setTimestamp(resultSet.getTimestamp(columnPrefix + "timestamp"));
            compositeBindingHistory.setCreationDate(resultSet.getTimestamp(columnPrefix + "creation_date"));
            compositeBindingHistory.setExitMailingId(resultSet.getInt(columnPrefix + "exit_mailing_id"));
            compositeBindingHistory.setMediaType(resultSet.getInt(columnPrefix + "mediatype"));
            compositeBindingHistory.setChangeType(resultSet.getInt(columnPrefix + "change_type"));
            compositeBindingHistory.setTimestampChange(resultSet.getTimestamp(columnPrefix + "timestamp_change"));
            compositeBindingHistory.setClientInfo(resultSet.getString(columnPrefix + "client_info"));
            compositeBindingHistory.setEmail(resultSet.getString(columnPrefix + "email"));

            return compositeBindingHistory;
        }
    }

    private String getRecipientBindingHistoryTableName(int companyId) {
        return String.format("hst_customer_%d_binding_tbl", companyId);
    }

    private String getRecipientBindingTableName(int companyId) {
        return String.format("customer_%d_binding_tbl", companyId);
    }

    private String getRecipientTableName(int companyId) {
        return String.format("customer_%d_tbl", companyId);
    }
}
