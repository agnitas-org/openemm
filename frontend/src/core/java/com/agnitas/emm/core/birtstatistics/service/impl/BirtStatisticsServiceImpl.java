/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.ReportGenerationHeaders;
import com.agnitas.emm.core.birtreport.ReportGenerationTokenService;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.birtreport.service.BirtReportFileService;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet;
import com.agnitas.reporting.birt.external.dataset.RecipientStatDataSet;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.FileDownload;
import com.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

// TODO: GWUA-7067: Remove redundant constants when removing old code
public class BirtStatisticsServiceImpl implements BirtStatisticsService {

	private static final Logger logger = LogManager.getLogger(BirtStatisticsServiceImpl.class);

	public static final String MAX_DOMAINS = "maxDomains";
	public static final String IS_TOP_LEVEL_DOMAIN = "topLevelDomain";
	private static final String COMPANY_ID = "companyID";
	private static final String FORMAT = "__format";
	private static final String START_DATE = "startDate";
	private static final String STOP_DATE = "stopDate";
	private static final String REPORT_ID = "reportId";
	private static final String DARKMODE = "darkmode";
	private static final String HIDDEN_TARGET_ID = "hiddenTargetId";
	private static final String LANGUAGE = "language";
	private static final String REPORT_CREATE_DATE = "reportCreateDate";
	protected static final String REPORT_NAME = "__report";
	protected static final String IS_SVG = "__svg";
	protected static final String TARGET_ID = "targetID";
	protected static final String SECURITY_TOKEN = "sec";
	protected static final String EMM_SESSION = "emmsession";
	protected static final String TARGET_BASE_URL = "targetBaseUrl";
	protected static final String HOUR_SCALE = "hourScale";
	protected static final String ACCOUNT_ID = "accountId";
	protected static final String MAILING_ID = "mailingID";
	protected static final String WORKFLOW_ID = "workflowId";
	protected static final String TRACKING_ALLOWED = "trackingAllowed";
	private static final String IS_ALOWED_DEEP_TRACKING = "isAlowedDeeptracking";
	private static final String SHORTNAME = "shortname";
	protected static final String SELECTED_TARGETS = "selectedTargets";
	private static final String DESCRIPTION = "description";
	protected static final String RECIPIENT_TYPE = "recipientType";
	private static final String BOUNCETYPE = "bouncetype";
	private static final String VIEW_CHART = "viewChart";
	private static final String SHOW_NET = "showNet";
	private static final String SHOW_GROSS = "showGross";
	private static final String HIDE_SENT_STATS = "hideSentStats";
	private static final String OPTIMIZATION_ID = "optimizationID";
	private static final String URL_ID = "urlID";
	protected static final String SHOW_SOFT_BOUNCES = "showSoftbounces";

	private static final String BIRT_REPORT_TEMP_DIR = AgnUtils.getTempDir() + File.separator + "BirtReport";
	private static final String BIRT_REPORT_TEMP_FILE_PATTERN = "birt-report-%d-body";

	protected ConfigService configService;
	protected AdminService adminService;
	private BirtReportFileService birtReportFileService;
	private ReportGenerationTokenService reportGenerationTokenService;

	@Override
	@Deprecated
	public Map<String, String> getReportStatisticsUrlMap(BirtReport report, List<BirtReportSettings> reportSettings) {
		int companyId = report.getCompanyID();
		Date currentDate = new Date();

		Map<String, String> reportUrlMap = new HashMap<>();
		Locale locale = Optional.ofNullable(Locale.forLanguageTag(report.getLanguage()))
				.orElse(Locale.ENGLISH);

		DateFormat dateFormat;
		if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}

		for (BirtReportSettings reportSetting : reportSettings) {
			if (reportSetting.isEnabled()) {
				Map<String, Object> map = new HashMap<>();

				map.put(COMPANY_ID, companyId);
				map.put(REPORT_ID, report.getId());
				map.put(FORMAT, report.getFormat().name().toLowerCase());
                map.put(REPORT_NAME, reportSetting.getReportName(report.getFormat()));
                map.put(LANGUAGE, locale.getLanguage());
                map.put(ACCOUNT_ID, companyId);
                map.put(REPORT_CREATE_DATE, dateFormat.format(currentDate));
                map.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, companyId));

                map.putAll(reportSetting.getReportUrlParameters());

