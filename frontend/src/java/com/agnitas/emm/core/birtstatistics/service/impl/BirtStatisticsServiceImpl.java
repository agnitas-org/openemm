/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.FileUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.optimization.dto.OptimizationStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;

public class BirtStatisticsServiceImpl implements BirtStatisticsService {
	private static final Logger logger = Logger.getLogger(BirtStatisticsServiceImpl.class);
	
	protected static final String REPORT_NAME = "__report";
	protected static final String IS_SVG = "__svg";
	protected static final String COMPANY_ID = "companyID";
	protected static final String ADMIN_ID = "adminID";
	protected static final String TARGET_ID = "targetID";
	protected static final String MAILING_LIST_ID = "mailinglistID";
	public static final String MAX_DOMAINS = "maxDomains";
	protected static final String LANGUAGE = "language";
	protected static final String SECURITY_TOKEN = "sec";
	protected static final String EMM_SESSION = "emmsession";
	protected static final String TARGET_BASE_URL = "targetBaseUrl";
	public static final String IS_TOP_LEVEL_DOMAIN = "topLevelDomain";
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
	
	private static final String RECIPIENTS_TYPE = "recipientsType";
	private static final String SELECTED_MAILINGS = "selectedMailings";

    private static final String OPTIMIZATION_ID = "optimizationID";

	private static final String URL_ID = "urlID";

	protected static final String BIRT_REPORT_TEMP_DIR = AgnUtils.getTempDir() + File.separator + "BirtReport";
	protected static final String BIRT_REPORT_TEMP_FILE_PATTERN = "birt-report-%d-body";
	
