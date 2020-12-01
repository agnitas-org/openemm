/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistDeleteForm;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistForm;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistListForm;
import com.agnitas.emm.core.globalblacklist.forms.validation.BlacklistDeleteFormValidator;
import com.agnitas.emm.core.globalblacklist.forms.validation.BlacklistFormValidator;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/recipients/blacklist")
@PermissionMapping("blacklist")
public class BlacklistController {

    private static final String BLACKLIST_LIST_FORM_KEY = "blacklistListForm";
    private static final String MAILING_LISTS_KEY = "mailinglists";
    private static final String BLACKLISTS_DTO_KEY = "blacklists";
    private static final String BLACKLIST_DELETE_FORM_KEY = "blacklistDeleteForm";
    private static final String DATE_TIME_FORMAT_KEY = "dateTimeFormat";

    private static final Logger logger = Logger.getLogger(BlacklistController.class);

    private UserActivityLogService userActivityLogService;
    private BlacklistService blacklistService;
    private TableGenerator csvTableGenerator;
    private WebStorage webStorage;

    private final BlacklistDeleteFormValidator deleteFormValidator = new BlacklistDeleteFormValidator();
    private final BlacklistFormValidator blacklistFormValidator = new BlacklistFormValidator();

    public BlacklistController(@Qualifier("BlacklistService") BlacklistService blacklistService,
                               UserActivityLogService userActivityLogService,
                               @Qualifier("csvTableGenerator") TableGenerator csvTableGenerator,
                               WebStorage webStorage) {

        this.blacklistService = blacklistService;
        this.userActivityLogService = userActivityLogService;
        this.csvTableGenerator = csvTableGenerator;
        this.webStorage = webStorage;
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(ComAdmin admin, HttpSession session, Model model,
                                       @ModelAttribute(BLACKLIST_LIST_FORM_KEY) BlacklistListForm listForm) {

        int companyId = admin.getCompanyID();
        String sessionId = session.getId();

        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.BLACKLIST_OVERVIEW, listForm);

        PollingUid pollingUid = PollingUid.builder(sessionId, BLACKLISTS_DTO_KEY)
                .arguments(listForm.getSort(), listForm.getOrder(), listForm.getPage(), listForm.getNumberOfRows())
                .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute(DATE_TIME_FORMAT_KEY, admin.getDateTimeFormat());
            model.addAttribute(BLACKLISTS_DTO_KEY,
                    blacklistService.getAll(companyId,
                            listForm.getSort(),
                            listForm.getOrder(),
                            listForm.getPage(),
                            listForm.getNumberOfRows(),
                            listForm.getSearchQuery()));

            return new ModelAndView("settings_blacklist_list", model.asMap());
        };

        ModelAndView modelAndView = new ModelAndView("redirect:/recipients/blacklist/list.action", listForm.toMap());

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    @PostMapping("/save.action")
    public @ResponseBody BooleanResponseDto save(ComAdmin admin, BlacklistForm saveForm, Popups popups) {
        if(!blacklistFormValidator.validate(saveForm, popups)) {
            return new BooleanResponseDto(popups, false);
        }

        try {
            int companyId = admin.getCompanyID();
            int adminId = admin.getAdminID();
            String email = saveForm.getEmail();

            if (blacklistService.isAlreadyExist(companyId, email)) {
                popups.alert("error.blacklist.recipient.isalreadyblacklisted", email);
            } else {
                blacklistService.add(companyId, adminId, email, saveForm.getReason());
                popups.success("default.changes_saved");

                // UAL
                String activityLogAction = "create blacklist entry";
                String activityLogDescription = "Created new blacklist entry: " + email;
                userActivityLogService.writeUserActivityLog(admin, activityLogAction, activityLogDescription);

                return new BooleanResponseDto(popups, true);
            }
		} catch (Exception e) {
			popups.alert("Error");
			logger.error("Error adding mail address to blacklist", e);
		}

        return new BooleanResponseDto(popups, false);
    }

    @PostMapping("/update.action")
    public @ResponseBody BooleanResponseDto update(ComAdmin admin, BlacklistForm saveForm, Popups popups) throws NotAllowedActionException {
        if(!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
            throw new NotAllowedActionException();
        }

        if(!blacklistFormValidator.validate(saveForm, popups)) {
            return new BooleanResponseDto(popups, false);
        }

        final int companyId = admin.getCompanyID();
        final String email = saveForm.getEmail();

        final boolean isSuccessUpdate = blacklistService.update(companyId, email, saveForm.getReason());
        if (!isSuccessUpdate) {
            logger.error("Could not update blacklisted recipient with email: " + email + " for company with id: " + companyId);
            popups.alert("Error");
            return new BooleanResponseDto(popups, false);
        }

        popups.success("default.changes_saved");

        // UAL
        final String activityLogDescription = "Updated blacklist entry: " + email;
        userActivityLogService.writeUserActivityLog(admin, "update blacklist entry", activityLogDescription);

        return new BooleanResponseDto(popups, true);

    }

    @PermissionMapping("confirm.delete")
    @GetMapping("/confirmDelete.action")
    public String confirmDelete(ComAdmin admin, @RequestParam(value = "email") String email, Model model, @ModelAttribute(BLACKLIST_DELETE_FORM_KEY) BlacklistDeleteForm blacklistDeleteForm) {
        int companyId = admin.getCompanyID();

        List<Mailinglist> mailingLists = blacklistService.getBindedMailingLists(companyId, email);
        model.addAttribute(MAILING_LISTS_KEY, mailingLists);

        List<Integer> mailingListIds = mailingLists.stream().map(Mailinglist::getId).collect(Collectors.toList());
        blacklistDeleteForm.setMailingListIds(mailingListIds);
        blacklistDeleteForm.setEmail(email);

        return "settings_blacklist_delete_ajax";
    }

    @PostMapping("/delete.action")
    public String delete(ComAdmin admin, BlacklistDeleteForm deleteForm, Popups popups) throws NotAllowedActionException {
        if(!admin.permissionAllowed(Permission.RECIPIENT_DELETE)) {
            throw new NotAllowedActionException();
        }

        if (deleteFormValidator.validate(deleteForm, popups)) {
            int companyID = admin.getCompanyID();
            String email = deleteForm.getEmail();
            if (blacklistService.delete(companyID, deleteForm.getEmail(), deleteForm.getMailingListIdSet())) {
                popups.success("default.selection.deleted");

                // UAL
                String activityLogAction = "delete from blacklist";
                String activityLogDescription = "Deleted blacklist entry: " + email;
                userActivityLogService.writeUserActivityLog(admin, activityLogAction, activityLogDescription);
            } else {
                popups.alert("Error");
            }
        }

        return "messages";
    }

    @GetMapping("/download.action")
    public ResponseEntity<Resource> download(ComAdmin admin) throws Exception {

        int companyId = admin.getCompanyID();
        Locale locale = admin.getLocale();

        String fileName = "blacklist.csv";
        String mediaType = "text/csv";

        List<BlacklistDto> recipientList = blacklistService.getAll(companyId);
        String csvContent = csvTableGenerator.generate(recipientList, locale);

        byte[] byteResource = csvContent.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(byteResource);

        // UAL
        String activityLogAction = "download blacklist";
        String activityLogDescription = "Row count: " + recipientList.size();
        userActivityLogService.writeUserActivityLog(admin, activityLogAction, activityLogDescription, logger);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentLength(byteResource.length)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(resource);
    }
}
