/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.dto;

import com.agnitas.util.importvalues.Charset;

public enum MailingCharset {
    ISO_8859_1(Charset.ISO_8859_1),
    UTF_8(Charset.UTF_8),
    CHINESE_SIMPLIFIED(Charset.CHINESE_SIMPLIFIED),
    ISO_8859_15(Charset.ISO_8859_15),
    ISO_8859_2(Charset.ISO_8859_2),
    ISO_2022_JP(Charset.ISO_2022_JP);

    private final Charset coreVal;

    MailingCharset(Charset coreVal) {
        this.coreVal = coreVal;
    }

    public Charset getCoreVal() {
        return coreVal;
    }
}
