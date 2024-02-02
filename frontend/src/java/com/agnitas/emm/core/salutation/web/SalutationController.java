/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.web;

import static com.agnitas.web.mvc.Pollable.DEFAULT_TIMEOUT;

import java.util.concurrent.Callable;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.TitleImpl;
import com.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.salutation.form.SalutationForm;
import com.agnitas.emm.core.salutation.service.SalutationService;
import com.agnitas.messages.I18nString;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/salutation/")
public class SalutationController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(SalutationController.class);

    private static final String MESSAGES_VIEW = "messages";
    private static final String ERROR_MSG_KEY = "Error";
    private static final String PERMISSION_ERROR_MSG_KEY = "error.salutation.change.permission";
    private static final String REDIRECT_TO_LIST = "redirect:/salutation/list.action";

    private final SalutationService salutationService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;

    public SalutationController(SalutationService salutationService, WebStorage webStorage,
                                ConversionService conversionService) {
        this.salutationService = salutationService;
        this.conversionService = conversionService;
        this.webStorage = webStorage;
    }

    @RequestMapping(value = "/list.action")
    public Pollable<ModelAndView> list(@ModelAttribute("form") SalutationForm form, Admin admin, Model model, HttpSession session) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.SALUTATION_OVERVIEW, form);

        return new Pollable<>(getPollingUid(form, session), DEFAULT_TIMEOUT,
                new ModelAndView(REDIRECT_TO_LIST, model.asMap()),
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

    @RequestMapping("{id:\\d+}/view.action")
    public String view(@PathVariable int id, Admin admin, Model model, Popups popups) {
        Title title = salutationService.get(id, admin.getCompanyID());
        if (title == null) {
            logger.error("Could not delete salutation id: {} not exist", id);
            popups.alert(ERROR_MSG_KEY);
            return MESSAGES_VIEW;
        }
        model.addAttribute("salutationCompanyId", title.getCompanyID());
        model.addAttribute("form", conversionService.convert(title, SalutationForm.class));
        return "salutation_view";
    }

    @PostMapping("/{salutationId:\\d+}/save.action")
    public String save(@PathVariable int salutationId, Admin admin, SalutationForm form, Popups popups) throws Exception {
        Title salutationToSave = getSalutationToSave(form, salutationId, admin.getCompanyID());
        if (!isAllowedToChange(salutationToSave, admin.getCompanyID(), popups)) {
            return MESSAGES_VIEW;
        }

        salutationService.save(salutationToSave);
        popups.success("default.changes_saved");
        return REDIRECT_TO_LIST;
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
            popups.alert(ERROR_MSG_KEY);
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
    public String create(@ModelAttribute("form") SalutationForm form, Admin admin) {
        form.setDescription(I18nString.getLocaleString("default.salutation.shortname", admin.getLocale()));
        return "salutation_view";
    }

    @GetMapping("/{salutationId:\\d+}/confirmDelete.action")
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
    public String delete(@PathVariable int salutationId, Admin admin, Popups popups) {
        deleteSalutation(salutationId, admin.getCompanyID(), popups);
        if (popups.hasAlertPopups()) {
            return MESSAGES_VIEW;
        }
        return REDIRECT_TO_LIST;
    }

    private void deleteSalutation(int salutationId, int companyId, Popups popups) {
        Title title = salutationService.get(salutationId, companyId);
        if (title != null) {
            if (title.getCompanyID() == companyId) {
                if (salutationService.delete(salutationId, companyId)) {
                    popups.success("default.selection.deleted");
                } else {
                    logger.error("Could not delete salutation: {}", salutationId);
                    popups.alert(ERROR_MSG_KEY);
                }
            } else {
                popups.alert(PERMISSION_ERROR_MSG_KEY);
            }
        } else {
            logger.error("Could not delete salutation id: {} not exist", salutationId);
            popups.alert(ERROR_MSG_KEY);
        }
    }
}
