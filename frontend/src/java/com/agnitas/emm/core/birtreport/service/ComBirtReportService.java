/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ComBirtReportService {
    boolean insert(ComBirtReport report) throws Exception;

    void logSentReport(ComBirtReport report);

    boolean isExistedBenchmarkMailingTbls();

	boolean announceStart(ComBirtReport birtReport);

	void announceEnd(ComBirtReport birtReport);

	int getRunningReportsByHost(String hostName);

	List<ComBirtReport> getReportsToSend(int maximumNumberOfReports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	void deleteExpired(Date expireDate, int companyId);

	BirtReportDto getBirtReport(Admin admin, int reportId);
	ComBirtReport getBirtReport(int reportId, int companyId);

	BirtReportDownload evaluate(Admin admin, BirtReportForm form) throws Exception;
	List<ReportSettingsType> getBirtReportSettingsTypesForEvaluation(Map<ReportSettingsType, Map<String, Object>> settings, int activeTab, Admin admin);
	List<BirtReportDownload> evaluate(int id, int activeTab, Admin admin) throws Exception;

	boolean deleteReport(int companyId, int reportId);

	PaginatedListImpl<ReportEntry> getPaginatedReportList(int companyId, String sort, String sortOrder, int page, int rownums);
	PaginatedListImpl<ReportEntry> getPaginatedReportList(BirtReportOverviewFilter filter, int companyId);

	List<Campaign> getCampaignList(int companyId);

	Map<String, LocalDate> getDatesRestrictionMap(Admin admin, ReportSettingsType type, SimpleDateFormat dateFormatPattern, Map<String, Object> settings);

	String getReportName(int companyId, int reportId);

	boolean isReportExist(int companyId, int reportId);

	List<MailingBase> getFilteredMailings(Admin admin, int filterType, int filterValue, MailingType mailingType);

	int saveBirtReport(Admin admin, BirtReportDto birtReport) throws Exception;

	boolean isReportEnabled(Admin admin, BirtReportDto birtReport);

	void preloadMailingsByRestriction(Admin admin, ReportSettingsType type, Map<String, Object> settingsByType, Map<String, LocalDate> dateRestrictions);

	void deactivateAllDeliveries(int reportId);

	boolean hasActiveDelivery(int reportId);

	BirtReportDto createSingleMailingStatisticsReport(int mailingId, Admin admin) throws Exception;

	void copySampleReports(int toCompanyId, int fromCompanyId) throws Exception;

	List<String> getNames(Set<Integer> ids, int companyID);

	ServiceResult<UserAction> markDeleted(Set<Integer> ids, int companyID);

    List<ReportEntry> findAllByEmailPart(String email, int companyID);
    List<ReportEntry> findAllByEmailPart(String email);

	void storeBirtReportEmailRecipients(List<String> emails, int reportId);

	void restore(Set<Integer> bulkIds, int companyId);
}
