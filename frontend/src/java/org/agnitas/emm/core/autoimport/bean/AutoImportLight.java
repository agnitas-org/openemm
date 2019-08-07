/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.bean;

/**
 * Light-weight object for AutoImports used for lists, etc.
 *
 * This does not require any cryptographic methods.
 */
public class AutoImportLight {

	private int autoImportId;
	private int companyId;
	private String shortname;
	private boolean active;

	public final int getAutoImportId() {
		return autoImportId;
	}

	public final void setAutoImportId(final int autoImportId) {
		this.autoImportId = autoImportId;
	}

	public final int getCompanyId() {
		return companyId;
	}

	public final void setCompanyId(final int companyId) {
		this.companyId = companyId;
	}

	public final String getShortname() {
		return shortname;
	}

	public final void setShortname(final String shortname) {
		this.shortname = shortname;
	}

	public final boolean isActive() {
		return active;
	}

	public final void setActive(final boolean active) {
		this.active = active;
	}

}
