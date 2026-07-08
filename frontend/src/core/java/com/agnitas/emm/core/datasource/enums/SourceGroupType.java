/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.enums;

import java.util.Objects;

public enum SourceGroupType {

	SubscriberInterface("A", GeneralDataSourceType.USER),
	File("D", GeneralDataSourceType.USER),
	Other("O", GeneralDataSourceType.OTHER),
	AutoinsertForms("FO", GeneralDataSourceType.FORMS),
	Default("DD", GeneralDataSourceType.OTHER),
	SoapWebservices("WS", GeneralDataSourceType.API),
	Facebook("FB", GeneralDataSourceType.API),
	DataAgent("DA", GeneralDataSourceType.OTHER),
	RestfulService("RS", GeneralDataSourceType.API),
	Velocity("V", GeneralDataSourceType.FORMS);
	
	private final String storageString;
	private final GeneralDataSourceType generalType;
	
	SourceGroupType(String storageString, GeneralDataSourceType generalType) {
		this.storageString = storageString;
        this.generalType = Objects.requireNonNull(generalType);
    }
	
	public String getStorageString() {
		return storageString;
	}

	public GeneralDataSourceType getDataSourceType() {
		return generalType;
	}

	public static SourceGroupType getUserSourceGroupType(String storageString) {
		for (SourceGroupType sourceGroupType : SourceGroupType.values()) {
			if (sourceGroupType.storageString.equals(storageString)) {
				return sourceGroupType;
			}
		}
		throw new IllegalArgumentException("Unknown storageString for SourceGroupType: " + storageString);
	}
}

