/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.updateForwardParameters;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.factory.MailingFactory;
import com.agnitas.dao.exception.TooBroadSearchException;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.imports.web.ImportController;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.bean.impl.MailingValidator;
import com.agnitas.emm.core.mailing.forms.MailingOverviewForm;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.validation.MailingSettingsFormValidator;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSettingsService;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.grid.grid.beans.GridTemplate;
import com.agnitas.preview.AgnTagException;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.MailingLightService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.FileUtils;
import com.agnitas.util.HtmlUtils;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class MailingController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingController.class);

    private static final String MAILING_OVERVIEW_KEY = "mailingOverview";
    private static final Set<String> EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS = Set.of(
            "emailMediatype.htmlTemplate",
            "emailMediatype.textTemplate",
            "textTemplate", // from content tab
            "htmlTemplate"  // from content tab
    );

    private static final String SETTINGS_VIEW = "mailing_settings";
    private static final String UNDO_ERROR_KEY = "error.undo_error";
    private static final String TEMPLATE_DYNTAGS_ERROR_KEY = "error.template.dyntags";
    private static final String IS_TEMPLATE_ATTR = "isTemplate";
    private static final String MAILING_ID_ATTR = "mailingId";
    private static final String MAILINGLISTS_ATTR = "mailinglists";
    private static final String REDIRECT_TO_LIST = "redirect:/mailing/list.action";

    protected final MailinglistApprovalService mailinglistApprovalService;
    protected final ExtendedConversionService conversionService;
    protected final MailingLightService mailingLightService;
    protected final MailinglistService mailinglistService;
    protected final GridServiceWrapper gridService;
    protected final TargetService targetService;
    protected final MailingService mailingService;
    protected final ConfigService configService;
    protected final AdminService adminService;
    protected final WorkflowService workflowService;
    protected final MailingSettingsService mailingSettingsService;
    private final UserActivityLogService userActivityLogService;
    private final JavaMailService javaMailService;
    private final MailingBaseService mailingBaseService;
    private final MailingParameterService mailingParameterService;
    private final MaildropService maildropService;
    private final MailingSettingsFormValidator mailingSettingsFormValidator;
    private final MailingFactory mailingFactory;
    private final MailingExporter mailingExporter;
    private final CopyMailingService copyMailingService;
    private final WebStorage webStorage;
    private final MailingValidator mailingValidator;
    private final ApplicationContext applicationContext;

    public MailingController(
            ExtendedConversionService conversionService,
            MailingLightService mailingLightService,
            MailinglistService mailinglistService,
            TargetService targetService,
            MailingService mailingService,
            ConfigService configService,
            AdminService adminService,
            UserActivityLogService userActivityLogService,
            JavaMailService javaMailService,
            MailingBaseService mailingBaseService,
            MailingParameterService mailingParameterService,
            WorkflowService workflowService,
            MaildropService maildropService,
            CopyMailingService copyMailingService,
            MailingSettingsFormValidator mailingSettingsFormValidator,
            MailingFactory mailingFactory,
            MailinglistApprovalService mailinglistApprovalService,
            MailingExporter mailingExporter,
            GridServiceWrapper gridService,
            WebStorage webStorage,
            MailingSettingsService mailingSettingsService,
            MailingValidator mailingValidator,
            ApplicationContext applicationContext) {
        this.conversionService = conversionService;
        this.mailingLightService = mailingLightService;
        this.mailinglistService = mailinglistService;
        this.targetService = targetService;
        this.mailingService = mailingService;
        this.configService = configService;
        this.adminService = adminService;
        this.userActivityLogService = userActivityLogService;
        this.javaMailService = javaMailService;
        this.mailingBaseService = mailingBaseService;
        this.mailingParameterService = mailingParameterService;
        this.workflowService = workflowService;
        this.maildropService = maildropService;
        this.copyMailingService = copyMailingService;
        this.mailingSettingsFormValidator = mailingSettingsFormValidator;
        this.mailingFactory = mailingFactory;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.mailingExporter = mailingExporter;
        this.gridService = gridService;
        this.webStorage = webStorage;
        this.mailingSettingsService = mailingSettingsService;
        this.mailingValidator = mailingValidator;
        this.applicationContext = applicationContext;
    }

    @ExceptionHandler(TooBroadSearchException.class)
    public String onTooBroadSearchException(HttpServletRequest req, Popups popups) {
        MailingOverviewForm form = FormUtils.bindRequestParameters(req, MailingOverviewForm.class);

        popups.alert("GWUA.error.search.tooBroad");
        LOGGER.warn("Too broad search from user. Name: '{}', Description: '{}', Content: '{}'",
                form.getFilterName(), form.getFilterDescription(), form.getFilterContent());

        return MESSAGES_VIEW;
    }

    @GetMapping("/list.action")
    @RequiredPermission("mailing.show")
    public Object list(MailingOverviewForm form, Model model, Admin admin, HttpServletRequest req, Popups popups, @RequestParam(required = false) Boolean restoreSort) {
        updateForwardParameters(req, true);
        FormUtils.updateSortingState(webStorage, WebStorage.MAILING_OVERVIEW, form, restoreSort);
        syncListStorage(form);

        if (!isValidSearch(form, popups)) {
            return MESSAGES_VIEW;
        }

        Callable<ModelAndView> listWorker = () -> {
            MailingsListProperties props = getListProperties(form, admin);
            ServiceResult<PaginatedList<Map<String, Object>>> result = mailingService.getOverview(admin, props);
            if (!result.isSuccess()) {
                popups.addPopups(result);
                return new ModelAndView(MESSAGES_VIEW);
            }
            addOverviewAttrs(form, model, admin, result);
            writeUserActivityLog(admin, "mailings list", "active tab - overview");
            return new ModelAndView("mailing_list", model.asMap());
        };
        return new Pollable<>(
                Pollable.uid(req.getSession().getId(), MAILING_OVERVIEW_KEY, form.toArray()),
                Pollable.LONG_TIMEOUT,
                new ModelAndView(REDIRECT_TO_LIST, form.toMap()),
                listWorker);
    }

    private boolean isValidSearch(MailingOverviewForm form, Popups popups) {
        if (StringUtils.length(form.getFilterName()) == 1) {
            popups.fieldError("filterName", "error.search.length");
        }

        if (StringUtils.length(form.getFilterDescription()) == 1) {
            popups.fieldError("filterDescription", "error.search.length");
        }

        if (StringUtils.length(form.getFilterContent()) == 1) {
            popups.fieldError("filterContent", "error.search.length");
        }

        Stream.of(form.getFilterName(), form.getFilterDescription(), form.getFilterContent())
                .flatMap(x -> DbUtilities.validateFulltextSearchQueryText(x).stream())
                .forEach(popups::alert);

        return !popups.hasAlertPopups();
    }

    private void addOverviewAttrs(MailingOverviewForm form, Model model, Admin admin, ServiceResult<PaginatedList<Map<String, Object>>> result) {
        model.addAttribute("mailinglist", result.getResult());
        model.addAttribute(MAILINGLISTS_ATTR, mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        addArchivesAttr(model, admin.getCompanyID());
        model.addAttribute("searchEnabled", mailingService.isBasicFullTextSearchSupported());
        model.addAttribute("contentSearchEnabled", !form.isForTemplates() && mailingService.isContentFullTextSearchSupported());
        model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat());
        model.addAttribute("adminDateFormat", admin.getDateFormat());
    }

    private void syncListStorage(MailingOverviewForm form) {
        if (form.isNumberOfRowsChanged()) {
            form.setPage(1);
        }
        webStorage.access(WebStorage.MAILING_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0 || form.getPage() > 1) {
                storage.setRowsCount(form.getNumberOfRows());
                storage.setMailingTypes(form.getMailingTypes());
                storage.setPage(form.getPage());

                if (!form.isForTemplates()) {
                    if (!form.isInEditColumnsMode()) {
                        form.setSelectedFields(storage.getSelectedFields());
                    }
                    storage.setMediaTypes(form.getMediaTypes());
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());
                form.setMailingTypes(storage.getMailingTypes());
                form.setPage(storage.getPage());

                if (!form.isForTemplates()) {
                    form.setSelectedFields(storage.getSelectedFields());
                    form.setMediaTypes(storage.getMediaTypes());
                }
            }
        });
    }

    protected MailingsListProperties getListProperties(MailingOverviewForm form, Admin admin) {
        MailingsListProperties props = new MailingsListProperties();
        props.setTypes(mailingTypesToCsv(form.getMailingTypes()));
        props.setTemplate(form.isForTemplates());
        props.setSearchNameStr(form.getFilterName());
        props.setSearchDescriptionStr(form.getFilterDescription());
        props.setSearchContentStr(form.getFilterContent());
        props.setSort(form.getSort());
        props.setDirection(form.getOrder());
        props.setPage(form.getPage());
        props.setRownums(form.getNumberOfRows());
        props.setAdditionalColumns(new HashSet<>(form.getSelectedFields()));
        props.setCreationDateBegin(tryParseDate(form.getFilterCreationDateBegin(), admin));
        props.setCreationDateEnd(tryParseDate(form.getFilterCreationDateEnd(), admin));
        props.setPlanDateBegin(tryParseDate(form.getFilterPlanDateBegin(), admin));
        props.setPlanDateEnd(tryParseDate(form.getFilterPlanDateEnd(), admin));
        props.setChangeDateBegin(tryParseDate(form.getFilterChangeDateBegin(), admin));
        props.setChangeDateEnd(tryParseDate(form.getFilterChangeDateEnd(), admin));
        props.setUseRecycleBin(form.isUseRecycleBin());
        props.setMailingLists(form.getFilterMailingLists());
        if (!form.isForTemplates()) {
            props.setBadge(form.getFilterBadges());
            props.setStatuses(formFilterStatusesToList(form));
            props.setArchives(form.getFilterArchives());
            props.setSendDateBegin(tryParseDate(form.getFilterSendDateBegin(), admin));
            props.setSendDateEnd(tryParseDate(form.getFilterSendDateEnd(), admin));
        }
        return props;
    }

    private List<String> formFilterStatusesToList(MailingOverviewForm form) {
        return CollectionUtils.emptyIfNull(form.getFilterStatuses()).stream()
                .map(MailingStatus::getDbKey)
                .collect(Collectors.toList());
    }

    private String mailingTypesToCsv(Set<MailingType> mailingTypes) {
        return mailingTypes.stream()
                .map(t -> String.valueOf(t.getCode()))
                .collect(Collectors.joining(","));
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

    @PostMapping("/setSelectedFields.action")
    @RequiredPermission("mailing.show")
    public @ResponseBody BooleanResponseDto updateSelectedFields(@RequestParam(required = false) List<String> selectedFields, Popups popups) {
        webStorage.access(WebStorage.MAILING_OVERVIEW, storage -> storage.setSelectedFields(selectedFields));
        popups.changesSaved();

        return new BooleanResponseDto(popups, !popups.hasAlertPopups());
    }

    @GetMapping("/{mailingId:\\d+}/settings.action")
    @RequiredPermission("mailing.show")
    public String view(@PathVariable int mailingId, @RequestParam(required = false) boolean checkMailingTags,
                       Admin admin, Model model, Popups popups, HttpServletRequest req) {
        updateForwardParameters(req, true);

        Mailing mailing = loadMailing(mailingId, admin);
        MailingSettingsForm form = mailingSettingsService.mailingToForm(mailing, admin);
        MailingSettingsOptions options = getOptionsBuilderForView(req, mailing).build();
        prepareMailingView(mailing, model, form, checkMailingTags, options, req, popups);

        writeUserActivityLog(admin, "view " + (getTypeForChangelog(mailing.isIsTemplate())), getMailingDescription(mailing) + " active tab - settings");
        return SETTINGS_VIEW;
    }

    private String getMailingDescription(Mailing mailing) {
        return String.format("%s (%d)", mailing.getShortname(), mailing.getId());
    }

    private MailingSettingsOptions.Builder getOptionsBuilderForView(HttpServletRequest req, Mailing mailing) {
        return getOptionsBuilderForView(req, false, false, mailing);
    }

    private MailingSettingsOptions.Builder getOptionsBuilderForView(HttpServletRequest req, boolean forCopy, boolean forFollowUp, Mailing mailing) {
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);
        return MailingSettingsOptions.builder()
                .setMailingId(mailing.getId())
                .setCompanyId(admin.getCompanyID())
                .setIsTemplate(mailing.isIsTemplate())
                .setWorkflowId(getWorkflowId(req, mailing.getId()))
                .setWorldSend(isMailingSentOrActive(mailing.getId(), admin))
                .setForCopy(forCopy)
                .setGridTemplateId(mailing.getId() > 0 ? gridService.getGridTemplateIdByMailingId(mailing.getId()) : 0)
                .setForFollowUp(forFollowUp);
    }

    @PostMapping("/{mailingId:\\d+}/restore.action")
    @RequiredPermission("mailing.change")
    public String restore(@PathVariable int mailingId, MailingOverviewForm form, Admin admin, Popups popups, RedirectAttributes ra) {
        mailingService.restoreMailing(mailingId, admin);
        popups.changesSaved();
        ra.addFlashAttribute("mailingOverviewForm", form);
        return REDIRECT_TO_LIST + "?restoreSort=true&forTemplates=" + form.isForTemplates();
    }

    @PostMapping("/bulkRestore.action")
    @RequiredPermission("mailing.change")
    public String bulkRestore(BulkActionForm form, MailingOverviewForm overviewForm, Admin admin, Popups popups, RedirectAttributes ra) {
        if (form.getBulkIds().isEmpty()) {
            popups.nothingSelected();
            return MESSAGES_VIEW;
        }

        mailingService.bulkRestore(form.getBulkIds(), admin);
        popups.changesSaved();

        ra.addFlashAttribute("mailingOverviewForm", overviewForm);
        return REDIRECT_TO_LIST + "?restoreSort=true&forTemplates=" + overviewForm.isForTemplates();
    }

    @GetMapping("/deleteMailings.action")
    @RequiredPermission("mailing.delete")
    public String confirmDeleteMailings(@RequestParam(required = false) Set<Integer> bulkIds,
                                        Admin admin, Popups popups, Model model) {
        return confirmDelete(bulkIds, false, admin, popups, model);
    }

    @GetMapping("/deleteTemplates.action")
    @RequiredPermission("template.delete")
    public String confirmDeleteTemplates(@RequestParam(required = false) Set<Integer> bulkIds,
                                         Admin admin, Popups popups, Model model) {
        return confirmDelete(bulkIds, true, admin, popups, model);
    }

    private String confirmDelete(Set<Integer> bulkIds, boolean isTemplate, Admin admin, Popups popups, Model model) {
        ServiceResult<List<Mailing>> result = mailingService.getMailingsForDeletion(bulkIds, admin);
        popups.addPopups(result);
        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }
        List<String> names = result.getResult().stream().map(MailingBase::getShortname).collect(Collectors.toList());
        if (isTemplate) {
            MvcUtils.addDeleteAttrs(model, names,
                    "template.delete", "template.delete.question",
                    "bulkAction.delete.template", "bulkAction.delete.template.question");
        } else {
            MvcUtils.addDeleteAttrs(model, names,
                    "mailing.MailingDelete", "mailing.delete.question",
                    "bulkAction.delete.mailing", "bulkAction.delete.mailing.question");
        }
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteMailings.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("mailing.delete")
    public String deleteMailings(@RequestParam(required = false) Set<Integer> bulkIds, @RequestParam(required = false) String toPage,
                                 Admin admin, Popups popups) {
        if (!delete(bulkIds, admin, popups) || "dashboard".equals(toPage)) {
            return MESSAGES_VIEW;
        }

        return StringUtils.isBlank(toPage) ? REDIRECT_TO_LIST + "?restoreSort=true" : "redirect:" + toPage;
    }

    @RequestMapping(value = "/deleteTemplates.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("template.delete")
    public String deleteTemplates(@RequestParam(required = false) Set<Integer> bulkIds,
                                  Admin admin, Popups popups) {
        if (delete(bulkIds, admin, popups)) {
            return REDIRECT_TO_LIST + "?restoreSort=true&forTemplates=true";
        }

        return MESSAGES_VIEW;
    }

    private boolean delete(Set<Integer> ids, Admin admin, Popups popups) {
        ServiceResult<List<UserAction>> result = mailingService.bulkDelete(ids, admin);
        if (!result.isSuccess()) {
            popups.addPopups(result);
            return false;
        }
        popups.selectionDeleted();
        result.getResult().forEach(ua -> writeUserActivityLog(admin, ua));
        return true;
    }

    @GetMapping("/{mailingId:\\d+}/export.action")
    @RequiredPermission("mailing.export")
    public Object export(@PathVariable int mailingId, Admin admin, Popups popups) throws IOException {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingService.getMailing(companyId, mailingId);
        String fileFriendlyMailingName = mailing.getShortname().replace("/", "_");
        String filename = "Mailing_" + fileFriendlyMailingName + "_" + companyId + "_" + mailingId + FileUtils.JSON_EXTENSION;
        File tmpFile = File.createTempFile("Mailing_" + companyId + "_" + mailingId, FileUtils.JSON_EXTENSION);
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            mailingExporter.exportMailingToJson(companyId, mailingId, outputStream, true, false);
        } catch (Exception e) {
            popups.alert("error.mailing.export");
            return redirectToView(mailingId);
        }
        writeUserActivityLog(admin, "export mailing", String.valueOf(mailingId));
        return ResponseEntity.ok()
                .contentLength(tmpFile.length())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(filename))
                .body(new DeleteFileAfterSuccessReadResource(tmpFile));
    }

    private String redirectToView(int mailingId) {
        return String.format("redirect:/mailing/%d/settings.action", mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/confirmUndo.action")
    @RequiredPermission("mailing.change")
    public String confirmUndo(@PathVariable int mailingId, Admin admin, Model model, Popups popups) {
        if (mailingId <= 0 || !mailingBaseService.checkUndoAvailable(mailingId)) {
            popups.alert(UNDO_ERROR_KEY);
            return MESSAGES_VIEW;
        }
        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        model.addAttribute(IS_TEMPLATE_ATTR, mailing.isIsTemplate());
        model.addAttribute("shortname", mailing.getShortname());
        return "mailing_undo";
    }

    @GetMapping("/{mailingId:\\d+}/undo.action")
    @RequiredPermission("mailing.change")
    public String undo(@PathVariable int mailingId, @RequestHeader(value = "referer", required = false) String referer, Popups popups, Admin admin) {
        if (mailingId <= 0 || !mailingBaseService.checkUndoAvailable(mailingId)) {
            popups.alert(UNDO_ERROR_KEY);
            return MESSAGES_VIEW;
        }

        restoreUndo(mailingId, admin);
        popups.changesSaved();

        return StringUtils.isNotBlank(referer)
                ? "redirect:" + referer
                : redirectToView(mailingId);
    }

    protected void restoreUndo(int mailingId, Admin admin) {
        mailingBaseService.restoreMailingUndo(applicationContext, mailingId, admin.getCompanyID());
        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        String description = String.format("%s %s (%d)", getTypeForChangelog(mailing.isIsTemplate()), mailing.getShortname(), mailing.getId());
        writeUserActivityLog(admin, "edit undo", description);
    }

    protected void prepareMailingView(Mailing mailing, Model model, MailingSettingsForm form, boolean showTagWarnings, MailingSettingsOptions options, HttpServletRequest req, Popups popups) {
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);
        int mailingId = mailing.getId();
        int companyId = admin.getCompanyID();
        List<TargetLight> allTargets = targetService.listTargetLightsForMailingSettings(admin, mailing);

        model.addAttribute(IS_TEMPLATE_ATTR, options.isTemplate());
        model.addAttribute("mailtracking", Boolean.toString(mailingLightService.isMailtrackingActive(admin.getCompanyID())));
        model.addAttribute("isActiveMailing", options.isWorldSend());
        model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, form.getMailinglistId()));
        model.addAttribute("MAILING_EDITABLE", isMailingEditable(mailing, admin, options));
        model.addAttribute("emailSettingsEditable", options.isTemplate() || !admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE));
        model.addAttribute("prioritizedMediatypes", getPrioritizedMediatypes(form, admin));
        model.addAttribute("worldMailingSend", !options.isForCopy() && !options.isForFollowUp() && options.isWorldSend());
        addArchivesAttr(model, companyId);
        model.addAttribute("showDynamicTemplateToggle", mailingService.isDynamicTemplateCheckboxVisible(mailing));
        model.addAttribute("templateShortname", options.isForFollowUp() || options.isForCopy() ? mailing.getShortname()
                : mailingBaseService.getMailingName(mailing.getMailTemplateID(), companyId));
        model.addAttribute("isCampaignEnableTargetGroups", configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, companyId));
        model.addAttribute("undoAvailable", mailingId > 0 && mailingBaseService.checkUndoAvailable(mailingId));
        model.addAttribute("workflowId", options.getWorkflowId());
        model.addAttribute("isSettingsReadonly", mailingService.isSettingsReadonly(admin, options.isTemplate()));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        addMailinglistsAttrs(form, admin, model, options);
        addTargetsAttrs(model, admin, mailing, allTargets, options);
        targetService.addSplitTargetModelAttrs(model, companyId, mailing.getSplitID(), form.getSplitSettings().getSplitBase(), form.getSplitSettings().getSplitPart());
        addAdditionalWorkflowForwardAttrs(form, req.getSession());
        addMailingGridTemplateAttrs(model, options, admin);
        if (showTagWarnings) {
            tryValidateMailingTagsAndComponents(mailing, req, popups);
        }
        model.addAttribute("mailingSettingsForm", form);
    }

    private void addArchivesAttr(Model model, int companyId) {
        model.addAttribute("archives", workflowService.getCampaignList(companyId, "lower(shortname)", 1).stream().limit(500).collect(Collectors.toList()));
    }

    private void tryValidateMailingTagsAndComponents(Mailing mailing, HttpServletRequest req, Popups popups) {
        try {
            Admin admin = AgnUtils.getAdmin(req);
            assert (admin != null);

            mailingValidator.validateMailingTagsAndComponents(mailing, admin.getLocale(), popups);
        } catch (AgnTagException e) {
            req.setAttribute("errorReport", e.getReport());
            popups.alert(TEMPLATE_DYNTAGS_ERROR_KEY);
        } catch (Exception e) {
            LOGGER.info(String.format("Error occurred: %s", e.getMessage()), e);
            popups.defaultError();
        }
    }

    protected void addAdditionalWorkflowForwardAttrs(MailingSettingsForm form, HttpSession session) {
        Map<String, String> forwardParams = AgnUtils.getParamsMap((String) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS));
        if (forwardParams.containsKey("workflowMailinglistId")) {
            form.setMailinglistId(Integer.parseInt(forwardParams.get("workflowMailinglistId")));
        }
    }

    protected int getWorkflowId(HttpServletRequest req, int mailingId) {
        int workflowId = WorkflowParametersHelper.getWorkflowIdFromSession(req.getSession());
        if (workflowId == 0) {
            workflowId = mailingBaseService.getWorkflowId(mailingId, AgnUtils.getCompanyID(req));
            // Check if a user came from a workflow manger in order to use this mailing there
        }
        return workflowId;
    }

    protected void addTargetsAttrs(Model model, Admin admin, Mailing mailing, List<TargetLight> allTargets, MailingSettingsOptions options) {
        boolean targetExpressionComplex = isTargetExpressionComplex(mailing);
        model.addAttribute("targets", targetService.filterTargetLightsByAltgMode(allTargets, AltgMode.NO_ALTG));
        model.addAttribute("targetComplexities", targetService.getTargetComplexities(admin.getCompanyID()));
        model.addAttribute("isTargetExpressionComplex", targetExpressionComplex);
        model.addAttribute("SHOW_TARGET_MODE_TOGGLE", mailingSettingsService.isTargetModeCheckboxVisible(mailing, targetExpressionComplex, options));
        model.addAttribute("TARGET_MODE_TOGGLE_DISABLED", mailingSettingsService.isTargetModeCheckboxDisabled(options));
    }

    protected boolean isTargetExpressionComplex(Mailing mailing) {
        return mailing.hasComplexTargetExpression(); // overridden in extended class
    }

    protected List<MediaTypes> getPrioritizedMediatypes(MailingSettingsForm form, Admin admin) {
        return List.of(MediaTypes.EMAIL);
    }

    private String getParentNameOrNameOfCopy(GridTemplate template) {
        String templateName = template.getName();
        if (template.getParentTemplateId() != 0) {
            template = gridService.getGridTemplate(template.getCompanyId(), template.getParentTemplateId());
            if (template != null) {
                templateName = template.getName();
            }
        }
        return templateName;
    }

    protected Mailing getMailingForSave(int mailingId, boolean isGrid, boolean isTemplate, MailingSettingsForm form,
                                        Admin admin, Popups popups) {
        int parentId = form.getParentId();
        if (parentId > 0 && mailingBaseService.isMailingExists(parentId, admin.getCompanyID())) {
            return copyMailingFromParent(parentId, isTemplate, form, admin, admin.getCompanyID());
        }
        return loadMailing(mailingId, admin);
    }

    private Mailing copyMailingFromParent(int parentId, boolean isTemplate, MailingSettingsForm form, Admin admin, int companyId) {
        int copiedMailingId = copyMailingService.copyMailing(companyId, parentId, companyId, form.getShortname(), form.getDescription());
        Mailing mailing = mailingService.getMailing(companyId, copiedMailingId);
        mailing.setCompanyID(companyId);
        mailing.setMailTemplateID(parentId);
        writeUserActivityLog(admin, "create " + getTypeForChangelog(isTemplate), "Copy from ID: " + parentId + ", to ID: " + copiedMailingId);
        return mailing;
    }

    private Mailing loadMailing(int mailingId, Admin admin) {
        Mailing mailing;
        if (mailingBaseService.isMailingExists(mailingId, admin.getCompanyID())) {
            mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        } else {
            mailing = generateNewMailing(admin.getCompanyID());
        }

        LOGGER.info("loadMailing: mailing loaded");
        return mailing;
    }

    private Mailing generateNewMailing(int companyId) {
        Mailing mailing;
        mailing = mailingFactory.newMailing();
        mailing.init(companyId, applicationContext);
        mailing.setId(0);
        mailing.setCompanyID(companyId);
        return mailing;
    }

    private void addMailingGridTemplateAttrs(Model model, MailingSettingsOptions options, Admin admin) {
        int gridTemplateId = options.getGridTemplateId();
        model.addAttribute("gridTemplateId", gridTemplateId);
        if (gridTemplateId > 0) {
            GridTemplate template = gridService.getGridTemplate(admin.getCompanyID(), gridTemplateId);
            if (template != null) {
                model.addAttribute("gridTemplateName", getParentNameOrNameOfCopy(template));
                model.addAttribute("ownerName", String.valueOf(getMailingGridInfo(gridTemplateId, options.getMailingId(), admin).get("OWNER_NAME")));
            }
        }
    }

    private Map<String, Object> getMailingGridInfo(int gridTemplateId, int mailingId, Admin admin) {
        Map<String, Object> meta = gridService.getMailingGridInfo(admin.getCompanyID(), mailingId);

        if (meta == null) {
            meta = mailingSettingsService.saveMailingGridInfo(gridTemplateId, mailingId, admin);
        }
        return meta;
    }

    @GetMapping("/{mailingId:\\d+}/copy.action")
    @RequiredPermission("mailing.change")
    public String copy(@PathVariable int mailingId, Admin admin, HttpServletRequest req, Model model,
                       @RequestParam(required = false) boolean forFollowUp, Popups popups) {
        updateForwardParameters(req, true);
        Mailing origin = mailingService.getMailing(admin.getCompanyID(), mailingId);

        if (forFollowUp) {
            int mailinglistID = origin.getMailinglistID();
            if (mailinglistID <= 0 || !mailinglistService.exist(mailinglistID, admin.getCompanyID())) {
                popups.alert("error.mailing.noMailinglist");
                return MESSAGES_VIEW;
            }
        }

        mailingSettingsService.removeInvalidTargets(origin, popups);
        MailingSettingsForm form = mailingSettingsService.prepareFormForCopy(origin, admin.getLocale(), forFollowUp);

        MailingSettingsOptions options = getOptionsBuilderForView(req, !forFollowUp, forFollowUp, origin)
                .setWorkflowId(0).build();
        form.setParentId(options.getGridTemplateId() > 0 ? options.getGridTemplateId() : mailingId);
        prepareMailingView(origin, model, form, false, options, req, popups);

        model.addAttribute(MAILING_ID_ATTR, 0);
        model.addAttribute("isCopying", true);
        return SETTINGS_VIEW;
    }

    /**
     * Sets available mailinglists only for the mailing view (not for list of mailings). <br>
     * If the admin has permission to see a mailinglist of the mailing
     * or mailing is copied
     * or mailing is created as followup
     * or there is no chosen mailinglist for the mailing
     * than sets all available mailinglists for the admin in the form. <br>
     * If the admin has no permission to see a mailinglist of mailing
     * than marks mailinglists as not editable.
     * So user will be able to only see the disabled mailinglist, but not to change it. <br>
     */
    private void addMailinglistsAttrs(MailingSettingsForm form, Admin admin, Model model, MailingSettingsOptions options) {
        List<Mailinglist> enabledMailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
        boolean exists = mailinglistService.exist(form.getMailinglistId(), admin.getCompanyID());

        if (exists
                && !options.isForCopy()
                && !options.isForFollowUp()
                && enabledMailinglists.stream().noneMatch(ml -> ml.getId() == form.getMailinglistId())) {
            model.addAttribute(MAILINGLISTS_ATTR, Collections.singletonList(mailinglistService.getMailinglist(form.getMailinglistId(), admin.getCompanyID())));
            model.addAttribute("mailinglistEditable", false);
        } else {
            checkIfSelectedMailinglistRemoved(model, form, exists, options);
            model.addAttribute(MAILINGLISTS_ATTR, enabledMailinglists);
            model.addAttribute("mailinglistEditable", true);
        }
    }

    private void checkIfSelectedMailinglistRemoved(Model model, MailingSettingsForm form, boolean exists, MailingSettingsOptions options) {
        if (!exists && !options.isTemplate()) {
            if (mailinglistService.mailinglistDeleted(form.getMailinglistId(), options.getCompanyId())
                    && (!options.isForCopy() && !options.isForFollowUp())) {
                model.addAttribute("selectedRemovedMailinglist", mailinglistService.getDeletedMailinglist(form.getMailinglistId(), options.getCompanyId()));
            } else {
                form.setMailinglistId(0);
            }
        }
    }

    // in case of modification, please check also saveFrameContent()
    @PostMapping("/{mailingId:\\d+}/settings.action")
    @RequiredPermission("mailing.change")
    public String save(@PathVariable int mailingId, HttpServletRequest req,
                       @RequestParam(defaultValue = "false") boolean isTemplate,
                       @RequestParam(defaultValue = "false") boolean isGrid,
                       @RequestParam(defaultValue = "false") boolean preventReload,
                       @ModelAttribute("mailingSettingsForm") MailingSettingsForm form, Popups popups) {
        int savedMailingId = saveMailing(mailingId, isGrid, isTemplate, form, popups, req);

        if (savedMailingId <= 0) {
            return MESSAGES_VIEW;
        }

        return preventReload ? MESSAGES_VIEW : redirectToView(savedMailingId);
    }

    private MailingSettingsOptions getOptionsForSave(Mailing mailing, boolean isNew, boolean isTemplate, MailingSettingsForm form, HttpServletRequest req) {
        int mailingId = mailing.getId();
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);
        return MailingSettingsOptions.builder()
                .setIsNew(isNew)
                .setMailingId(mailingId)
                .setIsTemplate(isTemplate)
                .setGridTemplateId(mailingId != 0 ? gridService.getGridTemplateIdByMailingId(mailingId) : 0)
                .setWorkflowId(getWorkflowId(req, mailingId))
                .setCompanyId(admin.getCompanyID())
                .setIsCopying(isNew && mailing.getId() > 0 && form.getParentId() > 0 && isTemplate == mailingService.isTemplate(form.getParentId(), admin.getCompanyID()))
                .setNewSplitId(targetService.getTargetListSplitIdForSave(mailing.getSplitID(), form.getSplitSettings().getSplitBase(), form.getSplitSettings().getSplitPart()))
                .setMailingParams(mailingParameterService.getMailingParameters(admin.getCompanyID(), mailingId))
                .setActiveOrSent(isMailingSentOrActive(mailingId, admin))
                .setWorkflowParams(WorkflowParametersHelper.find(req))
                .setSessionId(req.getSession(false).getId())
                .build();
    }

    private UserAction getSaveUserAction(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        final String type = getTypeForChangelog(options.isTemplate());
        final String shortnameEnding = form.getShortname() + " (" + mailing.getId() + ")";

        if (options.isNew()) {
            return new UserAction("create " + type, shortnameEnding);
        }
        String changelogs = collectChangelogs(mailing, form, options);
        if (isNotEmpty(changelogs) && form.getParentId() == 0) {
            mailingService.updateStatus(admin.getCompanyID(), mailing.getId(), MailingStatus.EDIT);
        }
        return new UserAction("edit " + type + " settings", changelogs + shortnameEnding);
    }

    /**
     * Create list of fields changed in mailing
     *
     * @param mailing - mailing to change loaded from database
     * @param form    - from user side
     * @return - list of string description of changes scheduled
     */
    private String collectChangelogs(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options) {
        return getPropertyChangelog("Short name", mailing.getShortname(), form.getShortname()) +
                getPropertyChangelog("Description", trimToEmpty(mailing.getDescription()), trimToEmpty(form.getDescription())) +
                (!workflowDriven(options.getWorkflowId()) ?
                        getPropertyChangelog("Mailing type", mailing.getMailingType().name(), form.getMailingType().name()) +
                                getPropertyChangelog("General archive", mailing.getCampaignID(), form.getArchiveId()) +
                                getPropertyChangelog("Archive", mailing.getArchived() != 0, form.isArchived()) +
                                getPropertyChangelog("Target mode", getTargetModeName(mailing.getTargetMode()), getTargetModeName(form.getTargetMode())) +
                                getTargetsChangelog(mailing, form) +
                                getMailinglistChangelog(mailing, form) : EMPTY) +
                getEmailMediatypeChangelog(mailing, form.getEmailMediatype()) +
                getSplitChangelog(mailing, form, options.getNewSplitId());
    }

    private String getTargetModeName(int mode) {
        return mode == Mailing.TARGET_MODE_AND ? "AND" : "OR";
    }

    private String getTargetsChangelog(Mailing mailing, MailingSettingsForm form) {
        Collection<Integer> mailingTargets = new ArrayList<>(CollectionUtils.emptyIfNull(mailing.getTargetGroups()));
        Collection<Integer> formTargets = getFormTargets(form);
        if (CollectionUtils.disjunction(mailingTargets, formTargets).isEmpty()) {
            return EMPTY;
        }
        Collection<Integer> intersection = CollectionUtils.intersection(mailingTargets, formTargets);
        Collection<Integer> removed = CollectionUtils.removeAll(mailingTargets, intersection);
        Collection<Integer> added = CollectionUtils.removeAll(formTargets, intersection);
        return "target groups " + "removed " + StringUtils.join(removed, ", ") + " added " + StringUtils.join(added, ", ") + "; ";
    }

    protected Collection<Integer> getFormTargets(MailingSettingsForm form) {
        return new ArrayList<>(CollectionUtils.emptyIfNull(form.getTargetGroupIds()));
    }

    private String getTextTemplateChangelog(Mailing mailing, EmailMediatypeForm form) {
        MailingComponent textComponent = mailing.getTextTemplate();
        if (textComponent == null || equalsIgnoreCase(textComponent.getEmmBlock(), form.getTextTemplate())) {
            return EMPTY;
        }
        return "mailing Frame content - Text block was changed; ";
    }

    private String getEmailMediatypeChangelog(Mailing mailing, EmailMediatypeForm form) {
        MediatypeEmail emailMediatype = mailing.getEmailParam();
        if (emailMediatype == null) {
            return EMPTY;
        }
        return getTextTemplateChangelog(mailing, form) +
                getHtmlTemplateChangelog(mailing, form) +
                getPropertyChangelog("Subject", emailMediatype.getSubject(), form.getSubject()) +
                getPropertyChangelog("Pre-Header", emailMediatype.getPreHeader(), form.getPreHeader()) +
                getPropertyChangelog("Format", getMailFormatName(emailMediatype.getMailFormat()), getMailFormatName(form.getMailFormat())) +
                getPropertyChangelog("Sender e-mail", emailMediatype.getFromEmail(), form.getFromEmail()) +
                getPropertyChangelog("Sender full name", trimToEmpty(emailMediatype.getFromFullname()), form.getFromFullname()) +
                getPropertyChangelog("Reply-to e-mail", emailMediatype.getReplyEmail(), form.getReplyEmail()) +
                getPropertyChangelog("Reply-to full name", trimToEmpty(emailMediatype.getReplyFullname()), form.getReplyFullname()) +
                getPropertyChangelog("Character set", emailMediatype.getCharset(), form.getCharset()) +
                getPropertyChangelog("Line break after", emailMediatype.getLinefeed(), form.getLinefeed()) +
                getPropertyChangelog("Measure opening rate", emailMediatype.getOnepixel(), form.getOnepixel());
    }

    private String getMailFormatName(int id) {
        switch (id) {
            case 0:
                return "only Text";
            case 1:
                return "Text and HTML";
            case 2:
                return "Text, HTML and Offline-HTML";
            default:
                return "Unknown mail format";
        }
    }

    protected String getPropertyChangelog(String valueName, Object oldValue, Object newValue) {
        return !Objects.equals(oldValue, newValue)
                ? "mailing " + valueName + " from " + oldValue + " to " + newValue + "; "
                : EMPTY;
    }

    private String getHtmlTemplateChangelog(Mailing mailing, EmailMediatypeForm form) {
        MailingComponent htmlComponent = mailing.getHtmlTemplate();
        if (htmlComponent != null && equalsIgnoreCase(htmlComponent.getEmmBlock(), form.getHtmlTemplate())) {
            return EMPTY;
        }
        return "mailing Frame content - Html block was changed; ";
    }

    private String getMailinglistChangelog(Mailing mailing, MailingSettingsForm form) {
        if (mailing.getMailinglistID() == form.getMailinglistId()) {
            return EMPTY;
        }
        if (form.getMailinglistId() == 0) {
            //send mail
            String message = "Mailinglist ID in mailing template (" + mailing.getId() + ") was set to 0.  Please check if the content still exists!";
            javaMailService.sendEmail(mailing.getCompanyID(), configService.getValue(ConfigValue.Mailaddress_Error), "Mailinglist set to 0", message, HtmlUtils.replaceLineFeedsForHTML(message));
        }
        return getPropertyChangelog("mailing list", mailing.getMailinglistID(), form.getMailinglistId());
    }

    private String getSplitChangelog(Mailing mailing, MailingSettingsForm form, int newSplitId) {
        if (mailing.getSplitID() == newSplitId) {
            return EMPTY;
        }
        if (newSplitId == Mailing.NONE_SPLIT_ID) {
            return "edit list split changed to " + Mailing.NONE_SPLIT + "; ";
        } else {
            return "edit list split changed to " + getFormattedSplitString(form.getSplitSettings().getSplitBase()) + " part #" + form.getSplitSettings().getSplitPart() + "; ";
        }
    }

    @ModelAttribute("mailingSettingsForm")
    public MailingSettingsForm getSettingsForm() {
        return getNewSettingsForm();
    }

    protected MailingSettingsForm getNewSettingsForm() {
        return new MailingSettingsForm();
    }

    /**
     * Format list split string from its name to human readable format.
     * For example converting "050505050575" to "5% / 5% / 5% / 5% / 5% / 75%"
     */
    private String getFormattedSplitString(String splitBase) {
        StringBuilder sb = new StringBuilder(splitBase);
        final int firstSeparatorPlace = 2;
        final int splitWithDelimiterLength = 6;
        for (int i = firstSeparatorPlace, j = 0;
             i < sb.length();
             i += splitWithDelimiterLength, j += splitWithDelimiterLength) {
            sb.insert(i, "% / ");
            if (sb.indexOf("0", j) == j) {
                sb.delete(j, j + 1);
                i--;
                j--;
            }
        }
        return sb.append("%").toString();
    }

    protected boolean isFormTargetsHaveConjunction(MailingSettingsForm form) {
        return form.getTargetMode() == Mailing.TARGET_MODE_AND; // overridden in extended class
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.contains(param);
    }

    private boolean isMailingSentOrActive(int mailingId, Admin admin) {
        return maildropService.isActiveMailing(mailingId, admin.getCompanyID());
    }

    private boolean isMailingEditable(Mailing mailing, Admin admin, MailingSettingsOptions options) {
        return options.isForCopy()
                || options.isForFollowUp()
                || !isMailingSentOrActive(mailing.getId(), admin);
    }

    protected boolean workflowDriven(Integer workflowId) {
        return workflowId != null && workflowId > 0;
    }

    private String getTypeForChangelog(boolean isTemplate) {
        return isTemplate ? "template" : "mailing";
    }

    @GetMapping("/templates.action")
    @RequiredPermission("template.show")
    public String templates(@ModelAttribute("filter") MailingTemplateSelectionFilter filter,
                            Model model, Admin admin, HttpServletRequest req, RedirectAttributes ra) {
        List<MailingBase> templates = mailingService.getTemplatesWithPreview(filter, admin);
        if (templates.isEmpty() && StringUtils.isBlank(filter.getName())) {
            Integer mediaType = Optional.ofNullable(filter.getMediaType())
                    .orElse(MediaTypes.EMAIL.getMediaCode());
            ra.addFlashAttribute("noMailingTemplatesFound", true);
            return "redirect:/mailing/new.action?mediaType=%d".formatted(mediaType);
        }

        model.addAttribute("templateMailingBases", templates);
        model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat());

        updateForwardParameters(req, true);
        return "mailing_templates";
    }

    @PostMapping("/generate.action")
    @RequiredPermission("mailing.change")
    public Object generateMailing(MailingSettingsForm settingsForm, @RequestParam int mediaType, @RequestParam boolean isGrid,
                                  @RequestParam boolean isTemplate, Popups popups, Admin admin, HttpServletRequest req) {
        boolean isWorkflowDriven = !WorkflowParametersHelper.isEmptyParams(req);
        MailingSettingsForm form = getSettingsForm();
        form.setParentId(settingsForm.getParentId());

        if (isGrid) {
            copyGridTemplateSettingsToForm(settingsForm.getParentId(), form, admin);
        } else if (mailingBaseService.isMailingExists(settingsForm.getParentId(), admin.getCompanyID())) {
            copyTemplateSettingsToForm(settingsForm.getParentId(), form, req);
        }

        setBaseInformationToForm(settingsForm, form, mediaType);

        mailingSettingsService.getMailingTypeFromForwardParams(WorkflowParametersHelper.find(req))
                .ifPresent(form::setMailingType);
        addAdditionalWorkflowForwardAttrs(form, req.getSession());

        int mailingId = saveMailing(0, isGrid, isTemplate, form, popups, req);
        if (mailingId <= 0) {
            return isWorkflowDriven
                    ? ResponseEntity.ok().body(new BooleanResponseDto(popups, false))
                    : MESSAGES_VIEW;
        }

        if (isWorkflowDriven) {
            // mark as deleted to prevent mailing displaying on overview in case if workflow will not be saved
            mailingService.deleteMailing(mailingId, admin);
            return ResponseEntity.ok().body(new DataResponseDto<>(
                    Map.of("mailingId", mailingId, "mailingName", form.getShortname()),
                    true
            ));
        }

        return redirectToView(mailingId);
    }

    private void copyTemplateSettingsToForm(int templateId, MailingSettingsForm form, HttpServletRequest req) {
        int companyId = AgnUtils.getCompanyID(req);

        Mailing mailing = mailingService.getMailing(companyId, templateId);
        mailingSettingsService.copyTemplateSettingsToMailingForm(mailing, form, true);
    }

    protected void copyGridTemplateSettingsToForm(int templateId, MailingSettingsForm form, Admin admin) {
        throw new UnsupportedOperationException();
    }

    private int saveMailing(int mailingId, boolean isGrid, boolean isTemplate, MailingSettingsForm form, Popups popups, HttpServletRequest req) {
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);

        if (mailingService.isSettingsReadonly(admin, isTemplate)) {
            throw new NotAllowedActionException();
        }

        Mailing mailing = getMailingForSave(mailingId, isGrid, isTemplate, form, admin, popups);
        if (mailing == null) {
            return 0;
        }

        mailingSettingsService.populateDisabledSettings(mailing, form, isGrid, admin, WorkflowParametersHelper.find(req));

        MailingSettingsOptions options = getOptionsForSave(mailing, mailingId == 0, isTemplate, form, req);
        if (!mailingSettingsFormValidator.isValidFormBeforeMailingSave(mailing, form, options, admin, popups)) {
            return 0;
        }

        UserAction userAction = getSaveUserAction(mailing, form, admin, options);
        if (!mailingSettingsService.saveSettings(mailing, form, admin, options, popups)) {
            return 0;
        }

        mailingValidator.validateMailingModules(mailing, popups);
        writeUserActivityLog(admin, userAction);
        popups.changesSaved();

        return mailing.getId();
    }

    protected void setBaseInformationToForm(MailingSettingsForm fromForm, MailingSettingsForm toForm, int mediaType) {
        toForm.setShortname(fromForm.getShortname());
        toForm.setMailinglistId(fromForm.getMailinglistId());
        toForm.setAssignTargetGroups(fromForm.isAssignTargetGroups());

        if (mediaType == MediaTypes.EMAIL.getMediaCode()) {
            toForm.getEmailMediatype().setSubject(fromForm.getEmailMediatype().getSubject());
            toForm.getEmailMediatype().setFromEmail(fromForm.getEmailMediatype().getFromEmail());
            toForm.getEmailMediatype().setReplyEmail(fromForm.getEmailMediatype().getReplyEmail());
        }

        toForm.getMediatypeForm(MediaTypes.getMediaTypeForCode(mediaType))
                .orElse(toForm.getEmailMediatype())
                .setActive(true);
    }

    @GetMapping("/new.action")
    @RequiredPermission("mailing.change")
    public String newClassic(@RequestParam(required = false, defaultValue = "0") int templateId,
                             @RequestParam(required = false, defaultValue = "false") boolean isTemplate,
                             @RequestParam(required = false) Integer mediaType,
                             Model model, Admin admin, HttpServletRequest req, Popups popups) {
        if (templateId == 0 && admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE)) {
            //could not create mailing without template if mailing.settings.hide permission activated
            popups.alert("error.permissionDenied");
            return MESSAGES_VIEW;
        }

        if (mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin).isEmpty()) {
            popups.alert("error.mailing.noMailinglist");
            return MESSAGES_VIEW;
        }

        MailingSettingsForm form = getNewSettingsForm();
        form.setParentId(templateId);

        Mailing mailingToCreate = loadMailing(templateId, admin);
        updateForwardParameters(req, true);
        if (mailingToCreate.getId() != 0) {
            mailingSettingsService.copyTemplateSettingsToMailingForm(mailingToCreate, form, false);
            mailingToCreate.setId(0);
        } else {
            form.getMediatypeForm(MediaTypes.getMediaTypeForCode(mediaType))
                    .orElse(form.getEmailMediatype())
                    .setActive(true);
        }

        MailingSettingsOptions options = getOptionsBuilderForView(req, mailingToCreate).setIsTemplate(isTemplate).build();

        model.addAttribute("mediaType", mediaType == null ? MediaTypes.EMAIL.getMediaCode() : mediaType);
        model.addAttribute("templateShortname", mailingBaseService.getMailingName(form.getParentId(), admin.getCompanyID()));
        model.addAttribute(IS_TEMPLATE_ATTR, isTemplate);
        addCommonAttrsForBaseSettingsView(form, options, admin, req, model);

        return "mailing_base_settings";
    }

    protected void addCommonAttrsForBaseSettingsView(MailingSettingsForm form, MailingSettingsOptions options, Admin admin, HttpServletRequest req, Model model) {
        mailingSettingsService.getMailingTypeFromForwardParams(WorkflowParametersHelper.find(req))
                .ifPresent(form::setMailingType);

        addAdditionalWorkflowForwardAttrs(form, req.getSession());
        addMailinglistsAttrs(form, admin, model, options);
        model.addAttribute("workflowId", options.getWorkflowId());
        model.addAttribute("mailingSettingsForm", form);
    }

    @GetMapping("/{mailingId:\\d+}/actions.action")
    @RequiredPermission("mailing.show")
    public String actions(@PathVariable int mailingId, Admin admin, Model model) {
        model.addAttribute("actions", mailingService.listTriggers(mailingId, admin.getCompanyID()));
        model.addAttribute("mailingShortname", mailingService.getMailingName(mailingId, admin.getCompanyID()));
        return "mailing_actions";
    }

    @GetMapping("/template/create.action")
    @RequiredPermission("template.change")
    public String createTemplate(Model model) {
        model.addAttribute("importType", ImportController.ImportType.TEMPLATE);
        model.addAttribute(IS_TEMPLATE_ATTR, true);
        return "mailing_creation_modal";
    }

    @GetMapping("/create.action")
    @RequiredPermission("mailing.change")
    public String create(Admin admin, HttpServletRequest req, Model model) {
        updateForwardParameters(req, true);
        addAttrsForCreationPage(req, admin, model);
        return "mailing_creation_modal";
    }

    protected void addAttrsForCreationPage(HttpServletRequest req, Admin admin, Model model) {
        model.addAttribute("workflowId", WorkflowParametersHelper.getWorkflowIdFromSession(req.getSession()));
        model.addAttribute("importType", ImportController.ImportType.MAILING);
    }

    @PostMapping("/{mailingId:\\d+}/frame.action")
    @RequiredPermission("mailing.change")
    public String saveFrameContent(@PathVariable int mailingId, FrameContentForm form, Admin admin,
                                   HttpSession session, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        if (!mailingSettingsService.saveFrameContent(mailing, form, admin, session.getId(), popups)) {
            return MESSAGES_VIEW;
        }

        mailingValidator.validateMailingModules(mailing, popups);
        popups.changesSaved();
        return "redirect:/mailing/content/" + mailingId + "/view.action";
    }

    @GetMapping("/{mailingId:\\d+}/name.action")
    @RequiredPermission("mailing.show")
    @ResponseBody
    public String name(@PathVariable int mailingId, Admin admin) {
        return mailingService.getMailingName(mailingId, admin.getCompanyID());
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, LOGGER);
    }

    private void writeUserActivityLog(Admin admin, UserAction ua) {
        UserActivityUtil.log(userActivityLogService, admin, ua, LOGGER);
    }

}
