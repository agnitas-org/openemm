/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.recipientsreport.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

public final class RecipientReportUtils {
    
    private static final String HTML_TAG = "<html>";
    private static final String DOCTYPE_HTML_TAG = "<!DOCTYPE html>";
    
    public static final String TXT_EXTENSION = ".txt";
    public static final String HTML_EXTENSION = ".html";
    
	public static final String IMPORT_RESULT_FILE_PREFIX = "ImportResult";
	public static final String INVALID_RECIPIENTS_FILE_PREFIX = "InvalidRecipients";
	public static final String AUTO_EXPORT_ERROR_FILE_NAME = "AutoExport Error";
	public static final String AUTO_IMPORT_ERROR_FILE_NAME = "AutoImport Error";
    
    public static String resolveFileName(String filename, String reportContent) {
        String fileNameWithoutExtension = FilenameUtils.removeExtension(filename);
        if (StringUtils.isNotEmpty(fileNameWithoutExtension)) {
            //resolve content type and create proper file name if file name doesn't have extension
            MediaType contentType = resolveMediaType(reportContent);
            
            return fileNameWithoutExtension + (MediaType.TEXT_HTML == contentType ? HTML_EXTENSION : TXT_EXTENSION);
        }
        return filename;
    }
    
    public static MediaType resolveMediaType(String reportContent) {
        if (StringUtils.startsWith(reportContent, HTML_TAG) || StringUtils.startsWith(reportContent, DOCTYPE_HTML_TAG)) {
            return MediaType.TEXT_HTML;
        } else {
            return MediaType.TEXT_PLAIN;
        }
    }
}
