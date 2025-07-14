/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

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
		if (propertyName == null) {
			return "";
		} else {
			return propertyName;
		}
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyValue() {
		if (propertyValue == null) {
			return "";
		} else {
			return propertyValue;
		}
	}
	
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(propertyName, propertyType, propertyValue);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LinkProperty other = (LinkProperty) o;
		
		return propertyType == other.propertyType &&
				StringUtils.equals(propertyName, other.propertyName) &&
				StringUtils.equals(propertyValue, other.propertyValue);
	}
	
	/**
	 * Simple toString for easier debugging
	 */
	@Override
	public String toString() {
		return getPropertyType() + ": " + getPropertyName() + " = " + getPropertyValue();
	}
}
