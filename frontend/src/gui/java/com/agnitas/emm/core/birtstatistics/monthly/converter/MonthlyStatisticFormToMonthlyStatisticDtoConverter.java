/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticFormOld;

@Component
// TODO: GWUA-6883: remove when deleting Monthly.rptdesign
public class MonthlyStatisticFormToMonthlyStatisticDtoConverter implements Converter<MonthlyStatisticFormOld, MonthlyStatisticDto> {
	
	@Override
	public MonthlyStatisticDto convert(MonthlyStatisticFormOld form) {
		MonthlyStatisticDto monthlyStatistic = new MonthlyStatisticDto();
		monthlyStatistic.setReportName(form.getReportName());
		monthlyStatistic.setStartMonth(form.getStartMonth());
		monthlyStatistic.setStartYear(form.getStartYear());
		monthlyStatistic.setTop10MetricsId(form.getTop10MetricsId());
		return monthlyStatistic;
	}
}
