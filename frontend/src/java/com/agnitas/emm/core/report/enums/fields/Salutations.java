/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums.fields;

import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;

public enum Salutations implements DatabaseField<Integer, Salutations> {

    MISTER(0, "Mr.", "recipient.gender.0.short"),
    MISSUS(1, "Mrs.", "recipient.gender.1.short"),
    UNKNOWN(2, "Unknown", "recipient.gender.2.short"),
    PRACTICE(4, "Practice", "recipient.gender.4.short"),
    COMPANY(5, "Company", "recipient.gender.5.short");

    private int code;
    private String readableName;
    private String translationKey;

    Salutations(int code, String readableName, String translationKey) {
        this.code = code;
        this.readableName = readableName;
        this.translationKey = translationKey;
    }

    @Nullable
    public static Salutations getByCode(final int code) {
        return (Salutations) DatabaseFieldUtils.getByCode(code, values());
    }

    @Nullable
    public static Salutations getByName(final String readableName) {
        return (Salutations) DatabaseFieldUtils.getByName(readableName, values());
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
