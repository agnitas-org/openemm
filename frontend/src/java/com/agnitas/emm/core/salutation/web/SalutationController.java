/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.web;

import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.web.mvc.Pollable.DEFAULT_TIMEOUT;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.Title;
import com.agnitas.beans.impl.TitleImpl;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.salutation.form.SalutationForm;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import com.agnitas.emm.core.salutation.form.SalutationSearchParams;
import com.agnitas.emm.core.salutation.service.SalutationService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.exception.BadRequestException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/list.action")
    @RequiredPermission("salutation.show")
    public Pollable<ModelAndView> list(@RequestParam(required = false) Boolean restoreSort,
                                                 SalutationOverviewFilter filter,
                                                 Admin admin, Model model, HttpSession session) {
        FormUtils.syncPaginationData(webStorage, WebStorage.SALUTATION_OVERVIEW, filter, restoreSort);
        filter.setCompanyId(admin.getCompanyID());
        return new Pollable<>(getPollingUid(filter, session), DEFAULT_TIMEOUT,
            new ModelAndView(REDIRECT_TO_LIST, model.asMap()),
            getListWorker(filter, model));
    }

    private Callable<ModelAndView> getListWorker(SalutationOverviewFilter filter, Model model) {
        return () -> {
            model.addAttribute("salutations", salutationService.overview(filter));
            return new ModelAndView("salutation_list", model.asMap());
        };
    }

    private PollingUid getPollingUid(SalutationOverviewFilter filter, HttpSession session) {
        return PollingUid.builder(session.getId(), "salutations")
            .arguments(filter.getSort(), filter.getOrder(), filter.getPage(), filter.getNumberOfRows())
            .build();
    }

    @GetMapping("/search.action")
    @RequiredPermission("salutation.show")
    public String search(SalutationOverviewFilter form, SalutationSearchParams searchParams, RedirectAttributes ra) {
        searchParams.storeParams(form);
        ra.addFlashAttribute("salutationOverviewFilter", form);
        return "redirect:/salutation/list.action?restoreSort=true";
    }

    @RequestMapping("{id:\\d+}/view.action")
    @RequiredPermission("salutation.show")
    public String view(@PathVariable int id, Admin admin, Model model, Popups popups) {
        Title title = salutationService.get(id, admin.getCompanyID());
        if (title == null) {
            logger.error("Could not delete salutation id: {} not exist", id);
            popups.defaultError();
            return MESSAGES_VIEW;
        }

        model.addAttribute("salutationCompanyId", title.getCompanyID());
        model.addAttribute("form", conversionService.convert(title, SalutationForm.class));
        model.addAttribute("recipients", recipientService.getAdminAndTestRecipientsSalutation(admin));

        return "salutation_view";
    }

    @PostMapping("/{salutationId:\\d+}/save.action")
    @RequiredPermission("salutation.change")
    public String save(@PathVariable int salutationId, Admin admin, SalutationForm form, Popups popups) {
        Title salutationToSave = getSalutationToSave(form, salutationId, admin.getCompanyID());
        if (!isAllowedToChange(salutationToSave, admin.getCompanyID(), popups)) {
            return MESSAGES_VIEW;
        }

        salutationService.save(salutationToSave);
        popups.changesSaved();
        return redirectToListWithRestoreSort();
    }

    private static String redirectToListWithRestoreSort() {
        return REDIRECT_TO_LIST + "?restoreSort=true";
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
            popups.defaultError();
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
    @RequiredPermission("salutation.change")
    public String create(@ModelAttribute("form") SalutationForm form, Admin admin, Model model) {
        form.setDescription(I18nString.getLocaleString("default.salutation.shortname", admin.getLocale()));
        model.addAttribute("recipients", recipientService.getAdminAndTestRecipientsSalutation(admin));
        return "salutation_view";
    }

    @GetMapping(value = "/delete.action")
    @RequiredPermission("salutation.delete")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
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

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("salutation.delete")
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateDeletion(bulkIds);
        ServiceResult<UserAction> result = salutationService.bulkDelete(bulkIds, admin.getCompanyID());

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return redirectToListWithRestoreSort();
    }

    private void validateDeletion(Set<Integer> bulkIds) {
        if (CollectionUtils.isEmpty(bulkIds)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    @GetMapping(value = "/list/json.action")
    @RequiredPermission("salutation.show")
    public ResponseEntity<List<Title>> getListAsJson(Admin admin) {
        return ResponseEntity.ok(salutationService.getAll(admin.getCompanyID(), true));
    }

    @GetMapping(value = "/{salutationId:\\d+}/resolve.action")
    @RequiredPermission("salutation.show")
    public ResponseEntity<String> resolve(@PathVariable int salutationId,
                                          @RequestParam int recipientId,
                                          @RequestParam int type, Admin admin) {
        return ResponseEntity.ok(salutationService.resolve(salutationId, recipientId, type, admin));
    }

}
