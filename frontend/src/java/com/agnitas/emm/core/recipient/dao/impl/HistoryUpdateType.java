/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dao.impl;

/**
 * Enuemration for encoding type of profile field modification.
 */
public enum HistoryUpdateType {
	/** New recipient added. */
	INSERT(2),

	/** Existing recipient modified. */
	UPDATE(1),

	/** Existing recipient deleted. */
	DELETE(0);
	
	/** Type code of modification. */
	public final int typeCode;
	
	/**
	 * Creates new enum item.
	 * 
	 * @param typeCode code for modification
	 */
	HistoryUpdateType(final int typeCode) {
		this.typeCode = typeCode;
	}
}
