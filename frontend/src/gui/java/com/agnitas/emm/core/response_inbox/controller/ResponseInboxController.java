/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.controller;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.util.Date;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus;
import com.agnitas.emm.core.response_inbox.forms.ResponseInboxFormSearchParams;
import com.agnitas.emm.core.response_inbox.forms.ResponseInboxOverviewFilter;
import com.agnitas.emm.core.response_inbox.service.MailloopReplyService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.exception.ResourceNotFoundException;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes(types = ResponseInboxFormSearchParams.class)
@SuppressWarnings("squid:S3753") // search parameters intentionally kept in session
public class ResponseInboxController {

    private static final String REDIRECT_TO_OVERVIEW = "redirect:/response-inbox.action?restoreSort=true";

    private final MailloopReplyService mailloopReplyService;
    private final UserActivityLogService userActivityLogService;
    private final WebStorage webStorage;

    public ResponseInboxController(MailloopReplyService mailloopReplyService, UserActivityLogService userActivityLogService, WebStorage webStorage) {
        this.mailloopReplyService = mailloopReplyService;
        this.userActivityLogService = userActivityLogService;
        this.webStorage = webStorage;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @GetMapping("/response-inbox.action")
    @RequiredPermission("inbox.show")
    public String list(@ModelAttribute("filter") ResponseInboxOverviewFilter filter, ResponseInboxFormSearchParams searchParams,
                       @RequestParam(required = false) Boolean restoreSort, Admin admin, Model model) {
        FormUtils.syncPaginationData(webStorage, WebStorage.RESPONSE_INBOX_OVERVIEW, filter, restoreSort);
        searchParams.restoreParams(filter);

        model.addAttribute("replies", mailloopReplyService.getOverviewList(filter, admin.getCompanyID()));

        return "response_inbox_list";
    }

    @GetMapping("/response-inbox/search.action")
    @RequiredPermission("inbox.show")
    public String search(@ModelAttribute ResponseInboxOverviewFilter filter, ResponseInboxFormSearchParams searchParams) {
        searchParams.storeParams(filter);
        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping(value = "/response-inbox/{id:\\d+}/view.action")
    @RequiredPermission("inbox.show")
    public String view(@PathVariable int id, Model model, Admin admin) {
        MailloopReplyEntry replyEntry = mailloopReplyService.getReply(id, admin.getCompanyID());
        if (replyEntry == null) {
            throw new ResourceNotFoundException(ERROR_MSG);
        }

        if (MailloopReplyStatus.UNREAD.equals(replyEntry.getStatus())) {
            mailloopReplyService.markAsRead(id, admin.getCompanyID());
            replyEntry.setStatus(MailloopReplyStatus.READ);
        }

        model.addAttribute("replyEntry", replyEntry);
        model.addAttribute("sendersIds", mailloopReplyService.findSendersIds(replyEntry, admin.getCompanyID()));

        return "response_inbox_view";
    }

    @GetMapping(value = "/response-inbox/{id:\\d+}/content/view.action")
    @RequiredPermission("inbox.show")
    public String viewContent(@PathVariable int id, Model model, Admin admin) {
        MailloopReplyEntry replyEntry = mailloopReplyService.getReply(id, admin.getCompanyID());
        if (replyEntry == null) {
            throw new ResourceNotFoundException(ERROR_MSG);
        }

        model.addAttribute("content", replyEntry.getContent());
        return "response_inbox_view_content";
    }

    @GetMapping(value = "/response-inbox/delete.action")
    @RequiredPermission("inbox.delete")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> ids, Admin admin, Model model) {
        validateSelectedIds(ids);
        model.addAttribute("replies", mailloopReplyService.getReplies(ids, admin.getCompanyID()));

        return "response_inbox_delete_modal";
    }

    @RequestMapping(value = "/response-inbox/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("inbox.delete")
    public String delete(@RequestParam(required = false) Set<Integer> ids, Admin admin, Popups popups) {
        validateSelectedIds(ids);

        mailloopReplyService.delete(ids, admin.getCompanyID());
        userActivityLogService.writeUserActivityLog(
                admin,
                "delete replies",
                "Deleted replies with following ids: " + StringUtils.join(ids, ", ")
        );

        popups.selectionDeleted();
        return REDIRECT_TO_OVERVIEW;
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

}
