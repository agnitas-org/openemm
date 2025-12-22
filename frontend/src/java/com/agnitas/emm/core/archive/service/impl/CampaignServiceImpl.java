/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.Const;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CampaignServiceImpl implements CampaignService {
	private static final Logger logger = LogManager.getLogger(CampaignServiceImpl.class);
	
    private CampaignDao campaignDao;
    private BulkActionValidationService<Integer, Campaign> bulkActionValidationService;

    @Override
    public PaginatedList<Campaign> getOverview(Admin admin, PaginationForm form) {
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
    public PaginatedList<MailingBase> getCampaignMailings(int campaignId, PaginationForm form, Admin admin) {
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

    @Override
    public ServiceResult<List<Campaign>> getAllowedForDeletion(Set<Integer> ids, Admin admin) {
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getArchiveForDeletion(id, admin));
    }

    @Override
    public ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin) {
        List<Campaign> archives = ids.stream()
                .map(id -> getArchiveForDeletion(id, admin))
                .filter(ServiceResult::isSuccess)
                .map(ServiceResult::getResult)
                .toList();

        for (Campaign archive : archives) {
            delete(archive);
        }

        List<Integer> removedIds = archives.stream().map(Campaign::getId).toList();

        return ServiceResult.success(
                new UserAction(
                        "delete archives",
                        "deleted archives with following ids: " + StringUtils.join(removedIds, ", ")
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    private ServiceResult<Campaign> getArchiveForDeletion(int id, Admin admin) {
        Campaign campaign = getCampaign(id, admin.getCompanyID());
        if (campaign == null) {
            return ServiceResult.error(Message.of("error.general.missing"));
        }

        List<Message> errors = new ArrayList<>();

        if (isContainMailings(id, admin)) {
            errors.add(Message.of("warning.campaign.delete.mailing"));
        }
        if (isDefinedForAutoOptimization(id, admin)) {
            errors.add(Message.of("warning.campaign.delete.autoopt"));
        }

        if (!errors.isEmpty()) {
            return ServiceResult.error(errors);
        }

        return ServiceResult.success(campaign);
    }

    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    public void setBulkActionValidationService(BulkActionValidationService<Integer, Campaign> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }
}
