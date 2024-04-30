/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.printer;


import java.util.Locale;
import java.util.Map;

import org.springframework.format.Printer;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.report.dto.RecipientEntityDto;
import com.agnitas.emm.core.report.enums.fields.RecipientFields;
import com.agnitas.emm.core.report.generator.constants.TableSpecialCharacters;
import com.agnitas.messages.I18nString;

@Component
public class RecipientEntityDtoPrinter implements Printer<RecipientEntityDto> {

    private static final String ROW_FORMAT = "%-35s\u00A0:\u00A0%s\r\n";

    @Override
    public String print(RecipientEntityDto recipient, Locale locale) {
        StringBuilder out = new StringBuilder("RECIPIENT DATA:" + TableSpecialCharacters.CRLF_LINE_SEPARATOR);

        String salutation = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_SALUTATION.getTranslationKey(),
                locale, RecipientFields.COLUMN_SALUTATION.getReadableName());
        out.append(String.format(ROW_FORMAT, salutation, recipient.getSalutation()));

        String title = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_TITLE.getTranslationKey(), locale,
                RecipientFields.COLUMN_TITLE.getReadableName());
        out.append(String.format(ROW_FORMAT, title, recipient.getTitle()));

        String firstName = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_FIRST_NAME.getTranslationKey(),
                locale, RecipientFields.COLUMN_FIRST_NAME.getReadableName());
        out.append(String.format(ROW_FORMAT, firstName, recipient.getFirstName()));

        String lastName = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_LAST_NAME.getTranslationKey(),
                locale, RecipientFields.COLUMN_LAST_NAME.getReadableName());
        out.append(String.format(ROW_FORMAT, lastName, recipient.getLastName()));

        String email = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_EMAIL.getTranslationKey(), locale,
                RecipientFields.COLUMN_EMAIL.getReadableName());
        out.append(String.format(ROW_FORMAT, email, recipient.getEmail()));

        String trackingVeto = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_TRACKING_VETO.getTranslationKey(),
                locale, RecipientFields.COLUMN_TRACKING_VETO.getReadableName());
        out.append(String.format(ROW_FORMAT, trackingVeto, recipient.isTrackingVeto()));

        String mailFormat = I18nString.getLocaleStringOrDefault(RecipientFields.COLUMN_MAIL_TYPE.getTranslationKey(),
                locale, RecipientFields.COLUMN_MAIL_TYPE.getReadableName());

        out.append(String.format(ROW_FORMAT, mailFormat, recipient.getMailFormat()));
        out.append(TableSpecialCharacters.CRLF_LINE_SEPARATOR);

        out.append("MORE PROFILE DATA:" + TableSpecialCharacters.CRLF_LINE_SEPARATOR);

        Map<String, Object> otherRecipientData = recipient.getOtherRecipientData();
        for (Map.Entry<String, Object> entry : otherRecipientData.entrySet()) {
            out.append(String.format(ROW_FORMAT, entry.getKey(), entry.getValue()));
        }
        out.append(TableSpecialCharacters.CRLF_LINE_SEPARATOR);

        return out.toString();
    }
}
