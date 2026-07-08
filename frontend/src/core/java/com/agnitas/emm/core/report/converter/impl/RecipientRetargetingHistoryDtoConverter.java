/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.emm.core.report.converter.CollectionConverter;
import com.agnitas.emm.core.report.dto.RecipientRetargetingHistoryDto;
import com.agnitas.emm.core.report.dto.impl.RecipientRetargetingHistoryDtoImpl;

@Component
public class RecipientRetargetingHistoryDtoConverter implements CollectionConverter<WebtrackingHistoryEntry, RecipientRetargetingHistoryDto> {

    @Override
    public RecipientRetargetingHistoryDto convert(WebtrackingHistoryEntry webtrackingHistory) {
        RecipientRetargetingHistoryDto retargetingHistory = new RecipientRetargetingHistoryDtoImpl();

        retargetingHistory.setDate(webtrackingHistory.getDate());
        String title = String.format("%s (%d)", webtrackingHistory.getMailingName(), webtrackingHistory.getMailingID());
        retargetingHistory.setMailingTitle(title);
        retargetingHistory.setTrackingPoint(webtrackingHistory.getName());
        String value = Objects.nonNull(webtrackingHistory.getValue()) ? webtrackingHistory.getValue().toString() : null;
        retargetingHistory.setValue(StringUtils.defaultString(value));
        retargetingHistory.setIp(webtrackingHistory.getIpAddress());

        return retargetingHistory;
    }
}
