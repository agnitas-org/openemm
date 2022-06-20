/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

public class MapResourceBundle extends ResourceBundle {
	private Map<String, String> data;
	
	public MapResourceBundle(Map<String, String> data) {
		this.data = data;
	}
	
	@Override
	protected Object handleGetObject(String key) {
		return data.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(data.keySet());
	}
}
