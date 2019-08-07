/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.service;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.apache.struts.upload.FormFile;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.service.dto.EmailEntry;
import com.agnitas.emm.core.upload.service.dto.PageSetUp;
import com.agnitas.emm.core.upload.service.dto.PageUploadData;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;

public interface UploadService {

    boolean exists(int id);

    PaginatedListImpl<PageUploadData> getPaginatedFilesDescription(PageSetUp pageSetUp);

    UploadFileDescription getUploadFileDescription(int id);

    List<AdminEntry> getUsers(int companyId);

    void deleteFile(int id);

    boolean hasPermissionForDelete(int id, int adminId, int companyId);

    void uploadFiles(UploadFileDescription description, List<MultipartFile> files);

    File getDataForDownload(int id);

    List<EmailEntry> getDefaultEmails(String email, Locale locale);

    List<UploadData> getUploadsByExtension(ComAdmin admin, String extension);

    @Deprecated
    FormFile getFormFileByUploadId(int uploadID, String mime);

}
