/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.util.List;

public class ComplexToken {
	String subaction;
	String aggregation;
	List<String> tokens;
	
	public String getSubaction() {
		return subaction;
	}
	public void setSubaction(String subaction) {
		this.subaction = subaction;
	}
	public String getAggregation() {
		return aggregation;
	}
	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}
	public List<String> getTokens() {
		return tokens;
	}
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}
}
