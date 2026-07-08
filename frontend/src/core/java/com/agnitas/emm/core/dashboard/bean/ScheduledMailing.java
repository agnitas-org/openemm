/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.bean;

import java.util.Date;

public class ScheduledMailing {

    private int id;
    private String shortname;
    private String workstatus;
    private String workstatusIn;
    private Date maildropSendDate;
    private String mailinglistName;
    private String sendDate;
    private String sendTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getWorkstatus() {
        return workstatus;
    }

    public void setWorkstatus(String workstatus) {
        this.workstatus = workstatus;
    }

    public String getWorkstatusIn() {
        return workstatusIn;
    }

    public void setWorkstatusIn(String workstatusIn) {
        this.workstatusIn = workstatusIn;
    }

    public Date getMaildropSendDate() {
        return maildropSendDate;
    }

    public void setMaildropSendDate(Date maildropSendDate) {
        this.maildropSendDate = maildropSendDate;
    }

    public String getMailinglistName() {
        return mailinglistName;
    }

    public void setMailinglistName(String mailinglistName) {
        this.mailinglistName = mailinglistName;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
