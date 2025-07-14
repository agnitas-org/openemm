/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.beans;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.common.MailingStatus;

public class CalendarUnsentMailing {

    private int mailingId;
    private int thumbnailComponentId;
    private boolean post;
    private String shortname;
    private String subject;
    private MediaTypes mediatype;
    private MailingStatus status;

    public CalendarUnsentMailing(int mailingId, int thumbnailComponentId, boolean post, String shortname, String subject, MediaTypes mediatype, MailingStatus status) {
        this.mailingId = mailingId;
        this.thumbnailComponentId = thumbnailComponentId;
        this.post = post;
        this.shortname = shortname;
        this.subject = subject;
        this.mediatype = mediatype;
        this.status = status;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public int getThumbnailComponentId() {
        return thumbnailComponentId;
    }

    public void setThumbnailComponentId(int thumbnailComponentId) {
        this.thumbnailComponentId = thumbnailComponentId;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(boolean post) {
        this.post = post;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MediaTypes getMediatype() {
        return mediatype;
    }

    public void setMediatype(MediaTypes mediatype) {
        this.mediatype = mediatype;
    }

    public MailingStatus getStatus() {
        return status;
    }

    public void setStatus(MailingStatus status) {
        this.status = status;
    }
}
