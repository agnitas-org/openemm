/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.navigation.condition;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import org.springframework.stereotype.Component;

@Component
public class ResponseInboxCondition implements NavItemCondition {

    private final ConfigService configService;

    public ResponseInboxCondition(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String getId() {
        return "responseInboxCondition";
    }

    @Override
    public ConditionResult check(HttpServletRequest req, Map<String, String> params) {
        return new ConditionResult(configService.getBooleanValue(ConfigValue.EnableResponseInbox, AgnUtils.getCompanyID(req)));
    }
}
