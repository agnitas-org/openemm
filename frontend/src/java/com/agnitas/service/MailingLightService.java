/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;

import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList;

/**
 * This service class has following task:
 * Get all Mailings from DB (over the DAO) and put the result into
 * a new Data-Structure consisting of Snowflake-Mailings (lightweight mailings).
 */
public interface MailingLightService {

    int TAKE_ALL_MAILINGS = 0;

    /**
     * this method returns lightweight Mailings without unneeded
     * informations like Attachements and so on.
     *
     * @return a list of lightweight mailings.
     */
    List<LightweightMailingWithMailingList> getLightweightMailings(int companyID, int adminId,
                                                                   int parentMailingId, int mailingId);

    /**
    * Check if the mailtracking is active for this company.
    * @return {@code true} if mailtracking is active for this company or {@code false} otherwise.
    */

    boolean isMailtrackingActive(int companyID);

    void setMailingDao(MailingDao mailingDao);
    void setCompanyDao(CompanyDao companyDao);
    void setMaildropService(MaildropService service);
}
