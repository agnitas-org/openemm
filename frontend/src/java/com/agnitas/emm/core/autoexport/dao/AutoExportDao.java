/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.autoexport.dao;

import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.autoexport.beans.AutoExport;
import com.agnitas.emm.core.autoexport.beans.AutoExportJobStatus;
import com.agnitas.emm.core.autoexport.beans.AutoExportWsJobState;
import com.agnitas.emm.core.autoexport.form.AutoExportOverviewFilter;

public interface AutoExportDao {

    List<AutoExport> getAutoExportsToRun(int maximumParallelAutoExports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    void changeActiveStatus(int autoExportId, int companyId, boolean active);

    String findName(int autoExportId, int companyId);

    AutoExport getAutoExport(int autoExportId, int companyId);

    void createAutoExport(AutoExport autoExport);

    void updateAutoExport(AutoExport autoExport);

    List<AutoExport> getAutoExports(int companyId);

    List<AutoExport> getAutoExports(List<Integer> allowedProfilesForRecipientAutoExport, int companyId);

	PaginatedList<AutoExport> getAutoExportsOverview(AutoExportOverviewFilter filter, List<Integer> allowedProfilesForRecipientAutoExport, boolean allowedNewIntervals, int companyID);

    void deleteAutoExport(int autoExportId, int companyId);

	boolean announceStart(int autoExportId, Date currentStart, Date nextStart);

	void announceEnd(AutoExport autoExport, int durationInSeconds, String result, int fieldCount, int exportCount, long fileSize);

	int resetAutoExportsForCurrentHost();

	List<AutoExport> listAutoExportsUsingProfile(int exportProfileID);
	
	DataSource getDataSource();

	int getRunningAutoExportsByHost(String hostName);
	
	boolean isExportStalling();

	int saveWsJobState(int companyId, int autoExportId, AutoExportJobStatus status, int expirationTimeout);

	void saveWsJobState(int jobId, int companyId, AutoExportWsJobState state, int expirationTimeout);

	AutoExportJobStatus getWsJobState(int jobId, int companyId);
	
	void removeExpiredWsJobs();

	List<Integer> getOutdatedAutoExports(int companyID, Date autoExportExportExpireDate);

    List<AutoExport> findAllByEmailPart(String email, int companyID);

    List<AutoExport> findAllByEmailPart(String email);

    void updateEmails(String emailForError, String emailForReport, int id);
}
