/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.common.ObjectUserType;
import com.agnitas.emm.core.target.form.TargetForm;
import com.agnitas.emm.core.target.form.TargetListFormSearchParams;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.web.forms.BulkActionForm;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

public class TargetController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(TargetController.class);
    private static final String TARGETS_LIST_REDIRECT = "redirect:/target/list.action?restoreSort=true";
    private static final String TARGET_DELETE_ERROR_MSG = "error.target.delete";

    protected final ComTargetService targetService;
    private final WebStorage webStorage;
    private final ComBirtReportDao birtReportDao;
    private final UserActivityLogService userActivityLogService;

    public TargetController(ComTargetService targetService, WebStorage webStorage,
                            UserActivityLogService userActivityLogService,
                            ComBirtReportDao birtReportDao) {
        this.webStorage = webStorage;
        this.birtReportDao = birtReportDao;
        this.targetService = targetService;
        this.userActivityLogService = userActivityLogService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @ModelAttribute
    public TargetListFormSearchParams getSearchParams() {
        return new TargetListFormSearchParams();
    }

    @RequestMapping("/list.action")
    public String list(@ModelAttribute TargetForm targetForm, @ModelAttribute TargetListFormSearchParams searchParams, @RequestParam(required = false) boolean restoreSort,
                       Admin admin, Model model) {
        if (isRedesignedUiUsed(admin)) {
            FormUtils.syncSearchParams(searchParams, targetForm, true);
            FormUtils.syncPaginationData(webStorage, WebStorage.TARGET_OVERVIEW, targetForm, restoreSort);
        } else {
            FormUtils.updateSortingState(webStorage, WebStorage.TARGET_OVERVIEW, targetForm, restoreSort);
            prepareListParameters(targetForm, admin);
        }
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        addListModelAttrs(targetForm, admin, model);
        return "targets_list";
    }

    protected void addListModelAttrs(TargetForm targetForm, Admin admin, Model model) { // overridden in extended class
        model.addAttribute("isSearchEnabled", targetService.isBasicFullTextSearchSupported());
        model.addAttribute("targetComplexities", targetService.getTargetComplexities(admin.getCompanyID()));
        model.addAttribute("targetEntries", getPaginatedTargetGroupsOverview(targetForm, admin));
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute TargetForm form, @ModelAttribute TargetListFormSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, form, false);
        ra.addFlashAttribute("targetForm", form);
        return TARGETS_LIST_REDIRECT;
    }

    @PostMapping("/restore.action")
    public String restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);

        targetService.restore(bulkIds, admin);
        popups.success(CHANGES_SAVED_MSG);

        return TARGETS_LIST_REDIRECT + "&showDeleted=true";
    }

    private void validateSelectedIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException("bulkAction.nothing.target");
        }
    }

    @RequestMapping("/confirm/bulk/delete.action")
    public String confirmBulkDelete(@ModelAttribute("form") BulkActionForm form, Popups popups, Admin admin, Model model) {
        validateSelectedIds(form.getBulkIds());

        if (isRedesignedUiUsed(admin)) {
            ServiceResult<List<String>> result = targetService.getTargetNamesForDeletion(form.getBulkIds(), admin);
            popups.addPopups(result);

            if (!result.isSuccess()) {
                return MESSAGES_VIEW;
            }

            model.addAttribute("names", result.getResult());
        }

        return "targets_bulk_delete_confirm";
    }

    private static boolean isRedesignedUiUsed(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

    @PostMapping("/bulk/delete.action")
    public String bulkDelete(@ModelAttribute("form") BulkActionForm form, Admin admin, Popups popups) {
        if (isRedesignedUiUsed(admin)) {
            List<Integer> ids = targetService.bulkDeleteRedesigned(new HashSet<>(form.getBulkIds()), admin);
            if (CollectionUtils.isNotEmpty(ids)) {
                popups.success(SELECTION_DELETED_MSG);
                userActivityLogService.writeUserActivityLog(
                        admin,
                        "delete target group",
                        "deleted targets with following ids: " + StringUtils.join(ids, ", ")
                );
            }
        } else {
            Map<Integer, String> targetNamesMap = getTargetNamesToDelete(new HashSet<>(form.getBulkIds()), admin.getCompanyID());
            Set<Integer> targetsToDelete = targetNamesMap.keySet();

            if (!targetNamesMap.isEmpty()) {
                ServiceResult<List<Integer>> deletionResult = targetService.bulkDelete(targetsToDelete, admin);

                if (deletionResult.isSuccess()) {
                    popups.success(SELECTION_DELETED_MSG);
                } else {
                    popups.addPopups(deletionResult);
                }

                for (Integer deletedTarget : deletionResult.getResult()) {
                    userActivityLogService.writeUserActivityLog(admin,
                            "delete target group",
                            String.format("%s (%d)", targetNamesMap.get(deletedTarget), deletedTarget));
                }
            }
        }

        return TARGETS_LIST_REDIRECT;
    }

    @RequestMapping("{id:\\d+}/confirm/delete.action")
    public String confirmDelete(@PathVariable int id, Admin admin, @ModelAttribute SimpleActionForm simpleActionForm, Popups popups, Model model) {
        final int companyId = admin.getCompanyID();
        try {
            final ComTarget targetGroup = targetService.getTargetGroup(id, companyId);
            simpleActionForm.setId(targetGroup.getId());
            simpleActionForm.setShortname(targetGroup.getTargetName());

            loadDependentBirtReports(id, popups, admin, model);
        } catch (UnknownTargetGroupIdException e) {
            popups.alert(TARGET_DELETE_ERROR_MSG);
            return MESSAGES_VIEW;
        }

        final SimpleServiceResult canBeDeletedResult = targetService.canBeDeleted(id, admin);
        if (canBeDeletedResult.isSuccess()) {
            return "targets_delete_confirm";
        }
        popups.addPopups(canBeDeletedResult);
        return MESSAGES_VIEW;
    }

    @PostMapping("/delete.action")
    public String delete(Admin admin, @ModelAttribute SimpleActionForm simpleActionForm, Popups popups) {
        final boolean success = deleteTarget(simpleActionForm.getId(), admin, popups);
        if (success) {
            return TARGETS_LIST_REDIRECT;
        }

        return MESSAGES_VIEW;
    }

    @RequestMapping("/{id:\\d+}/confirm/delete/recipients.action")
    public String confirmDeleteRecipients(Model model, @PathVariable int id, Admin admin, Popups popups) {
        final Optional<Integer> numOfRecipientsOpt = targetService.getNumberOfRecipients(id, admin.getCompanyID());
        if (numOfRecipientsOpt.isEmpty()) {
            logger.warn("It is not possible to delete recipients for target with id: {}", id);
            popups.alert(ERROR_MSG);
            return MESSAGES_VIEW;
        }
        model.addAttribute("numberOfRecipients", numOfRecipientsOpt.get());
        model.addAttribute("targetIdToDeleteRecipients", id);
        return "targets_delete_recipients_confirm";
    }

    @PostMapping("/{id:\\d+}/delete/recipients.action")
    public String deleteRecipients(Admin admin, @PathVariable int id, Popups popups) {
        final boolean deletedSuccessfully = targetService.deleteRecipients(id, admin.getCompanyID());

        if (!deletedSuccessfully) {
            logger.warn("Could not delete recipients for target with id: {}", id);
            popups.alert(ERROR_MSG);
            return MESSAGES_VIEW;
        }

        popups.success(CHANGES_SAVED_MSG);
        userActivityLogService.writeUserActivityLog(admin, "edit target group",
                "All recipients deleted from target group with id=" + id);
        return TARGETS_LIST_REDIRECT;
    }

    @RequestMapping("/{id:\\d+}/addToFavorites.action")
    public ResponseEntity<Object> addToFavorites(@PathVariable int id, Admin admin) {
        try {
            targetService.addToFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't add target group [id = %d] to favorites. %s", id, e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping("/{id:\\d+}/removeFromFavorites.action")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable int id, Admin admin) {
        try {
            targetService.removeFromFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't remove target group [id = %d] from favorites. %s.", id, e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private PaginatedListImpl<TargetLight> getPaginatedTargetGroupsOverview(TargetForm form, Admin admin) {
        return targetService.getTargetLightsPaginated(
                getOverviewTargetLightsOptionsBuilder(admin, form).build(),
                form.getSearchComplexity()
        );
    }

    protected TargetLightsOptions.Builder getOverviewTargetLightsOptionsBuilder(Admin admin, TargetForm form) {
        return TargetLightsOptions.builder()
                .setAdminId(admin.getAdminID())
                .setCompanyId(admin.getCompanyID())
                .setWorldDelivery(form.isShowWorldDelivery())
                .setAdminTestDelivery(form.isShowTestAndAdminDelivery())
                .setDeliveryOption(form.getSearchDeliveryOption())
                .setSearchName(form.isSearchNameChecked())
                .setSearchDescription(form.isSearchDescriptionChecked())
                .setSearchText(form.getSearchQueryText())
                .setSearchName(form.getSearchName())
                .setSearchDescription(form.getSearchDescription())
                .setCreationDate(form.getSearchCreationDate())
                .setChangeDate(form.getSearchChangeDate())
                .setRedesignedUiUsed(isRedesignedUiUsed(admin))
                .setDeleted(form.isShowDeleted())
                .setPageNumber(form.getPage())
                .setPageSize(form.getNumberOfRows())
                .setDirection(form.getDir())
                .setSorting(form.getSort());
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private void prepareListParameters(TargetForm form, Admin admin) {
        synchronized (WebStorage.TARGET_OVERVIEW) {
            final boolean isBundlePresented = webStorage.isPresented(WebStorage.TARGET_OVERVIEW);
            webStorage.access(WebStorage.TARGET_OVERVIEW, storage -> {
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

    private Map<Integer, String> getTargetNamesToDelete(Set<Integer> bulkIds, int companyId) {
        final Map<Integer, String> targetsToDelete = new HashMap<>();
        for (int id : bulkIds) {
            String name = targetService.getTargetName(id, companyId);
            if (name != null) {
                targetsToDelete.put(id, name);
            }
        }
        return targetsToDelete;
    }

    private void loadDependentBirtReports(int targetId, Popups popups, Admin admin, Model model) {
        final List<ComLightweightBirtReport> affectedReports = birtReportDao.getLightweightBirtReportsBySelectedTarget(admin.getCompanyID(), targetId);
        if (CollectionUtils.isNotEmpty(affectedReports)) {
            if (isRedesignedUiUsed(admin)) {
                List<ObjectUsage> usages = affectedReports.stream()
                        .map(r -> new ObjectUsage(ObjectUserType.BIRT_REPORT, r.getId(), r.getShortname()))
                        .collect(Collectors.toList());

                popups.warning(new ObjectUsages(usages).toMessage("warning.target.delete.affectedBirtReports", admin.getLocale()));
            } else {
                model.addAttribute("affectedReports", affectedReports);
                model.addAttribute("affectedReportsMessageKey", "warning.target.delete.affectedBirtReports");
                model.addAttribute("affectedReportsMessageType", GuiConstants.MESSAGE_TYPE_WARNING);
            }
        }
    }

    private boolean deleteTarget(int targetId, Admin admin, Popups popups) {
        final int companyId = admin.getCompanyID();
        final String name = targetService.getTargetName(targetId, companyId);
        if (name == null) {
            logger.error("No target exists, ID: {}", targetId);
            popups.alert(TARGET_DELETE_ERROR_MSG);
            return false;
        }

        SimpleServiceResult deletionResult = targetService.deleteTargetGroup(targetId, admin);

        if (deletionResult.isSuccess()) {
            userActivityLogService.writeUserActivityLog(admin, "delete target group", String.format("%s (%d)", name, targetId));

            popups.success(SELECTION_DELETED_MSG);
            return true;
        }

        popups.addPopups(deletionResult);
        return false;
    }
}
