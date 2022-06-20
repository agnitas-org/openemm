/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements Iterable<Object> {
	private List<Object> items = new ArrayList<>();

	public void add(Object value) {
		items.add(value);
	}

	public Object remove(Object value) {
		return items.remove(value);
	}

	public Object get(int index) {
		return items.get(index);
	}

	public int size() {
		return items.size();
	}

	@Override
	public Iterator<Object> iterator() {
		return items.iterator();
	}

	@Override
	public String toString() {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream(); JsonWriter writer = new JsonWriter(output, "UTF-8");) {
			writer.add(this);
			writer.flush();
			return new String(output.toByteArray(), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject) {
			return true;
		} else if (otherObject != null && otherObject instanceof JsonArray) {
			JsonArray otherArray = (JsonArray) otherObject;
			if (this.size() != otherArray.size()) {
				return false;
			} else {
				for (int i = 0; i < this.size(); i++) {
					Object thisValue = this.get(i);
					Object otherValue = otherArray.get(i);
					if ((thisValue != otherValue)
						&& (thisValue != null && !thisValue.equals(otherValue))) {
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}
}
