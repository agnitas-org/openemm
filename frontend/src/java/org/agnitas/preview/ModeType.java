/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

public enum ModeType {
	RECIPIENT(1), TARGET_GROUP(2);

	private final int code;

	ModeType(int code) {
		this.code = code;
	}

	public static ModeType getByCode(int code) {
		for (ModeType type : values()) {
			if (type.getCode() == code) {
				return type;
			}
		}
		return null;
	}

	public int getCode() {
		return code;
	}
}
