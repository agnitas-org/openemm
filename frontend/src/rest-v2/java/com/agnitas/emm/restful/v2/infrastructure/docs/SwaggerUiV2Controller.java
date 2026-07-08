/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.docs;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class SwaggerUiV2Controller {

    @GetMapping({"", "/"})
    public String ui(HttpServletResponse response) {
        String csp = """
                default-src 'self'; \
                script-src 'self' 'unsafe-inline'; \
                style-src 'self' 'unsafe-inline'; \
                img-src 'self' data:;""";
        response.setHeader("Content-Security-Policy", csp);
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        return "forward:/swagger-ui/index.html";
    }
}
