/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.converter;

import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CompareMailingsFormToMailingComparisonDtoConverter implements Converter<MailingComparisonForm, MailingComparisonDto> {
    @Override
    public MailingComparisonDto convert(MailingComparisonForm form) {
        MailingComparisonDto dto = new MailingComparisonDto();
        dto.setTargetId(0); //TODO: detect where targetID is set?
        dto.setTargetIds(form.getTargetIds());
        dto.setRecipientType(form.getRecipientType());
        dto.setMailingIds(form.getBulkIds());
        dto.setReportFormat(form.getReportFormat());
        return dto;
    }
}
