/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.service.impl;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.Admin;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.core.archive.service.CampaignService;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.web.forms.PaginationForm;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class CampaignServiceImpl implements CampaignService {

    private CampaignDao campaignDao;

    @Override
    public PaginatedListImpl<Campaign> getPaginatedList(Admin admin, PaginationForm form) {
        int sortOrder = form.getOrder().equals("desc") ? 2 : 1;
        List<Campaign> campaignList = campaignDao.getCampaignList(admin.getCompanyID(), form.getSort(), sortOrder);

        return new PaginatedListImpl<>(campaignList, campaignList.size(), form.getNumberOfRows(), form.getPage(), form.getSort(), form.getOrder());
    }

    @Override
    public Campaign getCampaign(int campaignId, int companyID) {
        return campaignDao.getCampaign(campaignId, companyID);
    }

    @Override
    public List<MailingBase> getCampaignMailings(int campaignId, Admin admin) {
        return campaignDao.getCampaignMailings(campaignId, admin);
    }

    @Override
    public int save(Campaign campaign) {
        return campaignDao.save(campaign);
    }

    @Override
    public boolean delete(Campaign campaign) {
        return campaignDao.delete(campaign);
    }

    @Override
    public boolean isContainMailings(int campaignId, Admin admin) {
        return campaignDao.isContainMailings(campaignId, admin);
    }

    @Override
    public boolean isDefinedForAutoOptimization(int campaignId, Admin admin) {
        return campaignDao.isDefinedForAutoOptimization(campaignId, admin);
    }

    @Required
    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }
}
