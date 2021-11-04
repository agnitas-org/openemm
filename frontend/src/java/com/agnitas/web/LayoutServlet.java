/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComEmmLayoutBaseDao;
import com.agnitas.dao.LayoutDao;

/**
 * Example Link: "https://<emm-domain>/layout/0/favicon.ico"
 */
public class LayoutServlet extends HttpServlet {
	private static final long serialVersionUID = -6593446000401010425L;

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LayoutServlet.class);

	protected LayoutDao layoutDao;
	
	protected ComEmmLayoutBaseDao emmLayoutBaseDao;
	
	private Map<String, Map<Integer, byte[]>> layoutData = null;
	
	private Map<String, Integer> mappendDomains = null;

	public void setLayoutDao(LayoutDao layoutDao) {
		this.layoutDao = layoutDao;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (layoutData == null) {
            	layoutData = getLayoutDao().getLayoutData();
            }
            
            if (mappendDomains == null) {
            	mappendDomains = getEmmLayoutBaseDao().getMappedDomains();
            }
            
            String companyIdString;
            String itemName;
			String[] uriParts = request.getRequestURI().split("/");
			if (uriParts.length >= 2 && AgnUtils.isNumber(uriParts[uriParts.length - 2]) && StringUtils.isNotBlank(uriParts[uriParts.length - 1])) {
				companyIdString = uriParts[uriParts.length - 2];
				itemName = uriParts[uriParts.length - 1];
				if (itemName.contains(";")) {
					// remove sessionid
					itemName = itemName.substring(0, itemName.indexOf(";"));
				}
			} else if (uriParts.length >= 1 && StringUtils.isNotBlank(uriParts[uriParts.length - 1]) && StringUtils.startsWithIgnoreCase(uriParts[uriParts.length - 1], "favicon.ico")) {
				companyIdString = "0";
				itemName = "favicon.ico";
			} else {
				throw new Exception("Invalid request data");
			}
			
			int companyID = 0;
			if (StringUtils.isNotBlank(companyIdString)) {
				try {
					companyID = Integer.parseInt(companyIdString);
				} catch (NumberFormatException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			}
			
			byte[] sendData = null;
			if (layoutData.containsKey(itemName)) {
				Map<Integer, byte[]> itemData = layoutData.get(itemName);
				if (companyID != 0 && itemData.containsKey(companyID)) {
					sendData = itemData.get(companyID);
				} else if (mappendDomains.containsKey(request.getServerName()) && itemData.containsKey(mappendDomains.get(request.getServerName()))) {
					sendData = itemData.get(mappendDomains.get(request.getServerName()));
				} else if (itemData.containsKey(0)) {
					sendData = itemData.get(0);
				}
			}
			
			if (sendData != null) {
				if (itemName.toLowerCase().endsWith(".ico")) {
					response.setContentType("image/x-icon");
				} else if (itemName.toLowerCase().endsWith(".png")) {
					response.setContentType("image/png");
				} else if (itemName.toLowerCase().endsWith(".jpg")) {
					response.setContentType("image/jpeg");
				} else if (itemName.toLowerCase().endsWith(".svg")) {
					response.setContentType("image/svg+xml");
				}
				
				try (ServletOutputStream out = response.getOutputStream()) {
					out.write(sendData);
					out.flush();
				}
			} else {
				throw new Exception("No layoutdata for " + companyIdString + "/" + itemName);
			}
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
			throw new ServletException("Error occurred when loading image", e);
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug("ShowImageServlet execute end: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_MS).format(new Date()));
			}
		}
	}

    private LayoutDao getLayoutDao() {
		if (layoutDao == null) {
			layoutDao = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("LayoutDao", LayoutDao.class);
		}
		return layoutDao;
	}

    private ComEmmLayoutBaseDao getEmmLayoutBaseDao() {
		if (emmLayoutBaseDao == null) {
			emmLayoutBaseDao = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("EmmLayoutBaseDao", ComEmmLayoutBaseDao.class);
		}
		return emmLayoutBaseDao;
	}
}
