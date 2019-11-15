/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailinglist.service;

import java.util.List;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;

public interface MailinglistService {

	int addMailinglist(MailinglistModel model) throws MailinglistException;

    void updateMailinglist(MailinglistModel model) throws MailinglistException;

	Mailinglist getMailinglist(MailinglistModel model) throws MailinglistException;

	boolean deleteMailinglist(MailinglistModel model) throws MailinglistException;

	/**
	 * Deprecated, because MailinglistModel is overkill here. This method just takes the company ID from the model.
	 * @see #listMailinglists(int)
	 */
	@Deprecated
	List<Mailinglist> getMailinglists(MailinglistModel model) throws MailinglistException;
	
	List<Mailinglist> listMailinglists(final int companyID) throws MailinglistException;
}
