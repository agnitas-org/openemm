/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.Date;

public class TimeBasedDeliveryStatRow {
	
	private int mailNum;
	private String sendTimeDisplay;
    private Date sendTime;

    public int getMailNum() {
        return mailNum;
    }

    public void setMailNum(int mailNum) {
        this.mailNum = mailNum;
    }

    public String getSendTimeDisplay() {
        return sendTimeDisplay;
    }

    public void setSendTimeDisplay(String sendTime) {
        this.sendTimeDisplay = sendTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
}
