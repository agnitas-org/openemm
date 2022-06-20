/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.service;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.bean.AutoImportWsJobState;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.referencetable.beans.ComReferenceTable;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.service.CsvImportExportDescription;

public interface AutoImportService {
    List<Mailinglist> getMailinglists(@VelocityCheck int companyId);

    List<LightweightMailing> getAvailableMailings(ComAdmin admin);

    List<ImportProfile> getImportProfiles(@VelocityCheck int companyId);

    ImportProfile getImportProfileById(int importProfileId);

    List<AutoImportLight> getListAutoImportsByProfileId(int importProfileId);

    AutoImport getAutoImport(int autoImportId, @VelocityCheck int companyId);

    List<Workflow> getDependentWorkflows(int autoImportId, @VelocityCheck int companyId, boolean exceptInactive);

    int getWorkflowId(int autoImportId, @VelocityCheck int companyId);

    boolean deleteAutoImport(int autoImportId, @VelocityCheck int companyId);

    List<AutoImportLight> listAutoImports(@VelocityCheck int companyId);

    List<AutoImport> getAutoImportsOverview(@VelocityCheck int companyId);

    List<AutoImport> getAutoImportsToRun(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    void changeAutoImportActiveStatus(int autoImportId, @VelocityCheck int companyId, boolean active);

    AutoImportResult doImportRecipientData(AutoImport autoImport) throws Exception;

    AutoImportResult doImportReferenceTableData(AutoImport autoImport) throws Exception;

    AutoImportResult doImportHtmlContentData(AutoImport autoImport) throws Exception;

    void setAutoActivationDateAndActivate(@VelocityCheck int companyId, int autoImportId, Date date, boolean isWmDriven) throws Exception;

    void deactivateAutoImport(@VelocityCheck int companyId, int autoImportId) throws Exception;

    List<CsvImportExportDescription> getCsvImportExportDescriptions(@VelocityCheck int companyId, String tableName);

    List<ComReferenceTable> getReferencetable(@VelocityCheck int companyId);

    boolean announceStart(int autoImportId, Date nextStart);

    void announceEnd(AutoImport autoImport) throws Exception;

    void writeResultData(int autoImportId, int durationInSeconds, String result, String detailedResult, int datasourceId, int fieldCount, int insertCount, int updateCount, long fileSize) throws Exception;

    void saveAutoImport(AutoImport autoImport) throws Exception;

    AutoImport copyAutoImport(ComAdmin admin, int autoImportId) throws Exception;

    AutoImportWsJobState getWsJobState(int autoImportJobId, @VelocityCheck int companyId);

    int saveNewWsJobState(@VelocityCheck int companyId, int autoImportId, AutoImportJobStatus status, int expirationTimeout);

    void saveWsJobState(int autoImportJobId, @VelocityCheck int companyId, AutoImportWsJobState state, int expirationTimeout);

    void removeExpiredWsJobs();
    
    List<AutoImport> getAutoImportsOverview(ComAdmin admin, String[] filters);
}
