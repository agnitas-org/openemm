/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import javax.servlet.http.HttpSession;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingBirtStatForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.birtstatistics.mailing.forms.BirtStatForm;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.reporting.birt.service.ComMailingBIRTStatService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.dao.AdminPreferencesDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.web.MailingAdditionalColumn;
import org.agnitas.web.forms.FormDate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing_stat")
@PermissionMapping("mailing_stat_new")
public class MailingBirtStatController {

    private static final transient Logger logger = Logger.getLogger(MailingBirtStatController.class);

    private static final String BIRT_PARAMS_DATE_FORMAT = BIRTDataSet.DATE_PARAMETER_FORMAT;

    private static final List<String> ALLOWED_STATISTIC = Arrays.asList(
            "mailing_summary.rptdesign",
            "mailing_linkclicks.rptdesign",
            "mailing_delivery_progress.rptdesign",
            "mailing_net_and_gross_openings_progress.rptdesign",
            "mailing_linkclicks_progress.rptdesign",
            "top_domains.rptdesign",
            "mailing_bounces.rptdesign"
    );

    private static final String DEFAULT_REPORT_NAME = "mailing_summary.rptdesign";


    private static final Map<String, DateMode> reportWithPeriods;

    static {
        reportWithPeriods = new HashMap<>();
        reportWithPeriods.put("mailing_tracking_point_week_overview.rptdesign", DateMode.LAST_TENHOURS);
        reportWithPeriods.put("mailing_num_tracking_point_week_overview.rptdesign", DateMode.LAST_TENHOURS);
        reportWithPeriods.put("mailing_summary.rptdesign", DateMode.SELECT_MONTH);
    }


    private final ComMailingBaseService mailingBaseService;
    private final ComMailinglistService mailingListService;
    private final ComMailingBIRTStatService birtService;
    private final ComTargetService targetGroupService;
    private final WebStorage webStorage;
    private final ConversionService conversionService;
    private final UserActivityLogService userActivityLogService;
    private final AdminPreferencesDao adminPreferencesDao;
    private final GridServiceWrapper gridServiceWrapper;
    private final BirtStatisticsService birtStatisticsService;
    private final ComMailingDao mailingDao;
    private final ComCompanyDao companyDao;
    private final ComTargetService targetService;

    public MailingBirtStatController(final ComMailingBaseService mailingBaseService, final ComMailinglistService mailingListService,
                                     final ComMailingBIRTStatService birtService, final ComTargetService targetGroupService,
                                     final WebStorage webStorage, final ConversionService conversionService,
                                     final UserActivityLogService userActivityLogService, final AdminPreferencesDao adminPreferencesDao,
                                     final GridServiceWrapper gridServiceWrapper,
                                     final BirtStatisticsService birtStatisticsService, final ComMailingDao mailingDao,
                                     final ComCompanyDao companyDao, final ComTargetService targetService) {
        this.mailingBaseService = mailingBaseService;
        this.mailingListService = mailingListService;
        this.birtService = birtService;
        this.targetGroupService = targetGroupService;
        this.webStorage = webStorage;
        this.conversionService = conversionService;
        this.userActivityLogService = userActivityLogService;
        this.adminPreferencesDao = adminPreferencesDao;
        this.gridServiceWrapper = gridServiceWrapper;
        this.birtStatisticsService = birtStatisticsService;
        this.mailingDao = mailingDao;
        this.companyDao = companyDao;
        this.targetService = targetService;
    }

    @RequestMapping("/list.action")
    public String list(final ComAdmin admin, final MailingBirtStatForm form, final Model model, final Popups popups) {
        if(!validate(form, popups)){
            return "messages";
        }

        syncFormProps(form);

        // init search properties;
        final MailingsListProperties props = getMailingsListProperties(form);

        model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
        model.addAttribute("mailingStatisticList", birtService.getMailingStats(admin.getCompanyID(), props));
        model.addAttribute("availableAdditionalFields", MailingAdditionalColumn.values());
        model.addAttribute("availableMailingLists", mailingListService.getAllMailingListsNames(admin.getCompanyID()));
        model.addAttribute("availableTargetGroups", targetGroupService.getTargetLights(admin.getCompanyID()));

        return "stats_mailing_stats_new";
    }

