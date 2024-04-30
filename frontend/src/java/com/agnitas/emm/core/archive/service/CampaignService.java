/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.service;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.Admin;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.web.forms.PaginationForm;

import java.util.List;

public interface CampaignService {

    PaginatedListImpl<Campaign> getPaginatedList(Admin admin, PaginationForm form);

    Campaign getCampaign(int campaignId, int companyID);

    List<MailingBase> getCampaignMailings(int campaignId, Admin admin);

    int save(Campaign campaign);

    boolean delete(Campaign campaign);

    boolean isContainMailings(int campaignId, Admin admin);

    boolean isDefinedForAutoOptimization(int campaignId, Admin admin);

}