	public static final String MAILINGCOMPARE_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "MailingCompare";


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
		map.put(ADMIN_ID, admin.getAdminID());

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateUtilities.YYYYMMDD);

		int startYear = monthlyStatistic.getStartYear();
		int startMonth = monthlyStatistic.getStartMonth();
		LocalDate startDate = LocalDate.of(startYear, startMonth + 1, 1);

		map.put(START_DATE, DateUtilities.format(startDate, dateFormatter));
		map.put(END_DATE, DateUtilities.format(startDate.with(lastDayOfMonth()), dateFormatter));
		
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

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD);

		int startYear = monthlyStatistic.getStartYear();
		int startMonth = monthlyStatistic.getStartMonth();
		LocalDate startDate = LocalDate.of(startYear, startMonth + 1, 1);

		map.put(RECIPIENT_START_DATE, DateUtilities.format(startDate, dateFormatter));
		map.put(RECIPIENT_STOP_DATE, DateUtilities.format(startDate.with(lastDayOfMonth()), dateFormatter));

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
		if (locale == null) {
			locale = Locale.ENGLISH;
		}
		
		DateFormat dateFormat;
		if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} else {
			dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}
		
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
		String birtUrl = getBirtUrl(true);
		
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(birtUrl);
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}

	@Override
	public String generateUrlWithParamsForExternalAccess(Map<String, Object> parameters) {
		String birtUrl = getBirtUrl(false);
		
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(birtUrl);
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}
	
	protected String getBirtUrl(boolean internalAccess) {
    	String birtUrl = "";
    	if (internalAccess) {
			birtUrl = configService.getValue(ConfigValue.BirtUrlIntern);
		}
		
		if (StringUtils.isBlank(birtUrl)) {
			birtUrl = configService.getValue(ConfigValue.BirtUrl);
		}

        return birtUrl + "/run";
    }

	@Override
	public String getRecipientStatisticUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientStatisticDto recipientStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, recipientStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(MEDIA_TYPE, recipientStatistic.getMediaType());
		map.put(IS_SVG, true);

		SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(format.toPattern());

		LocalDate startDate = recipientStatistic.getLocaleStartDate();
		LocalDate endDate = recipientStatistic.getLocalEndDate();
		map.put(RECIPIENT_START_DATE, DateUtilities.format(startDate, dateFormatter));
		map.put(RECIPIENT_STOP_DATE, DateUtilities.format(endDate, dateFormatter));

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
	public String getMailingStatisticUrl(ComAdmin admin, String sessionId, MailingStatisticDto mailingStatistic) throws Exception {
		final String language = StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN");
		final String reportName = getReportName(mailingStatistic);
		final Map<String, Object> params = new HashMap<>();

		params.put(REPORT_NAME, reportName);
		params.put(IS_SVG, true);
		params.put(FORMAT, "html");
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(LANGUAGE, language);
		params.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		params.put(EMM_SESSION, sessionId);
		params.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

		if (DateMode.NONE != mailingStatistic.getDateMode()) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT_WITH_HOUR2).toPattern());
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(DateUtilities.YYYY_MM_DD).toPattern());
			final LocalDateTime startDate = Objects.requireNonNull(mailingStatistic.getStartDate());
			final LocalDateTime endDate = Objects.requireNonNull(mailingStatistic.getEndDate());
			String startDateStr;
			String endDateStr;
			if (mailingStatistic.getDateMode() == DateMode.LAST_TENHOURS) {
				startDateStr = startDate.format(dateTimeFormatter);
				endDateStr = endDate.format(dateTimeFormatter);
			} else {
				LocalDate start = startDate.toLocalDate();
				LocalDate end = endDate.toLocalDate();
				if (mailingStatistic.getDateMode() == DateMode.SELECT_DAY) {
					startDateStr = startDate.withHour(0).format(dateTimeFormatter);
					endDateStr = endDate.withHour(23).format(dateTimeFormatter);
				} else {
					startDateStr = start.format(dateFormatter);
					endDateStr = end.format(dateFormatter);
				}
			}
			params.put(RECIPIENT_START_DATE, startDateStr);
			params.put(RECIPIENT_STOP_DATE, endDateStr);
			params.put(HOUR_SCALE, mailingStatistic.isHourScale());
		}

		collectParametersAccordingToType(mailingStatistic.getType(), admin, mailingStatistic, params);

		return generateUrlWithParamsForExternalAccess(params);
	}
	
	private String getReportName(MailingStatisticDto statistic) {
    	if (statistic.getType() == null) {
    		return "";
		}
		
        switch (statistic.getType()) {
            case SUMMARY:
				return "mailing_summary.rptdesign";
			case SUMMARY_AUTO_OPT:
				return "mailing_summary_autoopt.rptdesign";
            case CLICK_STATISTICS_PER_LINK:
                return "mailing_linkclicks.rptdesign";
            case PROGRESS_OF_DELIVERY:
                return "mailing_delivery_progress.rptdesign";
            case PROGRESS_OF_OPENINGS:
                return "mailing_net_and_gross_openings_progress.rptdesign";
            case PROGRESS_OF_CLICKS:
                return "mailing_linkclicks_progress.rptdesign";
            case PROGRESS_OF_SINGLE_LINK_CLICKS:
                return "mailing_single_linkclicks_progress.rptdesign";
            case TOP_DOMAINS:
                return "top_domains.rptdesign";
            case BOUNCES:
                return "mailing_bounces.rptdesign";
            case BENCHMARK:
                return "mailing_benchmark.rptdesign";
            case DEVICES_OVERVIEW:
                return "mailing_devices_overview.rptdesign";
            case TRACKING_POINT_WEEK_OVERVIEW:
                return "mailing_tracking_point_week_overview.rptdesign";
            case SOCIAL_NETWORKS:
                return "mailing_social_networks.rptdesign";
            case SIMPLE_TRACKING_POINT:
                return "mailing_simple_tracking_point.rptdesign";
            case NUM_TRACKING_POINT_WEEK_OVERVIEW:
                return "mailing_num_tracking_point_week_overview.rptdesign";
            case ALPHA_TRACKING_POINT:
                return "mailing_alpha_tracking_point.rptdesign";
            default:
                //Do nothing
        }
        return "";
    }
	
	private void collectParametersAccordingToType(StatisticType type, ComAdmin admin, MailingStatisticDto mailingStatistic, Map<String, Object> params) {
    	if (type == StatisticType.SUMMARY_AUTO_OPT) {
			params.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
			params.put(OPTIMIZATION_ID, mailingStatistic.getOptimizationId());
			params.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));
			boolean showNetto = mailingStatistic.isShowNetto();
			params.put(SHOW_NET, showNetto);
			params.put(SHOW_GROSS, !showNetto);
			return;
		}
		
		params.put(MAILING_ID, mailingStatistic.getMailingId());
		params.put(SHORTNAME, StringUtils.trimToEmpty(mailingStatistic.getShortname()));
		params.put(DESCRIPTION, StringUtils.trimToEmpty(mailingStatistic.getDescription()));
		params.put(SELECTED_TARGETS, StringUtils.defaultString(StringUtils.join(mailingStatistic.getSelectedTargets(), ",")));
		params.put(RECIPIENT_TYPE, CommonKeys.TYPE_ALL_SUBSCRIBERS);
		
    	if(type == StatisticType.BOUNCES) {
			params.put(BOUNCETYPE, MailingBouncesDataSet.BounceType.BOTH.name());
		}
		if(type == StatisticType.PROGRESS_OF_CLICKS) {
			params.put(VIEW_CHART, true);
		}
		if(type == StatisticType.PROGRESS_OF_SINGLE_LINK_CLICKS) {
    		params.put(VIEW_CHART, true);
    		params.put(URL_ID, mailingStatistic.getLinkId());
		}
		if(type == StatisticType.SUMMARY) {
			params.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
			params.put(IS_ALOWED_DEEP_TRACKING, admin.permissionAllowed(Permission.DEEPTRACKING));
			params.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));
			boolean showNetto = mailingStatistic.isShowNetto();
			params.put(SHOW_NET, showNetto);
			params.put(SHOW_GROSS, !showNetto);
		}
		if(type == StatisticType.TOP_DOMAINS) {
			params.put(HIDE_SENT_STATS, !mailingStatistic.isMailtrackingActive() || !mailingStatistic.isMailtrackingExpired());
			params.put(IS_TOP_LEVEL_DOMAIN, mailingStatistic.isTopLevelDomain());
			params.put(MAX_DOMAINS, mailingStatistic.getMaxDomains());
		}
	}

	@Override
	public String changeFormat(String inputUrl, String newFormat) {
		if(StringUtils.isBlank(inputUrl)){
			return inputUrl;
		}
		return inputUrl.replaceAll("([&?]" + FORMAT + "=)\\w*(&+)", "$1" + newFormat + "$2");
	}
	
	@Override
	public String getMailingComparisonStatisticUrl(ComAdmin admin, String sessionId, MailingComparisonDto mailingComparisonDto) throws Exception {
    	Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, mailingComparisonDto.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(TARGET_ID, mailingComparisonDto.getTargetId());
		map.put(SELECTED_TARGETS, StringUtils.join(mailingComparisonDto.getTargetIds(), ","));
		map.put(RECIPIENTS_TYPE, mailingComparisonDto.getRecipientType());
		map.put(SELECTED_MAILINGS, StringUtils.join(mailingComparisonDto.getMailingIds(), ","));
		map.put(LANGUAGE, admin.getAdminLang());
		map.put(FORMAT, mailingComparisonDto.getReportFormat());
		map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		map.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
		
		return generateUrlWithParamsForExternalAccess(map);
	}
	
	@Override
	public File getBirtMailingComparisonTmpFile(String birtURL, MailingComparisonDto mailingComparisonDto) throws Exception {
		try {
			return exportBirtStatistic("mailing_compare_export_", "." + mailingComparisonDto.getReportFormat(),
					MAILINGCOMPARE_FILE_DIRECTORY, birtURL);
        } catch (Exception e) {
            logger.error("Cannot get birt mailings comparison file: " + e.getMessage());
			return null;
		}
	}
	
	@Override
    public File getBirtReportTmpFile(int birtReportId, String birtUrl, HttpClient httpClient, Logger loggerParameter) {
        try {
            return exportBirtStatistic(String.format(BIRT_REPORT_TEMP_FILE_PATTERN, birtReportId), ".tmp",
                    BIRT_REPORT_TEMP_DIR, birtUrl, httpClient, loggerParameter);
        } catch (Exception e) {
            loggerParameter.error("Cannot get birt report file: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String getRecipientStatusStatisticUrl(ComAdmin admin, String sessionId, RecipientStatusStatisticDto recipientStatusDto) throws Exception {
    	final Map<String, Object> params = new LinkedHashMap<>();

		params.put(REPORT_NAME, recipientStatusDto.getReportName());
		params.put(FORMAT, recipientStatusDto.getFormat());
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(MEDIA_TYPE, recipientStatusDto.getMediaType());
		params.put(TARGET_ID, recipientStatusDto.getTargetId());
		params.put(MAILING_LIST_ID, recipientStatusDto.getMailinglistId());
		params.put(LANGUAGE, admin.getAdminLang());
		params.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
		params.put(EMM_SESSION, sessionId);
		
		return generateUrlWithParamsForExternalAccess(params);
	}

    @Override
    public String getOptimizationStatisticUrl(ComAdmin admin, OptimizationStatisticDto optimizationDto) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(REPORT_NAME, optimizationDto.getReportName());
        map.put(FORMAT, optimizationDto.getFormat());
        map.put(MAILING_ID, optimizationDto.getMailingId());
        map.put(OPTIMIZATION_ID, optimizationDto.getOptimizationId());
        map.put(COMPANY_ID, optimizationDto.getCompanyId());
        map.put(SELECTED_TARGETS, StringUtils.join(optimizationDto.getTargetIds(), ","));
        map.put(LANGUAGE, admin.getAdminLang());

        map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
        map.put(RECIPIENT_TYPE, optimizationDto.getRecipientType());
        map.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
        map.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));

        return generateUrlWithParamsForExternalAccess(map);
    }
	
	protected File exportBirtStatistic(String prefix, String suffix, String dir, String birtURL, HttpClient httpClient, Logger loggerParameter) throws Exception {
		return FileUtils.downloadAsTemporaryFile(prefix, suffix, dir, birtURL, httpClient, loggerParameter);
	}
	
	protected File exportBirtStatistic(String prefix, String suffix, String dir, String birtURL) throws Exception {
		return FileUtils.downloadAsTemporaryFile(prefix, suffix, dir, birtURL, getBirtUrl(true));
	}

	protected String generateTargetBaseUrl() {
		return configService.getValue(ConfigValue.BirtDrilldownUrl);
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
