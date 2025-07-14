/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public class OpenedMailingParser extends GenericEqlNodeParser<OpenedMailingRelationalEqlNode> {

    private static final String OPENED_MAILING = "opened mailing";

    private static final String EQUAL = "equal";

    @Override
    protected QueryBuilderGroupNode parse(OpenedMailingRelationalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException {
    	if (node.hasDeviceQuery()) {
    		throw new EqlToQueryBuilderConversionException("Device query not supported by QueryBuilder");
    	}
    	
        groupNode.addRule(new QueryBuilderRuleNode(OPENED_MAILING, EQUAL, node.getMailingId()));
        return groupNode;
    }

}
