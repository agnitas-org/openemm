/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import com.agnitas.emm.common.MailingType;

/**
 * POJO containing information about mailing sent to recipient
 */
public interface ComRecipientMailing {

    int getMailingId();

    void setMailingId(int mailingId);

    Date getSendDate();

    void setSendDate(Date sendDate);

    Date getDeliveryDate();

    void setDeliveryDate(Date deliveryDate);

    MailingType getMailingType();

    void setMailingType(MailingType mailingType);

    String getShortName();

    void setShortName(String shortName);

    String getSubject();

    void setSubject(String subject);

    int getNumberOfOpenings();

    void setNumberOfOpenings(int numberOfOpenings);

    int getNumberOfClicks();

    void setNumberOfClicks(int numberOfClicks);

    int getSendCount();
    
	void setSendCount(int sendCount);
	
}
