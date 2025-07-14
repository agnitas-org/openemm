/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.messages.I18nString.getLocaleString;
import static java.util.Collections.emptyList;
import static com.agnitas.util.DbUtilities.isTautologicWhereClause;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.agnitas.emm.core.profilefields.form.ProfileFieldStatForm;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ProfileFieldEvaluationDataSet extends RecipientsBasedDataSet {

    private final RecipientFieldService recipientFieldService;

    public ProfileFieldEvaluationDataSet() { // TODO: EMMGUI-714: remove when old design will be removed
        this.recipientFieldService = null;
    }

    @Autowired
    public ProfileFieldEvaluationDataSet(RecipientFieldService recipientFieldService) {
        this.recipientFieldService = recipientFieldService;
    }

    private static final RowMapper<ProfileFieldStatRow> ROW_MAPPER = (rs, i) -> new ProfileFieldStatRow(
        rs.getString("value"),
        rs.getInt("count"));


    // TODO: EMMGUI-714: replace with com.agnitas.reporting.birt.external.dataset.ProfileFieldEvaluationDataSet.collect(com.agnitas.emm.core.profilefields.form.ProfileFieldStatForm) when old design will be removed
    // used in profiledb_evaluation.rptdesign
    public List<ProfileFieldStatRow> collect(String colName, int companyId, int limit,
                                             int mailinglistId, int targetId, String hiddenTargetsCsv) {
        colName = sanitizeColumnName(colName); // guard

        List<Object> params = new ArrayList<>();
        String sql = """
            SELECT %s value, COUNT(*) count
            FROM %s cust WHERE %s IS NOT NULL AND %s = 0
            %s %s
            GROUP BY %s ORDER BY count DESC
            """.formatted(colName, getCustomerTableName(companyId), colName,
            RecipientStandardField.Bounceload.getColumnName(),
            applyMailinglistFilter(mailinglistId, companyId, params),
            applyTargetsFilter(targetId, hiddenTargetsCsv, companyId),
            colName);

        sql = applySqlLimit(limit, sql, params);
        try {
            List<ProfileFieldStatRow> stat = select(sql, ROW_MAPPER, params.toArray());
            addRates(stat);
            return stat;
        } catch (Exception e) {
            logger.error("Error while collecting profile fields stat: {}", e.getMessage(), e);
            return emptyList();
        }
    }

    private String sanitizeColumnName(String columnName) { return columnName.replaceAll("[^a-zA-Z0-9_]", ""); }

    public List<ProfileFieldStatRow> collect(ProfileFieldStatForm form) {
        final String colName = form.getColName();
        int companyId = form.getCompanyId();
        if (companyId <= 0) {
            throw new IllegalArgumentException("Invalid company id");
        }
        if (isInvalidColumn(colName, companyId)) {
            throw new IllegalArgumentException("Invalid column name: " + colName);
        }
        return collect(form.getColName(), form.getCompanyId(), form.getLimit(), form.getMailingListId(), form.getTargetId(), form.getHiddenTargetsCsv());
    }

    public byte[] csv(ProfileFieldStatForm form) throws Exception {
        return CsvWriter.csv(ListUtils.union(
            List.of(getHeaderForCsv(form.getLocale())),
            getRowsForCsv(form)));
    }

    private List<List<String>> getRowsForCsv(ProfileFieldStatForm form) {
        return collect(form).stream().map(row -> List.of(row.getValue(), row.getAmount())).toList();
    }

    private static List<String> getHeaderForCsv(Locale locale) {
        return List.of(getLocaleString("Value", locale), getLocaleString("statistic.amount.percent", locale));
    }

    private boolean isInvalidColumn(String colName, int companyId) {
        return recipientFieldService.getRecipientFields(companyId).stream()
            .noneMatch(field -> colName.equals(field.getColumnName()));
    }

    private void addRates(List<ProfileFieldStatRow> stat) {
        int total = stat.stream().mapToInt(ProfileFieldStatRow::getCount).sum();
        stat.forEach(row -> row.setRate((float) row.getCount() / total * 100)); // TODO: EMMGUI-714: remove when old design will be removed
        stat.forEach(row -> row.setAmount("%s (%.2f%%)".formatted(
            row.getCount(),
            (float) row.getCount() / total * 100)));
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

        private final int count;
        private final String value;
        private float rate; // TODO: EMMGUI-714: remove when old design will be removed
        private String amount;

        public ProfileFieldStatRow(String value, int count) {
            this.value = value;
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public float getRate() {
            return rate;
        }

        public void setRate(float rate) {
            this.rate = rate;
        }
    }
}
