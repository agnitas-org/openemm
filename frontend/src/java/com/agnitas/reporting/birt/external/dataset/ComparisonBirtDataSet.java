/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.LightTarget;

public class ComparisonBirtDataSet extends BIRTDataSet {
 
    private static final transient Logger logger = LogManager.getLogger(ComparisonBirtDataSet.class);
 
	/**
     * Create a List of targets with the "all subscribers"-target on index 0 and the given targets after that
     *
     * @param subTargets
     * @return
     */
    protected List<LightTarget> getTargetListWithAllSubscriberTarget(List<LightTarget> subTargets) {
        List<LightTarget> result = new ArrayList<>();
        LightTarget allSubscribers = new LightTarget();
        allSubscribers.setId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
        allSubscribers.setName(CommonKeys.ALL_SUBSCRIBERS);
        allSubscribers.setTargetSQL("1 = 1");
        result.add(allSubscribers);
        if (subTargets != null) {
            result.addAll(subTargets);
        }
        return result;
    }
    
    protected boolean isMailingNotExpired(int mailingId) {
        StringBuilder countOfPeriodicallySending = new StringBuilder();
        countOfPeriodicallySending.append("SELECT COUNT(mst.mailing_id) AS count");
        countOfPeriodicallySending.append(" FROM maildrop_status_tbl mst");
        countOfPeriodicallySending.append(" JOIN mailing_tbl mt ON mst.mailing_id = mt.mailing_id");
        countOfPeriodicallySending.append(" WHERE mst.mailing_id = ?");
        countOfPeriodicallySending.append(" AND mst.status_field IN ('C', 'E', 'R', 'D')");
        countOfPeriodicallySending.append(" AND mt.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "'");
        countOfPeriodicallySending.append(" AND mst.senddate < CURRENT_DATE");

        StringBuilder countOfOnceSending = new StringBuilder();
        countOfOnceSending.append("SELECT COUNT(mst.mailing_id) AS count");
        countOfOnceSending.append(" FROM maildrop_status_tbl mst");
        countOfOnceSending.append(" WHERE mst.mailing_id = ?");
        countOfOnceSending.append(" AND mst.status_field IN ('W')");
        countOfOnceSending.append(" AND mst.senddate < CURRENT_DATE");
        countOfOnceSending.append(" AND senddate >= ?");
        
        int companyId = selectInt(logger, "SELECT company_id FROM mailing_tbl WHERE mailing_id = ?", mailingId);
        int successExpirationDays = getConfigService().getIntegerValue(ConfigValue.ExpireSuccess, companyId);
        Date successExpirationDate = DateUtilities.getDateOfDaysAgo(successExpirationDays);

        StringBuilder unitedCount = new StringBuilder();
        unitedCount.append("SELECT SUM(not_expired_mailings.count) AS total_count");
        unitedCount.append(" FROM (");
        unitedCount.append(countOfPeriodicallySending).append(" UNION ").append(countOfOnceSending);
        unitedCount.append(") not_expired_mailings");

        return selectInt(logger, unitedCount.toString(), mailingId, mailingId, successExpirationDate) > 0;
    }
}
