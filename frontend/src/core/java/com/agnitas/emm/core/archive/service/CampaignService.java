/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.archive.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.forms.PaginationForm;

public interface CampaignService {

    PaginatedList<Campaign> getOverview(Admin admin, PaginationForm form);

    List<Campaign> getCampaigns(int companyID);
    
    Campaign getCampaign(int id, int companyID);

    Campaign getOrNull(int id, int companyID);

    boolean exists(int id, int companyId);

    Optional<String> findName(int id, int companyId);

    PaginatedList<MailingBase> getCampaignMailings(int campaignId, PaginationForm form, Admin admin);

    int save(Campaign campaign);

    boolean delete(Campaign campaign);

    ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);

    boolean copySampleCampaigns(int newCompanyId, int fromCompanyId);

    ServiceResult<List<Campaign>> getAllowedForDeletion(Set<Integer> ids, Admin admin);
}
