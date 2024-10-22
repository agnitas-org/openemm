/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums.fields;

import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;
import com.agnitas.emm.core.service.RecipientStandardField;

/**
 * @deprecated Use RecipientFieldServiceImpl.RecipientStandardField instead
 */
@Deprecated
public enum RecipientFields implements DatabaseField<String, RecipientFields> {

    /**
     * Common fields for all recipients
     */
    COLUMN_CUSTOMER_ID(RecipientStandardField.CustomerID.getColumnName(), "Customer Id", null),
    COLUMN_SALUTATION(RecipientStandardField.Gender.getColumnName(), "Salutation", "recipient.Salutation"),
    COLUMN_TITLE(RecipientStandardField.Title.getColumnName(), "Title", "Title"),
    COLUMN_FIRST_NAME(RecipientStandardField.Firstname.getColumnName(), "Firstname", "Firstname"),
    COLUMN_LAST_NAME(RecipientStandardField.Lastname.getColumnName(), "lastname", "Lastname"),
    COLUMN_EMAIL(RecipientStandardField.Email.getColumnName(), "email", "mailing.MediaType.0"),
    COLUMN_TRACKING_VETO(RecipientStandardField.DoNotTrack.getColumnName(), "sys_tracking_veto", "recipient.trackingVeto"),
    COLUMN_MAIL_TYPE(RecipientStandardField.Mailtype.getColumnName(), "mailtype", "Mailtype"),
    COLUMN_ENCRYPTED_SENDING(RecipientStandardField.EncryptedSending.getColumnName(), "sys_encrypted_sending", "recipient.encryptedSending");

    private String code;
    private String readableName;
    private String translationKey;

    RecipientFields(String code, String readableName, String translationKey) {
        this.code = code;
        this.readableName = readableName;
        this.translationKey = translationKey;
    }

    @Nullable
    public static RecipientFields getByCode(final String code) {
        return (RecipientFields) DatabaseFieldUtils.getByCode(code, values());
    }

    @Nullable
    public static RecipientFields getByName(final String readableName) {
        return (RecipientFields) DatabaseFieldUtils.getByName(readableName, values());
    }

    public static boolean isContainsCode(final String code) {
        return DatabaseFieldUtils.isContainsCode(code, values());
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getReadableName() {
        return readableName;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
