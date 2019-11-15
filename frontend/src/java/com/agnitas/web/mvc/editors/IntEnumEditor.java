/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc.editors;

import java.beans.PropertyEditorSupport;
import java.util.Objects;

import com.agnitas.beans.IntEnum;

/**
 * A property editor to be used when any enum that implements {@link IntEnum} should be (de-) serialized as integer.
 */
public class IntEnumEditor<T extends Enum<T> & IntEnum> extends PropertyEditorSupport {
    private Class<T> type;

    public IntEnumEditor(Class<T> type) {
        this.type = Objects.requireNonNull(type, "type == null");
    }

    @Override
    public String getAsText() {
        Object value = getValue();

        if (type.isInstance(value)) {
            return Integer.toString(((IntEnum) value).getId());
        } else {
            return null;
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(IntEnum.fromId(type, Integer.parseInt(text), false));
        } catch (Exception e) {
            throw new IllegalArgumentException(text);
        }
    }
}
