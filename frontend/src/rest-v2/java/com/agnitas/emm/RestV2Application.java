/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({
        "classpath:applicationContext-rest-v2.xml",
        "/WEB-INF/applicationContext-commons-basic.xml",
        "/WEB-INF/applicationContext-commons-extended.xml",
        "/WEB-INF/applicationContext-dao-basic.xml",
        "/WEB-INF/applicationContext-dao-extended.xml",
        "/WEB-INF/applicationContext-service-basic.xml",
        "/WEB-INF/applicationContext-service-extended.xml",
        "/WEB-INF/dataAccessContext.xml"
})
public class RestV2Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(RestV2Application.class, args);
    }

}
