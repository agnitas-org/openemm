/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.TagDefinition;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Dao for tag_tbl which contains the agnTAG definitions
 */
public interface TagDao {
	TagDefinition getTag(@VelocityCheck int companyID, String name);

	Set<String> extractDeprecatedTags(@VelocityCheck int companyID, Set<String> tagNames);
	
	List<TagDefinition> getTagDefinitions(@VelocityCheck int companyID);

	Map<String, TagDefinition> getTagDefinitionsMap(@VelocityCheck int companyID);

	Map<String, String> getSelectValues(@VelocityCheck int companyID);

	boolean deleteTagsByCompany(@VelocityCheck int companyId);

	@Deprecated /* Used by unit tests only. */
	int insertTag(String tagName, String tagSelectValue, String tagType, int companyId, String tagDescription);
}
