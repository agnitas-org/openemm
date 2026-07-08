/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.auto_import.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.auto_import.bean.AutoImport;
import com.agnitas.emm.core.auto_import.bean.AutoImportLight;
import com.agnitas.emm.core.auto_import.bean.AutoImportResult;
import com.agnitas.emm.core.auto_import.bean.AutoImportWsJobState;
import com.agnitas.emm.core.auto_import.enums.AutoImportJobStatus;
import com.agnitas.emm.core.auto_import.form.AutoImportOverviewFilter;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.referencetable.beans.ReferenceTable;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;
import com.agnitas.service.ServiceResult;

public interface AutoImportService {

    List<Mailinglist> getMailinglists(int companyId);

    List<LightweightMailing> getAvailableMailings(Admin admin);

    List<AutoImportLight> getListAutoImportsByProfileId(int importProfileId);

    AutoImport getAutoImport(int autoImportId, int companyId);

    List<Workflow> getDependentWorkflows(int autoImportId, int companyId, boolean exceptInactive);

    int getWorkflowId(int autoImportId, int companyId);

    List<AutoImportLight> listAutoImports(int companyId);

    List<AutoImportLight> getListOfAutoImportsForWorkflow(int workflowId, int companyId);

    List<AutoImport> getAutoImportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    AutoImportResult doImportRecipientData(AutoImport autoImport, Locale locale) throws Exception;

    AutoImportResult doImportReferenceTableData(AutoImport autoImport, Locale locale) throws Exception;

    AutoImportResult doImportContentSource(AutoImport autoImport, Locale locale) throws Exception;

    void setAutoActivationDateAndActivate(Admin admin, int autoImportId, Date date, boolean isWmDriven);

    void deactivateAutoImport(int companyId, int autoImportId);

    List<CsvImportExportDescription> getCsvImportExportDescriptions(int companyId, String tableName);

    List<ReferenceTable> getReferencetable(int companyId);

    boolean announceStart(int autoImportId, Date nextStart);

    void announceEnd(AutoImport autoImport);

    void writeResultData(final AutoImport autoImport, int durationInSeconds, String result, String detailedResult, int datasourceId, int fieldCount, int insertCount, int updateCount, long fileSize);

    void saveAutoImport(AutoImport autoImport);

    void updateEmail(String emailForError, int id);

    AutoImport copyAutoImport(Admin admin, int autoImportId);

    AutoImportWsJobState getWsJobState(int autoImportJobId, int companyId);

    int saveNewWsJobState(int companyId, int autoImportId, AutoImportJobStatus status, int expirationTimeout);

    void saveWsJobState(int autoImportJobId, int companyId, AutoImportWsJobState state, int expirationTimeout);

    void removeExpiredWsJobs();

    PaginatedList<AutoImport> getAutoImportsOverview(AutoImportOverviewFilter filter, Admin admin);

    List<AutoImport.AutoImportType> getAvailableTypes(Admin admin);

    String findName(int autoImportId, int companyId);

    List<AutoImport> findAllByEmailPart(String email, int companyID);

    List<AutoImport> findAllByEmailPart(String email);

    ServiceResult<List<AutoImport>> getAllowedForDeletion(Set<Integer> ids, Admin admin);

    ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);

    ServiceResult<List<AutoImport>> changeActiveness(Set<Integer> ids, int companyID, boolean activeness);
}
