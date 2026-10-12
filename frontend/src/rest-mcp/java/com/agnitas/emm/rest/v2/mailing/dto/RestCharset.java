/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.dto;

import java.util.Arrays;

import com.agnitas.util.importvalues.Charset;
import org.apache.commons.lang3.Strings;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public enum RestCharset {
    ISO_8859_1(Charset.ISO_8859_1),
    UTF_8(Charset.UTF_8),
    CHINESE_SIMPLIFIED(Charset.CHINESE_SIMPLIFIED),
    ISO_8859_15(Charset.ISO_8859_15),
    ISO_8859_2(Charset.ISO_8859_2),
    ISO_2022_JP(Charset.ISO_2022_JP);

    private final Charset coreVal;

    RestCharset(Charset coreVal) {
        this.coreVal = coreVal;
    }

    public Charset getCoreVal() {
        return coreVal;
    }

    public static RestCharset parse(String charset) {
        return Arrays.stream(values())
                .filter(value -> Strings.CI.equals(value.toString(), charset))
                .findFirst()
                .orElseGet(() -> RestCharset.from(Charset.getCharsetByName(charset)));
    }

    public static RestCharset from(Charset type) {
        return Arrays.stream(values())
                .filter(value -> value.coreVal == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Charset: " + type));
    }

    public static class RestCharsetSerializer extends ValueSerializer<RestCharset> {
        @Override
        public void serialize(
                RestCharset value,
                tools.jackson.core.JsonGenerator gen,
                SerializationContext ctxt
        ) throws JacksonException {
            gen.writeString(value.getCoreVal().getCharsetName());
        }
    }
}
