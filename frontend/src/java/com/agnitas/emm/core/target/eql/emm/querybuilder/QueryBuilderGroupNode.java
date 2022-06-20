/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

public final class QueryBuilderGroupNode extends QueryBuilderBaseNode {
	
	private String condition;
	private List<QueryBuilderBaseNode> rules;
	
	public QueryBuilderGroupNode() {
		this.rules = new ArrayList<>();
		
	}
	
	public QueryBuilderGroupNode(final String condition) {
		this();
		this.condition = condition;
	}
	
	public final void addRule(final QueryBuilderBaseNode node) {
		this.rules.add(node);
	}

	public final void setRules(final List<QueryBuilderBaseNode> rules) {
		this.rules = rules;
	}
	
	public final List<QueryBuilderBaseNode> getRules() {
		return this.rules;
	}
	
	public final String getCondition() {
		return this.condition;
	}
	
	public final void setCondition(final String condition) {
		this.condition = condition;
	}
	
	public final QueryBuilderRuleNode lastAddedRule() {
		if (!CollectionUtils.isEmpty(rules)) {
			for (int i = rules.size() - 1; i >= 0 ; i--) {
				QueryBuilderBaseNode node = rules.get(i);
				if (node.getClass() == QueryBuilderRuleNode.class) {
					return (QueryBuilderRuleNode) node;
				}
			}
		}
		return null;
	}
}
