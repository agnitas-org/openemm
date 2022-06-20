/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.beans;

import java.util.Date;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;

@TextTable(order = {"EMAIL_KEY", "REASON_KEY", "DATE_KEY"})
public class BlacklistDto {
    @TextColumn(translationKey = "mailing.MediaType.0", defaultValue = "Email", key = "EMAIL_KEY")
    private String email;

    @TextColumn(translationKey = "blacklist.reason", defaultValue = "Reason", key = "REASON_KEY")
    private String reason;

    @TextColumn(translationKey = "Date", defaultValue = "Date", key = "DATE_KEY")
    private Date date;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
