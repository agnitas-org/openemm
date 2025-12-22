/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;

public interface BirtReportService {

    boolean insert(BirtReport report);

    void logSentReport(BirtReport report);

    boolean isExistedBenchmarkMailingTbls();

	boolean announceStart(BirtReport birtReport);

	void announceEnd(BirtReport birtReport);

	int getRunningReportsByHost(String hostName);

	List<BirtReport> getReportsToSend(int maximumNumberOfReports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	void deleteExpired(Date expireDate, int companyId);

	BirtReportDto getBirtReport(Admin admin, int reportId);

	BirtReport getBirtReport(int reportId, int companyId);

	ReportSettingsType getBirtReportSettingsTypeForEvaluation(int activeTab, Admin admin);

	BirtReportDownload evaluate(BirtReportForm form, Admin admin);

	PaginatedList<ReportEntry> getPaginatedReportList(BirtReportOverviewFilter filter, int companyId);

	List<Campaign> getCampaignList(int companyId);

	Map<String, LocalDate> getDatesRestrictionMap(Admin admin, ReportSettingsType type, SimpleDateFormat dateFormatPattern, Map<String, Object> settings);

	List<MailingBase> getFilteredMailings(Admin admin, int filterType, int filterValue, MailingType mailingType);

	int saveBirtReport(Admin admin, BirtReportDto birtReport);

	boolean isReportEnabled(Admin admin, BirtReportDto birtReport);

	void preloadMailingsByRestriction(Admin admin, ReportSettingsType type, Map<String, Object> settingsByType, Map<String, LocalDate> dateRestrictions);

	void deactivateAllDeliveries(int reportId);

	boolean hasActiveDelivery(int reportId);

	BirtReportForm createSingleMailingStatisticsReportForm(int mailingId, Admin admin);

	void copySampleReports(int toCompanyId, int fromCompanyId);

	List<String> getNames(Set<Integer> ids, int companyID);

	ServiceResult<UserAction> markDeleted(Set<Integer> ids, int companyID);

    List<ReportEntry> findAllByEmailPart(String email, int companyID);

    List<ReportEntry> findAllByEmailPart(String email);

	void storeBirtReportEmailRecipients(List<String> emails, int reportId);

	void restore(Set<Integer> bulkIds, int companyId);

	Map<BirtReportType, Integer> getMailingAutomaticReportIdsMap(int mailingId, int companyId);

	List<String> getMailingAutomaticReportEmails(Collection<Integer> reportIds);
}
