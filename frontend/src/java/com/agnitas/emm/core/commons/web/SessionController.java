/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.web;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.agnitas.util.AgnUtils;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessionController {

	@Anonymous
    @RequestMapping("/info.action")
    public ResponseEntity<Map<String, Object>> info(HttpServletRequest request) {
        Admin sessionAdmin = AgnUtils.getAdmin(request);
        if (sessionAdmin == null) {
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(sessionToJsonObject(request).toMap());
    }

    private JSONObject sessionToJsonObject(HttpServletRequest request) {
        long creationTime = 0;
        long lastAccessedTime = 0;
        int maxInactiveInterval = 0;

        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            creationTime = httpSession.getCreationTime();
            lastAccessedTime = httpSession.getLastAccessedTime();
            maxInactiveInterval = httpSession.getMaxInactiveInterval();
        }

        JSONObject sessionInfo = new JSONObject();
        sessionInfo.accumulate("creationTime", creationTime);
        sessionInfo.accumulate("lastAccessedTime", lastAccessedTime);
        sessionInfo.accumulate("maxInactiveInterval", maxInactiveInterval);
        
        return sessionInfo;
    }
}
