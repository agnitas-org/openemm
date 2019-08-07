/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

public enum EmmActionType {
	Link(0),
	Form(1),
	All(9);
	
	private int actionTypeCode;
	
	EmmActionType(int actionTypeCode) {
		this.actionTypeCode = actionTypeCode;
	}
	
	public int getActionTypeCode() {
		return actionTypeCode;
	}
	
	public static EmmActionType getEmmActionTypeByID(int id) throws Exception {
		for (EmmActionType actionType : EmmActionType.values()) {
			if (actionType.actionTypeCode == id) {
				return actionType;
			}
		}
		throw new Exception("Invalid actionType: " + id);
	}
	
	public static EmmActionType getEmmActionTypeByName(String name) throws Exception {
		for (EmmActionType actionType : EmmActionType.values()) {
			if (actionType.name().equalsIgnoreCase(name)) {
				return actionType;
			}
		}
		throw new Exception("Invalid actionType: " + name);
	}
}
