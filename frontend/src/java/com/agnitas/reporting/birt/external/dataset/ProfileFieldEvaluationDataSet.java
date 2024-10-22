/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.emm.core.service.RecipientStandardField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.agnitas.util.DbUtilities.isTautologicWhereClause;

public class ProfileFieldEvaluationDataSet extends RecipientsBasedDataSet {

    private static final Logger logger = LogManager.getLogger(ProfileFieldEvaluationDataSet.class);

    // used in profiledb_evaluation.rptdesign
    public List<ProfileFieldStatRow> collect(String colName, int companyId, int limit,
                                             int mailinglistId, int targetId, String hiddenTargetsCsv) {
        List<Object> params = new ArrayList<>();

        String sql = " SELECT " + colName + " value, COUNT(*) count" +
            " FROM " + getCustomerTableName(companyId) + " cust" +
            " WHERE " + colName + " IS NOT NULL" +
            " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0" +
            applyMailinglistFilter(mailinglistId, companyId, params) +
            applyTargetsFilter(targetId, hiddenTargetsCsv, companyId) +
            " GROUP BY " + colName +
            " ORDER BY count DESC";
        sql = applySqlLimit(limit, sql, params);

        List<ProfileFieldStatRow> stat = select(logger, sql,
            (rs, i) -> new ProfileFieldStatRow(rs.getString("value"), rs.getInt("count")),
            params.toArray());
        calculateRates(stat);
        return stat;
    }

    private void calculateRates(List<ProfileFieldStatRow> stat) {
        int total = stat.stream().mapToInt(ProfileFieldStatRow::getCount).sum();
        stat.forEach(row -> row.setRate((float) row.getCount() / total * 100));
    }

    private String applySqlLimit(int limit, String sql, List<Object> params) {
        params.add(limit);
        if (isOracleDB()) {
            return  "SELECT * FROM (" + sql + ") WHERE ROWNUM <= ?";
        }
        sql += " LIMIT ?";
        return sql;
    }

    private String applyTargetsFilter(int targetId, String hiddenTargets, int companyId) {
        String targetSql = targetId > 0 ? getTargetSqlString(String.valueOf(targetId), companyId) : "";
        targetSql = joinWhereClause(targetSql, getTargetSqlString(hiddenTargets, companyId));
        if (isTautologicWhereClause(targetSql)) {
            return "";
        }
        return " AND (" + targetSql + ")";
    }

    private String applyMailinglistFilter(int mailinglistId, int companyId, List<Object> params) {
        if (mailinglistId <= 0) {
            return "";
        }
        params.add(mailinglistId);
        return " AND (EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyId) + " bind" +
            " WHERE (bind.customer_id = cust.customer_id) AND (bind.mailinglist_id = ?)))";
    }

    public static class ProfileFieldStatRow {

        private String value;
        private int count;
        private float rate;

        public ProfileFieldStatRow(String value, int count) {
            this.value = value;
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public float getRate() {
            return rate;
        }

        public void setRate(float rate) {
            this.rate = rate;
        }
    }
}
