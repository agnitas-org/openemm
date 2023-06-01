/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.core.mailing.bean.MailingsListResult;
import com.agnitas.messages.Message;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.util.FulltextSearchInvalidQueryException;

public class MailingsQueryWorker implements Callable<MailingsListResult> {

	private MailingDao mailingDao;
	private Admin admin;
	private MailingsListProperties props;

	public MailingsQueryWorker(MailingDao mailingDao, Admin admin, MailingsListProperties props) {
		this.mailingDao = mailingDao;
		this.admin = admin;
		this.props = props;
	}

	@Override
	public MailingsListResult call() throws Exception {
		Message errorMessage = null;
		PaginatedListImpl<Map<String, Object>> mailingList;

		try {
			mailingList =  mailingDao.getMailingList(admin, props);
		} catch (FulltextSearchInvalidQueryException e) {
			errorMessage = e.getUiMessage();
			mailingList = new PaginatedListImpl<>(new ArrayList<>(), 0, props.getRownums(), props.getPage(), "senddate", true);
		}

		return new MailingsListResult(mailingList, errorMessage);
	}
}
