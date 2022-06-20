/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.birtstatistics.recipient.converter;

import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.forms.RecipientStatisticForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecipientStatisticFormToRecipientStatisticDtoConverter implements Converter<RecipientStatisticForm, RecipientStatisticDto> {
	@Override
	public RecipientStatisticDto convert(RecipientStatisticForm form) {
		RecipientStatisticDto dto = new RecipientStatisticDto();
		dto.setReportName(StringUtils.defaultIfEmpty(form.getReportName(), "recipient_progress.rptdesign"));
		dto.setMediaType(form.getMediaType());
		dto.setTargetId(form.getTargetId());
		dto.setMailinglistId(form.getMailingListId());
		dto.setHourScale(false);

		return dto;
	}
}
