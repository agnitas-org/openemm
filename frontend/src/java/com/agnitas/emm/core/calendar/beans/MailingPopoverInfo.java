/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.beans;

public class MailingPopoverInfo {
    
    private int mailingId;
    private int sentCount;
    private int openers;
    private int clickers;
    private boolean post;
    private String subject;
    private int thumbnailComponentId;

    public MailingPopoverInfo(int mailingId, int sentCount, int openers, int clickers, String subject, int thumbnailComponentId, boolean post) {
        this.mailingId = mailingId;
        this.sentCount = sentCount;
        this.openers = openers;
        this.clickers = clickers;
        this.subject = subject;
        this.thumbnailComponentId = thumbnailComponentId;
        this.post = post;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(int sentCount) {
        this.sentCount = sentCount;
    }

    public int getOpeners() {
        return openers;
    }

    public void setOpeners(int openers) {
        this.openers = openers;
    }

    public int getClickers() {
        return clickers;
    }

    public void setClickers(int clickers) {
        this.clickers = clickers;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(boolean post) {
        this.post = post;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getThumbnailComponentId() {
        return thumbnailComponentId;
    }

    public void setThumbnailComponentId(int thumbnailComponentId) {
        this.thumbnailComponentId = thumbnailComponentId;
    }
}
