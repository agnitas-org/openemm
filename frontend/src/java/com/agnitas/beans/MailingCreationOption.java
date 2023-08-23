/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.agnitas.emm.core.Permission;

public enum MailingCreationOption {

    E_MAIL_CREATOR(Permission.MAILING_CHANGE, "/layoutbuilder/released.action", true),
    IMPORT_MAILING(Permission.MAILING_IMPORT, "/import/mailing.action", false),
    STANDART(Permission.MAILING_CLASSIC, "/mailing/templates.action", true);

    private Permission requiredPermission;
    private String redirectionUrl;
    private boolean needKeepForward;

    MailingCreationOption(Permission requiredPermission, String redirectionUrl, boolean needKeepForward) {
        this.requiredPermission = requiredPermission;
        this.redirectionUrl = redirectionUrl;
        this.needKeepForward = needKeepForward;
    }

    public Permission getRequiredPermission() {
        return requiredPermission;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public boolean isNeedKeepForward() {
        return needKeepForward;
    }
}
