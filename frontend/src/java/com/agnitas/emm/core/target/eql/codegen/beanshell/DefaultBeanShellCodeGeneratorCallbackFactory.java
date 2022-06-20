/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.eql.codegen.beanshell;

import java.util.Objects;

import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;

public class DefaultBeanShellCodeGeneratorCallbackFactory implements BeanShellCodeGeneratorCallbackFactory {
	
	protected final EmmProfileFieldResolverFactory profileFieldResolverFactory;
	
	public DefaultBeanShellCodeGeneratorCallbackFactory(final EmmProfileFieldResolverFactory profileFieldResolverFactory) {
		this.profileFieldResolverFactory = Objects.requireNonNull(profileFieldResolverFactory);
	}

	@Override
	public BeanShellCodeGeneratorCallback newCodeGeneratorCallback(final int companyID) throws ProfileFieldResolveException {
		final EmmProfileFieldResolver resolver = this.profileFieldResolverFactory.newInstance(companyID);
		
		return new DefaultBeanShellCodeGeneratorCallback(resolver, resolver);
	}

}
