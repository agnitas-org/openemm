/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.eql.emm.legacy;

import java.util.Objects;

import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmMailingTypeResolverFactory;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;

public final class OpenEmmEqlToTargetRepresentationConverterFactory implements EqlToTargetRepresentationConverterFactory {

	private final EmmProfileFieldResolverFactory profileFieldResolverFactory;
	private final EmmMailingTypeResolverFactory mailingTypeResolverFactory;
	private final SqlDialect sqlDialect;
	
	public OpenEmmEqlToTargetRepresentationConverterFactory(final EmmProfileFieldResolverFactory profileFieldResolverFactory, final EmmMailingTypeResolverFactory mailingTypeResolverFactory, final SqlDialect sqlDialect) {
		this.profileFieldResolverFactory = Objects.requireNonNull(profileFieldResolverFactory);
		this.mailingTypeResolverFactory = Objects.requireNonNull(mailingTypeResolverFactory);
		this.sqlDialect = Objects.requireNonNull(sqlDialect);
	}
	
	@Override
	public final EqlToTargetRepresentationConverter newConverter(final int companyId) throws ProfileFieldResolveException {
		final EmmProfileFieldResolver profileFieldResolver = this.profileFieldResolverFactory.newInstance(companyId);
		final MailingTypeResolver mailingTypeResolver = this.mailingTypeResolverFactory.newResolver();

		
		return new EqlToTargetRepresentationConverter(companyId, profileFieldResolver, profileFieldResolver, mailingTypeResolver, sqlDialect);
	}

}
