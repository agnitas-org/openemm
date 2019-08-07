/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.LightweightMailingImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComMailing;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.service.ComMailingLightVO;

public class ComMailingLightVOImpl implements ComMailingLightVO {
    private ComMailingDao mailingDao;
    private ComCompanyDao companyDao;
    private MaildropService maildropService;

    @Override
    public LightweightMailing getSnowflakeMailing(int mailingID) {
        return mailingDao.getLightweightMailing(mailingID);
    }

    @Override
    public List<LightweightMailing> getSnowflakeMailings(@VelocityCheck int companyID) {
        // holds the lightweight Mailings.
        List<LightweightMailing> results = new ArrayList<>();

        // get all world-mailings.
        List<ComMailing> mailings = mailingDao.getMailings(companyID, TAKE_ALL_SNOWFLAKE_MAILINGS, "W", true);

        // loop over all Mailings, get the important Informations and put it into returnlist
        for (ComMailing mailing : mailings) {
            if (mailing.getDeleted() == 0 || maildropService.isActiveMailing(mailing.getId(), companyID)) {
                // the constructor sets the needed values. you can also call "compress..."
                // on ComSnowflakeMailingImpl instead.
                results.add(new LightweightMailingImpl(mailing));
            }
        }

        return results;
    }

    @Override
    public boolean isMailtrackingActive(@VelocityCheck int companyID) {
        return companyDao.isMailtrackingActive(companyID);
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Required
    public void setMaildropService(final MaildropService service) {
        this.maildropService = service;
    }
}
