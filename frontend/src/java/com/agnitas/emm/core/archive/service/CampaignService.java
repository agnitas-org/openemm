/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;

import java.util.List;

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.web.forms.PaginationForm;

public interface CampaignService {
    PaginatedListImpl<Campaign> getOverview(Admin admin, PaginationForm form);

    List<Campaign> getCampaigns(int companyID);
    
    Campaign getCampaign(int campaignId, int companyID);

    PaginatedListImpl<MailingBase> getCampaignMailings(int campaignId, PaginationForm form, Admin admin);

    int save(Campaign campaign);

    boolean delete(Campaign campaign);

    boolean isContainMailings(int campaignId, Admin admin);

    boolean isDefinedForAutoOptimization(int campaignId, Admin admin);

    boolean copySampleCampaigns(int newCompanyId, int fromCompanyId);
}
