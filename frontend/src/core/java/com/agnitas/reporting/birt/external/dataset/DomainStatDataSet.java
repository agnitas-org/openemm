/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.DomainStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class DomainStatDataSet extends BIRTDataSet {

	private static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}

	private static String getCustomerBindingTableName(int companyId) {
		return "customer_" + companyId + "_binding_tbl";
	}

	public byte[] csv(DomainStatisticForm form, Admin admin) throws Exception {
		return CsvWriter.csv(ListUtils.union(
				List.of(getHeaderForCsv(admin.getLocale())),
				getRowsForCsv(form, admin))
		);
	}

	private static List<String> getHeaderForCsv(Locale locale) {
		return List.of(
				getLocaleString("statistic.domain", locale),
				getLocaleString("statistic.amount.percent", locale)
		);
	}

	private List<List<String>> getRowsForCsv(DomainStatisticForm form, Admin admin) {
		return getDomainStat(form, admin)
				.stream()
				.map(row -> List.of(row.getDomainName(), "%s (%.2f%%)".formatted(row.getCount(), row.getRate())))
				.toList();
	}

	public List<DomainStatRow> getDomainStat(DomainStatisticForm form, Admin admin) {
		return getDomainStat(
				admin.getCompanyID(),
				form.getTargetId(),
				form.getMailinglistId(),
				form.getMaxDomainNum(),
				StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN"),
				form.isTopLevelDomain()
		);
	}

	protected List<DomainStatRow> getDomainStat(int companyID, Integer targetID, Integer mailinglistId, int limit, String language, boolean topLevelDomains) {
		language = StringUtils.defaultIfEmpty(language, "EN");

		String targetSql = "";
		if (targetID != null) {
			LightTarget target =  getTarget(targetID, companyID);
			if (!StringUtils.isEmpty(target.getTargetSQL())) {
				targetSql = "AND (" + target.getTargetSQL()+ ")";
			}
		}

		String mailingListSql = "";
		if (mailinglistId != null) {
			mailingListSql = "AND bind.mailinglist_id = " + mailinglistId;
		}

		int totalCount = getDomainsTotalCount(companyID, mailingListSql, targetSql);

		List<DomainStatRow> domainList = getTopDomainList(companyID, mailingListSql, targetSql, limit, totalCount, language, topLevelDomains);
		domainList.forEach(d -> d.setRate(getRate(d.getCount(), totalCount)));

		return domainList;
	}

	private int getDomainsTotalCount(int companyId, String mailingListSql, String targetSql) {
		String sql = "SELECT COUNT(DISTINCT cust.customer_id) AS domain_count"
			+ " FROM " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind"
			+ " WHERE cust.customer_id = bind.customer_id"
			+ " AND cust." + RecipientStandardField.Bounceload.getColumnName() + " = 0"
			+ " AND bind.user_status = " + UserStatus.Active.getStatusCode()
			+ " " + mailingListSql
			+ " " + targetSql;

		return select(sql, Integer.class);
	}

	private float getRate(int count, int totalCount) {
		if (totalCount == 0) {
			return 0;
		}

		return (float) count / totalCount * 100;
	}

	private List<DomainStatRow> getTopDomainList(int companyId, String mailingListSql, String targetSql, int limit, int totalCount, String language, boolean topLevelDomains) {
		String instrEmail;
        if (isOracleDB()) {
        	instrEmail = topLevelDomains ? "INSTR(email, '.', -1)" : "INSTR(email, '@')";
        } else if (isPostgreSQL()) {
			instrEmail = topLevelDomains
					? "LENGTH(email) - POSITION('.' IN REVERSE(email))"
					: "POSITION('@' IN email)";
		} else {
            instrEmail = topLevelDomains ? "LENGTH(email) - INSTR(REVERSE(email), '.')" : "INSTR(email, '@')";
        }
		
		String sql = "SELECT COUNT(DISTINCT cust.customer_id) AS domain_count, SUBSTR(email, " + instrEmail + " + 1) AS domain_name"
				+ " FROM " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind "
				+ " WHERE cust.customer_id = bind.customer_id"
				+ " AND cust." + RecipientStandardField.Bounceload.getColumnName() + " = 0"
				+ " AND bind.user_status = " + UserStatus.Active.getStatusCode()
				+ " " + mailingListSql
				+ " " + targetSql
				+ " GROUP BY SUBSTR(email, " + instrEmail + " + 1)"
				+ " ORDER BY domain_count DESC";
		
		DomainRowMapper rowMapper = new DomainRowMapper();
		String sqlLimited = "SELECT * FROM (" + sql + ")";
		
		if (isOracleDB()) {
			sqlLimited += " WHERE ROWNUM <= " + limit;
		} else {
			sqlLimited += " cust_bind_data LIMIT " + limit;
		}
		
		List<DomainStatRow> domainList = select(sqlLimited, rowMapper);

		if (!domainList.isEmpty() || totalCount > 0) {
			// calculate "others"
			String othersText = I18nString.getLocaleString("statistic.Other", language);

			int othersTotal = totalCount - rowMapper.getTotalValue();
			domainList.add(new DomainStatRow(othersText, othersTotal));
		}

		return domainList;
	}

	private static class DomainRowMapper implements RowMapper<DomainStatRow> {
		
		private final AtomicInteger totalValue = new AtomicInteger();
		
		private int getTotalValue() {
			return totalValue.get();
		}
		
		@Override
		public DomainStatRow mapRow(ResultSet resultSet, int i) throws SQLException {
			int dc = resultSet.getInt("domain_count");
            totalValue.addAndGet(dc);
            return new DomainStatRow(resultSet.getString("domain_name"), dc);
		}
	}
}
