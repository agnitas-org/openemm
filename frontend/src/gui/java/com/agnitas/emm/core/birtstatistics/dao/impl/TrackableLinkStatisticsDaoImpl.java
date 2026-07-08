/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.TrackableLinkStatisticsDao;
import com.agnitas.emm.core.birtstatistics.model.LinkClickStatisticData;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TrackableLinkStatisticsDaoImpl extends BaseDaoImpl implements TrackableLinkStatisticsDao {

    protected TrackableLinkStatisticsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<Integer, LinkClickStatisticData> getClickStatisticsPerLink(
            int mailingId,
            int companyId,
            String targetSql,
            Set<UserType> userTypes,
            DateRange timestamp,
            DeviceClass deviceClass,
            Integer customerId
    ) {
        List<Object> params = new ArrayList<>(List.of(mailingId));

        StringBuilder query = new StringBuilder("SELECT rlog.url_id, COUNT(*) AS clicks, COUNT(DISTINCT rlog.customer_id) AS clickers");
        query.append(" FROM rdirlog_").append(companyId).append("_tbl rlog");

        if (containsCustomerCondition(targetSql) || CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON rlog.customer_id = cust.customer_id");
        }

        query.append(" WHERE rlog.mailing_id = ?");

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND cust.customer_id IN (SELECT DISTINCT customer_id FROM customer_").append(companyId).append("_binding_tbl")
                    .append(" WHERE ")
                    .append(makeBulkInClause("user_type", userTypes.size()))
                    .append(" AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
            params.add(mailingId);
        }

        if (deviceClass != null) {
            query.append(" AND rlog.device_class_id = ?");
            params.add(deviceClass.getId());
        }

        if (customerId != null) {
            query.append(" AND rlog.customer_id = ?");
            params.add(customerId);
        }

        query.append(getDateRangeFilterWithAnd("rlog.timestamp", timestamp, params));

        if (StringUtils.isNotEmpty(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        query.append(" GROUP BY rlog.url_id ORDER BY rlog.url_id");

        Map<Integer, LinkClickStatisticData> result = new LinkedHashMap<>();

        query(query.toString(), rs -> {
            int urlId = rs.getInt("url_id");
            result.put(
                    urlId,
                    new LinkClickStatisticData(
                            urlId,
                            rs.getInt("clicks"),
                            rs.getInt("clickers")
                    )
            );
        }, params.toArray());

        return result;
    }

    private static boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

}
