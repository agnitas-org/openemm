/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.Map;
import java.util.concurrent.Callable;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.MailingsListProperties;

public class MailingsQueryWorker implements Callable<PaginatedListImpl<Map<String, Object>>> {
	private MailingDao mDao;
	private int companyID;
	private MailingsListProperties props;

	public MailingsQueryWorker(MailingDao dao, @VelocityCheck int companyID, MailingsListProperties props) {
		this.mDao = dao;
		this.companyID = companyID;
		this.props = props;
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> call() throws Exception {
		return mDao.getMailingList(companyID, props);
	}
}
