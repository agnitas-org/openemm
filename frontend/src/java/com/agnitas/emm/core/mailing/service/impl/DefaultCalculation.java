/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import org.springframework.stereotype.Component;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.CalculationRecipients;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;

@Component
public class DefaultCalculation implements CalculationRecipients<CalculationRecipientsConfig> {

    private ComMailingBaseService mailingBaseService;
    private ComMailingDao mailingDao;

    public DefaultCalculation(ComMailingBaseService mailingBaseService, ComMailingDao mailingDao) {
        this.mailingBaseService = mailingBaseService;
        this.mailingDao = mailingDao;
    }

    @Override
    public int calculate(CalculationRecipientsConfig config) throws Exception {
        if (config.getFollowUpMailing() > 0) {
            return mailingDao.getFollowUpStat(config.getMailingId(), config.getFollowUpMailing(), config.getFollowUpType(), config.getCompanyId(), true);
        }
        return mailingBaseService.calculateRecipients(config.getCompanyId(), config.getMailingId());
    }
}
