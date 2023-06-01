/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;

import net.sf.json.JSONArray;

public interface MailinglistService {

	void bulkDelete(Set<Integer> mailinglistIds, @VelocityCheck int companyId);

	List<Mailing> getAllDependedMailing(Set<Integer> mailinglistIds, @VelocityCheck int companyId);

	void deleteRecipientBindings(Set<Integer> mailinglistIds, @VelocityCheck int companyId);

	List<ComLightweightBirtReport> getConnectedBirtReportList(int mailinglistId, @VelocityCheck int companyId);

	List<Mailinglist> getMailinglists(@VelocityCheck int companyId);

	Mailinglist getMailinglist(int mailinglistId, @VelocityCheck int companyId);

	boolean exist(int mailinglistId, @VelocityCheck int companyId);

	boolean existAndEnabled(Admin admin, int mailingListId);

	boolean isFrequencyCounterEnabled(Admin admin, int mailingListId);

	String getMailinglistName(int mailinglistId, @VelocityCheck int companyId);

	List<Mailinglist> getAllMailingListsNames(@VelocityCheck int companyId);
	
	int saveMailinglist(@VelocityCheck int companyId, MailinglistDto mailinglist);
	
	boolean isShortnameUnique(String newShortname, int mailinglistId, @VelocityCheck int companyId);
    
    @Transactional
    boolean deleteMailinglist(int mailinglistId, int companyId);

    void deleteMailinglistBindingRecipients(@VelocityCheck int companyId, int mailinglistId, boolean onlyActiveUsers, boolean withoutAdminAndTestUsers);

	JSONArray getMailingListsJson(Admin admin);

    boolean mailinglistDeleted(int mailinglistId, int companyId);

    Mailinglist getDeletedMailinglist(int mailinglistId, int companyId);

    List<Mailing> getUsedMailings(Set<Integer> mailinglistIds, int companyId);

    int getSentMailingsCount(int mailinglistId, int companyId);

    int getAffectedReportsCount(int mailinglistId, int companyId);
}
