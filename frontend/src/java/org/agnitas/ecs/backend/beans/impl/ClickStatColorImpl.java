/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans.impl;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Implementation of {@link ClickStatColor} bean
 */
public class ClickStatColorImpl implements ClickStatColor {

	private int id;

	private int companyId;

	private String color;

	private double rangeStart;

	private double rangeEnd;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getCompanyId() {
		return companyId;
	}

	@Override
	public void setCompanyId(@VelocityCheck int companyId) {
		this.companyId = companyId;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public double getRangeStart() {
		return rangeStart;
	}

	@Override
	public void setRangeStart(double rangeStart) {
		this.rangeStart = rangeStart;
	}

	@Override
	public double getRangeEnd() {
		return rangeEnd;
	}

	@Override
	public void setRangeEnd(double rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
}
