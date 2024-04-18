/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans.impl;

import org.agnitas.ecs.backend.beans.ClickStatColor;


/**
 * Implementation of {@link ClickStatColor} bean
 */
public class ClickStatColorImpl implements ClickStatColor {

	private String color;

	private double rangeStart;

	private double rangeEnd;

	public ClickStatColorImpl(double rangeStart, double rangeEnd, String color) {
		this.color = color;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public double getRangeStart() {
		return rangeStart;
	}

	@Override
	public double getRangeEnd() {
		return rangeEnd;
	}

}
