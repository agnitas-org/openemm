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
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistrationException;
import com.agnitas.emm.core.webhooks.registry.service.WebhookUrlException;
import com.agnitas.emm.core.webhooks.settings.common.WebhookSettings;
import com.agnitas.emm.core.webhooks.settings.service.WebhookSettingsService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@PermissionMapping("webhooks")
public final class WebhookAdministrationController implements XssCheckAware {

	public static final String WEBHOOK_ATTRIBUTE_NAME = "WEBHOOKS";
	public static final String EVENT_TYPE_ATTRIBUTE_NAME = "WEBHOOK_EVENT_TYPE";
	public static final String PROFILEFIELDS_ATTRIBUTE_NAME = "PROFILE_FIELDS";
	private static final String REDIRECT_TO_LIST = "redirect:/webhooks/list.action";

	private final WebhookConfigService webhookConfigService;
	private final WebhookSettingsService webhookSettingsService;
	private final RecipientFieldService recipientFieldService;

	public WebhookAdministrationController(ConfigService configService, WebhookSettingsService webhookSettingsService,
										   RecipientFieldService recipientFieldService) {
		this.webhookConfigService = new WebhookConfigService(configService);
		this.webhookSettingsService = Objects.requireNonNull(webhookSettingsService, "WebhookSettingsService is null");
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "RecipientFieldService is null");
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
	public String enableInterface(Admin admin, Model model, @ModelAttribute("enableInterfaceForm") WebhookEnableForm form) {
		// TODO Check permission and update settings
		this.webhookConfigService.enableWebhooksInterface(admin.getCompanyID(), form.isEnable());
		
		return doListWebhookUrls(admin, model, form);
	}

	@GetMapping("/webhooks/{eventTypeName:[A-Z_]+}/view.action")
	public String view(final Admin admin, @ModelAttribute("webhookConfigForm") final WebhookConfigurationForm configForm, @PathVariable final String eventTypeName, final Model model) throws ProfileFieldListException {
		try {
			final WebhookEventType eventType = webhookEventTypeFromString(eventTypeName);
	
			populateConfigureWebhookData(admin.getCompanyID(), eventType, configForm);
			
			return doView(admin.getCompanyID(), eventType, model);
		} catch(final UnknownEventTypeException e) {
			// Viewing error messages for unknown event type makes less sense here. Navigate to webhook list.
			return REDIRECT_TO_LIST;
		}
	}
	
	private void populateConfigureWebhookData(int companyID, WebhookEventType eventType, WebhookConfigurationForm configForm) {
		final WebhookSettings settings = this.webhookSettingsService.findWebhookSettings(companyID, eventType);

		configForm.fillFrom(settings);
	}

	private String doView(final int companyId, final WebhookEventType eventType, final Model model) {
		model.addAttribute(EVENT_TYPE_ATTRIBUTE_NAME, eventType);
		model.addAttribute(PROFILEFIELDS_ATTRIBUTE_NAME,  eventType == WebhookEventType.PROFILE_FIELD_CHANGED
            ? recipientFieldService.getHistorizedFields(companyId)
            : recipientFieldService.getRecipientFields(companyId));
		
		return "webhook_view";
	}
	
	@PostMapping("/webhooks/{eventTypeName:[A-Z_]+}/save.action")
	public String updateWebhookSettings(final Admin admin, @ModelAttribute("webhookConfigForm") final WebhookConfigurationForm configForm, @PathVariable final String eventTypeName, final Model model, final Popups popups) throws ProfileFieldListException {
		try {
			final WebhookEventType eventType = webhookEventTypeFromString(eventTypeName);
	
			try {
                final Set<String> includedProfileFields = configForm.getIncludedProfileFields();
				this.webhookSettingsService.updateWebhookSettings(admin.getCompanyID(), eventType, configForm.getUrl(), includedProfileFields);
			
				popups.success("default.changes_saved");
				
				return REDIRECT_TO_LIST;
			} catch(final WebhookRegistrationException e) {
				popups.alert("error.webhooks.general");

				return doView(admin.getCompanyID(), eventType, model);
			} catch(final WebhookUrlException e) {
				WebhookUrlExceptionsToPopups.exceptionToPopup(e, popups);

				return isUiRedesign(admin) ? MESSAGES_VIEW : doView(admin.getCompanyID(), eventType, model);
			}
		} catch(final UnknownEventTypeException e) {
			popups.alert("error.webhooks.general");
			
			return REDIRECT_TO_LIST;
		}
	}

    private boolean isUiRedesign(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

	private void includeWebhookConfigurationData(Admin admin, Model model) {
		final List<WebhookSettings> registeredWebhooks = this.webhookSettingsService.listWebhookSettings(admin.getCompanyID());
		final List<WebhookListItem> listItems = registeredWebhooks.stream()
				.map(WebhookListItem::from)
				.collect(Collectors.toList());

		model.addAttribute(WEBHOOK_ATTRIBUTE_NAME,  listItems);
	}
	
	private static WebhookEventType webhookEventTypeFromString(final String name) throws UnknownEventTypeException {
		try {
			return WebhookEventType.valueOf(name);
		} catch(final Exception e) {
			throw new UnknownEventTypeException(e);
		}
	}
}
