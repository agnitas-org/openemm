/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.domain.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm;


@Component
public class DomainStatisticFormToDomainStatisticDtoConverter implements Converter<DomainStatisticForm, DomainStatisticDto> {
	
	@Override
	public DomainStatisticDto convert(DomainStatisticForm form) {
		DomainStatisticDto dto = new DomainStatisticDto();
		dto.setReportName(form.getReportName());
		dto.setTargetId(form.getTargetId());
		dto.setMailingListId(form.getMailingListId());
		dto.setMaxDomainNum(form.getMaxDomainNum());
		dto.setTopLevelDomain(form.isTopLevelDomain());
		return dto;
	}
}
