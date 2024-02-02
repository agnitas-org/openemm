/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.service;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.ExportPredef;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.bean.AutoExportWsJobState;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.autoexport.beans.AutoExportJobStatus;
import com.agnitas.emm.core.referencetable.beans.ComReferenceTable;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;

public interface AutoExportService {

    List<AutoExport> getAutoExportsOverview(Admin admin);

    List<AutoExport> getAutoExportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    AutoExportStatus doExportRecipientData(AutoExport autoExport) throws Exception;

    AutoExportStatus doExportMailingRecipientsData(AutoExport autoExport) throws Exception;

    List<Workflow> getDependentWorkflows(int autoExportId, int companyId, boolean exceptInactive);

    int getWorkflowId(int autoExportId, int companyId);

    void setAutoActivationDateAndActivate(int companyId, int autoExportId, Date date, boolean isWmDriven) throws Exception;

    void saveAutoExport(AutoExport autoExport) throws Exception;

    void changeAutoExportActiveStatus(int autoExportId, int companyId, boolean active);

    String findName(int autoExportId, int companyId);

    AutoExport getAutoExport(int autoExportId, int companyId);

    List<ExportPredef> getExportProfiles(Admin admin);

    boolean deleteAutoExport(int autoExportId, int companyId);

    List<CsvImportExportDescription> getCsvImportExportDescriptions(int companyId, String tableName);

    List<ComReferenceTable> getReferencetable(int companyId);

    AutoExportStatus doExportReferenceTableData(AutoExport autoExport) throws Exception;

    boolean announceStart(int autoExportId, Date currentStart, Date nextStart);

    void announceEnd(AutoExport autoExport, int durationInSeconds, String result, int fieldCount, int exportCount, long fileSize) throws Exception;

    void finishMailingAutoExport(AutoExport autoExport);

    List<AutoExport> getAutoExports(int companyId, boolean active);

    List<AutoExport> getMailingAutoExports(int companyId, boolean active);

    AutoExportStatus doExportReactionsData(AutoExport autoExport) throws Exception;

	AutoExportStatus doExportReactionsAndStatusData(AutoExport autoExport) throws Exception;

    AutoExport copyAutoExport(Admin admin, int autoExportId) throws Exception;

    AutoExportStatus doExportBlacklistData(AutoExport autoExport) throws Exception;

    int saveNewWsJobState(int companyId, int autoExportId, AutoExportJobStatus status, int expirationTimeout);

    void saveWsJobState(int jobId, int companyID, AutoExportWsJobState state, int expirationTimeoutDefaultSeconds);

    AutoExportWsJobState getWsJobState(int jobId, int companyId);

    void removeExpiredWsJobs();

}
