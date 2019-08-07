/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode.Operator;

public enum QueryBuilderOperator {
	EQ("equal", 						Operator.EQ, 	true, true),
	NEQ("not_equal", 					Operator.NEQ, 	true, true),
	LT("less", 							Operator.LT, 	true, true),
	LEQ("less_or_equal",	 			Operator.LEQ, 	true, true),
	GT("greater", 						Operator.GT, 	true, true),
	GEQ("greater_or_equal",	 			Operator.GEQ, 	true, true),
	MOD("mod",	 	            		Operator.MOD, 	true, false),
	LIKE("like", 						Operator.LIKE, false, true),
	NOT_LIKE("not_like", 				Operator.LIKE, false, true),
	CONTAINS("contains", 				Operator.CONTAINS, false, true),
	NOT_CONTAINS("not_contains",		Operator.CONTAINS, false, true),
	STARTS_WITH("begins_with",			Operator.STARTS_WITH, false, true),
	NOT_STARTS_WITH("not_begins_with",	Operator.STARTS_WITH, false, true);

	private final String queryBuilderName;
	private final Operator eqlOperator;
	private final boolean supportsNumeric;
	private final boolean supportsText;
	
	QueryBuilderOperator(final String queryBuilderName, final Operator eqlOperator, final boolean supportsNumeric, final boolean supportesText) {
		this.queryBuilderName = queryBuilderName;
		this.eqlOperator = eqlOperator;
		this.supportsNumeric = supportsNumeric;
		this.supportsText = supportesText;
	}
	
	public final boolean isNumericOperator() {
		return this.supportsNumeric;
	}
	
	public final boolean isTextOperator() {
		return this.supportsText;
	}
	
	public final String queryBuilderName() {
		return this.queryBuilderName;
	}
	
	public final Operator eqlOperator() {
		return this.eqlOperator;
	}
	
	public static final QueryBuilderOperator findByQueryBuilderName(final String name) {
		for(QueryBuilderOperator op : values()) {
			if(op.queryBuilderName.equals(name)) {
				return op;
			}
		}
		
		return null;
	}
	
	public static final QueryBuilderOperator findByEqlOperator(final Operator eqlOperator) {
		for(QueryBuilderOperator op : values()) {
			if(op.eqlOperator == eqlOperator) {
				return op;
			}
		}
		
		return null;
	}
}
