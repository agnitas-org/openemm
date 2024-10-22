/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.forms.BirtReportFormSearchParams;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import com.agnitas.emm.core.birtreport.forms.FiltersForm;
import com.agnitas.emm.core.birtreport.forms.validation.BirtReportFormValidator;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MvcUtils;
import org.agnitas.util.UserActivityUtil;
import org.agnitas.web.forms.FormTime;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_FILTER_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.PREDEFINED_ID_KEY;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.DELETE_VIEW;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

@Controller
@RequestMapping("/statistics")
@PermissionMapping("report.statistics")
@SessionAttributes(types = BirtReportFormSearchParams.class)
public class BirtReportController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(BirtReportController.class);
    private static final String REDIRECT_TO_OVERVIEW = "redirect:/statistics/reports.action?restoreSort=true";

    private final ComBirtReportService birtReportService;
    private final WebStorage webStorage;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final ExtendedConversionService conversionService;
    private final UserActivityLogService userActivityLogService;
    private final BirtStatisticsService birtStatisticsService;
    private final ComTargetService targetService;
    private final BirtReportFormValidator formValidator;
    private final AdminService adminService;
    private final ConfigService configService;

    public BirtReportController(ConfigService configService, ComBirtReportService birtReportService,
                                WebStorage webStorage, MailinglistApprovalService mailinglistApprovalService,
                                ExtendedConversionService conversionService, UserActivityLogService userActivityLogService,
                                BirtStatisticsService birtStatisticsService, ComTargetService targetService, AdminService adminService) {
        this.birtReportService = birtReportService;
        this.webStorage = webStorage;
        this.conversionService = conversionService;
        this.userActivityLogService = userActivityLogService;
        this.birtStatisticsService = birtStatisticsService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.targetService = targetService;
        this.configService = configService;

        this.formValidator = new BirtReportFormValidator(configService, birtReportService, conversionService);

        this.adminService = adminService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @ModelAttribute
    public BirtReportFormSearchParams getSearchParams() {
        return new BirtReportFormSearchParams();
    }

    @RequestMapping("/reports.action")
    public String list(Admin admin, @ModelAttribute("birtReportsForm") BirtReportOverviewFilter filter,
                       @ModelAttribute BirtReportFormSearchParams searchParams, @RequestParam(required = false) boolean restoreSort, Model model) {
        FormUtils.syncPaginationData(webStorage, WebStorage.BIRT_REPORT_OVERVIEW, filter, restoreSort);

        if (isRedesign(admin)) {
            FormUtils.syncSearchParams(searchParams, filter, true);
            model.addAttribute("reports", birtReportService.getPaginatedReportList(filter, admin.getCompanyID()));
        } else {
            model.addAttribute("reports",
                    birtReportService.getPaginatedReportList(
                            admin.getCompanyID(),
                            filter.getSort(), filter.getOrder(), filter.getPage(), filter.getNumberOfRows()));
        }
        model.addAttribute("dateFormat", admin.getDateFormat());

        writeUserActivityLog(admin, new UserAction("statistics reports", "active tab - overview"));

        return "birtreport_list";
    }

    private boolean isRedesign(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

    @GetMapping("/reports/search.action")
    public String search(@ModelAttribute BirtReportOverviewFilter filter, RedirectAttributes ra,
                         @ModelAttribute BirtReportFormSearchParams searchParams, Admin admin) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        if (admin.isRedesignedUiUsed()) {
            ra.addFlashAttribute("birtReportsForm", filter);
        }
        return REDIRECT_TO_OVERVIEW;
    }

    @RequestMapping("/report/{id:\\d+}/view.action")
    public String view(Admin admin, @PathVariable int id, Model model) {
        BirtReportForm form = null;
        setUpDateFormats(admin, model);

        if (!model.containsAttribute("birtReportForm")) {
            BirtReportDto birtReport = birtReportService.getBirtReport(admin, id);
            if (birtReport != null) {
                form = conversionService.convert(birtReport, BirtReportForm.class);

                form.getSendDate().set(birtReport.getNextStart(), AgnUtils.getZoneId(admin));
                form.getEndDate().set(birtReport.getEndDate(), isRedesign(admin) ? admin.getDateFormat() : BirtReportSettingsUtils.getReportDateFormatLocalized(admin));

                writeUserActivityLog(admin, new UserAction("view report", getReportUalDescription(birtReport.getShortname(), birtReport.getId())));
            }

            model.addAttribute("birtReportForm", form);
        } else {
            form = (BirtReportForm) model.asMap().get("birtReportForm");
        }

        if (form != null) {
            BirtReportSettingsUtils.convertReportDatesIntoClientFormat(admin, form.getSettings());
        }

        setUpViewParameters(admin, model);
        model.addAttribute("hasActiveDelivery", birtReportService.hasActiveDelivery(id));
        if (isRedesign(admin) && form != null) {
            model.addAttribute("activeTabType", ReportSettingsType.getTypeByCode(form.getActiveTab()));
        }

        return "birtreport_view";
    }

    @RequestMapping(value = {"/report/new.action", "/report/0/view.action"})
    public String create(Admin admin, Model model, BirtReportForm form) {
        form.setSettings(BirtReportSettingsUtils.getDefaultSettings());

        setUpDateFormats(admin, model);
        setUpViewParameters(admin, model);
        presetAltgInSettings(admin, form);

        return "birtreport_view";
    }

    @PostMapping("/report/save.action")
    public ModelAndView save(Admin admin, BirtReportForm form, Popups popups) throws Exception {
        if (!formValidator.validateBeforeSave(form, admin, popups)) {
            return new ModelAndView(MESSAGES_VIEW, HttpStatus.BAD_REQUEST);
        }

        saveReport(admin, form, popups);
        return new ModelAndView(redirectToView(form.getReportId()));
    }

    @PostMapping("/report/evaluate.action")
    public Object evaluate(Admin admin, BirtReportForm form, RedirectAttributes redirectModel, Model model, Popups popups) throws Exception {
        if (!formValidator.isValidToEvaluate(form, admin, popups)) {
            if (isRedesign(admin)) {
                return MESSAGES_VIEW;
            } else {
                redirectModel.addFlashAttribute("birtReportForm", form);
                return "redirect:/statistics/report/" + form.getReportId() + "/view.action";
            }
        }

        if (isRedesign(admin)) {
            return evaluate(form, admin, popups);
        } else {
            prepareDownloadPage(form, admin, model);
            return "birtreport_download";
        }
    }

    private Object evaluate(BirtReportForm form, Admin admin, Popups popups) throws Exception {
        List<BirtReportDownload> downloads = birtReportService.evaluate(form.getReportId(), form.getActiveTab(), admin);
        if (CollectionUtils.isEmpty(downloads)) {
            return "evaluation_finished";
        }

        File tmpFile = birtStatisticsService.getBirtReportTmpFile(downloads, admin.getCompanyID());
        if (tmpFile == null || !tmpFile.exists() || tmpFile.length() <= 0) {
            return "evaluation_finished";
        }

        String fileName = downloads.size() == 1 ? downloads.get(0).getFileName() : "birt_reports.zip";

        String downloadUrl = String.format("/statistics/report/download.action?fileName=%s&tmpFileName=%s",
                fileName, Objects.requireNonNull(tmpFile).getAbsolutePath());
        return ResponseEntity.ok(new DataResponseDto<>(downloadUrl, popups));
    }

    // TODO: EMMGUI-714: remove when old design will be removed
    private void prepareDownloadPage(BirtReportForm form, Admin admin, Model model) throws Exception {
        model.addAttribute("reportId", form.getReportId());
        model.addAttribute("success", evaluate(form, admin, model));
    }

    // TODO: EMMGUI-714: remove when old design will be removed
    private boolean evaluate(BirtReportForm form, Admin admin, Model model) throws Exception {
        BirtReportDownload birtDownload = birtReportService.evaluate(admin, form);
        if (birtDownload == null) {
            return false;
        }
        File tmpFile = birtStatisticsService.getBirtReportTmpFile(birtDownload, admin.getCompanyID());
        if (tmpFile == null || !tmpFile.exists() || tmpFile.length() <= 0) {
            return false;
        }
        birtDownload.setTmpFileName(Objects.requireNonNull(tmpFile).getAbsolutePath());
        model.addAttribute("fileName", birtDownload.getFileName());
        model.addAttribute("tmpFileName", birtDownload.getTmpFileName());
        model.addAttribute("reportShortname", birtDownload.getShortname());
        if (isRedesign(admin)) {
            model.addAttribute("downloadUrl", "/statistics/report/download.action");
        }

        return true;
    }

    @GetMapping(value = "/report/download.action", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody FileSystemResource download(BirtReportDownload form, HttpServletResponse response) {
        File file = new File(form.getTmpFileName());
        HttpUtils.setDownloadFilenameHeader(response, form.getFileName());
        return new FileSystemResource(file);
    }

    @GetMapping(value = "/report/deleteRedesigned.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model) {
        validateSelectedIds(bulkIds);

        List<String> names = birtReportService.getNames(bulkIds, admin.getCompanyID());
        if (names.isEmpty()) {
            throw new RequestErrorException(ERROR_MSG);
        }

        MvcUtils.addDeleteAttrs(model, names,
                "statistic.reports.delete", "statistic.reports.delete.question",
                "bulkAction.delete.report", "bulkAction.delete.report.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/report/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);
        ServiceResult<UserAction> result = birtReportService.markDeleted(bulkIds, admin.getCompanyID());

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return REDIRECT_TO_OVERVIEW;
    }

    @PostMapping("/report/restore.action")
    public String restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);
        birtReportService.restore(bulkIds, admin.getCompanyID());
        popups.success(CHANGES_SAVED_MSG);
        return REDIRECT_TO_OVERVIEW + "&showDeleted=true";
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    @GetMapping("/report/{id:\\d+}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String confirmDelete(Admin admin, @PathVariable int id, Model model, Popups popups) {
        if (!birtReportService.isReportExist(admin.getCompanyID(), id)) {
            popups.alert("recipient.reports.notAvailable");
            return MESSAGES_VIEW;
        }

        SimpleActionForm form = new SimpleActionForm();
        form.setId(id);
        form.setShortname(birtReportService.getReportName(admin.getCompanyID(), id));
        model.addAttribute("birtReportDeleteForm", form);
        return "birtreport_delete";
    }

    @RequestMapping(value = "/report/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    // TODO: EMMGUI-714: remove when old design will be removed
    public String delete(Admin admin, @ModelAttribute("birtReportDeleteForm") SimpleActionForm form, Popups popups) {
        int companyId = admin.getCompanyID();
        int reportId = form.getId();

        if (birtReportService.isReportExist(companyId, reportId)) {
            if (birtReportService.deleteReport(companyId, reportId)) {
                writeUserActivityLog(admin, new UserAction("delete report", getReportUalDescription(form.getShortname(), reportId)));
                popups.success(SELECTION_DELETED_MSG);
            } else {
                popups.alert(ERROR_MSG);
            }
        } else {
            popups.alert("recipient.reports.notAvailable");
        }

        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping(value = "/report/getFilteredMailing.action", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody JSONArray getFilteredMailing(Admin admin, FiltersForm form) throws Exception {
        JSONArray mailinglist = new JSONArray();

        List<MailingBase> predefinedMailingsForReports =
                birtReportService.getFilteredMailings(admin, form.getType(), form.getValue(), MailingType.fromCode(form.getMailingType()));

        for (MailingBase mailingBase : predefinedMailingsForReports) {
            JSONObject m = new JSONObject();
            m.put("id", mailingBase.getId());
            m.put("shortname", mailingBase.getShortname());
            mailinglist.add(m);
        }

        return mailinglist;
    }
    
    @PostMapping("/report/{reportId:\\d+}/deactivateAllDeliveries.action")
    public String deactivateAllDeliveries(@PathVariable int reportId, Admin admin, Popups popups) {
        birtReportService.deactivateAllDeliveries(reportId);
        popups.success(CHANGES_SAVED_MSG);
        writeUserActivityLog(admin, new UserAction(
                "deactivate deliveries",
                "Deactivated deliveries of all types of statistic for the report (id: " + reportId + ")"));
        return redirectToView(reportId);
    }

    @RequestMapping("/singleMailingStatistics/create.action")
    public Object createSingleMailingStatisticsReport(@RequestParam(name = "mailingId") int mailingId, Admin admin, Model model, Popups popups) throws Exception {
        BirtReportDto newReport = birtReportService.createSingleMailingStatisticsReport(mailingId, admin);
        BirtReportForm form = conversionService.convert(newReport, BirtReportForm.class);

        if (isRedesign(admin)) {
            Object evaluationResult = evaluate(form, admin, popups);
            birtReportService.deleteReport(admin.getCompanyID(), newReport.getId());
            return evaluationResult;
        } else {
            prepareDownloadPage(form, admin, model);
            model.addAttribute("backUrl", "/statistics/mailing/" + mailingId + "/view.action");
            birtReportService.deleteReport(admin.getCompanyID(), newReport.getId());

            return "birtreport_download";
        }
    }

    private String redirectToView(@PathVariable int reportId) {
        return String.format("redirect:/statistics/report/%d/view.action", reportId);
    }

    private void setUpDateFormats(Admin admin, Model model) {
        model.addAttribute("datePickerFormatPattern", admin.getDateFormat().toPattern());
        model.addAttribute("reportDateFormatPattern", BirtReportSettingsUtils.getReportDateFormatLocalized(admin).toPattern());
    }

    private void saveReport(Admin admin, BirtReportForm form, Popups popups) throws Exception {
        boolean isNew = form.getReportId() == 0;

        BirtReportDto existedReport = null;

        if (form.getReportId() > 0) {
            existedReport = birtReportService.getBirtReport(admin, form.getReportId());
        }

        if (form.getSendDate().get() != null) {
            // Apply admin's timezone to defined report execution time
            ZonedDateTime adminZonedSendTime = ZonedDateTime.of(LocalDateTime.of(LocalDate.now(), form.getSendDate().get()), ZoneId.of(admin.getAdminTimezone()));
            ZonedDateTime dbZonedSendTime = adminZonedSendTime.withZoneSameInstant(ZoneId.of("Europe/Berlin"));
            LocalTime storageTime = dbZonedSendTime.toLocalDateTime().toLocalTime();
            form.setSendDate(new FormTime().set(storageTime));
        }

        BirtReportDto birtReport = conversionService.convert(form, BirtReportDto.class);

        SimpleDateFormat reportDateFormat = BirtReportSettingsUtils.getReportDateFormatLocalized(admin);
        birtReport.setEndDate(form.getEndDate().get(reportDateFormat));

        int newId = birtReportService.saveBirtReport(admin, birtReport);

        form.setReportId(newId);

        if (isNew) {
            writeUserActivityLog(admin, new UserAction("create report", getReportUalDescription(birtReport.getShortname(), birtReport.getId())));
        } else {
            writeReportChangesLog(admin, existedReport, birtReport);
        }

        if (!birtReportService.isReportEnabled(admin, birtReport)) {
            popups.warning("warning.report.notActivated");
        }

        popups.success(CHANGES_SAVED_MSG);
    }

    private void setUpViewParameters(Admin admin, Model model) {
        int companyId = admin.getCompanyID();

        final List<TargetLight> targets = targetService.getTargetLights(admin);

        model.addAttribute("targetList", targets);

        model.addAttribute("propertiesMap", loadPropertiesMap());
        model.addAttribute("isMailTrackingEnabled", AgnUtils.isMailTrackingAvailable(admin));

        List<Mailinglist> mailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
        model.addAttribute("mailinglistList", mailinglists);

        List<Campaign> campaigns = birtReportService.getCampaignList(companyId);
        model.addAttribute("campaignList", campaigns);

        // data as JSON for settings filter
        loadSettingsFiltersData(mailinglists, campaigns, targets, model);

        loadMailingSettingsData(admin, model);
    }

    private void loadMailingSettingsData(Admin admin, Model model) {
        model.addAttribute("actionBasedMailings",
                birtReportService.getFilteredMailings(admin, 0, 0, MailingType.ACTION_BASED));

        model.addAttribute("dateBasedMailings",
                birtReportService.getFilteredMailings(admin, 0, 0, MailingType.DATE_BASED));

        model.addAttribute("intervalBasedMailings",
                birtReportService.getFilteredMailings(admin, 0, 0, MailingType.INTERVAL));

        model.addAttribute("followupMailings",
                birtReportService.getFilteredMailings(admin, 0, 0, MailingType.FOLLOW_UP));
    }

    private Map<ReportSettingsType, Map<String, List<BirtReportSettingsUtils.Properties>>> loadPropertiesMap() {
        Map<ReportSettingsType, Map<String, List<BirtReportSettingsUtils.Properties>>> settingType = new EnumMap<>(ReportSettingsType.class);

        Map<String, List<BirtReportSettingsUtils.Properties>> comparisonMap = new LinkedHashMap<>();
        comparisonMap.put(BirtReportSettingsUtils.GENERAL_GROUP, BirtReportSettingsUtils.COMPARISON_GENERAL_GROUP);
        comparisonMap.put(BirtReportSettingsUtils.OPENER_GROUP, BirtReportSettingsUtils.COMPARISON_OPENER_GROUP);
        comparisonMap.put(BirtReportSettingsUtils.DEVICES_GROUP, BirtReportSettingsUtils.COMPARISON_DEVICES_GROUP);
        comparisonMap.put(BirtReportSettingsUtils.FORMATS_GROUP, BirtReportSettingsUtils.COMPARISON_FORMATS_GROUP);
        settingType.put(ReportSettingsType.COMPARISON, comparisonMap);

        Map<String, List<BirtReportSettingsUtils.Properties>> mailingMap = new LinkedHashMap<>();
        mailingMap.put(BirtReportSettingsUtils.FORMATS_GROUP, BirtReportSettingsUtils.MAILING_FORMATS_GROUP);
        mailingMap.put(BirtReportSettingsUtils.SENDING_OPENER_GROUP, BirtReportSettingsUtils.MAILING_OPENER_GROUP);
        mailingMap.put(BirtReportSettingsUtils.GENERAL_GROUP, BirtReportSettingsUtils.MAILING_GENERAL_GROUP);
        mailingMap.put(BirtReportSettingsUtils.DEVICES_GROUP, BirtReportSettingsUtils.MAILING_DEVICES_GROUP);
        settingType.put(ReportSettingsType.MAILING, mailingMap);

        Map<String, List<BirtReportSettingsUtils.Properties>> recipientMap = new LinkedHashMap<>();
        recipientMap.put(BirtReportSettingsUtils.WITHOUT_GROUP, BirtReportSettingsUtils.RECIPIENT_WITHOUT_GROUP);
        recipientMap.put(BirtReportSettingsUtils.Properties.ACTIVITY_ANALYSIS.getPropName(), BirtReportSettingsUtils.RECIPIENT_ANALYSIS_GROUP);
        settingType.put(ReportSettingsType.RECIPIENT, recipientMap);

        Map<String, List<BirtReportSettingsUtils.Properties>> topDomainMap = new LinkedHashMap<>();
        topDomainMap.put(BirtReportSettingsUtils.WITHOUT_GROUP, BirtReportSettingsUtils.TOP_DOMAIN_WITHOUT_GROUP);
        settingType.put(ReportSettingsType.TOP_DOMAIN, topDomainMap);

        return settingType;
    }

    private void loadSettingsFiltersData(List<Mailinglist> mailinglists, List<Campaign> campaigns,
                                         List<TargetLight> targets, Model model) {
        JSONObject filterLists = new JSONObject();

        JSONArray archiveFilter = new JSONArray();
        for (Campaign campaign : campaigns) {
            JSONObject ca = new JSONObject();
            ca.put("id", campaign.getId());
            ca.put("shortname", campaign.getShortname());
            archiveFilter.add(ca);
        }
        filterLists.put(FilterType.FILTER_ARCHIVE.name(), archiveFilter);

        JSONArray mailinglistFilter = new JSONArray();
        for (Mailinglist mailinglist : mailinglists) {
            JSONObject ma = new JSONObject();
            ma.put("id", mailinglist.getId());
            ma.put("shortname", mailinglist.getShortname());
            ma.put("changeDate", mailinglist.getChangeDate());
            mailinglistFilter.add(ma);
        }
        filterLists.put(FilterType.FILTER_MAILINGLIST.name(), mailinglistFilter);

        final JSONArray targetsFilter = new JSONArray();
        for (TargetLight target : targets) {
            final JSONObject targetObj = new JSONObject();
            targetObj.put("id", target.getId());
            targetObj.put("shortname", target.getTargetName());
            targetsFilter.add(targetObj);
        }
        filterLists.put(FilterType.FILTER_TARGET.name(), targetsFilter);

        model.addAttribute("settingsFilters", filterLists);
    }

    private void writeReportChangesLog(Admin admin, BirtReportDto existedBirtReport, BirtReportDto birtReport) {
        try {
            UserAction userAction = getReportChangesLog(admin, existedBirtReport, birtReport);

            writeUserActivityLog(admin, userAction);

            if (logger.isInfoEnabled()) {
                logger.info("saveReport: update report {}", existedBirtReport.getId());
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log Template creation error {}", e.getMessage(), e);
            }
        }
    }

    private String getReportTypeName(int type) {
        BirtReportType typeByCode = BirtReportType.getTypeByCode(type);
        if (typeByCode == null) {
            return null;
        }

        switch (typeByCode) {
            case TYPE_DAILY:
                return "daily";
            case TYPE_WEEKLY:
                return "weekly";
            case TYPE_BIWEEKLY:
                return "fortnightly";
            case TYPE_MONTHLY_FIRST:
                return "first day of the month";
            case TYPE_MONTHLY_15TH:
                return "15th day of the month";
            case TYPE_MONTHLY_LAST:
                return "last day of the month";
            case TYPE_AFTER_MAILING_24HOURS:
                return "24 hours after mailing dispatch";
            case TYPE_AFTER_MAILING_48HOURS:
                return "48 hours after mailing dispatch";
            case TYPE_AFTER_MAILING_WEEK:
                return "one week after mailing dispatch";
            default:
                return null;
        }
    }

    private UserAction getReportChangesLog(Admin admin, BirtReportDto existedBirtReport, BirtReportDto newBirtReport) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        if (!StringUtils.equals(existedBirtReport.getShortname(), newBirtReport.getShortname())) {
            changes.add(String.format("Renamed to %s.", newBirtReport.getShortname()));
        }

        if (!StringUtils.equals(existedBirtReport.getDescription(), newBirtReport.getDescription())) {
            changes.add(String.format("Description changed from %s to %s.",
                    existedBirtReport.getDescription(), newBirtReport.getShortname()));
        }

        if (!existedBirtReport.getEmailRecipientList().equals(newBirtReport.getEmailRecipientList())) {
            changes.add(String.format("Send e-mail changed from %s to %s.",
                    StringUtils.join(existedBirtReport.getEmailRecipientList(), ","), StringUtils.join(newBirtReport.getEmailRecipientList(), ",")));
        }

        if (!StringUtils.equals(existedBirtReport.getEmailSubject(), newBirtReport.getEmailSubject())) {
            changes.add(String.format("E-mail subject changed from %s to %s.",
                    existedBirtReport.getEmailSubject(), newBirtReport.getEmailSubject()));
        }

        if (!StringUtils.equals(existedBirtReport.getEmailDescription(), newBirtReport.getEmailDescription())) {
            changes.add(String.format("E-mail description changed from %s to %s.",
                    existedBirtReport.getEmailDescription(), newBirtReport.getEmailDescription()));
        }

        if (existedBirtReport.getType() != newBirtReport.getType()) {
            changes.add(String.format("Timeout after mailing dispatch changed from %s to %s.",
                    getReportTypeName(existedBirtReport.getType()),
                    getReportTypeName(newBirtReport.getType())));
        }

        if (existedBirtReport.getFormat() != newBirtReport.getFormat()) {
            changes.add(String.format("Data type changed from %d to %d.", existedBirtReport.getFormat(), newBirtReport.getFormat()));
        }

        if (DateUtilities.compare(existedBirtReport.getEndDate(), newBirtReport.getEndDate()) != 0) {
            SimpleDateFormat datePickerFormat = admin.getDateFormat();
            String oldStringValue = Optional.ofNullable(existedBirtReport.getEndDate()).map(datePickerFormat::format).orElse(null);
            String newStringValue = Optional.ofNullable(newBirtReport.getEndDate()).map(datePickerFormat::format).orElse(null);
            changes.add(String.format("End date changed from %s to %s.", oldStringValue, newStringValue));
        }

        if (existedBirtReport.getActiveTab() != newBirtReport.getActiveTab()) {
            changes.add(String.format("Report type changed from %d to %d.", existedBirtReport.getActiveTab(), newBirtReport.getActiveTab()));
        }

        Map<ReportSettingsType, Map<String, Object>> existedSettings = existedBirtReport.getSettings();
        Map<ReportSettingsType, Map<String, Object>> newSettings = newBirtReport.getSettings();

        boolean isEnabled = BirtReportSettingsUtils.getBooleanProperty(existedSettings.get(ReportSettingsType.COMPARISON), ENABLED_KEY);
        boolean isNewEnabled = BirtReportSettingsUtils.getBooleanProperty(newSettings.get(ReportSettingsType.COMPARISON), ENABLED_KEY);
        if (isEnabled != isNewEnabled) {
            changes.add(getStatisticActivenessChangelog("Mailing comparison", isNewEnabled));
        }

        isEnabled = BirtReportSettingsUtils.getBooleanProperty(existedSettings.get(ReportSettingsType.MAILING), ENABLED_KEY);
        isNewEnabled = BirtReportSettingsUtils.getBooleanProperty(newSettings.get(ReportSettingsType.MAILING), ENABLED_KEY);
        if (isEnabled != isNewEnabled) {
            changes.add(getStatisticActivenessChangelog("Mailing", isNewEnabled));
        }

        isEnabled = BirtReportSettingsUtils.getBooleanProperty(existedSettings.get(ReportSettingsType.RECIPIENT), ENABLED_KEY);
        isNewEnabled = BirtReportSettingsUtils.getBooleanProperty(newSettings.get(ReportSettingsType.RECIPIENT), ENABLED_KEY);
        if (isEnabled != isNewEnabled) {
            changes.add(getStatisticActivenessChangelog("Recipient", isNewEnabled));
        }

        if (AgnUtils.isMailTrackingAvailable(admin)) {
            isEnabled = BirtReportSettingsUtils.getBooleanProperty(existedSettings.get(ReportSettingsType.TOP_DOMAIN), ENABLED_KEY);
            isNewEnabled = BirtReportSettingsUtils.getBooleanProperty(newSettings.get(ReportSettingsType.TOP_DOMAIN), ENABLED_KEY);
            if (isEnabled != isNewEnabled) {
                changes.add(getStatisticActivenessChangelog("Top domain", isNewEnabled));
            }
        }

        if (!changes.isEmpty()) {
            userAction = new UserAction("edit birt report", String.format("%s (%d)%n%s",
                    existedBirtReport.getShortname(), existedBirtReport.getId(),
                    StringUtils.join(changes, "\n")));
        }
        return userAction;
    }

    private String getReportUalDescription(String name, int id) {
        return String.format("%s (%d)", name, id);
    }

    private String getStatisticActivenessChangelog(String type, boolean isNewEnabled) {
        return String.format("%s statistics %s.", type, isNewEnabled ? "activated" : "deactivated");
    }

    private void writeUserActivityLog(Admin admin, UserAction ua) {
        UserActivityUtil.log(userActivityLogService, admin, ua, logger);
    }

    private void presetAltgInSettings(final Admin admin, final BirtReportForm form) {
        if (configService.isExtendedAltgEnabled(admin.getCompanyID())) {
            Set<Integer> altgIds = admin.getAltgIds();
            if (CollectionUtils.isNotEmpty(altgIds)) {
                final Map<String, Object> comparisonSettings = form.getSettingsByType(ReportSettingsType.COMPARISON);
                comparisonSettings.put(MAILING_FILTER_KEY, FilterType.FILTER_TARGET.getKey());
                comparisonSettings.put(PREDEFINED_ID_KEY, altgIds.iterator().next());

                final Map<String, Object> mailingSettings = form.getSettingsByType(ReportSettingsType.MAILING);
                mailingSettings.put(MAILING_FILTER_KEY, FilterType.FILTER_TARGET.getKey());
                mailingSettings.put(PREDEFINED_ID_KEY, altgIds.iterator().next());
            }
        } else {
            final int altg = adminService.getAccessLimitTargetId(admin);
            if (altg > 0) {
                final Map<String, Object> comparisonSettings = form.getSettingsByType(ReportSettingsType.COMPARISON);
                comparisonSettings.put(MAILING_FILTER_KEY, FilterType.FILTER_TARGET.getKey());
                comparisonSettings.put(PREDEFINED_ID_KEY, altg);
    
                final Map<String, Object> mailingSettings = form.getSettingsByType(ReportSettingsType.MAILING);
                mailingSettings.put(MAILING_FILTER_KEY, FilterType.FILTER_TARGET.getKey());
                mailingSettings.put(PREDEFINED_ID_KEY, altg);
            }
        }
    }
}
