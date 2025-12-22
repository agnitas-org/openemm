/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.emm.core.preview.service.PreviewSettings;

public interface PdfService {

    byte[] writeUsersToPdfAndGetByteArray(List<AdminEntry> users);

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String customCss, String windowStatusForWaiting);

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey);

    @Deprecated
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String windowStatusForWaiting);

    File generatePDF(Admin admin, PreviewSettings previewSettings, boolean landscape, String title, String footerMsgKey);

}
