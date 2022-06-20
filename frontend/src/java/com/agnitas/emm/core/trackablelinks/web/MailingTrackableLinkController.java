/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm;
import com.agnitas.emm.core.trackablelinks.form.TrackableLinksForm;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.exception.ClearLinkExtensionsException;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.exceptions.InsufficientPermissionException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.BulkActionForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import static org.agnitas.beans.BaseTrackableLink.KEEP_UNCHANGED;

@Controller
@RequestMapping("/mailing/{mailingId:\\d+}/trackablelink")
@PermissionMapping("mailing.trackablelink")
public class MailingTrackableLinkController {

    private static final Logger logger = LogManager.getLogger(MailingTrackableLinkController.class);

    private static final String CHANGE_MSG = "%s changed from %s to %s. ";
    private static final String MESSAGES_VIEW = "messages";
    private static final String ERROR_CODE = "Error";
    private static final String CHANGES_SAVED_CODE = "default.changes_saved";
    private static final String REDIRECT_TO_LIST_STR = "redirect:/mailing/%d/trackablelink/list.action";
    private static final String NOT_FORM_ACTIONS_ATTR = "notFormActions";
    private static final String EDIT_MESSAGE = "edit mailing links";

    private final ComMailingBaseService mailingBaseService;
    private final GridServiceWrapper gridService;
    private final ConfigService configService;
    private final LinkService linkService;
    private final MailingDao mailingDao;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final ComEmmActionService actionService;
    private final ComTrackableLinkService trackableLinkService;
    private final ExtendedConversionService conversionService;

    public MailingTrackableLinkController(UserActivityLogService userActivityLogService, ComTrackableLinkService trackableLinkService, ExtendedConversionService conversionService, ComMailingBaseService mailingBaseService, ComEmmActionService actionService, GridServiceWrapper gridService, ConfigService configService, LinkService linkService, MailingDao mailingDao, WebStorage webStorage) {
        this.mailingBaseService = mailingBaseService;
        this.gridService = gridService;
        this.configService = configService;
        this.linkService = linkService;
        this.mailingDao = mailingDao;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.actionService = actionService;
        this.trackableLinkService = trackableLinkService;
        this.conversionService = conversionService;
    }

    @RequestMapping("/list.action")
    public String list(@PathVariable int mailingId, TrackableLinksForm form, ComAdmin admin, Model model,
                       @RequestParam(required = false) Integer scrollToLinkId) {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);

        addMailingModelAttrs(mailing, model, mailingId, admin);
        addLinkListModelAttrs(mailingId, model, companyId, mailing, scrollToLinkId);
        setupFormGlobalSettings(form, mailing);
        setupList(form, mailing.getTrackableLinks().values(), model);

