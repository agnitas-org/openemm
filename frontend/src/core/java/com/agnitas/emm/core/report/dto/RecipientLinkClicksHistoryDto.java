/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto;

import com.agnitas.emm.core.report.generator.TextColumn;

public interface RecipientLinkClicksHistoryDto extends RecipientHistoryDto {

    @TextColumn(width = 20, translationKey = "decode.urlID", defaultValue = "URL ID")
    int getLinkId();

    void setLinkId(int linkId);

    @TextColumn(width = 200, translationKey = "URL", defaultValue = "URL")
    String getLinkUrl();

    void setLinkUrl(String linkUrl);

    @TextColumn(width = 50, translationKey = "statistic.IPAddress", defaultValue = "IP ADDRESS")
    String getIpAddress();

    void setIpAddress(String ipAddress);
}
