/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

public class StringUtils {

    private StringUtils() {

    }

    /**
     * Search for parameters in Mediatype param-string (method is copied from AngUtils)
     * @param paramName the name of param to search
     * @param paramList the string with all params
     * @return the value of param
     */
    public static String findParam(String paramName, String paramList) {
        String result = null;
        if(paramName != null) {
            int posA = paramList.indexOf(paramName+"=\"");
            if(posA != -1) {
                int posB = paramList.indexOf("\",", posA);
                if(posB != -1) {
                    result = paramList.substring(posA+paramName.length()+2, posB);
                    result = result.replace("\\=", "=");
                    result = result.replace("\\\"", "\"");
                    result = result.replace("\\,", ",");
                }
            }
        }
        return result;
    }
	
}
