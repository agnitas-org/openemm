/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dao;

import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.LightweightBirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import com.agnitas.beans.impl.PaginatedListImpl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BirtReportDao {

    boolean markDeleted(int reportId, int companyId);

    void restore(Set<Integer> ids, int companyId);

    List<Integer> getMarkedAsDeletedBefore(Date date, int companyId);

    boolean insert(BirtReport report);

    boolean update(BirtReport report);
    
    boolean update(BirtReport report, List<Integer> justDeactivateSettingTypes);

    List<LightweightBirtReport> getLightweightBirtReportList(int companyID);

    void deactivateReportSettings(int reportId, Collection<Integer> settingsTypes);

    boolean hasActiveDelivery(int reportId, Collection<Integer> settingsTypes);

    void updateReportMailinglists(int reportId, int reportType, List<Integer> mailinglistIds);

    void insertSentMailings(Integer reportId, Integer companyID, List<Integer> sentMailings);

    List<Map<String, Object>> getReportParamValues(int companyID, String paramName);

    List<LightweightBirtReport> getLightweightBirtReportsBySelectedTarget(int companyID, int targetGroupID);

	int resetBirtReportsForCurrentHost();

	boolean announceStart(BirtReport birtReport);

	void announceEnd(BirtReport birtReport);

	int getRunningReportsByHost(String hostName);

	List<BirtReport> getReportsToSend(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	void deactivateBirtReport(int reportID);

	List<BirtReport> selectErroneousReports();

    BirtReport get(int id, int companyID);

    boolean delete(BirtReport report);

    Date getReportActivationDay(int companyId, int reportId);

    List<BirtReport> getReportsByIds(List<Integer> reportIds);

    PaginatedListImpl<ReportEntry> getPaginatedReportList(int companyId, String sort, String order, int pageNumber, int rownums);
    PaginatedListImpl<ReportEntry> getPaginatedReportList(BirtReportOverviewFilter filter, int companyId);

    List<BirtReport> getAllReportsByCompanyID(int companyId);

    String getReportName(int companyId, int reportId);

    boolean isReportExist(int companyId, int reportId);

    boolean deleteReport(int companyId, int reportId);

    List<Integer> getSampleReportIds(int companyId);

    List<ReportEntry> findAllByEmailPart(String email, int companyID);
    List<ReportEntry> findAllByEmailPart(String email);

    void storeBirtReportEmailRecipients(List<String> emails, int reportId);

    Map<BirtReportType, Integer> getMailingAutomaticReportIdsMap(int mailingId, int companyId);

    List<String> getMailingAutomaticReportEmails(Collection<Integer> reportIds);
}
