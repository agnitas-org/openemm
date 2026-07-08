/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

/**
 * Heavy-weight extension of {@link TargetLight} interface providing additional and structural
 * information on target groups.
 */
public interface Target extends TargetLight {

    /**
     * Getter for property targetSQL.
     * 
     * @return Value of property targetSQL.
     */
    String getTargetSQL();

    /**
     * Setter for property targetSQL.
     * 
     * @param sql New value of property targetSQL.
     */
    void setTargetSQL(String sql);

	boolean isAdminTestDelivery();
	void setAdminTestDelivery( boolean adminTestDelivery);

	/**
	 * Set EQL representation of target group.
	 * 
	 * @param eql EQL representation of target group
	 */
	void setEQL(String eql);
	
	/**
	 * Returns EQL representation of target group.
	 * 
	 * @return EQL representation of target group
	 */
	String getEQL();

}
