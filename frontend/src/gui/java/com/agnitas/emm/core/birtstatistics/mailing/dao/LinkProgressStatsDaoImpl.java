/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dao;

import javax.sql.DataSource;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.model.CustomerEventStats;
import com.agnitas.emm.core.birtstatistics.model.MailingProgressStatisticFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LinkProgressStatsDaoImpl extends AbstractCustomerEventProgressStatDao implements LinkProgressStatsDao {

    private static final String TIMESTAMP_COLUMN = "timestamp";

    public LinkProgressStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    protected String getKeyColumnName() {
        return "url_id";
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

    private static String getRdirLogTableName(int companyId) {
        return "rdirlog_%d_tbl".formatted(companyId);
    }

}
