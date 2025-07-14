/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.BOUNCES;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.CLICK_STATISTICS_PER_LINK;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_CLICKS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_DELIVERY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_OPENINGS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY_AUTO_OPT;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.TOP_DOMAINS;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatatisticListForm;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowStatisticsService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.WebStorage;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.beans.MailingSendStatus;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import com.agnitas.emm.core.mailing.enums.MailingAdditionalColumn;
import com.agnitas.web.forms.FormDateTime;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

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

    protected final BirtStatisticsService birtStatisticsService;
    protected final CompanyService companyService;
    protected final BirtReportService birtReportService;
    private final MailingBaseService mailingBaseService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final WebStorage webStorage;
    private final ConversionService conversionService;
    private final UserActivityLogService userActivityLogService;
    private final AdminService adminService;
    private final GridServiceWrapper gridServiceWrapper;
    private final TargetService targetService;
    private final OptimizationService optimizationService;
    private final MaildropService maildropService;
    private final CampaignService campaignService;
    private final WorkflowStatisticsService workflowStatisticsService;

    public MailingBirtStatController(
            MailingBaseService mailingBaseService,
            MailinglistApprovalService mailinglistApprovalService,
            WebStorage webStorage,
            ConversionService conversionService,
            UserActivityLogService userActivityLogService,
            AdminService adminService,
            GridServiceWrapper gridServiceWrapper,
            BirtStatisticsService birtStatisticsService,
            CompanyService companyService,
            TargetService targetService,
            BirtReportService birtReportService,
            OptimizationService optimizationService,
            MaildropService maildropService, CampaignService campaignService,
            WorkflowStatisticsService workflowStatisticsService) {
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
        this.maildropService = maildropService;
        this.campaignService = campaignService;
        this.workflowStatisticsService = workflowStatisticsService;
    }

    @RequestMapping("/list.action")
    public String list(Admin admin, MailingStatatisticListForm form, Model model, Popups popups) {
        if (!validate(form, popups)){
            return MESSAGES_VIEW;
        }

        syncFormProps(form, admin);

        // init search properties;
        MailingsListProperties props = getMailingsListProperties(admin, form);

        model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
        model.addAttribute("mailingStatisticList", mailingBaseService.getPaginatedMailingsData(admin, props));
        model.addAttribute("availableAdditionalFields", getAvailableAdditionalFields(admin));
        model.addAttribute("availableMailingLists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
        model.addAttribute("availableArchives", campaignService.getCampaigns(admin.getCompanyID()));
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("availableTargetGroups", targetService.getTargetLights(admin));
        }

        return "stats_mailing_list";
    }

    private static MailingAdditionalColumn[] getAvailableAdditionalFields(Admin admin) {
        if (admin.isRedesignedUiUsed()) {
            return new MailingAdditionalColumn[]{
                    MailingAdditionalColumn.CREATION_DATE,
                    MailingAdditionalColumn.TEMPLATE,
                    MailingAdditionalColumn.SUBJECT,
                    MailingAdditionalColumn.TARGET_GROUPS,
                    MailingAdditionalColumn.MAILING_ID,
                    MailingAdditionalColumn.RECIPIENTS_COUNT
            };
        } else {
            return MailingAdditionalColumn.values();
        }
    }

    @PostMapping("/setSelectedFields.action")
    public @ResponseBody BooleanResponseDto updateSelectedFields(@RequestParam(required = false) List<String> selectedFields, Popups popups) {
        webStorage.access(WebStorage.MAILING_SEPARATE_STATS_OVERVIEW, storage -> storage.setSelectedFields(selectedFields));
        popups.success(CHANGES_SAVED_MSG);

        return new BooleanResponseDto(popups, !popups.hasAlertPopups());
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(@RequestParam(required = false) Integer statWorkflowId, Admin admin, Model model,
                       @PathVariable int mailingId, MailingStatisticForm form, Popups popups) {
        if (!validateDates(admin, form, popups)){
            return MESSAGES_VIEW;
        }
        
        Mailing mailing = mailingBaseService.getMailing(admin.getCompanyID(), mailingId);

        if (mailing == null) {
            return "redirect:/statistics/mailing/list.action";
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
            int optimizationId = optimizationService.getOptimizationIdByFinalMailing(mailingId, admin.getCompanyID());
            if (optimizationId > 0 && !form.isIgnoreAutoOptSummary()) {
                mailingStatisticDto.setType(SUMMARY_AUTO_OPT);
                mailingStatisticDto.setDateMode(DateMode.NONE);
                mailingStatisticDto.setOptimizationId(optimizationId);
                model.addAttribute("hideStatActions", true);
            }
            model.addAttribute("isTotalAutoOpt", optimizationId > 0);
        }

        if (mailingStatisticDto.getType() == TOP_DOMAINS) {
            processMailingInfo(admin, mailingId, mailingStatisticDto, model);
        }

        processStatisticView(admin, model, mailingStatisticDto, form, mailing);

        int workflowId = mailingBaseService.getWorkflowId(mailingId, admin.getCompanyID());
        model.addAttribute("workflowId", workflowId);
        if (admin.isRedesignedUiUsed() && statWorkflowId != null && statWorkflowId > 0) {
            model.addAttribute("workflowStatMailings", workflowStatisticsService.getStatMailings(statWorkflowId, admin));
            model.addAttribute("statWorkflowId", statWorkflowId);
        }

        userActivityLogService.writeUserActivityLog(admin, "view statistics", form.getShortname() + " (" + mailingId + ")" + " active tab - statistics", logger);

        return "stats_mailing_view";
    }

    private MailingStatisticDto convertFormToDto(MailingStatisticForm form, Admin admin, Mailing mailing) {
        Date mailingStartDate = getMinSendDate(mailing);

        final MailingStatisticDto mailingStatisticDto = conversionService.convert(form, MailingStatisticDto.class);
        mailingStatisticDto.setMailingStartDate(mailingStartDate);
        final DateTimeFormatter dateFormatter = admin.getDateFormatter();

        LocalDateTime mailingSendLocalDate = mailingStartDate != null ? DateUtilities.toLocalDateTime(mailingStartDate, admin.getZoneId()) : null;
        DateTimeRange dateRestrictions = getDateTimeRestrictions(form, mailingSendLocalDate, dateFormatter);

        mailingStatisticDto.setDateRange(dateRestrictions);
        form.getStartDate().set(dateRestrictions.getFrom(), dateFormatter);
        form.getEndDate().set(dateRestrictions.getTo(), dateFormatter);

        return mailingStatisticDto;
    }

    private Date getMinSendDate(Mailing mailing) {
        List<Date> sendDates = Stream.of(mailing.getSenddate(), mailingBaseService.getMailingLastSendDate(mailing.getId()))
                .filter(Objects::nonNull)
                .toList();

        if (!sendDates.isEmpty()) {
            return Collections.min(sendDates);
        }

        return null;
    }

    protected MailingsListProperties getMailingsListProperties(final Admin admin, final MailingStatatisticListForm statForm) {
        MailingsListProperties props = new MailingsListProperties();
        props.setSearchQuery(statForm.getSearchQueryText());
        props.setSearchName(statForm.isSearchNameChecked());
        props.setSearchDescription(statForm.isSearchDescriptionChecked());
        props.setSearchNameStr(statForm.getFilterName());
        props.setSearchDescriptionStr(statForm.getFilterDescription());
        props.setRedesignedUiUsed(admin.isRedesignedUiUsed());
        props.setMailingStatisticsOverview(true);
        props.setTypes("0,1,2,3,4"); // all mailing types
        props.setStatuses(Collections.singletonList(MailingStatus.SENT.getDbKey())); // just sent mailings
        props.setSort(StringUtils.defaultString(statForm.getSort(), "senddate")); // sort by send date by default
        props.setDirection(StringUtils.defaultString(statForm.getDir(), "desc")); // desc order by default
        props.setPage(statForm.getPage());
        props.setRownums(statForm.getNumberOfRows());
        props.setAdditionalColumns(SetUtils.union(Set.of("archives"), statForm.getAdditionalFieldsSet()));
        props.setMailingLists(statForm.getFilteredMailingListsAsList());
        props.setArchives(statForm.getFilterArchives());
        if (admin.isRedesignedUiUsed()) {
            props.setSendDateBegin(tryParseDate(statForm.getFilterSendDateBegin(), admin));
            props.setSendDateEnd(tryParseDate(statForm.getFilterSendDateEnd(), admin));
        } else {
            props.setTargetGroups(statForm.getFilteredTargetGroupsAsList());
        }

        return props;
    }

    private Date tryParseDate(String date, Admin admin) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        try {
            return admin.getDateFormat().parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    protected void processStatisticView(Admin admin, Model model, MailingStatisticDto mailingStatisticDto, MailingStatisticForm form, Mailing mailing) {
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        String birtUrl = getBirtUrl(admin, sessionId, mailingStatisticDto);
        String birtDownloadUrl = "";
        if (StringUtils.isNotBlank(birtUrl)) {
            birtDownloadUrl = birtStatisticsService.changeFormat(birtUrl, "csv");
        }
        SimpleDateFormat localeFormat = admin.getDateFormat();

        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("isActiveMailing", maildropService.isActiveMailing(mailing.getId(), admin.getCompanyID()));
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        } else {
            model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingStatisticDto.getMailingId()));
        }
        model.addAttribute("isMailingGrid", form.getTemplateId() > 0);
        model.addAttribute("targetlist", targetService.getTargetLights(admin));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getYearList(getStartYear(mailingStatisticDto.getMailingStartDate())));
        model.addAttribute("localDatePattern", localeFormat.toPattern());
        model.addAttribute("birtUrl", StringUtils.defaultString(birtUrl));
        model.addAttribute("downloadBirtUrl", StringUtils.defaultString(birtDownloadUrl));
    }

    protected String getBirtUrl(Admin admin,  String sessionId, MailingStatisticDto mailingStatisticDto) {
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

    private DateMode getDateMode(StatisticType statisticType, int mailingId, DateMode oldDateMode) {
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

    private void syncFormProps(MailingStatatisticListForm form, Admin admin){
        webStorage.access(WebStorage.MAILING_SEPARATE_STATS_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0) {
                storage.setRowsCount(form.getNumberOfRows());
                if (admin.isRedesignedUiUsed()) {
                    if (!form.isInEditColumnsMode()) {
                        form.setAdditionalFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    }
                } else {
                    storage.setSelectedFields(Arrays.asList(form.getAdditionalFields()));
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());
                form.setAdditionalFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            }
        });
    }

    private boolean validate(MailingStatatisticListForm form, Popups popups) {
        if (form.isSearchNameChecked() || form.isSearchDescriptionChecked()) {
            for (String error : DbUtilities.validateFulltextSearchQueryText(form.getSearchQueryText())) {
                popups.alert(error);
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

    private DateTimeRange getDateTimeRestrictions(MailingStatisticForm form, LocalDateTime mailingStart, DateTimeFormatter dateFormatter) {
        LocalDateTime startDate = form.getStartDate().get(dateFormatter);
        LocalDateTime endDate = form.getEndDate().get(dateFormatter);

        return birtStatisticsService.getDateTimeRestrictions(
                new DateTimeRange(startDate, endDate),
                form.getDateMode(),
                mailingStart,
                form.getYear(),
                form.getMonthValue()
        );
    }

    private void checkAbsentDateFields(MailingStatisticForm form) {
        if (form.getMonth() == -1) {
            form.setMonth(YearMonth.now().getMonth());
        }
        if (form.getYear() == 0) {
            form.setYear(YearMonth.now().getYear());
        }
    }
}
