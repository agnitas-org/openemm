/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.agnitas.service.JobDto;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.serverstatus.bean.ServerStatus;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Version;

import jakarta.servlet.ServletContext;
import net.sf.json.JSONArray;

public interface ServerStatusService {
    
    boolean checkDatabaseConnection();
    
    String getDbUrl();
    
    boolean isDBStatusOK();
    
    List<VersionStatus> getLatestDBVersionsAndErrors();
    
    Map<String, Object> getStatusProperties(ServletContext servletContext) throws Exception;
    
    ServerStatus getServerStatus(ServletContext servletContext, ComAdmin admin);
    
    SimpleServiceResult sendTestMail(ComAdmin admin, String testMailAddress);
    
    SimpleServiceResult sendDiagnosisInfo(ServletContext context, ComAdmin admin, String sendDiagnosisEmail);
    
    boolean saveServerConfig(int companyId, String configName, String configValue, String description);
    
    ConfigValueDto getServerConfigurations(int companyId, String configName);

	String getVersion();

	boolean isJobQueueRunning();

	boolean isJobQueueStatusOK();

	boolean isImportStalling();

	boolean isExportStalling();

	List<JobDto> getErroneousJobs();

	List<String> killRunningImports();

	boolean checkActiveNode();

	Version getAvailableUpdateVersion() throws Exception;

	boolean isReportStatusOK();

	File downloadConfigFile() throws IOException, Exception;

	File getFullTbl(String dbStatement, String tableName) throws Exception;

	JSONArray getSystemStatus();

	void acknowledgeErroneousJob(int idToAcknowledge);

	String getDbVersion();

	List<String> getErroneousImports();

	List<String> getErroneousExports();
}
