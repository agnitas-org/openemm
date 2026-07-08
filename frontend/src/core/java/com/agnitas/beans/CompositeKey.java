/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Arrays;

public class CompositeKey {

    private Object[] keys;

    private CompositeKey(Object... keys) {
        this.keys = keys;
    }

    public static CompositeKey of(Object... keys) {
        return new CompositeKey(keys);
    }

    public Object[] getKeys() {
        return keys;
    }

    public Object getKey(int indexOfKey) {
        return keys[indexOfKey];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof CompositeKey) {
            CompositeKey compositeKey = (CompositeKey) object;
            return Arrays.equals(getKeys(), compositeKey.getKeys());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getKeys());
    }
}
