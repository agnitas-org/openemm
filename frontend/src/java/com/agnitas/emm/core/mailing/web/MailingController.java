/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingCreationOption;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.bean.impl.MailingValidator;
import com.agnitas.emm.core.mailing.forms.MailingOverviewForm;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mailing.forms.validation.MailingSettingsFormValidator;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSettingsService;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowMailingAwareImpl;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.messages.Message;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.ComMailingContentChecker;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.UserMessageException;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.DynTagException;
import org.agnitas.util.FileUtils;
import org.agnitas.util.HtmlUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MissingEndTagException;
import org.agnitas.util.MvcUtils;
import org.agnitas.util.SafeString;
import org.agnitas.util.UnclosedTagException;
import org.agnitas.util.UserActivityUtil;
import org.agnitas.web.forms.BulkActionForm;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.ReservedMailingParam.isReservedParam;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.updateForwardParameters;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.DELETE_VIEW;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class MailingController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingController.class);

    private static final String MAILING_OVERVIEW_KEY = "mailingOverview";
    private static final Set<String> EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS = new HashSet<>();
    static {
        EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.add("emailMediatype.htmlTemplate");
        EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.add("emailMediatype.textTemplate");
        EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.add("textTemplate"); // from content tab
        EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.add("htmlTemplate"); // from content tab
    }
    private static final String SETTINGS_VIEW = "mailing_settings_view";
    private static final String UNDO_ERROR_KEY = "error.undo_error";
    private static final String TEMPLATE_DYNTAGS_ERROR_KEY = "error.template.dyntags";
    private static final String IS_TEMPLATE_ATTR = "isTemplate";
    private static final String MAILING_ID_ATTR = "mailingId";
    private static final String MAILINGLISTS_ATTR = "mailinglists";
    private static final String REDIRECT_TO_LIST = "redirect:/mailing/list.action";

    protected final MailinglistApprovalService mailinglistApprovalService;
    protected final ExtendedConversionService conversionService;
    protected final ComMailingLightService mailingLightService;
    protected final MailinglistService mailinglistService;
    protected final GridServiceWrapper gridService;
    protected final ComTargetService targetService;
    protected final MailingService mailingService;
    protected final ConfigService configService;
    protected final AdminService adminService;
    protected final ComWorkflowService workflowService;
    protected final MailingSettingsService mailingSettingsService;
    private final UserActivityLogService userActivityLogService;
    private final JavaMailService javaMailService;
    private final ComMailingBaseService mailingBaseService;
    private final DynamicTagDao dynamicTagDao;
    private final ComMailingParameterService mailingParameterService;
    private final LinkService linkService;
    private final MaildropService maildropService;
    private final AgnTagService agnTagService;
    private final AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private final MailingSettingsFormValidator mailingSettingsFormValidator;
    private final TAGCheckFactory tagCheckFactory;
    private final MailingFactory mailingFactory;
    private final PreviewImageService previewImageService;
    private final MailingExporter mailingExporter;
    private final CopyMailingService copyMailingService;
    private final WebStorage webStorage;
    private final MailingValidator mailingValidator;
    private final ApplicationContext applicationContext;

    public MailingController(
    		ExtendedConversionService conversionService,
    		ComMailingLightService mailingLightService,
    		MailinglistService mailinglistService,
    		ComTargetService targetService,
    		MailingService mailingService,
    		ConfigService configService,
    		AdminService adminService,
    		UserActivityLogService userActivityLogService,
    		JavaMailService javaMailService,
    		ComMailingBaseService mailingBaseService,
    		DynamicTagDao dynamicTagDao,
    		ComMailingParameterService mailingParameterService,
    		ComWorkflowService workflowService,
    		LinkService linkService,
    		MaildropService maildropService,
    		CopyMailingService copyMailingService,
    		AgnTagService agnTagService,
    		AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory,
    		MailingSettingsFormValidator mailingSettingsFormValidator,
    		TAGCheckFactory tagCheckFactory,
    		MailingFactory mailingFactory,
    		MailinglistApprovalService mailinglistApprovalService,
    		PreviewImageService previewImageService,
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
        this.dynamicTagDao = dynamicTagDao;
        this.mailingParameterService = mailingParameterService;
        this.workflowService = workflowService;
        this.linkService = linkService;
        this.maildropService = maildropService;
        this.copyMailingService = copyMailingService;
        this.agnTagService = agnTagService;
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
        this.mailingSettingsFormValidator = mailingSettingsFormValidator;
        this.tagCheckFactory = tagCheckFactory;
        this.mailingFactory = mailingFactory;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.previewImageService = previewImageService;
        this.mailingExporter = mailingExporter;
        this.gridService = gridService;
        this.webStorage = webStorage;
        this.mailingSettingsService = mailingSettingsService;
        this.mailingValidator = mailingValidator;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/list.action")
    public Object list(MailingOverviewForm form, Model model, Admin admin, HttpServletRequest req, Popups popups, @RequestParam(required = false) boolean restoreSort) {
        updateForwardParameters(req, true);
        FormUtils.updateSortingState(webStorage, WebStorage.MAILING_OVERVIEW, form, restoreSort);
        syncListStorage(form, admin);

        if (!isValidSearch(form, popups)) {
            return MESSAGES_VIEW;
        }
        
        Callable<ModelAndView> listWorker = () -> {
            MailingsListProperties props = getListProperties(form, admin);
            ServiceResult<PaginatedListImpl<Map<String, Object>>> result = mailingService.getOverview(admin, props);
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
                Pollable.SHORT_TIMEOUT,
                new ModelAndView(REDIRECT_TO_LIST, form.toMap()),
                listWorker);
    }

    private boolean isValidSearch(MailingOverviewForm form, Popups popups) {
        if (StringUtils.length(form.getSearchQueryText()) == 1) {
            popups.alert("error.search.length");
        }
        if (form.isSearchInName() || form.isSearchInDescription() || form.isSearchInContent()) {
            DbUtilities.validateFulltextSearchQueryText(form.getSearchQueryText())
                    .forEach(popups::alert);
        }
        return !popups.hasAlertPopups();
    }

    protected void addOverviewAttrs(MailingOverviewForm form, Model model, Admin admin, ServiceResult<PaginatedListImpl<Map<String, Object>>> result) {
        model.addAttribute("mailinglist", result.getResult());
        model.addAttribute(MAILINGLISTS_ATTR, mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        addArchivesAttr(model, admin.getCompanyID());
        if (isRedesign(admin)) {
            model.addAttribute("searchEnabled", mailingService.isBasicFullTextSearchSupported());
            model.addAttribute("contentSearchEnabled", !form.isForTemplates() && mailingService.isContentFullTextSearchSupported());
            model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat());
            model.addAttribute("adminDateFormat", admin.getDateFormat());
        } else {
            if (!form.isForTemplates()) {
                model.addAttribute("searchEnabled", mailingService.isBasicFullTextSearchSupported());
                model.addAttribute("contentSearchEnabled", mailingService.isContentFullTextSearchSupported());
            }
            model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat().toPattern());
            model.addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
        }
    }

    protected boolean isRedesign(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

    private void syncListStorage(MailingOverviewForm form, Admin admin) {
        if (form.isNumberOfRowsChanged()) {
            form.setPage(1);
        }
        webStorage.access(WebStorage.MAILING_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0 || form.getPage() > 1) {
                storage.setRowsCount(form.getNumberOfRows());

                if (!form.isForTemplates()) {
                    storage.setPage(form.getPage());
                    storage.setSearchQueryText(form.getSearchQueryText());
                    if (isRedesign(admin)) {
                        if (!form.isInEditColumnsMode()) {
                            form.setSelectedFields(storage.getSelectedFields());
                        }
                    } else {
                        storage.setSelectedFields(form.getSelectedFields());
                    }
                    storage.setMailingTypes(form.getMailingTypes());
                    storage.setMediaTypes(form.getMediaTypes());
                } else {
                    form.setSearchQueryText("");
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());

                if (!form.isForTemplates()) {
                    form.setPage(storage.getPage());
                    form.setSearchQueryText(storage.getSearchQueryText());
                    form.setSelectedFields(storage.getSelectedFields());
                    form.setMailingTypes(storage.getMailingTypes());
                    form.setMediaTypes(storage.getMediaTypes());
                } else {
                    form.setSearchQueryText("");
                }
            }
        });
    }

    protected MailingsListProperties getListProperties(MailingOverviewForm form, Admin admin) {
        MailingsListProperties props = new MailingsListProperties();
        props.setTypes(mailingTypesToCsv(form.getMailingTypes()));
        props.setTemplate(form.isForTemplates());
        props.setSearchQuery(form.getSearchQueryText());
        props.setSearchName(form.isSearchInName());
        props.setSearchDescription(form.isSearchInDescription());
        props.setSearchContent(form.isSearchInContent());
        props.setSearchNameStr(form.getFilterName());
        props.setRedesignedUiUsed(isRedesign(admin));
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
        if (isRedesign(admin)) {
            props.setMailingLists(form.getFilterMailingLists());
            if (!form.isForTemplates()) {
                props.setBadge(form.getFilterBadges());
                props.setStatuses(formFilterStatusesToList(form));
                props.setArchives(form.getFilterArchives());
                props.setSendDateBegin(tryParseDate(form.getFilterSendDateBegin(), admin));
                props.setSendDateEnd(tryParseDate(form.getFilterSendDateEnd(), admin));
            }
        } else {
            if (!form.isForTemplates()) {
                props.setBadge(form.getFilterBadges());
                props.setStatuses(formFilterStatusesToList(form));
                props.setMailingLists(form.getFilterMailingLists());
                props.setArchives(form.getFilterArchives());
                props.setSendDateBegin(tryParseDate(form.getFilterSendDateBegin(), admin));
                props.setSendDateEnd(tryParseDate(form.getFilterSendDateEnd(), admin));
            }
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
    public @ResponseBody BooleanResponseDto updateSelectedFields(@RequestParam(required = false) List<String> selectedFields, Popups popups) {
        webStorage.access(WebStorage.MAILING_OVERVIEW, storage -> storage.setSelectedFields(selectedFields));
        popups.success(CHANGES_SAVED_MSG);

        return new BooleanResponseDto(popups, !popups.hasAlertPopups());
    }

    @GetMapping("/{mailingId:\\d+}/settings.action")
    public String view(@PathVariable int mailingId, @RequestParam(required = false) boolean checkMailingTags,
                       Admin admin, Model model, Popups popups, HttpServletRequest req) {
        updateForwardParameters(req, true);

        Mailing mailing = loadMailing(mailingId, admin);
        MailingSettingsForm form = isRedesign(admin)
                ? mailingSettingsService.mailingToForm(mailing, admin)
                : mailingToForm(mailing, admin);
        MailingSettingsOptions options = getOptionsBuilderForView(req, false, false, mailing).build();
        prepareMailingView(mailing, model, form, checkMailingTags, options, req, popups);

        writeUserActivityLog(admin, "view " + (getTypeForChangelog(mailing.isIsTemplate())), getMailingDescription(mailing) + " active tab - settings");
        return SETTINGS_VIEW;
    }

    private String getMailingDescription(Mailing mailing) {
        return String.format("%s (%d)", mailing.getShortname(), mailing.getId());
    }

    protected MailingSettingsOptions.Builder getOptionsBuilderForView(HttpServletRequest req, boolean forCopy, boolean forFollowUp, Mailing mailing) {
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

    
    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    protected MailingSettingsForm mailingToForm(Mailing mailing, Admin admin) {
        MailingSettingsForm form = getNewSettingsForm();
        form.setShortname(mailing.getShortname());
        form.setDescription(mailing.getDescription());
        form.setMailingContentType(mailing.getMailingContentType());
        form.setMailingType(mailing.getMailingType());
        form.setMailinglistId(mailing.getMailinglistID());
        form.setArchiveId(mailing.getCampaignID());
        form.setArchived(mailing.getArchived() != 0);
        setMailingTargetsToForm(form, mailing);
        form.setNeedsTarget(mailing.getNeedsTarget());
        form.setUseDynamicTemplate(mailing.getUseDynamicTemplate());
        setMediatypesToForm(mailing, form);
        setMailingParamsToForm(form, mailing.getId(), admin.getCompanyID());
        setReferenceContentSettingsToForm(form, mailing.getId());
        setSplitTargetToForm(form, mailing.getSplitID(), true);
        if (mailing.getPlanDate() != null) {
            form.setPlanDate(definePlanDateFormat(mailing.getPlanDate(), admin).format(mailing.getPlanDate()));
        } else {
            form.setPlanDate("");
        }
        return form;
    }

    private static SimpleDateFormat definePlanDateFormat(Date planDate, Admin admin) {
        return new SimpleDateFormat("HH:mm:ss").format(planDate).equals("00:00:00") // time was not set
                ? admin.getDateFormat()
                : admin.getDateTimeFormat();
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    private void setMediatypesToForm(Mailing mailing, MailingSettingsForm form) {
        mailing.getMediatypes().values().forEach(mt -> form.getMediatypes()
                .put(mt.getMediaType().getMediaCode(), conversionService.convert(mt, MediatypeForm.class)));

        MailingComponent comp;

        for (MediaTypes type : MediaTypes.values()) {
			comp = mailing.getTemplate(type.getKey());
			if (comp != null) {
                setTemplateToMediatypeForm(form, comp, type);
			}
		}
        comp = mailing.getHtmlTemplate();
        if (comp != null) {
            form.getEmailMediatype().setHtmlTemplate(comp.getEmmBlock());
        }
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    protected void setTemplateToMediatypeForm(MailingSettingsForm form, MailingComponent comp, MediaTypes mediatype) {
        if (mediatype == MediaTypes.EMAIL) {
            form.getEmailMediatype().setTextTemplate(comp.getEmmBlock());
        }
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @GetMapping("/{mailingId:\\d+}/confirmDelete.action")
    public String confirmDelete(@PathVariable int mailingId, Admin admin, Popups popups, Model model,
                                @RequestParam(defaultValue = "") String fromPage) {
        ServiceResult<Mailing> result = mailingService.getMailingForDeletion(mailingId, admin);
        popups.addPopups(result);
        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }
        model.addAttribute(IS_TEMPLATE_ATTR, result.getResult().isIsTemplate());
        model.addAttribute("shortname", result.getResult().getShortname());
        model.addAttribute("fromPage", fromPage);
        return "mailing_delete";
    }

    @PostMapping("/{mailingId:\\d+}/restore.action")
    public String restore(@PathVariable int mailingId, MailingOverviewForm form, Admin admin, Popups popups, RedirectAttributes ra) {
        mailingService.restoreMailing(mailingId, admin);
        popups.success(CHANGES_SAVED_MSG);
        ra.addFlashAttribute("mailingOverviewForm", form);
        return REDIRECT_TO_LIST + "?restoreSort=true&forTemplates=" + form.isForTemplates();
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @PostMapping("/{mailingId:\\d+}/delete.action")
    public String delete(@PathVariable int mailingId, Admin admin, Popups popups,
                         @RequestParam(defaultValue = "") String fromPage,
                         @RequestParam(defaultValue = "false") boolean isTemplate) throws Exception {
        List<UserAction> userActions = mailingService.deleteMailing(mailingId, admin);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
        popups.success("default.selection.deleted");
        if ("dashboard".equals(fromPage)) {
            return "redirect:/dashboard.action";
        }
        return "redirect:/mailing/list.action?restoreSort=true&forTemplates=" + isTemplate;
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @GetMapping("/confirmBulkDelete.action")
    public String confirmBulkDelete(@RequestParam(defaultValue = "false") boolean forTemplates,
                                    Model model, BulkActionForm form, Admin admin, Popups popups) {
        ServiceResult<List<Mailing>> result = mailingService.getMailingsForDeletion(form.getBulkIds(), admin);
        if (!result.isSuccess()) {
            popups.addPopups(result);
            return MESSAGES_VIEW;
        }
        model.addAttribute("forTemplates", forTemplates);
        return "mailing_bulk_delete";
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @PostMapping("/bulkDelete.action")
    public String bulkDelete(@RequestParam(defaultValue = "false") boolean forTemplates,
                             BulkActionForm form, Admin admin, Popups popups) {
        ServiceResult<List<UserAction>> result = mailingService.bulkDelete(form.getBulkIds(), admin);
        if (!result.isSuccess()) {
            popups.addPopups(result);
            return MESSAGES_VIEW;
        }
        popups.success(SELECTION_DELETED_MSG);
        result.getResult().forEach(ua -> writeUserActivityLog(admin, ua));
        return "redirect:/mailing/list.action?restoreSort=true&forTemplates=" + forTemplates;
    }

    @PostMapping("/bulkRestore.action")
    public String bulkRestore(BulkActionForm form, MailingOverviewForm overviewForm, Admin admin, Popups popups, RedirectAttributes ra) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert(NOTHING_SELECTED_MSG);
            return MESSAGES_VIEW;
        }

        mailingService.bulkRestore(form.getBulkIds(), admin);
        popups.success(CHANGES_SAVED_MSG);

        ra.addFlashAttribute("mailingOverviewForm", overviewForm);
        return REDIRECT_TO_LIST + "?restoreSort=true&forTemplates=" + overviewForm.isForTemplates();
    }

    @GetMapping("/deleteMailings.action")
    public String confirmDeleteMailings(@RequestParam(required = false) Set<Integer> bulkIds,
                                        Admin admin, Popups popups, Model model) {
        return confirmDelete(bulkIds, false, admin, popups, model);
    }

    @GetMapping("/deleteTemplates.action")
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
    public String deleteMailings(@RequestParam(required = false) Set<Integer> bulkIds, @RequestParam(required = false) String toPage,
                                 Admin admin, Popups popups) {
        if (!delete(bulkIds, admin, popups) || "dashboard".equals(toPage)) {
            return MESSAGES_VIEW;
        }

        return StringUtils.isBlank(toPage) ? REDIRECT_TO_LIST + "?restoreSort=true" : "redirect:" + toPage;
    }

    @RequestMapping(value = "/deleteTemplates.action", method = {RequestMethod.POST, RequestMethod.DELETE})
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
        popups.success(SELECTION_DELETED_MSG);
        result.getResult().forEach(ua -> writeUserActivityLog(admin, ua));
        return true;
    }

    @GetMapping("/{mailingId:\\d+}/export.action")
    public Object export(@PathVariable int mailingId, Admin admin, Popups popups) throws IOException {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingService.getMailing(companyId, mailingId);
        String fileFriendlyMailingName = mailing.getShortname().replace("/", "_");
        String filename = "Mailing_" + fileFriendlyMailingName + "_" + companyId + "_" + mailingId + FileUtils.JSON_EXTENSION;
        File tmpFile = File.createTempFile("Mailing_" + companyId + "_" + mailingId, FileUtils.JSON_EXTENSION);
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            mailingExporter.exportMailingToJson(companyId, mailingId, outputStream, true);
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

    private String redirectToView(@PathVariable int mailingId) {
        return String.format("redirect:/mailing/%d/settings.action", mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/confirmUndo.action")
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
    public String undo(@PathVariable int mailingId, @RequestHeader(value = "referer", required = false) String referer, Popups popups, Admin admin) {
        if (mailingId <= 0 || !mailingBaseService.checkUndoAvailable(mailingId)) {
            popups.alert(UNDO_ERROR_KEY);
            return MESSAGES_VIEW;
        }
        try {
            restoreUndo(mailingId, admin);
            popups.success(CHANGES_SAVED_MSG);
            if (admin.isRedesignedUiUsed() && StringUtils.isNotBlank(referer)) {
                return "redirect:" + referer;
            }
            return redirectToView(mailingId);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to undo: %s", e.getMessage()), e);
            popups.alert(UNDO_ERROR_KEY);
            return MESSAGES_VIEW;
        }
    }

    protected void restoreUndo(int mailingId, Admin admin) throws Exception {
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
        if (isRedesign(admin)) {
            model.addAttribute("isActiveMailing", options.isWorldSend());
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, form.getMailinglistId()));
        } else {
            model.addAttribute("limitedRecipientOverview", options.isWorldSend() && !mailinglistApprovalService.isAdminHaveAccess(admin, form.getMailinglistId()));
        }
        model.addAttribute("MAILING_EDITABLE", isMailingEditable(mailing, admin, options));
        model.addAttribute("emailSettingsEditable", options.isTemplate() || !admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE));
        model.addAttribute("prioritizedMediatypes", getPrioritizedMediatypes(form, admin));
        model.addAttribute("worldMailingSend", !options.isForCopy() && !options.isForFollowUp() && options.isWorldSend());
        addArchivesAttr(model, companyId);
        model.addAttribute("showDynamicTemplateToggle", isRedesign(admin)
                ? mailingService.isDynamicTemplateCheckboxVisible(mailing)
                : isDynamicTemplateCheckboxVisible(options.isTemplate(), mailing.getMailTemplateID(), companyId));
        model.addAttribute("templateShortname", options.isForFollowUp() || options.isForCopy() ? mailing.getShortname()
                : mailingBaseService.getMailingName(mailing.getMailTemplateID(), companyId));
        model.addAttribute("isCampaignEnableTargetGroups", configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, companyId));
        model.addAttribute("undoAvailable", mailingId > 0 && mailingBaseService.checkUndoAvailable(mailingId));
        model.addAttribute("workflowId", options.getWorkflowId());
        model.addAttribute("isSettingsReadonly", mailingService.isSettingsReadonly(admin, options.isTemplate()));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        addMailinglistsAttrs(form, admin, model, options);
        addTargetsAttrs(model, admin, mailing, allTargets, options);
        if (!isRedesign(admin)) {
            addSplitTargetAttrs(model, companyId, mailing.getSplitID(), form.getSplitBase(), form.getSplitPart(), true);
        }
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
            if (isRedesign(admin)) {
                mailingValidator.validateMailingTagsAndComponents(mailing, admin.getLocale(), popups);
            } else {
                validateMailingTagsAndComponents(mailing, req, popups);
            }
        } catch (AgnTagException e) {
            req.setAttribute("errorReport", e.getReport());
            popups.alert(TEMPLATE_DYNTAGS_ERROR_KEY);
        } catch (Exception e) {
            logInfoException(String.format("Error occurred: %s", e.getMessage()), e);
            popups.alert(ERROR_MSG);
        }
    }

    protected void addAdditionalWorkflowForwardAttrs(MailingSettingsForm form, HttpSession session) {
        // Do nothing
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
        model.addAttribute("SHOW_TARGET_MODE_TOGGLE", isTargetModeCheckboxVisible(mailing, targetExpressionComplex, options));
        model.addAttribute("TARGET_MODE_TOGGLE_DISABLED", isTargetModeCheckboxDisabled(options));
    }

    protected boolean isTargetExpressionComplex(Mailing mailing) {
        return mailing.hasComplexTargetExpression(); // overridden in extended class
    }

    protected List<MediaTypes> getPrioritizedMediatypes(MailingSettingsForm form, Admin admin) {
        return List.of(MediaTypes.EMAIL);
    }

    // overridden in extended class
    protected void setMailingTargetsToForm(MailingSettingsForm form, Mailing mailing) {
        form.setTargetGroupIds(mailing.getTargetGroups());
        form.setTargetMode(mailing.getTargetMode());
        form.setTargetExpression(mailing.getTargetExpression());
    }

    private void updateMailingParams(Mailing mailing, Admin admin) {
        if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
            return;
        }
        List<UserAction> userActions = new ArrayList<>();
        mailingParameterService.updateParameters(admin.getCompanyID(), mailing.getId(), mailing.getParameters(), admin.getAdminID(), userActions);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
    }

    private String getParentNameOrNameOfCopy(ComGridTemplate template) {
        String templateName = template.getName();
        if (template.getParentTemplateId() != 0) {
            template = gridService.getGridTemplate(template.getCompanyId(), template.getParentTemplateId());
            if (template != null) {
                templateName = template.getName();
            }
        }
        return templateName;
    }

    private Mailing tryGetMailingForSave(int mailingId, boolean isGrid, boolean isTemplate, MailingSettingsForm form,
                                         Admin admin, Popups popups) {
        try {
            return getMailingForSave(mailingId, isGrid, isTemplate, form, admin, popups);
        } catch (Exception e) {
            popups.alert(ERROR_MSG);
            return null;
        }
    }

    protected Mailing getMailingForSave(int mailingId, boolean isGrid, boolean isTemplate, MailingSettingsForm form,
                                        Admin admin, Popups popups) throws Exception {
        int parentId = form.getParentId();
        if (parentId > 0 && mailingBaseService.isMailingExists(parentId, admin.getCompanyID())) {
            return copyMailingFromParent(parentId, isTemplate, form, admin, admin.getCompanyID());
        }
        return loadMailing(mailingId, admin);
    }

    private Mailing copyMailingFromParent(int parentId, boolean isTemplate, MailingSettingsForm form, Admin admin, int companyId) throws Exception {
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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("loadMailing: mailing loaded");
        }
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

    protected void setReferenceContentSettingsToForm(MailingSettingsForm form, int mailingId) {
        // Do nothing. Overridden in extended class
    }

    private void addMailingGridTemplateAttrs(Model model, MailingSettingsOptions options, Admin admin) {
        int gridTemplateId = options.getGridTemplateId();
        model.addAttribute("gridTemplateId", gridTemplateId);
        if (gridTemplateId > 0) {
            ComGridTemplate template = gridService.getGridTemplate(admin.getCompanyID(), gridTemplateId);
            if (template != null) {
                model.addAttribute("gridTemplateName", getParentNameOrNameOfCopy(template));
                model.addAttribute("ownerName", String.valueOf(getMailingGridInfo(gridTemplateId, options.getMailingId(), admin).get("OWNER_NAME")));
            }
        }
    }

    private Map<String, Object> getMailingGridInfo(int gridTemplateId, int mailingId, Admin admin) {
        Map<String, Object> meta = gridService.getMailingGridInfo(admin.getCompanyID(), mailingId);

        if (meta == null) {
            meta = isRedesign(admin)
                    ? mailingSettingsService.saveMailingGridInfo(gridTemplateId, mailingId, admin)
                    : saveMailingGridInfo(gridTemplateId, mailingId, admin);
        }
        return meta;
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private Map<String, Object> saveMailingGridInfo(int gridTemplateId, int mailingId, Admin admin) {
        Map<String, Object> data = new HashMap<>();
        data.put("TEMPLATE_ID", gridTemplateId);
        data.put("OWNER", admin.getAdminID());
        data.put("OWNER_NAME", admin.getUsername());

        gridService.saveMailingGridInfo(mailingId, admin.getCompanyID(), data);
        return data;
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    private void setMailingParamsToForm(MailingSettingsForm form, int mailingId, int companyId) {
        List<ComMailingParameter> params = mailingParameterService.getMailingParameters(companyId, mailingId);
        form.setParams(params.stream()
                .filter(param -> !isReservedParam(param.getName()))
                .collect(Collectors.toList()));
    }

    // Redesigned in MailingValidatorImpl.java TODO remove when EMMGUI-714 migration finished
    private void validateMailingTagsAndComponents(Mailing mailing, HttpServletRequest request, Popups popups) throws Exception {
        List<String[]> errorReports = new ArrayList<>();

        Map<String, List<AgnTagError>> agnTagsValidationErrors = mailing.checkAgnTagSyntax(applicationContext);
        if (MapUtils.isNotEmpty(agnTagsValidationErrors)) {
            validateMailingTags(request, popups, errorReports, agnTagsValidationErrors);
        } else {
            validateMailingComponents(mailing, request, popups, errorReports);
        }

        if (CollectionUtils.isNotEmpty(errorReports)) {
            throw new AgnTagException(TEMPLATE_DYNTAGS_ERROR_KEY, errorReports);
        }
    }

    // Redesigned in MailingValidatorImpl.java TODO remove when EMMGUI-714 migration finished
    private void validateMailingComponents(Mailing mailing, HttpServletRequest request, Popups popups, List<String[]> errorReports) throws Exception {
        Map<String, MailingComponent> components = mailing.getComponents();
        if (validateDeprecatedTags(mailing.getCompanyID(), components, popups) && mailing.getId() > 0) {
            // Only use backend/preview agn syntax check if mailing was already stored in database before (= not new mailing)
            List<String> outFailures = new ArrayList<>();
            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailing.getCompanyID(), mailing.getId(), mailing.getMailinglistID(), AgnUtils.getLocale(request));

            try {
                components.forEach((name, component) -> {
                    StringBuffer reportContents = new StringBuffer();
                    if (component.getEmmBlock() != null && !tagCheck.checkContent(component.getEmmBlock(), reportContents, outFailures)) {
                        appendErrorsToList(name, errorReports, reportContents);
                    }
                });
            } finally {
                tagCheck.done();
            }
        }
    }

    // Redesigned in MailingValidatorImpl.java. TODO remove when EMMGUI-714 migration finished
    private void validateMailingTags(HttpServletRequest request, Popups popups, List<String[]> errorReports, Map<String, List<AgnTagError>> agnTagsValidationErrors) {
        agnTagsValidationErrors.forEach((componentName, validationErrors) -> {
            // noinspection ThrowableResultOfMethodCallIgnored

            if (componentName.startsWith("agn")) {
                componentName = componentName.substring(3);
            }
            AgnTagError firstError = validationErrors.get(0);
            popups.alert("error.agntag.mailing.component", componentName, firstError.getFullAgnTagText());
            popups.alert(firstError.getErrorKey().getMessageKey(), firstError.getAdditionalErrorDataWithLineInfo());

            for (AgnTagError error : validationErrors) {
                errorReports.add(new String[]{componentName, error.getFullAgnTagText(), error.getLocalizedMessage(request.getLocale())});
            }
        });
    }

    // Redesigned in MailingValidatorImpl.java. TODO remove when EMMGUI-714 migration finished
    /**
     * Creates report about errors in dynamic tags.
     *
     * @param blockName      name of content block with invalid content
     * @param errorReports   list of messages about parsing errors (is changing inside the method)
     * @param templateReport content with errors
     */
    protected void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
        Map<String, String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
        for (Map.Entry<String, String> entry : tagsWithErrors.entrySet()) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName; // block
            errorRow[1] = entry.getKey(); // tag
            errorRow[2] = entry.getValue(); // value

            errorReports.add(errorRow);
        }
        List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);
        for (String error : errorsWithoutATag) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName;
            errorRow[1] = "";
            errorRow[2] = error;
            errorReports.add(errorRow);
        }
    }

    // Redesigned in MailingValidatorImpl.java. TODO remove when EMMGUI-714 migration finished
    private boolean validateDeprecatedTags(final int companyId, final Map<String, MailingComponent> components, Popups popups) {
        boolean valid = true;
        for (MailingComponent component : components.values()) {
            if (component.getEmmBlock() != null) {
                final Set<String> deprecatedTagsNames = agnTagService.parseDeprecatedTagNamesFromString(component.getEmmBlock(), companyId);
                if (CollectionUtils.isNotEmpty(deprecatedTagsNames)) {
                    deprecatedTagsNames.forEach(el -> popups.warning("warning.mailing.agntag.deprecated", el));
                    valid = false;
                }
            }
        }
        return valid;
    }


    // TODO EMMGUI-714 redesigned in MailingSettingsServiceImpl. Remove when migration finished
    /**
     * Loads chosen mailing template data into form.
     *
     * @param template        Mailing bean object, contains mailing template data
     * @param form            MailingSettingsForm object
     * @param regularTemplate whether the regular template is used or a mailing (clone & edit)
     */
    protected void copyTemplateSettingsToMailingForm(Mailing template, MailingSettingsForm form, HttpServletRequest req, boolean regularTemplate) {
        MailingComponent tmpComp;
        Integer workflowId = getWorkflowIdFromReq(req);
        // If we already have a campaign we don't have to override settings inherited from it
        boolean overrideInherited = (workflowId == null || workflowId == 0 || !regularTemplate);

        if (overrideInherited) {
            form.setMailingType(template.getMailingType());
            form.setMailinglistId(template.getMailinglistID());
            form.setArchiveId(template.getCampaignID());
        }
        if (overrideInherited || template.getMailingType() == MailingType.DATE_BASED) {
            form.setTargetGroupIds(template.getTargetGroups());
        }
        form.setTargetMode(getTargetModeForForm(template));
        setMediatypesToForm(template, form);
        form.setArchived(template.getArchived() != 0);
        form.setNeedsTarget(template.getNeedsTarget());
        form.setUseDynamicTemplate(template.getUseDynamicTemplate());
        form.setMailingContentType(template.getMailingContentType());

        // load template for this mailing
        EmailMediatypeForm emailMediatypeForm = form.getEmailMediatype();
        if ((tmpComp = template.getHtmlTemplate()) != null) {
            emailMediatypeForm.setHtmlTemplate(tmpComp.getEmmBlock());
        }
        if ((tmpComp = template.getTextTemplate()) != null) {
            emailMediatypeForm.setTextTemplate(tmpComp.getEmmBlock());
        }

        MediatypeEmail emailMediatype = template.getEmailParam();
        if (emailMediatype != null) {
            emailMediatypeForm.setOnepixel(emailMediatype.getOnepixel());
            try {
                emailMediatypeForm.setReplyEmail(new InternetAddress(emailMediatype.getReplyAdr()).getAddress());
            } catch (Exception e) {
                // do nothing
            }
            try {
                emailMediatypeForm.setReplyFullname(new InternetAddress(emailMediatype.getReplyAdr()).getPersonal());
            } catch (Exception e) {
                // do nothing
            }
        }

        // Create a clone copy of all mailing parameters
        List<ComMailingParameter> templateMailingParameters = mailingParameterService.getMailingParameters(template.getCompanyID(), template.getId());
        List<ComMailingParameter> newParameters = new ArrayList<>();

        if (templateMailingParameters != null) {
            for (ComMailingParameter parameter : templateMailingParameters) {
                ComMailingParameter newParameter = new ComMailingParameter();

                newParameter.setName(parameter.getName());
                newParameter.setValue(parameter.getValue());
                newParameter.setDescription(parameter.getDescription());
                newParameter.setCreationDate(parameter.getCreationDate());

                newParameters.add(newParameter);
            }
        }
        form.setParams(newParameters);
    }

    protected int getTargetModeForForm(Mailing mailing) {
        return mailing.getTargetMode(); // overridden in extended class
    }

    protected void setSplitTargetToForm(MailingSettingsForm form, int splitId, boolean preserveCmListSplit) {
        if (splitId > 0) {
            String name = targetService.getTargetSplitName(splitId);

            if (isNotEmpty(name)) {
                if (name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
                    if (preserveCmListSplit) {
                        form.setSplitBase(name.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length(), name.lastIndexOf('_')));
                        form.setSplitPart(name.substring(name.lastIndexOf("_") + 1));
                        return;
                    }
                } else {
                    form.setSplitBase(name.substring(12, name.indexOf('_', 13)));
                    form.setSplitPart(name.substring(name.indexOf('_', 13) + 1));
                    return;
                }
            }
        }
        form.setSplitBase(splitId == Mailing.YES_SPLIT_ID ? Mailing.YES_SPLIT : Mailing.NONE_SPLIT);
        form.setSplitPart("1");
    }

    protected void addSplitTargetAttrs(Model model, int companyId, int splitId, String splitBase, String splitPart, boolean preserveCmListSplit) {
        model.addAttribute("splitId", splitId);
        model.addAttribute("splitTargets", targetService.getSplitTargetLights(companyId, "").stream().limit(500).collect(Collectors.toList()));
        model.addAttribute("splitTargetsForSplitBase", targetService.getSplitTargetLights(companyId, splitBase).stream().limit(500).collect(Collectors.toList()));
        if (splitId > 0) {
            String name = targetService.getTargetSplitName(splitId);
            if (isNotEmpty(name)
                    && name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)
                    && preserveCmListSplit) {
                String[] parts = splitBase.split(";");
                StringBuilder splitBaseMessage = new StringBuilder();
                for (int i = 1; i <= parts.length; i++) {
                    String part = parts[i - 1];
                    splitBaseMessage.append(part).append("% / ");
                    if (i == Integer.parseInt(splitPart)) {
                        model.addAttribute("splitPartMessage", i + ". " + part + "%");
                    }
                }
                model.addAttribute("splitBaseMessage", splitBaseMessage.substring(0, splitBaseMessage.length() - 2));
                model.addAttribute("wmSplit", true);
            }
        }
    }

    protected boolean isWmSplit(int splitId) {
        if (splitId > 0) {
            String name = targetService.getTargetSplitName(splitId);
            return isNotEmpty(name)
                    && name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX);
        }
        return false;
    }

    @GetMapping("/{mailingId:\\d+}/copy.action")
    public String copy(@PathVariable int mailingId, Admin admin, HttpServletRequest req, Model model,
                       @RequestParam(required = false) boolean forFollowUp, Popups popups) {
        updateForwardParameters(req, true);
        Mailing origin = mailingService.getMailing(admin.getCompanyID(), mailingId);
        if (origin == null) {
            popups.alert(ERROR_MSG);
            return MESSAGES_VIEW;
        }
        if (forFollowUp) {
            int mailinglistID = origin.getMailinglistID();
            if (mailinglistID <= 0 || !mailinglistService.exist(mailinglistID, admin.getCompanyID())) {
                popups.alert("error.mailing.noMailinglist");
                return MESSAGES_VIEW;
            }
        }
        
        MailingSettingsForm form;
        if (isRedesign(admin)) {
            mailingSettingsService.removeInvalidTargets(origin, popups);
            Integer workflowId = getWorkflowIdFromReq(req);
            form = mailingSettingsService.prepareFormForCopy(origin, admin.getLocale(), workflowId, forFollowUp);
        } else {
            removeInvalidTargets(origin, popups);
            form = prepareFormForCopy(origin, req, forFollowUp);
        }
        MailingSettingsOptions options = getOptionsBuilderForView(req, !forFollowUp, forFollowUp, origin)
                .setWorkflowId(0).build();
        form.setParentId(options.getGridTemplateId() > 0 ? options.getGridTemplateId() : mailingId);
        prepareMailingView(origin, model, form, false, options, req, popups);

        model.addAttribute(MAILING_ID_ATTR, 0);
        model.addAttribute("isCopying", true);
        return SETTINGS_VIEW;
    }
    
    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private final void removeInvalidTargets(final Mailing mailing, final Popups popups) {
    	if(mailing.getTargetGroups() != null) {
	    	final List<TargetLight> targets = this.targetService.getTargetLights(mailing.getCompanyID(), mailing.getTargetGroups(), true);
	    	final Set<Integer> toRemove = new HashSet<>();
	
	    	for(final TargetLight target : targets) {
	    		if(!target.isValid()) {
		    		popups.warning(new Message("warning.mailing.import.targetgroupInvalid", new Object[] {target.getId(), target.getTargetName() }));
		    		
		    		toRemove.add(target.getId());
	    		}
	    	}
	    	
	    	mailing.getTargetGroups().removeAll(toRemove);
    	}
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    protected MailingSettingsForm prepareFormForCopy(Mailing origin, HttpServletRequest req, boolean forFollowUp) {
        MailingSettingsForm form = getNewSettingsForm();

        copyTemplateSettingsToMailingForm(origin, form, req, false);
        form.setShortname(forFollowUp
                ? SafeString.getLocaleString("mailing.Followup_Mailing", AgnUtils.getLocale(req)) + " " + origin.getShortname()
                : SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(req)) + " " + origin.getShortname());
        form.setDescription(forFollowUp ? "" : origin.getDescription());
        setSplitTargetToForm(form, origin.getSplitID(), false);
        return form;
    }

    private boolean isDynamicTemplateCheckboxVisible(boolean isTemplate, int templateId, int companyId) {
        if (isTemplate) {
            // For templates checkbox is always show and enabled
            return true;
        } else if (templateId != 0) {
            // For mailings, checkbox is always shows if and only if referenced mailing-record defines template
            // Checkbox is only enabled, if such a mailing has ID 0 (new mailing)
            return mailingService.checkMailingReferencesTemplate(templateId, companyId);
        } else {
            // in all other cases, the checkbox is hidden
            return false;
        }
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
    public String save(@PathVariable int mailingId, HttpServletRequest req, Admin admin,
                       @RequestParam(defaultValue = "false") boolean isTemplate,
                       @RequestParam(defaultValue = "false") boolean isGrid,
                       @ModelAttribute("mailingSettingsForm") MailingSettingsForm form, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, isTemplate)) {
            throw new UnsupportedOperationException();
        }

        Mailing mailing = tryGetMailingForSave(mailingId, isGrid, isTemplate, form, admin, popups);
        if (mailing == null) {
            return MESSAGES_VIEW;
        }
        if (isRedesign(admin)) {
            mailingSettingsService.populateDisabledSettings(mailing, form, isGrid, admin, WorkflowParametersHelper.find(req));
        } else {
            populateDisabledSettings(req, isGrid, form, mailing);
        }
        MailingSettingsOptions options = getOptionsForSave(mailing, mailingId == 0, isTemplate, form, req);
        if (!mailingSettingsFormValidator.isValidFormBeforeMailingSave(mailing, form, options, admin, popups)) {
            return MESSAGES_VIEW;
        }
        UserAction userAction = getSaveUserAction(mailing, form, admin, options);
        
        if (isRedesign(admin)) {
            if (!mailingSettingsService.saveSettings(mailing, form, admin, options, popups)) {
                return MESSAGES_VIEW;
            }
        } else {
            if (!tryPrepareMailingForSave(mailing, form, options, req, popups)
                    || !trySaveMailing(mailing, form, options, req, popups)) {
                return MESSAGES_VIEW;
            }
        }

        validateSavedMailing(mailing, admin, popups);
        writeUserActivityLog(admin, userAction);
        popups.success(CHANGES_SAVED_MSG);
        return redirectToView(mailing.getId());
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    private void populateDisabledSettings(HttpServletRequest req, boolean isGrid, MailingSettingsForm form, Mailing mailing) {
        Admin admin = AgnUtils.getAdmin(req);
        assert admin != null;
        if (!mailing.isIsTemplate() && admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE)) {
            populateDisabledGeneralSettings(form, mailing);
        }
        populateActiveMailingSettings(form, mailing, admin);
        populateWorkflowDrivenSettings(form, mailing.getId() == 0 && isGrid ? new MailingImpl() : mailing, req);
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    // When 'mailing.settings.hide' permission is set
    // then general settings should be passed from original mailing
    // But text template and html template should be editable
    protected void populateDisabledGeneralSettings(MailingSettingsForm form, Mailing mailing) {
        form.setMailinglistId(mailing.getMailinglistID());
        form.setMailingType(mailing.getMailingType());
        form.setArchived(mailing.getArchived() == 1);
        int emailMediatypeCode = MediaTypes.EMAIL.getMediaCode();
        EmailMediatypeForm emailMediatype = (EmailMediatypeForm)
                conversionService.convert(mailing.getMediatypes().get(emailMediatypeCode), MediatypeForm.class);
        if (emailMediatype != null) {
            emailMediatype.setTextTemplate(form.getEmailMediatype().getTextTemplate());
            emailMediatype.setHtmlTemplate(form.getEmailMediatype().getHtmlTemplate());
        }
        form.getMediatypes().put(emailMediatypeCode, emailMediatype);
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private void populateActiveMailingSettings(MailingSettingsForm form, Mailing mailing, Admin admin) {
        if (isMailingSentOrActive(mailing.getId(), admin)) {
            setMediatypesToForm(mailing, form);
            setSplitTargetToForm(form, mailing.getSplitID(), true);
            setDisabledSettingsToForm(form, mailing, admin);
        }
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
                .setNewSplitId(getTargetListSplitIdForSave(mailing, form))
                .setMailingParams(mailingParameterService.getMailingParameters(admin.getCompanyID(), mailingId))
                .setActiveOrSent(isMailingSentOrActive(mailingId, admin))
                .setWorkflowParams(WorkflowParametersHelper.find(req))
                .setSessionId(req.getSession(false).getId())
                .build();
    }

    private int getTargetListSplitIdForSave(Mailing mailing, MailingSettingsForm form) {
        int splitId = targetService.getTargetListSplitId(form.getSplitBase(), form.getSplitPart(), isWmSplit(mailing.getSplitID()));
        if (splitId == Mailing.YES_SPLIT_ID) { //-1 Should not be saved to DB
            splitId = Mailing.NONE_SPLIT_ID;
        }
        return splitId;
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

    private void validateSavedMailing(Mailing mailing, Admin admin, Popups popups) {
        if (isRedesign(admin)) {
            mailingValidator.validateMailingModules(mailing, popups);
        } else {
            validateMailingModules(mailing, popups);
        }
    }
    
    private boolean trySaveMailing(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, HttpServletRequest req, Popups popups) {
        int mailingId = mailing.getId();
        try {
            if (options.isActiveOrSent()) {
                mailingService.saveMailingDescriptiveData(mailing);
            } else {
                saveMailing(mailing, form, options, req, popups);
                previewImageService.generateMailingPreview(AgnUtils.getAdmin(req), req.getSession(false).getId(), mailingId, true);
            }

            updateMailingIconInRelatedWorkflowIfNeeded(options.isNew(), options.getWorkflowId(), mailing, req);
        } catch (AgnTagException e) {
            req.setAttribute("errorReport", e.getReport());
            popups.alert(TEMPLATE_DYNTAGS_ERROR_KEY);
        } catch (TooManyTargetGroupsInMailingException e) {
            logInfoException(String.format("Too many target groups for mailing %d", mailingId), e);
            popups.alert("error.mailing.tooManyTargetGroups");
        } catch (Exception e) {
            logInfoException(String.format("Error occurred: %s", e.getMessage()), e);
            popups.alert(ERROR_MSG);
        }
        return !popups.hasAlertPopups();
    }

    private void logInfoException(String message, Throwable e) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(message, e);
        }
    }

    // Redesigned in MailingValidatorImpl.java TODO remove when EMMGUI-714 migration finished
    /**
     * Validate agn-tags and dyn-tags.
     */
    private void validateMailingModules(Mailing mailing, Popups popups) {
        try {
            validateMailingModule(mailing.getTextTemplate());
            validateMailingModule(mailing.getHtmlTemplate());
        } catch (DynTagException e) {
            logInfoException("General error in tag", e);
            popups.alert("error.template.dyntags.general_tag_error", e.getLineNumber(), e.getTag());
        }
    }

    // Redesigned in MailingValidatorImpl.java TODO remove when EMMGUI-714 migration finished
    private void validateMailingModule(MailingComponent template) throws DynTagException {
        if (template != null) {
            agnTagService.getDynTags(template.getEmmBlock(), agnDynTagGroupResolverFactory.create(template.getCompanyID(), template.getMailingID()));
        }
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    /**
     * Saves current mailing in DB (including mailing components, content blocks, dynamic tags, dynamic tags contents
     * and trackable links)
     *
     * @throws Exception if anything went wrong
     */
    protected void saveMailing(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, HttpServletRequest req, Popups popups) throws Exception {
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);

        validateMailingBeforeSave(mailing, req, popups);
        boolean approved = mailingService.isApproved(mailing.getId(), mailing.getCompanyID());

        mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
        mailingService.saveMailingWithNewContent(mailing, admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
        if (options.getGridTemplateId() > 0) {
            saveMailingGridInfo(options.getGridTemplateId(), mailing.getId(), admin);
        }
        updateMailingParams(mailing, admin);
        if (mailing.isIsTemplate()) {
            mailingService.updateMailingsWithDynamicTemplate(mailing, applicationContext);
        }

        if (approved && !options.isNew()) {
            mailingService.writeRemoveApprovalLog(mailing.getId(), admin);
        }
    }

    // Redesigned in MailingValidatorImpl.java TODO remove when EMMGUI-714 migration finished
    private void validateMailingBeforeSave(Mailing mailing, HttpServletRequest req, Popups popups) throws Exception {
        validateMailingTagsAndComponents(mailing, req, popups);
        
        String rdirDomain = linkService.getRdirDomain(mailing.getCompanyID());

        mailing.getComponents().forEach((name, component) -> {
            if (component.getEmmBlock() != null) {
                String text = AgnTagUtils.unescapeAgnTags(component.getEmmBlock());
                component.setEmmBlock(text, component.getMimeType());

                Integer rdirLinkLine = linkService.getLineNumberOfFirstRdirLink(rdirDomain, text);
                if (rdirLinkLine != null) {
                    popups.warning("warning.mailing.link.encoded", name, rdirLinkLine);
                }
            }
        });
        ComMailingContentChecker.checkHtmlWarningConditions(mailing, popups);
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
            return "edit list split changed to " + getFormattedSplitString(form.getSplitBase()) + " part #" + form.getSplitPart() + "; ";
        }
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private List<ComMailingParameter> collectMailingParams(MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        List<ComMailingParameter> params = options.getMailingParams();
        // Let's retrieve all the parameters currently stored.
        if (isMailingRequiresOriginParams(form, admin, options)) {
            return params;
        }
        // Overwrite all the parameters with the user-defined ones if user is permitted to change parameters.
        List<ComMailingParameter> intervalParams = retrieveReservedParams(params);
        params = form.getParams().stream()
                .filter(param -> isNotEmpty(param.getName()))
                .collect(Collectors.toList());
        params.addAll(intervalParams);
        return params;
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    protected boolean isMailingRequiresOriginParams(MailingSettingsForm form, Admin admin, MailingSettingsOptions opts) {
        return !admin.permissionAllowed(Permission.MAILING_PARAMETER_CHANGE);
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private List<ComMailingParameter> retrieveReservedParams(List<ComMailingParameter> params) {
        return params.stream()
                .filter(p -> isReservedParam(p.getName()))
                .collect(Collectors.toList());
    }
    
    private boolean tryPrepareMailingForSave(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, HttpServletRequest req, Popups popups) {
        try {
            prepareMailingForSave(mailing, form, options, req, popups);
        } catch (UserMessageException e) {
            popups.alert(e.getErrorMessageKey(), e.getAdditionalErrorData());
        } catch (ParseException e) {
            popups.alert("error.mailing.wrong.plan.date.format");
        } catch (MissingEndTagException e) {
            logInfoException("Missing end tag", e);
            popups.alert("error.template.dyntags.missing_end_tag", e.getLineNumber(), e.getTag());
        } catch (UnclosedTagException e) {
            logInfoException("Unclosed tag", e);
            popups.alert("error.template.dyntags.unclosed_tag", e.getTag());
        } catch (Exception e) {
            LOGGER.error(String.format("Error in save mailing id: %d", mailing.getId()), e);
            popups.alert("error.mailing.content");
        }
        return !popups.hasAlertPopups();
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    private void prepareMailingForSave(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, HttpServletRequest req, Popups popups) throws Exception {
        Admin admin = AgnUtils.getAdmin(req);
        if (!options.isActiveOrSent()) {
            setMailingPropertiesFromForm(mailing, form, options, req);

            mailing.setParameters(collectMailingParams(form, admin, options));
            List<String> dynNamesForDeletion = new ArrayList<>();
            mailing.buildDependencies(popups, true, dynNamesForDeletion, applicationContext, admin);
            dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);
            if (form.getEmailMediatype().isActive()) {
                mailingBaseService.doTextTemplateFilling(mailing, admin, popups);
            }
        } else if (isFormContainsOnlyAlwaysAllowedChanges(mailing, form, options, popups)) {
            String shortname = form.getShortname();
            String description = form.getDescription();
            int archiveId = form.getArchiveId();
            boolean isArchived = form.isArchived();

            form = mailingToForm(mailing, admin);

            form.setShortname(shortname);
            form.setDescription(description);
            form.setArchiveId(archiveId);
            form.setArchived(isArchived);
            setMailingPropertiesFromForm(mailing, form, options, req);
        }
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    protected void setMailingPropertiesFromForm(Mailing mailing, MailingSettingsForm form,
                                                MailingSettingsOptions options,
                                                HttpServletRequest req) throws Exception {
        Admin admin = AgnUtils.getAdmin(req);
        mailing.setSplitID(options.getNewSplitId());
        mailing.setIsTemplate(options.isTemplate());
        mailing.setCampaignID(form.getArchiveId());
        mailing.setDescription(form.getDescription());
        mailing.setShortname(form.getShortname());
        mailing.setMailinglistID(form.getMailinglistId());
        mailing.setMailingType(form.getMailingType());
        mailing.setPlanDate(getMailingPlanDateFromForm(form, admin));
        mailing.setArchived(form.isArchived() ? 1 : 0);
        mailing.setTargetExpression(generateMailingTargetExpression(mailing, form, admin, options));
        mailing.setMailingContentType(form.getMailingContentType());
        mailing.setLocked(1);
        mailing.setNeedsTarget(form.isNeedsTarget());
        mailing.setUseDynamicTemplate(form.isUseDynamicTemplate());
        setMediatypesToMailing(mailing, options, form);
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    protected void setMediatypesToMailing(Mailing mailing, MailingSettingsOptions options, MailingSettingsForm form) throws Exception {
        boolean requestApproval = mailing.getEmailParam().isRequestApproval();
        String approvedBy = mailing.getEmailParam().getApprovedBy();
        mailing.setMediatypes(form.getMediatypes().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                mt -> conversionService.convert(mt.getValue(), Mediatype.class))));
        MediatypeEmail mailingEmailMediatype = mailing.getEmailParam();
        EmailMediatypeForm formEmailMediatype = form.getEmailMediatype();
        mailingEmailMediatype.setLinefeed(formEmailMediatype.getLinefeed());
        mailingEmailMediatype.setCharset(formEmailMediatype.getCharset());
        mailingEmailMediatype.setOnepixel(formEmailMediatype.getOnepixel());
        mailingEmailMediatype.setRequestApproval(requestApproval);
        mailingEmailMediatype.setApprovedBy(approvedBy);
        setMailingFollowupProperties(mailing, form);
        if (mailing.isGridMailing()) {
            mailingEmailMediatype.setStatus(MediaTypeStatus.Active.getCode());
        }
        if (mailing.getMailingType() != MailingType.DATE_BASED) {
            mailingEmailMediatype.deleteDateBasedParameters();
        }
        for (Mediatype mediatype : mailing.getMediatypes().values()) {
            if (mediatype != null && mediatype.getStatus() == MediaTypeStatus.Active.getCode()) {
                mediatype.syncTemplate(mailing, applicationContext);
            }
        }
    }

    private Date getMailingPlanDateFromForm(MailingSettingsForm form, Admin admin) throws ParseException {
        if (StringUtils.isBlank(form.getPlanDate())) {
            return null;
        }
        String formPlanDate = StringUtils.trimToEmpty(form.getPlanDate());
        if (formPlanDate.split(" ").length == 2) { // date and time is set
            Date planDate = admin.getDateTimeFormat().parse(formPlanDate);
            if ("00:00".equals(formPlanDate.split(" ")[1])) {
                return DateUtils.addSeconds(planDate, 1); // add a second to indicate in db DATE column that the time was not left empty and user set 00:00 by intention
            }
            return planDate;
        }
        return admin.getDateFormat().parse(formPlanDate); // date is set, time is not
    }

    protected void setMailingFollowupProperties(Mailing mailing, MailingSettingsForm form) {
        // Do nothing.
    }

    @ModelAttribute("mailingSettingsForm")
    public MailingSettingsForm getSettingsForm() {
        return getNewSettingsForm();
    }

    protected MailingSettingsForm getNewSettingsForm() {
        return new MailingSettingsForm();
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    // overridden in extended class
    protected String generateMailingTargetExpression(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        // Only change target expressions, that are NOT complex and not managed by workflow manager
        if (isTargetExpressionComplex(mailing) || !form.isAssignTargetGroups()) {
            return mailing.getTargetExpression();
        }
        boolean useOperatorAnd = useOperatorAndForRegularTargetsPart(mailing, form, options);
        return isBlank(form.getTargetExpression())
                ? TargetExpressionUtils.makeTargetExpression(form.getTargetGroupIds(), useOperatorAnd)
                : form.getTargetExpression();
    }

    protected boolean useOperatorAndForRegularTargetsPart(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options) {
        if (isTargetModeCheckboxVisible(mailing, isTargetExpressionComplex(mailing), options)
                && !isTargetModeCheckboxDisabled(options)) {
            return isFormTargetsHaveConjunction(form);
        }
        // Use currently set target group operator
        return StringUtils.isBlank(mailing.getTargetExpression()) ||
                !TargetExpressionUtils.extractNotAltgTargetExpressionPart(mailing.getTargetExpression(), targetService)
                        .contains(TargetExpressionUtils.OPERATOR_OR);
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

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private void updateMailingIconInRelatedWorkflowIfNeeded(boolean isNewMailing, int workflowId, Mailing mailing, HttpServletRequest req) {
        if (!workflowDriven(workflowId)) {
            return;
        }

        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);
        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        Optional<WorkflowMailingAwareImpl> icon;
        if (isNewMailing) {
            icon = findMailingIconInRelatedWorkflow(workflow, 0, WorkflowParametersHelper.defaultIfEmpty(req, workflowId).getNodeId());
        } else {
            icon = findMailingIconInRelatedWorkflow(workflow, mailing.getId(), null);
        }

        icon.ifPresent(i -> {
            i.setMailingId(mailing.getId());
            i.setIconTitle(mailing.getShortname());
            i.setFilled(true);
        });

        workflowService.saveWorkflow(admin, workflow, workflow.getWorkflowIcons());
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private Optional<WorkflowMailingAwareImpl> findMailingIconInRelatedWorkflow(Workflow workflow, int mailingId, Integer nodeId) {
        return workflow.getWorkflowIcons().stream()
                .filter(i -> isAllowedMailingIconTypeForFill(i.getType()) && i instanceof WorkflowMailingAwareImpl)
                .filter(i -> nodeId == null || i.getId() == nodeId)
                .map(i -> (WorkflowMailingAwareImpl) i)
                .filter(i -> i.getMailingId() == mailingId)
                .findFirst();
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    protected boolean isAllowedMailingIconTypeForFill(int type) {
        return type == WorkflowIconType.MAILING.getId();
    }

    @Override
   	public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return EXCLUDED_FROM_UNSAFE_TAG_CHECK_PARAMS.contains(param);
   	}

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private boolean isFormContainsOnlyAlwaysAllowedChanges(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, Popups popups) {
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(options.getMailingId());
        String textTemplateText = form.getEmailMediatype().getTextTemplate();
        String htmlTemplateText = form.getEmailMediatype().getHtmlTemplate();

        if (options.getCompanyId() == mailing.getCompanyID()
                && options.getMailingId() == mailing.getId()
                && StringUtils.equals(textTemplateText, mailing.getTextTemplate().getEmmBlock())
                && ((gridTemplateId > 0 || mailing.getEmailParam().getMailFormat() == 0) || StringUtils.equals(htmlTemplateText, mailing.getHtmlTemplate().getEmmBlock()))) {
            return true;
        }

        MailingStatus mailingStatus = mailingService.getMailingStatus(mailing.getCompanyID(), mailing.getId());
        popups.alert(mailingStatus == MailingStatus.SENT ? "error.sent.mailing.change.denied" : "status_changed");
        return false;
    }

    private boolean isMailingSentOrActive(int mailingId, Admin admin) {
        return maildropService.isActiveMailing(mailingId, admin.getCompanyID());
    }

    private boolean isMailingEditable(Mailing mailing, Admin admin, MailingSettingsOptions options) {
        return options.isForCopy()
                || options.isForFollowUp()
                || !isMailingSentOrActive(mailing.getId(), admin);
    }

    protected boolean isTargetModeCheckboxDisabled(MailingSettingsOptions options) {
        return (workflowDriven(options.getWorkflowId()) || options.isWorldSend())
                && !(options.isForCopy() || options.isForFollowUp());
    }

    protected boolean isTargetModeCheckboxVisible(Mailing mailing, boolean isTargetExpressionComplex, MailingSettingsOptions options) {
        return !(isTargetExpressionComplex ||
                ((workflowDriven(options.getWorkflowId()) || options.isWorldSend())
                        && CollectionUtils.size(mailing.getTargetGroups()) < 2));
    }

    protected boolean workflowDriven(int workflowId) {
        return workflowId > 0;
    }

    private String getTypeForChangelog(boolean isTemplate) {
        return isTemplate ? "template" : "mailing";
    }

    @GetMapping("/templates.action")
    public String templates(@RequestParam(required = false) Integer mediaType, Model model, Admin admin, HttpServletRequest req) {
        List<MailingBase> templates = mailingService.getTemplatesWithPreview(MediaTypes.getMediaTypeForCode(mediaType), admin);
        if (templates.isEmpty() && admin.isRedesignedUiUsed()) {
            return String.format("redirect:/mailing/new.action?mediaType=%d", mediaType == null ? MediaTypes.EMAIL.getMediaCode() : mediaType);
        }

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        model.addAttribute("templateMailingBases", templates);
        model.addAttribute("mediaType", mediaType);

        updateForwardParameters(req, true);
        return "mailing_templates";
    }

    // Redesigned in MailingSettingsServiceImpl.java. TODO remove when EMMGUI-714 migration finished
    protected void populateWorkflowDrivenSettings(MailingSettingsForm form, Mailing mailing, HttpServletRequest req) {
        Admin admin = AgnUtils.getAdmin(req);
        assert (admin != null);
        WorkflowParameters params = WorkflowParametersHelper.find(req);
        if (params != null) {
            workflowService.assignWorkflowDrivenSettings(admin, mailing, params.getWorkflowId(), params.getNodeId());
            if (StringUtils.isNotBlank(params.getParamsAsMap().get("mailingType"))) {
                mailing.setMailingType(getMailingTypeFromForwardParams(params));
            }

            populateWorkflowMediaTypesSettings(form, mailing, params);
            setDisabledSettingsToForm(form, mailing, admin);
            form.setArchiveId(mailing.getCampaignID());
            form.setArchived(mailing.getArchived() == 1);
        }
    }

    protected void populateWorkflowMediaTypesSettings(MailingSettingsForm form, Mailing mailing, WorkflowParameters params) {
        // nothing to do
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private void setDisabledSettingsToForm(MailingSettingsForm form, Mailing mailing, Admin admin) {
        form.setMailinglistId(mailing.getMailinglistID());
        setMailingTargetsToForm(form, mailing);
        form.setTargetExpression(mailing.getTargetExpression());
        if (mailing.getPlanDate() != null) {
            SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
            form.setPlanDate(dateTimeFormat.format(mailing.getPlanDate()));
        }
        form.setMailingType(mailing.getMailingType());
    }

    // Redesigned in MailingSettingsServiceImpl.java TODO remove when EMMGUI-714 migration finished
    private MailingType getMailingTypeFromForwardParams(WorkflowParameters params) {
        try {
            String mailingTypeStr = params.getParamsAsMap().get("mailingType");
            return MailingType.fromCode(NumberUtils.toInt(mailingTypeStr, 0));
        } catch (Exception e) {
            return MailingType.NORMAL;
        }
    }

    @GetMapping("/new.action")
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
            if (isRedesign(admin)) {
                Integer workflowId = getWorkflowIdFromReq(req);
                mailingSettingsService.copyTemplateSettingsToMailingForm(mailingToCreate, form, workflowId, isTemplate);    
            } else {
                copyTemplateSettingsToMailingForm(mailingToCreate, form, req, isTemplate);
            }
            mailingToCreate.setId(0);
        } else {
            if (isRedesign(admin)) {
                form.getMediatypeForm(MediaTypes.getMediaTypeForCode(mediaType))
                        .orElse(form.getEmailMediatype())
                        .setActive(true);
            } else {
                form.getEmailMediatype().setActive(true);
            }
        }
        if (isRedesign(admin)) {
            mailingSettingsService.populateWorkflowDrivenSettings(form, mailingToCreate, admin,WorkflowParametersHelper.find(req));
        } else {
            populateWorkflowDrivenSettings(form, mailingToCreate, req);
        }

        MailingSettingsOptions options = getOptionsBuilderForView(req, false, false, mailingToCreate)
            .setIsTemplate(isTemplate).build();
        prepareMailingView(mailingToCreate, model, form, false, options, req, popups);
        model.addAttribute(MAILING_ID_ATTR, 0);
        model.addAttribute(IS_TEMPLATE_ATTR, isTemplate);
        return SETTINGS_VIEW;
    }

    private static Integer getWorkflowIdFromReq(HttpServletRequest req) {
        return (Integer) req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
    }

    @GetMapping("/{mailingId:\\d+}/actions.action")
    public String actions(@PathVariable int mailingId, Admin admin, Model model) {
        if (!isRedesign(admin)) {
            model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));
        }
        model.addAttribute("actions", mailingService.listTriggers(mailingId, admin.getCompanyID()));
        model.addAttribute("mailingShortname", mailingService.getMailingName(mailingId, admin.getCompanyID()));
        return "mailing_actions";
    }

    // TODO: check usage and remove after EMMGUI-714 will be finished and old design will be removed
    @GetMapping("/create.action")
    public String create(Admin admin, HttpServletRequest req, Model model) {
        updateForwardParameters(req, true);
        List<MailingCreationOption> allowedOptions = Arrays.stream(MailingCreationOption.values())
                .filter(option -> admin.permissionAllowed(option.getRequiredPermission()))
                .collect(Collectors.toList());
        if (allowedOptions.size() == 1) {
            return autoSelectOptionNew(allowedOptions.get(0), req);
        }
        addAttrsForCreationPage(model, admin);
        return "mailing_create_start";
    }

    protected void addAttrsForCreationPage(Model model, Admin admin) {
        // empty for OpenEMM
    }

    private String autoSelectOptionNew(MailingCreationOption optionToSelect, HttpServletRequest req) {
        String redirectionUrl = optionToSelect.getRedirectionUrl();
        boolean keepForward = false;
        if (optionToSelect.isNeedKeepForward()) {
            String workflowIdParam = req.getParameter(WorkflowParametersHelper.WORKFLOW_ID);
            if (StringUtils.isNoneBlank(workflowIdParam)) {
                keepForward = Integer.parseInt(workflowIdParam) > 0;
            }
        }
        return keepForward ? "forward:" + redirectionUrl : "redirect:" + redirectionUrl;
    }

    @PostMapping("/{mailingId:\\d+}/frame.action")
    public String saveFrameContent(@PathVariable int mailingId, FrameContentForm form, Admin admin,
                                   HttpSession session, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, mailingId)) {
            throw new UnsupportedOperationException();
        }

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        if (!mailingSettingsService.saveFrameContent(mailing, form, admin, session.getId(), popups)) {
            return MESSAGES_VIEW;
        }
        validateSavedMailing(mailing, admin, popups);
        popups.success(CHANGES_SAVED_MSG);
        return "redirect:/mailing/content/" + mailingId + "/view.action";
    }
    
    private void writeUserActivityLog(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, LOGGER);
    }

    private void writeUserActivityLog(Admin admin, UserAction ua) {
        UserActivityUtil.log(userActivityLogService, admin, ua, LOGGER);
    }
}
