/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.mailing.web;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.dao.MailingDao;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.agnitas.beans.Admin;

public class MailingSendAjaxAction extends DispatchAction {

	private MailingDao mailingDao;
	
	public ActionForward transmissionRunning(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {

		Admin admin = AgnUtils.getAdmin(request); 
		if (admin == null ) {
			return null;
		}
		
		String message = "TRUE";
		String mailingIDStr = request.getParameter("mailingID");
		if( StringUtils.isNotEmpty(mailingIDStr) && StringUtils.isNumeric(mailingIDStr)) {
			boolean transmissionRunning = mailingDao.isTransmissionRunning(Integer.parseInt(mailingIDStr));
			message = transmissionRunning ? "TRUE": "FALSE";
		}
		
		response.setContentType("text/plain"); 
		response.setHeader("Cache-Control", "no-cache");
		try (PrintWriter pw = response.getWriter()) {
			pw.write(message);
		}
		
		return null;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
}
