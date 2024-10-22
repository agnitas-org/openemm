/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.common.exceptions.ZipArchiveException;
import com.agnitas.emm.common.service.ZipArchiveService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import com.agnitas.emm.core.birtreport.service.BirtReportFileService;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.profilefields.form.ProfileFieldStatForm;
import com.agnitas.emm.core.userform.form.WebFormStatFrom;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.FileDownload;
import org.agnitas.util.FileUtils;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.agnitas.util.DateUtilities.toDate;

public class BirtStatisticsServiceImpl implements BirtStatisticsService {
	private static final Logger logger = LogManager.getLogger(BirtStatisticsServiceImpl.class);

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
	protected static final String WORKFLOW_ID = "workflowId";
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
	private static final String FORM_ID = "formID";

	private static final String DARKMODE = "darkmode";

	private static final String SUCCESS_EXPIRE_PERIOD = "successExpirePeriod";
	private static final String ONEPIXEL_EXPIRE_PERIOD = "onepixelExpirePeriod";

	protected static final String BIRT_REPORT_TEMP_DIR = AgnUtils.getTempDir() + File.separator + "BirtReport";
	protected static final String BIRT_REPORT_TEMP_FILE_PATTERN = "birt-report-%d-body";

	public static final String MAILINGCOMPARE_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "MailingCompare";
    private static final String HIDDEN_TARGET_ID = "hiddenTargetId";
    private static final String COL_NAME = "colName";
    private static final String LIMIT = "limit";

	protected ConfigService configService;
	protected AdminService adminService;
	private BirtReportFileService birtReportFileService;
	private ZipArchiveService<BirtReportDownload> zipArchiveService;

    @Override
	public String getDomainStatisticsUrlWithoutFormat(Admin admin, String sessionId, DomainStatisticDto domainStatistic, boolean forInternalUse) throws Exception {
		Map<String, Object> map = new LinkedMap<>();

		map.put(REPORT_NAME, domainStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(TARGET_ID, domainStatistic.getTargetId());
		map.put(MAILING_LIST_ID, domainStatistic.getMailingListId());
		map.put(MAX_DOMAINS, domainStatistic.getMaxDomainNum());
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		map.put(IS_TOP_LEVEL_DOMAIN, domainStatistic.isTopLevelDomain());

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(forInternalUse)
				.setAdmin(admin)
				.setParameters(map)
				.build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

    @Override
    public String getProfileFieldEvalStatUrl(Admin admin, String sessionId, ProfileFieldStatForm form) throws Exception {
		Map<String, Object> params = Map.of(
			REPORT_NAME, form.getReportName(),
			COMPANY_ID, admin.getCompanyID(),
			TARGET_ID, form.getTargetId(),
			MAILING_LIST_ID, form.getMailingListId(),
			LIMIT, form.getLimit(),
			COL_NAME, form.getColName(),
			EMM_SESSION, sessionId,
			TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
			.setInternalAccess(false)
			.setAdmin(admin)
			.setParameters(params)
			.build();
        return generateUrlWithParams(options, admin.getCompanyID());
    }

	@Override
	public String getMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, MonthlyStatisticDto monthlyStatistic, boolean forInternalUse) throws Exception {
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
		map.put(SESSION_ID, sessionId);
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL, generateTargetBaseUrl());

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(forInternalUse)
				.setParameters(map)
				.setAdmin(admin).build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	public String getRecipientMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, RecipientProgressStatisticDto monthlyStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, monthlyStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(MEDIA_TYPE, monthlyStatistic.getMediaType());

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD);

		int startYear = monthlyStatistic.getStartYear();
		int startMonth = monthlyStatistic.getStartMonth();
		LocalDate startDate = LocalDate.of(startYear, startMonth + 1, 1);

		map.put(RECIPIENT_START_DATE, DateUtilities.format(startDate, dateFormatter));
		map.put(RECIPIENT_STOP_DATE, DateUtilities.format(startDate.with(lastDayOfMonth()), dateFormatter));

		map.put(TARGET_ID, monthlyStatistic.getTargetId());
		map.put(MAILING_LIST_ID, monthlyStatistic.getMailinglistId());
		map.put(HOUR_SCALE, monthlyStatistic.isHourScale());
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

        BirtUrlOptions options = getOptionsBuilderForMonthlyStatisticUrl(admin, map, monthlyStatistic.getTargetId()).build();
		return generateUrlWithParams(options, admin.getCompanyID());
	}

