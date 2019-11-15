/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.reporting.birt.util.URLUtils;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

public class BirtStatisticsServiceImpl implements BirtStatisticsService {
	protected static final String REPORT_NAME = "__report";
	protected static final String IS_SVG = "__svg";
	protected static final String COMPANY_ID = "companyID";
	protected static final String TARGET_ID = "targetID";
	protected static final String MAILING_LIST_ID = "mailinglistID";
	protected static final String MAX_DOMAINS = "maxdomains";
	protected static final String LANGUAGE = "language";
	protected static final String SECURITY_TOKEN = "sec";
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
	
	protected static final String DEVICE_STATISTIC_TYPE = "statisticType";

	protected static final String TRACKING_ALLOWED = "trackingAllowed";
	private static final String IS_ALOWED_DEEP_TRACKING = "isAlowedDeeptracking";
	private static final String SHORTNAME = "shortname";
	private static final String DESCRIPTION = "description";
	protected static final String SELECTED_TARGETS = "selectedTargets";
	protected static final String RECIPIENT_TYPE = "recipientType";
	private static final String BOUNCETYPE = "bouncetype";
	private static final String VIEW_CHART = "viewChart";
	protected static final String SHOW_SOFT_BOUNCES = "showSoftbounces";
	private static final String SHOW_NET = "showNet";
	private static final String SHOW_GROSS = "showGross";
	private static final String HIDE_SENT_STATS = "hideSentStats";

