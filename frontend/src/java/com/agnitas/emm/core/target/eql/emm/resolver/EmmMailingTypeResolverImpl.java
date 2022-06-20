/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingResolverException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;

public class EmmMailingTypeResolverImpl implements MailingTypeResolver {

	private final ComMailingDao mailingDao;
	
	public EmmMailingTypeResolverImpl(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Override
	public MailingType resolveMailingType(int mailingId, int companyId) throws MailingResolverException {
		try {
			MailingType mailingType = mailingDao.getMailingType(mailingId);
			if (mailingType == null) {
				throw new MailingResolverException("Invalid MailingType for mailing ID: " + mailingId + " (company " + companyId + ")");
			} else {
				return mailingType;
			}
		} catch (Exception e) {
			throw new MailingResolverException("Invalid MailingType for mailing ID: " + mailingId + " (company " + companyId + ")");
		}
	}
}
