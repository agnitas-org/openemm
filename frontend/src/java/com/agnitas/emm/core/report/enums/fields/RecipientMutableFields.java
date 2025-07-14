/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums.fields;

import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.beans.RecipientHistory;
import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;


/**
 * Contains list of fields that can be changed for recipient.
 */
public enum RecipientMutableFields implements DatabaseField<String, RecipientMutableFields> {

    // mutable fields from binding history
    USER_TYPE(RecipientHistory.USER_TYPE, "User Type", "recipient.history.usertype", true),
    USER_STATUS(RecipientHistory.USER_STATUS, "User Status", "recipient.Status", true),
    USER_REMARK(RecipientHistory.USER_REMARK, "User Remark", "recipient.Remark", true),
    EXIT_MAILING_ID(RecipientHistory.EXIT_MAILING_ID, "Exit Mailing Id", "recipient.history.mailingid", true),
    MAILINGLIST_DELETED(RecipientHistory.MAILINGLIST_DELETED, "Mailinglist Deleted", "Mailinglist", true),
    CUSTOMER_BINDING_DELETED(RecipientHistory.CUSTOMER_BINDING_DELETED, "Customer Binding Deleted", "Binding", true),

    // mutable fields from profile history
    FIRST_NAME(RecipientHistory.FIRSTNAME, "Firstname", "recipient.Firstname", false),
    LAST_NAME(RecipientHistory.LASTNAME, "lastname", "recipient.Lastname", false),
    GENDER(RecipientHistory.GENDER, "Gender", "Gender", false),
    MAIL_TYPE(RecipientHistory.MAILTYPE, "Mailtype", "Mailtype", false),
    TITLE(RecipientHistory.TITLE, "Title", "Title", false),
    DATASOURCE_ID(RecipientHistory.DATASOURCE_ID, "Datasource Id", "recipient.DatasourceId", false),
    EMAIL(RecipientHistory.EMAIL, "Email", "mailing.MediaType.0", false);

    /**
     * Necessary 'cause current enumeration dependent on constants which situate in {@link RecipientHistory}
     */
    private String code;

    /**
     * Represent default readable value.
     */
    private String readableName;

    /**
     * Key is need for translation current type of change.
     */
    private String translationKey;

    /**
     * Specific value for current type of enum.
     * Current enum contains fields name from two different tables,
     * so this fields allows to distinguish Binding History fields from Profile History fields.
     */
    private boolean isBindingHistoryMutableField;

    RecipientMutableFields(String code, String readableName, String translationKey, boolean isBindingHistoryMutableField) {
        this.code = code;
        this.readableName = readableName;
        this.translationKey = translationKey;
        this.isBindingHistoryMutableField = isBindingHistoryMutableField;
    }

    @Nullable
    public static RecipientMutableFields getByCode(final String code) {
        return (RecipientMutableFields) DatabaseFieldUtils.getByCode(code, values());
    }

    @Nullable
    public static RecipientMutableFields getByName(final String readableName) {
        return (RecipientMutableFields) DatabaseFieldUtils.getByName(readableName, values());
    }

    @Nullable
    public static String getTranslationKeyByCode(final String code) {
        return DatabaseFieldUtils.getTranslationKeyByCode(code, values());
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

    public boolean isBindingHistoryField() {
        return isBindingHistoryMutableField;
    }

    public boolean isProfileHistoryField() {
        return !isBindingHistoryMutableField;
    }
}
