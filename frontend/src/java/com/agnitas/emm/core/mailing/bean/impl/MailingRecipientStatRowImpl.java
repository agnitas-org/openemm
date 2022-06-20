/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean.impl;

import java.util.Date;

import org.agnitas.beans.Recipient;

import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;

public class MailingRecipientStatRowImpl implements MailingRecipientStatRow {
    private Recipient recipient;

    @Override
    public String getTitle() {
        return recipient.getCustParametersNotNull("title");
    }

    @Override
    public String getFirstName() {
        return recipient.getFirstname();
    }

    @Override
    public String getLastName() {
        return recipient.getLastname();
    }

    @Override
    public String getEmail() {
        return recipient.getEmail();
    }

    @Override
    public Date getReceiveTime() {
        return (Date) recipient.getCustParameters().get("receive_time");
    }

    @Override
    public Date getOpenTime() {
        return (Date) recipient.getCustParameters().get("open_time");
    }

    @Override
    public int getOpeningsCount() {
        return recipient.getCustParametersNotNull("openings").isEmpty() ? 0 : Integer.parseInt(recipient.getCustParametersNotNull("openings"));
    }

    @Override
    public Date getClickTime()  {
        return (Date) recipient.getCustParameters().get("click_time");
    }

    @Override
    public int getClicksCount() {
        return recipient.getCustParametersNotNull("clicks").isEmpty() ? 0 : Integer.parseInt(recipient.getCustParametersNotNull("clicks"));
    }

    @Override
    public Date getBounceTime()  {
       return (Date) recipient.getCustParameters().get("bounce_time");
    }

    @Override
    public Date getUnsubscribeTime(){

        return (Date) recipient.getCustParameters().get("optout_time");
    }

    @Override
	public Recipient getRecipient() {
        return recipient;
    }

    @Override
	public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }
    
    @Override
    public Object getVal(String col) {
        return recipient.getCustParameters().get(col);
    }
    
    @Override
    public void setVal(String col, String val) {
        recipient.getCustParameters().put(col, val);
    }
}
