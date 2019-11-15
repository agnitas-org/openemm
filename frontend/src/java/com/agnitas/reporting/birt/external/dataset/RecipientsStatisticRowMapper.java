/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class RecipientsStatisticRowMapper implements RowMapper<RecipientsStatisticRow> {

	@Override
	public RecipientsStatisticRow mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		RecipientsStatisticRow row = new RecipientsStatisticRow();
		row.setMailingListId(resultSet.getInt("mailinglist_id"));
		row.setMailingListName(resultSet.getString("mailinglist_name"));
		row.setMailingListGroupId(resultSet.getInt("mailinglist_group_id"));
		row.setTargetGroupId(resultSet.getInt("targetgroup_id"));
		row.setTargetGroupName(resultSet.getString("targetgroup_name"));
		row.setCountTypeText(resultSet.getInt("count_type_text"));
		row.setCountTypeHtml(resultSet.getInt("count_type_html"));
		row.setCountTypeOfflineHtml(resultSet.getInt("count_type_offline_html"));
		row.setCountActive(resultSet.getInt("count_active"));
		row.setCountActiveForPeriod(resultSet.getInt("count_active_for_period"));
		row.setCountWaitingForConfirm(resultSet.getInt("count_waiting_for_confirm"));
		row.setCountBlacklisted(resultSet.getInt("count_blacklisted"));
		row.setCountOptout(resultSet.getInt("count_optout"));
		row.setCountBounced(resultSet.getInt("count_bounced"));
		row.setCountGenderMale(resultSet.getInt("count_gender_male"));
		row.setCountGenderFemale(resultSet.getInt("count_gender_female"));
		row.setCountGenderUnknown(resultSet.getInt("count_gender_unknown"));
		row.setCountRecipient(resultSet.getInt("count_recipient"));
		row.setCountTargetGroup(resultSet.getInt("count_target_group"));
		row.setCountActiveAsOf(resultSet.getInt("count_active_as_of"));
		row.setCountBlacklistedAsOf(resultSet.getInt("count_blacklisted_as_of"));
		row.setCountOptoutAsOf(resultSet.getInt("count_optout_as_of"));
		row.setCountBouncedAsOf(resultSet.getInt("count_bounced_as_of"));
		row.setCountWaitingForConfirmAsOf(resultSet.getInt("count_waiting_for_confirm_as_of"));
		row.setCountRecipientAsOf(resultSet.getInt("count_recipient_as_of"));

		return row;
	}
}
