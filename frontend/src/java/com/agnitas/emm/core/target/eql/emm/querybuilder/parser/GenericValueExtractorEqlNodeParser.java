/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderBaseNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public abstract class GenericValueExtractorEqlNodeParser<T> extends GenericEqlNodeParser<T> {

    @Override
    protected QueryBuilderGroupNode parse(T node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException {
        List<QueryBuilderBaseNode> rules = groupNode.getRules();
        if (!CollectionUtils.isEmpty(rules)) {
            QueryBuilderBaseNode baseNode = rules.get(rules.size() - 1);
            if (baseNode instanceof QueryBuilderRuleNode) {
                QueryBuilderRuleNode ruleNode = (QueryBuilderRuleNode) baseNode;
                return parse(node, ruleNode, groupNode, profileFields);
            }
        }
        throw new EqlToQueryBuilderConversionException("Unable to obtain rule id for node " + node);
    }

    protected abstract QueryBuilderGroupNode parse(T node, QueryBuilderRuleNode ruleNode, QueryBuilderGroupNode groupNode, Set<String> profileFields);
}