        writeUserActivityLog(admin, "trackable link list", "active tab - links");
        return "mailing_trackablelink_list_new";
    }

    private void addLinkListModelAttrs(int mailingId, Model model, int companyId, Mailing mailing, Integer scrollToLinkId) {
        model.addAttribute(NOT_FORM_ACTIONS_ATTR, actionService.getEmmNotFormActions(companyId, false));
        model.addAttribute("scrollToLinkId", scrollToLinkId == null ? 0 : scrollToLinkId);
        model.addAttribute("defaultExtensions", conversionService.convert(linkService.getDefaultExtensions(companyId), LinkProperty.class, ExtensionProperty.class));
        model.addAttribute("allLinksExtensions", conversionService.convert(mailing.getCommonLinkExtensions(), LinkProperty.class, ExtensionProperty.class));
        model.addAttribute("hasDefaultLinkExtension", StringUtils.isNotBlank(configService.getValue(ConfigValue.DefaultLinkExtension, companyId)));
        model.addAttribute("SHOW_CREATE_SUBSTITUTE_LINK", configService.getBooleanValue(ConfigValue.RedirectMakeAgnDynMultiLinksTrackable, companyId));
        model.addAttribute("isTrackingOnEveryPositionAvailable", trackableLinkService.isTrackingOnEveryPositionAvailable(companyId, mailingId));
    }

    private void setupList(TrackableLinksForm form, Collection<ComTrackableLink> comLinks, Model model) {
        syncListForm(form);
        Map<Integer, String> originalUrls = new HashMap<>();
        for (ComTrackableLink link : comLinks) {
            originalUrls.put(link.getId(), link.getOriginalUrl());
        }
        model.addAttribute("originalUrls", originalUrls);
        List<ComTrackableLink> sortedLinks = comLinks.stream().sorted(getComparator(form)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sortedLinks)) {
            form.setNumberOfRows(sortedLinks.size());
        }
        List<TrackableLinkForm> links = conversionService.convert(sortedLinks, ComTrackableLink.class, TrackableLinkForm.class);
        form.setLinks(links);
        model.addAttribute("paginatedTrackableLinks", new PaginatedListImpl<>(links, sortedLinks.size(), form.getNumberOfRows(), 1, form.getSort(), form.getOrder()));
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

    private void addMailingModelAttrs(Mailing mailing, Model model, int mailingId, ComAdmin admin) {
        model.addAttribute("isTemplate", mailing.isIsTemplate());
        model.addAttribute("mailingShortname", mailing.getShortname());
        model.addAttribute("gridTemplateId", gridService.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));
        if (mailingId > 0) {
            model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));
            model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, admin.getCompanyID()));
        } else {
            model.addAttribute("isMailingUndoAvailable", false);
            model.addAttribute("workflowId", 0);
        }
    }

    @PostMapping("/confirmBulkClearExtensions.action")
    public String confirmBulkClearExtensions(@PathVariable int mailingId, TrackableLinksForm form, Model model, ComAdmin admin, Popups popups) {
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
    public String bulkClearExtensions(@PathVariable int mailingId, BulkActionForm form, ComAdmin admin, Popups popups) {
        try {
            trackableLinkService.bulkClearExtensions(mailingId, admin.getCompanyID(), new HashSet<>(form.getBulkIds()));
            writeUserActivityLog(admin, "edit mailing", "ID = " + mailingId + ". Removed global and individual link extensions");
            popups.success(CHANGES_SAVED_CODE);
            return String.format(REDIRECT_TO_LIST_STR, mailingId);
        } catch (ClearLinkExtensionsException e) {
            logger.error("Error removing global and individual link extensions (mailing ID: " + mailingId + ")", e);
            popups.alert("error.trackablelinks.extensions.remove");
            return MESSAGES_VIEW;
        }
    }

    @RequestMapping("/bulkActionsView.action")
    public String bulkActionsView(@PathVariable int mailingId, TrackableLinksForm form, Model model, ComAdmin admin, Popups popups) {
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
    public String activateTrackingLinksOnEveryPosition(@PathVariable int mailingId, TrackableLinksForm form,
                                                       ComAdmin admin, Popups popups, HttpServletRequest req) {
        if (form.isTrackOnEveryPosition()) {
            try {
                mailingBaseService.activateTrackingLinksOnEveryPosition(admin,
                        mailingDao.getMailing(mailingId, admin.getCompanyID()), getApplicationContext(req));
            } catch (Exception e) {
                popups.alert(ERROR_CODE);
                return MESSAGES_VIEW;
            }
        }
        popups.success(CHANGES_SAVED_CODE);
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @PostMapping("/saveAll.action")
    public String saveAll(@PathVariable int mailingId, TrackableLinksForm form, ComAdmin admin, Popups popups) {
        if (!intelliAdSettingsValid(form, popups)) {
            return MESSAGES_VIEW;
        }
        saveAll(mailingId, form, admin);
        popups.success(CHANGES_SAVED_CODE);
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private void saveAll(int mailingId, TrackableLinksForm form, ComAdmin admin) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        Collection<ComTrackableLink> links = mailing.getTrackableLinks().values();

        updateGlobalSettings(form, mailing, admin);
        updateIndividualLinks(links, form, admin);
        updateBulkLinks(links, form, mailing, admin);
        mailingDao.saveMailing(mailing, false);
    }

    private void updateGlobalSettings(TrackableLinksForm form, Mailing mailing, ComAdmin admin) {
        writeCommonActionChanges(mailing, form, admin);
        mailing.setOpenActionID(form.getOpenActionId());
        mailing.setClickActionID(form.getClickActionId());
        saveIntelliAdSettings(form, mailing);

        if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS) && form.isModifyAllLinksExtensions()) {
            updateGlobalLinksExtensions(form, mailing, admin);
        }
    }

    private void updateGlobalLinksExtensions(TrackableLinksForm form, Mailing mailing, ComAdmin admin) {
        List<UserAction> userActions = new ArrayList<>();
        List<LinkProperty> extensions = conversionService.convert(form.getExtensions(), ExtensionProperty.class, LinkProperty.class);
        Set<Integer> linkIds = mailing.getTrackableLinks().values().stream().map(BaseTrackableLink::getId).collect(Collectors.toSet());
        trackableLinkService.addExtensions(mailing, linkIds, extensions, userActions);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
    }

    private void updateIndividualLinks(Collection<ComTrackableLink> links, TrackableLinksForm linksForm, ComAdmin admin) {
        Map<Integer, ComTrackableLink> existLinks = links.stream()
                .collect(Collectors.toMap(BaseTrackableLink::getId, Function.identity()));

        for (TrackableLinkForm form : linksForm.getLinks()) {
            updateIndividualLink(existLinks.get(form.getId()), form, linksForm.getBulkIds(), admin);
        }
    }

    private void updateBulkLinks(Collection<ComTrackableLink> links, TrackableLinksForm form, Mailing mailing, ComAdmin admin) {
        links.stream()
                .filter(link -> form.getBulkIds().contains(link.getId()))
                .forEach(link -> logLinkEdited(admin, updateBulkLinkAndGetLog(link, form, admin.getCompanyID()), link));

        if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS) && form.isModifyBulkLinksExtensions()) {
            updateBulkLinksExtensions(form, mailing, admin);
        }
    }

    private void updateBulkLinksExtensions(TrackableLinksForm form, Mailing mailing, ComAdmin admin) {
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
    public String view(@PathVariable int mailingId, @PathVariable int linkId, ComAdmin admin, Model model, Popups popups) {
        int companyId = admin.getCompanyID();
        ComTrackableLink link = trackableLinkService.getTrackableLink(companyId, linkId);
        if (link == null) {
            popups.alert(ERROR_CODE);
            logger.error("could not load link: " + linkId);
            return MESSAGES_VIEW;
        }
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        addMailingModelAttrs(mailing, model, mailingId, admin);
        addLinkViewModelAttrs(mailingId, admin, model, companyId, link);
        writeUserActivityLog(admin, "trackable link list", "active tab - links");
        return "mailing_trackablelink_view_new";
    }

    private void addLinkViewModelAttrs(int mailingId, ComAdmin admin, Model model, int companyId, ComTrackableLink link) {
        model.addAttribute("trackableLinkForm", conversionService.convert(link, TrackableLinkForm.class));
        model.addAttribute("altText", link.getAltText());
        model.addAttribute("originalUrl", link.getOriginalUrl());
        model.addAttribute(NOT_FORM_ACTIONS_ATTR, actionService.getEmmNotFormActions(companyId, false));
        model.addAttribute("isUrlEditingAllowed", trackableLinkService.isUrlEditingAllowed(admin, mailingId));
        model.addAttribute("SHOW_CREATE_SUBSTITUTE_LINK", configService.getBooleanValue(ConfigValue.RedirectMakeAgnDynMultiLinksTrackable, companyId));
    }

    @PostMapping("/{linkId:\\d+}/save.action")
    public String save(@PathVariable int mailingId, @PathVariable int linkId, TrackableLinkForm form, ComAdmin admin, Popups popups, HttpServletRequest req, RedirectAttributes redirectAttrs) throws TrackableLinkException {
        ComTrackableLink link = trackableLinkService.getTrackableLink(admin.getCompanyID(), linkId);
        if (link == null) {
            popups.alert(ERROR_CODE);
            return MESSAGES_VIEW;
        }
        if (StringUtils.isNotBlank(form.getUrl()) && !tryUpdateLinkUrl(linkId, form.getUrl(), admin)) {
            popups.alert("error.permissionDenied");
            return MESSAGES_VIEW;
        }

        saveLink(link, form, admin, req.getParameter("deepTracking") != null);
        redirectAttrs.addAttribute("scrollToLinkId", linkId);
        popups.success(CHANGES_SAVED_CODE);
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private boolean tryUpdateLinkUrl(int linkId, String newUrl, ComAdmin admin) throws TrackableLinkException {
        try {
            updateLinkUrl(linkId, newUrl, admin);
            return true;
        } catch (InsufficientPermissionException e) {
            return false;
        }
    }

    /**
     * Update link target including permission checks, ...
     *
     * @param linkId id of the trackable link
     * @param newUrl URL to be updated
     * @param admin  current admin
     * @throws InsufficientPermissionException if user does not have sufficient permissions to change link target
     * @throws TrackableLinkException          on errors updating link target
     */
    private void updateLinkUrl(int linkId, String newUrl, ComAdmin admin) throws InsufficientPermissionException, TrackableLinkException {
        ComTrackableLink link = trackableLinkService.getTrackableLink(admin.getCompanyID(), linkId);
        if (!newUrl.equals(link.getFullUrl())) {
            if (admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_URL_CHANGE)) {
                trackableLinkService.updateLinkTarget(link, newUrl);

                writeUserActivityLog(admin, "edit link target", "Set target of link " + link.getId() + " to " + newUrl);
            } else {
                logger.warn("Admin " + admin.getUsername() + " (ID " + admin.getAdminID() + ") has no permission to edit link URLs");
                throw new InsufficientPermissionException(admin.getAdminID(), "mailing.trackablelinks.url.change");
            }
        }
    }

    private WebApplicationContext getApplicationContext(HttpServletRequest req) {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(req
                .getSession().getServletContext());
    }

    private void writeCommonActionChanges(Mailing mailing, TrackableLinksForm form, ComAdmin admin) {
        int mailingId = mailing.getId();
        writeCommonActionChangeLog("Open", mailingId, admin, mailing.getOpenActionID(), form.getOpenActionId());
        writeCommonActionChangeLog("Click", mailingId, admin, mailing.getClickActionID(), form.getClickActionId());
        if (logger.isInfoEnabled()) {
            logger.info("save Trackable links Open/Click Actions, mailing ID =  " + mailingId);
        }
    }

    private void saveLink(ComTrackableLink link, TrackableLinkForm form, ComAdmin admin, boolean includeDeepTracking) {
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

    private void updateIndividualLink(ComTrackableLink link, TrackableLinkForm form, List<Integer> bulkIds, ComAdmin admin) {
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

    private StringBuilder updateBulkLinkAndGetLog(ComTrackableLink link, TrackableLinksForm form, int companyId) {
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

    private String updateLinkDescription(ComTrackableLink link, String newDescription) {
        String log = getLinkPropertyChangeLog("Description", link.getShortname(), newDescription);
        link.setShortname(StringUtils.defaultString(newDescription));
        return log;
    }

    private String updateLinkUsage(ComTrackableLink link, int newVal) {
        newVal = StringUtils.defaultString(link.getFullUrl()).contains("##") ? TrackableLink.TRACKABLE_TEXT_HTML : newVal;
        String log = getLinkPropertyChangeLog("Measurable", getUsageName(link.getUsage()), getUsageName(newVal));
        link.setUsage(newVal);
        return log;
    }

    private String updateLinkAction(ComTrackableLink link, int newVal, int companyId) {
        String log = getLinkPropertyChangeLog("Action",
                actionService.getEmmActionName(link.getActionID(), companyId),
                actionService.getEmmActionName(newVal, companyId));
        link.setActionID(newVal);
        return log;
    }

    private String updateLinkStatic(ComTrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Is static",
                AgnUtils.boolToString(link.isStaticValue()),
                AgnUtils.boolToString(newVal));
        link.setStaticValue(newVal);
        return log;
    }

    private String updateLinkDeepTracking(ComTrackableLink link, int newVal) {
        String log = getLinkPropertyChangeLog("Tracking at shop/website",
                getDeepTrackingName(link.getDeepTracking()),
                getDeepTrackingName(newVal));
        link.setDeepTracking(newVal);
        return log;
    }

    private String updateLinkAdmin(ComTrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Is administrative link",
                AgnUtils.boolToString(link.isAdminLink()),
                AgnUtils.boolToString(newVal));
        link.setAdminLink(newVal);
        return log;
    }

    private String updateLinkCreateSubstituteLink(ComTrackableLink link, boolean newVal) {
        String log = getLinkPropertyChangeLog("Create substitute link",
                AgnUtils.boolToString(link.isCreateSubstituteLinkForAgnDynMulti()),
                AgnUtils.boolToString(newVal));
        link.setCreateSubstituteLinkForAgnDynMulti(newVal);
        return log;
    }

    private Comparator<ComTrackableLink> getComparator(TrackableLinksForm form) {
        Comparator<ComTrackableLink> comparator = Comparator.comparing(TrackableLink::getId);
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
        webStorage.access(ComWebStorage.TRACKABLE_LINKS, storage -> {
            if (StringUtils.isNoneBlank(form.getSort())) {
                storage.setColumnName(form.getSort());
                storage.setAscendingOrder(AgnUtils.sortingDirectionToBoolean(form.getOrder()));
            } else {
                form.setSort(storage.getColumnName());
                form.setOrder(storage.isAscendingOrder() ? "ascending" : "descending");
            }
        });
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

    private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        if (userActivityLogService != null) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info("Userlog: " + admin.getUsername() + " " + userAction.getAction() + " " + userAction.getDescription());
        }
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

    private void logLinkEdited(ComAdmin admin, StringBuilder logBuilder, ComTrackableLink link) {
        if (logBuilder.length() != 0) {
            logBuilder.insert(0, String.format("ID = %d. Trackable link %s. ", link.getId(), link.getFullUrl()));
            writeUserActivityLog(admin, EDIT_MESSAGE, logBuilder.toString().trim());
        }
    }

    private void writeCommonActionChangeLog(String actionName, int id, ComAdmin admin, int oldId, int newId) {
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

    private void writeLinkExtensionsChangesLog(int mailingId, String url, ComAdmin admin, List<LinkProperty> oldProperties, List<LinkProperty> newProperties) {
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

        if (logger.isInfoEnabled()) {
            logger.info("save Trackable links Extensions");
        }
    }
}
