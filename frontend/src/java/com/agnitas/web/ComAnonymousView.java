/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.PubID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComAnonymousView extends HttpServlet {
	private static final long serialVersionUID = -8924026377915570739L;
	
	private static final transient Logger logger = LogManager.getLogger(ComAnonymousView.class);

	private PreviewFactory previewFactory;

	public static final String SWYN_CLICK_INSERT =
		"INSERT INTO swyn_click_tbl (network_id, mailing_id, customer_id, ip_address, timestamp, selector) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String uid = req.getParameter("uid");
		PubID pubId = new PubID();
		
		if (pubId.parseID(uid)) {
			Preview preview = getPreviewFactory().createPreview();
			Page content = preview.makePreview(pubId.getMailingID (), pubId.getCustomerID (), pubId.getParm (), null, true, false, false, false, true);
			String html = content.getHTML();
			
			res.setContentType( "text/html; charset=utf-8");
			res.setCharacterEncoding( "UTF-8");
			res.getWriter().println( html);
			
			logClick( pubId, req.getRemoteAddr());
		} else {
			res.setContentType( "text/html");
			res.getOutputStream().println("Anzeige nicht m&ouml;glich");
			logger.warn("invalid pub-id: " + uid);
		}
	}

	@DaoUpdateReturnValueCheck
	private void logClick(PubID pubId, String ipAddress) {
		ApplicationContext con = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		DataSource dataSource = (DataSource) con.getBean("dataSource");
		JdbcTemplate template = new JdbcTemplate(dataSource);

		try {
			template.update(SWYN_CLICK_INSERT, new Object[]{ pubId.getSource(), pubId.getMailingID(), pubId.getCustomerID(), ipAddress, pubId.getParm()} );
		} catch( Exception e) {
			logger.error("Error inserting into swyn_click_log: source=" + pubId.getSource() + ", mailingID=" + pubId.getMailingID() + ", customerID=" + pubId.getCustomerID() + ", ipAddress=" + ipAddress, e);
		}
	}

    private PreviewFactory getPreviewFactory() {
		if (previewFactory == null) {
			previewFactory = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("PreviewFactory", PreviewFactory.class);
		}
		return previewFactory;
	}
}
