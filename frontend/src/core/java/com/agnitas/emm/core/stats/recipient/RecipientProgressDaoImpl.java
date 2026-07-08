/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stats.recipient;

import static com.agnitas.util.DbUtilities.getCustomerBindingTableName;
import static com.agnitas.util.DbUtilities.getHstCustomerBindingTableName;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dto.RecipientProgress;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.DbUtilities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@Component
class RecipientProgressDaoImpl extends BaseDaoImpl implements RecipientProgressDao {

    private final ConfigService configService;

    public RecipientProgressDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            JavaMailService javaMailService,
            ConfigService configService
    ) {
        super(dataSource, javaMailService);
        this.configService = configService;
    }

    private record RecipientDetailsMapCallback(Map<LocalDate, RecipientProgress> dateMap) implements RowCallbackHandler {

        private RecipientDetailsMapCallback(Map<LocalDate, RecipientProgress> dateMap) {
            this.dateMap = Objects.requireNonNull(dateMap);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            LocalDate time = rs.getDate("time").toLocalDate();
            RecipientProgress item = dateMap.computeIfAbsent(time, p -> new RecipientProgress());
            UserStatus status = UserStatus.getUserStatusOrNull(rs.getInt("userstatus"));
            int amount = rs.getInt("amount");
            if (status == null) {
                item.setOptOuts(item.getOptOuts() + amount);
            } else {
                updateByStatus(item, status, amount);
            }
        }

        private void updateByStatus(RecipientProgress item, UserStatus status, int amount) {
            switch (status) {
                case Active -> item.setOptIns(item.getOptIns() + amount);
                case Bounce -> item.setBounced(item.getBounced() + amount);
                case WaitForConfirm -> item.setDoubleOptIn(item.getDoubleOptIn() + amount);
                case Blacklisted -> item.setBlocklisted(item.getBlocklisted() + amount);
                default -> item.setOptOuts(item.getOptOuts() + amount);
            }
        }
    }

    @Override
    public Map<LocalDate, RecipientProgress> getProgress(int companyId, Filter filter) {
        Map<LocalDate, RecipientProgress> progress = new HashMap<>();
        List<Object> params = new ArrayList<>(Arrays.asList(filter.dateRange().getFrom(), filter.dateRange().getTo()));
        String dateSelectPart = dateSelectPart();

        StringBuilder sql = new StringBuilder("SELECT ")
                .append(dateSelectPart).append(" AS time,")
                .append(" bind.user_status AS userstatus,")
                .append(" COUNT(*) AS amount FROM customer_%d_binding_tbl bind".formatted(companyId));

        if (!isBlank(filter.targetSql())) {
            sql.append(" JOIN customer_%d_binding_tbl cust ON bind.customer_id = cust.customer_id".formatted(companyId));
        }
        sql.append(" WHERE bind.timestamp >= ? AND bind.timestamp < ? ");
        sql.append(andClause(filter.targetSql()));

        // bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

        if (filter.mailinglistId() > 0) {
            sql.append(" AND bind.mailinglist_id = ?");
            params.add(filter.mailinglistId());
        }
        if (filter.mediaType() != null && filter.mediaType() != MediaTypes.EMAIL) {
            sql.append(" AND bind.mediatype = ?");
            params.add(filter.mediaType());
        } else if (filter.mediaType() == MediaTypes.EMAIL) {
            sql.append(" AND (bind.mediatype = 0 OR bind.mediatype IS NULL)");
        }
        if (DbUtilities.checkIfTableExists(getDataSource(), "ahv_" + companyId + "_tbl")) {
            // Exclude bounced recipients that are included in AHV process
            sql.append(" AND (bind.user_status != %s OR bind.customer_id NOT IN (SELECT customer_id FROM ahv_%d_tbl WHERE bouncecount > 1))"
                    .formatted(UserStatus.Bounce.getStatusCode(), companyId));
        }
        sql.append(" GROUP BY ").append(dateSelectPart).append(", bind.user_status");
        sql.append(" ORDER BY ").append(dateSelectPart).append(", bind.user_status");

        RecipientDetailsMapCallback callback = new RecipientDetailsMapCallback(progress);
        query(sql.toString(), callback, params.toArray(new Object[0]));

        if (configService.getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyId)) {
            query(adaptProgressSqlForHst(companyId, sql), callback, params.toArray(new Object[0]));
        }
        return progress;
    }

    private static String adaptProgressSqlForHst(int companyId, StringBuilder sql) {
        return sql.toString().replace(
                getCustomerBindingTableName(companyId),
                getHstCustomerBindingTableName(companyId)
        );
    }

    private String dateSelectPart() {
        if (isOracleDB() || isPostgreSQL()) {
            return "TO_CHAR(bind.timestamp, 'dd.mm.yyyy')";
        }
        return "DATE_FORMAT(bind.timestamp, '%d.%m.%Y')";
    }
}
