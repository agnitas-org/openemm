/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dao;

import java.util.ArrayList;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.dto.DateRange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MailingSummaryStatsDaoImpl extends BaseDaoImpl implements MailingSummaryStatsDao {

    public MailingSummaryStatsDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    @Override
    public DispatchInfo getDispatchInfo(int mailingId, DateRange dateRange) {
        ArrayList<Object> params = new ArrayList<>();
        params.add(mailingId);

        return selectObject("""
                        SELECT
                          COALESCE(SUM(no_of_mailings), 0) AS count,
                          MIN(timestamp) AS min_timestamp,
                          MAX(timestamp) AS max_timestamp,
                          COALESCE(SUM(no_of_bytes), 0) AS bytes
                        FROM mailing_account_tbl
                        WHERE mailing_id = ?
                          AND status_field NOT IN ('A', 'T', 'V')
                          %s
                        """.formatted(getRangeFilterWithAnd("timestamp", dateRange, params))
                , (rs, i) -> new DispatchInfo(
                        rs.getInt("count"),
                        rs.getTimestamp("min_timestamp"),
                        rs.getTimestamp("max_timestamp"),
                        rs.getInt("bytes")
                ), params.toArray());
    }

    @Override
    public int getRdirTrafficAgrSize(int mailingId, int companyId) {
        return selectInt("""
                        SELECT SUM(content_size * amount) AS bytes
                        FROM rdir_traffic_agr_%d_tbl WHERE mailing_id = ?
                        """.formatted(companyId),
                mailingId
        );
    }

    @Override
    public long getRdirTrafficAmountSize(int mailingId, int companyId) {
        return selectLong("""
                        SELECT SUM(content_size) AS bytes
                        FROM rdir_traffic_amount_%d_tbl
                        WHERE mailing_id = ?
                        """.formatted(companyId),
                mailingId
        );
    }

    @Override
    public int getAnonymousUsersCount(int mailingId, int companyId) {
        return selectInt("""
                        SELECT COUNT(DISTINCT cust.customer_id)
                        FROM mailtrack_%d_tbl mtrack
                          JOIN customer_%d_tbl cust ON cust.customer_id = mtrack.customer_id
                        WHERE mtrack.mailing_id = ?
                          AND cust.sys_tracking_veto > 0
                        """.formatted(companyId, companyId),
                mailingId
        );
    }
}
