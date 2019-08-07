/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientMailingHistoryDto;

public class RecipientMailingHistoryDtoImpl implements RecipientMailingHistoryDto {

    private Date sendDate;

    private Date deliveryDate;

    private String mailingType;

    private String shortName;

    private String subject;

    private int numberOfOpenings;

    private int numberOfClicks;

    @Override
    public Date getSendDate() {
        return sendDate;
    }

    @Override
    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    @Override
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public String getMailingType() {
        return mailingType;
    }

    @Override
    public void setMailingType(String mailingType) {
        this.mailingType = mailingType;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public int getNumberOfOpenings() {
        return numberOfOpenings;
    }

    @Override
    public void setNumberOfOpenings(int numberOfOpenings) {
        this.numberOfOpenings = numberOfOpenings;
    }

    @Override
    public int getNumberOfClicks() {
        return numberOfClicks;
    }

    @Override
    public void setNumberOfClicks(int numberOfClicks) {
        this.numberOfClicks = numberOfClicks;
    }
}
