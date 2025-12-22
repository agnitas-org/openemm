/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.navigation.condition;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface NavItemCondition {

    String getId();

    ConditionResult check(HttpServletRequest req, Map<String, String> params);


    class ConditionResult {
        private boolean satisfied;
        private String message;

        public ConditionResult(boolean satisfied) {
            this.satisfied = satisfied;
        }

        public ConditionResult(boolean satisfied, String message) {
            this.satisfied = satisfied;
            this.message = message;
        }

        public boolean isSatisfied() {
            return satisfied;
        }

        public String getMessage() {
            return message;
        }
    }

}
