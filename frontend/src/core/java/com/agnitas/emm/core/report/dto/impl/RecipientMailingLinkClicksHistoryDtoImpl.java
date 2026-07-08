/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientMailingLinkClicksHistoryDto;

public class RecipientMailingLinkClicksHistoryDtoImpl implements RecipientMailingLinkClicksHistoryDto {

    private int entityId;
    private String entityName;
    private Date date;
    private int linkId;
    private String linkUrl;

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int getLinkId() {
        return linkId;
    }

    @Override
    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    @Override
    public String getLinkUrl() {
        return linkUrl;
    }

    @Override
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @Override
    public String getIpAddress() {
        return null;
    }

    @Override
    public void setIpAddress(String ipAddress) {
        // nothing to do
    }
}
