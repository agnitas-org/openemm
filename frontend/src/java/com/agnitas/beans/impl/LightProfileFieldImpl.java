/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Objects;

import com.agnitas.beans.LightProfileField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @deprecated Use RecipientFieldDescription instead
 */
@Deprecated
public class LightProfileFieldImpl implements LightProfileField {
	protected String column;
	protected String shortname = "";

	@Override
	public String getColumn() {
		return column;
	}
	
	@Override
	public void setColumn(String column) {
		if (column != null) {
			this.column = column.toLowerCase();
		} else {
			this.column = null;
		}

		// Fallback for special cases in which the shortname is not set
		if (StringUtils.isEmpty(shortname)) {
			if (column != null) {
				shortname = column.toUpperCase();
			} else {
				shortname = null;
			}
		}
	}

	@Override
	public String getShortname() {
		return shortname;
	}
	
	@Override
	public void setShortname(String shortname) {
		if (shortname == null) {
			this.shortname = "";
		} else {
			this.shortname = shortname;
		}
	}

	@Override
	public boolean equals(Object rhs) {
		if (rhs instanceof LightProfileField) {
			LightProfileField other = (LightProfileField) rhs;
			return StringUtils.equalsIgnoreCase(column, other.getColumn());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(column);
	}

	/**
	 * String representation for easier debugging
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("column", column)
			.append("shortname", shortname)
			.build();
	}
}
