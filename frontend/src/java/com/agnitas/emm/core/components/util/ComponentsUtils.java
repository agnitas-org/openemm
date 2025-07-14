/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.components.util;

public final class ComponentsUtils {

    private static final String imagePattern = "%s/image?ci=%d&mi=%d&name=%s";

    private ComponentsUtils() {}

    public static String getImageUrl(final String rdirDomain, final int companyId, final int mailingId,
                                              final String componentName) {
        return String.format(imagePattern, rdirDomain, companyId, mailingId, componentName);
    }
}
