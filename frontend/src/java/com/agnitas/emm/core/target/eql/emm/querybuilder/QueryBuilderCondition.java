/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode.InfixOperator;

public enum QueryBuilderCondition {

	AND("AND", InfixOperator.AND),
	OR("OR", InfixOperator.OR);
	
	private final String queryBuilderName;
	private final InfixOperator eqlOperator;
	
	QueryBuilderCondition(final String name, final InfixOperator eqlOperator) {
		this.queryBuilderName = name;
		this.eqlOperator = eqlOperator;
	}
	
	public final String queryBuilderName() {
		return this.name();
	}
	
	public final InfixOperator eqlOperator() {
		return this.eqlOperator;
	}
	
	public static final QueryBuilderCondition findByQueryBuilderName(final String name) {
		for(QueryBuilderCondition c : values()) {
			if(c.queryBuilderName.equals(name)) {
				return c;
			}
		}
		
		return null;
	}
	
	public static final QueryBuilderCondition findByEqlOperator(final InfixOperator eqlOperator) {
		for(QueryBuilderCondition c : values()) {
			if(c.eqlOperator == eqlOperator) {
				return c;
			}
		}
		
		return null;
	}
}
