/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;

public abstract class GenericEqlNodeParser<T> implements EqlNodeParser<T> {

    protected EqlToQueryBuilderParserConfiguration configuration;

    @Override
    public QueryBuilderGroupNode parse(AbstractEqlNode node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException {
        T eqlNode = getEqlNode(node);
        return parse(eqlNode, groupNode, unknownProfileFields);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getEqlNode(AbstractEqlNode node) throws EqlToQueryBuilderConversionException {
        try {
            return (T) node;
        } catch (ClassCastException e) {
            throw new EqlToQueryBuilderConversionException("Cannot cast class, probably a configuration error.", e);
        }
    }

    protected abstract QueryBuilderGroupNode parse(T node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException;

    public void setConfiguration(EqlToQueryBuilderParserConfiguration configuration) {
        this.configuration = configuration;
    }
}
