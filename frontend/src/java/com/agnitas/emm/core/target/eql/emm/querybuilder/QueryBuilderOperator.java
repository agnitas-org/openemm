/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalInfixOperator;
import com.agnitas.emm.core.target.eql.ast.PrefixRelationalOperator;
import com.agnitas.emm.core.target.eql.codegen.DataType;

public enum QueryBuilderOperator {
	EQ("equal", 						RelationalInfixOperator.EQ,		true,	true,	true),
	NEQ("not_equal", 					RelationalInfixOperator.NEQ,		true,	true,	true),
	LT("less", 							RelationalInfixOperator.LT,		true,	true,	false),
	LEQ("less_or_equal",	 			RelationalInfixOperator.LEQ,		true,	true,	false),
	GT("greater", 						RelationalInfixOperator.GT,		true,	true,	false),
	GEQ("greater_or_equal",	 			RelationalInfixOperator.GEQ,		true,	true,	false),
	EMPTY("is_empty",	 				null,													true,	true,	true),
	NOT_EMPTY("is_not_empty",	 		null,													true,	true,	true),
	MOD("mod",	 	            		BinaryOperatorExpressionalEqlNode.InfixOperator.MOD, 	true,	false,	false),
	LIKE("like", 						PrefixRelationalOperator.LIKE,							false,	true,	false),
	NOT_LIKE("not_like", 				PrefixRelationalOperator.LIKE,							false,	true,	false),
	CONTAINS("contains", 				PrefixRelationalOperator.CONTAINS,						false,	true,	false),
	NOT_CONTAINS("not_contains",		PrefixRelationalOperator.CONTAINS,						false,	true,	false),
	STARTS_WITH("begins_with",			PrefixRelationalOperator.STARTS_WITH,					false,	true,	false),
	NOT_STARTS_WITH("not_begins_with",	PrefixRelationalOperator.STARTS_WITH,					false,	true,	false),
	BEFORE("before",					RelationalInfixOperator.LT,		false,	false,	true),
	AFTER("after",						RelationalInfixOperator.GT,		false,	false,	true);

	private final String queryBuilderName;
	private final Object eqlOperator;
	private final boolean supportsNumeric;
	private final boolean supportsText;
	private final boolean supportsDate;

	QueryBuilderOperator(final String queryBuilderName, final Object eqlOperator, final boolean supportsNumeric, final boolean supportsText, final boolean supportsDate) {
		this.queryBuilderName = queryBuilderName;
		this.eqlOperator = eqlOperator;
		this.supportsNumeric = supportsNumeric;
		this.supportsText = supportsText;
		this.supportsDate = supportsDate;
	}

	public final boolean isNumericOperator() {
		return this.supportsNumeric;
	}

	public final boolean isTextOperator() {
		return this.supportsText;
	}

	public final boolean isDateOperator() {
		return this.supportsDate;
	}

	public final String queryBuilderName() {
		return this.queryBuilderName;
	}
	
	/*
	public final Operator eqlOperator() {
		return this.eqlOperator;
	}
	*/

	public static final QueryBuilderOperator findByQueryBuilderName(final String name) {
		for(QueryBuilderOperator op : values()) {
			if(op.queryBuilderName.equals(name)) {
				return op;
			}
		}

		return null;
	}

	public static final QueryBuilderOperator findByEqlOperator(final RelationalInfixOperator eqlOperator) {
		for(QueryBuilderOperator op : values()) {
			if(op.eqlOperator == eqlOperator) {
				return op;
			}
		}

		return null;
	}
	
	public static final QueryBuilderOperator findByEqlOperator(final RelationalInfixOperator eqlOperator, DataType dataType) {
		for(QueryBuilderOperator op : values()) {
			if(op.eqlOperator == eqlOperator) {
				boolean support = false;
				switch (dataType) {
					case NUMERIC:
						support = op.isNumericOperator();
						break;
					case DATE:
						support = op.isDateOperator();
						break;
					case TEXT:
						support = op.isTextOperator();
						break;
					default:
						//continue looking for supported operator
				}
				if (support) {
					return op;
				}
			}
		}

		return null;
	}
}
