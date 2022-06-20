/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.service.ComMailingLightService;

public class ComMailingLightServiceImpl implements ComMailingLightService {
    private ComMailingDao mailingDao;
    private ComCompanyDao companyDao;
    private MaildropService maildropService;

    @Override
    public List<LightweightMailingWithMailingList> getLightweightMailings(@VelocityCheck int companyID, int adminId,
                                                                          int parentMailingId, int mailingId) {

        List<Mailing> mailings = mailingDao.getMailings(companyID, adminId, TAKE_ALL_MAILINGS, "W", true);

        List<LightweightMailingWithMailingList> results = mailings.stream()
                .filter(mailing -> mailing.getDeleted() == 0
                        || maildropService.isActiveMailing(mailing.getId(), companyID))
                .map(mailing -> new LightweightMailingWithMailingList(mailing, mailing.getMailinglistID()))
                .collect(Collectors.toList());

        boolean isParentIdNotInResultsList = results.stream()
                .mapToInt(LightweightMailingWithMailingList::getMailingListId)
                .noneMatch(id -> id == parentMailingId);

        if (parentMailingId > 0 && parentMailingId != mailingId && isParentIdNotInResultsList) {
            Mailing parentMailing = mailingDao.getMailing(parentMailingId, companyID);
            results.add(new LightweightMailingWithMailingList(parentMailing, parentMailing.getMailinglistID()));
        }
        return results;
    }

    @Override
    public boolean isMailtrackingActive(@VelocityCheck int companyID) {
        return companyDao.isMailtrackingActive(companyID);
    }

    @Override
	@Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Override
	@Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Override
	@Required
    public void setMaildropService(final MaildropService service) {
        this.maildropService = service;
    }
}
