/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;


public interface SendActionbasedMailingService {
	/**
	 * Send action-based mailing.
	 *
	 * @param companyId an identifier of a company that owns the referenced mailing.
	 * @param mailingId an identifier of the mailing to send.
	 * @param customerId ID of recipient.
	 * @param delayMinutes delay to send in minutes.
	 * @param options options for mailgun
	 *
	 * @throws SendActionbasedMailingException on errors sending action-based mailing.
	 */
	void sendActionbasedMailing(final int companyId, final int mailingId, final int customerId, final int delayMinutes, final MailgunOptions options) throws SendActionbasedMailingException;
}
