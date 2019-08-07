/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto.converter;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;

import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.web.forms.ComMailingBaseForm;

public class ComMailingBaseFormToCalculationRecipientConfigConverter implements Converter<ComMailingBaseForm, CalculationRecipientsConfig> {

    @Override
    public CalculationRecipientsConfig convert(ComMailingBaseForm form) {
        CalculationRecipientsConfig config = new CalculationRecipientsConfig();

        config.setMailingId(form.getMailingID());
        config.setAssignTargetGroups(form.getAssignTargetGroups());
        config.setFollowUpMailing(NumberUtils.toInt(form.getFollowMailing(),0));
        config.setFollowUpType(form.getFollowUpMailingType());
        config.setMailingListId(form.getMailinglistID());
        config.setTargetGroupIds(form.getTargetGroups());
        config.setChangeMailing(form.isChangeMailing());

        return config;
    }
}
