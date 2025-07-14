/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidator;
import com.agnitas.emm.core.import_profile.form.ImportProfileForm;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.web.mvc.Popups;
import jakarta.mail.internet.InternetAddress;
import com.agnitas.service.ImportProfileService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.ImportMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ImportProfileFormValidator {

    private final ImportProfileService importProfileService;

    public ImportProfileFormValidator(ImportProfileService importProfileService) {
        this.importProfileService = importProfileService;
    }

    public boolean validate(ImportProfileForm form, Admin admin, Popups popups) {
        if (StringUtils.trimToNull(form.getName()) == null) {
            popups.fieldError("name", "error.name.is.empty");
        } else if (form.getName().trim().length() < 3) {
            popups.fieldError("name", "error.name.too.short");
        }

        if (!isValidEmails(form.getMailForReport())) {
            popups.fieldError("mailForReport", "error.invalid.email");
        }

        if (!isValidEmails(form.getMailForError())) {
            popups.fieldError("mailForError", "error.invalid.email");
        }

        if (form.getImportProcessActionID() > 0 && !admin.permissionAllowed(Permission.IMPORT_PREPROCESSING)) {
            popups.alert("error.notAllowed");
            return false;
        }

        if (form.isAutoMapping() && form.isNoHeaders()) {
            popups.alert("error.import.automapping.missing.header");
            return false;
        }

        if ((form.getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue() || form.getImportMode() == ImportMode.UPDATE.getIntValue())
                && form.getCheckForDuplicates() != CheckForDuplicates.COMPLETE.getIntValue()) {

            popups.alert("error.import.updateNeedsCheckForDuplicates");
            return false;
        }

        if (importProfileService.isDuplicatedName(form.getName(), form.getId(), admin.getCompanyID())) {
            popups.alert("error.import.duplicate_profile_name");
        }

        boolean isCustomerIdImported = importProfileService.isColumnWasImported(RecipientStandardField.CustomerID.getColumnName(), form.getId());
        if (isCustomerIdImported && (form.getImportMode() == ImportMode.ADD.getIntValue() || form.getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue())) {
            popups.alert("error.import.customerid_insert");
        }

        return !popups.hasAlertPopups();
    }

    private boolean isValidEmails(String emails) {
        if (StringUtils.isBlank(emails)) {
            return true;
        }

        try {
            for (InternetAddress emailAddress : AgnUtils.getEmailAddressesFromList(emails)) {
                if (!AgnitasEmailValidator.getInstance().isValid(emailAddress.getAddress())) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
