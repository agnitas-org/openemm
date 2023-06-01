/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.web;

import java.util.concurrent.ThreadLocalRandom;

import com.agnitas.web.mvc.XssCheckAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.web.perm.annotations.Anonymous;

@Controller
@RequestMapping("/connectionSpeed")
public class ConnectionSpeedTestController implements XssCheckAware {

    public static final int SPEED_TEST_RESOURCE_SIZE = 10000; // bytes

    @Anonymous
    @RequestMapping("/test.action")
    @ResponseBody
    public byte[] test() {
    	byte[] r = new byte[SPEED_TEST_RESOURCE_SIZE];
        ThreadLocalRandom.current().nextBytes(r);
        return r;
    }
}
