/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatatisticListForm;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.Tuple;
import org.agnitas.web.MailingAdditionalColumn;
import org.agnitas.web.forms.FormDateTime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.BOUNCES;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.CLICK_STATISTICS_PER_LINK;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_CLICKS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_DELIVERY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_OPENINGS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY_AUTO_OPT;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.TOP_DOMAINS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class MailingBirtStatController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(MailingBirtStatController.class);

    private static final List<StatisticType> ALLOWED_STATISTIC = Arrays.asList(
            SUMMARY,
            SUMMARY_AUTO_OPT,
            CLICK_STATISTICS_PER_LINK,
            PROGRESS_OF_DELIVERY,
            PROGRESS_OF_OPENINGS,
            PROGRESS_OF_CLICKS,
            TOP_DOMAINS,
            BOUNCES
    );

    protected ComMailingBaseService mailingBaseService;
    protected MailinglistApprovalService mailinglistApprovalService;
    protected WebStorage webStorage;
    protected ConversionService conversionService;
    protected UserActivityLogService userActivityLogService;
    protected AdminService adminService;
    protected GridServiceWrapper gridServiceWrapper;
    protected BirtStatisticsService birtStatisticsService;
    protected CompanyService companyService;
    protected ComTargetService targetService;
    protected ComBirtReportService birtReportService;
    protected ComOptimizationService optimizationService;

    public MailingBirtStatController(
            ComMailingBaseService mailingBaseService,
    		MailinglistApprovalService mailinglistApprovalService,
			WebStorage webStorage,
			ConversionService conversionService,
			UserActivityLogService userActivityLogService,
			AdminService adminService,
			GridServiceWrapper gridServiceWrapper,
			BirtStatisticsService birtStatisticsService,
			CompanyService companyService,
			ComTargetService targetService,
			ComBirtReportService birtReportService,
            ComOptimizationService optimizationService) {
        this.mailingBaseService = mailingBaseService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.webStorage = webStorage;
        this.conversionService = conversionService;
        this.userActivityLogService = userActivityLogService;
        this.adminService = adminService;
        this.gridServiceWrapper = gridServiceWrapper;
        this.birtStatisticsService = birtStatisticsService;
        this.companyService = companyService;
        this.targetService = targetService;
        this.birtReportService = birtReportService;
        this.optimizationService = optimizationService;
    }

    @RequestMapping("/list.action")
    public String list(Admin admin, MailingStatatisticListForm form, Model model, Popups popups) {
        if(!validate(form, popups)){
            return "messages";
        }

        syncFormProps(form);

        // init search properties;
        MailingsListProperties props = getMailingsListProperties(admin, form);

        model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
        model.addAttribute("mailingStatisticList", mailingBaseService.getPaginatedMailingsData(admin, props));
        model.addAttribute("availableAdditionalFields", MailingAdditionalColumn.values());
        model.addAttribute("availableMailingLists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
        model.addAttribute("availableTargetGroups", targetService.getTargetLights(admin));

        return "stats_mailing_list";
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(Admin admin, Model model, @PathVariable int mailingId, MailingStatisticForm form, Popups popups) throws Exception {
        if(!validateDates(admin, form, popups)){
            return "messages";
        }
        
        Mailing mailing = mailingBaseService.getMailing(admin.getCompanyID(), mailingId);
    
        if (mailing == null) {
            return "/statistics/mailing/list.action";
        }

        boolean show10HoursTab = mailing.getMailingType().equals(MailingType.NORMAL) && birtStatisticsService.isWorldMailing(mailing);

        form.setMailingID(mailingId);
        form.setShortname(mailing.getShortname());
        form.setDescription(mailing.getDescription());
        form.setTemplateId(gridServiceWrapper.getGridTemplateIdByMailingId(mailing.getId()));
        form.setShow10HoursTab(show10HoursTab);

        AdminPreferences adminPreferences = adminService.getAdminPreferences(admin.getAdminID());
        form.setStatisticType(getReportType(form.getStatisticType(), adminPreferences));
        form.setDateMode(getDateMode(form.getStatisticType(), mailingId, form.getDateMode()));

        if (form.getDateMode().equals(DateMode.LAST_TENHOURS) && !form.isShow10HoursTab()) {
            form.setDateMode(DateMode.SELECT_DAY);
        }

        checkAbsentDateFields(form);

        MailingStatisticDto mailingStatisticDto = convertFormToDto(form, admin, mailing);

        if (form.getStatisticType() == SUMMARY) {
            prepareForSummaryStatistic(mailingStatisticDto, admin.getCompanyID(), mailingId, model);
        }

        if (mailingStatisticDto.getType() == TOP_DOMAINS) {
            processMailingInfo(admin, mailingId, mailingStatisticDto, model);
        }

        processStatisticView(admin, model, mailingStatisticDto, form, mailing);

        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, admin.getCompanyID()));

        userActivityLogService.writeUserActivityLog(admin, "view statistics", form.getShortname() + " (" + mailingId + ")" + " active tab - statistics", logger);

        return "stats_mailing_view";
    }

    protected MailingStatisticDto convertFormToDto(final MailingStatisticForm form, final Admin admin, Mailing mailing) {
        int mailingId = mailing.getId();

        List<Date> sendDates = Arrays.stream(new Date[]{mailing.getSenddate(), mailingBaseService.getMailingLastSendDate(mailingId)})
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Date mailingStartDate = null;
        if (!sendDates.isEmpty()) {
            mailingStartDate = Collections.min(sendDates);
        }

        final MailingStatisticDto mailingStatisticDto = conversionService.convert(form, MailingStatisticDto.class);
        mailingStatisticDto.setMailingStartDate(mailingStartDate);
        final DateTimeFormatter dateFormatter = admin.getDateFormatter();

        LocalDateTime mailingSendLocalDate = mailingStartDate != null ? DateUtilities.toLocalDateTime(mailingStartDate, AgnUtils.getZoneId(admin)) : null;
        Tuple<LocalDateTime, LocalDateTime> dateRestrictions = dateTimeRestrictions(form, mailingSendLocalDate, dateFormatter);

        mailingStatisticDto.setStartDate(dateRestrictions.getFirst());
        mailingStatisticDto.setEndDate(dateRestrictions.getSecond());
        form.getStartDate().set(dateRestrictions.getFirst(), dateFormatter);
        form.getEndDate().set(dateRestrictions.getSecond(), dateFormatter);

        return mailingStatisticDto;
    }

    protected MailingsListProperties getMailingsListProperties(final Admin admin, final MailingStatatisticListForm statForm) {
        boolean hasTargetGroups = statForm.getAdditionalFieldsSet()
                .contains(MailingAdditionalColumn.TARGET_GROUPS.getSortColumn());

        MailingsListProperties props = new MailingsListProperties();
        props.setSearchQuery(statForm.getSearchQueryText());
        props.setSearchName(statForm.isSearchNameChecked());
        props.setSearchDescription(statForm.isSearchDescriptionChecked());
        props.setTypes("0,1,2,3,4"); // all mailing types
        props.setStatuses(Collections.singletonList(MailingStatus.SENT.getDbKey())); // just set mailings
        props.setSort(StringUtils.defaultString(statForm.getSort(), "senddate")); // sort by send date by default
        props.setDirection(StringUtils.defaultString(statForm.getDir(), "desc")); // desc order by default
        props.setPage(statForm.getPage());
        props.setRownums(statForm.getNumberOfRows());
        props.setIncludeTargetGroups(hasTargetGroups);
        props.setAdditionalColumns(statForm.getAdditionalFieldsSet());
        props.setMailingLists(statForm.getFilteredMailingListsAsList());
        props.setTargetGroups(statForm.getFilteredTargetGroupsAsList());

        return props;
    }

    protected void processStatisticView(Admin admin, Model model, MailingStatisticDto mailingStatisticDto, MailingStatisticForm form, Mailing mailing) throws Exception {
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        String birtUrl = getBirtUrl(admin, sessionId, mailingStatisticDto);
        String birtDownloadUrl = "";
        if (StringUtils.isNotBlank(birtUrl)) {
            birtDownloadUrl = birtStatisticsService.changeFormat(birtUrl, "csv");
        }
        SimpleDateFormat localeFormat = admin.getDateFormat();

        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingStatisticDto.getMailingId()));
        model.addAttribute("isMailingGrid", form.getTemplateId() > 0);
        model.addAttribute("targetlist", targetService.getTargetLights(admin));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getYearList(getStartYear(mailingStatisticDto.getMailingStartDate())));
        model.addAttribute("localDatePattern", localeFormat.toPattern());
        model.addAttribute("birtUrl", StringUtils.defaultString(birtUrl));
        model.addAttribute("downloadBirtUrl", StringUtils.defaultString(birtDownloadUrl));
    }

    protected String getBirtUrl(Admin admin,  String sessionId, MailingStatisticDto mailingStatisticDto) throws Exception {
        if(mailingStatisticDto.getType() == null) {
            return null;
        }

        return birtStatisticsService.getMailingStatisticUrl(admin, sessionId, mailingStatisticDto);
    }

    private StatisticType getReportType(StatisticType statisticType, AdminPreferences adminPreferences) {
        if (statisticType != null && isAllowedStatistic(statisticType)) {
            return statisticType;
        }

        if (adminPreferences.getStatisticLoadType() == AdminPreferences.STATISTIC_LOADTYPE_ON_CLICK) {
            return null;
        }

        return StatisticType.SUMMARY;
    }

    protected boolean isAllowedStatistic(StatisticType statisticType) {
        return ALLOWED_STATISTIC.contains(statisticType);
    }

    private DateMode getDateMode(StatisticType statisticType, int mailingId, DateMode oldDateMode) throws Exception {
        DateMode dateMode = statisticType != null ? statisticType.getDateMode() : DateMode.NONE;
        if (dateMode != DateMode.NONE && SUMMARY == statisticType) {
        	MailingType mailingType = mailingBaseService.getMailingType(mailingId);
            if ((mailingType != MailingType.ACTION_BASED) &&
                    (mailingType != MailingType.DATE_BASED) &&
                    (mailingType != MailingType.INTERVAL)) {
                return DateMode.NONE;
            }
        }
        
        return DateMode.NONE == oldDateMode || DateMode.NONE == dateMode ? dateMode : oldDateMode;
    }

    private void syncFormProps(MailingStatatisticListForm form){
        webStorage.access(WebStorage.MAILING_SEPARATE_STATS_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0) {
                storage.setRowsCount(form.getNumberOfRows());
                storage.setSelectedFields(Arrays.asList(form.getAdditionalFields()));
            } else {
                form.setNumberOfRows(storage.getRowsCount());
                form.setAdditionalFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            }
        });
    }

    private boolean validate(MailingStatatisticListForm form, Popups popups) {
        if (form.isSearchNameChecked() || form.isSearchDescriptionChecked()) {
            for (String error : DbUtilities.validateFulltextSearchQueryText(form.getSearchQueryText())) {
                popups.field("invalid_search_query", error);
            }
        }

        return !popups.hasAlertPopups();
    }

    protected boolean validateDates(Admin admin, MailingStatisticForm form, Popups popups) {
        return validateDates(admin, form.getStartDate(), form.getEndDate(), popups);
    }

    private boolean validateDates(Admin admin, FormDateTime startDate, FormDateTime endDate, Popups popups) {
        String pattern = admin.getDateFormat().toPattern();

        String startDateValue = startDate == null ? null : startDate.getDate();
        if (StringUtils.isNotBlank(startDateValue) && !AgnUtils.isDateValid(startDateValue, pattern)) {
            popups.alert("error.date.format");
            return false;
        }

        String endDateValue = endDate == null ? null : endDate.getDate();
        if (StringUtils.isNotBlank(endDateValue) && !AgnUtils.isDateValid(endDateValue, pattern)) {
            popups.alert("error.date.format");
            return false;
        }

        if (StringUtils.isNotBlank(startDateValue) && StringUtils.isNotBlank(endDateValue) &&
                !isPeriodDateValid(startDateValue, endDateValue, pattern)) {
            popups.alert("error.period.format");
            return false;
        }

        return true;
    }

    private boolean isPeriodDateValid(String startDateValue, String endDateValue, String pattern) {
        return AgnUtils.isDatePeriodValid(startDateValue, endDateValue, pattern) || startDateValue.equals(endDateValue);
    }

    protected void processMailingInfo(Admin admin, int mailingId, MailingStatisticDto mailingStatisticDto,
                                    Model model){
        MailingSendStatus status = mailingBaseService.getMailingSendStatus(mailingId, admin.getCompanyID());
        boolean isEverSent = status.getHasMailtracks();
        boolean isMailtrackingActive = AgnUtils.isMailTrackingAvailable(admin);

        mailingStatisticDto.setMailtrackingActive(isMailtrackingActive);
        model.addAttribute("isMailtrackingActive", isMailtrackingActive);
        model.addAttribute("isEverSent", isEverSent);

        boolean mailTrackingExpired = isEverSent && status.getHasMailtrackData();
        mailingStatisticDto.setMailtrackingExpired(mailTrackingExpired);
        model.addAttribute("mailtrackingExpired", mailTrackingExpired);
    
        if (!mailTrackingExpired) {
            model.addAttribute("mailtrackingExpirationDays", status.getExpirationDays());
        }
    }

    private int getStartYear(Date mailingStartDate) {
        if (mailingStartDate != null) {
            return DateUtilities.getYear(mailingStartDate);
        } else {
            return Year.now().getValue(); // fallback is current year
        }
    }

    private Tuple<LocalDateTime, LocalDateTime> dateTimeRestrictions(MailingStatisticForm form, LocalDateTime mailingStart, DateTimeFormatter dateFormatter) {
        DateMode dateMode = form.getDateMode();

        LocalDateTime startDate = form.getStartDate().get(dateFormatter);
        LocalDateTime endDate = form.getEndDate().get(dateFormatter);
        switch (dateMode) {
            case LAST_TENHOURS:
                if (mailingStart != null) {
                    return new Tuple<>(mailingStart,  mailingStart.plusHours(10));
                } else {
                    return new Tuple<>(LocalDateTime.now().minusHours(10),  LocalDateTime.now());
                }
            case SELECT_DAY:
                //this mode is hour scale, don't ignore hour values
                if (startDate == null) {
                    return new Tuple<>(LocalDateTime.now().withHour(0), LocalDateTime.now().withHour(23));
                } else {
                    //ignore endDate
                    return new Tuple<>(startDate.withHour(0), startDate.plusDays(1));
                }
            case LAST_MONTH:
                //ignore start and end dates an always set values of first and last day of month
                return new Tuple<>(LocalDateTime.now().with(firstDayOfMonth()), LocalDateTime.now().with(lastDayOfMonth()));
            case SELECT_MONTH:
                //ignore start and end dates, take into account only year and month selected on GUI dropdown
                LocalDateTime selectedMonth = LocalDateTime.now()
                        .withYear(form.getYear())
                        .withMonth(form.getMonthValue().getValue()).truncatedTo(ChronoUnit.DAYS);

                return new Tuple<>(selectedMonth.with(firstDayOfMonth()), selectedMonth.with(firstDayOfNextMonth()));
            case SELECT_PERIOD:
                if(startDate == null) {
                    startDate = LocalDateTime.now().with(firstDayOfMonth());
                }

                if(endDate == null){
                    endDate = LocalDateTime.now();
                }
                return new Tuple<>(startDate, endDate);
                default:
                    //do nothing
        }

        return new Tuple<>(startDate, endDate);
    }

    private void checkAbsentDateFields(MailingStatisticForm form) {
        if (form.getMonth() == -1) {
            form.setMonth(YearMonth.now().getMonth());
        }
        if (form.getYear() == 0) {
            form.setYear(YearMonth.now().getYear());
        }
    }

    private void prepareForSummaryStatistic(MailingStatisticDto mailingStatisticDto, int companyId, int mailingId, Model model) {
        int optimizationId = optimizationService.getOptimizationIdByFinalMailing(mailingId, companyId);
        if (optimizationId > 0) {
            mailingStatisticDto.setType(SUMMARY_AUTO_OPT);
            mailingStatisticDto.setDateMode(DateMode.NONE);
            mailingStatisticDto.setOptimizationId(optimizationId);

            model.addAttribute("isTotalAutoOpt", true);
        }
    }
}
