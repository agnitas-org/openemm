/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto;

import java.util.Date;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;

@TextTable(translationKey = "stat.impression.retargeting", defaultTitle = "RETARGETING HISTORY",
        order = {"dete", "title", "point", "value", "ip"})
public interface RecipientRetargetingHistoryDto {

    @TextColumn(width = 30, translationKey = "settings.fieldType.DATE", defaultValue = "DATE", key = "dete")
    Date getDate();

    void setDate(Date date);

    @TextColumn(width = 50, translationKey = "mailing.searchName", defaultValue = "MAILING TITLE", key = "title")
    String getMailingTitle();

    void setMailingTitle(String mailingTitle);

    @TextColumn(width = 30, translationKey = "deeptracking.trackpoint", defaultValue = "TRACKING POINT", key = "point")
    String getTrackingPoint();

    void setTrackingPoint(String trackingPoint);

    @TextColumn(width = 30, translationKey = "Value", defaultValue = "VALUE", key = "value")
    String getValue();

    void setValue(String value);

    @TextColumn(translationKey = "statistic.IPAddress", defaultValue = "IP ADDRESS", key = "ip")
    String getIp();

    void setIp(String ipAddress);
}
