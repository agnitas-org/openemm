/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.mailing.web;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.forms.SaveMailStatusSettingsForm;
import com.agnitas.emm.core.mailing.forms.SaveSendSecuritySettingsForm;
import com.agnitas.emm.core.mailing.service.MailingDeliveryLockService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.messages.I18nString;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Popups;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/mailing/ajax")
public final class MailingAjaxController {
	
	private static final Logger LOGGER = LogManager.getLogger(MailingAjaxController.class);
	
	private final MailingService mailingService;
	private final UserActivityLogService userActivityLogService;
	private final AutoImportService autoImportService;
	private final MailingDeliveryLockService mailingDeliveryLockService;

	public MailingAjaxController(@Qualifier("MailingService") MailingService mailingService, UserActivityLogService userActivityLogService, @Autowired(required = false) AutoImportService autoImportService,
								 MailingDeliveryLockService mailingDeliveryLockService) {
		this.mailingService = Objects.requireNonNull(mailingService, "Mailing service is null");
		this.userActivityLogService = userActivityLogService;
		this.autoImportService = autoImportService;
		this.mailingDeliveryLockService = mailingDeliveryLockService;
	}
	
	@RequestMapping(value="/listActionBasedForMailinglist.action", produces="application/json")
	public final ResponseEntity<String> listAllActionBasedMailingsForMailinglist(final ComAdmin admin, @RequestParam(value="mailinglist") final int mailinglistID) {
		try {
			final List<LightweightMailing> list = mailingService.listAllActionBasedMailingsForMailinglist(admin.getCompanyID(), mailinglistID);
			
			final JSONObject root = new JSONObject();
			final JSONArray mailings = new JSONArray();
			
			for(final LightweightMailing mailing : list) {
				final JSONObject mailingJson = new JSONObject();
				
				mailingJson.put("id", mailing.getMailingID());
				mailingJson.put("shortname", mailing.getShortname());
				
				mailings.add(mailingJson);
			}
			
			root.put("mailings", mailings);
			
			return ResponseEntity.ok(root.toString());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error listing action based mailings for mailing list %d", mailinglistID), e);
			
			final JSONObject json = new JSONObject();
			json.put("error", "Internal server error");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(json.toString());
		}
	}

	@PostMapping("{mailingId:\\d+}/lock.action")
	public ResponseEntity<BooleanResponseDto> tryToLock(ComAdmin admin, @PathVariable int mailingId) {
		try {
			// Start or prolong locking unless other admin is holding it.
			return ResponseEntity.ok(new BooleanResponseDto(mailingService.tryToLock(admin, mailingId)));
		} catch (MailingNotExistException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{mailingId:\\d+}/setStatusOnError.action")
	public ResponseEntity<BooleanResponseDto> setStatusOnErrorOnly(ComAdmin admin, @PathVariable int mailingId, SaveMailStatusSettingsForm form) {
        boolean isUpdated = false;

		try {
			// creation of UAL entry
			Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
			String action = "switched mailing statusmailonerroronly";
			String description = String.format("statusmailonerroronly: %s, mailing type: %s. %s(%d)",
					form.isStatusOnErrorEnabled(), mailing.getMailingType().name(),
					mailing.getShortname(),
					mailing.getId());

			if (mailingService.switchStatusmailOnErrorOnly(admin.getCompanyID(), mailingId, form.isStatusOnErrorEnabled())) {
				isUpdated = true;
				writeUserActivityLog(admin, action, description);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred: " + e.getMessage(), e);
		}

        return ResponseEntity.ok(new BooleanResponseDto(isUpdated));
	}

	@PostMapping("/{mailingId:\\d+}/saveSecuritySettings.action")
	public @ResponseBody BooleanResponseDto saveSecuritySettings(ComAdmin admin, @PathVariable int mailingId, SaveSendSecuritySettingsForm form, Popups popups) {
		try {
			mailingDeliveryLockService.blockIfNecessary(form.getAutoImportId(), mailingId, admin.getCompanyID());

			if (!isValidSecuritySettings(admin, form, popups)) {
				return new BooleanResponseDto(popups, false);
			}

			MailingSendSecurityOptions options = MailingSendSecurityOptions.builder()
					.setNoSendNotificationEnabled(form.isEnableNoSendCheckNotifications())
					.withNotifications(form.isEnableNotifications(), form.getClearanceEmail())
					.setClearanceThreshold(form.getClearanceThreshold())
					.build();

			if (mailingService.saveSecuritySettings(admin.getCompanyID(), mailingId, options)) {
				popups.success("default.changes_saved");
				return new BooleanResponseDto(popups, true);
			}
		} catch (Exception e) {
			LOGGER.error("Saving security settings failed!", e);
		}

		popups.alert("Error");
		return new BooleanResponseDto(popups, false);
	}

    @GetMapping("{mailingId:\\d+}/load-security-settings.action")
    public String loadSecuritySettings(ComAdmin admin, @PathVariable int mailingId, Model model) {
		int companyID = admin.getCompanyID();
		Mailing mailing = mailingService.getMailing(companyID, mailingId);

        model.addAttribute("mailingID", mailingId);
        model.addAttribute("clearanceThreshold", mailing.getClearanceThreshold());
        model.addAttribute("statusOnErrorEnabled", mailing.isStatusmailOnErrorOnly());
        model.addAttribute("clearanceEmail", mailing.getClearanceEmail());
		model.addAttribute("autoImportId", mailingDeliveryLockService.getMailingImportLock(mailingId).getAutoImportId());

		if (admin.permissionAllowed(Permission.RECIPIENT_IMPORT_AUTO_MAILING)) {
			model.addAttribute("autoImports", autoImportService == null ? new ArrayList<AutoImportLight>() : autoImportService.listAutoImports(companyID));
		}

        return "security_settings";
    }

    private boolean isValidSecuritySettings(ComAdmin admin, SaveSendSecuritySettingsForm form, Popups popups) {
        if (form.isEnableNotifications()) {
            if (StringUtils.isBlank(form.getClearanceEmail())) {
                popups.alert("error.email.empty");
                return false;
            }
        } else if (!StringUtils.isBlank(form.getClearanceEmail()) || form.isEnableNoSendCheckNotifications() || form.getClearanceThreshold() != null) {
            popups.alert("error.notification.off");
            return false;
        }

        if (!AgnUtils.isValidEmailAddresses(form.getClearanceEmail())) {
            popups.alert("error.email.wrong");
            return false;
        }

        if (form.getClearanceThreshold() != null && form.getClearanceThreshold() <= 0) {
            popups.alert("grid.errors.wrong.int", I18nString.getLocaleString("mailing.autooptimization.threshold", admin.getLocale()));
            return false;
        }

        return true;
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, String description) {
        writeUserActivityLog(admin, action, description, LOGGER);
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, String description, Logger callerLog) {
        if (userActivityLogService != null) {
            userActivityLogService.writeUserActivityLog(admin, action, description, callerLog);
        } else {
            callerLog.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            callerLog.info("Userlog: " + admin.getUsername() + " " + action + " " + description);
        }
    }
}
