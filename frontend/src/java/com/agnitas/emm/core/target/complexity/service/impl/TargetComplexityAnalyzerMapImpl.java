/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.target.complexity.service.TargetComplexityAnalyzer;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityAnalyzerMap;
import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("TargetComplexityAnalyzerMap")
public class TargetComplexityAnalyzerMapImpl implements TargetComplexityAnalyzerMap {
    private Map<Class<?>, TargetComplexityAnalyzer<?>> map = new HashMap<>();

    public TargetComplexityAnalyzerMapImpl(@Autowired List<TargetComplexityAnalyzer<?>> analyzers) {
        for (TargetComplexityAnalyzer<?> analyzer : analyzers) {
            map.put(analyzer.getNodeType(), analyzer);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractEqlNode> TargetComplexityAnalyzer<T> get(Class<T> type) {
        return (TargetComplexityAnalyzer<T>) map.get(type);
    }
}
