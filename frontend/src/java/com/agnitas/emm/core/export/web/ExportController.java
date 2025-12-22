/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.web;

import static com.agnitas.emm.core.Permission.EXPORT_OWN_COLUMNS;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.UserActivityUtil.addChangedFieldLog;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ExportColumnMapping;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.export.form.ExportForm;
import com.agnitas.emm.core.export.reporter.RecipientExportReporter;
import com.agnitas.emm.core.export.util.ExportUtils;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.exception.BadRequestException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.RecipientExportWorker;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public class ExportController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(ExportController.class);

    public static final String EXPORT_KEY = "EXPORT_RECIPIENTS";
    private static final String SHORTNAME_FIELD = "shortname";

    protected final ExportPredefService exportService;
    private final TargetService targetService;
    private final ColumnInfoService columnInfoService;
    private final MailinglistService mailinglistService;
    private final UserActivityLogService userActivityLogService;
    private final RecipientExportReporter recipientExportReporter;
    private final WebStorage webStorage;

    public ExportController(ColumnInfoService columnInfoService, TargetService targetService,
                            ExportPredefService exportService, MailinglistService mailinglistService,
                            UserActivityLogService userActivityLogService,
                            RecipientExportReporter recipientExportReporter, WebStorage webStorage) {
        this.columnInfoService = columnInfoService;
        this.targetService = targetService;
        this.exportService = exportService;
        this.mailinglistService = mailinglistService;
        this.userActivityLogService = userActivityLogService;
        this.recipientExportReporter = recipientExportReporter;
        this.webStorage = webStorage;
    }

    @RequestMapping("/list.action")
    @RequiredPermission("wizard.export")
    public String list(@RequestParam(required = false) Boolean restoreSort, @ModelAttribute("form") PaginationForm form, Model model, Admin admin) {
        FormUtils.syncPaginationData(webStorage, WebStorage.EXPORT_PROFILE_OVERVIEW, form, restoreSort);
        model.addAttribute("exports", exportService.getExportProfilesOverview(form, admin));
        writeUserActivityLog(admin, "exports list", "Data management -> Export");
        return "export_list";
    }

    @GetMapping("/{id:\\d+}/view.action")
    @RequiredPermission("wizard.export")
    public String view(@PathVariable int id, ExportForm form, Model model, Admin admin,
                       @RequestHeader("referer") String referringUrl, Popups popups) {
        ExportPredef export = null;
        if (id != 0) {
            export = exportService.get(id, admin.getCompanyID());
            if (!allowedToView(admin, export)) {
                popups.permissionDenied();
                return "redirect:" + AgnUtils.removeJsessionIdFromUrl(referringUrl);
            }
            exportToForm(export, form, admin);
            writeUserActivityLog(admin, "view export", export.toString());
        } else {
            prepareFormToCreateNewExport(form, admin);
        }
        prepareViewAttrs(model, admin, export);
        return "export_view";
    }

    protected boolean allowedToView(Admin admin, ExportPredef export) {
        return true; // overridden in extended class
    }

    @PostMapping("/{id:\\d+}/save.action")
    @RequiredPermission("export.change")
    public String save(@PathVariable int id, ExportForm form, Admin admin, Popups popups) {
        if (isInvalidForm(form, admin, popups)) {
            return MESSAGES_VIEW;
        }
        if (id <= 0) {
            id = createNewExport(form, admin, admin.getCompanyID());
        } else {
            updateExport(id, form, admin, admin.getCompanyID());
        }
        popups.changesSaved();
        return redirectToView(id);
    }

    private boolean isOwnColumnsExportAllowed(Admin admin) {
        return admin.permissionAllowed(EXPORT_OWN_COLUMNS);
    }

    private String redirectToView(@PathVariable int id) {
        return "redirect:/export/" + id + "/view.action";
    }

    private void updateExport(int id, ExportForm form, Admin admin, int companyId) {
        ExportPredef export = exportService.get(id, companyId);
        ExportPredef oldExport = exportService.get(id, companyId);
        formToExport(form, export, admin);
        exportService.save(export, admin);

        String changelog = changelog(oldExport, export, admin);
        if (StringUtils.isNotBlank(changelog)) {
            writeUserActivityLog(admin, "edit export", export + ". " + changelog);
        }
    }

    private int createNewExport(ExportForm form, Admin admin, int companyId) {
        ExportPredef export = new ExportPredef();
        export.setCompanyID(companyId);
        formToExport(form, export, admin);
        int newId = exportService.save(export, admin);
        writeUserActivityLog(admin, "create export definition", export.toString());
        return newId;
    }

    protected void formToExport(ExportForm form, ExportPredef export, Admin admin) {
        export.setShortname(form.getShortname());
        export.setDescription(form.getDescription());
        export.setMailinglists(StringUtils.join(ArrayUtils.toObject(form.getMailinglists()), ";"));

        formFileFormatToExport(form, export);
        formRecipientInfoToExport(form, export, admin);
        setColumnsToExport(form, export, admin);
        formDateLimitsToExport(form, export, admin);
    }

    private void formRecipientInfoToExport(ExportForm form, ExportPredef export, Admin admin) {
        formMailinglistToExport(form, export, admin);
        export.setTargetID(form.getTargetId());
        export.setUserType(form.getUserType());
        export.setUserStatus(form.getUserStatus());
    }

    protected void formMailinglistToExport(ExportForm form, ExportPredef export, Admin admin) {
        export.setMailinglistID(form.getMailinglistId());
    }

    private void formFileFormatToExport(ExportForm form, ExportPredef export) {
        String separator = form.getSeparator();
        export.setSeparator("\t".equals(separator) ? "t" : separator);
        export.setDelimiter(form.getDelimiter());
        export.setAlwaysQuote(form.isAlwaysQuote());
        export.setCharset(form.getCharset());
        export.setDateFormat(form.getDateFormat().getIntValue());
        export.setDateTimeFormat(form.getDateTimeFormat().getIntValue());
        export.setLocale(form.getLocale());
        export.setTimezone(form.getTimezone());
        export.setDecimalSeparator(form.getDecimalSeparator());
        export.setUseDecodedValues(form.isUseDecodedValues());
    }

    protected void formDateLimitsToExport(ExportForm form, ExportPredef export, Admin admin) {
        export.setTimestampStart(parseDate(form.getTimestampStart(), admin));
        export.setTimestampEnd(parseDate(form.getTimestampEnd(), admin));
        export.setTimestampLastDays(form.getTimestampLastDays());
        export.setTimestampIncludeCurrentDay(form.isTimestampIncludeCurrentDay());

        export.setCreationDateStart(parseDate(form.getCreationDateStart(), admin));
        export.setCreationDateEnd(parseDate(form.getCreationDateEnd(), admin));
        export.setCreationDateLastDays(form.getCreationDateLastDays());
        export.setCreationDateIncludeCurrentDay(form.isCreationDateIncludeCurrentDay());

        export.setMailinglistBindStart(parseDate(form.getMailinglistBindStart(), admin));
        export.setMailinglistBindEnd(parseDate(form.getMailinglistBindEnd(), admin));
        export.setMailinglistBindLastDays(form.getMailinglistBindLastDays());
        export.setMailinglistBindIncludeCurrentDay(form.isMailinglistBindIncludeCurrentDay());

        export.setTimeLimitsLinkedByAnd(form.isTimeLimitsLinkedByAnd());
    }

    private Date parseDate(String str, Admin admin) {
        try {
            return StringUtils.isNotEmpty(str)
                    ? admin.getDateFormat().parse(str)
                    : null;
        } catch (ParseException e) {
            LOGGER.warn("Unable to parse date '{}' using format {}", str, admin.getDateFormat(), e);
            return null;
        }
    }

    private void setColumnsToExport(ExportForm form, ExportPredef export, Admin admin) {
        List<ExportColumnMapping> columns = Arrays.stream(form.getUserColumns())
                .map(ExportColumnMapping::new)
                .collect(Collectors.toList());
        if (isOwnColumnsExportAllowed(admin)) {
            columns.addAll(form.getCustomColumns());
        } else {
            columns.addAll(getExportCustomColumns(export, admin));
        }
        export.setExportColumnMappings(columns);
    }

    private List<ExportColumnMapping> getExportCustomColumns(ExportPredef export, Admin admin) {
        List<ExportColumnMapping> customCols = ExportUtils.getCustomColumnMappingsFromExport(export, admin, columnInfoService);
        customCols.sort(Comparator.comparing(ExportColumnMapping::getDbColumn));
        return customCols;
    }

    private String changelog(ExportPredef oldExport, ExportPredef newExport, Admin admin) {
        SimpleDateFormat dateFormat = admin.getDateTimeFormatWithSeconds();
        return addChangedFieldLog(SHORTNAME_FIELD, newExport.getShortname(), oldExport.getShortname()) +
                addChangedFieldLog("description", newExport.getDescription(), oldExport.getDescription()) +
                getMailinglistChangeLog(oldExport, newExport, admin) +
                addChangedFieldLog("target group", newExport.getTargetID(), oldExport.getTargetID()) +
                addChangedFieldLog("recipient type", newExport.getUserType(), oldExport.getUserType()) +
                addChangedFieldLog("recipient status", newExport.getUserStatus(), oldExport.getUserStatus()) +
                addChangedFieldLog("columns", getExportColumnsCsv(newExport), getExportColumnsCsv(oldExport)) +
                addChangedFieldLog("status mailing lists", newExport.getMailinglists(), oldExport.getMailinglists()) +
                addChangedFieldLog("separator", newExport.getSeparator(), oldExport.getSeparator()) +
                addChangedFieldLog("delimiter", newExport.getDelimiter(), oldExport.getDelimiter()) +
                addChangedFieldLog("charset", newExport.getCharset(), oldExport.getCharset()) +
                addChangedFieldLog("change period start", newExport.getTimestampStart(), oldExport.getTimestampStart(), dateFormat) +
                addChangedFieldLog("change period end", newExport.getTimestampEnd(), oldExport.getTimestampEnd(), dateFormat) +
                addChangedFieldLog("change period last days", newExport.getTimestampLastDays(), oldExport.getTimestampLastDays()) +
                addChangedFieldLog("creation period start", newExport.getCreationDateStart(), oldExport.getCreationDateStart(), dateFormat) +
                addChangedFieldLog("creation period end", newExport.getCreationDateEnd(), oldExport.getCreationDateEnd(), dateFormat) +
                addChangedFieldLog("creation period last days", newExport.getCreationDateLastDays(), oldExport.getCreationDateLastDays()) +
                addChangedFieldLog("ML binding period start", newExport.getMailinglistBindStart(), oldExport.getMailinglistBindStart(), dateFormat) +
                addChangedFieldLog("ML binding period end", newExport.getMailinglistBindEnd(), oldExport.getMailinglistBindEnd(), dateFormat) +
                addChangedFieldLog("ML binding period last days", newExport.getMailinglistBindLastDays(), oldExport.getMailinglistBindLastDays());
    }

    protected String getMailinglistChangeLog(ExportPredef oldExport, ExportPredef newExport, Admin admin) {
        return addChangedFieldLog("mailing list", newExport.getMailinglistID(), oldExport.getMailinglistID());
    }

    private String getExportColumnsCsv(ExportPredef newExport) {
        return StringUtils.join(newExport.getExportColumnMappings().stream()
                .map(ExportColumnMapping::getDbColumn)
                .toList(), ",");
    }

    protected boolean isInvalidForm(ExportForm form, Admin admin, Popups popups) {
        if (StringUtils.length(form.getShortname()) < 3) {
            popups.fieldError(SHORTNAME_FIELD, "error.name.too.short");
        }
        return popups.hasAlertPopups();
    }

    protected void prepareViewAttrs(Model model, Admin admin, ExportPredef export) {
        model.addAttribute("timeZones", TimeZone.getAvailableIDs());
        model.addAttribute("dateFormats", DateFormat.values());
        model.addAttribute("dateTimeFormats", DateFormat.values());
        model.addAttribute("targetGroups", targetService.getTargetLights(admin));
        model.addAttribute("mailinglists", getViewMailinglists(admin));
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        model.addAttribute("availableCharsetOptions", exportService.getAvailableCharsetOptionsForDisplay(admin, export));
        model.addAttribute("availableUserStatusOptions", exportService.getAvailableUserStatusOptionsForDisplay(admin, export));
        model.addAttribute("availableUserTypeOptions", exportService.getAvailableUserTypeOptionsForDisplay(admin, export));
        model.addAttribute("isManageAllowed", exportService.isManageAllowed(export, admin));
        model.addAttribute("isOwnColumnsExportAllowed", isOwnColumnsExportAllowed(admin));
        model.addAttribute("profileFields", columnInfoService
                .getComColumnInfos(admin.getCompanyID(), admin.getAdminID()).stream()
                .filter(t -> EnumSet.of(ProfileFieldMode.Editable, ProfileFieldMode.ReadOnly).contains(t.getModeEdit()))
                .collect(Collectors.toList()));
    }

    protected List<Mailinglist> getViewMailinglists(Admin admin) {
        return mailinglistService.getMailinglists(admin.getCompanyID());
    }

    private void prepareFormToCreateNewExport(ExportForm form, Admin admin) {
        form.setDateFormat(isGermanLang(admin) ? DateFormat.ddMMyyyy : DateFormat.MMddyyyy);
        form.setDateTimeFormat(isGermanLang(admin) ? DateFormat.ddMMyyyyHHmmss : DateFormat.MMddyyyyhhmmss);
        form.setDecimalSeparator(isGermanLang(admin) ? "," : ".");
        form.setTimezone(admin.getAdminTimezone());
        form.setLocale(admin.getLocale());
        form.setTimeLimitsLinkedByAnd(true);
    }

    protected void exportToForm(ExportPredef export, ExportForm form, Admin admin) {
        form.setShortname(export.getShortname());
        form.setDescription(export.getDescription());

        setRecipientsInfoToForm(export, form);
        setFileFormatToForm(export, form);
        setDateLimitsToForm(export, form, admin);

        exportMailinglistsToForm(export, form);
        exportColumnsToForm(export, form, admin);
    }

    private void exportColumnsToForm(ExportPredef export, ExportForm form, Admin admin) {
        form.setUserColumns(export.getExportColumnMappings().stream()
                .map(ExportColumnMapping::getDbColumn)
                .toArray(String[]::new));
        if (isOwnColumnsExportAllowed(admin)) {
            form.setCustomColumns(getExportCustomColumns(export, admin));
        }
    }

    private void exportMailinglistsToForm(ExportPredef export, ExportForm form) {
        form.setMailinglists(Arrays.stream(StringUtils.defaultString(export.getMailinglists()).split(";"))
                .mapToInt(NumberUtils::toInt)
                .toArray());
    }

    private void setRecipientsInfoToForm(ExportPredef export, ExportForm form) {
        form.setMailinglistId(export.getMailinglistID());
        if (CollectionUtils.isEmpty(export.getMailinglistIds())) {
            form.setMailinglistIds(Set.of(RecipientExportWorker.NO_MAILINGLIST));
        } else {
            form.setMailinglistIds(export.getMailinglistIds());
        }
        form.setTargetId(export.getTargetID());
        form.setUserType(export.getUserType());
        form.setUserStatus(export.getUserStatus());
    }

    private void setFileFormatToForm(ExportPredef export, ExportForm form) {
        form.setSeparator(export.getSeparator());
        form.setDelimiter(export.getDelimiter());
        form.setAlwaysQuote(export.isAlwaysQuote());
        form.setCharset(export.getCharset());
        form.setDateFormat(DateFormat.getDateFormatById(export.getDateFormat()));
        form.setDateTimeFormat(DateFormat.getDateFormatById(export.getDateTimeFormat()));
        form.setLocale(export.getLocale());
        form.setTimezone(export.getTimezone());
        form.setDecimalSeparator(export.getDecimalSeparator());
        form.setUseDecodedValues(export.isUseDecodedValues());
    }

    private void setDateLimitsToForm(ExportPredef export, ExportForm form, Admin admin) {
        form.setTimestampStart(formatDate(admin, export.getTimestampStart()));
        form.setTimestampEnd(formatDate(admin, export.getTimestampEnd()));
        form.setTimestampLastDays(export.getTimestampLastDays());
        form.setTimestampIncludeCurrentDay(export.isTimestampIncludeCurrentDay());

        form.setCreationDateStart(formatDate(admin, export.getCreationDateStart()));
        form.setCreationDateEnd(formatDate(admin, export.getCreationDateEnd()));
        form.setCreationDateLastDays(export.getCreationDateLastDays());
        form.setCreationDateIncludeCurrentDay(export.isCreationDateIncludeCurrentDay());

        form.setMailinglistBindStart(formatDate(admin, export.getMailinglistBindStart()));
        form.setMailinglistBindEnd(formatDate(admin, export.getMailinglistBindEnd()));
        form.setMailinglistBindLastDays(export.getMailinglistBindLastDays());
        form.setMailinglistBindIncludeCurrentDay(export.isMailinglistBindIncludeCurrentDay());

        form.setTimeLimitsLinkedByAnd(export.isTimeLimitsLinkedByAnd());
    }

    private String formatDate(Admin admin, Date date) {
        return date != null ? admin.getDateFormat().format(date) : "";
    }

    private boolean isGermanLang(Admin admin) {
        return "de".equalsIgnoreCase(admin.getAdminLang());
    }

    @GetMapping(value = "/delete.action")
    @RequiredPermission("export.delete")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateDeletion(bulkIds);

        ServiceResult<List<ExportPredef>> result = exportService.getAllowedForDeletion(bulkIds, admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(ExportPredef::getShortname).toList(),
                "export.ExportDelete", "export.delete.question",
                "bulk.export.delete", "bulk.export.delete.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("export.delete")
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateDeletion(bulkIds);
        ServiceResult<UserAction> result = exportService.delete(bulkIds, admin);

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return "redirect:/export/list.action?restoreSort=true";
    }

    private void validateDeletion(Set<Integer> ids) {
        if (isEmpty(ids)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    @PostMapping("/{id:\\d+}/evaluate.action")
    @RequiredPermission("wizard.export")
    public Object evaluate(@PathVariable int id, ExportForm form, Model model,
                           Admin admin, HttpSession session, Popups popups) {
        Callable<Object> exportWorker = () -> {
            if (form.isInProgress()) {
                LOGGER.error("Evaluation already in progress. Maybe some delayed client request occurred due to connection hang.");
                return ResponseEntity.noContent().build();
            }

            ExportPredef export = prepareExportToEvaluate(id, form, admin);

            if (!exportService.isManageAllowed(export, admin)) {
                throw new NotAllowedActionException();
            }

            RecipientExportWorker worker = exportService.getRecipientsToZipWorker(export, admin);
            worker.call();
            createExportReport(worker, admin, popups);
            writeEvaluationLog(form, admin, worker);
            model.addAttribute("titleKey", "export.finish")
                    .addAttribute("message", I18nString.getLocaleString("export.finished.result", admin.getLocale(), worker.getExportedLines()))
                    .addAttribute("downloadUrl", String.format("/export/%d/download.action", id))
                    .addAttribute("tmpFileName", new File(worker.getExportFile()).getName())
                    .addAttribute("success", true);
            return new ModelAndView("evaluation_finished", model.asMap());
        };
        updateProgressStatus(model, form);
        return new Pollable<>(
                PollingUid.builder(session.getId(), EXPORT_KEY).arguments(id, form.getExportStartTime()).build(),
                Pollable.DEFAULT_TIMEOUT,
                new ModelAndView("export_progress", Map.of("exportStartTime", form.getExportStartTime())),
                exportWorker);
    }

    private void updateProgressStatus(Model model, ExportForm form) {
        if (form.getExportStartTime() <= 0) {
            form.setExportStartTime(new Date().getTime());
        }
        model.addAttribute("progressPercentage", AgnUtils
                .calculateProgressPercentage(new Date(form.getExportStartTime())));
    }

    private ExportPredef prepareExportToEvaluate(int id, ExportForm form, Admin admin) {
        ExportPredef export = new ExportPredef();
        export.setId(id);
        export.setCompanyID(admin.getCompanyID());
        formToExport(form, export, admin);
        return export;
    }

    private void createExportReport(RecipientExportWorker worker, Admin admin, Popups popups) {
        if (worker.getError() != null) {
            recipientExportReporter.sendExportErrorMail(worker);
            recipientExportReporter.createAndSaveExportReport(worker, admin, true);
            popups.alert("export.result.error", worker.getError().getMessage());
        } else {
            recipientExportReporter.createAndSaveExportReport(worker, admin, false);
            recipientExportReporter.sendExportReportMail(worker);
        }
    }

    private void writeEvaluationLog(ExportForm form, Admin admin, RecipientExportWorker worker) {
        writeUserActivityLog(admin, "export",
                "Export started at: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(worker.getStartTime()) + ". " +
                        "ended at: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(worker.getEndTime()) + ". " +
                        "Number of profiles: " + worker.getExportedLines() + ". " +
                        "Export parameters:"
                        + getEvaluationMailinglistLog(form, admin) +
                        ", target group: " + getTargetNameById(form.getTargetId(), admin.getCompanyID()) +
                        ", recipient type: " + getRecipientTypeByLetter(form.getUserType()) +
                        ", recipient status: " + getRecipientStatusById(form.getUserStatus()) +
                        ", number of selected columns: " + form.getUserColumns().length);
    }

    protected String getEvaluationMailinglistLog(ExportForm form, Admin admin) {
        return " mailing list: " + getMailingListNameById(form.getMailinglistId(), admin.getCompanyID());
    }

    /**
     *  Get a text representation of export parameter "Recipient type"
     *
     * @param letter recipient type letter
     * @return text representation of recipient type
     */
    private String getRecipientTypeByLetter(String letter) {
        switch (letter) {
            case "E":
                return "All";
            case "A":
                return "Administrator";
            case "T":
                return "Test recipient";
            case "W":
                return "Normal recipient";
            default:
                return "not set";
        }
    }
    
    /**
     *  Get a text representation of export parameter "Recipient status"
     *
     * @param statusId recipient status id
     * @return text representation of recipient status
     */
    private String getRecipientStatusById(int statusId) {
        switch (statusId) {
            case 0:
                return "All";
            case 1:
                return "Active";
            case 2:
                return "Bounced";
            case 3:
                return "Opt-Out by admin";
            case 4:
                return "Opt-Out by recipient";
            case 5:
                return "Waiting for user confirmation";
            case 6:
                return "blacklisted";
            case 7:
                return "suspended";
            default:
                return "not set";
        }
    }
    
    /**
     *  Get a text representation of export parameter "Mailing list"
     *
     * @param listId mailing list id
     * @param companyId company id
     * @return a text representation of export parameter "Mailing list"
     */
    protected String getMailingListNameById(int listId, int companyId) {
       if (listId == 0) {
           return "All";
       } else if (listId == -1) {
           return "No mailing list";
       } else {
           return mailinglistService.getMailinglistName(listId, companyId);
       }
    }
    
    /**
     *  Get a text representation of export parameter "target group"
     *
     * @param targetId target group id
     * @param companyId company id
     * @return a text representation of export parameter "target group"
     */
    private String getTargetNameById(int targetId, int companyId) {
       return targetId == 0 ? "All" : targetService.getTargetName(targetId, companyId, true);
    }

    @GetMapping(value = "/{id:\\d+}/download.action", produces = "application/zip")
    @RequiredPermission("wizard.export")
    public Object download(@PathVariable int id, @RequestParam String tmpFileName, Admin admin, Popups popups) {
        ServiceResult<File> result = exportService.getExportFileToDownload(tmpFileName, admin);
        if (!result.isSuccess()) {
            popups.addPopups(result);
            return redirectToView(id);
        }
        String downloadFileName = result.getResult().getName();
        writeUserActivityLog(admin, "export recipients", downloadFileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(downloadFileName))
                .contentLength(result.getResult().length())
                .body(new FileSystemResource(result.getResult()));
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, LOGGER);
    }

}
