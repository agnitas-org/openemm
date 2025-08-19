/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.preview.service.PreviewSettings;
import com.lowagie.text.DocumentException;
import com.agnitas.beans.AdminEntry;

public interface PdfService {

    byte[] writeUsersToPdfAndGetByteArray(List<AdminEntry> users) throws DocumentException, IOException;

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String customCss, String windowStatusForWaiting) throws IOException, DocumentException;

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey) throws IOException, DocumentException;

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String windowStatusForWaiting) throws IOException, DocumentException;

    File generatePDF(Admin admin, PreviewSettings previewSettings, boolean landscape, String title, String footerMsgKey) throws Exception;

}
