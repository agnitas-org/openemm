/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

import com.agnitas.emm.core.target.eql.codegen.DataType;

/**
 * Converter for profile field name used in EQL ("shown name") to its datatype.
 */
public interface ProfileFieldTypeResolver {

	/**
	 * Takes the real name (not database name) of a profile field and returns
	 * its type.
	 * 
	 * @param profileFieldName name of profile field
	 * 
	 * @return type of profile field
	 * 
	 * @throws ProfileFieldResolveException on errors resolving the profile field name
	 */
	DataType resolveProfileFieldType(String profileFieldName) throws ProfileFieldResolveException;
	
}
