/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.web;

import java.text.MessageFormat;

import com.agnitas.web.mvc.Popups;
import org.apache.log4j.Logger;

public class AbstractAdminControllerBase {

    private static final transient Logger LOGGER = Logger.getLogger(AbstractAdminControllerBase.class);

    protected String prepareErrorPageForNotLoadedAdmin(final int adminId, final int companyID, final Popups popups,
                                                       final String viewName) {
        popups.alert("Error");
        LOGGER.warn(MessageFormat.format("Could not load admin by admin id: {0}, company id: {1}.", adminId, companyID));
        return viewName;
    }
}
