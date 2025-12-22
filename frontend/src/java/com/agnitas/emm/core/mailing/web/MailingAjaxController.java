/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.mailing.web;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.bean.LightweightMailingWithMailingList;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.forms.SaveMailStatusSettingsForm;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class MailingAjaxController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingAjaxController.class);

    private final MailingService mailingService;
    private final UserActivityLogService userActivityLogService;
    private final MailingBaseService mailingBaseService;
    private final TargetService targetService;
    private final TrackableLinkService trackableLinkService;
    
    public MailingAjaxController(@Qualifier("MailingService") MailingService mailingService, UserActivityLogService userActivityLogService,
                                 MailingBaseService mailingBaseService, TargetService targetService, TrackableLinkService trackableLinkService) {
        this.mailingService = Objects.requireNonNull(mailingService, "Mailing service is null");
        this.userActivityLogService = userActivityLogService;
        this.mailingBaseService = mailingBaseService;
        this.targetService = targetService;
        this.trackableLinkService = trackableLinkService;
    }

    @RequestMapping(value = "/listActionBasedForMailinglist.action", produces = "application/json")
    @RequiredPermission("mailing.show")
    public ResponseEntity<String> listAllActionBasedMailingsForMailinglist(Admin admin, @RequestParam(value = "mailinglist") int mailinglistID) {
        try {
            final List<LightweightMailingWithMailingList> list = mailingService.listAllActionBasedMailingsForMailinglist(admin.getCompanyID(), mailinglistID);

            final JSONObject root = new JSONObject();
            final JSONArray mailings = new JSONArray();

            for (final LightweightMailing mailing : list) {
                final JSONObject mailingJson = new JSONObject();

                mailingJson.put("id", mailing.getMailingID());
                mailingJson.put("shortname", mailing.getShortname());

                mailings.put(mailingJson);
            }

            root.put("mailings", mailings);

            return ResponseEntity.ok(root.toString());
        } catch (Exception e) {
            LOGGER.error(String.format("Error listing action based mailings for mailing list %d", mailinglistID), e);

            final JSONObject json = new JSONObject();
            json.put("error", "Internal server error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(json.toString());
        }
    }

    @PostMapping("{mailingId:\\d+}/lock.action")
    @RequiredPermission("mailing.content.show")
    public ResponseEntity<BooleanResponseDto> tryToLock(Admin admin, @PathVariable int mailingId) {
        try {
            // Start or prolong locking unless other admin is holding it.
            return ResponseEntity.ok(new BooleanResponseDto(mailingService.tryToLock(admin, mailingId)));
        } catch (MailingNotExistException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{mailingId:\\d+}/setStatusOnError.action")
    @RequiredPermission("mailing.send.world")
    public ResponseEntity<BooleanResponseDto> setStatusOnErrorOnly(Admin admin, @PathVariable int mailingId, SaveMailStatusSettingsForm form) {
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
            LOGGER.error("Error occurred: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok(new BooleanResponseDto(isUpdated));
    }

    protected void writeUserActivityLog(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, LOGGER);
    }

    @GetMapping("{mailingId:\\d+}/isAdvertisingContentType.action")
    @RequiredPermission("mailing.show")
    public ResponseEntity<BooleanResponseDto> isAdvertisingContentType(@PathVariable int mailingId, Admin admin) {
        return ResponseEntity.ok(
                new BooleanResponseDto(mailingBaseService.isAdvertisingContentType(admin.getCompanyID(), mailingId)));
    }

    @PostMapping("{mailingId:\\d+}/calculateRecipients.action")
    @AlwaysAllowed
    public ResponseEntity<Map<String, Object>> calculateRecipients(@PathVariable int mailingId, Admin admin,
                                                                   @ModelAttribute("settingsForm") MailingSettingsForm form,
                                                                   @RequestParam boolean changeMailing, @RequestParam boolean isWmSplit) {
        final JSONObject data = new JSONObject();

        CalculationRecipientsConfig config = getCalculationRecipientsConfig(mailingId, form, admin, changeMailing, isWmSplit);
        NumberFormat numberFormatCount = NumberFormat.getNumberInstance(admin.getLocale());
        DecimalFormat decimalFormatCount = (DecimalFormat) numberFormatCount;
        decimalFormatCount.applyPattern(",###");

        data.put("count", decimalFormatCount.format(mailingBaseService.calculateRecipients(config)));
        data.put("success", true);

        return ResponseEntity.ok(data.toMap());
    }

    @ModelAttribute("settingsForm")
    public MailingSettingsForm getSettingsForm() {
        return getMailingSettingsForm();
    }

    protected MailingSettingsForm getMailingSettingsForm() {
        return new MailingSettingsForm();
    }

    protected CalculationRecipientsConfig getCalculationRecipientsConfig(int mailingId, MailingSettingsForm form, Admin admin,
                                                                         boolean changeMailing, boolean isWmSplit) {
        CalculationRecipientsConfig config = new CalculationRecipientsConfig();
        config.setMailingId(mailingId);
        config.setCompanyId(admin.getCompanyID());
        config.setAssignTargetGroups(form.isAssignTargetGroups());
        config.setMailingListId(form.getMailinglistId());
        config.setTargetGroupIds(form.getTargetGroupIds());
        config.setChangeMailing(changeMailing);
        config.setSplitId(targetService.getTargetListSplitId(form.getSplitSettings().getSplitBase(), form.getSplitSettings().getSplitPart(), isWmSplit));
        config.setConjunction(form.getTargetMode() != Mailing.TARGET_MODE_OR);
        return config;
    }

    @GetMapping(value = "/{mailingId:\\d+}/links.action", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiredPermission("mailing.show")
    public @ResponseBody Map<String, Object> getMailingLinks(@PathVariable int mailingId, Admin admin) {
        return trackableLinkService.getMailingLinksJson(mailingId, admin.getCompanyID()).toMap();
    }

}
