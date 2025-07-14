/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.beans;

public class SubscribeWidgetSettings extends WidgetSettingsBase {

    private Integer mailinglistId;
    private Integer doiMailingId;
    private String successMessage;
    private String errorMessage;
    private String remoteAddress;
    private String referrer;

    public Integer getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(Integer mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public Integer getDoiMailingId() {
        return doiMailingId;
    }

    public void setDoiMailingId(Integer doiMailingId) {
        this.doiMailingId = doiMailingId;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
