/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.core.RowCallbackHandler;

public class RecipientStatDataSet extends RecipientsBasedDataSet {

	public List<RecipientStatusRow> getRecipientStatus(int companyID, String targetIdStr, Integer mailinglistID,
                                                       int mediaType, String language, final String hiddenFilterTargetIdStr) {
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}

		final int targetId = NumberUtils.toInt(targetIdStr, -1);
		final LightTarget target = targetId < 0 ? null : getDefaultTarget(getTarget(targetId, companyID));
		final String filterTargetSql = getHiddenTargetSql(companyID, target, hiddenFilterTargetIdStr);

		StringBuilder query = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		query.append("SELECT bind.user_status AS userstatus, COUNT(DISTINCT cust.customer_id) AS amount")
				.append(" FROM ").append(getCustomerTableName(companyID)).append(" cust")
				.append(" LEFT JOIN ").append(getCustomerBindingTableName(companyID)).append(" bind ON (cust.customer_id = bind.customer_id)")
				.append(" WHERE (bind.user_status IN (0, 1, 2, 3, 4, 5, 6, 7) OR bind.user_status IS NULL) AND cust." + RecipientStandardField.Bounceload.getColumnName() + " = 0")
				.append(" AND NOT EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind2 WHERE cust.customer_id = bind2.customer_id AND bind2.user_status < bind.user_status");
		if (mailinglistID != null){
			query.append(" AND bind2.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}
		query.append(")");

		// bounceload check is NEEDED here, because we also select customers without binding table entries
		if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
			query.append(" AND (").append(target.getTargetSQL()).append(")");
		}

		if(StringUtils.isNotBlank(filterTargetSql)) {
			query.append(" AND (").append(filterTargetSql).append(")");
		}

		if (mailinglistID != null){
			query.append(" AND bind.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}

		query.append(" GROUP BY bind.user_status");

		@SuppressWarnings("squid:S1640")
		Map<UserStatus, Integer> resultMap = new HashMap<>();
		RecipientStatusesMapCallback mapCallback = new RecipientStatusesMapCallback(resultMap);
		query(query.toString(), mapCallback, parameters.toArray(new Object[0]));

		List<RecipientStatusRow> returnList = new ArrayList<>();

		// active
		int activeCount = resultMap.getOrDefault(UserStatus.Active, 0);
		addToResultList(returnList, activeCount, "recipient.Opt_Ins", language);

		// allout
		int outCount = resultMap.getOrDefault(UserStatus.AdminOut, 0);
		outCount += resultMap.getOrDefault(UserStatus.UserOut, 0);
		outCount += resultMap.getOrDefault(UserStatus.Suspend, 0);
		addToResultList(returnList, outCount, "statistic.Opt_Outs", language);

		// bounced
		int bouncedCount = resultMap.getOrDefault(UserStatus.Bounce, 0);
		addToResultList(returnList, bouncedCount, "recipient.MailingState2", language);

		// without confirmation
		int waitForConfirmCount = resultMap.getOrDefault(UserStatus.WaitForConfirm, 0);
		addToResultList(returnList, waitForConfirmCount, "Opt_In_NotConfirmed", language);

		// blacklisted
		int blacklisedCount = resultMap.getOrDefault(UserStatus.Blacklisted, 0);
		addToResultList(returnList, blacklisedCount, "recipient.MailingState6", language);

		// no mailing
		int noMailingsCount = resultMap.getOrDefault(null, 0);
		addToResultList(returnList, noMailingsCount, "noMailinglistAssigned", language);

		return returnList;
	}

	private void addToResultList(List<RecipientStatusRow> list, int count, String key, String language){
		if (count > 0) {
			String translation = I18nString.getLocaleString(key, language);
			list.add(new RecipientStatusRow(translation, count));
		}
	}

    private class RecipientStatusesMapCallback implements RowCallbackHandler {

		private final Map<UserStatus, Integer> map;

		public RecipientStatusesMapCallback(Map<UserStatus, Integer> map) {
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			int amount = rs.getInt("amount");
			UserStatus status = getUserStatus(rs.getInt("userstatus"));
			if (status == null) {
				map.put(null, map.getOrDefault(null, 0) + amount);
			} else {
				map.put(status, amount);
			}
		}
	}
}
