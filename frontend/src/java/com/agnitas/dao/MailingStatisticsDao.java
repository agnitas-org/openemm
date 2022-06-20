/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Map;

import com.agnitas.beans.Mailing;

public interface MailingStatisticsDao {
	int SEND_STATS_TEXT = 100;
	int SEND_STATS_HTML = 101;
	int SEND_STATS_OFFLINE = 102;
	
	/**
	 * This method returns the Follow-up statistics for the given mailing.
	 *
	 * @throws Exception
	 */
	int getFollowUpStat(int mailingID, int baseMailing, String followUpType, int companyID, boolean useTargetGroups) throws Exception;

	int getFollowUpStat(int followUpFor, String followUpType, int companyID, String sqlTargetExpression) throws Exception;

	Map<Integer, Integer> getSendStats(Mailing mailing, int companyId) throws Exception;
}
