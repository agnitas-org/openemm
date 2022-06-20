/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean;

import java.util.Date;

import org.agnitas.beans.Recipient;

public interface MailingRecipientStatRow {

    String getTitle();

    String getFirstName();

    String getLastName();

    String getEmail();

    Date getReceiveTime();

    Date getOpenTime();

    int getOpeningsCount();

    Date getClickTime();

    int getClicksCount();

    Date getBounceTime();

    Date getUnsubscribeTime();

    Recipient getRecipient();

    void setRecipient(Recipient recipient);
    
    Object getVal(String col);

    void setVal(String col, String val);
}
