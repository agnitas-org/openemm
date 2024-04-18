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
import com.agnitas.emm.core.report.generator.constants.TableSpecialCharacters;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;
import com.agnitas.messages.I18nString;

@Component
public class RecipientEntityDtoPrinter implements Printer<RecipientEntityDto> {

    private static final String ROW_FORMAT = "%-35s\u00A0:\u00A0%s\r\n";

    @Override
    public String print(RecipientEntityDto recipient, Locale locale) {
        StringBuilder out = new StringBuilder("RECIPIENT DATA:" + TableSpecialCharacters.CRLF_LINE_SEPARATOR);

        String salutation = I18nString.getLocaleStringOrDefault(RecipientStandardField.Gender.getMessageKey(),
                locale, RecipientStandardField.Gender.getColumnName());
        out.append(String.format(ROW_FORMAT, salutation, recipient.getSalutation()));

        String title = I18nString.getLocaleStringOrDefault(RecipientStandardField.Title.getMessageKey(), locale,
                RecipientStandardField.Title.getColumnName());
        out.append(String.format(ROW_FORMAT, title, recipient.getTitle()));

        String firstName = I18nString.getLocaleStringOrDefault(RecipientStandardField.Firstname.getMessageKey(),
                locale, RecipientStandardField.Firstname.getColumnName());
        out.append(String.format(ROW_FORMAT, firstName, recipient.getFirstName()));

        String lastName = I18nString.getLocaleStringOrDefault(RecipientStandardField.Lastname.getMessageKey(),
                locale, RecipientStandardField.Lastname.getColumnName());
        out.append(String.format(ROW_FORMAT, lastName, recipient.getLastName()));

        String email = I18nString.getLocaleStringOrDefault(RecipientStandardField.Email.getMessageKey(), locale,
                RecipientStandardField.Email.getColumnName());
        out.append(String.format(ROW_FORMAT, email, recipient.getEmail()));

        String trackingVeto = I18nString.getLocaleStringOrDefault(RecipientStandardField.DoNotTrack.getMessageKey(),
                locale, RecipientStandardField.DoNotTrack.getColumnName());
        out.append(String.format(ROW_FORMAT, trackingVeto, recipient.isTrackingVeto()));

        String mailFormat = I18nString.getLocaleStringOrDefault(RecipientStandardField.Mailtype.getMessageKey(),
                locale, RecipientStandardField.Mailtype.getColumnName());

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
