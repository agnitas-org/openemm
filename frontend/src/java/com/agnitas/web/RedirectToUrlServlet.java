/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RedirectToUrlServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(RedirectToUrlServlet.class);

	/** Serial version UID: */
	private static final long serialVersionUID = -595094416663851734L;

	private String destinationUrl = null;
	private int httpCode = 301;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		try {
			destinationUrl = config.getInitParameter("destinationUrl");
		} catch (NumberFormatException e) {
			logger.error("Invalid destinationUrl");
		}

		try {
			httpCode = Integer.parseInt(config.getInitParameter("httpCode"));
		} catch (NumberFormatException e) {
			logger.error("Invalid httpCode");
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		getInitParameter("destinationUrl");
		response.setStatus(httpCode);
		response.setContentType("text/html");
		response.setHeader("Location", destinationUrl);
	}
}
