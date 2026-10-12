/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.converter;

import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// TODO: GWUA-7067: Remove after migration
@Component
public class BirtReportToBirtReportStatisticConverter implements Converter<BirtReport, BirtReportStatisticDto> {

    @Override
    public BirtReportStatisticDto convert(BirtReport source) {
        BirtReportSettings settings = source.getActiveReportSetting();

        return new BirtReportStatisticDto(
                source.getId(),
                settings.getReportName(source.getFormat()),
                source.getFormat(),
                settings.getReportUrlParameters()
        );
    }
}