	protected BirtUrlOptions.Builder getOptionsBuilderForMonthlyStatisticUrl(Admin admin, Map<String, Object> map, int targetId) {
        return BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(false)
				.setAdmin(admin)
				.setParameters(map);
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

				String reportSettingsName = birtReportFileService.buildLocalizedFileName(reportSetting, companyId, locale, reportFormat);

				reportUrlMap.put(reportSettingsName, generateUrlWithParams(map, true, companyId));
			}
		}

		return reportUrlMap;
	}

	@Override
	public String generateUrlWithParams(Map<String, Object> parameters, boolean internalAccess, final int companyID) {
		String birtUrl = getBirtUrl(internalAccess, companyID);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(birtUrl);
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}

	public String generateUrlWithParams(BirtUrlOptions options, final int companyID) {
		return generateUrlWithParams(options.getParameters(), options.isInternalAccess(), companyID);
	}

	protected String getBirtUrl(boolean internalAccess, final int companyID) {
    	String birtUrl = "";
    	if (internalAccess) {
			birtUrl = configService.getValue(ConfigValue.BirtUrlIntern, companyID);
		}

		if (StringUtils.isBlank(birtUrl)) {
			birtUrl = configService.getValue(ConfigValue.BirtUrl, companyID);
		}

        return birtUrl + "/run";
    }

	@Override
	public String getRecipientStatisticUrlWithoutFormat(Admin admin, String sessionId, RecipientStatisticDto recipientStatistic) throws Exception {
		Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, recipientStatistic.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(MEDIA_TYPE, recipientStatistic.getMediaType());

		SimpleDateFormat format = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(format.toPattern());

		LocalDate startDate = recipientStatistic.getLocaleStartDate();
		LocalDate endDate = recipientStatistic.getLocalEndDate();
		map.put(RECIPIENT_START_DATE, DateUtilities.format(startDate, dateFormatter));
		map.put(RECIPIENT_STOP_DATE, DateUtilities.format(endDate, dateFormatter));

		map.put(TARGET_ID, recipientStatistic.getTargetId());
		map.put(MAILING_LIST_ID, recipientStatistic.getMailinglistId());
		map.put(HOUR_SCALE, recipientStatistic.isHourScale());
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

        BirtUrlOptions options = getOptionsBuilderForRecipientStatisticUrl(admin, recipientStatistic, map).build();
		return generateUrlWithParams(options, admin.getCompanyID());
	}

    protected BirtUrlOptions.Builder getOptionsBuilderForRecipientStatisticUrl(Admin admin, RecipientStatisticDto recipientStatistic, Map<String, Object> map) {
        return BirtUrlOptions.builder(configService, adminService)
                .setAdmin(admin)
                .setParameters(map)
                .setInternalAccess(false);
    }

	@Override
	public String getMailingStatisticUrl(Admin admin, String sessionId, MailingStatisticDto mailingStatistic) throws Exception {
		final String reportName = getReportName(mailingStatistic);
		final Map<String, Object> params = new HashMap<>();

		params.put(REPORT_NAME, reportName);
		params.put(FORMAT, "html");
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(EMM_SESSION, sessionId);
		params.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

		DateMode dateMode = mailingStatistic.getDateMode();
		if (DateMode.NONE != dateMode) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT_WITH_HOUR2).toPattern());
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(DateUtilities.YYYY_MM_DD).toPattern());

			final Tuple<LocalDateTime, LocalDateTime> dateRestrictions =
					computeMailingSummaryDateRestrictions(mailingStatistic.getStartDate(), mailingStatistic.getEndDate(), dateMode);

			String startDateStr;
			String endDateStr;
			if (dateMode == DateMode.LAST_TENHOURS || dateMode == DateMode.SELECT_DAY) {
				startDateStr = dateRestrictions.getFirst().format(dateTimeFormatter);
				endDateStr = dateRestrictions.getSecond().format(dateTimeFormatter);
			} else {
				startDateStr = dateRestrictions.getFirst().toLocalDate().format(dateFormatter);
				endDateStr = dateRestrictions.getSecond().toLocalDate().format(dateFormatter);
			}
			params.put(RECIPIENT_START_DATE, startDateStr);
			params.put(RECIPIENT_STOP_DATE, endDateStr);
			params.put(HOUR_SCALE, mailingStatistic.isHourScale());
			params.put(SUCCESS_EXPIRE_PERIOD, configService.getIntegerValue(ConfigValue.ExpireSuccess));
			params.put(ONEPIXEL_EXPIRE_PERIOD, configService.getIntegerValue(ConfigValue.ExpireStatistics));
		}

		collectParametersAccordingToType(mailingStatistic.getType(), admin, mailingStatistic, params);

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(false)
				.setParameters(params)
				.setAdmin(admin).build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	private Tuple<LocalDateTime, LocalDateTime> computeMailingSummaryDateRestrictions(LocalDateTime startDate, LocalDateTime endDate, DateMode dateMode) {
		if (dateMode == DateMode.SELECT_DAY) {
			if (startDate == null) {
				startDate = LocalDateTime.now();
			}

			return new Tuple<>(startDate.withHour(0), startDate.plusDays(1).withHour(0));
		}

		if (endDate == null) {
			endDate = LocalDateTime.now();
		}

		if (startDate == null) {
			if (dateMode == DateMode.LAST_TENHOURS) {
				startDate = endDate.minusHours(10);
			} else if (dateMode == DateMode.LAST_DAY) {
				startDate = endDate.minusDays(1);
			} else if (dateMode == DateMode.LAST_FORTNIGHT) {
				startDate = endDate.minusDays(14);
			} else if (dateMode == DateMode.LAST_MONTH) {
				startDate = endDate.minusMonths(1);
			} else if (dateMode == DateMode.LAST_WEEK) {
				startDate = endDate.minusWeeks(1);
			} else if (dateMode == DateMode.SELECT_MONTH) {
				startDate = endDate.minusMonths(1);
			} else {
				startDate = endDate.minusDays(1);
			}
		}

		return new Tuple<>(startDate, endDate);
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

	private void collectParametersAccordingToType(StatisticType type, Admin admin, MailingStatisticDto mailingStatistic, Map<String, Object> params) {
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
	public String getMailingComparisonStatisticUrl(Admin admin, String sessionId, MailingComparisonDto mailingComparisonDto) throws Exception {
    	Map<String, Object> map = new LinkedMap<>();
		map.put(REPORT_NAME, mailingComparisonDto.getReportName());
		map.put(COMPANY_ID, admin.getCompanyID());
		map.put(TARGET_ID, mailingComparisonDto.getTargetId());
		map.put(SELECTED_TARGETS, StringUtils.join(mailingComparisonDto.getTargetIds(), ","));
		map.put(RECIPIENTS_TYPE, mailingComparisonDto.getRecipientType());
		map.put(SELECTED_MAILINGS, StringUtils.join(mailingComparisonDto.getMailingIds(), ","));
		map.put(FORMAT, mailingComparisonDto.getReportFormat());
		map.put(EMM_SESSION, sessionId);
		map.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());
		map.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));

        BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(false)
				.setParameters(map)
				.setAdmin(admin).build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	public File getBirtMailingComparisonTmpFile(String birtURL, MailingComparisonDto mailingComparisonDto, final int companyId) {
		try {
			return exportBirtStatistic("mailing_compare_export_", "." + mailingComparisonDto.getReportFormat(),
					MAILINGCOMPARE_FILE_DIRECTORY, birtURL, companyId);
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
	public File getBirtReportTmpFile(final int birtReportId, final String birtUrl, final CloseableHttpClient httpClient, final Logger loggerForErrors) {
        try {
            return exportBirtStatistic(
            		String.format(BIRT_REPORT_TEMP_FILE_PATTERN, birtReportId),
            		".tmp",
                    BIRT_REPORT_TEMP_DIR,
                    birtUrl,
                    httpClient);
        } catch (Exception e) {
        	loggerForErrors.error("Cannot get birt report file: " + e.getMessage());
            return null;
        }
	}

	@Override
    public String getRecipientStatusStatisticUrl(Admin admin, String sessionId, RecipientStatusStatisticDto recipientStatusDto) throws Exception {
    	final Map<String, Object> params = new LinkedHashMap<>();

		params.put(REPORT_NAME, recipientStatusDto.getReportName());
		params.put(FORMAT, recipientStatusDto.getFormat());
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(MEDIA_TYPE, recipientStatusDto.getMediaType());
		params.put(TARGET_ID, recipientStatusDto.getTargetId());
		params.put(MAILING_LIST_ID, recipientStatusDto.getMailinglistId());
		params.put(EMM_SESSION, sessionId);

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(false)
				.setParameters(params)
				.setAdmin(admin).build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	public boolean isWorldMailing(Mailing mailing) {
		Set<MaildropEntry> maildropStatus = mailing.getMaildropStatus();

		List<Character> invalidStatuses = Arrays.stream(MaildropStatus.values())
				.filter(status -> !status.equals(MaildropStatus.WORLD))
				.map(MaildropStatus::getCode)
				.collect(Collectors.toList());

		return maildropStatus.stream()
				.noneMatch(status -> invalidStatuses.contains(status.getStatus()));
	}

	@Override
	public String getWorkflowStatisticUrl(Admin admin, WorkflowStatisticDto workflowStatisticDto) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(REPORT_NAME, workflowStatisticDto.getReportName());
		map.put(FORMAT, workflowStatisticDto.getFormat());
		map.put(WORKFLOW_ID, workflowStatisticDto.getWorkflowId());
		map.put(COMPANY_ID, workflowStatisticDto.getCompanyId());

		map.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
		map.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setParameters(map)
				.setAdmin(admin)
				.setInternalAccess(false)
				.build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	public String getWebFormStatUrl(WebFormStatFrom form, Admin admin, String sessionId) throws Exception {
		DateRange period = getWebFormStatPeriod(form, AgnUtils.getTimeZone(admin).toZoneId());
		Map<String, Object> map = Map.of(
			REPORT_NAME, "form_click_statistics.rptdesign",
			COMPANY_ID, admin.getCompanyID(),
			START_DATE, new SimpleDateFormat(DateUtilities.YYYY_MM_DD).format(period.getFrom()),
			END_DATE, new SimpleDateFormat(DateUtilities.YYYY_MM_DD).format(period.getTo()),
			EMM_SESSION, sessionId,
			FORM_ID, form.getFormId()
		);
		return generateUrlWithParams(BirtUrlOptions.builder(configService, adminService)
			.setAdmin(admin)
			.setParameters(map)
			.setInternalAccess(false).build(), admin.getCompanyID());
	}

	private DateRange getWebFormStatPeriod(WebFormStatFrom form, ZoneId zoneId) {
		if (form.getPeriodMode() == DateMode.SELECT_MONTH) {
			YearMonth yearMonth = YearMonth.of(form.getYear(), form.getMonth());
			LocalDate from = yearMonth.atDay(1);
			LocalDate to = yearMonth.plusMonths(1).atDay(1);
			return new DateRange(toDate(from, zoneId), toDate(to, zoneId));
		}
		if (form.getPeriodMode() == DateMode.SELECT_YEAR) {
			LocalDate from = Year.of(form.getYear()).atDay(1);
			LocalDate to = Year.of(form.getYear() + 1).atDay(1);
			return new DateRange(toDate(from, zoneId), toDate(to, zoneId));
		}
		return form.getDateRange();
	}

	@Override
	public String getUserFormTrackableLinkStatisticUrl(Admin admin, String sessionId, int formId) throws Exception {
    	Map<String, Object> map = new HashMap<>();
    	map.put(REPORT_NAME, "form_click_statistics.rptdesign");
    	map.put(COMPANY_ID, admin.getCompanyID());
		map.put(EMM_SESSION, sessionId);
		map.put(FORM_ID, formId);

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setAdmin(admin)
				.setParameters(map)
				.setInternalAccess(false).build();

        return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	public String getReportStatisticsUrl(Admin admin, BirtReportStatisticDto settings) throws Exception {
		int companyId = admin.getCompanyID();
		Map<String, Object> map = new HashMap<>();
		map.put(REPORT_NAME, settings.getReportName());
		map.put(COMPANY_ID, companyId);
		map.put(ACCOUNT_ID, companyId);
		map.put(REPORT_ID, settings.geReportId());

		DateFormat dateFormat = admin.getDateFormat();
		map.put(REPORT_CREATE_DATE, dateFormat.format(new Date()));
		map.put(FORMAT, settings.getReportFormat());
		map.putAll(settings.getReportUrlParameters());

		BirtUrlOptions options;
		if (configService.isExtendedAltgEnabled(admin.getCompanyID())) {
			options = BirtUrlOptions.builder(configService, adminService)
					.setAdmin(admin)
					.setAltgMatcher(a -> false)
					.setParameters(map)
					.setInternalAccess(true)
					.build();
		} else {
			options = BirtUrlOptions.builder(configService, adminService)
					.setAdmin(admin)
					.setAltgChecker((a) -> false)
					.setParameters(map)
					.setInternalAccess(true)
					.build();
		}

		return generateUrlWithParams(options, companyId);
	}

	@Override
	public File getBirtReportTmpFile(BirtReportDownload birtDownload, final int companyId) {
		try {
			int birtReportId = birtDownload.getReportId();
			return exportBirtStatistic(String.format(BIRT_REPORT_TEMP_FILE_PATTERN, birtReportId), ".tmp", BIRT_REPORT_TEMP_DIR, birtDownload.getBirtFileUrl(), companyId);
		} catch (Exception e) {
			logger.error("Cannot get birt report file: " + e.getMessage());
			return null;
		}
	}

	@Override
	public File getBirtReportTmpFile(List<BirtReportDownload> birtDownloads, int companyId) {
		Map<BirtReportDownload, File> filesMap = birtDownloads.stream()
				.collect(Collectors.toMap(Function.identity(), bd -> getBirtReportTmpFile(bd, companyId)));

		List<BirtReportDownload> downloadsWithFiles = filesMap.entrySet().stream()
				.filter(e -> e.getValue() != null && e.getValue().exists())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		if (downloadsWithFiles.size() == 1) {
			return filesMap.get(downloadsWithFiles.get(0));
		}

		try {
			return zipArchiveService.createZipArchive(downloadsWithFiles, "birt_reports_", d -> {
				String fileName = d.getFileName();
				try {
					return new Tuple<>(fileName, org.apache.commons.io.FileUtils.readFileToByteArray(filesMap.get(d)));
				} catch (IOException e) {
					logger.error("Error occurred when trying to read birt report file: " + e.getMessage());
					return new Tuple<>(fileName, null);
				}
			});
		} catch (ZipArchiveException e) {
			logger.error("Error occurred when create zip archive! Message: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @see #exportBirtStatistic(String, String, String, String, CloseableHttpClient)
	 */
	@Deprecated
	protected File exportBirtStatistic(String prefix, String suffix, String dir, String birtURL, HttpClient httpClient, Logger loggerParameter) throws Exception {
		return FileUtils.downloadAsTemporaryFile(prefix, suffix, dir, birtURL, httpClient, loggerParameter);
	}

	@Deprecated
	protected File exportBirtStatistic(String prefix, String suffix, String dir, String birtURL, final int companyId) throws Exception {
		return FileUtils.downloadAsTemporaryFile(prefix, suffix, dir, birtURL, getBirtUrl(true, companyId));
	}

	protected File exportBirtStatistic(final String prefix, final String suffix, final String dir, final String url, final CloseableHttpClient httpClient) throws Exception {
		final File file = File.createTempFile(prefix, suffix, AgnUtils.createDirectory(dir));

		final boolean result = FileDownload.downloadAsFile(url, file, httpClient);

		return result ? file : null;
	}

	protected String generateTargetBaseUrl() {
		return configService.getValue(ConfigValue.SystemUrl);
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@Required
	public void setBirtReportFileService(BirtReportFileService birtReportFileService) {
		this.birtReportFileService = birtReportFileService;
	}

	@Required
	public void setZipArchiveService(ZipArchiveService zipArchiveService) {
		this.zipArchiveService = zipArchiveService;
	}

	protected static class BirtUrlOptions {
		private Map<String, Object> parameters;
		private boolean internalAccess;

		public static Builder builder(ConfigService configService, AdminService adminService) {
			return new Builder(configService, adminService);
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public boolean isInternalAccess() {
			return internalAccess;
		}

		protected static class Builder {
			private Admin admin;
			private boolean internalAccess = true;
			private Map<String, Object> defaultParameters = new HashMap<>();
			private Map<String, Object> parameters = new HashMap<>();
			private Function<Integer, Boolean> altgChecker = null;
            private Function<Set<Integer>, Boolean> altgMatcher = null;

			private ConfigService configService;
			private AdminService adminService;

			public Builder(ConfigService configService, AdminService adminService) {
				this.configService = configService;
				this.adminService = adminService;
			}

			public Builder setAdmin(Admin admin) {
				this.admin = admin;
				return this;
			}

			public Builder setInternalAccess(boolean internalAccess) {
				this.internalAccess = internalAccess;
				return this;
			}

			public Builder setParameters(Map<String, Object> parameters) {
				this.parameters.clear();
				this.parameters.putAll(Objects.requireNonNull(parameters));
				return this;
			}

			public Builder addParameter(String name, Object value) {
				parameters.put(name, value);
				return this;
			}

			public Builder setAltgChecker(Function<Integer, Boolean> function) {
				this.altgChecker = function;
				return this;
			}

            public Builder setAltgMatcher(Function<Set<Integer>, Boolean> function) {
                this.altgMatcher = function;
                return this;
            }

			protected BirtUrlOptions build() throws Exception {
				BirtUrlOptions options = new BirtUrlOptions();

				Objects.requireNonNull(admin);

				if (parameters.get(REPORT_NAME) == null) {
					throw new IllegalArgumentException("Report name can not be empty!");
				}


				defaultParameters.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
				defaultParameters.put(LANGUAGE, StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN"));
				defaultParameters.put(IS_SVG, true);
				
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Security token for accessing BIRT is: %s", defaultParameters.get(SECURITY_TOKEN)));
				}

				if (adminService.isExtendedAltgEnabled(admin)) {
                    Set<Integer> adminAltgIds = admin.getAltgIds();
                    if (altgMatcher != null) {
                        if (Boolean.TRUE.equals(altgMatcher.apply(adminAltgIds))) {
                            defaultParameters.put(HIDDEN_TARGET_ID, adminAltgIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
                        }
                    } else if (CollectionUtils.isNotEmpty(adminAltgIds)) {
                        defaultParameters.put(HIDDEN_TARGET_ID, adminAltgIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
                    }
                } else {
                    int altgId = adminService.getAccessLimitTargetId(admin);

    				if (altgChecker != null) {
    					if (altgChecker.apply(altgId)) {
    						defaultParameters.put(HIDDEN_TARGET_ID, altgId);
    					}
    				} else if (altgId > 0) {
    					defaultParameters.put(HIDDEN_TARGET_ID, altgId);
    				}
                }

				defaultParameters.put(DARKMODE, adminService.isDarkmodeEnabled(admin));

				options.parameters = new HashMap<>();
				options.parameters.putAll(defaultParameters);
				options.parameters.putAll(parameters);
				options.internalAccess = internalAccess;

				return options;
			}

		}
	}
}
