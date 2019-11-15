/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.converter;

import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.BirtStatForm;
import org.agnitas.util.AgnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MailingStatisticFormToMailingStatisticDtoConverter implements Converter<BirtStatForm, MailingStatisticDto> {

    private final HttpServletRequest request;

    @Autowired
    public MailingStatisticFormToMailingStatisticDtoConverter(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public MailingStatisticDto convert(final BirtStatForm form) {
        final MailingStatisticDto dto = new MailingStatisticDto();
        dto.setReportName(form.getReportName());
        dto.setType(StatisticType.getByCode(form.getReportName()));
        dto.setShortname(form.getShortname());
        dto.setDateMode(form.getDateMode());
        dto.setSelectedTargets(form.getSelectedTargets());
        dto.setTopLevelDomain(form.isTopLevelDomain());
        dto.setShowNetto(form.isShowNetto());
        dto.setMaxDomains(form.getMaxDomains());
        dto.setMailingId(form.getMailingID());

        ComAdmin admin = AgnUtils.getAdmin(request);
        assert admin != null;
        final DateTimeFormatter dateFormatter = admin.getDateFormatter();

        dto.setStartDate(form.getStartDate().get(dateFormatter));
        dto.setEndDate(form.getEndDate().get(dateFormatter));

        if(form.getDateMode() == DateMode.LAST_TENHOURS || form.getDateMode() == DateMode.SELECT_DAY){
            dto.setHourScale(true);
        } else {
            dto.setHourScale(false);
        }
        return dto;
    }
}
