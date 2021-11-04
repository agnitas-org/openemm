/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.landingpage.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.emm.landingpage.beans.RedirectSettings;
import com.agnitas.emm.landingpage.service.LandingpageService;

public final class LandingpageServlet extends HttpServlet {
	
	private static final transient Logger LOGGER = Logger.getLogger(LandingpageServlet.class);
	
	private LandingpageService landingpageService;

	/** Serial version UID. */
	private static final long serialVersionUID = -6994372676546100544L;

	@Override
	protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		redirectToLandingPage(req, resp);
	}

	@Override
	protected final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		redirectToLandingPage(req, resp);
	}
	
	private final void redirectToLandingPage(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final String uri = req.getRequestURL().toString();
		
		final RedirectSettings settings = getLandingpageService().getLandingPageRedirection(uri);
		
		if(settings.getHttpCode() == 0) {
			doHtmlMetaRedirect(settings, req, resp);
		} else {
			doHttpRedirect(settings, req,resp);
		}
	}
	
	private final void doHtmlMetaRedirect(final RedirectSettings settings, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); 
		final String html = "<html><head><meta http-equiv=\"refresh\" content=\"0; URL=%s\"></head></html>";
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Redirecting to landing page '%s' by HTML meta", settings.getRedirectUrl()));
		}
		
		resp.getWriter().println(String.format(html, settings.getRedirectUrl()));
	}
	
	private final void doHttpRedirect(final RedirectSettings settings, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Redirecting to landing page '%s' by HTTP %d", settings.getRedirectUrl(), settings.getHttpCode()));
		}
		
		resp.setStatus(settings.getHttpCode());
		resp.addHeader("Location", settings.getRedirectUrl());
	}

	private final LandingpageService getLandingpageService() {
		synchronized(this) {
			if(this.landingpageService == null) {
				this.landingpageService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("LandingpageService", LandingpageService.class);
			}
		}
		
		return this.landingpageService;
	}
	
}
