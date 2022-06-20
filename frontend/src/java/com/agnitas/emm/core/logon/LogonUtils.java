/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.util.WebUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.logon.beans.LogonState;
import com.agnitas.emm.core.logon.beans.LogonStateBundle;

public class LogonUtils {
    private static final String BUNDLE_KEY = "com.agnitas.emm.logon.TEMPORARY_LOGON_DATA";

    public static LogonStateBundle getBundle(HttpServletRequest request, boolean create) {
        Object object = WebUtils.getSessionAttribute(request, BUNDLE_KEY);

        if (object instanceof LogonStateBundle) {
            return (LogonStateBundle) object;
        }

        if (create) {
            LogonStateBundle bundle = new LogonStateBundle(LogonState.PENDING);
            WebUtils.setSessionAttribute(request, BUNDLE_KEY, bundle);
            return bundle;
        }

        return null;
    }

    public static ComAdmin getAdmin(HttpServletRequest request) {
        LogonStateBundle bundle = getBundle(request, false);

        if (bundle == null) {
            return null;
        }

        return bundle.getAdmin();
    }
 }