    @RequestMapping("/view.action")
    public String view(final ComAdmin admin, final BirtStatForm form, final Model model,
                       final HttpSession session, final Popups popups) throws Exception {

        final int companyID = admin.getCompanyID();
        final SimpleDateFormat localeFormat = AgnUtils.getDatePickerFormat(admin, true);
        final Date mailingStartDate = birtService.getMailingStartDate(form.getMailingID());
        final Mailing mailing = mailingDao.getMailing(form.getMailingID(), admin.getCompanyID());

        setDefaultValues(admin, form, mailingStartDate, mailing);

        final MailingStatisticDto mailingStatisticDto = conversionService.convert(form, MailingStatisticDto.class);
        if (mailing != null) {
            mailingStatisticDto.setDescription(mailing.getDescription());
        }
        if (mailingStatisticDto.getType() == StatisticType.TOP_DOMAINS) {
            processMailingInfo(companyID, form.getMailingID(), mailingStatisticDto, model);
        }

        final String birtUrl = birtStatisticsService.getMailingStatisticUrl(admin, session.getId(), mailingStatisticDto);
        final String birtDownloadUrl = birtStatisticsService.changeFormat(birtUrl, "csv");

        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, form.getMailingID()));
        model.addAttribute("isMailingGrid", gridServiceWrapper.getGridTemplateIdByMailingId(form.getMailingID()) > 0);
        model.addAttribute("targetlist", targetService.getTargetLights(companyID));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getYearList(getStartYear(mailingStartDate)));
        model.addAttribute("localDatePattern", localeFormat.toPattern());
        model.addAttribute("workflowId", mailingDao.getWorkflowId(form.getMailingID()));
        model.addAttribute("birtUrl", birtUrl);
        model.addAttribute("downloadBirtUrl", birtDownloadUrl);

        userActivityLogService.writeUserActivityLog(admin, "view statistics", form.getShortname() + " (" + form.getMailingID() + ")" + " active tab - statistics", logger);

        return "stats_birt_mailing_stat_new";
    }

    private MailingsListProperties getMailingsListProperties(final MailingBirtStatForm statForm) {
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

    private boolean isMailingSummaryStatistic(String reportName) {
        return StringUtils.equals("mailing_summary.rptdesign", reportName);
    }

    private String getBirtReportName(String reportNameParam, int statisticLoadType) {
        if (StringUtils.isNotBlank(reportNameParam) && isAllowedStatistic(reportNameParam)) {
            return reportNameParam;
        }

        if (statisticLoadType == ComAdminPreferences.STATISTIC_LOADTYPE_ON_CLICK) {
            return "";
        }

        return DEFAULT_REPORT_NAME;
    }

    private boolean isAllowedStatistic(String reportNameParam) {
        return ALLOWED_STATISTIC.contains(reportNameParam);
    }

    private DateMode getDateMode(String reportName, int mailingId) {
        if (reportWithPeriods.containsKey(reportName)) {
            if (isMailingSummaryStatistic(reportName)) {
                int mailingType = mailingDao.getMailingType(mailingId);
                if ((mailingType != Mailing.TYPE_ACTIONBASED) &&
                        (mailingType != Mailing.TYPE_DATEBASED) &&
                        (mailingType != Mailing.TYPE_INTERVAL)) {
                    return DateMode.NONE;
                }
            }
            return reportWithPeriods.get(reportName);
        } else if (reportName.endsWith("_progress.rptdesign")) {
            return DateMode.LAST_TENHOURS;
        } else {
            return DateMode.NONE;
        }
    }

    private void syncFormProps(final MailingBirtStatForm form){
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

    private boolean validate(final MailingBirtStatForm form, final Popups popups) {
        boolean result = true;
        if (form.isSearchNameChecked() || form.isSearchDescriptionChecked()) {
            for (String error : DbUtilities.validateFulltextSearchQueryText(form.getSearchQueryText())) {
                popups.field("invalid_search_query", error);
                result = false;
            }
        }

        return result;
    }

    private boolean validate(final Locale locale, final BirtStatForm form, final Popups popups) {
        return validateDates(locale, form.getStartDate(), form.getEndDate(), popups);
    }

    private boolean validateDates(final Locale locale, final FormDate startDate, final FormDate endDate, final Popups popups) {
        if((startDate == null || StringUtils.isBlank(startDate.getDate())) && (endDate == null || StringUtils.isBlank(endDate.getDate()))){
            return true;
        }

        String pattern = AgnUtils.getLocaleDateFormatSpecific(locale).toPattern();
        if (startDate != null && !AgnUtils.isDateValid(startDate.getDate(), pattern)) {
            popups.alert("error.date.format");
            return false;
        }
        if (endDate != null && !AgnUtils.isDateValid(endDate.getDate(), pattern)) {
            popups.alert("error.date.format");
            return false;
        }
        if (startDate != null && StringUtils.isNotBlank(startDate.getDate()) && endDate != null && StringUtils.isNotBlank(endDate.getDate())
                &&!AgnUtils.isDatePeriodValid(startDate.getDate(), endDate.getDate(), pattern)) {
            popups.alert("error.period.format");
            return false;
        }
        return true;
    }

    private void processMailingInfo(final int companyID, final int mailingId, final MailingStatisticDto mailingStatisticDto,
                                     final Model model){
        final MailingSendStatus status = mailingDao.getMailingSendStatus(mailingId, companyID);
        final boolean isEverSent = status.getHasMailtracks();
        final boolean isMailtrackingActive = companyDao.isMailtrackingActive(companyID);

        mailingStatisticDto.setMailtrackingActive(isMailtrackingActive);
        model.addAttribute("isMailtrackingActive", isMailtrackingActive);
        model.addAttribute("isEverSent", isEverSent);

        if (isEverSent) {
            if (status.getHasMailtrackData()) {
                mailingStatisticDto.setMailtrackingExpired(true);
                model.addAttribute("mailtrackingExpired", true);
            } else {
                mailingStatisticDto.setMailtrackingExpired(false);
                model.addAttribute("mailtrackingExpired", false);
                model.addAttribute("mailtrackingExpirationDays", status.getExpirationDays());
            }
        }
    }

    private int getStartYear(final Date mailingStartDate){
        if (mailingStartDate != null) {
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(mailingStartDate);
            return calendar.get(Calendar.YEAR);
        } else {
            return new GregorianCalendar().get(Calendar.YEAR); // fallback is current year
        }
    }

    private void setDefaultValues(final ComAdmin admin, final BirtStatForm form, final Date mailingStartDate, final Mailing mailing){

        final ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());
        form.setReportName(getBirtReportName(form.getReportName(), adminPreferences.getStatisticLoadType()));

        final DateMode newDateMode = getDateMode(form.getReportName(), form.getMailingID());
        if (null == form.getDateMode() || DateMode.NONE == form.getDateMode() || DateMode.NONE == newDateMode) {
            form.setDateMode(newDateMode);
        }


        if (mailing != null) {
            form.setShortname(mailing.getShortname());
        }

        setDefaultDates(form, mailingStartDate, admin);
    }

    private void setDefaultDates(final BirtStatForm form, final Date mailingStartDate, final ComAdmin admin) {
        final Calendar mailingCalendar = GregorianCalendar.getInstance();
        final Calendar currentDateCalendar = GregorianCalendar.getInstance();
        final SimpleDateFormat localeFormat = AgnUtils.getDatePickerFormat(admin, true);

        if (mailingStartDate != null) {
            mailingCalendar.setTime(mailingStartDate);
        }

        if (form.getDateMode() == DateMode.LAST_TENHOURS) {
            form.getStartDate().set(mailingCalendar.getTime(), localeFormat);
            mailingCalendar.add(Calendar.HOUR_OF_DAY, 10);
            form.getEndDate().set(mailingCalendar.getTime(), localeFormat);
        } else if (form.getDateMode() == DateMode.SELECT_DAY) {
            if(StringUtils.isBlank(form.getSelectDay().getDate())){
                form.getSelectDay().set(currentDateCalendar.getTime(), localeFormat);
            }
            form.setStartDate(form.getSelectDay());
            form.setEndDate(form.getSelectDay());
        } else if (form.getDateMode() == DateMode.SELECT_MONTH) {
            if (form.getMonth() == null) {
                form.setMonth(currentDateCalendar.get(Calendar.MONTH));
            }
            if (form.getYear() == null) {
                form.setYear(currentDateCalendar.get(Calendar.YEAR));
            }
            mailingCalendar.set(form.getYear(), form.getMonth(), 1);
            form.getStartDate().set(mailingCalendar.getTime(), localeFormat);
            mailingCalendar.set(Calendar.DAY_OF_MONTH, mailingCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            form.getEndDate().set(mailingCalendar.getTime(), localeFormat);
        } else if(form.getDateMode() == DateMode.SELECT_PERIOD ){
            if(StringUtils.isBlank(form.getEndDate().getDate())){
                form.getEndDate().set(currentDateCalendar.getTime(), localeFormat);
            }
            if(StringUtils.isBlank(form.getStartDate().getDate())){
                currentDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
                form.getStartDate().set(currentDateCalendar.getTime(), localeFormat);
            }
        }

    }

}
