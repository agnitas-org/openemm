/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.services;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.emm.core.report.bean.RecipientBindingHistory;
import com.agnitas.emm.core.report.bean.RecipientEntity;
import net.sf.json.JSONArray;

public interface RecipientReportService {

    /**
     * Represents change history of recipient binding. In other words, change history of bindings
     * between the recipient and mailing lists.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return list with binding changes for current recipient.
     */
    List<RecipientBindingHistory> getBindingHistory(int recipientId, int companyId);

    /**
     * Represents change history of recipient profile fields.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return list with profile changes for current recipient.
     */
    List<ComRecipientHistory> getProfileHistory(int recipientId, int companyId);

    /**
     * Represents merged history of profile changes and binding changes for current recipient.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return list with profile changes for current recipient.
     */
    List<ComRecipientHistory> getStatusHistory(int recipientId, int companyId);

    /**
     * Represents history of already sent out mailings like count of clicks, openings etc.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return list with already sent out mailings statistic.
     */
    List<ComRecipientMailing> getMailingHistory(int recipientId, int companyId);

    /**
     * Represents history of user changes.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return tracking history for current recipient.
     */
    List<WebtrackingHistoryEntry> getRetargetingHistory(int recipientId, int companyId);

    /**
     * Represent history of devices which used for viewing mail by current recipient.
     * Also includes actions which describe what recipient have done with the mail
     * and information about operation system.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return list of recipient's devices and actions.
     */
    List<ComRecipientReaction> getDeviceHistory(int recipientId, int companyId);

    /**
     * Represent common and specific customer's information.
     *
     * @param recipientId identifier of recipient.
     * @param companyId   identifier of company.
     * @return information about recipient.
     */
    RecipientEntity getRecipientInfo(int recipientId, int companyId);

    Map<String, Integer> getRecipientRemarksStat(int mailinglistId, int targetId, int companyId);

    JSONArray getFilteredRemarksJson(Map<String, Integer> remarks, boolean summary);

    byte[] getRecipientRemarksCSV(Admin admin, int mailingListId, int targetId) throws Exception;
}
