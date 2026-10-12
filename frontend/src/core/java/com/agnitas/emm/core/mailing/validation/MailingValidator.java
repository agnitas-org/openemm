/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.validation;

import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.service.SimpleServiceResult;

public interface MailingValidator {

    void validateContainsOnlyAlwaysAllowedChanges(Mailing mailing, PatchMailingSettingsCommand command);

    SimpleServiceResult validateForSave(Mailing mailing, Admin admin);

    SimpleServiceResult validateMailingTagsAndComponents(Mailing mailing, Locale locale);

    SimpleServiceResult validateMailingAfterSave(Admin admin, Mailing mailing);

    SimpleServiceResult validateNeedDkimKey(Admin admin, Mailing mailing);
}
