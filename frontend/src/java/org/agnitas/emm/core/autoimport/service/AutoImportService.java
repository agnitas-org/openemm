/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.auto_import.form.AutoImportOverviewFilter;
import com.agnitas.emm.core.referencetable.beans.ReferenceTable;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.bean.AutoImportWsJobState;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;

public interface AutoImportService {

    List<Mailinglist> getMailinglists(int companyId);

    List<LightweightMailing> getAvailableMailings(Admin admin);

    List<AutoImportLight> getListAutoImportsByProfileId(int importProfileId);

    AutoImport getAutoImport(int autoImportId, int companyId);

    List<Workflow> getDependentWorkflows(int autoImportId, int companyId, boolean exceptInactive);

    int getWorkflowId(int autoImportId, int companyId);

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    boolean deleteAutoImport(int autoImportId, int companyId);

    List<AutoImportLight> listAutoImports(int companyId);

    List<AutoImportLight> getListOfAutoImportsForWorkflow(int workflowId, int companyId);

    List<AutoImport> getAutoImportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    void changeAutoImportActiveStatus(int autoImportId, int companyId, boolean active);

    AutoImportResult doImportRecipientData(AutoImport autoImport) throws Exception;

    AutoImportResult doImportReferenceTableData(AutoImport autoImport) throws Exception;

    AutoImportResult doImportContentSource(AutoImport autoImport) throws Exception;

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

    PaginatedListImpl<AutoImport> getAutoImportsOverview(AutoImportOverviewFilter filter, Admin admin);
    List<AutoImport.AutoImportType> getAvailableTypes(Admin admin);

    String findName(int autoImportId, int companyId);

    List<AutoImport> findAllByEmailPart(String email, int companyID);
    List<AutoImport> findAllByEmailPart(String email);

    ServiceResult<List<AutoImport>> getAllowedForDeletion(Set<Integer> ids, Admin admin);

    ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);

    ServiceResult<List<AutoImport>> changeActiveness(Set<Integer> ids, int companyID, boolean activeness);
}
