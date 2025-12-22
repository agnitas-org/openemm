/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.auto_import.dao;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.auto_import.bean.AutoImport;
import com.agnitas.emm.core.auto_import.bean.AutoImportLight;
import com.agnitas.emm.core.auto_import.enums.AutoImportJobStatus;
import com.agnitas.emm.core.auto_import.form.AutoImportOverviewFilter;
import com.agnitas.util.Tuple;

public interface AutoImportDao {

	boolean exists(int autoImportId, int companyId);

	String findName(int autoImportId, int companyId);
	
	List<AutoImportLight> listAutoImports(final int companyID);
	
	AutoImport getAutoImport(int autoImportId, int companyId);

	List<AutoImport.UsedFile> getUsedFiles(int autoImportId, int companyId);

	void deleteAutoImport(int autoImportId, int companyId);
	
	boolean deleteAutoImportByCompanyID(int companyId);

	void addUsedFile(AutoImport.UsedFile usedFile, int lines, Date startTime, Date endTime, int autoImportId, int companyId);

	void changeActiveStatus(int autoImportId, int companyId, boolean active);

	void updateAutoImport(AutoImport autoImport);

	void createAutoImport(AutoImport autoImport);

	List<AutoImport> getAutoImportsToRun(int maximumParallelAutoImports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	boolean announceStart(int autoImportId, Date nextStart);

	void announceInterimResult(AutoImport autoImport);

	void announceEnd(AutoImport autoImport);

	int resetAutoImportsForCurrentHost();

	PaginatedList<AutoImport> getAutoImportsOverview(AutoImportOverviewFilter filter, int companyId, boolean restrictContentSourceType, boolean showReferenceTableImports, boolean allowedNewIntervals);

	int getRunningAutoImportsByHost(String hostname);

	void writeResultData(int autoImportId, int durationInSeconds, String result, String detailedResult, int datasourceId, int fieldCount, int insertCount, int updateCount, long fileSize);

	List<AutoImportLight> listAutoImportsUsingProfile(int importProfileID);

	/**
	 * Get a state (status + report) of an auto-import job (triggered by webservice) referenced by {@code autoImportJobId}.
	 *
	 * @param autoImportJobId an identifier of an auto-import job to get a state of.
	 * @param companyId an identifier of a company of the current user.
	 * @return a tuple containing job status and report or {@code null} if there's no such job (invalid id or a job has been expired and removed).
	 */
    Tuple<AutoImportJobStatus, String> getWsJobState(int autoImportJobId, int companyId);

	/**
	 * Create a new entry describing an auto-import job (triggered by webservice).
	 *
	 * @param companyId an identifier of a company of the current user.
	 * @param autoImportId an identifier of an auto-import to be processed.
	 * @param status a status describing execution stage or result.
	 * @param expirationTimeout how long (in seconds) this entry should stay in database.
	 * @return an identifier of an entry just created.
	 */
	int saveWsJobState(int companyId, int autoImportId, AutoImportJobStatus status, int expirationTimeout);

	/**
	 * Update an entry describing an auto-import job (triggered by webservice).
	 *
	 * @param autoImportJobId an identifier of an auto-import job entry (to be returned by {@link #saveWsJobState(int, int, AutoImportJobStatus, int)}).
	 * @param companyId an identifier of a company of the current user.
	 * @param status a new status to store.
	 * @param report a new report to store.
	 * @param expirationTimeout how long (in seconds) this entry should stay in database.
	 */
	void saveWsJobState(int autoImportJobId, int companyId, AutoImportJobStatus status, String report, int expirationTimeout);

	/**
	 * Remove all the auto-import job entries whose expiration timeout is reached.
	 */
	void removeExpiredWsJobs();

	List<AutoImport> getStallingAutoImports();

	int getStallingImportsAmount(int maxUserImportDurationMinutes);

	List<Integer> getOutdatedAutoImports(int companyID, Date autoImportExportExpireDate);

	List<AutoImportLight> getListOfAllowedAutoImportsForWorkflow(int workflowId, int companyId);

    List<AutoImport> findAllByEmailPart(String email, int companyID);

    List<AutoImport> findAllByEmailPart(String email);

    void updateEmails(String emailForError, int id);
}
