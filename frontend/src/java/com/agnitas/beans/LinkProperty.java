/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.apache.commons.lang.StringUtils;

public class LinkProperty {
	public enum PropertyType {
		LinkExtension;
		
		public static PropertyType parseString(String value) throws Exception {
			if (StringUtils.isBlank(value)) {
				throw new Exception("Invalid empty type of LinkProperty");
			} else {
				for (PropertyType type : PropertyType.values()) {
					if (type.toString().equalsIgnoreCase(value)) {
						return type;
					}
				}
				throw new Exception("Invalid type of LinkProperty: " + value);
			}
		}
	}
	
	private PropertyType propertyType;
	private String propertyName;
	private String propertyValue;
	
	public LinkProperty(PropertyType propertyType, String propertyName, String propertyValue) {
		this.propertyType = propertyType;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	public PropertyType getPropertyType() {
		return propertyType;
	}
	
	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyValue() {
		return propertyValue;
	}
	
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
		result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof LinkProperty)) {
			return false;
		} else {
			LinkProperty linkProperty = (LinkProperty) object;
			if (this.getPropertyType() == linkProperty.getPropertyType() 
					&& ((this.getPropertyName() == null && linkProperty.getPropertyName() == null)
						|| (this.getPropertyName() != null && this.getPropertyName().equals(linkProperty.getPropertyName())))
					&& ((this.getPropertyValue() == null && linkProperty.getPropertyValue() == null)
						|| (this.getPropertyValue() != null && this.getPropertyValue().equals(linkProperty.getPropertyValue())))) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Simple toString for easier debugging
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getPropertyType());
		result.append(": ");
		result.append(getPropertyName());
		result.append(" = ");
		result.append(getPropertyValue());
		return result.toString();
	}
}
