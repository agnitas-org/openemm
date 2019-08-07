/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Bean that stores color value for percent range (used for ECS)
 */
public interface ClickStatColor {

	/**
	 * Getter for id property
	 *
	 * @return id
	 */
	public int getId();

	/**
	 * Setter for id property
	 *
	 * @param id new id of object
	 */
	public void setId(int id);

	/**
	 * Getter for companyId property
	 *
	 * @return id of company
	 */
	public int getCompanyId();

	/**
	 * Setter for companyId property
	 *
	 * @param companyId new id of company
	 */
	public void setCompanyId(@VelocityCheck int companyId);

	/**
	 * Getter for color property (should be in HEX i.e. "FF00FF")
	 *
	 * @return color property
	 */
	public String getColor();

	/**
	 * Setter for color property (should be in HEX i.e. "FF00FF")
	 *
	 * @param color new color property for this object
	 */
	public void setColor(String color);

	/**
	 * Getter for rangeStart property - lower limit of percent range
	 *
	 * @return rangeStart property
	 */
	public double getRangeStart();

	/**
	 * Setter for rangeStart property - lower limit of percent range
	 *
	 * @param rangeStart new rangeStart for this object
	 */
	public void setRangeStart(double rangeStart);

	/**
	 * Getter for rangeEnd property - upper limit of percent range
	 *
	 * @return rangeEnd property
	 */
	public double getRangeEnd();

	/**
	 * Setter for rangeEnd property - upper limit of percent range
	 *
	 * @param rangeEnd new rangeEnd for this object
	 */
	public void setRangeEnd(double rangeEnd);

}
