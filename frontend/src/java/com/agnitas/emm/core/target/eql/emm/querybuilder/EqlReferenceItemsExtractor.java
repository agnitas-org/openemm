/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.referencecollector.ReferencedItemsCollection;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EqlReferenceItemsExtractor {

    private final EqlParser parser;
    private final EqlParserConfiguration parserConfiguration;

    @Autowired
    public EqlReferenceItemsExtractor(EqlParser parser) {
        this.parser = parser;
        this.parserConfiguration = new EqlParserConfiguration().setCreateBooleanAnnotationNodes(true);
    }

    public ReferencedItemsCollection collectReferences(String eql) throws EqlParserException {
        SimpleReferenceCollector referenceCollector = new SimpleReferenceCollector();
        Optional<AbstractBooleanEqlNode> child = parser.parseEql(eql, parserConfiguration).getChild();

        if (child.isPresent()) {
            AbstractBooleanEqlNode node = child.get();
            node.collectReferencedItems(referenceCollector);
        }

        return referenceCollector;
    }
}
