/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import static com.agnitas.beans.BaseTrackableLink.KEEP_UNCHANGED;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BaseTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.trackablelinks.common.LinkTrackingMode;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm;
import com.agnitas.emm.core.trackablelinks.form.TrackableLinksForm;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.LinkcheckService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.exception.ClearLinkExtensionsException;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.agnitas.web.perm.exceptions.InsufficientPermissionException;
import jakarta.servlet.http.HttpServletRequest;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mailing/{mailingId:\\d+}/trackablelink")
@RequiredPermission("mailing.content.show")
public class MailingTrackableLinkController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingTrackableLinkController.class);

    private static final String CHANGE_MSG = "%s changed from %s to %s. ";
    private static final String REDIRECT_TO_LIST_STR = "redirect:/mailing/%d/trackablelink/list.action?restoreSort=true";
    private static final String NOT_FORM_ACTIONS_ATTR = "notFormActions";
    private static final String EDIT_MESSAGE = "edit mailing links";

    private final MailingBaseService mailingBaseService;
    private final MailingService mailingService;
    private final GridServiceWrapper gridService;
    private final ConfigService configService;
    private final LinkService linkService;
    private final MailingDao mailingDao;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final EmmActionService actionService;
    private final TrackableLinkService trackableLinkService;
    private final ExtendedConversionService conversionService;
    private final LinkcheckService linkcheckService;
    private final MaildropService maildropService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final ApplicationContext applicationContext;

    public MailingTrackableLinkController(UserActivityLogService userActivityLogService, TrackableLinkService trackableLinkService,
                                          ExtendedConversionService conversionService, MailingBaseService mailingBaseService, MailingService mailingService,
                                          EmmActionService actionService, GridServiceWrapper gridService, ConfigService configService,
                                          LinkService linkService, MailingDao mailingDao, WebStorage webStorage, LinkcheckService linkcheckService,
                                          MaildropService maildropService, MailinglistApprovalService mailinglistApprovalService, ApplicationContext applicationContext) {
        this.mailingBaseService = mailingBaseService;
        this.mailingService = mailingService;
        this.gridService = gridService;
        this.configService = configService;
        this.linkService = linkService;
        this.mailingDao = mailingDao;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.actionService = actionService;
        this.trackableLinkService = trackableLinkService;
        this.conversionService = conversionService;
        this.linkcheckService = linkcheckService;
        this.maildropService = maildropService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.applicationContext = applicationContext;
    }

    @RequestMapping("/list.action")
    public String list(@PathVariable int mailingId, TrackableLinksForm form, Admin admin, Model model,
                       @RequestParam(required = false) Integer scrollToLinkId, @RequestParam(required = false) Boolean restoreSort) {
        syncListForm(form);
        FormUtils.updateSortingState(webStorage, WebStorage.TRACKABLE_LINKS, form, restoreSort);
        Mailing mailing = trackableLinkService.getMailingForLinksOverview(mailingId, admin.getCompanyID(), form.getIncludeDeleted());

        addMailingModelAttrs(mailing, model, admin);
        addLinkListModelAttrs(mailingId, model, admin, mailing, scrollToLinkId);
        setupFormGlobalSettings(form, mailing);
        setupList(form, mailing.getTrackableLinks().values(), model);

        writeUserActivityLog(admin, "trackable link list", "active tab - links");
        return "mailing_trackablelink_list";
    }

    private void addLinkListModelAttrs(int mailingId, Model model, Admin admin, Mailing mailing, Integer scrollToLinkId) {
        int companyId = admin.getCompanyID();

        model.addAttribute(NOT_FORM_ACTIONS_ATTR, actionService.getEmmNotFormActions(companyId, false));
        model.addAttribute("scrollToLinkId", scrollToLinkId == null ? 0 : scrollToLinkId);
        addDefaultExtensionsModelAttr(model, companyId);
        model.addAttribute("allLinksExtensions", conversionService.convert(mailing.getCommonLinkExtensions(), LinkProperty.class, ExtensionProperty.class));
        model.addAttribute("hasDefaultLinkExtension", StringUtils.isNotBlank(configService.getValue(ConfigValue.DefaultLinkExtension, companyId)));
        model.addAttribute("SHOW_CREATE_SUBSTITUTE_LINK", configService.getBooleanValue(ConfigValue.RedirectMakeAgnDynMultiLinksTrackable, companyId));
        model.addAttribute("isTrackingOnEveryPositionAvailable", trackableLinkService.isTrackingOnEveryPositionAvailable(companyId, mailingId));
        model.addAttribute("isAutoDeeptrackingEnabled", configService.isAutoDeeptracking(companyId));
        model.addAttribute("isSettingsReadonly", isSettingsReadonly(admin, mailingId));
    }

    private boolean isSettingsReadonly(Admin admin, int mailingId) {
        return mailingService.isSettingsReadonly(admin, mailingId);
    }

    private void addDefaultExtensionsModelAttr(Model model, int companyId) {
        model.addAttribute("defaultExtensions", conversionService.convert(
                linkService.getDefaultExtensions(companyId),
                LinkProperty.class, ExtensionProperty.class));
    }

    private void setupList(TrackableLinksForm form, Collection<TrackableLink> links, Model model) {
        Map<Integer, String> originalUrls = new HashMap<>();
        for (TrackableLink link : links) {
            originalUrls.put(link.getId(), link.getOriginalUrl());
        }
        model.addAttribute("originalUrls", originalUrls);

        int page = AgnUtils.getValidPageNumber(links.size(), form.getPage(), form.getNumberOfRows());
        List<TrackableLink> sortedLinks = links.stream()
                .sorted(getComparator(form))
                .skip((long) (page - 1) * form.getNumberOfRows())
                .limit(form.getNumberOfRows())
                .collect(Collectors.toList());
        List<TrackableLinkForm> linkForms = conversionService.convert(sortedLinks, TrackableLink.class, TrackableLinkForm.class);
        form.setLinks(linkForms);

        model.addAttribute("paginatedTrackableLinks", new PaginatedList<>(linkForms, links.size(), form.getNumberOfRows(), page, form.getSort(), form.getOrder()));
    }

    private void setupFormGlobalSettings(TrackableLinksForm form, Mailing mailing) {
        setIntelliAdSettingsToListForm(form, mailing);
        form.setOpenActionId(mailing.getOpenActionID());
        form.setClickActionId(mailing.getClickActionID());
    }

    private void setIntelliAdSettingsToListForm(TrackableLinksForm form, Mailing mailing) {
        Mediatype mediaType = mailing.getMediatypes().get(MediaTypes.EMAIL.getMediaCode());
        if (mediaType != null) {
            try {
                MediatypeEmail emailType = (MediatypeEmail) mediaType;
                form.setIntelliAdEnabled(emailType.isIntelliAdEnabled());
                form.setIntelliAdIdString(emailType.getIntelliAdString());
            } catch (ClassCastException e) {
                logger.warn("No EMM Mail?", e);
            }
        }
    }

    private void addMailingModelAttrs(Mailing mailing, Model model, Admin admin) {
        int mailingId = mailing.getId();
        model.addAttribute("isTemplate", mailing.isIsTemplate());
        model.addAttribute("mailingShortname", mailing.getShortname());
        model.addAttribute("gridTemplateId", gridService.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("isActiveMailing", maildropService.isActiveMailing(mailingId, admin.getCompanyID()));
        model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));

        if (mailingId > 0) {
            model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));
            model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, admin.getCompanyID()));
        } else {
            model.addAttribute("isMailingUndoAvailable", false);
            model.addAttribute("workflowId", 0);
        }
    }

    @PostMapping("/confirmBulkClearExtensions.action")
    public String confirmBulkClearExtensions(@PathVariable int mailingId, TrackableLinksForm form, Model model, Admin admin, Popups popups) {
        if (CollectionUtils.isEmpty(form.getBulkIds())) {
            popups.alert("error.workflow.noLinkSelected");
            return MESSAGES_VIEW;
        }
        Set<LinkProperty> extensionsToDelete = getExtensionsToDelete(new HashSet<>(form.getBulkIds()), admin.getCompanyID());
        if (CollectionUtils.isEmpty(extensionsToDelete)) {
            popups.alert("error.mailing.trackablelinks.extension.empty");
            return MESSAGES_VIEW;
        }
        model.addAttribute("extensionsToDelete", extensionsToDelete);
        return "mailing_trackablelink_bulk_clear_extensions";
    }

    @PostMapping("/bulkClearExtensions.action")
    public String bulkClearExtensions(@PathVariable int mailingId, BulkActionForm form, Admin admin, Popups popups) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        try {
            trackableLinkService.bulkClearExtensions(mailingId, admin.getCompanyID(), new HashSet<>(form.getBulkIds()));
            writeUserActivityLog(admin, "edit mailing", "ID = " + mailingId + ". Removed global and individual link extensions");
            popups.changesSaved();
            return String.format(REDIRECT_TO_LIST_STR, mailingId);
        } catch (ClearLinkExtensionsException e) {
            logger.error("Error removing global and individual link extensions (mailing ID: {})", mailingId, e);
            popups.alert("error.trackablelinks.extensions.remove");
            return MESSAGES_VIEW;
        }
    }

    @PostMapping("/check.action")
    @RequiredPermission("mailing.send.show")
    public String checkLinks(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());

        mailing.buildDependencies(popups, false, null, applicationContext, admin.getLocale());
        popups.addPopups(linkcheckService.checkForUnreachableLinks(mailing));

        if (!popups.hasAlertPopups()) {
            popups.success("link.check.success");
        }

        return MESSAGES_VIEW;
    }

    @RequestMapping("/bulkActionsView.action")
    public String bulkActionsView(@PathVariable int mailingId, TrackableLinksForm form, Model model, Admin admin, Popups popups) {
        HashSet<Integer> bulkIds = new HashSet<>(form.getBulkIds());
        if (CollectionUtils.isEmpty(form.getBulkIds())) {
            popups.alert("error.workflow.noLinkSelected");
            return MESSAGES_VIEW;
        }
        model.addAttribute(NOT_FORM_ACTIONS_ATTR, actionService.getEmmNotFormActions(admin.getCompanyID(), false));
        model.addAttribute("commonExtensions", conversionService.convert(trackableLinkService.getCommonExtensions(mailingId, admin.getCompanyID(), bulkIds), LinkProperty.class, ExtensionProperty.class));
        return "mailing_trackablelink_bulk_actions";
    }

    private Set<LinkProperty> getExtensionsToDelete(final Set<Integer> bulkIDs, final int companyId) {
        return bulkIDs.stream()
                .map(id -> trackableLinkService.getTrackableLinkSettings(id, companyId).getLinkProperties())
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @PostMapping("/activateTrackingLinksOnEveryPosition.action")
    public String activateTrackingLinksOnEveryPosition(@PathVariable int mailingId, Admin admin, Popups popups, HttpServletRequest req) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        try {
            Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
            mailingBaseService.activateTrackingLinksOnEveryPosition(mailing, getApplicationContext(req));
            mailingBaseService.saveMailingWithUndo(mailing, admin.getAdminID(), false);
        } catch (Exception e) {
            popups.defaultError();
            return MESSAGES_VIEW;
        }
        popups.changesSaved();
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @PostMapping("/deactivateTrackingLinksOnEveryPosition.action")
    public String deactivateTrackingLinksOnEveryPosition(@PathVariable int mailingId, Admin admin, Popups popups, HttpServletRequest req) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        mailingBaseService.deactivateTrackingLinksOnEveryPosition(mailing, getApplicationContext(req));
        mailingBaseService.saveMailingWithUndo(mailing, admin.getAdminID(), false);

        popups.changesSaved();

        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @PostMapping("/saveAll.action")
    public String saveAll(@PathVariable int mailingId, TrackableLinksForm form, Admin admin, Popups popups) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        if (!intelliAdSettingsValid(form, popups)) {
            return MESSAGES_VIEW;
        }
        saveAll(mailingId, form, admin);
        popups.changesSaved();
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private void saveAll(int mailingId, TrackableLinksForm form, Admin admin) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        Collection<TrackableLink> links = mailing.getTrackableLinks().values();

        updateGlobalSettings(form, mailing, admin);
        updateIndividualLinks(links, form, admin);
        updateBulkLinks(links, form, mailing, admin);
        mailingDao.saveMailing(mailing, false);
    }

    private void updateGlobalSettings(TrackableLinksForm form, Mailing mailing, Admin admin) {
        writeCommonActionChanges(mailing, form, admin);
        mailing.setOpenActionID(form.getOpenActionId());
        mailing.setClickActionID(form.getClickActionId());
        saveIntelliAdSettings(form, mailing);

        if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS) && form.isModifyAllLinksExtensions()) {
            updateGlobalLinksExtensions(form, mailing, admin);
        }
    }

    private void updateGlobalLinksExtensions(TrackableLinksForm form, Mailing mailing, Admin admin) {
        List<UserAction> userActions = new ArrayList<>();
        List<LinkProperty> extensions = conversionService.convert(form.getExtensions(), ExtensionProperty.class, LinkProperty.class);
        Set<Integer> linkIds = mailing.getTrackableLinks().values().stream().map(BaseTrackableLink::getId).collect(Collectors.toSet());
        trackableLinkService.addExtensions(mailing, linkIds, extensions, userActions);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
    }

    private void updateIndividualLinks(Collection<TrackableLink> links, TrackableLinksForm linksForm, Admin admin) {
        Map<Integer, TrackableLink> existLinks = links.stream()
                .collect(Collectors.toMap(BaseTrackableLink::getId, Function.identity()));

        linksForm.getLinks().stream()
                .filter(form -> existLinks.get(form.getId()) != null && !existLinks.get(form.getId()).isDeleted())
                .forEach(form -> updateIndividualLink(existLinks.get(form.getId()), form, linksForm.getBulkIds(), admin));
    }

    private void updateBulkLinks(Collection<TrackableLink> links, TrackableLinksForm form, Mailing mailing, Admin admin) {
        links.stream()
                .filter(link -> form.getBulkIds().contains(link.getId()))
                .forEach(link -> logLinkEdited(admin, updateBulkLinkAndGetLog(link, form, admin.getCompanyID()), link));

        if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS) && form.isModifyBulkLinksExtensions()) {
            updateBulkLinksExtensions(form, mailing, admin);
        }
    }

    private void updateBulkLinksExtensions(TrackableLinksForm form, Mailing mailing, Admin admin) {
        List<UserAction> userActions = new ArrayList<>();
        List<LinkProperty> extensions = conversionService.convert(form.getExtensions(), ExtensionProperty.class, LinkProperty.class);
        Set<Integer> linkIds = new HashSet<>(form.getBulkIds());
        trackableLinkService.addExtensions(mailing, linkIds, extensions, userActions);
        trackableLinkService.removeLegacyMailingLinkExtension(mailing, linkIds);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
    }

    private void saveIntelliAdSettings(TrackableLinksForm form, Mailing mailing) {
        try {
            MediatypeEmail mediatype = (MediatypeEmail) mailing.getMediatypes().get(MediaTypes.EMAIL.getMediaCode());
            if (mediatype != null) {
                mediatype.setIntelliAdEnabled(form.isIntelliAdEnabled());
                if (form.isIntelliAdEnabled()) {
                    mediatype.setIntelliAdString(form.getIntelliAdIdString());
                }
            }
        } catch (ClassCastException e) {
            logger.warn("No EMM email?", e);
        }
    }

    @GetMapping("/{linkId:\\d+}/view.action")
    public String view(@PathVariable int mailingId, @PathVariable int linkId, Admin admin, Model model, Popups popups) {
        int companyId = admin.getCompanyID();
        TrackableLink link = trackableLinkService.getTrackableLink(companyId, linkId);
        if (link == null) {
            popups.defaultError();
            logger.error("could not load link: {}", linkId);
            return MESSAGES_VIEW;
        }
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        addMailingModelAttrs(mailing, model, admin);
        addLinkViewModelAttrs(mailingId, admin, model, companyId, link);
        writeUserActivityLog(admin, "trackable link list", "active tab - links");
        return "mailing_trackablelink_view";
    }

    private void addLinkViewModelAttrs(int mailingId, Admin admin, Model model, int companyId, TrackableLink link) {
        model.addAttribute("trackableLinkForm", conversionService.convert(link, TrackableLinkForm.class));
        model.addAttribute("altText", link.getAltText());
        model.addAttribute("originalUrl", link.getOriginalUrl());
        model.addAttribute(NOT_FORM_ACTIONS_ATTR, actionService.getEmmNotFormActions(companyId, false));
        model.addAttribute("isUrlEditingAllowed", trackableLinkService.isUrlEditingAllowed(admin, mailingId));
        model.addAttribute("SHOW_CREATE_SUBSTITUTE_LINK", configService.getBooleanValue(ConfigValue.RedirectMakeAgnDynMultiLinksTrackable, companyId));
        model.addAttribute("isSettingsReadonly", isSettingsReadonly(admin, mailingId));
        addDefaultExtensionsModelAttr(model, companyId);
    }

    @PostMapping("/{linkId:\\d+}/save.action")
    public String save(@PathVariable int mailingId, @PathVariable int linkId, TrackableLinkForm form, Admin admin, Popups popups, HttpServletRequest req, RedirectAttributes redirectAttrs) throws TrackableLinkException {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }

        TrackableLink link = trackableLinkService.getTrackableLink(admin.getCompanyID(), linkId);
        if (link == null) {
            popups.defaultError();
            return MESSAGES_VIEW;
        }
        if (StringUtils.isNotBlank(form.getUrl()) && !tryUpdateLinkUrl(link, form.getUrl(), admin)) {
            popups.alert("error.permissionDenied");
            return MESSAGES_VIEW;
        }

        saveLink(link, form, admin, req.getParameter("deepTracking") != null);
        redirectAttrs.addAttribute("scrollToLinkId", linkId);
        popups.changesSaved();
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private boolean tryUpdateLinkUrl(final TrackableLink link, String newUrl, Admin admin) throws TrackableLinkException {
        try {
            updateLinkUrl(link, newUrl, admin);
            return true;
        } catch (InsufficientPermissionException e) {
            return false;
        }
    }

    /**
     * Update link target including permission checks, ...
     *
     * @param link trackable link
     * @param newUrl URL to be updated
     * @param admin  current admin
     * @throws InsufficientPermissionException if user does not have sufficient permissions to change link target
     * @throws TrackableLinkException          on errors updating link target
     */
    private void updateLinkUrl(final TrackableLink link, String newUrl, Admin admin) throws InsufficientPermissionException, TrackableLinkException {
        if (!newUrl.equals(link.getFullUrl())) {
            if (admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_URL_CHANGE)) {
                trackableLinkService.updateLinkTarget(link, newUrl);

                writeUserActivityLog(admin, "edit link target", "Set target of link " + link.getId() + " to " + newUrl);
            } else {
                logger.warn("Admin {} (ID {}) has no permission to edit link URLs", admin.getUsername(), admin.getAdminID());
                throw new InsufficientPermissionException(admin.getAdminID(), "mailing.trackablelinks.url.change");
            }
        }
    }

    private WebApplicationContext getApplicationContext(HttpServletRequest req) {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(req
                .getSession().getServletContext());
    }

    private void writeCommonActionChanges(Mailing mailing, TrackableLinksForm form, Admin admin) {
        int mailingId = mailing.getId();
        writeCommonActionChangeLog("Open", mailingId, admin, mailing.getOpenActionID(), form.getOpenActionId());
        writeCommonActionChangeLog("Click", mailingId, admin, mailing.getClickActionID(), form.getClickActionId());
        if (logger.isInfoEnabled()) {
            logger.info("save Trackable links Open/Click Actions, mailing ID = {}", mailingId);
        }
    }

    private void saveLink(TrackableLink link, TrackableLinkForm form, Admin admin, boolean includeDeepTracking) {
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append(updateLinkDescription(link, form.getShortname()));
        logBuilder.append(updateLinkUsage(link, form.getUsage()));
        logBuilder.append(updateLinkAdmin(link, form.isAdmin()));
        logBuilder.append(updateLinkAction(link, form.getAction(), admin.getCompanyID()));
        logBuilder.append(updateLinkCreateSubstituteLink(link, form.isCreateSubstituteForAgnDynMulti()));
        logBuilder.append(updateLinkStatic(link, form.isStaticLink()));
        if (includeDeepTracking) {
            logBuilder.append(updateLinkDeepTracking(link, form.getDeepTracking()));
        }

        logLinkEdited(admin, logBuilder, link);

        if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
            List<LinkProperty> newProperties = conversionService.convert(form.getExtensions(), ExtensionProperty.class, LinkProperty.class)
                    .stream().filter(extension -> StringUtils.isNotBlank(extension.getPropertyName())).collect(Collectors.toList());
            writeLinkExtensionsChangesLog(link.getMailingID(), link.getFullUrl(), admin, link.getProperties(), newProperties);
            link.setProperties(newProperties);
        }

        trackableLinkService.saveTrackableLink(link);
    }

    private void updateIndividualLink(TrackableLink link, TrackableLinkForm form, List<Integer> bulkIds, Admin admin) {
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append(updateLinkAdmin(link, form.isAdmin()));
        logBuilder.append(updateLinkCreateSubstituteLink(link, form.isCreateSubstituteForAgnDynMulti()));
        if (!bulkIds.contains(form.getId())) {
            logBuilder.append(updateLinkDescription(link, form.getShortname()));
            logBuilder.append(updateLinkUsage(link, form.getUsage()));
            logBuilder.append(updateLinkAction(link, form.getAction(), admin.getCompanyID()));
            logBuilder.append(updateLinkDeepTracking(link, form.getDeepTracking()));
        }
        logLinkEdited(admin, logBuilder, link);
    }

    private StringBuilder updateBulkLinkAndGetLog(TrackableLink link, TrackableLinksForm form, int companyId) {
        StringBuilder log = new StringBuilder();
        if (form.isBulkModifyDescription()) {
            log.append(updateLinkDescription(link, form.getBulkDescription()));
        }
        if (form.getBulkUsage() != KEEP_UNCHANGED) {
            log.append(updateLinkUsage(link, form.getBulkUsage()));
        }
        if (form.getBulkAction() != KEEP_UNCHANGED) {
            log.append(updateLinkAction(link, form.getBulkAction(), companyId));
        }
        if (form.getBulkDeepTracking() != KEEP_UNCHANGED) {
            log.append(updateLinkDeepTracking(link, form.getBulkDeepTracking()));
        }
        if (form.getBulkStatic() != KEEP_UNCHANGED) {
            log.append(updateLinkStatic(link, form.getBulkStatic() == 1));
        }
        return log;
    }

    private String updateLinkDescription(TrackableLink link, String newDescription) {
        String log = getLinkPropertyChangeLog("Description", link.getShortname(), newDescription);
        link.setShortname(StringUtils.defaultString(newDescription));
        return log;
    }

    private String updateLinkUsage(TrackableLink link, int newVal) {
        newVal = StringUtils.defaultString(link.getFullUrl()).contains("##") ? LinkTrackingMode.TEXT_AND_HTML.getMode() : newVal;
        String log = getLinkPropertyChangeLog("Measurable", getUsageName(link.getUsage()), getUsageName(newVal));
        link.setUsage(newVal);
        return log;
    }

    private String updateLinkAction(TrackableLink link, int newVal, int companyId) {
        String log = getLinkPropertyChangeLog("Action",
                actionService.getEmmActionName(link.getActionID(), companyId),
                actionService.getEmmActionName(newVal, companyId));
        link.setActionID(newVal);
        return log;
    }

    private String updateLinkStatic(TrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Is static",
                AgnUtils.boolToString(link.isStaticValue()),
                AgnUtils.boolToString(newVal));
        link.setStaticValue(newVal);
        return log;
    }

    private String updateLinkDeepTracking(TrackableLink link, int newVal) {
        String log = getLinkPropertyChangeLog("Tracking at shop/website",
                getDeepTrackingName(link.getDeepTracking()),
                getDeepTrackingName(newVal));
        link.setDeepTracking(newVal);
        return log;
    }

    private String updateLinkAdmin(TrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Is administrative link",
                AgnUtils.boolToString(link.isAdminLink()),
                AgnUtils.boolToString(newVal));
        link.setAdminLink(newVal);
        return log;
    }

    private String updateLinkCreateSubstituteLink(TrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Create substitute link",
                AgnUtils.boolToString(link.isCreateSubstituteLinkForAgnDynMulti()),
                AgnUtils.boolToString(newVal));
        link.setCreateSubstituteLinkForAgnDynMulti(newVal);
        return log;
    }

    private Comparator<TrackableLink> getComparator(TrackableLinksForm form) {
        Comparator<TrackableLink> comparator = Comparator
                .comparing(TrackableLink::isDeleted)
                .thenComparing(TrackableLink::getId);
        if (StringUtils.equalsIgnoreCase("fullUrlWithExtensions", form.getSort())) {
            comparator = Comparator.comparing(TrackableLink::getFullUrlWithExtensions);
        } else if (StringUtils.equalsIgnoreCase("description", form.getSort())) {
            comparator = Comparator.comparing(l -> StringUtils.trimToEmpty(l.getShortname()));
        }
        if (!AgnUtils.sortingDirectionToBoolean(form.getOrder(), true)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private void syncListForm(TrackableLinksForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.TRACKABLE_LINKS, form);
        if (form.isNumberOfRowsChanged()) {
            form.setPage(1);
        }
        webStorage.access(WebStorage.TRACKABLE_LINKS, storage -> {
            if (form.getIncludeDeleted() == null) {
                form.setIncludeDeleted(storage.isIncludeDeleted());
            } else {
                storage.setIncludeDeleted(form.getIncludeDeleted());
            }
        });
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        UserActivityUtil.log(userActivityLogService, admin, userAction, logger);
    }

    private String getUsageName(int type) {
        switch (type) {
            case 0:
                return "not trackable";
            case 1:
                return "only text version";
            case 2:
                return "only HTML version";
            case 3:
                return "text and HTML version";
            default:
                return "unknown type";
        }
    }

    private String getDeepTrackingName(int type) {
        switch (type) {
            case 0:
                return "no";
            case 1:
                return "with cookie";
            case 2:
                return "with URL-parameters";
            case 3:
                return "Cookie and URL";
            default:
                return "unknown type";
        }
    }

    private boolean intelliAdSettingsValid(TrackableLinksForm form, Popups popups) {
        if (form.isIntelliAdEnabled()) {
            if (StringUtils.isBlank(form.getIntelliAdIdString())) {
                popups.alert("error.intelliad.no_tracking_string");
            }
            if (!StringUtils.defaultString(form.getIntelliAdIdString()).matches("^\\d+-\\d+-\\d+-\\d+-\\d+-\\d+$")) {
                popups.alert("error.intelliad.invalid_format");
            }
        }
        return !popups.hasAlertPopups();
    }

    private void logLinkEdited(Admin admin, StringBuilder logBuilder, TrackableLink link) {
        if (logBuilder.length() != 0) {
            logBuilder.insert(0, String.format("ID = %d. Trackable link %s. ", link.getId(), link.getFullUrl()));
            writeUserActivityLog(admin, EDIT_MESSAGE, logBuilder.toString().trim());
        }
    }

    private void writeCommonActionChangeLog(String actionName, int id, Admin admin, int oldId, int newId) {
        int companyId = admin.getCompanyID();
        if (oldId != newId) {
            writeUserActivityLog(admin, EDIT_MESSAGE,
                    String.format("ID = %d. Trackable links %s Action changed from %s to %s", id, actionName,
                            StringUtils.defaultString(actionService.getEmmActionName(oldId, companyId)),
                            StringUtils.defaultString(actionService.getEmmActionName(newId, companyId))));
        }
    }

    private String getLinkPropertyChangeLog(String property, String oldValue, String newValue) {
        return !StringUtils.equals(oldValue, newValue)
                ? String.format(CHANGE_MSG, property, oldValue, newValue)
                : "";
    }

    private void writeLinkExtensionsChangesLog(int mailingId, String url, Admin admin, List<LinkProperty> oldProperties, List<LinkProperty> newProperties) {
        if (oldProperties == null || newProperties == null
                || (CollectionUtils.isEmpty(oldProperties) && CollectionUtils.isEmpty(newProperties))) {
            return;
        }
        String oldPropertyName;
        String oldPropertyValue;
        String newPropertyName;
        String newPropertyValue;

        //log edited or removed extensions
        int counter = 0;

        for (LinkProperty oldLinkProperty : oldProperties) {
            oldPropertyName = oldLinkProperty.getPropertyName();
            oldPropertyValue = oldLinkProperty.getPropertyValue();

            newPropertyName = "";
            newPropertyValue = "";
            if (newProperties.size() > counter) {
                newPropertyName = newProperties.get(counter).getPropertyName();
                newPropertyValue = newProperties.get(counter).getPropertyValue();
            }

            if (StringUtils.isBlank(newPropertyName)) {
                writeUserActivityLog(admin, EDIT_MESSAGE,
                        String.format("ID = %d. Trackable link %s extension %d removed",
                                mailingId, url, counter + 1));
            } else if ((!oldPropertyName.equals(newPropertyName)) || (!oldPropertyValue.equals(newPropertyValue))) {
                writeUserActivityLog(admin, EDIT_MESSAGE,
                        String.format("ID = %d. Trackable link %s extension %d changed from %s : %s to %s : %s",
                                mailingId, url, counter + 1,
                                oldPropertyName, oldPropertyValue,
                                newPropertyName, newPropertyValue));
            }
            counter++;
        }

        //log added extensions
        if (newProperties.size() > oldProperties.size()) {
            for (int i = oldProperties.size(); i < newProperties.size(); i++) {
                writeUserActivityLog(admin, EDIT_MESSAGE,
                        String.format("ID = %d. Trackable link %s extension %d added %s : %s",
                                mailingId, url, counter + 1,
                                newProperties.get(i).getPropertyName(), newProperties.get(i).getPropertyValue()));
                counter++;
            }
        }

        logger.info("save Trackable links Extensions");
    }

}
