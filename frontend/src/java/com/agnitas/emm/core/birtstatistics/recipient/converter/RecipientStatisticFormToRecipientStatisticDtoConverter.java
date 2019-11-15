/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.birtstatistics.recipient.converter;

import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.forms.RecipientStatisticForm;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecipientStatisticFormToRecipientStatisticDtoConverter implements Converter<RecipientStatisticForm, RecipientStatisticDto> {
    
    @Autowired
    private HttpServletRequest request;
	
	@Override
	public RecipientStatisticDto convert(RecipientStatisticForm form) {
		RecipientStatisticDto dto = new RecipientStatisticDto();
		dto.setReportName(StringUtils.defaultIfEmpty(form.getReportName(), "recipient_progress.rptdesign"));
		dto.setMediaType(form.getMediaType());
		dto.setTargetId(form.getTargetId());
		dto.setMailinglistId(form.getMailingListId());
		dto.setHourScale(false);

		resolveDateModeDateRestrictions(dto, form);
		return dto;
	}
    
    private void resolveDateModeDateRestrictions(RecipientStatisticDto dto, RecipientStatisticForm form) {
	    switch (form.getDateMode()) {
            case LAST_WEEK:
                LocalDate now = LocalDate.now();
                dto.setLocalEndDate(now);
                dto.setLocalStartDate(now.minusWeeks(1));
                break;
            
            case SELECT_PERIOD:
                ComAdmin admin = AgnUtils.getAdmin(request);
                assert admin != null;
                dto.setLocalStartDate(form.getStartDate().get(admin.getDateFormatter()));
                dto.setLocalEndDate(form.getEndDate().get(admin.getDateFormatter()));
                break;
                
            case SELECT_MONTH:
            default:
                LocalDate dateByParams = LocalDate.of(form.getYear(), form.getMonthValue(), 1);
                dto.setLocalStartDate(dateByParams);
                dto.setLocalEndDate(dateByParams.plusMonths(1));
        }
    }
}
