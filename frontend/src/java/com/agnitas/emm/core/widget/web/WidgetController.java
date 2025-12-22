/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.web;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipient.service.SubscribeRecipientService;
import com.agnitas.emm.core.widget.beans.SubscribeWidgetSettings;
import com.agnitas.emm.core.widget.enums.WidgetType;
import com.agnitas.emm.core.widget.exception.InvalidWidgetTokenException;
import com.agnitas.emm.core.widget.form.SubscribeWidgetForm;
import com.agnitas.emm.core.widget.form.SubscribeWidgetSettingsForm;
import com.agnitas.emm.core.widget.service.WidgetService;
import com.agnitas.emm.core.widget.validator.SubscribeWidgetFormValidator;
import com.agnitas.emm.core.widget.validator.SubscribeWidgetSettingsFormValidator;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.Anonymous;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredPermission("forms.change")
public class WidgetController implements XssCheckAware {

    private final MailinglistApprovalService mailinglistApprovalService;
    private final MailingService mailingService;
    private final WidgetService widgetService;
    private final SubscribeWidgetSettingsFormValidator subscribeWidgetSettingsFormValidator;
    private final SubscribeWidgetFormValidator subscribeWidgetFormValidator;
    private final ExtendedConversionService conversionService;
    private final SubscribeRecipientService subscribeRecipientService;

    public WidgetController(MailinglistApprovalService mailinglistApprovalService, MailingService mailingService, WidgetService widgetService,
                            SubscribeWidgetSettingsFormValidator subscribeWidgetSettingsFormValidator, SubscribeWidgetFormValidator subscribeWidgetFormValidator,
                            ExtendedConversionService conversionService, SubscribeRecipientService subscribeRecipientService) {
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.mailingService = mailingService;
        this.widgetService = widgetService;
        this.subscribeWidgetSettingsFormValidator = subscribeWidgetSettingsFormValidator;
        this.subscribeWidgetFormValidator = subscribeWidgetFormValidator;
        this.conversionService = conversionService;
        this.subscribeRecipientService = subscribeRecipientService;
    }

    @GetMapping("/widget/create.action")
    public String create(@ModelAttribute("subscribeSettingsForm") SubscribeWidgetSettingsForm subscribeSettingsForm, Admin admin, Model model) {
        List<Mailinglist> mailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
        model.addAttribute("mailinglists", mailinglists);
        model.addAttribute("doiMailings", mailingService.findActionBasedMailingsForMailinglists(mailinglists, admin.getCompanyID()));
        model.addAttribute("rdirDomain", admin.getCompany().getRdirDomain());

        return "widget_create";
    }

    @PostMapping("/widget/subscribe/token.action")
    public ResponseEntity<DataResponseDto<String>> generateSubscribeToken(SubscribeWidgetSettingsForm form, Admin admin, Popups popups) throws Exception {
        if (!subscribeWidgetSettingsFormValidator.validate(form, popups)) {
            return ResponseEntity.badRequest().body(new DataResponseDto<>("", popups));
        }

        String token = widgetService.generateToken(
                WidgetType.SUBSCRIBE,
                conversionService.convert(form, SubscribeWidgetSettings.class),
                admin.getCompanyID()
        );

        return ResponseEntity.ok(new DataResponseDto<>(token, popups));
    }

    @GetMapping("/widget.action")
    @Anonymous
    public String viewWidget(@RequestParam String token) {
        if (!widgetService.isTokenValid(token)) {
            throw new InvalidWidgetTokenException(token);
        }

        WidgetType widgetType = widgetService.getWidgetType(token);

        switch (widgetType) {
            case SUBSCRIBE:
                return "forward:/widget/subscribe.action";
            default:
                throw new UnsupportedOperationException("Unexpected widget type: " + widgetType);
        }
    }

    @GetMapping("/widget/subscribe.action")
    @Anonymous
    public String viewSubscribeWidget(@ModelAttribute("form") SubscribeWidgetForm form) {
        return "widget_subscribe";
    }

    @PostMapping("/widget/subscribe.action")
    @Anonymous
    public String subscribeRecipient(SubscribeWidgetForm form, Popups popups, HttpServletRequest req, RedirectAttributes ra) throws JsonProcessingException {
        if (!widgetService.isTokenValid(form.getToken(), WidgetType.SUBSCRIBE)) {
            throw new InvalidWidgetTokenException(form.getToken());
        }

        if (subscribeWidgetFormValidator.validate(form, popups)) {
            SubscribeWidgetSettings settings = widgetService.parseSettings(form.getToken(), SubscribeWidgetSettings.class);

            settings.setRemoteAddress(req.getRemoteAddr());
            settings.setReferrer(HttpUtils.getReferrer(req));

            SimpleServiceResult subscribeResult = subscribeRecipientService.subscribe(form, settings);
            popups.addPopups(subscribeResult);

            if (subscribeResult.isSuccess()) {
                ra.addFlashAttribute("successMsg", settings.getSuccessMessage());
            } else {
                ra.addFlashAttribute(
                        "errorMsg",
                        popups.hasAlertPopups() ? popups.getFullErrorsText(req.getLocale()) : settings.getErrorMessage()
                );
            }
        } else {
            ra.addFlashAttribute("errorMsg", popups.getFullErrorsText(req.getLocale()));
        }

        ra.addFlashAttribute("form", form);
        return "redirect:/widget/subscribe.action";
    }

}
