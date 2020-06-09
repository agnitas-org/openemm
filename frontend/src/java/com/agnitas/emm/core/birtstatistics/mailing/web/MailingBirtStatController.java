/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.core.Permission;
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
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.Popups;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.agnitas.web.MailingAdditionalColumn;
import org.agnitas.web.forms.FormDateTime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;

import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.BOUNCES;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.CLICK_STATISTICS_PER_LINK;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_CLICKS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_DELIVERY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.PROGRESS_OF_OPENINGS;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.SUMMARY_AUTO_OPT;
import static com.agnitas.emm.core.birtstatistics.enums.StatisticType.TOP_DOMAINS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class MailingBirtStatController {

    private static final transient Logger logger = Logger.getLogger(MailingBirtStatController.class);

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
    public String list(ComAdmin admin, MailingStatatisticListForm form, Model model, Popups popups) {
        if(!validate(form, popups)){
            return "messages";
        }

        syncFormProps(form);

        // init search properties;
        MailingsListProperties props = getMailingsListProperties(form);

        model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
        model.addAttribute("mailingStatisticList", mailingBaseService.getPaginatedMailingsData(admin, props));
        model.addAttribute("availableAdditionalFields", MailingAdditionalColumn.values());
        model.addAttribute("availableMailingLists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
        model.addAttribute("availableTargetGroups", targetService.getTargetLights(admin.getCompanyID()));

        return "stats_mailing_list";
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(ComAdmin admin, Model model, @PathVariable int mailingId, MailingStatisticForm form, Popups popups) throws Exception {
        if(!validate(admin, form, popups)){
            return "messages";
        }
        
        Mailing mailing = mailingBaseService.getMailing(admin.getCompanyID(), mailingId);

    
        if (mailing == null) {
            return "/statistics/mailing/list.action";
        }
    
        Date mailingStartDate = mailingBaseService.getMailingLastSendDate(mailingId);
        form.setMailingID(mailingId);
        form.setShortname(mailing.getShortname());
        setDefaultValues(admin, form);
        setDefaultDates(admin, form, mailingStartDate);

        MailingStatisticDto mailingStatisticDto = convertFormToDto(form, admin);

        mailingStatisticDto.setMailingStartDate(mailingStartDate);
        mailingStatisticDto.setDescription(mailing.getDescription());
        form.setTemplateId(gridServiceWrapper.getGridTemplateIdByMailingId(mailing.getId()));
        
        if (form.getStatisticType() == SUMMARY && admin.permissionAllowed(Permission.TEMP_BETA)) {
            int optimizationId = optimizationService.getOptimizationIdByFinalMailing(form.getMailingID(), admin.getCompanyID());
            if (optimizationId > 0) {
                mailingStatisticDto.setType(SUMMARY_AUTO_OPT);
                mailingStatisticDto.setDateMode(DateMode.NONE);
                mailingStatisticDto.setOptimizationId(optimizationId);
                
                model.addAttribute("isTotalAutoOpt", true);
            }
        }
        
        if (mailingStatisticDto.getType() == TOP_DOMAINS) {
            processMailingInfo(admin, form.getMailingID(), mailingStatisticDto, model);
        }

        processStatisticView(admin, model, mailingStatisticDto, form);

        userActivityLogService.writeUserActivityLog(admin, "view statistics", form.getShortname() + " (" + mailingId + ")" + " active tab - statistics", logger);

        return "stats_mailing_view";
    }

    private MailingStatisticDto convertFormToDto(final MailingStatisticForm form, final ComAdmin admin) {
        final MailingStatisticDto mailingStatisticDto = conversionService.convert(form, MailingStatisticDto.class);
        final DateTimeFormatter dateFormatter = admin.getDateFormatter();

        mailingStatisticDto.setStartDate(form.getStartDate().get(dateFormatter));
        mailingStatisticDto.setEndDate(form.getEndDate().get(dateFormatter));

        return mailingStatisticDto;
    }

    private MailingsListProperties getMailingsListProperties(MailingStatatisticListForm statForm) {
        boolean hasTargetGroups = statForm.getAdditionalFieldsSet()
                .contains(MailingAdditionalColumn.TARGET_GROUPS.getSortColumn());

        MailingsListProperties props = new MailingsListProperties();
        props.setSearchQuery(statForm.getSearchQueryText());
        props.setSearchName(statForm.isSearchNameChecked());
        props.setSearchDescription(statForm.isSearchDescriptionChecked());
        props.setTypes("0,1,2,3,4"); // all mailing types
        props.setStatuses(Collections.singletonList("sent")); // just set mailings
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

    protected void processStatisticView(ComAdmin admin, Model model, MailingStatisticDto mailingStatisticDto, MailingStatisticForm form) throws Exception {
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        String birtUrl = getBirtUrl(admin, sessionId, mailingStatisticDto);
        String birtDownloadUrl = birtStatisticsService.changeFormat(birtUrl, "csv");
        SimpleDateFormat localeFormat = admin.getDateFormat();

        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingStatisticDto.getMailingId()));
        model.addAttribute("isMailingGrid", form.getTemplateId() > 0);
        model.addAttribute("targetlist", targetService.getTargetLights(admin.getCompanyID()));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getYearList(getStartYear(mailingStatisticDto.getMailingStartDate())));
        model.addAttribute("localDatePattern", localeFormat.toPattern());
        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingStatisticDto.getMailingId(), admin.getCompanyID()));
        model.addAttribute("birtUrl", birtUrl);
        model.addAttribute("downloadBirtUrl", birtDownloadUrl);
    }

    protected String getBirtUrl(ComAdmin admin,  String sessionId, MailingStatisticDto mailingStatisticDto) throws Exception {
        if(mailingStatisticDto.getType() == null) {
            return null;
        }

        return birtStatisticsService.getMailingStatisticUrl(admin, sessionId, mailingStatisticDto);
    }

    private StatisticType getReportType(StatisticType statisticType, ComAdminPreferences adminPreferences) {
        if (statisticType != null && isAllowedStatistic(statisticType)) {
            return statisticType;
        
        }

        if (adminPreferences.getStatisticLoadType() == ComAdminPreferences.STATISTIC_LOADTYPE_ON_CLICK) {
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
            int mailingType = mailingBaseService.getMailingType(mailingId);
            if ((mailingType != MailingTypes.ACTION_BASED.getCode()) &&
                    (mailingType != MailingTypes.DATE_BASED.getCode()) &&
                    (mailingType != MailingTypes.INTERVAL.getCode())) {
                return DateMode.NONE;
            }
        }
        
        return DateMode.NONE == oldDateMode || DateMode.NONE == dateMode ? dateMode : oldDateMode;
    }

    private void syncFormProps(MailingStatatisticListForm form){
        webStorage.access(ComWebStorage.MAILING_SEPARATE_STATS_OVERVIEW, storage -> {
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

        return !popups.hasFieldPopups();
    }

    private boolean validate(ComAdmin admin, MailingStatisticForm form, Popups popups) {
        return validateDates(admin, form.getStartDate(), form.getEndDate(), popups);
    }

    private boolean validateDates(ComAdmin admin, FormDateTime startDate, FormDateTime endDate, Popups popups) {
        if((startDate == null || StringUtils.isBlank(startDate.getDate())) &&
                (endDate == null || StringUtils.isBlank(endDate.getDate()))){
            return true;
        }

        String pattern = admin.getDateFormat().toPattern();
        if (startDate != null && !AgnUtils.isDateValid(startDate.getDate(), pattern)) {
            popups.alert("error.date.format");
            return false;
        }
        if (endDate != null && !AgnUtils.isDateValid(endDate.getDate(), pattern)) {
            popups.alert("error.date.format");
            return false;
        }
        if (startDate != null && StringUtils.isNotBlank(startDate.getDate())
                && endDate != null && StringUtils.isNotBlank(endDate.getDate())
                &&!AgnUtils.isDatePeriodValid(startDate.getDate(), endDate.getDate(), pattern)) {
            popups.alert("error.period.format");
            return false;
        }
        return true;
    }

    private void processMailingInfo(ComAdmin admin, int mailingId, MailingStatisticDto mailingStatisticDto,
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

    private void setDefaultValues(ComAdmin admin, MailingStatisticForm form){
        ComAdminPreferences adminPreferences = adminService.getAdminPreferences(admin.getAdminID());
        form.setStatisticType(getReportType(form.getStatisticType(), adminPreferences));
        form.setDateMode(getDateMode(form.getStatisticType(), form.getMailingID(), form.getDateMode()));
    }

    private void setDefaultDates(ComAdmin admin, MailingStatisticForm form, Date mailingStartDate) {
        LocalDateTime mailingStart = LocalDateTime.now();
        LocalDateTime currentTime = LocalDateTime.now();

        int currentYear = currentTime.getYear();
        Month currentMonth = currentTime.getMonth();
        if (form.getMonth() == -1) {
            form.setMonth(currentMonth);
        }
        if (form.getYear() == 0) {
            form.setYear(currentYear);
        }

        if (mailingStartDate != null) {
            mailingStart = DateUtilities.toLocalDateTime(mailingStartDate, AgnUtils.getZoneId(admin));
        }
        
        SimpleDateFormat adminDateFormat = admin.getDateFormat();
        DateTimeFormatter dateFormatter = admin.getDateFormatter();
    
        DateMode dateMode = form.getDateMode();
        FormDateTime startDate = form.getStartDate();
        FormDateTime endDate = form.getEndDate();
        switch (dateMode) {
            case LAST_TENHOURS:
                startDate.set(mailingStart, dateFormatter);
                endDate.set(mailingStart.plusHours(10), dateFormatter);
                break;
            case SELECT_DAY:
                FormDateTime selectDay = form.getSelectDay();
                if(StringUtils.isBlank(selectDay.getDate())){
                    selectDay.getFormDate().set(currentTime.toLocalDate(), dateFormatter);
                }
    
                Date selectDate = selectDay.get(adminDateFormat);
                startDate.set(selectDate, adminDateFormat);
                endDate.set(selectDate, adminDateFormat);
                break;
                
            case LAST_MONTH:
                startDate.getFormDate().set(currentTime.toLocalDate().with(firstDayOfMonth()), dateFormatter);
                endDate.getFormDate().set(currentTime.toLocalDate().with(lastDayOfMonth()), dateFormatter);
                break;
            case SELECT_MONTH:
                LocalDate selectedYear = currentTime.toLocalDate().withYear(form.getYear());
                LocalDate selectedMonth = selectedYear.withMonth(form.getMonthValue().getValue());
                startDate.getFormDate().set(selectedMonth.with(firstDayOfMonth()), dateFormatter);
                endDate.getFormDate().set(selectedMonth.with(lastDayOfMonth()), dateFormatter);
                break;
            case SELECT_PERIOD:
                if(StringUtils.isBlank(endDate.getDate())){
                    endDate.getFormDate().set(currentTime.toLocalDate(), dateFormatter);
                }
                if(StringUtils.isBlank(startDate.getDate())){
                    startDate.getFormDate().set(currentTime.toLocalDate().with(firstDayOfMonth()), dateFormatter);
                }
                break;
                default:
                    //do nothing
        }
    }
}