	private static final SimpleDateFormat DATE_HOUR_FORMAT = new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT_WITH_HOUR2);

	protected ConfigService configService;

    @Override
	public String getDomainStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, DomainStatisticDto domainStatistic, boolean forInternalUse) throws Exception {
		Map<String, Object> map = new LinkedMap<>();

		map.put(REPORT_NAME, domainStatistic.getReportName());
		map.put(IS_SVG, true);
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(TARGET_ID, domainStatistic.getTargetId());
		map.put(MAILING_LIST_ID, domainStatistic.getMailingListId());
		map.put(MAX_DOMAINS, domainStatistic.getMaxDomainNum());
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		map.put(IS_TOP_LEVEL_DOMAIN, domainStatistic.isTopLevelDomain());

		if (forInternalUse) {
			return generateUrlWithParamsForInternalAccess(map);
		} else {
			return generateUrlWithParamsForExternalAccess(map);
		}
	}

	@Override
	public String getMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, MonthlyStatisticDto monthlyStatistic, boolean forInternalUse) throws Exception {
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
		map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		map.put(SESSION_ID, sessionId);
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL, generateTargetBaseUrl());
		
		if (forInternalUse) {
			return generateUrlWithParamsForInternalAccess(map);
		} else {
			return generateUrlWithParamsForExternalAccess(map);
		}
	}

	@Override
	public String getRecipientMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientProgressStatisticDto monthlyStatistic) throws Exception {
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
		map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		
		return generateUrlWithParamsForExternalAccess(map);
	}
	
	@Override
	public Map<String, String> getReportStatisticsUrlMap(List<ComBirtReportSettings> reportSettings, Date currentDate, ComBirtReport report, int companyId, Integer accountId) throws Exception {
		HashMap<String, String> reportUrlMap = new HashMap<>();

		Locale locale = Locale.forLanguageTag(report.getLanguage());
		DateFormat dateFormat = BirtReportSettingsUtils.getLocalDateFormat(locale);
		String createDate = dateFormat.format(currentDate);
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
				map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, companyId));
				map.putAll(reportSetting.getReportUrlParameters());

				String reportSettingsName = BirtReportSettingsUtils.getLocalizedReportName(
						reportSetting, locale, reportFormat);

				reportUrlMap.put(reportSettingsName, generateUrlWithParamsForInternalAccess(map));
			}
		}

		return reportUrlMap;
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

	@Override
	public String getRecipientStatisticUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientStatisticDto recipientStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, recipientStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(MEDIA_TYPE, recipientStatistic.getMediaType());
		map.put(IS_SVG, true);

		SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));
		LocalDate startDate = recipientStatistic.getLocaleStartDate();
		LocalDate endDate = recipientStatistic.getLocalEndDate();
		map.put(RECIPIENT_START_DATE, formatDateParam(startDate.getYear(), startDate.getMonthValue() - 1, startDate.getDayOfMonth(), format));
		map.put(RECIPIENT_STOP_DATE, formatDateParam(endDate.getYear(), endDate.getMonthValue() - 1, endDate.getDayOfMonth(), format));

		map.put(TARGET_ID, recipientStatistic.getTargetId());
		map.put(MAILING_LIST_ID, recipientStatistic.getMailinglistId());
		map.put(HOUR_SCALE, recipientStatistic.isHourScale());
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

		return generateUrlWithParamsForExternalAccess(map);
	}

	@Override
	public String getMailingStatisticUrl(final ComAdmin admin, final String sessionId, final MailingStatisticDto mailingStatistic) throws Exception {

		final String language = StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN");
		final LocalDate startDate = mailingStatistic.getStartDate();
		final LocalDate endDate = mailingStatistic.getEndDate();
		final String reportName = mailingStatistic.getReportName();
		final SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));

		final Map<String, Object> params = new HashMap<>();

		params.put(REPORT_NAME, reportName);
		params.put(IS_SVG, true);
		params.put(FORMAT, "html");
		params.put(MAILING_ID, mailingStatistic.getMailingId());
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(SHORTNAME, StringUtils.trimToEmpty(mailingStatistic.getShortname()));
		params.put(DESCRIPTION, StringUtils.trimToEmpty(mailingStatistic.getDescription()));
		params.put(SELECTED_TARGETS, mailingStatistic.getСoncatenatedSelectedTargets());
		params.put(LANGUAGE, language);
		params.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		params.put(EMM_SESSION, sessionId);
		params.put(TARGET_BASE_URL.toLowerCase(), URLUtils.encodeURL(generateTargetBaseUrl()));
		params.put(RECIPIENT_TYPE, CommonKeys.TYPE_ALL_SUBSCRIBERS);

		if (DateMode.NONE != mailingStatistic.getDateMode()) {
			String startDateStr;
			String endDateStr;
			if (mailingStatistic.getDateMode() == DateMode.LAST_TENHOURS) {
				Calendar cal = Calendar.getInstance();
				if(mailingStatistic.getMailingStartDate() != null ) {
					cal.setTime(mailingStatistic.getMailingStartDate());
				}
				startDateStr = DATE_HOUR_FORMAT.format(cal.getTime());
				cal.add(Calendar.HOUR_OF_DAY, 10);
				endDateStr = DATE_HOUR_FORMAT.format(cal.getTime());
			} else {
				startDateStr = formatDateParam(startDate.getYear(), startDate.getMonthValue() - 1, startDate.getDayOfMonth(), format);
				endDateStr = formatDateParam(endDate.getYear(), endDate.getMonthValue() - 1, endDate.getDayOfMonth(), format);
				if (mailingStatistic.getDateMode() == DateMode.SELECT_DAY) {
					startDateStr += ":00";
					endDateStr += ":23";
				}
			}
			params.put(RECIPIENT_START_DATE, startDateStr);
			params.put(RECIPIENT_STOP_DATE, endDateStr);
			params.put(HOUR_SCALE, mailingStatistic.isHourScale());
		}

		if(mailingStatistic.getType() == StatisticType.BOUNCES){
			params.put(BOUNCETYPE, MailingBouncesDataSet.BounceType.BOTH.name());
		} else if(mailingStatistic.getType() == StatisticType.PROGRESS_OF_CLICKS){
			params.put(VIEW_CHART, true);
		} else if(mailingStatistic.getType() == StatisticType.SUMMARY) {
			params.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
			params.put(IS_ALOWED_DEEP_TRACKING, admin.permissionAllowed(Permission.DEEPTRACKING));
			params.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));
			params.put(SHOW_NET, mailingStatistic.isShowNetto());
			params.put(SHOW_GROSS, !mailingStatistic.isShowNetto());
		} else if(mailingStatistic.getType() == StatisticType.TOP_DOMAINS){
			params.put(HIDE_SENT_STATS, !mailingStatistic.isMailtrackingActive() || !mailingStatistic.isMailtrackingExpired());
			params.put(IS_TOP_LEVEL_DOMAIN, mailingStatistic.isTopLevelDomain());
			params.put(MAX_DOMAINS, mailingStatistic.getMaxDomains());
		}

		return generateUrlWithParamsForExternalAccess(params);
	}

	@Override
	public String changeFormat(final String inputUrl, final String newFormat) {
		if(StringUtils.isBlank(inputUrl)){
			return inputUrl;
		}
		return inputUrl.replaceAll("([&?]" + FORMAT + "=)\\w*(&+)", "$1" + newFormat + "$2");
	}
	
	@Override
    public String getRecipientStatusStatisticUrl(ComAdmin admin, String sessionId, RecipientStatusStatisticDto recipientStatusDto) throws Exception {
    	final Map<String, Object> params = new HashMap<>();

		params.put(REPORT_NAME, recipientStatusDto.getReportName());
		params.put(FORMAT, recipientStatusDto.getFormat());
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(MEDIA_TYPE, recipientStatusDto.getMediaType());
		params.put(TARGET_ID, recipientStatusDto.getTargetId());
		params.put(MAILING_LIST_ID, recipientStatusDto.getMailinglistId());
		params.put(LANGUAGE, admin.getAdminLang());
		params.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		params.put(EMM_SESSION, sessionId);
		
		return generateUrlWithParamsForInternalAccess(params);
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
}
