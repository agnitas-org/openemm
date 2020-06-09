/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.delivery.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.emm.core.delivery.beans.DeliveryInfo;
import com.agnitas.emm.core.delivery.dao.DeliveryDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class DeliveryDaoImpl extends BaseDaoImpl implements DeliveryDao {

    private static final transient Logger LOGGER = Logger.getLogger(DeliveryDaoImpl.class);

    private static final String COMPANY_ID_PLACEHOLDER = "#_company_id_#";
    private static final String DELIVERY_TABLE_NAME_TEMPLATE = "deliver_" + COMPANY_ID_PLACEHOLDER + "_tbl";

    private static final String QUERY_DELIVERIES_INFO = "SELECT id, timestamp, line FROM " + DELIVERY_TABLE_NAME_TEMPLATE + " WHERE mailing_id = ? AND customer_id = ?";

    @Override
    public List<DeliveryInfo> getDeliveriesInfo(@VelocityCheck final int companyId, final int mailingId, final int customerId) {
        final String query = setCompanyIdToPlaceholder(QUERY_DELIVERIES_INFO, companyId);
        return select(LOGGER, query, new DeliveryInfoMapper(), mailingId, customerId);
    }

    @Override
    public boolean checkIfDeliveryTableExists(final int companyId) {
        return DbUtilities.checkIfTableExists(getDataSource(), setCompanyIdToPlaceholder(DELIVERY_TABLE_NAME_TEMPLATE, companyId));
    }

    private static String setCompanyIdToPlaceholder(final String query, final int companyId) {
        if(StringUtils.isBlank(query)) {
            return query;
        }
        return query.replaceAll(COMPANY_ID_PLACEHOLDER, Integer.toString(companyId));
    }

    private static class DeliveryInfoMapper implements RowMapper<DeliveryInfo> {

        @Override
        public DeliveryInfo mapRow(ResultSet resultSet, int i) throws SQLException {
            final DeliveryInfo deliveryInfo = new DeliveryInfo();
            deliveryInfo.setId(resultSet.getInt("id"));
            deliveryInfo.setTimestamp(new Date(resultSet.getDate("timestamp").getTime()));

            final String line = resultSet.getString("line");
            if(StringUtils.isNotBlank(line)) {
                deliveryInfo.setDsn(parsePropertyFromLine(line, "dsn"));
                deliveryInfo.setRelay(parsePropertyFromLine(line, "relay"));
                deliveryInfo.setStatus(parsePropertyFromLine(line, "status"));
            }

            return deliveryInfo;
        }

        private String parsePropertyFromLine(final String line, final String propertyName) {
            final Pattern pattern = Pattern.compile(propertyName + "=(.*?)(:?, \\w+=|$)");
            final Matcher matcher = pattern.matcher(line);
            if(matcher.find()){
                return matcher.group(1);
            }
            return null;
        }
    }
}
