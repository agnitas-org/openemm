/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.web;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.mailloop.MailloopException;
import com.agnitas.emm.core.mailloop.service.MailloopService;
import com.agnitas.web.perm.annotations.Anonymous;

/**
 * Controller to trigger sending of mailloop auto-responder mails.
 */
@Controller
public class SendAutoresponderController {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(SendAutoresponderController.class);

	/** Service dealing with mailloops. */
	private final MailloopService mailloopService;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param mailloopService service dealing with mailloops
	 * 
	 * @throws NullPointerException if given mailloopService is <code>null</code>
	 */
	public SendAutoresponderController(final MailloopService mailloopService) {
		this.mailloopService = Objects.requireNonNull(mailloopService, "mailloopService");
	}

	/**
	 * Triggers sending mailloop auto-responder mail.
	 * 
	 * @param autoresponderForm form bean
	 * 
	 * @return HTTP response entity with status code and reason phrase
	 */
	@Anonymous	// Invoked by backend without authenticated user. Secured by security token.
	@RequestMapping("/sendMailloopAutoresponder.action")
	public final ResponseEntity<String> sendAutoresponse(final SendAutoresponderForm autoresponderForm) {
		final int mailloopID = autoresponderForm.getMailloopID();
		final int companyID = autoresponderForm.getCompanyID();
		final int customerID = autoresponderForm.getCustomerID();
		final String securityToken = autoresponderForm.getSecurityToken();

		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(
					String.format(
							"Requested sending auto-responder mail - mailloop %d, company %d, customer %d", 
							mailloopID, 
							companyID, 
							customerID));
		}

		try {
			this.mailloopService.sendAutoresponderMail(mailloopID, companyID, customerID, securityToken);
			
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(
						String.format(
								"Sent auto-responder mail - mailloop %d, company %d, customer %d", 
								mailloopID, 
								companyID, 
								customerID));
			}
			
			return ResponseEntity.ok().build();
		} catch(final MailloopException e) {
			LOGGER.info(
					String.format(
							"Error sending auto-responder mail - mailloop %d, company %d, customer %d", 
							mailloopID, 
							companyID, 
							customerID), 
					e);

			return ResponseEntity.internalServerError().build();
		}
		
	}
	
}
