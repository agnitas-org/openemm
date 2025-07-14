/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.web;

import static com.agnitas.web.mvc.Pollable.DEFAULT_TIMEOUT;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.salutation.form.SalutationForm;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import com.agnitas.emm.core.salutation.form.SalutationSearchParams;
import com.agnitas.emm.core.salutation.service.SalutationService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.Title;
import com.agnitas.beans.impl.TitleImpl;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/salutation/")
public class SalutationController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(SalutationController.class);

    private static final String PERMISSION_ERROR_MSG_KEY = "error.salutation.change.permission";
    private static final String REDIRECT_TO_LIST = "redirect:/salutation/list.action";
    private static final String REDIRECT_TO_LIST_REDESIGNED = "redirect:/salutation/listRedesigned.action";

    private final SalutationService salutationService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final RecipientService recipientService;

    public SalutationController(SalutationService salutationService, WebStorage webStorage, ConversionService conversionService,
                                UserActivityLogService userActivityLogService, RecipientService recipientService) {
        this.salutationService = salutationService;
        this.conversionService = conversionService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.recipientService = recipientService;
    }

    @RequestMapping(value = "/list.action")
    // TODO: EMMGUI-714: remove when removing old design
    public Pollable<ModelAndView> list(@ModelAttribute("form") SalutationForm form, @RequestParam(required = false) Boolean restoreSort,
                                       Admin admin, Model model, HttpSession session) {
        FormUtils.syncPaginationData(webStorage, WebStorage.SALUTATION_OVERVIEW, form, restoreSort);

        return new Pollable<>(getPollingUid(form, session), DEFAULT_TIMEOUT,
                new ModelAndView(redirectToList(admin), model.asMap()),
                getListWorker(admin.getCompanyID(), form, model));
    }

    private Callable<ModelAndView> getListWorker(int companyId, SalutationForm form, Model model) {
        return () -> {
            model.addAttribute("salutations", salutationService.paginatedList(
                    companyId, form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows()));
            return new ModelAndView("salutation_list", model.asMap());
        };
    }

    private PollingUid getPollingUid(SalutationForm form, HttpSession session) {
        return PollingUid.builder(session.getId(), "salutations")
                .arguments(form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows())
                .build();
    }

    @GetMapping("/listRedesigned.action")
    public Pollable<ModelAndView> listRedesigned(@RequestParam(required = false) Boolean restoreSort,
                                                 SalutationOverviewFilter filter,
                                                 Admin admin, Model model, HttpSession session) {
        FormUtils.syncPaginationData(webStorage, WebStorage.SALUTATION_OVERVIEW, filter, restoreSort);
        filter.setCompanyId(admin.getCompanyID());
        return new Pollable<>(getPollingUidRedesigned(filter, session), DEFAULT_TIMEOUT,
            new ModelAndView(redirectToList(admin), model.asMap()),
            getListWorker(filter, model));
    }

    private static String redirectToList(Admin admin) {
        return admin.isRedesignedUiUsed() ? REDIRECT_TO_LIST_REDESIGNED : REDIRECT_TO_LIST;
    }

    private Callable<ModelAndView> getListWorker(SalutationOverviewFilter filter, Model model) {
        return () -> {
            model.addAttribute("salutations", salutationService.overview(filter));
            return new ModelAndView("salutation_list", model.asMap());
        };
    }

    private PollingUid getPollingUidRedesigned(SalutationOverviewFilter filter, HttpSession session) {
        return PollingUid.builder(session.getId(), "salutations")
            .arguments(filter.getSort(), filter.getOrder(), filter.getPage(), filter.getNumberOfRows())
            .build();
    }

    @GetMapping("/search.action")
    public String search(SalutationOverviewFilter form, SalutationSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, form, false);
        ra.addFlashAttribute("salutationOverviewFilter", form);
        return "redirect:/salutation/listRedesigned.action?restoreSort=true";
    }

    @RequestMapping("{id:\\d+}/view.action")
    public String view(@PathVariable int id, Admin admin, Model model, Popups popups) {
        Title title = salutationService.get(id, admin.getCompanyID());
        if (title == null) {
            logger.error("Could not delete salutation id: {} not exist", id);
            popups.alert(ERROR_MSG);
            return MESSAGES_VIEW;
        }
        model.addAttribute("salutationCompanyId", title.getCompanyID());
        model.addAttribute("form", conversionService.convert(title, SalutationForm.class));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("recipients", recipientService.getAdminAndTestRecipientsSalutation(admin));
        }
        return "salutation_view";
    }

    @PostMapping("/{salutationId:\\d+}/save.action")
    public String save(@PathVariable int salutationId, Admin admin, SalutationForm form, Popups popups) {
        Title salutationToSave = getSalutationToSave(form, salutationId, admin.getCompanyID());
        if (!isAllowedToChange(salutationToSave, admin.getCompanyID(), popups)) {
            return MESSAGES_VIEW;
        }

        salutationService.save(salutationToSave);
        popups.success(CHANGES_SAVED_MSG);
        return redirectToListWithRestoreSort(admin);
    }

    private static String redirectToListWithRestoreSort(Admin admin) {
        return redirectToList(admin) + "?restoreSort=true";
    }

    private Title getSalutationToSave(SalutationForm form, int salutationId, int companyId) {
        Title salutation = salutationId == 0
                ? getNewTitle(companyId)
                : salutationService.get(salutationId, companyId);
        form.getGenderMapping().entrySet().removeIf(mapping -> mapping.getValue().isEmpty());
        salutation.setDescription(form.getDescription());
        salutation.setTitleGender(form.getGenderMapping());
        return salutation;
    }

    private Title getNewTitle(int companyId) {
        Title salutation;
        salutation = new TitleImpl();
        salutation.setCompanyID(companyId);
        return salutation;
    }

    private boolean isAllowedToChange(Title title, int companyId, Popups popups) {
        if (title == null) {
            popups.alert(ERROR_MSG);
            return false;
        }
        if (title.getCompanyID() == 0 && companyId != 1) {
            popups.alert(PERMISSION_ERROR_MSG_KEY);
            return false;
        }
        
        String titleDesc = title.getDescription();
        
        if (StringUtils.trimToNull(titleDesc) == null) {
            popups.alert("error.name.is.empty");
            return false;
        }

        if (StringUtils.trimToNull(titleDesc).length() < 3) {
            popups.alert("error.name.too.short");
            return false;
        }
        
        return true;
    }

    @RequestMapping(value = {"/create.action", "/0/view.action"})
    public String create(@ModelAttribute("form") SalutationForm form, Admin admin, Model model) {
        form.setDescription(I18nString.getLocaleString("default.salutation.shortname", admin.getLocale()));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("recipients", recipientService.getAdminAndTestRecipientsSalutation(admin));
        }
        return "salutation_view";
    }

    @GetMapping(value = "/deleteRedesigned.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateDeletion(bulkIds);

        ServiceResult<List<Title>> result = salutationService.getAllowedForDeletion(bulkIds, admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(Title::getDescription).toList(),
                "salutation.delete", "salutation.delete.question",
                "salutation.delete", "bulkAction.delete.salutation.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateDeletion(bulkIds);
        ServiceResult<UserAction> result = salutationService.bulkDelete(bulkIds, admin.getCompanyID());

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return redirectToListWithRestoreSort(admin);
    }

    private void validateDeletion(Set<Integer> bulkIds) {
        if (CollectionUtils.isEmpty(bulkIds)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    @GetMapping("/{salutationId:\\d+}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String confirmDelete(@PathVariable int salutationId, Admin admin, Model model, Popups popups) {
        Title title = salutationService.get(salutationId, admin.getCompanyID());
        if (title == null || (title.getCompanyID() < 1 && admin.getCompanyID() != 1)) {
            popups.alert(PERMISSION_ERROR_MSG_KEY);
            return MESSAGES_VIEW;
        }
        model.addAttribute("id", salutationId);
        model.addAttribute("shortname", title.getDescription());
        return "salutation_delete";
    }

    @PostMapping("{salutationId:\\d+}/delete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String delete(@PathVariable int salutationId, Admin admin, Popups popups) {
        deleteSalutation(salutationId, admin.getCompanyID(), popups);
        if (popups.hasAlertPopups()) {
            return MESSAGES_VIEW;
        }
        return redirectToListWithRestoreSort(admin);
    }

    // TODO: EMMGUI-714: remove when old design will be removed
    private void deleteSalutation(int salutationId, int companyId, Popups popups) {
        Title title = salutationService.get(salutationId, companyId);
        if (title != null) {
            if (title.getCompanyID() == companyId) {
                if (salutationService.delete(salutationId, companyId)) {
                    popups.success("default.selection.deleted");
                } else {
                    logger.error("Could not delete salutation: {}", salutationId);
                    popups.alert(ERROR_MSG);
                }
            } else {
                popups.alert(PERMISSION_ERROR_MSG_KEY);
            }
        } else {
            logger.error("Could not delete salutation id: {} not exist", salutationId);
            popups.alert(ERROR_MSG);
        }
    }

    @GetMapping(value = "/list/json.action")
    public ResponseEntity<List<Title>> getListAsJson(Admin admin) {
        return ResponseEntity.ok(salutationService.getAll(admin.getCompanyID(), true));
    }

    @GetMapping(value = "/{salutationId:\\d+}/resolve.action")
    public ResponseEntity<String> resolve(@PathVariable int salutationId,
                                          @RequestParam int recipientId,
                                          @RequestParam int type, Admin admin) {
        return ResponseEntity.ok(salutationService.resolve(salutationId, recipientId, type, admin));
    }
}
