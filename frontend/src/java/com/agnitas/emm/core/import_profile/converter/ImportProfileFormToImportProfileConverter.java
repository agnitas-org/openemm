/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.converter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.impl.ImportProfileImpl;
import com.agnitas.emm.core.import_profile.form.ImportProfileForm;
import com.agnitas.util.importvalues.CheckForDuplicates;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ImportProfileFormToImportProfileConverter implements Converter<ImportProfileForm, ImportProfile> {

    @Override
    public ImportProfile convert(ImportProfileForm form) {
        ImportProfile importProfile = new ImportProfileImpl();

        importProfile.setId(form.getId());
        importProfile.setName(form.getName());
        importProfile.setDatatype(form.getDatatype());
        importProfile.setSeparator(form.getSeparator());
        importProfile.setNoHeaders(form.isNoHeaders());
        importProfile.setZipPassword(form.getZipPassword());
        importProfile.setFirstKeyColumn(form.getFirstKeyColumn());
        importProfile.setCheckForDuplicates(form.isShouldCheckForDuplicates() ? CheckForDuplicates.COMPLETE.getIntValue() : CheckForDuplicates.NO_CHECK.getIntValue());
        importProfile.setUpdateAllDuplicates(form.isUpdateAllDuplicates());
        importProfile.setDefaultMailType(form.getDefaultMailType());
        importProfile.setImportProcessActionID(form.getImportProcessActionID());
        importProfile.setMailForReport(form.getMailForReport());
        importProfile.setMailForError(form.getMailForError());

        if (StringUtils.isNotBlank(form.getReportLocale())) {
            String[] localeParts = form.getReportLocale().split("_");
            importProfile.setReportLocale(new Locale(localeParts[0], localeParts[1]));
        }

        importProfile.setReportTimezone(form.getReportTimezone());
        importProfile.setAutoMapping(form.isAutoMapping());
        importProfile.setActionForNewRecipients(form.getActionForNewRecipients());
        importProfile.setTextRecognitionChar(form.getTextRecognitionChar());
        importProfile.setCharset(form.getCharset());
        importProfile.setDateFormat(form.getDateFormat());
        importProfile.setNullValuesAction(form.getNullValuesAction());
        importProfile.setMailinglistsAll(form.isMailinglistsAll());
        importProfile.setImportMode(form.getImportMode());
        importProfile.setDecimalSeparator(form.getDecimalSeparator());
        importProfile.setGenderMapping(getGenderMappings(form));
        importProfile.setMediatypes(form.getSelectedMediatypes());

        return importProfile;
    }

    private Map<String, Integer> getGenderMappings(ImportProfileForm form) {
        Map<String, Integer> map = new HashMap<>();

        form.getGenderMapping().entrySet().stream()
                .filter(entry -> NumberUtils.isDigits(String.valueOf(entry.getKey())))
                .forEach(mapping -> {
                    int intValue = NumberUtils.toInt(String.valueOf(mapping.getKey()));
                    String textValuesStr = mapping.getValue();
                    Stream.of(textValuesStr.split(","))
                            .map(String::trim)
                            .filter(v -> !v.isEmpty())
                            .distinct()
                            .forEach(textVal -> map.put(textVal, intValue));
                });

        return map;
    }
}
