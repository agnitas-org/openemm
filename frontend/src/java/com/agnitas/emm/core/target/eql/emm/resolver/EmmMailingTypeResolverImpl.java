/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingResolverException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;

public class EmmMailingTypeResolverImpl implements MailingTypeResolver {

	private final ComMailingDao mailingDao;
	
	public EmmMailingTypeResolverImpl(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Override
	public MailingType resolveMailingType(int mailingId, int companyId) throws MailingResolverException {
		int mailingType = mailingDao.getMailingType(mailingId);

		if(mailingType == -1) {
			throw new MailingResolverException("Unknown mailing ID: " + mailingId + " (company " + companyId + ")");
		}

		if(mailingType == MailingTypes.INTERVAL.getCode())
			return MailingType.INTERVAL;
		else
			return MailingType.NORMAL;
	}
}
