/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterForm;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.emm.core.bounce.form.validation.BounceFilterFormValidator;
import com.agnitas.emm.core.bounce.form.validation.BounceFilterSearchParams;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedAutoResponderEmailException;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedFilterEmailException;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedForwardEmailException;
import com.agnitas.emm.core.bounce.service.impl.EmailInUseException;
import com.agnitas.emm.core.bounce.util.BounceUtils;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpSession;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
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

import java.net.IDN;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.agnitas.web.mvc.Pollable.DEFAULT_TIMEOUT;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

public class BounceFilterController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(BounceFilterController.class);
    private static final String REDIRECT_TO_OVERVIEW = "redirect:/administration/bounce/list.action";

    private static final String MAILING_LISTS = "mailingLists";
    private static final String USER_FORM_LIST = "userFormList";
    private static final String BOUNCE_FILTER_FORM = "bounceFilterForm";
    private static final String ACTIONBASED_MAILINGS = "actionBasedMailings";
    private static final String FILTER_EMAIL_ADDRESS_DEFAULT = "filterEmailAddressDefault";
    private static final String IS_ALLOWED_MAILLOOP_DOMAIN = "isAllowedMailloopDomain";

    private final BounceFilterService bounceFilterService;
    private final MailingBaseService mailingService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UserformService userFormService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;

    private final BounceFilterFormValidator bounceFilterFormValidator = new BounceFilterFormValidator();

    public BounceFilterController(@Qualifier("BounceFilterService") BounceFilterService bounceFilterService, @Qualifier("MailingBaseService") MailingBaseService mailingService,
                                  final MailinglistApprovalService mailinglistApprovalService,
                                  UserformService userFormService, ConversionService conversionService,
                                  WebStorage webStorage,
                                  UserActivityLogService userActivityLogService) {
        this.bounceFilterService = bounceFilterService;
        this.mailingService = mailingService;
        this.userFormService = userFormService;
        this.conversionService = conversionService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.mailinglistApprovalService = mailinglistApprovalService;
    }

    @RequestMapping(value = "/list.action")
    public Pollable<ModelAndView> list(Admin admin, HttpSession session, BounceFilterListForm form, BounceFilterSearchParams searchParams,
                                       @RequestParam(required = false) Boolean restoreSort, Model model) {
        if (admin.isRedesignedUiUsed()) {
            form.setCompanyId(admin.getCompanyID());
            form.setCompanyDomain(admin.getCompany().getMailloopDomain());
            FormUtils.syncSearchParams(searchParams, form, true);
        }
        FormUtils.syncPaginationData(webStorage, WebStorage.BOUNCE_FILTER_OVERVIEW, form, restoreSort);

        PollingUid uid = PollingUid.builder(session.getId(), "bounceFilterList")
            .arguments(form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows())
            .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute("bounceFilterList", admin.isRedesignedUiUsed()
                    ? bounceFilterService.overview(form)
                    : bounceFilterService.getPaginatedBounceFilterList(
                            admin,
                            form.getSort(),
                            form.getOrder(),
                            form.getPage(),
                            form.getNumberOfRows()));

            writeUserActivityLog(admin, "bounce filter list", "active submenu item - overview");

            return new ModelAndView("bounce_filter_list", model.asMap());
        };

        return new Pollable<>(uid, DEFAULT_TIMEOUT, new ModelAndView(REDIRECT_TO_OVERVIEW, model.asMap()), worker);
    }

    @GetMapping("/search.action")
    public String search(BounceFilterListForm listForm, BounceFilterSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, listForm, false);
        ra.addFlashAttribute("bounceFilterListForm", listForm);
        return REDIRECT_TO_OVERVIEW + "?restoreSort=true";
    }

    @GetMapping(value = "/{id:\\d+}/view.action")
    public String view(@PathVariable int id, Model model, Admin admin,
                       @RequestParam(required = false, defaultValue = "0") int forAddress) {
        if (id <= 0) {
            return "redirect:/administration/bounce/new.action";
        }
        int companyId = admin.getCompanyID();

        BounceFilterForm form = loadBounceFilter(companyId, id, model);
        loadAdditionalFormData(admin, model);

        setFilterEmailAttributes(admin, model, id);

        writeUserActivityLog(admin, "view bounce filter", getBounceFilterDescription(form));
        model.addAttribute("forAddress", forAddress);

        return "bounce_filter_view";
    }

    @GetMapping(value = "/new.action")
    public String create(@ModelAttribute BounceFilterForm form, Admin admin,  Model model,
                         @RequestParam(required = false, defaultValue = "0") int forAddress) {
        loadAdditionalFormData(admin, model);
        setFilterEmailAttributes(admin, model, 0);
        model.addAttribute("forAddress", forAddress);
        return "bounce_filter_view";
    }

    @PostMapping(value = "/save.action")
    public String save(@ModelAttribute BounceFilterForm form, Admin admin, Popups popups,
                       @RequestParam(required = false, defaultValue = "0") int forAddress) {
        if (isValid(admin, form, popups)) {
            BounceFilterDto bounceFilter = conversionService.convert(form, BounceFilterDto.class);
            try {
                boolean isNew = form.getId() <= 0;
                int id = bounceFilterService.saveBounceFilter(admin, bounceFilter, isNew);

                if (id > 0) {
                    popups.success("default.changes_saved");
                    writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "bounce filter", getBounceFilterDescription(id, form.getShortName()));
                } else {
                    throw new Exception("Could not create bounce filter, returned ID is 0");
                }
                return redirectAfterSave(id, forAddress, admin);
            } catch (BlacklistedFilterEmailException e) {
                logger.error("Could not save bounce filter!", e);
                popups.alert("error.blacklistedFilterEmail");
            } catch (BlacklistedForwardEmailException e) {
                logger.error("Could not save bounce filter!", e);
                popups.alert("error.blacklistedForwardEmail");
            } catch (BlacklistedAutoResponderEmailException e) {
                logger.error("Could not save bounce filter!", e);
                popups.alert("error.blacklistedAutoresponderEmail");
            } catch (EmailInUseException e) {
                logger.error("Could not save bounce filter!", e);
                popups.alert("error.email.used");
            } catch (Exception e) {
                logger.error("Could not save bounce filter!", e);
                popups.alert("Error");
            }
        }

        return "messages";
    }

    protected String redirectAfterSave(int filterId, int forAddress, Admin admin) {
        return admin.isRedesignedUiUsed()
                ? REDIRECT_TO_OVERVIEW + "?restoreSort=true"
                : "redirect:/administration/bounce/" + filterId + "/view.action";
    }

    @GetMapping(value = "/{id:\\d+}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String confirmDelete(Admin admin, @PathVariable int id, Popups popups, Model model) {
        ServiceResult<List<BounceFilterDto>> result = bounceFilterService.getAllowedForDeletion(Set.of(id), admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        loadBounceFilter(admin.getCompanyID(), id, model);
        model.addAttribute("containsReplies", bounceFilterService.containsReply(id));
        return "bounce_filter_delete_ajax";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    // TODO: EMMGUI-714: remove when old design will be removed
    public String delete(Admin admin, @ModelAttribute("bounceFilterForm") BounceFilterForm form, Popups popups) {
        int id = form.getId();
        if (id <= 0) {
            popups.alert("Error");
            return REDIRECT_TO_OVERVIEW + "?restoreSort=true";
        }

        if(bounceFilterService.deleteBounceFilter(id, admin.getCompanyID())){
            writeUserActivityLog(admin, "delete bounce filter", getBounceFilterDescription(form));
            popups.success("default.selection.deleted");
        } else {
            popups.alert("Error");
        }
        return REDIRECT_TO_OVERVIEW + "?restoreSort=true";
    }

    @GetMapping(value = "/deleteRedesigned.action")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups, Model model) {
        validateSelectedIds(bulkIds);

        ServiceResult<List<BounceFilterDto>> result = bounceFilterService.getAllowedForDeletion(bulkIds, admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        String singleDeletionQuestion = bulkIds.size() == 1 && bounceFilterService.containsReply(bulkIds.iterator().next())
                ? "mailloop.delete.inbox.question" : "settings.mailloop.delete.question";

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(BounceFilterDto::getShortName).toList(),
                "mailloop.mailloopDelete", singleDeletionQuestion,
                "mailloop.mailloopDelete", "bulkAction.settings.mailloop.delete");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);

        ServiceResult<UserAction> result = bounceFilterService.delete(bulkIds, admin.getCompanyID());
        popups.addPopups(result);
        writeUserActivityLog(result.getResult(), admin);

        return REDIRECT_TO_OVERVIEW + "?restoreSort=true";
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    private BounceFilterForm loadBounceFilter(int companyId, int id, Model model) {
        BounceFilterForm form = (BounceFilterForm) model.asMap().get(BOUNCE_FILTER_FORM);
        if(id > 0) {
            form = conversionService.convert(bounceFilterService.getBounceFilter(companyId, id), BounceFilterForm.class);
            model.addAttribute(BOUNCE_FILTER_FORM, form);
        }
        return form;
    }

    private void loadAdditionalFormData(Admin admin, Model model){
        model.addAttribute(MAILING_LISTS, mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        model.addAttribute(USER_FORM_LIST, userFormService.getUserForms(admin.getCompanyID()));
        model.addAttribute(ACTIONBASED_MAILINGS, mailingService.getMailingsByType(MailingType.ACTION_BASED, admin.getCompanyID(), false));
    }

    private boolean isValid(Admin admin, BounceFilterForm form, Popups popups){
        boolean success = true;
        final String companyMailloopDomain = admin.getCompany().getMailloopDomain();
        if(!isAllowedMailloopDomain(companyMailloopDomain) || form.isOwnForwardEmailSelected()) {
            if(StringUtils.isBlank(form.getFilterEmail())) {
                popups.fieldError("filterEmail", "error.mailloop.address.empty");
                success = false;
            }
        }
        success &= bounceFilterFormValidator.validate(form, popups);
        return success;
    }

    private boolean isAllowedMailloopDomain(String mailloopDomain){
    	if(mailloopDomain != null) {
    		try {
    			IDN.toASCII(mailloopDomain);
    			return true;
    		} catch(final IllegalArgumentException e) {
    			// Does not conform to a RFC3490 domain name

    			return false;
    		}
    	}

    	return false;
    }

    private void setFilterEmailAttributes(Admin admin, Model model, int id) {
        final String companyMailloopDomain = admin.getCompany().getMailloopDomain();
        boolean isAllowedMailloopDomain = isAllowedMailloopDomain(companyMailloopDomain);
        model.addAttribute(IS_ALLOWED_MAILLOOP_DOMAIN, isAllowedMailloopDomain);

        if(id > 0 && isAllowedMailloopDomain) {
            model.addAttribute(FILTER_EMAIL_ADDRESS_DEFAULT, BounceUtils.getFilterEmailDefault(companyMailloopDomain, id));
        }
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }

    private void writeUserActivityLog(UserAction userAction, Admin admin) {
        userActivityLogService.writeUserActivityLog(admin, userAction);
    }

    private String getBounceFilterDescription(BounceFilterForm form) {
        return getBounceFilterDescription(form.getId(), form.getShortName());
    }

    private String getBounceFilterDescription(int filterId, String shortname) {
        return String.format("%s (%d)", shortname, filterId);
    }
}
