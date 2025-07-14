/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.web;

import com.agnitas.beans.ProfileField;

public final class ProfileFieldListItem {

	private final String internalName;
	private final String shortname;
	
	public ProfileFieldListItem(final String internalName, final String shortname) {
		this.internalName = internalName;
		this.shortname = shortname;
	}
	
	public static final ProfileFieldListItem from(final ProfileField profileField) {
		return new ProfileFieldListItem(profileField.getColumn(), profileField.getShortname());
	}

	public final String getInternalName() {
		return internalName;
	}

	public final String getShortname() {
		return shortname;
	}
	
}
