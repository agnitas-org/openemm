/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.web;

import static com.agnitas.web.mvc.Pollable.DEFAULT_TIMEOUT;

import java.net.IDN;
import java.util.concurrent.Callable;

import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterForm;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.emm.core.bounce.form.validation.BounceFilterFormValidator;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedAutoResponderEmailException;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedFilterEmailException;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedForwardEmailException;
import com.agnitas.emm.core.bounce.service.impl.EmailInUseException;
import com.agnitas.emm.core.bounce.util.BounceUtils;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@PermissionMapping("bounce.filter")
@RequestMapping("/administration/bounce")
public class BounceFilterController implements XssCheckAware {
    
	/** The logger. */
    private static final Logger logger = LogManager.getLogger(BounceFilterController.class);
    
    private static final String MAILING_LISTS = "mailingLists";
    private static final String USER_FORM_LIST = "userFormList";
    private static final String BOUNCE_FILTER_FORM = "bounceFilterForm";
    private static final String ACTIONBASED_MAILINGS = "actionBasedMailings";
    private static final String FILTER_EMAIL_ADDRESS_DEFAULT = "filterEmailAddressDefault";
    private static final String IS_ALLOWED_MAILLOOP_DOMAIN = "isAllowedMailloopDomain";

    private final BounceFilterService bounceFilterService;
    private final ComMailingBaseService mailingService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final ComUserformService userFormService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;

    private final BounceFilterFormValidator bounceFilterFormValidator = new BounceFilterFormValidator();


    public BounceFilterController(@Qualifier("BounceFilterService") BounceFilterService bounceFilterService, @Qualifier("MailingBaseService") ComMailingBaseService mailingService,
                                  final MailinglistApprovalService mailinglistApprovalService,
                                  ComUserformService userFormService, ConversionService conversionService,
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
    public Pollable<ModelAndView> list(ComAdmin admin, HttpSession session, BounceFilterListForm form, Model model) {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.BOUNCE_FILTER_OVERVIEW, form);

        PollingUid uid = PollingUid.builder(session.getId(), "bounceFilterList")
            .arguments(form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows())
            .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute("bounceFilterList",
                    bounceFilterService.getPaginatedBounceFilterList(
                            admin,
                            form.getSort(),
                            form.getOrder(),
                            form.getPage(),
                            form.getNumberOfRows()));

            writeUserActivityLog(admin, "bounce filter list", "active submenu item - overview");

            return new ModelAndView("bounce_filter_list", model.asMap());
        };

        return new Pollable<>(uid, DEFAULT_TIMEOUT, new ModelAndView("redirect:/administration/bounce/list.action", model.asMap()), worker);
    }

    @GetMapping(value = "/{id:\\d+}/view.action")
    public String view(ComAdmin admin, @PathVariable int id, Model model) {
        if (id <= 0) {
            return "redirect:/administration/bounce/new.action";
        }
        int companyId = admin.getCompanyID();

        BounceFilterForm form = loadBounceFilter(companyId, id, model);
        loadAdditionalFormData(admin, model);

        setFilterEmailAttributes(admin, model, id);

        writeUserActivityLog(admin, "view bounce filter", getBounceFilterDescription(form));

        return "bounce_filter_view";
    }

    @GetMapping(value = "/new.action")
    public String create(ComAdmin admin, @ModelAttribute BounceFilterForm form, Model model) {
        loadAdditionalFormData(admin, model);
        setFilterEmailAttributes(admin, model, 0);
        return "bounce_filter_view";
    }

    @PostMapping(value = "/save.action")
    public String save(ComAdmin admin, @ModelAttribute BounceFilterForm form, Popups popups) throws Exception {
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
        
                return "redirect:/administration/bounce/" + id + "/view.action";
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

    @GetMapping(value = "/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(ComAdmin admin, @PathVariable int id, Model model) {
        loadBounceFilter(admin.getCompanyID(), id, model);
        return "bounce_filter_delete_ajax";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(ComAdmin admin, BounceFilterForm form, Popups popups) {
        int id = form.getId();
        if(id > 0 && bounceFilterService.deleteBounceFilter(id, admin.getCompanyID())){
            writeUserActivityLog(admin, "delete bounce filter", getBounceFilterDescription(form));
            popups.success("default.selection.deleted");
        } else {
            popups.alert("Error");
        }
        return "redirect:/administration/bounce/list.action";
    }

    private BounceFilterForm loadBounceFilter(int companyId, int id, Model model) {
        BounceFilterForm form = (BounceFilterForm) model.asMap().get(BOUNCE_FILTER_FORM);
        if(id > 0) {
            form = conversionService.convert(bounceFilterService.getBounceFilter(companyId, id), BounceFilterForm.class);
            model.addAttribute(BOUNCE_FILTER_FORM, form);
        }
        return form;
    }

    private void loadAdditionalFormData(ComAdmin admin, Model model){
        model.addAttribute(MAILING_LISTS, mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        model.addAttribute(USER_FORM_LIST, userFormService.getUserForms(admin.getCompanyID()));
        model.addAttribute(ACTIONBASED_MAILINGS, mailingService.getMailingsByType(MailingType.ACTION_BASED, admin.getCompanyID(), false));
    }

    private boolean isValid(ComAdmin admin, BounceFilterForm form, Popups popups){
        boolean success = true;
        final String companyMailloopDomain = admin.getCompany().getMailloopDomain();
        if(!isAllowedMailloopDomain(companyMailloopDomain) || form.isOwnForwardEmailSelected()) {
            if(StringUtils.isBlank(form.getFilterEmail())) {
                popups.field("filterEmail", "error.mailloop.address.empty");
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

    private void setFilterEmailAttributes(ComAdmin admin, Model model, int id) {
        final String companyMailloopDomain = admin.getCompany().getMailloopDomain();
        boolean isAllowedMailloopDomain = isAllowedMailloopDomain(companyMailloopDomain);
        model.addAttribute(IS_ALLOWED_MAILLOOP_DOMAIN, isAllowedMailloopDomain);

        if(id > 0 && isAllowedMailloopDomain) {
            model.addAttribute(FILTER_EMAIL_ADDRESS_DEFAULT, BounceUtils.getFilterEmailDefault(companyMailloopDomain, id));
        }
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }

    private String getBounceFilterDescription(BounceFilterForm form) {
        return getBounceFilterDescription(form.getId(), form.getShortName());
    }
    
    private String getBounceFilterDescription(int filterId, String shortname) {
        return String.format("%s (%d)", shortname, filterId);
    }
}
