/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.web;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.forms.RecipientBulkForm;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.emm.core.recipient.service.RecipientLogService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import net.sf.json.JSONObject;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recipient")
@PermissionMapping("recipient")
public class RecipientController {
	private static final Logger logger = Logger.getLogger(RecipientController.class);
    
    private final RecipientService recipientService;
    private final RecipientLogService recipientLogService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final ComTargetService targetService;
    private final UserActivityLogService userActivityLogService;
	
	public RecipientController(RecipientService recipientService, RecipientLogService recipientLogService, ComTargetService targetService, UserActivityLogService userActivityLogService, final MailinglistApprovalService mailinglistApprovalService) {
		this.recipientService = recipientService;
		this.recipientLogService = recipientLogService;
		this.targetService = targetService;
		this.userActivityLogService = userActivityLogService;
		this.mailinglistApprovalService = mailinglistApprovalService;
	}
	
	@RequestMapping("/bulkView.action")
    public String bulkView(ComAdmin admin, RecipientBulkForm form, Model model) {
		List<ProfileField> recipientColumns = recipientService.getRecipientBulkFields(admin.getCompanyID());
		form.setRecipientFieldChanges(recipientColumns.stream().map(ProfileField::getColumn).collect(Collectors.toList()));

		model.addAttribute("recipientColumns", recipientColumns);
		
		model.addAttribute("hasAnyDisabledMailingLists", mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(admin));
		model.addAttribute("mailingLists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		model.addAttribute("targetGroups", targetService.getTargetLights(admin));
		model.addAttribute("calculatedRecipients", recipientService.calculateRecipient(admin, form.getTargetId(), form.getMailinglistId()));
		
		model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());

        return "recipient_bulk_change";
    }
    
    @GetMapping("/calculate.action")
    public @ResponseBody JSONObject calculateRecipients(ComAdmin admin, RecipientBulkForm form) {
		JSONObject result = new JSONObject();
		result.put("targetId", form.getTargetId());
		result.put("mailinglistId", form.getMailinglistId());
		result.put("count", recipientService.calculateRecipient(admin, form.getTargetId(), form.getMailinglistId()));
		return result;
	}
    
    @PostMapping("/bulkSave.action")
    public String bulkSave(ComAdmin admin, RecipientBulkForm form, RedirectAttributes model, Popups popups) {
		if (mailinglistApprovalService.isAdminHaveAccess(admin, form.getMailinglistId())) {
			// saving in case current admin have permission on manage this mailing list

			Map<String, RecipientFieldDto> fieldChanges = form.getRecipientFieldChanges().entrySet().stream()
					.filter(change -> change.getValue().isClear() || StringUtils.isNotEmpty(change.getValue().getNewValue()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			ServiceResult<FieldsSaveResults> saveResult =
					recipientService.saveBulkRecipientFields(admin, form.getTargetId(), form.getMailinglistId(), fieldChanges);
			
			if (saveResult.isSuccess()) {
				writeRecipientBulkChangesLog(admin, form.getTargetId(), form.getMailinglistId(), saveResult.getResult().getAffectedFields());
				
				int affected = saveResult.getResult().getAffectedRecipients();
				popups.success("bulkAction.changed", affected);
				if(affected > 0) {
					popups.success("default.changes_saved");
				}
			} else {

				popups.addPopups(saveResult);
			}
		} else {
			popups.warning("warning.mailinglist.disabled");
		}
		
		model.addFlashAttribute("recipientBulkForm", form);
		
		return "redirect:/recipient/bulkView.action";
    }
    
    private void writeRecipientBulkChangesLog(ComAdmin admin, int targetId, int mailitnlistId, Map<String, Object> affectedFields) {
		try {
			UserAction userAction = recipientLogService.getRecipientFieldsBulkChangeLog(targetId, mailitnlistId, affectedFields);
            writeUserActivityLog(admin, userAction);
            
            if (logger.isInfoEnabled()) {
                logger.info("bulkRecipientFieldEdit: edit field content target ID " + targetId + " and mailit list ID " + mailitnlistId);
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log Recipient bulk edit error" + e);
            }
        }
	}
    
    private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }
}
