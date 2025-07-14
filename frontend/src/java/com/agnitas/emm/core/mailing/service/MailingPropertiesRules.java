/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;

public interface MailingPropertiesRules {

	/**
	 * Returns <code>true</code> if the mailing content (frame content, content blocks, target groups, ...)
	 * is editable by given admin.
	 * 
	 * @param mailingID mailing ID
	 * @param admin admin
	 * 
	 * @return <code>true</code> if mailing content is editable
	 */
	boolean isMailingContentEditable(final int mailingID, final Admin admin);

	/**
	 * Returns <code>true</code> if the mailing content (frame content, content blocks, target groups, ...)
	 * is editable by given admin.
	 * 
	 * @param mailing mailing
	 * @param admin admin
	 * 
	 * @return <code>true</code> if mailing content is editable
	 */
	boolean isMailingContentEditable(final Mailing mailing, final Admin admin);

	/**
	 * Returns <code>true</code> if mailing has been world-sent or mailing is active.
	 * 
	 * @param mailing mailing
	 * 
	 * @return <code>true</code> if mailing has been world-sent or mailing is active
	 */
	boolean mailingIsWorldSentOrActive(final Mailing mailing);
	
	boolean mailingIsWorldSentOrActive(final int mailingID, final int companyID);

}
