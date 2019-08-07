/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;

public class UnknownProfileFieldFaultyCodeException extends FaultyCodeException {
	private static final long serialVersionUID = 5756579936784972184L;
	
	private final String name;
	
	public UnknownProfileFieldFaultyCodeException(ProfileFieldAtomEqlNode node, Throwable cause) {
		super(node, "Unknown profile field: " + node.getName(), cause);
		
		this.name = node.getName();
	}
	
	public String getName() {
		return this.name;
	}
}
