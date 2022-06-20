/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class CategoryTwoValues {
	private String category;
	private int category_index;
	private double value_1;
	private double value_2;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getCategory_index() {
		return category_index;
	}
	public void setCategory_index(int category_index) {
		this.category_index = category_index;
	}
	public double getValue_1() {
		return value_1;
	}
	public void setValue_1(double value_1) {
		this.value_1 = value_1;
	}
	public double getValue_2() {
		return value_2;
	}
	public void setValue_2(double value_2) {
		this.value_2 = value_2;
	}
	
}
