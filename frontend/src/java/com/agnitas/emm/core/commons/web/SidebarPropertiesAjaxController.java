/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.web;

import com.agnitas.web.perm.annotations.AlwaysAllowed;
import org.agnitas.service.WebStorage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AlwaysAllowed
@RestController
@RequestMapping("/sidebar/ajax")
public class SidebarPropertiesAjaxController {

    private final WebStorage webStorage;

    public SidebarPropertiesAjaxController(final WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    @RequestMapping("setIsWide.action")
    public void setIsWide(@RequestParam(name = "isWide") boolean isWide) {
        webStorage.access(WebStorage.IS_WIDE_SIDEBAR, entry -> entry.setValue(isWide));
    }
}
