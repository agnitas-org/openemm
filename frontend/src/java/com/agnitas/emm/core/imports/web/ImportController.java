/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.imports.form.ImportForm;
import com.agnitas.emm.core.imports.service.MailingImportService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.service.ImportResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public class ImportController {

    private static final Logger LOGGER = LogManager.getLogger(ImportController.class);

    public enum ImportType {
        MAILING, TEMPLATE, USER_FORM, BUILDING_BLOCK
    }

    private final MailingImportService mailingImportService;
    private final UserActivityLogService userActivityLogService;
    private final MailingService mailingService;

    public ImportController(MailingImportService mailingImportService, UserActivityLogService userActivityLogService, MailingService mailingService) {
        this.mailingImportService = mailingImportService;
        this.userActivityLogService = userActivityLogService;
        this.mailingService = mailingService;
    }

    @GetMapping("/view.action")
    @RequiredPermission("mailing.import|forms.import")
    public String view(Model model) {
        addModelAttributesForViewPage(model);
        return "import_view";
    }

    protected void addModelAttributesForViewPage(Model model) {
        model.addAttribute("form", new ImportForm());
    }

    @PostMapping("/execute.action")
    @RequiredPermission("mailing.import")
    public Object execute(@ModelAttribute ImportForm form, Popups popups, Admin admin, HttpServletRequest req) {
        boolean isWorkflowDriven = !WorkflowParametersHelper.isEmptyParams(req);
        if (!existsUploadedFile(form.getUploadFile(), popups)) {
            return isWorkflowDriven ? ResponseEntity.ok().body(new BooleanResponseDto(popups, false)) : MESSAGES_VIEW;
        }

        ImportResult importResult = mailingImportService.importMailing(form, admin);

        if (importResult == null) {
            popups.alert("error.import.data.missing");
            return isWorkflowDriven ? ResponseEntity.ok().body(new BooleanResponseDto(popups, false)) : MESSAGES_VIEW;
        }

        if (!importResult.isSuccess()) {
            addImportErrors(importResult, popups);
            return isWorkflowDriven ? ResponseEntity.ok().body(new BooleanResponseDto(popups, false)) : MESSAGES_VIEW;
        }

        if (isWorkflowDriven) {
            Map<String, ?> responseData = Map.of(
                    "mailingId", importResult.getMailingID(),
                    "mailingName", mailingService.getMailingName(importResult.getMailingID(), admin.getCompanyID())
            );

            // mark as deleted to prevent mailing displaying on overview in case if workflow will not be saved
            mailingService.deleteMailing(importResult.getMailingID(), admin);

            return ResponseEntity.ok().body(new DataResponseDto<>(
                    responseData,
                    true
            ));
        } else {
            popups.success("mailing.imported");
            addImportWarnings(importResult, popups);

            return viewImportedItem(importResult, admin);
        }
    }

    protected String viewImportedItem(ImportResult result, Admin admin) {
        String action = result.isTemplate() ? "import template" : "import mailing";
        writeUserActivityLog(admin, action, String.valueOf(result.getMailingID()));

        return String.format("redirect:/mailing/%d/settings.action", result.getMailingID());
    }

    @GetMapping("/file.action")
    @RequiredPermission("mailing.import|forms.import")
    public String file(@RequestParam ImportType type, @ModelAttribute("form") ImportForm form, Model model) {
        model.addAttribute("type", type);
        return "import_modal";
    }

    private void addImportErrors(ImportResult importResult, Popups popups) {
        for (Map.Entry<String, Object[]> errorEntry : importResult.getErrors().entrySet()) {
            popups.alert(errorEntry.getKey(), errorEntry.getValue());
        }
    }

    private void addImportWarnings(ImportResult importResult, Popups popups) {
        for (Map.Entry<String, Object[]> warningEntry : importResult.getWarnings().entrySet()) {
            popups.warning(warningEntry.getKey(), warningEntry.getValue());
        }
    }

    protected boolean existsUploadedFile(MultipartFile file, Popups popups) {
        if (file == null || file.getSize() == 0) {
            popups.alert("error.file.missingOrEmpty");
            return false;
        }

        return true;
    }

    protected void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description, LOGGER);
    }

}
