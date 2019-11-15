/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service;

import java.util.List;
import java.util.Map;

import org.agnitas.service.JobDto;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.serverstatus.bean.ServerStatus;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Version;

public interface ServerStatusService {
    
    boolean checkDatabaseConnection();
    
    String getDbUrl();
    
    boolean isDBStatusOK();
    
    List<VersionStatus> getLatestDBVersionsAndErrors();
    
    Map<String, Object> getStatusProperties() throws Exception;
    
    ServerStatus getServerStatus(ComAdmin admin);
    
    SimpleServiceResult sendTestMail(ComAdmin admin, String testMailAddress);
    
    SimpleServiceResult sendDiagnosisInfo(ComAdmin admin, String sendDiagnosisEmail);
    
    boolean saveServerConfig(int companyId, String configName, String configValue, String description);
    
    ConfigValueDto getServerConfigurations(int companyId, String configName);

	String getVersion();

	boolean isJobQueueRunning();

	boolean isJobQueueStatusOK();

	boolean isImportStalling();

	List<JobDto> getErrorneousJobs();

	List<String> killRunningImports();

	Version getAvailableUpdateVersion() throws Exception;
}
