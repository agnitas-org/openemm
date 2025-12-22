/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.recipient.exception.RecipientProfileHistoryException;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistrationException;
import com.agnitas.emm.core.webhooks.registry.service.WebhookUrlException;
import com.agnitas.emm.core.webhooks.settings.common.WebhookSettings;
import com.agnitas.emm.core.webhooks.settings.service.WebhookSettingsService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredPermission("webhooks.admin")
public final class WebhookAdministrationController implements XssCheckAware {

	private static final String REDIRECT_TO_LIST = "redirect:/webhooks/list.action";

	private final WebhookConfigService webhookConfigService;
	private final WebhookSettingsService webhookSettingsService;
	private final RecipientFieldService recipientFieldService;
	private final RecipientProfileHistoryService profileHistoryService;

	public WebhookAdministrationController(ConfigService configService, WebhookSettingsService webhookSettingsService,
										   RecipientFieldService recipientFieldService, RecipientProfileHistoryService profileHistoryService) {
		this.webhookConfigService = new WebhookConfigService(configService);
		this.webhookSettingsService = Objects.requireNonNull(webhookSettingsService, "WebhookSettingsService is null");
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "RecipientFieldService is null");
		this.profileHistoryService = profileHistoryService;
	}
	
	@GetMapping("/webhooks/list.action")
	public String list(Admin admin, Model model, @ModelAttribute("enableInterfaceForm") WebhookEnableForm form) {
		return doListWebhookUrls(admin, model, form);
	}

	private String doListWebhookUrls(Admin admin, Model model, WebhookEnableForm form) {
		form.setEnable(this.webhookConfigService.isWebhookInterfaceEnabled(admin.getCompanyID()));

		includeWebhookConfigurationData(admin, model);
	
		return "webhooks_list";
	}
	
	@PostMapping("/webhooks/enableInterface.action")
	@RequiredPermission("webhooks.enable")
	public String enableInterface(Admin admin, Model model, @ModelAttribute("enableInterfaceForm") WebhookEnableForm form) throws RecipientProfileHistoryException {
		// TODO Check permission and update settings
		profileHistoryService.enableProfileFieldHistory(admin.getCompanyID());
		this.webhookConfigService.enableWebhooksInterface(admin.getCompanyID(), form.isEnable());
		
		return doListWebhookUrls(admin, model, form);
	}

	@GetMapping("/webhooks/{eventTypeName:[A-Z_]+}/view.action")
	public String view(Admin admin, @ModelAttribute("webhookConfigForm") WebhookConfigurationForm configForm, @PathVariable String eventTypeName, Model model) {
		try {
			final WebhookEventType eventType = webhookEventTypeFromString(eventTypeName);
	
			populateConfigureWebhookData(admin.getCompanyID(), eventType, configForm);
			
			return doView(admin.getCompanyID(), eventType, model);
		} catch (UnknownEventTypeException e) {
			// Viewing error messages for unknown event type makes less sense here. Navigate to webhook list.
			return REDIRECT_TO_LIST;
		}
	}
	
	private void populateConfigureWebhookData(int companyID, WebhookEventType eventType, WebhookConfigurationForm configForm) {
		final WebhookSettings settings = this.webhookSettingsService.findWebhookSettings(companyID, eventType);

		configForm.fillFrom(settings);
	}

	private String doView(int companyId, WebhookEventType eventType, Model model) {
		model.addAttribute("WEBHOOK_EVENT_TYPE", eventType);
		model.addAttribute("PROFILE_FIELDS", recipientFieldService.getRecipientFields(companyId));
		return "webhook_view";
	}
	
	@PostMapping("/webhooks/{eventTypeName:[A-Z_]+}/save.action")
	public String updateWebhookSettings(@ModelAttribute("webhookConfigForm") WebhookConfigurationForm configForm,
										@PathVariable String eventTypeName, Admin admin, Model model, Popups popups) {
		try {
			final WebhookEventType eventType = webhookEventTypeFromString(eventTypeName);
	
			try {
                final Set<String> includedProfileFields = configForm.getIncludedProfileFields();
				this.webhookSettingsService.updateWebhookSettings(admin.getCompanyID(), eventType, configForm.getUrl(), includedProfileFields);
			
				popups.changesSaved();
				
				return REDIRECT_TO_LIST;
			} catch (WebhookRegistrationException e) {
				popups.alert("error.webhooks.general");
				return doView(admin.getCompanyID(), eventType, model);
			} catch (WebhookUrlException e) {
				WebhookUrlExceptionsToPopups.exceptionToPopup(e, popups);
				return MESSAGES_VIEW;
			}
		} catch (UnknownEventTypeException e) {
			popups.alert("error.webhooks.general");
			return REDIRECT_TO_LIST;
		}
	}

	private void includeWebhookConfigurationData(Admin admin, Model model) {
		final List<WebhookSettings> registeredWebhooks = this.webhookSettingsService.listWebhookSettings(admin.getCompanyID());
		final List<WebhookListItem> listItems = registeredWebhooks.stream()
				.map(WebhookListItem::from)
				.collect(Collectors.toList());

		model.addAttribute("WEBHOOKS",  listItems);
	}
	
	private static WebhookEventType webhookEventTypeFromString(final String name) throws UnknownEventTypeException {
		try {
			return WebhookEventType.valueOf(name);
		} catch(final Exception e) {
			throw new UnknownEventTypeException(e);
		}
	}
}
