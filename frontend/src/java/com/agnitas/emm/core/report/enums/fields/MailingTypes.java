/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums.fields;


import org.agnitas.beans.Mailing;
import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;

public enum MailingTypes implements DatabaseField<Integer, MailingTypes> {

    NORMAL(Mailing.TYPE_NORMAL, "Normal", "mailing.Normal_Mailing"),
    ACTION_BASED(Mailing.TYPE_ACTIONBASED, "Action Based", "mailing.action.based.mailing"),
    DATE_BASED(Mailing.TYPE_DATEBASED, "Date Based", "mailing.Rulebased_Mailing"),
    FOLLOW_UP(Mailing.TYPE_FOLLOWUP, "Follow Up", "mailing.Followup_Mailing"),
    INTERVAL(Mailing.TYPE_INTERVAL, "Interval", "mailing.Interval_Mailing");

    /**
     * Necessary 'cause current enumeration dependent on constants which situate in {@link Mailing}
     * That's why we use its constants.
     */
    private int code;

    private String readableName;

    private String translationKey;

    MailingTypes(int code, String readableName, String translationKey) {
        this.code = code;
        this.readableName = readableName;
        this.translationKey = translationKey;
    }

    @Nullable
    public static MailingTypes getByCode(final int code) {
        return (MailingTypes) DatabaseFieldUtils.getByCode(code, values());
    }

    @Nullable
    public static MailingTypes getByName(final String readableName) {
        return (MailingTypes) DatabaseFieldUtils.getByName(readableName, values());
    }

    @Nullable
    public static String getTranslationKeyByCode(final int code) {
        return DatabaseFieldUtils.getTranslationKeyByCode(code, values());
    }

    public static boolean isContainsCode(final int code) {
        return DatabaseFieldUtils.isContainsCode(code, values());
    }

    @Override
    public Integer getCode() {
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
