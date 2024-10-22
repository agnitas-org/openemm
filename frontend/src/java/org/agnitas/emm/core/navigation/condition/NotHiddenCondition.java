/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.navigation.condition;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

// TODO: EMMGUI-714 check and remove after remove of old design (if not used)
@Component
public class NotHiddenCondition implements NavItemCondition {

    @Override
    public String getId() {
        return "notHiddenCondition";
    }

    @Override
    public ConditionResult check(HttpServletRequest req, Map<String, String> params) {
        return new ConditionResult(!Boolean.TRUE.equals(req.getAttribute("hidden")));
    }
}
