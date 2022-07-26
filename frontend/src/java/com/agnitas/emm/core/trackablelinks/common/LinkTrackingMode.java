/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.common;

public enum LinkTrackingMode {

	NONE(0),
	TEXT_ONLY(1),
	HTML_ONLY(2),
	TEXT_AND_HTML(3);

	private final int mode;
	
	private LinkTrackingMode(final int mode) {
		this.mode = mode;
	}
	
	public final int getMode() {
		return this.mode;
	}

	public static LinkTrackingMode getDefault() {
		return TEXT_AND_HTML;
	}

}
