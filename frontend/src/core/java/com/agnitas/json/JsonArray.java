/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements Iterable<Object> {

	private final List<Object> items = new ArrayList<>();

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
		try (
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				JsonWriter writer = new JsonWriter(output)
		) {
			writer.add(this);
			writer.flush();
			return output.toString(StandardCharsets.UTF_8);
		} catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	@Override
	public int hashCode() {
		final int prime = 31;
        return prime + items.hashCode();
	}

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject) {
			return true;
		} else if (otherObject instanceof JsonArray otherArray) {
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
