/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.imports.form.ImportForm;
import com.agnitas.emm.core.imports.service.MailingImportService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.service.ImportResult;
import org.agnitas.service.UserActivityLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

public class ImportController {

    private static final Logger LOGGER = LogManager.getLogger(ImportController.class);

    public enum ImportType {
        MAILING, CLASSIC_TEMPLATE, LB_TEMPLATE, USER_FORM
    }
    
    private final MailingImportService mailingImportService;
    private final UserActivityLogService userActivityLogService;

    public ImportController(MailingImportService mailingImportService, UserActivityLogService userActivityLogService) {
        this.mailingImportService = mailingImportService;
        this.userActivityLogService = userActivityLogService;
    }

    @GetMapping("/view.action")
    public String view(Model model) {
        addModelAttributesForViewPage(model);
        return "import_view";
    }

    protected void addModelAttributesForViewPage(Model model) {
        model.addAttribute("form", new ImportForm());
    }

    @PostMapping("/execute.action")
    public String execute(@ModelAttribute ImportForm form, Popups popups, Admin admin) throws Exception {
        if (!existsUploadedFile(form.getUploadFile(), popups)) {
            return MESSAGES_VIEW;
        }

        ImportResult importResult = mailingImportService.importMailing(form, admin);

        if (importResult == null) {
            popups.alert("error.import.data.missing");
            return MESSAGES_VIEW;
        }

        if (!importResult.isSuccess()) {
            addImportErrors(importResult, popups);
            return MESSAGES_VIEW;
        }

        popups.success("mailing.imported");
        addImportWarnings(importResult, popups);

        return viewImportedItem(importResult, admin);
    }

    protected String viewImportedItem(ImportResult result, Admin admin) {
        String action = result.isTemplate() ? "import template" : "import mailing";
        writeUserActivityLog(admin, action, String.valueOf(result.getMailingID()));

        return String.format("redirect:/mailing/%d/settings.action", result.getMailingID());
    }

    @GetMapping("/file.action")
    public String file(@RequestParam ImportType type, @ModelAttribute("form") ImportForm form, Model model) {
        model.addAttribute("type", type);
        return "import_modal";
    }

    @GetMapping("/mailing.action")
    @PermissionMapping("view.mailing")
    public String viewMailingImport() {
        return "mailing_import";
    }

    @GetMapping("/template.action")
    @PermissionMapping("view.template")
    public String viewTemplateImport(@ModelAttribute("form") ImportForm form) {
        return "template_import";
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
