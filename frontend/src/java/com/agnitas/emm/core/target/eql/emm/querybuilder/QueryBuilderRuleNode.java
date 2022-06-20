/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

public final class QueryBuilderRuleNode extends QueryBuilderBaseNode {

	private String id;
	private String operator;
	private Object value;
	private String type;
	private boolean negated;
	private boolean includeEmpty;
	
	public QueryBuilderRuleNode() {
		// Ctor required by JSON deserialization
	}
	
	public QueryBuilderRuleNode(final String id, final String operator, final Object value) {
		this.id = id;
		this.operator = operator;
		this.value = value;
	}

	public QueryBuilderRuleNode(final String id, final String operator, final Object value, String type) {
		this.id = id;
		this.operator = operator;
		this.value = value;
		this.type = type;
	}
	
	public final String getId() {
		return this.id;
	}
	
	public final void setId(final String id) {
		this.id = id;
	}
	
	public final String getOperator() {
		return this.operator;
	}
	
	public final void setOperator(final String operator) {
		this.operator = operator;
	}
	
	public final Object getValue() {
		return this.value;
	}
	
	public final void setValue(final Object value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public boolean isIncludeEmpty() {
		return includeEmpty;
	}

	public void setIncludeEmpty(boolean includeEmpty) {
		this.includeEmpty = includeEmpty;
	}
}
