/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.Locale;
import java.util.Objects;

import org.agnitas.beans.BindingEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.emm.core.report.converter.GenericLocalizableConverter;
import com.agnitas.emm.core.report.dto.RecipientStatusHistoryDto;
import com.agnitas.emm.core.report.dto.impl.RecipientStatusHistoryDtoImpl;
import com.agnitas.emm.core.report.enums.fields.RecipientMutableFields;
import com.agnitas.messages.I18nString;

@Component
public class RecipientStatusHistoryDtoConverter implements GenericLocalizableConverter<ComRecipientHistory, RecipientStatusHistoryDto> {

    private static final String NOT_SET = "not set";

    @Override
    public RecipientStatusHistoryDto convert(ComRecipientHistory recipientHistory, Locale locale) {
        RecipientStatusHistoryDto statusHistoryDto = new RecipientStatusHistoryDtoImpl();

        statusHistoryDto.setChangeDate(recipientHistory.getChangeDate());
        statusHistoryDto.setChangeDescription(getTranslatedDescription(recipientHistory, locale));
        statusHistoryDto.setOldValue(getOldTranslatedValue((String) recipientHistory.getOldValue(),
                recipientHistory.getFieldName(), locale));
        statusHistoryDto.setNewValue(getNewTranslatedValue((String) recipientHistory.getNewValue(),
                recipientHistory.getFieldName(), locale));

        return statusHistoryDto;
    }

    /**
     * Translate the descriptions ONLY for BindingHistory entries.
     * Descriptions of ProfileHistory entries returns without any changes.
     *
     * @param recipientHistory general entry contains both BindingHistory(Mailinglist changes) and ProfileHistory.
     * @param locale           locale for current user.
     * @return translated description.
     */
    private String getTranslatedDescription(ComRecipientHistory recipientHistory, Locale locale) {
        // we translate only BindingHistory entries so in case of ProfileHistory change we return value without changes
        RecipientMutableFields field = RecipientMutableFields.getByCode(recipientHistory.getFieldName());
        if (Objects.isNull(field) || field.isProfileHistoryField()) {
            return recipientHistory.getFieldName();
        }

        // translate mailinglist info
        String prefix = I18nString.getLocaleString("Mailinglist", locale);
        String mailinglistInfo = String.format("%s %s", prefix, recipientHistory.getMailingList());

        // translate media info
        String mediaInfo = "";
        Number mediaTypeCode = recipientHistory.getMediaType();
        if (Objects.nonNull(mediaTypeCode)) {
            String mediaTypeTranslation = I18nString.getLocaleString("mailing.MediaType." + mediaTypeCode, locale);
            mediaInfo = String.format("Medium: %s", mediaTypeTranslation);
        }

        // translate changed field info
        String fieldTranslation = I18nString.getLocaleString("Field", locale);
        String changeTranslationKey = RecipientMutableFields.getTranslationKeyByCode(recipientHistory.getFieldName());
        String changeTranslation = I18nString.getLocaleString(changeTranslationKey, locale);
        String fieldInfo = String.format("%s : %s", fieldTranslation, changeTranslation);

        return String.format("%s %s %s", mailinglistInfo, mediaInfo, fieldInfo);
    }

    /**
     * Translates NewValue of history entry. It works in the same way as
     * {@link RecipientStatusHistoryDtoConverter#getOldTranslatedValue(String, String, Locale)} but
     * contains additional checking for deleted Binding and deleted Mailing list.
     *
     * @param changeValue  value which was changed.
     * @param changedField type of change.
     * @param locale       locale for current user.
     * @return translated Value.
     */
	private String getNewTranslatedValue(String changeValue, String changedField, Locale locale) {
		String deletedText = I18nString.getLocaleString("Deleted", locale);

		// in case of we don't have the rule for current kind of change we return value without any changes
		RecipientMutableFields fieldByText = RecipientMutableFields.getByCode(changedField);
		if (Objects.isNull(fieldByText)) {
			return StringUtils.defaultString(changeValue, NOT_SET);
		}

		// deletion actions have a special behaviour. They applicable only for `NewValue`
		switch (fieldByText) {
			case MAILINGLIST_DELETED:
				String mailinglistText = I18nString.getLocaleString("Mailinglist", locale);
				return String.format("%s %s", mailinglistText, deletedText).toUpperCase();
	
			case CUSTOMER_BINDING_DELETED:
				String bindingText = I18nString.getLocaleString("Binding", locale);
				return String.format("%s %s", bindingText, deletedText).toUpperCase();
	
			default:
				// going ahead if it's not deletion action
				return getOldTranslatedValue(changeValue, changedField, locale);
		}
	}

    /**
     * Translates OldValue of history entry.
     *
     * @param changeValue  value which was changed.
     * @param changedField type of change.
     * @param locale       locale for current user.
     * @return translated Value.
     */
	private String getOldTranslatedValue(String changeValue, String changedField, Locale locale) {
		// in case of we don't have the rule for current kind of change we return value without any changes
		RecipientMutableFields fieldByText = RecipientMutableFields.getByCode(changedField);
		if (Objects.isNull(fieldByText)) {
			return StringUtils.defaultString(changeValue, NOT_SET);
		}

		switch (fieldByText) {
			case USER_TYPE:
				return StringUtils.defaultString(BindingEntry.UserType.getReadableNameByCode(changeValue), NOT_SET);
	
			case USER_STATUS:
				Number status = NumberUtils.toInt(changeValue);
				if (status.equals(0)) {
					return I18nString.getLocaleString("recipient.NewRecipient", locale);
				}
				return I18nString.getLocaleString("recipient.MailingState" + status, locale);
	
			case GENDER:
				return I18nString.getLocaleString("recipient.gender." + NumberUtils.toInt(changeValue) + ".short", locale);
	
			case MAIL_TYPE:
				return I18nString.getLocaleString("MailType." + NumberUtils.toInt(changeValue), locale);
	
			default:
				// in case of we don't have rule for this kind of change but this change is known
				return StringUtils.defaultString(changeValue, NOT_SET);
		}
	}
}
