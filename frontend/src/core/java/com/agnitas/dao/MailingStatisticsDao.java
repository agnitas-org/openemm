/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.apache.commons.lang3.StringUtils;

public interface MailingStatisticsDao {

	int SEND_STATS_TEXT = 100;
	int SEND_STATS_HTML = 101;
	int SEND_STATS_OFFLINE = 102;

	List<DeviceClass> AVAILABLE_DEVICE_CLASSES = List.of(
			DeviceClass.DESKTOP,
			DeviceClass.MOBILE,
			DeviceClass.TABLET,
			DeviceClass.SMARTTV
	);

	static boolean isTargetFilterRequired(String targetSql) {
		return StringUtils.isNotBlank(targetSql) && !"1=1".equals(targetSql.replaceAll("\\s+", ""));
	}

	/**
	 * This method returns the Follow-up statistics for the given mailing.
	 */
	int getFollowUpRecipientsCount(int mailingID, int baseMailing, String followUpType, int companyID);

	int getFollowUpRecipientsCount(int followUpFor, String followUpType, int companyID, String sqlTargetExpression);

	Map<Integer, Integer> getSendStats(Mailing mailing, int companyId);

	int getRecipientsCount(Mailing mailing);

	Set<Integer> getRecipientsIds(Mailing mailing);

	int getSentCountFromMailingAccount(int mailingId, DateRange timestamp, Set<MaildropStatus> maildropStatuses);

	int getOpenersCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp, Set<Integer> deviceIds);

	int getOpeningsCount(int mailingId, int companyId, Set<UserType> userTypes, String targetSql, DateRange timestamp);

    int getClicksCount(int mailingId, int companyId, Set<UserType> userTypes, String targetSql, DateRange dateRange);

    int getAnonymousOpenings(int mailingId, int companyId, DateRange timestamp);

    int getHardBouncesCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	int getHardBouncesCountFromBindings(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	Optional<Integer> getMailAgeInDays(int mailingId, int companyId);

	int getOptOutsCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	int getAnonymousClicks(int mailingId, int companyId, DateRange timestamp);

	int getSentCountFromMailtrackTbl(int mailingId, int companyId, String targetSql, DateRange dateRange);

	int getSentCountForIntervalMailing(int mailingId, int companyId, String targetSql, DateRange dateRange);

	int getClickersCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	int getDeliveredCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	boolean hasSuccessTableData(int mailingId, int companyId);

	int getPeriodicallySendCount(int mailingId);

	int countOfOnceSending(int mailingId, Date thresholdDate);

	boolean isMailtrackTableExists(int companyId);

	double getRevenue(int mailingId, int companyId, String targetSql, DateRange timestamp);

	boolean isRevenueTableExists(int companyId);

	int getOpeningClickers(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

	int getNonOpeningClickers(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp);

    boolean isIntervalTrackDataExists(int mailingId, int companyId);

	boolean isTrackingAvailableForMailing(int mailingId, int companyId);

	Map<DeviceClass, Integer> getOpenersByDevice(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange dateRange);

	Map<DeviceClass, Integer> getClickersByDevice(int mailingID, int companyID, String targetSql, Set<UserType> userTypes, DateRange dateRange);
}
