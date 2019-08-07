/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.codegen.sql.DefaultCommonSqlCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;
import com.agnitas.emm.core.target.eql.codegen.validate.LinkIdValidator;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidator;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmMailingTypeResolverFactory;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;

public final class DefaultCommonSqlCodeGeneratorCallbackFactory implements SqlCodeGeneratorCallbackFactory {

	private final EmmProfileFieldResolverFactory profileFieldResolverFactory;
	private final MailingIdValidator mailingIdValidator;
	private final EmmMailingTypeResolverFactory mailingTypeResolverFactory;
	private final LinkIdValidator linkIdValidator;
	private final SqlDialect sqlDialect;
	
	public DefaultCommonSqlCodeGeneratorCallbackFactory(final EmmProfileFieldResolverFactory profileFieldResolverFactory,
			final MailingIdValidator mailingIdValidator, 
			final EmmMailingTypeResolverFactory mailingTypeResolverFactory,
			final LinkIdValidator linkIdValidator, 
			final SqlDialect sqlDialect) {
		this.profileFieldResolverFactory = profileFieldResolverFactory;
		this.mailingIdValidator = mailingIdValidator;
		this.mailingTypeResolverFactory = mailingTypeResolverFactory;
		this.linkIdValidator = linkIdValidator;
		this.sqlDialect = sqlDialect;
	}
	
	@Override
	public final SqlCodeGeneratorCallback newCodeGeneratorCallback(final int companyId) throws ProfileFieldResolveException, ReferenceTableResolveException {
		final EmmProfileFieldResolver profileFieldResolver = this.profileFieldResolverFactory.newInstance(companyId);
		final MailingTypeResolver mailingTypeResolver = this.mailingTypeResolverFactory.newResolver();
		
		return new DefaultCommonSqlCodeGeneratorCallback(
				companyId, 
				profileFieldResolver,
				profileFieldResolver,
				mailingIdValidator, 
				mailingTypeResolver, 
				linkIdValidator, 
				sqlDialect);
	}

}
