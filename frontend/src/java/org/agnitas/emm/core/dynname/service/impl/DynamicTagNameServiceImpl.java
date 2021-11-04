/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.dynname.service.impl;

import java.util.List;

import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.dynname.service.DynamicTagNameService;
import org.agnitas.emm.core.dynname.service.NameModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.validator.annotation.Validate;

import com.agnitas.beans.DynamicTag;
import com.agnitas.dao.DynamicTagDao;

import jakarta.annotation.Resource;

public class DynamicTagNameServiceImpl implements DynamicTagNameService {

	@Resource(name="DynamicTagDao")
	private DynamicTagDao dynamicTagDao;
	@Resource(name="MailingDao")
	private MailingDao mailingDao;

	@Override
	@Validate
	public List<DynamicTag> getNameList(NameModel model) {
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		return dynamicTagDao.getNameList(model.getCompanyId(), model.getMailingId());
	}

}
