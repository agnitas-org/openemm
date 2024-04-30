/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import org.springframework.stereotype.Component;

import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.CalculationRecipients;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;

@Component
public class UnsavedChanges implements CalculationRecipients<CalculationRecipientsConfig> {
    private MailingStatisticsDao mailingStatisticsDao;
    private ComMailingBaseService mailingBaseService;
    private ComTargetService targetService;

    public UnsavedChanges(ComMailingBaseService mailingBaseService, ComTargetService targetService, MailingStatisticsDao mailingStatisticsDao) {
        this.mailingBaseService = mailingBaseService;
        this.targetService = targetService;
        this.mailingStatisticsDao = mailingStatisticsDao;
    }

    @Override
    public int calculate(CalculationRecipientsConfig config) throws Exception {
        if (config.getFollowUpMailing() > 0) {
            String sqlTargetExpression = targetService.getSQLFromTargetExpression(TargetExpressionUtils.makeTargetExpression(config.getTargetGroupIds(), config.isConjunction()), config.getSplitId(), config.getCompanyId());
            return mailingStatisticsDao.getFollowUpStat(config.getFollowUpMailing(), config.getFollowUpType(), config.getCompanyId(), sqlTargetExpression);
        }
        return mailingBaseService.calculateRecipients(config.getCompanyId(), config.getMailingListId(), config.getSplitId(), config.getAltgIds(), config.getTargetGroupIds(), config.isConjunction());
    }
}
