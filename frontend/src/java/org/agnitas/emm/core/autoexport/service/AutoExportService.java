/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.autoexport.form.AutoExportOverviewFilter;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.bean.AutoExportWsJobState;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.autoexport.beans.AutoExportJobStatus;
import com.agnitas.emm.core.referencetable.beans.ReferenceTable;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;

public interface AutoExportService {

    int MAX_SHORTNAME_LENGTH = 100;

    List<AutoExport> getAutoExports(Admin admin);
    PaginatedListImpl<AutoExport> getAutoExportsOverview(AutoExportOverviewFilter filter, Admin admin);

    List<AutoExport> getAutoExportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    AutoExportStatus doExportRecipientData(AutoExport autoExport) throws Exception;

    AutoExportStatus doExportMailingRecipientsData(AutoExport autoExport) throws Exception;

    List<Workflow> getDependentWorkflows(int autoExportId, int companyId, boolean exceptInactive);

    int getWorkflowId(int autoExportId, int companyId);

    void setAutoActivationDateAndActivate(int companyId, int autoExportId, Date date, boolean isWmDriven);

    boolean saveAutoExport(AutoExport autoExport);

    void changeAutoExportActiveStatus(int autoExportId, int companyId, boolean active);

    String findName(int autoExportId, int companyId);

    AutoExport getAutoExport(int autoExportId, int companyId);

    List<ExportPredef> getExportProfiles(Admin admin);

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    boolean deleteAutoExport(int autoExportId, int companyId);

    List<CsvImportExportDescription> getCsvImportExportDescriptions(int companyId, String tableName);

    List<ReferenceTable> getReferencetable(int companyId);

    AutoExportStatus doExportReferenceTableData(AutoExport autoExport) throws Exception;

    boolean announceStart(int autoExportId, Date currentStart, Date nextStart);

    void announceEnd(AutoExport autoExport, int durationInSeconds, String result, int fieldCount, int exportCount, long fileSize);

    void finishMailingAutoExport(AutoExport autoExport);

    AutoExportStatus doExportReactionsData(AutoExport autoExport) throws Exception;

	AutoExportStatus doExportReactionsAndStatusData(AutoExport autoExport) throws Exception;

    ServiceResult<AutoExport> copyAutoExport(Admin admin, int autoExportId);

    AutoExportStatus doExportBlacklistData(AutoExport autoExport) throws Exception;

    int saveNewWsJobState(int companyId, int autoExportId, AutoExportJobStatus status, int expirationTimeout);

    void saveWsJobState(int jobId, int companyID, AutoExportWsJobState state, int expirationTimeoutDefaultSeconds);

    AutoExportWsJobState getWsJobState(int jobId, int companyId);

    void removeExpiredWsJobs();
    boolean isManageAllowed(AutoExport autoExport, Admin admin);
    boolean hasPermissionForType(AutoExport.AutoExportType type, Admin admin);
    ServiceResult<List<AutoExport>> getAllowedForDeletion(Set<Integer> ids, Admin admin);
    ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);

    List<AutoExport> findAllByEmailPart(String email, int companyID);
    List<AutoExport> findAllByEmailPart(String email);
    void updateEmails(String emailForError, String emailForReport, int id);

    ServiceResult<List<AutoExport>> changeActiveness(Set<Integer> ids, Admin admin, boolean activeness);
}
