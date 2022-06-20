/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.target.exception.TargetGroupIsInUseException;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.web.forms.BulkActionForm;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.form.TargetForm;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

public class TargetController {

    private static final Logger logger = LogManager.getLogger(TargetController.class);

    private final ComWebStorage webStorage;
    private final MailingService mailingService;
    private final ComBirtReportDao birtReportDao;
    private final ComTargetService targetService;
    private final UserActivityLogService userActivityLogService;

    public TargetController(ComTargetService targetService, ComWebStorage webStorage,
                            UserActivityLogService userActivityLogService, MailingService mailingService,
                            ComBirtReportDao birtReportDao) {
        this.webStorage = webStorage;
        this.birtReportDao = birtReportDao;
        this.mailingService = mailingService;
        this.targetService = targetService;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping("/list.action")
    public String list(@ModelAttribute TargetForm targetForm, final ComAdmin admin, final Model model,
                       final HttpServletRequest request) {
        prepareListParameters(targetForm, admin);
        AgnUtils.setAdminDateTimeFormatPatterns(request);

        model.addAttribute("isSearchEnabled", targetService.isBasicFullTextSearchSupported());
        model.addAttribute("targetComplexities", targetService.getTargetComplexities(admin.getCompanyID()));
        model.addAttribute("targets", getTargetGroupsOverview(targetForm, admin));

        return "targets_list";
    }

    @RequestMapping("/confirm/bulk/delete.action")
    public String confirmBulkDelete(@ModelAttribute("form") BulkActionForm form, final Popups popups) {
        if (CollectionUtils.isEmpty(form.getBulkIds())) {
            popups.alert("bulkAction.nothing.target");
            return "messages";
        }

        return "targets_bulk_delete_confirm";
    }

    @PostMapping("/bulk/delete.action")
    public String bulkDelete(@ModelAttribute("form") BulkActionForm form, final ComAdmin admin,
                             final Popups popups) {
        final int companyId = admin.getCompanyID();

        final Map<Integer, String> targetsToDelete = getTargetNamesToDelete(new HashSet<>(form.getBulkIds()), companyId);

        final boolean success = tryDeleteTargets(targetsToDelete.keySet(), companyId, popups);
        if (!success) {
            return "messages";
        }

        for (Map.Entry<Integer, String> entry : targetsToDelete.entrySet()) {
            userActivityLogService.writeUserActivityLog(admin,
                    "delete target group",
                    entry.getValue() + " (" + entry.getKey() + ")");
        }

        popups.success("default.selection.deleted");

        return "redirect:/target/list.action";
    }

    @RequestMapping("{id:\\d+}/confirm/delete.action")
    public String confirmDelete(@PathVariable final int id, final ComAdmin admin, @ModelAttribute SimpleActionForm simpleActionForm,
                                @RequestParam(required = false) boolean isWizard,
                                final Popups popups, final Model model) {
        final int companyId = admin.getCompanyID();
        try {
            final ComTarget targetGroup = targetService.getTargetGroup(id, companyId);
            simpleActionForm.setId(targetGroup.getId());
            simpleActionForm.setShortname(targetGroup.getTargetName());

            model.addAttribute("isWizard", isWizard);

            loadDependentEntities(id, model, companyId);
        } catch (UnknownTargetGroupIdException e) {
            popups.alert("error.target.delete");
            return "messages";
        }

        final SimpleServiceResult canBeDeletedResult = targetService.canBeDeleted(id, companyId);
        if (canBeDeletedResult.isSuccess()) {
            return "targets_delete_confirm";
        }
        popups.addPopups(canBeDeletedResult);
        return "messages";
    }

    @PostMapping("/delete.action")
    public String delete(final ComAdmin admin, @ModelAttribute SimpleActionForm simpleActionForm, final Popups popups) {
        final boolean success = deleteTarget(simpleActionForm.getId(), admin, popups);
        if (success) {
            return "redirect:/target/list.action";
        }

        return "messages";
    }

    @PostMapping("/wizardDelete.action")
    @PermissionMapping("delete")
    public String wizardDelete(final ComAdmin admin, @ModelAttribute SimpleActionForm simpleActionForm, final Popups popups) {
        final boolean success = deleteTarget(simpleActionForm.getId(), admin, popups);
        if (success) {
            return "redirect:/mwTarget.do?action=newTarget";
        }

        return "messages";
    }

    @RequestMapping("/{id:\\d+}/confirm/delete/recipients.action")
    public String confirmDeleteRecipients(final Model model, @PathVariable final int id,
                                          final ComAdmin admin, final Popups popups) {
        final Optional<Integer> numOfRecipientsOpt = targetService.getNumberOfRecipients(id, admin.getCompanyID());
        if(!numOfRecipientsOpt.isPresent()) {
            logger.warn("It is not possible to delete recipients for target with id: " + id);
            popups.alert("Error");
            return "messages";
        }
        model.addAttribute("numberOfRecipients", numOfRecipientsOpt.get());
        model.addAttribute("targetIdToDeleteRecipients", id);
        return "targets_delete_recipients_confirm";
    }

    @PostMapping("/{id:\\d+}/delete/recipients.action")
    public String deleteRecipients(final ComAdmin admin, @PathVariable final int id, final Popups popups) {
        final boolean deletedSuccessfully = targetService.deleteRecipients(id, admin.getCompanyID());

        if (!deletedSuccessfully) {
            logger.warn("Could not delete recipients for target with id: " + id);
            popups.alert("Error");
            return "messages";
        }

        popups.success("default.changes_saved");
        userActivityLogService.writeUserActivityLog(admin, "edit target group",
                "All recipients deleted from target group with id=" + id);
        return "redirect:/target/list.action";
    }

    @RequestMapping("/{id:\\d+}/addToFavorites.action")
    public ResponseEntity<Object> addToFavorites(@PathVariable final int id, ComAdmin admin) {
        try {
            targetService.addToFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't add target group [id = %d] to favorites. %s", id, e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping("/{id:\\d+}/removeFromFavorites.action")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable final int id, ComAdmin admin) {
        try {
            targetService.removeFromFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't remove target group [id = %d] from favorites. %s.", id, e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<TargetLight> getTargetGroupsOverview(final TargetForm form, final ComAdmin admin) {
        return targetService.getTargetLights(getOverviewTargetLightsOptionsBuilder(admin, form).build());
    }
    
    protected TargetLightsOptions.Builder getOverviewTargetLightsOptionsBuilder(ComAdmin admin, TargetForm form) {
        return TargetLightsOptions.builder()
                        .setCompanyId(admin.getCompanyID())
                        .setWorldDelivery(form.isShowWorldDelivery())
                        .setAdminTestDelivery(form.isShowTestAndAdminDelivery())
                        .setSearchName(form.isSearchNameChecked())
                        .setSearchDescription(form.isSearchDescriptionChecked())
                        .setSearchText(form.getSearchQueryText());
    }

    private void prepareListParameters(final TargetForm form, final ComAdmin admin) {
        synchronized (ComWebStorage.TARGET_OVERVIEW) {
            final boolean isBundlePresented = webStorage.isPresented(ComWebStorage.TARGET_OVERVIEW);
            webStorage.access(ComWebStorage.TARGET_OVERVIEW, storage -> {
                if (form.getNumberOfRows() > 0) {
                    storage.setRowsCount(form.getNumberOfRows());
                    storage.setShowWorldDelivery(form.isShowWorldDelivery());
                    storage.setShowTestAndAdminDelivery(form.isShowTestAndAdminDelivery());
                } else {
                    form.setNumberOfRows(storage.getRowsCount());
                    form.setShowWorldDelivery(storage.isShowWorldDelivery());
                    if (!isBundlePresented && admin.permissionAllowed(Permission.MAILING_SEND_ADMIN_TARGET)) {
                        storage.setShowTestAndAdminDelivery(true);
                    }
                    form.setShowTestAndAdminDelivery(storage.isShowTestAndAdminDelivery());
                }
            });
        }
    }

    private boolean tryDeleteTargets(final Set<Integer> targetsToDelete, final int companyId, final Popups popups) {
        try {
            if (!targetsToDelete.isEmpty()) {
                targetService.bulkDelete(targetsToDelete, companyId);
                return true; // success
            }
        } catch (TargetGroupLockedException e) {
            popups.alert("target.locked");
        } catch (TargetGroupIsInUseException e) {
            popups.alert("error.target.in_use");
        } catch (Exception e) {
            popups.alert("error.target.delete");
        }

        return false; // failed
    }

    private Map<Integer, String> getTargetNamesToDelete(final Set<Integer> bulkIds, final int companyId) {
        final Map<Integer, String> targetsToDelete = new HashMap<>();
        for (int id : bulkIds) {
            String name = targetService.getTargetName(id, companyId);
            if (name != null) {
                targetsToDelete.put(id, name);
            }
        }
        return targetsToDelete;
    }

    private void loadDependentEntities(final int targetId, final Model model, final int companyId) {
        final List<LightweightMailing> affectedMailings = mailingService.getMailingsDependentOnTargetGroup(companyId, targetId);
        if (CollectionUtils.isNotEmpty(affectedMailings)) {
            model.addAttribute("affectedMailingsLightweight", affectedMailings);
            model.addAttribute("affectedMailingsMessageKey", "warning.target.delete.affectedMailings");
            model.addAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
        }

        final List<ComLightweightBirtReport> affectedReports = birtReportDao.getLightweightBirtReportsBySelectedTarget(companyId, targetId);
        if (CollectionUtils.isNotEmpty(affectedReports)) {
            model.addAttribute("affectedReports", affectedReports);
            model.addAttribute("affectedReportsMessageKey", "warning.target.delete.affectedBirtReports");
            model.addAttribute("affectedReportsMessageType", GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
        }
    }

    private boolean deleteTarget(final int targetId, final ComAdmin admin, final Popups popups) {
        final int companyId = admin.getCompanyID();
        final String name = targetService.getTargetName(targetId, companyId);
        if (name == null) {
            logger.error("No target exists, ID: " + targetId);
            popups.alert("error.target.delete");
            return false; // failed- not exists
        }
        try {
            targetService.deleteTargetGroup(targetId, companyId);
            userActivityLogService.writeUserActivityLog(admin, "delete target group", name + " (" + targetId + ")");

            popups.success("default.selection.deleted");
            return true; // successfully deleted
        } catch (TargetGroupLockedException e) {
            popups.alert("target.locked");
        } catch (TargetGroupIsInUseException e) {
            popups.alert("error.target.in_use");
        } catch (Exception e) {
            logger.error("Target deletion failed!", e);
            popups.alert("error.target.delete");
        }
        return false;// failed
    }
}
