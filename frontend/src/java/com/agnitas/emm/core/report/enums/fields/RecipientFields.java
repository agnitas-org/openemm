/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums.fields;

import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.antlr.v4.runtime.misc.Nullable;

public enum RecipientFields implements DatabaseField<String, RecipientFields> {

    /**
     * Common fields for all recipients
     */
    COLUMN_CUSTOMER_ID(RecipientUtils.COLUMN_CUSTOMER_ID, "Customer Id", null),
    COLUMN_SALUTATION(RecipientUtils.COLUMN_GENDER, "Salutation", "recipient.Salutation"),
    COLUMN_TITLE(RecipientUtils.COLUMN_TITLE, "Title", "Title"),
    COLUMN_FIRST_NAME(RecipientUtils.COLUMN_FIRSTNAME, "Firstname", "Firstname"),
    COLUMN_LAST_NAME(RecipientUtils.COLUMN_LASTNAME, "lastname", "Lastname"),
    COLUMN_EMAIL(RecipientUtils.COLUMN_EMAIL, "email", "mailing.MediaType.0"),
    COLUMN_TRACKING_VETO(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK, "sys_tracking_veto", "recipient.trackingVeto"),
    COLUMN_MAIL_TYPE(RecipientUtils.COLUMN_MAILTYPE, "mailtype", "Mailtype");

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
}
