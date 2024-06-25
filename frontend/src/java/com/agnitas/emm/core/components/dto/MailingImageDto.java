/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.dto;

import java.util.Date;

import org.agnitas.beans.MailingComponentType;

import com.agnitas.beans.TrackableLink;

public class MailingImageDto {
	private int id;
    private int size;
    private int present;
    private boolean mobile;
    private String name;
    private String mimeType;
    private String description;
    private Date creationDate;
    private TrackableLink link;
    private MailingComponentType type;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

    public int getPresent() {
        return present;
    }

    public void setPresent(int present) {
        this.present = present;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

    public TrackableLink getLink() {
        return link;
    }

    public void setLink(TrackableLink link) {
        this.link = link;
    }

    public MailingComponentType getType() {
        return type;
    }

    public void setType(MailingComponentType type) {
        this.type = type;
    }
}
