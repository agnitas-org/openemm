/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

public class PercentageNumber {
	private int number;
	private float percentage;
	
	public PercentageNumber() {
		this.number = 0;
		this.percentage = 0;
	}
	
	public PercentageNumber(int number) {
		this.number = number;
		this.percentage = 0;
	}
	
	public PercentageNumber(int number, float percentage) {
		this.number = number;
		this.percentage = percentage;
	}
	
	public int getNumber() {
		return number;
	}
	
	public PercentageNumber setNumber(int number) {
		this.number = number;
		return this;
	}
	
	public float getPercentage() {
		return percentage;
	}
	
	public PercentageNumber setPercentage(float percentage) {
		this.percentage = percentage;
		return this;
	}
	
	public PercentageNumber setPercentageForTotal(int total) {
		if (total > 0) {
			this.percentage = Math.round(number * 100f / total);
		} else {
			this.percentage = 0;
		}
		return this;
	}
}
