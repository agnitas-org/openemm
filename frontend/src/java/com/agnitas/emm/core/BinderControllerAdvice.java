/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice // global applicable
@Order(Ordered.LOWEST_PRECEDENCE) // lowest prio for ordering

/**
 * Fix for "Spring4Shell" (CVE-2022-22965)
 * See EMM-9293
 */
public class BinderControllerAdvice {
    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        String[] denyList = new String[] {"class.*","Class.*","*.class.*","*.Class.*"};
        dataBinder.setDisallowedFields(denyList);
    }
}
