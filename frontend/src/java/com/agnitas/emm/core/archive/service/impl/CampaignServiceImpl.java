/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.service.impl;

import java.util.List;

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.web.forms.PaginationForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.core.archive.service.CampaignService;

public class CampaignServiceImpl implements CampaignService {
	private static final Logger logger = LogManager.getLogger(CampaignServiceImpl.class);
	
    private CampaignDao campaignDao;

    @Override
    public PaginatedListImpl<Campaign> getOverview(Admin admin, PaginationForm form) {
        return campaignDao.getOverview(admin.getCompanyID(), form.getSort(), form.ascending(), form.getPage(), form.getNumberOfRows());
    }

	@Override
	public List<Campaign> getCampaigns(int companyID) {
		return campaignDao.getCampaigns(companyID);
	}

    @Override
    public Campaign getCampaign(int campaignId, int companyID) {
        return campaignDao.getCampaign(campaignId, companyID);
    }

    @Override
    public PaginatedListImpl<MailingBase> getCampaignMailings(int campaignId, PaginationForm form, Admin admin) {
        return campaignDao.getCampaignMailings(campaignId, form, admin);
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

    @Override
    public boolean copySampleCampaigns(int newCompanyId, int fromCompanyId) {
		try {
			for (int sampleCampaignID : campaignDao.getSampleCampaignIDs(fromCompanyId)) {
				Campaign sampleCampaign = campaignDao.getCampaign(sampleCampaignID, fromCompanyId);
				sampleCampaign.setCompanyID(newCompanyId);
				campaignDao.save(sampleCampaign);
			}
			return true;
		} catch (Exception e) {
			logger.error("Cannot copy sample campaigns to company " + newCompanyId, e);
			return false;
		}
	}

    @Required
    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }
}
