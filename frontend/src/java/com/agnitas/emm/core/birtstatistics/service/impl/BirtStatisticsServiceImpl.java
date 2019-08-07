/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.reporting.birt.util.RSACryptUtil;
import com.agnitas.reporting.birt.util.UIDUtils;

public class BirtStatisticsServiceImpl implements BirtStatisticsService {
	protected static final String REPORT_NAME = "__report";
	protected static final String IS_SVG = "__svg";
	protected static final String COMPANY_ID = "companyID";
	protected static final String TARGET_ID = "targetID";
	protected static final String MAILING_LIST_ID = "mailinglistID";
	protected static final String MAX_DOMAINS = "maxdomains";
	protected static final String LANGUAGE = "language";
	protected static final String UID = "uid";
	protected static final String EMM_SESSION = "emmsession";
	protected static final String TARGET_BASE_URL = "targetBaseUrl";
	protected static final String IS_TOP_LEVEL_DOMAIN = "topLevelDomain";
	protected static final String HOUR_SCALE = "hourScale";
	protected static final String FORMAT = "__format";

	protected static final String RECIPIENT_START_DATE = "startDate";
	protected static final String RECIPIENT_STOP_DATE = "stopDate";
	protected static final String START_DATE = "startdate";
	protected static final String END_DATE = "enddate";

	protected static final String INCLUDE_ADMIN_AND_TEST_MAILS = "includeAdminAndTestMails";
	protected static final String TOP_10_METRICS = "top10metrics";
	protected static final String SESSION_ID = "sessionID";
	
	protected static final String MEDIA_TYPE = "mediaType";
	
	protected static final String ACCOUNT_ID = "accountId";
	protected static final String REPORT_ID = "reportId";
	protected static final String REPORT_CREATE_DATE = "reportCreateDate";

    protected static final String MAX_DEVICES_NUMBER = "maxdevices";

	protected static final String MAILING_ID = "mailingID";
	protected static final String SECTOR = "sector";

	protected ConfigService configService;
	protected String publicKeyFilename;
	
    @Override
	public String getDomainStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, DomainStatisticDto domainStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();

		map.put(REPORT_NAME, domainStatistic.getReportName());
		map.put(IS_SVG, true);
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(TARGET_ID, domainStatistic.getTargetId());
		map.put(MAILING_LIST_ID, domainStatistic.getMailingListId());
		map.put(MAX_DOMAINS, domainStatistic.getMaxDomainNum());
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(UID, generateUid(admin));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		map.put(IS_TOP_LEVEL_DOMAIN, domainStatistic.isTopLevelDomain());
		
		return generateUrlWithParamsForInternalAccess(map);
	}

	@Override
	public String getMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, MonthlyStatisticDto monthlyStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, monthlyStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		
		SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYYMMDD, AgnUtils.getTimeZone(admin));
		int startYear = monthlyStatistic.getStartYear();
		int startMonth = monthlyStatistic.getStartMonth();
		map.put(START_DATE, formatDateParam(startYear, startMonth, 1, format));
		map.put(END_DATE, formatDateParam(startYear, startMonth + 1, 0, format));
		
		map.put(INCLUDE_ADMIN_AND_TEST_MAILS, false);
		map.put(TOP_10_METRICS, monthlyStatistic.getTop10MetricsId());
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(UID, generateUid(admin));
		map.put(SESSION_ID, sessionId);
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL, generateTargetBaseUrl());
		
		return generateUrlWithParamsForInternalAccess(map);
	}

	@Override
	public String getRecipientMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientStatisticDto monthlyStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, monthlyStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(MEDIA_TYPE, monthlyStatistic.getMediaType());
		map.put(IS_SVG, true);

		SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));
		int startYear = monthlyStatistic.getStartYear();
		int startMonth = monthlyStatistic.getStartMonth();
		map.put(RECIPIENT_START_DATE, formatDateParam(startYear, startMonth, 1, format));
		map.put(RECIPIENT_STOP_DATE, formatDateParam(startYear, startMonth + 1, 0, format));
		
		map.put(TARGET_ID, monthlyStatistic.getTargetId());
		map.put(MAILING_LIST_ID, monthlyStatistic.getMailinglistId());
		map.put(HOUR_SCALE, monthlyStatistic.isHourScale());
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(UID, generateUid(admin));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		
		return generateUrlWithParamsForInternalAccess(map);
	}
	
	@Override
	public Map<String, String> getReportStatisticsUrlMap(List<ComBirtReportSettings> reportSettings, ComAdmin admin, Date currentDate, ComBirtReport report, int companyId, Integer accountId) throws Exception {
		HashMap<String, String> reportUrlMap = new HashMap<>();
		
		Locale locale = Locale.forLanguageTag(report.getLanguage());
		DateFormat dateFormat = BirtReportSettingsUtils.getLocalDateFormat(locale);
		String createDate = dateFormat.format(currentDate);
		String uid = generateUid(admin);
		int reportId = report.getId();
		String reportFormat = report.getFormatName();

		for (ComBirtReportSettings reportSetting : reportSettings) {
			if(reportSetting.isEnabled()) {
				Map<String, Object> map = new HashMap<>();

				map.put(REPORT_NAME, reportSetting.getReportName(reportFormat));
				map.put(COMPANY_ID, companyId);
				map.put(LANGUAGE, locale.getLanguage());
				map.put(ACCOUNT_ID, accountId);
				map.put(REPORT_ID, reportId);

				map.put(REPORT_CREATE_DATE, createDate);
				map.put(FORMAT, reportFormat);
				map.put(UID, uid);
				map.putAll(reportSetting.getReportUrlParameters());

				String reportSettingsName = BirtReportSettingsUtils.getLocalizedReportName(
						reportSetting, locale, reportFormat);

				reportUrlMap.put(reportSettingsName, generateUrlWithParamsForInternalAccess(map));
			}
		}

		return reportUrlMap;
	}
	
	@Override
	public String generateUid(ComAdmin admin) throws Exception {
        if (StringUtils.isBlank(publicKeyFilename)) {
            throw new Exception("Parameter 'birt.publickeyfile' is missing");
        }

		return RSACryptUtil.encrypt(UIDUtils.createUID(admin), RSACryptUtil.getPublicKey(publicKeyFilename));
	}
	
	@Override
	public String generateUrlWithParamsForInternalAccess(Map<String, Object> parameters) {
		String birtUrl = configService.getValue(ConfigValue.BirtUrlIntern);
		if (StringUtils.isBlank(birtUrl)) {
			birtUrl = configService.getValue(ConfigValue.BirtUrl);
		}
		
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(birtUrl + "/run");
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}
	
	@Override
	public String generateUrlWithParamsForExternalAccess(Map<String, Object> parameters) {
		String birtUrl = configService.getValue(ConfigValue.BirtUrl);
		
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(birtUrl + "/run");
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}

	private String formatDateParam(int year, int month, int date, SimpleDateFormat format) {
		Calendar calendarValue = new GregorianCalendar();
		calendarValue.set(year, month, date);
		return format.format(calendarValue.getTime());
	}

	protected String generateTargetBaseUrl() {
		return configService.getValue(ConfigValue.BirtDrilldownUrl);
	}
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setPublicKeyFilename(String publicKeyFilename) {
		this.publicKeyFilename = publicKeyFilename;
	}
}
