/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.util;

import com.agnitas.emm.core.Permission;
import jakarta.servlet.http.HttpServletRequest;

public final class CKEditorUtils {

    public static boolean isCKEditorTrimmed(HttpServletRequest request) throws Exception {
        return AgnUtils.allowed(request, Permission.CKEDITOR_TRIMMED);
    }

    public static boolean fullPackAllowed(HttpServletRequest request) throws Exception {
        return AgnUtils.allowed(request, Permission.CKEDITOR_EXTENDED);
    }
}
