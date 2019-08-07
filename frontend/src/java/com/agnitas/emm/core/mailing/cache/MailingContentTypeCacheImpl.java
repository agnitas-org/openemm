/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.cache;

import java.util.Objects;

import org.agnitas.emm.core.commons.daocache.AbstractCompanyBasedDaoCache;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.dao.ComMailingDao;

public final class MailingContentTypeCacheImpl extends AbstractCompanyBasedDaoCache<MailingContentType> implements MailingContentTypeCache {

	private ComMailingDao mailingDao;
	
	@Required
	public final void setMailingDao(final ComMailingDao dao) {
		this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
	}

	@Override
	protected final MailingContentType getItemFromDao(final int mailingId, final int companyId) {
		final ComMailing mailing = (ComMailing) this.mailingDao.getMailing(mailingId, companyId);
		
		if(mailing == null) {
			return null;
		} else {
			return mailing.getMailingContentType();
		}
	}

}
