/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.util.List;

import org.agnitas.beans.ExportPredef;

import com.agnitas.beans.Admin;
import org.agnitas.service.RecipientExportWorker;

public interface ExportPredefService {

    ExportPredef get(int id, int companyId);

    String findName(int id, int companyId);

    ExportPredef create(int companyId);

    int save(ExportPredef src);

    List<ExportPredef> getExportProfiles(Admin admin);

    List<Integer> getExportProfileIds(Admin admin);

    ServiceResult<ExportPredef> getExportForDeletion(int exportId, int companyId);

    ServiceResult<ExportPredef> delete(int exportId, int companyId);

    RecipientExportWorker getRecipientsToZipWorker(ExportPredef export, Admin admin) throws Exception;

    ServiceResult<File> getExportFileToDownload(String exportFileName, Admin admin);

    String getExportDownloadZipName(Admin admin);

}
