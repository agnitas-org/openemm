/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * This Properties class has a ordered list of keys to keep the insertion order of entries from the read properties file
 */
public class OrderedProperties extends Properties {
	private static final long serialVersionUID = 9213377024706186516L;
	
	private final LinkedHashSet<Object> keys = new LinkedHashSet<>();

	@Override
    public synchronized Enumeration<Object> keys() {
        return Collections.<Object>enumeration(keys);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        keys.remove(key);

        keys.add(key);
        return super.put(key, value);
    }
}