				String reportSettingsName = birtReportFileService.buildLocalizedFileName(reportSetting, companyId, locale, report.getFormat());
				reportUrlMap.put(reportSettingsName, generateUrlWithParams(map, true, companyId));
			}
		}

		return reportUrlMap;
	}

	@Override
	public Map<String, StatisticsReportRequest> getReportRequestsMap(BirtReport report, List<BirtReportSettings> reportSettings) {
		int companyId = report.getCompanyID();
		Locale locale = Optional.ofNullable(Locale.forLanguageTag(report.getLanguage()))
				.orElse(Locale.ENGLISH);

		Map<String, StatisticsReportRequest> reportRequests = new HashMap<>();

		for (BirtReportSettings reportSetting : reportSettings) {
			if (reportSetting.isEnabled()) {
				String reportSettingsName = birtReportFileService.buildLocalizedFileName(reportSetting, companyId, locale, report.getFormat());
				StatisticsReportRequest request = StatisticsReportRequestFactory.from(
						reportSetting,
						report.getFormat(),
						false,
						report.getLanguage(),
						report.getShortname()
				);

				reportRequests.put(reportSettingsName, request);
			}
		}

		return reportRequests;
	}

	@Override
	public File generateReportFile(int reportId, int companyId, StatisticsReportRequest reportRequest) {
		String url = "%s/statistics/reports/%d/generate.action?companyId=%d".formatted(
				configService.getValue(ConfigValue.SystemUrl),
				reportId,
				companyId
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(ReportGenerationHeaders.INTERNAL_TOKEN_HEADER, reportGenerationTokenService.sign(reportId, companyId));

		try (CloseableHttpClient httpClient = FileDownload.createHttpClientForDownload(url)) {
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

			ResponseEntity<byte[]> response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					new HttpEntity<>(reportRequest, headers),
					byte[].class
			);

			byte[] body = response.getBody();
			if (body == null || body.length == 0) {
				return null;
			}

			File file = File.createTempFile(
					String.format(BIRT_REPORT_TEMP_FILE_PATTERN, reportId),
					".tmp",
					AgnUtils.createDirectory(BIRT_REPORT_TEMP_DIR)
			);
			Files.write(file.toPath(), body);
			return file;
		} catch (Exception e) {
			logger.error("Cannot generate report (ID: {}) via POST: {}", reportId, e.getMessage(), e);
			return null;
		}
	}

	private String generateUrlWithParams(Map<String, Object> parameters, boolean internalAccess, int companyID) {
		String birtUrl = getBirtUrl(internalAccess, companyID);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(birtUrl);
        parameters.forEach(uriBuilder::queryParam);
        return uriBuilder.toUriString();
	}

	private String generateUrlWithParams(BirtUrlOptions options, int companyID) {
		return generateUrlWithParams(options.getParameters(), options.isInternalAccess(), companyID);
	}

	protected String getBirtUrl(boolean internalAccess, int companyID) {
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
	public DateTimeRange getDateTimeRestrictions(DateTimeRange selectedDate, DateMode dateMode, LocalDateTime mailingStart, int year, Month month) {
		LocalDateTime startDate = selectedDate.getFrom();
		LocalDateTime endDate = selectedDate.getTo();

		switch (dateMode) {
			case LAST_TENHOURS:
				if (mailingStart != null) {
					return new DateTimeRange(mailingStart,  mailingStart.plusHours(10));
				} else {
					return new DateTimeRange(LocalDateTime.now().minusHours(10), LocalDateTime.now());
				}
			case SELECT_DAY:
				//this mode is hour scale, don't ignore hour values
				if (startDate == null) {
					return new DateTimeRange(LocalDateTime.now().withHour(0), LocalDateTime.now().withHour(23));
				}

				//ignore endDate
				return new DateTimeRange(startDate.withHour(0), startDate.plusDays(1));
			case LAST_MONTH:
				//ignore start and end dates an always set values of first and last day of month
				return new DateTimeRange(LocalDateTime.now().with(firstDayOfMonth()), LocalDateTime.now().with(lastDayOfMonth()));
			case SELECT_MONTH:
				//ignore start and end dates, take into account only year and month selected on GUI dropdown
				LocalDateTime selectedMonth = LocalDateTime.now()
						.withYear(year)
						.withMonth(month.getValue()).truncatedTo(ChronoUnit.DAYS);

				return new DateTimeRange(selectedMonth.with(firstDayOfMonth()), selectedMonth.with(firstDayOfNextMonth()));
			case SELECT_PERIOD:
				if (startDate == null) {
					startDate = LocalDateTime.now().with(firstDayOfMonth());
				}

				if (endDate == null){
					endDate = LocalDateTime.now();
				}
				return new DateTimeRange(startDate, endDate);
			default:
				//do nothing
		}

		return new DateTimeRange(startDate, endDate);
	}

	@Override
	public String getMailingStatisticUrl(Admin admin, String sessionId, MailingStatisticDto mailingStatistic) {
		final String reportName = getReportName(mailingStatistic);
		final Map<String, Object> params = new HashMap<>();

		params.put(REPORT_NAME, reportName);
		params.put(FORMAT, "html");
		params.put(COMPANY_ID, admin.getCompanyID());
		params.put(EMM_SESSION, sessionId);
		params.put(TARGET_BASE_URL.toLowerCase(), generateTargetBaseUrl());

        params.putAll(collectParametersAccordingToDateMode(
                mailingStatistic.getDateMode(),
				mailingStatistic.getDateTimeRange(),
				mailingStatistic.isHourScale()
		));

		collectParametersAccordingToType(mailingStatistic.getType(), admin, mailingStatistic, params);

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setInternalAccess(false)
				.setParameters(params)
				.setAdmin(admin).build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	private Map<String, Object> collectParametersAccordingToDateMode(DateMode dateMode, DateTimeRange dateRange, boolean isHourScale) {
		if (DateMode.NONE.equals(dateMode)) {
			return Collections.emptyMap();
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT_WITH_HOUR2).toPattern());
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(new SimpleDateFormat(DateUtilities.YYYY_MM_DD).toPattern());

		final Tuple<LocalDateTime, LocalDateTime> dateRestrictions =
				computeMailingSummaryDateRestrictions(dateRange.getFrom(), dateRange.getTo(), dateMode);

		String startDateStr;
		String endDateStr;
		if (dateMode == DateMode.LAST_TENHOURS || dateMode == DateMode.SELECT_DAY) {
			startDateStr = dateRestrictions.getFirst().format(dateTimeFormatter);
			endDateStr = dateRestrictions.getSecond().format(dateTimeFormatter);
		} else {
			startDateStr = dateRestrictions.getFirst().toLocalDate().format(dateFormatter);
			endDateStr = dateRestrictions.getSecond().toLocalDate().format(dateFormatter);
		}

		return Map.of(
				START_DATE, startDateStr,
				STOP_DATE, endDateStr,
				HOUR_SCALE, isHourScale
		);
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
			case SUMMARY_AUTO_OPT:
				// TODO: GWUA-7000: Remove after migration
				return "mailing_summary_autoopt.rptdesign";
            case SIMPLE_TRACKING_POINT:
				// TODO: GWUA-7117: Remove after migration
                return "mailing_simple_tracking_point.rptdesign";
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
			params.put(SELECTED_TARGETS, StringUtils.defaultString(StringUtils.join(mailingStatistic.getSelectedTargets(), ",")));
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
	@Deprecated
	public File getReportTmpFile(int birtReportId, String birtUrl, CloseableHttpClient httpClient, Logger loggerForErrors) {
        try {
			File file = File.createTempFile(String.format(BIRT_REPORT_TEMP_FILE_PATTERN, birtReportId), ".tmp", AgnUtils.createDirectory(BIRT_REPORT_TEMP_DIR));
            return FileDownload.downloadAsFile(birtUrl, file, httpClient) ? file : null;
        } catch (Exception e) {
        	loggerForErrors.error("Cannot get birt report (ID: {}) file: {}", birtReportId, e.getMessage());
            return null;
        }
	}

	@Override
	public List<RecipientStatusRow> getRecipientStatusStatistic(int targetId, int mailinglistId, Admin admin) {
		// TODO replace with com.agnitas.emm.core.stats.recipient.RecipientStatsServiceImpl.getRecipientStatus
		return new RecipientStatDataSet().getRecipientStatus(
				admin.getCompanyID(),
				String.valueOf(targetId),
				mailinglistId == 0 ? null : mailinglistId,
				MediaTypes.EMAIL.getMediaCode(),
				admin.getAdminLang(),
				null
		);
	}

	@Override
	public boolean isWorldMailing(Mailing mailing) {
		Set<MaildropEntry> maildropStatus = mailing.getMaildropStatus();

		List<Character> invalidStatuses = Arrays.stream(MaildropStatus.values())
				.filter(status -> !status.equals(MaildropStatus.WORLD))
				.map(MaildropStatus::getCode)
				.toList();

		return maildropStatus.stream()
				.noneMatch(status -> invalidStatuses.contains(status.getStatus()));
	}

	@Override
	public String getWorkflowStatisticUrl(Admin admin, WorkflowStatisticDto workflowStatisticDto) {
		Map<String, Object> map = new HashMap<>();
		map.put(REPORT_NAME, workflowStatisticDto.getReportName());
		map.put(FORMAT, workflowStatisticDto.getFormat());
		map.put(WORKFLOW_ID, workflowStatisticDto.getWorkflowId());
		map.put(COMPANY_ID, workflowStatisticDto.getCompanyId());

		map.put(TRACKING_ALLOWED, AgnUtils.isMailTrackingAvailable(admin));
		map.put(SHOW_SOFT_BOUNCES, admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));

		map.putAll(collectParametersAccordingToDateMode(
				workflowStatisticDto.getDateMode(),
				workflowStatisticDto.getDateRange(),
				workflowStatisticDto.getDateMode() == DateMode.SELECT_DAY
		));

		BirtUrlOptions options = BirtUrlOptions.builder(configService, adminService)
				.setParameters(map)
				.setAdmin(admin)
				.setInternalAccess(false)
				.build();

		return generateUrlWithParams(options, admin.getCompanyID());
	}

	@Override
	@Deprecated
	public String getReportStatisticsUrl(Admin admin, BirtReportStatisticDto settings) {
		int companyId = admin.getCompanyID();
		Map<String, Object> map = new HashMap<>();

		map.put(COMPANY_ID, companyId);
		map.put(REPORT_ID, settings.reportId());
		map.put(FORMAT, settings.reportFormat());
		map.put(REPORT_NAME, settings.reportName());
		map.put(ACCOUNT_ID, companyId);
		map.put(REPORT_CREATE_DATE, admin.getDateFormat().format(new Date()));

		map.putAll(settings.reportUrlParameters());

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
					.setAltgChecker(a -> false)
					.setParameters(map)
					.setInternalAccess(true)
					.build();
		}

		return generateUrlWithParams(options, companyId);
	}

	@Override
	public StatisticsReportRequest getReportRequest(BirtReport report, BirtReportSettings settings, Admin admin) {
		return StatisticsReportRequestFactory.from(
				settings,
				report.getFormat(),
				adminService.isDarkmodeEnabled(admin),
				admin.getLocale().toLanguageTag(),
				report.getShortname()
		);
	}

	@Override
	public File getReportTmpFile(BirtReportDownload birtDownload, int companyId) {
		if (!configService.getBooleanValue(ConfigValue.Development.BirtReportsMigration, companyId)) {
			try {
				String prefix = String.format(BIRT_REPORT_TEMP_FILE_PATTERN, birtDownload.getReportId());
				String birtURL = birtDownload.getBirtFileUrl();
				String clientInternalUrl = getBirtUrl(true, companyId);

				try (CloseableHttpClient httpClient = FileDownload.createHttpClientForDownload(clientInternalUrl)) {
					File file = File.createTempFile(prefix, ".tmp", AgnUtils.createDirectory(BIRT_REPORT_TEMP_DIR));

					if (FileDownload.downloadAsFile(birtURL, file, httpClient, logger)) {
						return file;
					}

					file.delete();
					return null;
				}
			} catch (Exception e) {
				logger.error("Cannot get birt report (ID: {}) file: {}", birtDownload.getReportId(), e.getMessage(), e);
				return null;
			}
		}

		return generateReportFile(birtDownload.getReportId(), companyId, birtDownload.getReportRequest());
	}

	protected String generateTargetBaseUrl() {
		return configService.getValue(ConfigValue.SystemUrl);
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	public void setBirtReportFileService(BirtReportFileService birtReportFileService) {
		this.birtReportFileService = birtReportFileService;
	}

	public void setReportGenerationTokenService(ReportGenerationTokenService reportGenerationTokenService) {
		this.reportGenerationTokenService = reportGenerationTokenService;
	}

	// TODO: GWUA-7067: Remove when removing old code
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
			private final Map<String, Object> defaultParameters = new HashMap<>();
			private final Map<String, Object> parameters = new HashMap<>();
			private Function<Integer, Boolean> altgChecker = null;
            private Function<Set<Integer>, Boolean> altgMatcher = null;

			private final ConfigService configService;
			private final AdminService adminService;

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

			protected BirtUrlOptions build() {
				BirtUrlOptions options = new BirtUrlOptions();

				Objects.requireNonNull(admin);

				if (parameters.get(REPORT_NAME) == null) {
					throw new IllegalArgumentException("Report name can not be empty!");
				}

				defaultParameters.put(SECURITY_TOKEN, BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID()));
				defaultParameters.put(LANGUAGE, StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN"));
				defaultParameters.put(IS_SVG, true);

				if (logger.isDebugEnabled()) {
					logger.debug("Security token for accessing BIRT is: {}", defaultParameters.get(SECURITY_TOKEN));
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
    					if (Boolean.TRUE.equals(altgChecker.apply(altgId))) {
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
