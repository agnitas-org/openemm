/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class QueryBuilderNodeDeserializer extends JsonDeserializer<QueryBuilderBaseNode> {

    @Override
    public QueryBuilderBaseNode deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        if (!node.isArray() && !node.has("condition")) {
            return parser.getCodec().treeToValue(node, QueryBuilderRuleNode.class);
        }

        QueryBuilderGroupNode groupNode = new QueryBuilderGroupNode();
        groupNode.setCondition(node.isArray() ? QueryBuilderCondition.AND.queryBuilderName() : node.get("condition").asText());

        if (node.has("rules")) {
            List<QueryBuilderBaseNode> rules = new ArrayList<>();
            for (JsonNode ruleNode : node.get("rules")) {
                rules.add(parser.getCodec().treeToValue(ruleNode, QueryBuilderBaseNode.class));
            }
            groupNode.setRules(rules);
        }
        return groupNode;
    }

}
