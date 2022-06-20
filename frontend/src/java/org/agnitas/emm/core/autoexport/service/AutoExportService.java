/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.service;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.agnitas.beans.ExportPredef;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.bean.AutoExport.AutoExportType;
import org.agnitas.emm.core.autoexport.bean.AutoExportWsJobState;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.autoexport.beans.AutoExportJobStatus;
import com.agnitas.emm.core.referencetable.beans.ComReferenceTable;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;

public interface AutoExportService {
    List<AutoExport> getAutoExportsOverview(ComAdmin admin);

    List<AutoExport> getAutoExportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    AutoExportStatus doExportRecipientData(AutoExport autoExport) throws Exception;

    AutoExportStatus doExportMailingRecipientsData(AutoExport autoExport) throws Exception;

    List<Workflow> getDependentWorkflows(int autoExportId, int companyId, boolean exceptInactive);

    int getWorkflowId(int autoExportId, int companyId);

    void setAutoActivationDateAndActivate(int companyId, int autoExportId, Date date, boolean isWmDriven) throws Exception;

    void deactivateAutoExport(int companyId, int autoExportId) throws Exception;

    void saveAutoExport(AutoExport autoExport) throws Exception;

    void changeAutoExportActiveStatus(int autoExportId, int companyId, boolean active);

    AutoExport getAutoExport(int autoExportId, int companyId);

    List<ExportPredef> getExportProfiles(ComAdmin admin);

    boolean deleteAutoExport(int autoExportId, int companyId);

    List<CsvImportExportDescription> getCsvImportExportDescriptions(int companyId, String tableName);

    List<ComReferenceTable> getReferencetable(int companyId);

    AutoExportStatus doExportReferenceTableData(AutoExport autoExport) throws Exception;

    boolean announceStart(int autoExportId, Date nextStart);

    void announceEnd(AutoExport autoExport, int durationInSeconds, String result, int fieldCount, int exportCount, long fileSize) throws Exception;

    void finishMailingAutoExport(AutoExport autoExport);

    List<AutoExport> getAutoExports(int companyId, boolean active);
    
    List<AutoExport> getMailingAutoExports(int companyId, boolean active);
    
    void saveMailingAutoExport(ComAdmin admin, int autoExportId, int mailingId, Date activationAutoExportDate);

    void createMailingAutoExport(ComAdmin admin, int autoExportId, int mailingId, Date activationAutoExportDate);
    
    void updateMailingAutoExport(ComAdmin admin, int autoExportId, int mailingId, Date activationAutoExportDate);

    /**
     * Returns temporary auto-export file
     *
     * @return null or file (can exist or not)
     */
    File getSavedAutoExportFile(int companyId, AutoExportType exportType, String fileName);

    AutoExportStatus doExportReactionsData(AutoExport autoExport) throws Exception;

    AutoExport copyAutoExport(ComAdmin admin, int autoExportId) throws Exception;

	AutoExportStatus doExportBlacklistData(AutoExport autoExport) throws Exception;
	
    int saveNewWsJobState(int companyId, int autoExportId, AutoExportJobStatus status, int expirationTimeout);

	void saveWsJobState(int jobId, int companyID, AutoExportWsJobState state, int expirationTimeoutDefaultSeconds);

	AutoExportWsJobState getWsJobState(int jobId, int companyId);
	
	void removeExpiredWsJobs();

}
