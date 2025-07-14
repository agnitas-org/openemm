/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.exception.UnknownUserStatusException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.web.forms.PaginationForm;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface ExportPredefService {

    ExportPredef get(int id, int companyId);

    String findName(int id, int companyId);

    int save(ExportPredef src, Admin admin);

    List<ExportPredef> getExportProfiles(Admin admin);
    PaginatedListImpl<ExportPredef> getExportProfilesOverview(PaginationForm form, Admin admin);

    List<Integer> getExportProfileIds(Admin admin);

    ServiceResult<ExportPredef> getExportForDeletion(int exportId, int companyId);

    // TODO: EMMGUI-714: Check usages and remove when removing old design
    ServiceResult<ExportPredef> delete(int exportId, int companyId);

    RecipientExportWorker getRecipientsToZipWorker(ExportPredef export, Admin admin) throws Exception;

    ServiceResult<File> getExportFileToDownload(String exportFileName, Admin admin);

    Set<Charset> getAvailableCharsetOptionsForDisplay(Admin admin, ExportPredef export);

    Set<UserStatus> getAvailableUserStatusOptionsForDisplay(Admin admin, ExportPredef export) throws UnknownUserStatusException;

    EnumSet<BindingEntry.UserType> getAvailableUserTypeOptionsForDisplay(Admin admin, ExportPredef export);

    boolean isManageAllowed(ExportPredef export, Admin admin);

    ServiceResult<List<ExportPredef>> getAllowedForDeletion(Set<Integer> ids, int companyId);

    ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);
}
