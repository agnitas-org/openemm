/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans;

/**
 * Bean that stores color value for percent range (used for ECS)
 */
public interface ClickStatColor {


	/**
	 * Getter for color property (should be in HEX i.e. "FF00FF")
	 *
	 * @return color property
	 */
	String getColor();

	/**
	 * Getter for rangeStart property - lower limit of percent range
	 *
	 * @return rangeStart property
	 */
	double getRangeStart();

	/**
	 * Getter for rangeEnd property - upper limit of percent range
	 *
	 * @return rangeEnd property
	 */
	double getRangeEnd();

}
