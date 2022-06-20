/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.util.List;
import java.util.Map;

public class ActionsTokenResolver {
	
	private Map<String, String> tokens;
	
	private  Map<String, List<ComplexToken>> complexTokens;


	public void setTokens(Map<String, String> tokens) {
		this.tokens = tokens;
	}

	public void setComplexTokens(Map<String, List<ComplexToken>> complexTokens) {
		this.complexTokens = complexTokens;
	}


	public String get(String token) {
		return tokens.get(token);
	}
	
	public List<ComplexToken> getComplex(String action) {
		return complexTokens.get(action);
	}
}
