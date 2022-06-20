/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

public class PermissionInfo {
	private String category;
	private String subCategory;
	private int sortOrder;
	private String featurePackage;
	
	public PermissionInfo(String category, String subCategory, int sortOrder, String featurePackage) {
		this.category = category;
		this.subCategory = subCategory;
		this.sortOrder = sortOrder;
		this.featurePackage = featurePackage;
	}

	public String getCategory() {
		return category;
	}
	
	public String getSubCategory() {
		return subCategory;
	}
	
	public int getSortOrder() {
		return sortOrder;
	}
	
	public String getFeaturePackage() {
		return featurePackage;
	}
}
