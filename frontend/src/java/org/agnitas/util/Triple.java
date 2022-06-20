/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

public class Triple<T1, T2, T3> {
	private T1 value1;
	private T2 value2;
	private T3 value3;
	
	public Triple(T1 value1, T2 value2, T3 value3) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}
	
	public T1 getFirst() {
		return value1;
	}
	
	public T2 getSecond() {
		return value2;
	}
	
	public T3 getThird() {
		return value3;
	}
}
