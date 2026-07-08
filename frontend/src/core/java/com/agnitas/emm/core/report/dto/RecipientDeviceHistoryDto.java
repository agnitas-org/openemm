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

@TextTable(translationKey = "recipient.history.device", defaultTitle = "END DEVICE HISTORY",
        order = {"dateColumn", "mailingColumn", "actionColumn", "typeColumn", "nameColumn"})
public interface RecipientDeviceHistoryDto {

    @TextColumn(width = 30, translationKey = "Date", defaultValue = "DATE", key = "dateColumn")
    Date getDate();

    void setDate(Date date);

    @TextColumn(width = 50, translationKey = "Mailing", defaultValue = "MAILING", key = "mailingColumn")
    String getMailingDescription();

    void setMailingDescription(String mailingDescription);

    @TextColumn(translationKey = "action.Action", defaultValue = "ACTION", key = "actionColumn")
    String getActionDescription();

    void setActionDescription(String actionDescription);

    @TextColumn(width = 30, translationKey = "recipient.deviceType", defaultValue = "END DEVICE TYPE", key = "typeColumn")
    String getDeviceType();

    void setDeviceType(String deviceType);

    @TextColumn(width = 30, translationKey = "statistic.device_name", defaultValue = "END DEVICE NAME", key = "nameColumn")
    String getDeviceName();

    void setDeviceName(String deviceName);
}
