/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

public enum VariableType {
	/**
	 * Free text to the maximum length of 255 characters. The backslash and control characters are not allowed.
	 */
	String("string", 10),
	
	/**
	 *  A whole number within the range from -2^31 to 2^31-1.
	 *  E.g.: "643", "-12".
	 */
	Integer("integer", 20),
	
	/**
	 * A boolean value.
	 * These values are accepted: "true", "false", "0", "1".
	 */
	Boolean("boolean", 30),
	
	/**
	 * A date in ISO format: YYYY-MM-DD.
	 * YYYY stands for the year.
	 * MM stands for the month.
	 * DD stands for the day.
	 * E.g.: "2016-04-14" for April 14, 2016
	 */
	Date("date", 40),
	
	/**
	 * An image file name as used for an asset in the editor.
	 * The maximum length is 255 characters.
	 * Folder names and path delimiters are not allowed.
	 * E.g.: "myProduct.jpg".
	 */
	Image("image", 50),
	
	/**
	 * A valid URL up to the maximum length of 255 characters referencing an image.
	 * The application will try to download the image from this URL.
	 * The path part of the URL must end with either ".png" or ".jpg".
	 * E.g.: "https://image.yourdomain.com/folder/name.png?param=value"
	 */
	ImageUrl("image_url", 60),
	
	/**
	 * A fraction number.
	 * The value range is approximately ±3.40282347E+38F (6-7 significant decimal digits).
	 * E.g.: "123", "-12.34", "1.23e7"
	 */
	Float("float", 70),
	
	/**
	 * A postal code.
	 * Currently it needs to be a valid German ZIP code of five digits.
	 * There must be exactly one variable with this data type. The value is mandatory.
	 * E.g.: "01234".
	 */
	Zip("zip", 80),
	
	/**
	 * A two uppercase letter country code according to the ISO 3166-1-alpha-2 standard.
	 * E.g.: "DE".
	 */
	CountryCode("countryCode", 90);

	private String key;
	private int dataTypeId;

	public String getKey() {
		return key;
	}

	public int getDataTypeId() {
		return dataTypeId;
	}

	VariableType(String key, int dataTypeId) {
		this.key = key;
		this.dataTypeId = dataTypeId;
	}

	public static VariableType getByKey(java.lang.String variableTypeString) throws Exception {
		for (VariableType variableType : VariableType.values()) {
			if (variableType.getKey().replace("_", "").equalsIgnoreCase(variableTypeString)) {
				return variableType;
			}
		}
		throw new Exception("Unknown variableType: " + variableTypeString);
	}
}
