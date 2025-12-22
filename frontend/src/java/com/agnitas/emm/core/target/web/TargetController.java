/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.birtreport.bean.LightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsageType;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.target.form.TargetForm;
import com.agnitas.emm.core.target.form.TargetListFormSearchParams;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.SimpleActionForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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

public class TargetController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(TargetController.class);
    private static final String TARGETS_LIST_REDIRECT = "redirect:/target/list.action?restoreSort=true";
    private static final String TARGET_DELETE_ERROR_MSG = "error.target.delete";

    protected final TargetService targetService;
    private final WebStorage webStorage;
    private final BirtReportDao birtReportDao;
    private final UserActivityLogService userActivityLogService;

    public TargetController(TargetService targetService, WebStorage webStorage,
                            UserActivityLogService userActivityLogService,
                            BirtReportDao birtReportDao) {
        this.webStorage = webStorage;
        this.birtReportDao = birtReportDao;
        this.targetService = targetService;
        this.userActivityLogService = userActivityLogService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @RequestMapping("/list.action")
    @RequiredPermission("targets.show")
    public String list(@ModelAttribute TargetForm targetForm, TargetListFormSearchParams searchParams,
                       @RequestParam(required = false) Boolean restoreSort, Admin admin, Model model) {
        searchParams.restoreParams(targetForm);
        FormUtils.syncPaginationData(webStorage, WebStorage.TARGET_OVERVIEW, targetForm, restoreSort);

        addListModelAttrs(targetForm, admin, model);
        return "targets_list";
    }

    protected void addListModelAttrs(TargetForm targetForm, Admin admin, Model model) { // overridden in extended class
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        model.addAttribute("isSearchEnabled", targetService.isBasicFullTextSearchSupported());
        model.addAttribute("targetComplexities", targetService.getTargetComplexities(admin.getCompanyID()));
        model.addAttribute("targetEntries", getPaginatedTargetGroupsOverview(targetForm, admin));
    }

    @GetMapping("/search.action")
    @RequiredPermission("targets.show")
    public String search(@ModelAttribute TargetForm form, TargetListFormSearchParams searchParams, RedirectAttributes ra) {
        searchParams.storeParams(form);
        ra.addFlashAttribute("targetForm", form);
        return TARGETS_LIST_REDIRECT;
    }

    @PostMapping("/restore.action")
    @RequiredPermission("targets.delete")
    public String restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);

        targetService.restore(bulkIds, admin);
        popups.changesSaved();

        return TARGETS_LIST_REDIRECT + "&showDeleted=true";
    }

    private void validateSelectedIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BadRequestException("bulkAction.nothing.target");
        }
    }

    @RequestMapping("/confirm/bulk/delete.action")
    @RequiredPermission("targets.delete")
    public String confirmBulkDelete(@ModelAttribute("form") BulkActionForm form, Popups popups, Admin admin, Model model) {
        validateSelectedIds(form.getBulkIds());

        ServiceResult<List<String>> result = targetService.getTargetNamesForDeletion(form.getBulkIds(), admin);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        model.addAttribute("names", result.getResult());

        return "targets_bulk_delete_confirm";
    }

    @PostMapping("/bulk/delete.action")
    @RequiredPermission("targets.delete")
    public String bulkDelete(@ModelAttribute("form") BulkActionForm form, Admin admin, Popups popups) {
        List<Integer> ids = targetService.bulkDelete(new HashSet<>(form.getBulkIds()), admin);
        if (CollectionUtils.isNotEmpty(ids)) {
            popups.selectionDeleted();
            userActivityLogService.writeUserActivityLog(
                    admin,
                    "delete target group",
                    "deleted targets with following ids: " + StringUtils.join(ids, ", ")
            );
        }

        return TARGETS_LIST_REDIRECT;
    }

    @RequestMapping("{id:\\d+}/confirm/delete.action")
    @RequiredPermission("targets.delete")
    public String confirmDelete(@PathVariable int id, Admin admin, @ModelAttribute SimpleActionForm simpleActionForm, Popups popups) {
        Target targetGroup = targetService.getTargetGroupOrNull(id, admin.getCompanyID());
        if (targetGroup == null) {
            popups.alert(TARGET_DELETE_ERROR_MSG);
            return MESSAGES_VIEW;
        }

        simpleActionForm.setId(targetGroup.getId());
        simpleActionForm.setShortname(targetGroup.getTargetName());

        loadDependentBirtReports(id, popups, admin);

        final SimpleServiceResult canBeDeletedResult = targetService.canBeDeleted(id, admin);
        if (canBeDeletedResult.isSuccess()) {
            return "targets_delete_confirm";
        }
        popups.addPopups(canBeDeletedResult);
        return MESSAGES_VIEW;
    }

    @PostMapping("/delete.action")
    @RequiredPermission("targets.delete")
    public String delete(Admin admin, @ModelAttribute SimpleActionForm simpleActionForm, Popups popups) {
        if (deleteTarget(simpleActionForm.getId(), admin, popups)) {
            return TARGETS_LIST_REDIRECT;
        }

        return MESSAGES_VIEW;
    }

    @RequestMapping("/{id:\\d+}/confirm/delete/recipients.action")
    @RequiredPermission("recipient.delete")
    public String confirmDeleteRecipients(Model model, @PathVariable int id, Admin admin, Popups popups) {
        final Optional<Integer> numOfRecipientsOpt = targetService.getNumberOfRecipients(id, admin.getCompanyID());
        if (numOfRecipientsOpt.isEmpty()) {
            logger.warn("It is not possible to delete recipients for target with id: {}", id);
            popups.defaultError();
            return MESSAGES_VIEW;
        }
        model.addAttribute("numberOfRecipients", numOfRecipientsOpt.get());
        model.addAttribute("targetIdToDeleteRecipients", id);
        return "targets_delete_recipients_confirm";
    }

    @PostMapping("/{id:\\d+}/delete/recipients.action")
    @RequiredPermission("recipient.delete")
    public String deleteRecipients(Admin admin, @PathVariable int id, Popups popups) {
        final boolean deletedSuccessfully = targetService.deleteRecipients(id, admin.getCompanyID());

        if (!deletedSuccessfully) {
            logger.warn("Could not delete recipients for target with id: {}", id);
            popups.defaultError();
            return MESSAGES_VIEW;
        }

        popups.changesSaved();
        userActivityLogService.writeUserActivityLog(admin, "edit target group",
                "All recipients deleted from target group with id=" + id);
        return TARGETS_LIST_REDIRECT;
    }

    @PostMapping("/{id:\\d+}/addToFavorites.action")
    @RequiredPermission("targets.show")
    public ResponseEntity<Object> addToFavorites(@PathVariable int id, Admin admin) {
        try {
            targetService.addToFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't add target group [id = %d] to favorites. %s", id, e.getMessage()), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id:\\d+}/removeFromFavorites.action")
    @RequiredPermission("targets.show")
    public ResponseEntity<Object> removeFromFavorites(@PathVariable int id, Admin admin) {
        try {
            targetService.removeFromFavorites(id, admin.getCompanyID());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(String.format("Can't remove target group [id = %d] from favorites. %s.", id, e.getMessage()), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private PaginatedList<TargetLight> getPaginatedTargetGroupsOverview(TargetForm form, Admin admin) {
        return targetService.getTargetLightsPaginated(
                getOverviewTargetLightsOptionsBuilder(admin, form).build(),
                form.getSearchComplexity()
        );
    }

    protected TargetLightsOptions.Builder getOverviewTargetLightsOptionsBuilder(Admin admin, TargetForm form) {
        return TargetLightsOptions.builder()
                .setAdminId(admin.getAdminID())
                .setCompanyId(admin.getCompanyID())
                .setDeliveryOption(form.getSearchDeliveryOption())
                .setSearchName(form.getSearchName())
                .setSearchDescription(form.getSearchDescription())
                .setCreationDate(form.getSearchCreationDate())
                .setChangeDate(form.getSearchChangeDate())
                .setDeleted(form.isShowDeleted())
                .setPageNumber(form.getPage())
                .setPageSize(form.getNumberOfRows())
                .setDirection(form.getDir())
                .setSorting(form.getSort());
    }

    private void loadDependentBirtReports(int targetId, Popups popups, Admin admin) {
        final List<LightweightBirtReport> affectedReports = birtReportDao.getLightweightBirtReportsBySelectedTarget(admin.getCompanyID(), targetId);
        if (CollectionUtils.isNotEmpty(affectedReports)) {
            List<ObjectUsage> usages = affectedReports.stream()
                    .map(r -> new ObjectUsage(ObjectUsageType.BIRT_REPORT, r.getId(), r.getShortname()))
                    .toList();

            popups.warning(new ObjectUsages(usages).toMessage("warning.target.delete.affectedBirtReports", admin.getLocale()));
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

            popups.selectionDeleted();
            return true;
        }

        popups.addPopups(deletionResult);
        return false;
    }

}
