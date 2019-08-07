/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.dao;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface AutoExportDao {
    List<AutoExport> getAutoExportsToRun(int maximumParallelAutoExports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    void changeActiveStatus(int autoExportId, @VelocityCheck int companyId, boolean active);

    AutoExport getAutoExport(int autoExportId, @VelocityCheck int companyId);

    void createAutoExport(AutoExport autoExport) throws Exception;

    void updateAutoExport(AutoExport autoExport) throws Exception;

    List<AutoExport> getAutoExportsOverview(@VelocityCheck int companyId);

    void deleteAutoExport(int autoExportId, @VelocityCheck int companyId);

	boolean announceStart(int autoExportId, Date nextStart);

	void announceEnd(int autoExportId, int durationInSeconds, String result, int fieldCount, int exportCount, long fileSize) throws Exception;

	List<AutoExport> getAutoExports(@VelocityCheck int companyId, boolean active);

	void scheduleMailingReport(@VelocityCheck int companyId, int autoExportId, int mailing_id,  Date activationDate);

	int resetAutoExportsForCurrentHost();

	List<AutoExport> listAutoExportsUsingProfile(int exportProfileID);
	
	DataSource getDataSource();

	int getRunningAutoExportsByHost(String hostName);
	
	List<AutoExport> getMailingAutoExports(@VelocityCheck int companyId, boolean active);
}
