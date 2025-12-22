/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.web;

import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistDeleteForm;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistForm;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.emm.core.globalblacklist.forms.validation.BlacklistFormValidator;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/recipients/blacklist")
@RequiredPermission("blacklist")
public class BlacklistController implements XssCheckAware {

    private static final String BLACKLIST_LIST_FORM_KEY = "blacklistListForm";
    private static final String MAILING_LISTS_KEY = "mailinglists";
    private static final String BLACKLISTS_DTO_KEY = "blacklists";
    private static final String BLACKLIST_DELETE_FORM_KEY = "blacklistDeleteForm";
    private static final String DATE_TIME_FORMAT_KEY = "dateTimeFormat";

    private static final Logger logger = LogManager.getLogger(BlacklistController.class);

    private final UserActivityLogService userActivityLogService;
    private final BlacklistService blacklistService;
    private final TableGenerator csvTableGenerator;
    private final WebStorage webStorage;

    private final BlacklistFormValidator blacklistFormValidator = new BlacklistFormValidator();

    public BlacklistController(@Qualifier("BlacklistService") BlacklistService blacklistService, UserActivityLogService userActivityLogService,
                               @Qualifier("csvTableGenerator") TableGenerator csvTableGenerator, WebStorage webStorage) {

        this.blacklistService = blacklistService;
        this.userActivityLogService = userActivityLogService;
        this.csvTableGenerator = csvTableGenerator;
        this.webStorage = webStorage;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(Admin admin, HttpSession session, Model model,
                                       @ModelAttribute(BLACKLIST_LIST_FORM_KEY) BlacklistOverviewFilter filter) {

        int companyId = admin.getCompanyID();

        FormUtils.syncNumberOfRows(webStorage, WebStorage.BLACKLIST_OVERVIEW, filter);

        PollingUid pollingUid = PollingUid.builder(session.getId(), BLACKLISTS_DTO_KEY)
                .arguments(filter.toArray())
                .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute(DATE_TIME_FORMAT_KEY, admin.getDateTimeFormat());
            model.addAttribute(BLACKLISTS_DTO_KEY, blacklistService.getAll(filter, companyId));

            return new ModelAndView("settings_blacklist_list", model.asMap());
        };

        ModelAndView modelAndView = new ModelAndView("redirect:/recipients/blacklist/list.action", filter.toMap());

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    @PostMapping("/save.action")
    public @ResponseBody BooleanResponseDto save(Admin admin, BlacklistForm saveForm, Popups popups) {
        if (!blacklistFormValidator.validate(saveForm, popups)) {
            return new BooleanResponseDto(popups, false);
        }

        int companyId = admin.getCompanyID();
        int adminId = admin.getAdminID();
        String email = saveForm.getEmail();

        if (blacklistService.isAlreadyExist(companyId, email)) {
            popups.alert("error.blacklist.recipient.isalreadyblacklisted", email);
            return new BooleanResponseDto(popups, false);
        }

        blacklistService.add(companyId, adminId, email, saveForm.getReason());
        popups.changesSaved();

        // UAL
        String activityLogAction = "create blacklist entry";
        String activityLogDescription = "Created new blacklist entry: " + email;
        userActivityLogService.writeUserActivityLog(admin, activityLogAction, activityLogDescription);

        return new BooleanResponseDto(popups, true);
    }

    @PostMapping("/update.action")
    public @ResponseBody BooleanResponseDto update(Admin admin, BlacklistForm saveForm, Popups popups) {
        if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
            throw new NotAllowedActionException();
        }

        if (!blacklistFormValidator.validate(saveForm, popups)) {
            return new BooleanResponseDto(popups, false);
        }

        final int companyId = admin.getCompanyID();
        final String email = saveForm.getEmail();

        final boolean isSuccessUpdate = blacklistService.update(companyId, email, saveForm.getReason());
        if (!isSuccessUpdate) {
            logger.error("Could not update blacklisted recipient with email: {} for company with id: {}", email, companyId);
            popups.defaultError();
            return new BooleanResponseDto(popups, false);
        }

        popups.changesSaved();

        // UAL
        final String activityLogDescription = "Updated blacklist entry: " + email;
        userActivityLogService.writeUserActivityLog(admin, "update blacklist entry", activityLogDescription);

        return new BooleanResponseDto(popups, true);

    }

    @GetMapping("/delete.action")
    public String delete(@ModelAttribute(BLACKLIST_DELETE_FORM_KEY) BlacklistDeleteForm form,
                                Admin admin, Model model) {
        validateSelectedEntries(form.getEmails());

        int companyId = admin.getCompanyID();

        List<Mailinglist> mailingLists = blacklistService.getBindedMailingLists(form.getEmails(), companyId);
        model.addAttribute(MAILING_LISTS_KEY, mailingLists);

        form.setMailingListIds(mailingLists.stream().map(Mailinglist::getId).toList());

        return "settings_blacklist_delete_ajax";
    }

    @PostMapping("/delete.action")
    public String delete(BlacklistDeleteForm deleteForm, Admin admin, Popups popups) {
        if (!admin.permissionAllowed(Permission.RECIPIENT_DELETE)) {
            throw new NotAllowedActionException();
        }

        validateSelectedEntries(deleteForm.getEmails());

        if (blacklistService.delete(deleteForm.getEmails(), Set.copyOf(deleteForm.getMailingListIds()), admin.getCompanyID())) {
            popups.selectionDeleted();
            userActivityLogService.writeUserActivityLog(
                    admin,
                    "delete from blacklist",
                    String.format("Deleted blacklist entries: (%s)", StringUtils.join(deleteForm.getEmails(), ","))
            );
        } else {
            popups.defaultError();
        }

        return "redirect:/recipients/blacklist/list.action";
    }

    private void validateSelectedEntries(Set<?> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    @GetMapping("/download.action")
    public ResponseEntity<Resource> download(Admin admin) {
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
