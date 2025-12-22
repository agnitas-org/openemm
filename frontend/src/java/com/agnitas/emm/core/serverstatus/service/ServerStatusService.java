/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.auto_import.bean.AutoImport;
import com.agnitas.emm.core.serverstatus.bean.ServerStatus;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.service.JobDto;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Version;
import jakarta.servlet.ServletContext;
import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

public interface ServerStatusService {
    
    String getDbVendor();
    
    String getDbUrl();
    
    boolean isDBStatusOK();
    
    List<VersionStatus> getLatestDBVersionsAndErrors();
    
	int calcDiskSpaceFreePercentage();
    
    ServerStatus getServerStatus(ServletContext servletContext, Admin admin);
    
    ServerStatus getAnonymousServerStatus(ServletContext servletContext);
    
    SimpleServiceResult sendTestMail(Admin admin, String testMailAddress);
    
    SimpleServiceResult sendDiagnosisInfo(ServletContext context, Admin admin, String sendDiagnosisEmail);
    
    boolean saveServerConfig(int companyId, String configName, String configValue, String description);
    
    ConfigValueDto getServerConfigurations(int companyId, String configName);

	String getVersion();

	boolean isJobQueueRunning();

	boolean isJobQueueStatusOK();

	boolean isExportStalling();

	List<JobDto> getErroneousJobs();

	List<String> killRunningImports();

	boolean checkActiveNode();

	Version getAvailableUpdateVersion(Version currentVersion) throws Exception;

	boolean isReportStatusOK();

	File downloadConfigFile() throws IOException;

	JSONArray getSystemStatus();

	void acknowledgeErroneousJob(int idToAcknowledge);

	String getDbVersion();

	List<String> getErroneousImports();

	List<String> getErroneousExports();

	List<AutoImport> getStallingAutoImports();

	int getStallingImportsAmount(int maxUserImportDurationMinutes);

	boolean isLicenseStatusOK();

	boolean isOverallStatusOK();

	File unzipLicenseFile(MultipartFile archiveFile) throws Exception;
}
