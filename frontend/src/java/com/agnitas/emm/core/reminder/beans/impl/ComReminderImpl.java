/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.reminder.beans.impl;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.reminder.beans.ComReminder;

public class ComReminderImpl implements ComReminder {
    private int id;
    private int companyId;
    private String recipientEmails;
    private String message;
    private String lang;
    private String senderName;
    private String title;
    private boolean sent;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public String getRecipientEmail() {
        return recipientEmails;
    }

    @Override
    public void setRecipientEmail(String recipientEmails) {
        this.recipientEmails = recipientEmails;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String getSenderName() {
        return senderName;
    }

    @Override
    public void setSenderName(String recipientName) {
        this.senderName = recipientName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isSent() {
        return sent;
    }

    @Override
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + companyId;
		result = prime * result + id;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((recipientEmails == null) ? 0 : recipientEmails.hashCode());
		result = prime * result + ((senderName == null) ? 0 : senderName.hashCode());
		result = prime * result + (sent ? 1231 : 1237);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComReminderImpl)) return false;

        ComReminderImpl that = (ComReminderImpl) o;

        return that.id == id
                && that.companyId == companyId
                && that.sent == sent
                && StringUtils.equals(that.lang, lang)
                && StringUtils.equals(that.message, message)
                && StringUtils.equals(that.recipientEmails, recipientEmails)
                && StringUtils.equals(that.senderName, senderName)
                && StringUtils.equals(that.title, title);
    }
}
