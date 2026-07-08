/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

public class TagID {
	private String	name;
	private String	tagDefinition;
	private DynName	dynName;
	public TagID (String name, String tagDefinition, DynName dynName) {
		this.name = name;
		this.tagDefinition = tagDefinition;
		this.dynName = dynName;
	}
	public String toString () {
		return name + "(" + (dynName != null ? dynName.name : "component") + "):" + tagDefinition;
	}
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj != null) && (obj instanceof TagID other)) {
			return other.toString ().equals (toString ());
		}
		return false;
	}
	public int hashCode () {
		return toString ().hashCode ();
	}
	public String dynName () {
		return dynName != null ? dynName.name : null;
	}
	public long dynID () {
		return dynName != null ? dynName.id : 0;
	}
	public long divID () {
		return dynName != null ? dynName.getDivId () : 0;
	}
}
