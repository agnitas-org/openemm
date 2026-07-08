/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.dao.impl;

import static com.agnitas.util.DbUtilities.resultsetHasColumn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import com.agnitas.beans.IntEnum;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry;
import com.agnitas.emm.core.response_inbox.dao.MailloopReplyDao;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyContentType;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus;
import com.agnitas.emm.core.response_inbox.forms.ResponseInboxOverviewFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class MailloopReplyDaoImpl extends PaginatedBaseDaoImpl implements MailloopReplyDao {

    private static final RowMapper<MailloopReplyEntry> ROW_MAPPER = (rs, rowNum) -> {
        final MailloopReplyEntry entry = new MailloopReplyEntry();

        entry.setId(rs.getInt("id"));
        entry.setStatus(IntEnum.fromId(MailloopReplyStatus.class, rs.getInt("status")));
        entry.setSubject(rs.getString("subject"));
        entry.setSenderFullName(rs.getString("sender_full_name"));
        entry.setSenderEmail(rs.getString("sender_email"));

        if (resultsetHasColumn(rs, "response_email")) {
            entry.setResponseEmail(rs.getString("response_email"));
        }

        if (resultsetHasColumn(rs, "timestamp")) {
            entry.setTimestamp(rs.getTimestamp("timestamp"));
        }

        if (resultsetHasColumn(rs, "content")) {
            entry.setContent(rs.getString("content"));
            entry.setContentType(IntEnum.fromId(MailloopReplyContentType.class, rs.getInt("content_type")));
        }

        if (resultsetHasColumn(rs, "customer_id") && rs.getObject("customer_id") != null) {
            entry.setCustomerId(rs.getInt("customer_id"));
        }

        return entry;
    };

    public MailloopReplyDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public void markAsRead(int id, int companyID) {
        String query = "UPDATE mailloop_replies_tbl SET status = ?, change_date = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?";
        update(query, MailloopReplyStatus.READ.getId(), id, companyID);
    }

    @Override
    public void delete(Set<Integer> ids, int companyID) {
        update("DELETE FROM mailloop_replies_tbl WHERE company_id = ? AND" + makeBulkInClauseForInteger("id", ids), companyID);
    }

    @Override
    public boolean deleteByCompany(int companyId) {
        update("DELETE FROM mailloop_replies_tbl WHERE company_id = ?", companyId);
        return selectInt("SELECT COUNT(*) FROM mailloop_replies_tbl WHERE company_id = ?", companyId) == 0;
    }

    @Override
    public void deleteExpired(int companyID, Date cutoffDate) {
        update("DELETE FROM mailloop_replies_tbl WHERE company_id = ? AND creation_date < ?", companyID, cutoffDate);
    }

    @Override
    public boolean existsForMailloop(int mailloopId) {
        return selectInt("SELECT COUNT(*) FROM mailloop_replies_tbl WHERE mailloop_id = ?", mailloopId) > 0;
    }

    @Override
    public void deleteForMailloop(int mailloopId, int companyId) {
        update("DELETE FROM mailloop_replies_tbl WHERE mailloop_id = ? AND company_id = ?", mailloopId, companyId);
    }

    @Override
    public MailloopReplyEntry getReply(int id, int companyID) {
        String query = "SELECT id, status, subject, sender_full_name, sender_email, content, content_type, customer_id, response_email, timestamp FROM mailloop_replies_tbl WHERE company_id = ? AND id = ?";
        return selectObjectDefaultNull(query, ROW_MAPPER, companyID, id);
    }

    @Override
    public List<MailloopReplyEntry> getReplies(Set<Integer> ids, int companyID) {
        String query = "SELECT id, status, subject, sender_full_name, sender_email FROM mailloop_replies_tbl WHERE company_id = ? AND "
                + makeBulkInClauseForInteger("id", ids);

        return select(query, ROW_MAPPER, companyID);
    }

    @Override
    public PaginatedList<MailloopReplyEntry> getOverviewList(ResponseInboxOverviewFilter filter, int companyId) {
        StringBuilder query = new StringBuilder("SELECT id, status, subject, sender_full_name, sender_email, response_email, timestamp FROM mailloop_replies_tbl");
        List<Object> params = applyOverviewFilter(filter, query, companyId);

        PaginatedList<MailloopReplyEntry> list;
        if (StringUtils.isBlank(filter.getSort())) {
            String sortClause = "ORDER BY status, timestamp DESC";
            list = selectPaginatedListWithSortClause(query.toString(), sortClause, filter, ROW_MAPPER, params.toArray());
        } else {
            list = selectPaginatedList(query.toString(), "mailloop_replies_tbl", filter, ROW_MAPPER, params.toArray());
        }

        if (filter.isUiFiltersSet()) {
            list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(companyId));
        }

        return list;
    }

    private List<Object> applyOverviewFilter(ResponseInboxOverviewFilter filter, StringBuilder query, int companyId) {
        List<Object> params = applyRequiredOverviewFilter(query, companyId);

        if (StringUtils.isNotBlank(filter.getSubject())) {
            query.append(getPartialSearchFilterWithAnd("subject"));
            params.add(filter.getSubject());
        }

        if (StringUtils.isNotBlank(filter.getSender())) {
            query.append(" AND (").append(getPartialSearchFilter("sender_full_name"))
                    .append(" OR ").append(getPartialSearchFilter("sender_email"))
                    .append(" OR ").append(getPartialSearchFilter("CONCAT(CONCAT(CONCAT(sender_full_name, ' <'), sender_email), '>')")).append(")");
            params.addAll(List.of(filter.getSender(), filter.getSender(), filter.getSender()));
        }

        if (StringUtils.isNotBlank(filter.getResponseAddress())) {
            query.append(getPartialSearchFilterWithAnd("response_email"));
            params.add(filter.getResponseAddress());
        }

        if (filter.getStatus() != null) {
            query.append(" AND status = ?");
            params.add(filter.getStatus().getId());
        }

        query.append(getDateRangeFilterWithAnd("timestamp", filter.getTimestamp(), params));

        return params;
    }

    private int getTotalUnfilteredCountForOverview(int companyId) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM mailloop_replies_tbl");
        List<Object> params = applyRequiredOverviewFilter(query, companyId);

        return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
    }

    private List<Object> applyRequiredOverviewFilter(StringBuilder query, int companyId) {
        query.append(" WHERE company_id = ? AND (company_id = customer_company_id OR customer_company_id IS NULL)");
        return new ArrayList<>(List.of(companyId));
    }

}
