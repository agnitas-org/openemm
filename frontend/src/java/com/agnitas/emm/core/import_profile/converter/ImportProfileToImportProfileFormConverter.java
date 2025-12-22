/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.converter;

import java.util.Locale;

import com.agnitas.beans.ImportProfile;
import com.agnitas.emm.core.import_profile.form.ImportProfileForm;
import com.agnitas.util.importvalues.CheckForDuplicates;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ImportProfileToImportProfileFormConverter implements Converter<ImportProfile, ImportProfileForm> {

    @Override
    public ImportProfileForm convert(ImportProfile importProfile) {
        ImportProfileForm form = new ImportProfileForm();

        form.setId(importProfile.getId());
        form.setName(importProfile.getName());
        form.setDatatype(importProfile.getDatatype());
        form.setDecimalSeparator(importProfile.getDecimalSeparator());
        form.setSeparator(importProfile.getSeparator());
        form.setNoHeaders(importProfile.isNoHeaders());
        form.setZipPassword(importProfile.getZipPassword());
        form.setFirstKeyColumn(importProfile.getFirstKeyColumn());
        form.setShouldCheckForDuplicates(CheckForDuplicates.COMPLETE.equals(detectCheckForDuplicates(importProfile.getCheckForDuplicates())));
        form.setUpdateAllDuplicates(importProfile.getUpdateAllDuplicates());
        form.setDefaultMailType(importProfile.getDefaultMailType());
        form.setImportProcessActionID(importProfile.getImportProcessActionID());
        form.setMailForReport(importProfile.getMailForReport());
        form.setMailForError(importProfile.getMailForError());
        form.setReportTimezone(importProfile.getReportTimezone());
        form.setAutoMapping(importProfile.isAutoMapping());
        form.setActionForNewRecipients(importProfile.getActionForNewRecipients());
        form.setTextRecognitionChar(importProfile.getTextRecognitionChar());
        form.setCharset(importProfile.getCharset());
        form.setDateFormat(importProfile.getDateFormat());
        form.setNullValuesAction(importProfile.getNullValuesAction());
        form.setMailinglistsAll(importProfile.isMailinglistsAll());
        form.setImportMode(importProfile.getImportMode());
        form.setReportLocale(localeAsStr(importProfile.getReportLocale()));
        form.setSelectedMediatypes(importProfile.getMediatypes());

        return form;
    }

    private CheckForDuplicates detectCheckForDuplicates(int code) {
        try {
            return CheckForDuplicates.getFromInt(code);
        } catch (Exception e) {
            return CheckForDuplicates.COMPLETE;
        }
    }

    private String localeAsStr(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }
}
