/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dao;

import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import org.agnitas.beans.impl.PaginatedListImpl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ComBirtReportDao {
    boolean insert(ComBirtReport report) throws Exception;

    boolean update(ComBirtReport report) throws Exception;
    
    boolean update(ComBirtReport report, List<Integer> justDeactivateSettingTypes) throws Exception;

    List<ComLightweightBirtReport> getLightweightBirtReportList(int companyID);

    void deactivateReportSettings(int reportId, Collection<Integer> settingsTypes);

    boolean hasActiveDelivery(int reportId, Collection<Integer> settingsTypes);

    void updateReportMailinglists(int reportId, int reportType, List<Integer> mailinglistIds);

    void insertSentMailings(Integer reportId, Integer companyID, List<Integer> sentMailings);

    List<Map<String, Object>> getReportParamValues(int companyID, String paramName);

    List<ComLightweightBirtReport> getLightweightBirtReportsBySelectedTarget(int companyID, int targetGroupID);

	int resetBirtReportsForCurrentHost();

	boolean announceStart(ComBirtReport birtReport);

	void announceEnd(ComBirtReport birtReport);

	int getRunningReportsByHost(String hostName);

	List<ComBirtReport> getReportsToSend(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	void deactivateBirtReport(int reportID);

	List<ComBirtReport> selectErroneousReports();

    ComBirtReport get(int id, int companyID);

    boolean delete(ComBirtReport report);

    Date getReportActivationDay(int companyId, int reportId);

    List<ComBirtReport> getReportsByIds(List<Integer> reportIds);

    PaginatedListImpl<ReportEntry> getPaginatedReportList(int companyId, String sort, String order, int pageNumber, int rownums);
    PaginatedListImpl<ReportEntry> getPaginatedReportList(BirtReportOverviewFilter filter, int companyId);

    List<ComBirtReport> getAllReportsByCompanyID(int companyId);

    String getReportName(int companyId, int reportId);

    boolean isReportExist(int companyId, int reportId);

    boolean deleteReport(int companyId, int reportId);

    List<Integer> getSampleReportIds(int companyId);
}
