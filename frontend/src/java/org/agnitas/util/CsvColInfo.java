/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

public class CsvColInfo {

	/**
     * Holds value of property name.
     */
    private String name;
    
    /**
     * Holds value of property type.
     */
    private int type;
    
    /**
     * Holds value of property lenght.
     */
    private long length;

	/**
     * Holds value of property nullable.
     */
	private boolean nullable;
    
    /**
     * Holds value of property active.
     */
    private boolean active;
    
    public static final int TYPE_CHAR = 1;
    
    public static final int TYPE_NUMERIC = 2;
    
    public static final int TYPE_DATE = 3;
    
    public static final int TYPE_UNKNOWN = 0;
    
    /**
     * Creates a new instance of CsvColInfo
     */
    public CsvColInfo() {
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }
    
    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Getter for property lenght.
     *
     * @return Value of property lenght.
     */
    public long getLength() {
        return length;
    }
    
    /**
     * Setter for property lenght.
     *
     * @param len
     */
    public void setLength(long len) {
        this.length = len;
    }
    
    /**
     * Getter for property active.
     *
     * @return Value of property active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Setter for property active.
     *
     * @param active New value of property active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

	/**
     * Getter for property nullable.
     *
     * @return Value of property nullable.
     */
	public boolean isNullable() {
		return nullable;
	}

	/**
     * Setter for property nullable.
     *
     * @param nullable New value of property nullable.
     */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
     * Getter for property active.
     *
     * @return Value of property active.
     */
    public String getActive() {
        if(this.active) {
            return "true";
        } else {
            return "false";
        }
    }
    
}
