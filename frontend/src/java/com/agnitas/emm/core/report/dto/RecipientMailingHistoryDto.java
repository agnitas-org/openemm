/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto;

import java.util.Date;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;

@TextTable(translationKey = "recipient.history.mailing", defaultTitle = "MAILING HISTORY",
        order = {"SEND_DATE", "MAILING_TYPE", "SHORT_NAME", "SUBJECT", "DELIVERY_DATE", "NUMBER_OF_OPENING", "NUMBER_OF_CLICKS"})
public interface RecipientMailingHistoryDto {

    @TextColumn(width = 30, translationKey = "mailing.senddate", defaultValue = "SEND DATE", key = "SEND_DATE")
    Date getSendDate();

    void setSendDate(Date sendDate);

    @TextColumn(width = 30, translationKey = "recipient.Mailings.deliverydate", defaultValue = "DELIVERED ON", key = "DELIVERY_DATE")
    Date getDeliveryDate();

    void setDeliveryDate(Date deliveryDate);

    @TextColumn(width = 30, translationKey = "default.Type", defaultValue = "MAILING TYPE", key = "MAILING_TYPE")
    String getMailingType();

    void setMailingType(String mailingType);

    @TextColumn(width = 50, translationKey = "Mailing", defaultValue = "MAILING", key = "SHORT_NAME")
    String getShortName();

    void setShortName(String shortName);

    @TextColumn(width = 50, translationKey = "mailing.Subject", defaultValue = "SUBJECT", key = "SUBJECT")
    String getSubject();

    void setSubject(String subject);

    @TextColumn(width = 20, translationKey = "recipient.Mailings.openings", defaultValue = "NUMBER OF OPENING", key = "NUMBER_OF_OPENING")
    int getNumberOfOpenings();

    void setNumberOfOpenings(int numberOfOpenings);

    @TextColumn(width = 20, translationKey = "recipient.Mailings.clicks", defaultValue = "NUMBER OF CLICKS", key = "NUMBER_OF_CLICKS")
    int getNumberOfClicks();

    void setNumberOfClicks(int numberOfClicks);
}
