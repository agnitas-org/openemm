/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.navigation.condition.mailing;

import com.agnitas.messages.I18nString;
import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.emm.core.navigation.condition.NavItemCondition;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MailingRecipientsTabAllowedCondition implements NavItemCondition {

    @Override
    public String getId() {
        return "mailingRecipientsTabAllowed";
    }

    @Override
    public ConditionResult check(HttpServletRequest req, Map<String, String> params) {
        boolean isActiveMailing = BooleanUtils.toBoolean(params.get("isActiveMailing"));
        boolean mailinglistDisabled = BooleanUtils.toBoolean(params.get("mailinglistDisabled"));

        if (isActiveMailing) {
            return new ConditionResult(!mailinglistDisabled);
        }

        return new ConditionResult(
                false,
                I18nString.getLocaleString("mailing.recipients.hint", AgnUtils.getLocale(req))
        );
    }

}
