/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.net.URL;
import java.util.Date;

import com.agnitas.beans.ComRecipientReaction;

public class ComRecipientReactionImpl implements ComRecipientReaction {

    private Date timestamp;
    private int mailingId;
    private String mailingName;
    private ReactionType reactionType;
    private String deviceClass;
    private String deviceName;
    private URL clickedUrl;

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getMailingId() {
        return mailingId;
    }

    @Override
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public String getMailingName() {
        return mailingName;
    }

    @Override
    public void setMailingName(String mailingName) {
        this.mailingName = mailingName;
    }

    @Override
	public ReactionType getReactionType() {
        return reactionType;
    }

    @Override
	public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    @Override
    public String getDeviceClass() {
        return deviceClass;
    }

    @Override
    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void setClickedUrl(URL clickedUrl) {
        this.clickedUrl = clickedUrl;
    }

    @Override
    public URL getClickedUrl() {
        return clickedUrl;
    }
}
