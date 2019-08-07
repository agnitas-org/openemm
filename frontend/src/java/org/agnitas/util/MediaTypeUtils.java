/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

public class MediaTypeUtils {
    
    public static final String HTML_TAG = "<html>";
    public static final String DOCTYPE_HTML_TAG = "<!DOCTYPE html>";
    
    public static final String TXT_MEDIATYPE = "application/txt";
    
    
    public static String resolveContentType(String reportContent) {
        if (StringUtils.startsWith(reportContent, HTML_TAG) || StringUtils.startsWith(reportContent, DOCTYPE_HTML_TAG)) {
            return MediaType.TEXT_HTML_VALUE;
        } else {
            return TXT_MEDIATYPE;
        }
    }
    
    public static boolean isHtmlContentType(String contentType) {
        return StringUtils.equalsIgnoreCase(contentType, MediaType.TEXT_HTML_VALUE);
    }
}
