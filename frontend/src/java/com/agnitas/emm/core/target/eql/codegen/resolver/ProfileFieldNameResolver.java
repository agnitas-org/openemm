/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

/**
 * Converter for profile field name used in EQL ("shown name") to column name in database.
 */
public interface ProfileFieldNameResolver {

	/**
	 * Converts profile field name used in EQL ("shown name") to column name in database.
	 * 
	 * @param name name of profile field name in EQL
	 * 
	 * @return column name of profile field
	 * 
	 * @throws ProfileFieldResolveException on errors resolving profile field name
	 */
	String resolveProfileFieldName(String name) throws ProfileFieldResolveException;
	
	/**
	 * Converts a legacy profile field name to an EQL shortname.
	 *
	 * @param dbName name of the profile field
	 *
	 * @return EQL-styled profile field name
	 *
	 * @throws ProfileFieldResolveException on errors during conversion
	 */
	String resolveProfileFieldColumnName(String dbName) throws ProfileFieldResolveException;

}
