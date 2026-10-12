/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.navigation.condition.mailing;

import java.util.Map;

import com.agnitas.emm.core.navigation.condition.NavItemCondition;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
public class IsStandaloneSmsMailingCondition implements NavItemCondition {

    @Override
    public String getId() {
        return "isStandaloneSmsMailing";
    }

    @Override
    public ConditionResult check(HttpServletRequest req, Map<String, String> params) {
        return new ConditionResult(BooleanUtils.toBoolean(params.get("isStandaloneSmsMailing")));
    }

}
