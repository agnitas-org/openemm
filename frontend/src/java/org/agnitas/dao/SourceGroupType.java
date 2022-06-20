/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

public enum SourceGroupType {
	SubscriberInterface("A"),
	File("D"),
	Other("O"),
	AutoinsertForms("FO"),
	Default("DD"),
	SoapWebservices("WS"),
	Facebook("FB"),
	DataAgent("DA"),
	RestfulService("RS"),
	Velocity("V")
	
	/** Old Webservices, DO NOT USE ANYMORE **/
	//AxisWebservices("WA")
	;
	
	private String storageString;
	
	SourceGroupType(String storageString) {
		this.storageString = storageString;
	}
	
	public String getStorageString() {
		return storageString;
	}
	
	public static SourceGroupType getUserSourceGroupType(String storageString) throws Exception {
		for (SourceGroupType sourceGroupType : SourceGroupType.values()) {
			if (sourceGroupType.storageString.equals(storageString)) {
				return sourceGroupType;
			}
		}
		throw new Exception("Unknwon storageString for SourceGroupType: " + storageString);
	}
}

