/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.CalculationRecipients;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import org.springframework.stereotype.Component;

@Component
public class DefaultCalculation implements CalculationRecipients<CalculationRecipientsConfig> {

    private final MailingBaseService mailingBaseService;
    private final MailingStatisticsDao mailingStatisticsDao;

    public DefaultCalculation(MailingBaseService mailingBaseService, MailingStatisticsDao mailingStatisticsDao) {
        this.mailingBaseService = mailingBaseService;
        this.mailingStatisticsDao = mailingStatisticsDao;
    }

    @Override
    public int calculate(CalculationRecipientsConfig config) {
        if (config.getFollowUpMailing() > 0) {
            return mailingStatisticsDao.getFollowUpRecipientsCount(config.getMailingId(), config.getFollowUpMailing(), config.getFollowUpType(), config.getCompanyId());
        }
        return mailingBaseService.calculateRecipients(config.getCompanyId(), config.getMailingId());
    }
}
