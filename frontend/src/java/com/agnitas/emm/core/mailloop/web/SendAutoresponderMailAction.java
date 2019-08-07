/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.mailloop.MailloopException;
import com.agnitas.emm.core.mailloop.service.MailloopService;

/**
 * Action called when auto-responder mail is to be sent.
 */
public class SendAutoresponderMailAction extends Action {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(SendAutoresponderMailAction.class);
	
	/** Service for mailloop feature. */
	private MailloopService mailloopService;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SendAutoresponderMailForm autoresponderForm = (SendAutoresponderMailForm) form;
		
		final int mailloopID = autoresponderForm.getMailloopID();
		final int companyID = autoresponderForm.getCompanyID();
		final int customerID = autoresponderForm.getCustomerID();
		final String securityToken = autoresponderForm.getSecurityToken();

		if(logger.isInfoEnabled()) {
			logger.info(String.format("Requested sending auto-responder mail - mailloop %d, company %d, customer %d", mailloopID, companyID, customerID));
		}

		try {
			this.mailloopService.sendAutoresponderMail(mailloopID, companyID, customerID, securityToken);
			
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Sent auto-responder mail - mailloop %d, company %d, customer %d", mailloopID, companyID, customerID));
			}
			
			response.setStatus(200);
		} catch(MailloopException e) {
			logger.info(String.format("Error sending auto-responder mail - mailloop %d, company %d, customer %d", mailloopID, companyID, customerID), e);
			
			response.sendError(500, "Unable to send auto-responder");
		}
		
		return null;
	}

	/**
	 * Set service for mailloop feature.
	 * 
	 * @param service service for mailloop feature
	 */
	@Required
	public void setMailloopService(final MailloopService service) {
		this.mailloopService = service;
	}
	
}
