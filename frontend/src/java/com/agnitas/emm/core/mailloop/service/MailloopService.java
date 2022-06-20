/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.service;

import com.agnitas.emm.core.mailloop.MailloopException;

/**
 * Service interface for mailloop.
 */
public interface MailloopService {

	/**
	 * Send auto-responder mail defined in mailloop.
	 * 
	 * @param mailloopID ID of mailloop
	 * @param companyID company ID of customer that triggered the auto-responder mail 
	 * @param customerID customer ID that triggered the auto-responder mail
	 * @param securityToken security token to verify validity of request
	 * 
	 * @throws MailloopException on errors during processing
	 */
	public void sendAutoresponderMail(final int mailloopID, final int companyID, final int customerID, final String securityToken) throws MailloopException;
}
