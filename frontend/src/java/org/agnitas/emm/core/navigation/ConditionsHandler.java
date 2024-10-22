/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.emm.core.navigation.condition.NavItemCondition;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConditionsHandler {

    private static final Logger LOGGER = LogManager.getLogger(ConditionsHandler.class);

    private final Map<String, NavItemCondition> conditionsMap = new HashMap<>();

    public ConditionsHandler(@Autowired(required = false) final List<NavItemCondition> conditions) {
        fillConditionsMap(conditions);
    }

    public NavItemCondition.ConditionResult checkCondition(final String conditionId, HttpServletRequest req) {
        final NavItemCondition condition = conditionsMap.get(conditionId);
        if (condition == null) {
            LOGGER.warn("Invalid condition id sent: {}.", conditionId);
            return new NavItemCondition.ConditionResult(false);
        }

        Map<String, String> params = Collections.emptyMap();

        Object paramsAttr = req.getAttribute("agnNavConditionsParams");
        if (paramsAttr instanceof Map) {
            params = ((Map<?, ?>) paramsAttr).entrySet()
                    .stream()
                    .map(e -> new Tuple<>(e.getKey().toString(), e.getValue().toString()))
                    .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        }

        return condition.check(req, params);
    }

    private void fillConditionsMap(final Collection<NavItemCondition> conditions) {
        for (NavItemCondition condition : CollectionUtils.emptyIfNull(conditions)) {
            if (conditionsMap.containsKey(condition.getId())) {
                throw new IllegalArgumentException("Cannot be several conditions with one name!");
            }
            conditionsMap.put(condition.getId(), condition);
        }
    }
}
