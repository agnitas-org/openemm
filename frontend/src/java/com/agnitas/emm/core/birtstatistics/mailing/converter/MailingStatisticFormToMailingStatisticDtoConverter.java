/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;

@Component
public class MailingStatisticFormToMailingStatisticDtoConverter implements Converter<MailingStatisticForm, MailingStatisticDto> {
    @Override
    public MailingStatisticDto convert(MailingStatisticForm form) {
        MailingStatisticDto dto = new MailingStatisticDto();
        dto.setType(form.getStatisticType());
        dto.setShortname(form.getShortname());
        dto.setDateMode(form.getDateMode());
        dto.setSelectedTargets(form.getSelectedTargets());
        dto.setTopLevelDomain(form.isTopLevelDomain());
        dto.setShowNetto(form.isShowNetto());
        dto.setMaxDomains(form.getMaxDomains());
        dto.setMailingId(form.getMailingID());
        dto.setDescription(form.getDescription());
        dto.setSector(form.getSector());
        dto.setLinkId(form.getUrlID());

        return dto;
    }
}
