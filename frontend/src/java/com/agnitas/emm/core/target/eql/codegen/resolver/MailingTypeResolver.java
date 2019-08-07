/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

/**
 * Resolver for mailing types.
 */
public interface MailingTypeResolver {
	
	/**
	 * Returns the mailing type for given mailing ID.
	 * 
	 * @param mailingId ID of mailing
	 * @param companyId company ID of mailing
	 * 
	 * @return type of mailing
	 * 
	 * @throws MailingResolverException on errors during resolving mailing type
	 */
	public MailingType resolveMailingType(int mailingId, int companyId) throws MailingResolverException;
	
}
