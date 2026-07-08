/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dao;

import java.util.List;
import javax.sql.DataSource;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.model.MailingProgressStatisticFilter;
import com.agnitas.emm.core.birtstatistics.model.CustomerEventStats;
import com.agnitas.emm.core.birtstatistics.model.MailingDeliveryProgressStats;
import com.agnitas.emm.core.commons.dto.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MailingProgressStatsDaoImpl extends AbstractCustomerEventProgressStatDao implements MailingProgressStatsDao {

    private static final String TIMESTAMP_COLUMN = "timestamp";
    private static final String CREATION_COLUMN = "creation";

    public MailingProgressStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    protected String getKeyColumnName() {
        return "mailing_id";
    }

    @Override
    public CustomerEventStats getMultiDeviceClicksStats(MailingProgressStatisticFilter filter) {
        return getEventStats(
                filter,
                DeviceFilterType.MULTI,
                getRdirLogTableName(filter.getCompanyId()),
                TIMESTAMP_COLUMN
        );
    }

    @Override
    public CustomerEventStats getSingleDeviceClicksStats(MailingProgressStatisticFilter filter) {
        return getEventStats(
                filter,
                DeviceFilterType.SINGLE,
                getRdirLogTableName(filter.getCompanyId()),
                TIMESTAMP_COLUMN
        );
    }

    @Override
    public CustomerEventStats getMultiDeviceOpensStats(MailingProgressStatisticFilter filter) {
        return getEventStats(
                filter,
                DeviceFilterType.MULTI,
                getOnePixelDeviceTableName(filter.getCompanyId()),
                CREATION_COLUMN
        );
    }

    @Override
    public CustomerEventStats getSingleDeviceOpensStats(MailingProgressStatisticFilter filter) {
        return getEventStats(
                filter,
                DeviceFilterType.SINGLE,
                getOnePixelDeviceTableName(filter.getCompanyId()),
                CREATION_COLUMN
        );
    }

    @Override
    public List<MailingDeliveryProgressStats> getDeliveries(int mailingId, int companyId, DateRange timestamp, boolean groupByDay) {
        String query = """
                SELECT SUM(no_of_mailings) AS mail_num,
                       bucket_ts
                FROM (SELECT no_of_mailings,
                             %s AS bucket_ts
                      FROM mailing_account_tbl
                      WHERE ? <= timestamp
                        AND timestamp < ?
                        AND mailing_id = ?
                        AND company_id = ?
                        AND status_field NOT IN ('A', 'T')) t
                GROUP BY bucket_ts
                ORDER BY bucket_ts
                """.formatted(getTimestampBucketExpression(groupByDay));

        return select(
                query,
                (rs, rowNum) ->
                        new MailingDeliveryProgressStats(
                                rs.getInt("mail_num"),
                                rs.getTimestamp("bucket_ts")
                        ),
                timestamp.getFrom(),
                timestamp.getTo(),
                mailingId,
                companyId
        );
    }

    private String getTimestampBucketExpression(boolean groupByDay) {
        if (isOracleDB()) {
            return groupByDay
                    ? "TRUNC(timestamp)"
                    : "TRUNC(timestamp,'MI') - MOD(TO_NUMBER(TO_CHAR(timestamp,'MI')),10)/1440";
        }

        if (isPostgreSQL()) {
            return groupByDay
                    ? "date_trunc('day', timestamp)"
                    : "date_trunc('minute', timestamp) - (EXTRACT(minute FROM timestamp)::int % 10) * INTERVAL '1 minute'";
        }

        return groupByDay
                ? "DATE(timestamp)"
                : "FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(timestamp)/600)*600)";
    }

    private static String getOnePixelDeviceTableName(int companyId) {
        return "onepixellog_device_%d_tbl".formatted(companyId);
    }

    private static String getRdirLogTableName(int companyId) {
        return "rdirlog_%d_tbl".formatted(companyId);
    }

}
