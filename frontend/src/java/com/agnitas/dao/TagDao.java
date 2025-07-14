/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.TagDefinition;

/**
 * Dao for tag_tbl which contains the agnTAG definitions
 */
public interface TagDao {
	TagDefinition getTag(int companyID, String name);

	Set<String> extractDeprecatedTags(int companyID, Set<String> tagNames);
	
	List<TagDefinition> getTagDefinitions(int companyID);

	Map<String, TagDefinition> getTagDefinitionsMap(int companyID);

	Map<String, String> getSelectValues(int companyID);

	boolean deleteTagsByCompany(int companyId);

}
